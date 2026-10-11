#!/usr/bin/env bash
# ===========================================================================
# 恢复演练 —— 证明备份真的能恢复，而不只是"文件传上去了"
#
# 详细说明见 deploy/备份与恢复手册.md
#
# 【一句话】把 OSS 上最新的备份拉回来，导进一个临时库，逐表对数，然后销毁。
#
# 【为什么要做】只备份、不验证恢复 = 没有备份。
#   备份里的问题（导出了空文件、表结构不全、gzip 截断、上传中断）
#   平时完全看不出来，只有真正恢复一次才会暴露 —— 而那时候已经晚了。
#
# 【★ 两处刻意的设计】
#   1. 从 OSS 拉，而不是从本地拉
#      → 顺带验证了【异地副本真的取得回来】。灾难发生时你只能靠它。
#   2. 导进临时库，不碰生产库
#      → 全程对 myblog 只读。这台机器只剩 468M 内存，
#        单独再起一个 MySQL 容器做演练是撑不住的，所以在现有容器里建临时库。
#
# 用法（在云服务器上）：bash /opt/myblog/backup-restore-check.sh
# ===========================================================================
set -euo pipefail

BACKUP_DIR=/opt/myblog/backup
OSSUTIL_CONF="$BACKUP_DIR/ossutil.conf"
ENV_FILE=/opt/myblog/.env
DB_CONTAINER=myblog-mysql
TMP_DB=myblog_restore_check

# 备份桶名：优先用环境变量；没设就去问 systemd 单元（这样手工跑也不用记）
if [ -z "${OSS_BACKUP_BUCKET:-}" ]; then
  OSS_BACKUP_BUCKET=$(systemctl show -p Environment myblog-backup.service 2>/dev/null \
    | sed -n 's/.*OSS_BACKUP_BUCKET=\([^ ]*\).*/\1/p')
fi
: "${OSS_BACKUP_BUCKET:?没拿到备份桶名 —— 先确认 myblog-backup.service 已经由 Ansible 投递}"
OSS_BACKUP_PREFIX="${OSS_BACKUP_PREFIX:-daily}"

ok()   { echo "✅ $*"; }
info() { echo "   $*"; }
die()  { echo "❌ $*" >&2; exit 1; }

[ -r "$OSSUTIL_CONF" ] || die "读不到 $OSSUTIL_CONF"
[ -r "$ENV_FILE" ]     || die "读不到 $ENV_FILE"

DB_NAME=$(sed -n 's/^MYSQL_DATABASE=//p' "$ENV_FILE" | head -n1)
[ -n "$DB_NAME" ] || die ".env 里没有 MYSQL_DATABASE"

# 所有查询都走这个函数：-N 去掉表头、-B 用制表符分隔，方便直接比较
# SQL 从 stdin 进来，不经过命令行 —— 和其他地方一样，不往 ps 里留痕
q() { docker exec -i "$DB_CONTAINER" sh -c 'exec mysql -uroot -p"$MYSQL_ROOT_PASSWORD" -N -B'; }

# 和 backup.sh 里同一个道理：-c 放在子命令之前的全局选项位置
oss() { ossutil -c "$OSSUTIL_CONF" "$@"; }

TMP_DIR=$(mktemp -d)
created_tmp_db=0

cleanup() {
  # 无论成功、失败还是中途被打断，临时库和临时文件都要清掉：
  # 留着会污染下一次演练，也白占那 40G 的盘
  if [ "$created_tmp_db" = "1" ]; then
    echo "DROP DATABASE IF EXISTS \`$TMP_DB\`;" | q >/dev/null 2>&1 || true
  fi
  rm -rf "$TMP_DIR"
}
trap cleanup EXIT

# 数一遍某个库里所有表的总行数。
# 用法：count_rows <库名> <表名列表>
# 生产库要数两次（演练前后）—— 两次一致才说明演练确实没碰它。
count_rows() {
  local schema=$1 tables=$2 t total=0 n
  for t in $tables; do
    n=$(echo "SELECT COUNT(*) FROM \`$schema\`.\`$t\`;" | q)
    total=$((total + n))
  done
  echo "$total"
}

# ---------------------------------------------------------------------------
# 1. 从 OSS 找最新的备份
# ---------------------------------------------------------------------------
info "在 oss://$OSS_BACKUP_BUCKET/$OSS_BACKUP_PREFIX/ 里找最新的备份…"

# 文件名带时间戳，所以字典序 = 时间序，直接 sort | tail 就是最新那份
LATEST=$(oss ls "oss://${OSS_BACKUP_BUCKET}/${OSS_BACKUP_PREFIX}/" 2>/dev/null \
  | awk '{print $NF}' \
  | grep -E '\.sql\.gz$' \
  | sort \
  | tail -n1 || true)

[ -n "$LATEST" ] || die "OSS 上一份备份都没有 —— 先确认 myblog-backup.timer 跑过、看 journalctl -u myblog-backup"
ok "找到：$(basename "$LATEST")"

# ---------------------------------------------------------------------------
# 2. 拉下来并解压
# ---------------------------------------------------------------------------
info "下载到本地临时目录…"
oss cp -f "$LATEST" "$TMP_DIR/restore.sql.gz" >/dev/null || die "从 OSS 下载失败"

gzip -t "$TMP_DIR/restore.sql.gz" || die "下载下来的文件 gzip 校验不过 —— 异地副本可能损坏了"
gzip -dc "$TMP_DIR/restore.sql.gz" > "$TMP_DIR/restore.sql"
ok "下载并解压完成（解开后 $(du -h "$TMP_DIR/restore.sql" | cut -f1)）"

# ---------------------------------------------------------------------------
# 3. 建临时库并导入
# ---------------------------------------------------------------------------
# || true 是为了不让 set -e 抢在下面那句清楚的报错前面把脚本杀掉
PROD_TABLES=$(echo "SELECT table_name FROM information_schema.tables WHERE table_schema='$DB_NAME' ORDER BY table_name;" | q || true)
[ -n "$PROD_TABLES" ] || die "查不到生产库 $DB_NAME 的表 —— 容器没起来？库名不对？先手工试一下 mysql 连不连得上"
PROD_ROWS_BEFORE=$(count_rows "$DB_NAME" "$PROD_TABLES")
info "生产库：$(echo "$PROD_TABLES" | wc -l) 张表、共 $PROD_ROWS_BEFORE 行（演练期间不会改动它）"

info "建临时库 $TMP_DB 并导入…"
echo "DROP DATABASE IF EXISTS \`$TMP_DB\`; CREATE DATABASE \`$TMP_DB\` CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;" | q
created_tmp_db=1

# ★ 能导进 $TMP_DB，全靠备份时【没有】加 --databases：
#   那样文件里才没有写死的 CREATE DATABASE / USE 语句。
# ★ 但反过来说，文件里也就【没有 USE】—— 所以这里必须自己补一条，
#   否则 mysql 不知道往哪个库里导，第一条 CREATE TABLE 就报
#   "ERROR 1046 (3D000): No database selected"。（这个坑真踩过。）
#   如果报的换成 "Unknown database 'myblog'"，那是另一种错 ——
#   说明备份那头【加】了 --databases。
#   （外键检查不用我们操心，mysqldump 的输出里自己带了 SET FOREIGN_KEY_CHECKS=0）
{ echo "USE \`$TMP_DB\`;"; cat "$TMP_DIR/restore.sql"; } | q >/dev/null \
  || die "导入失败 —— 备份文件本身可能有问题（这正是演练要抓的东西）"
ok "导入完成"

# ---------------------------------------------------------------------------
# 4. 逐表对数
# ---------------------------------------------------------------------------
TMP_TABLES=$(echo "SELECT table_name FROM information_schema.tables WHERE table_schema='$TMP_DB' ORDER BY table_name;" | q || true)

if [ "$PROD_TABLES" != "$TMP_TABLES" ]; then
  echo "--- 生产库表清单 ---"; echo "$PROD_TABLES"
  echo "--- 恢复库表清单 ---"; echo "$TMP_TABLES"
  die "表清单不一致 —— 备份不完整"
fi
ok "表清单一致（共 $(echo "$PROD_TABLES" | wc -l) 张）"

mismatch=0
for t in $PROD_TABLES; do
  p=$(echo "SELECT COUNT(*) FROM \`$DB_NAME\`.\`$t\`;" | q)
  r=$(echo "SELECT COUNT(*) FROM \`$TMP_DB\`.\`$t\`;" | q)
  if [ "$p" != "$r" ]; then
    echo "❌ 表 $t：生产库 $p 行，恢复出来 $r 行"
    mismatch=1
  fi
done
[ "$mismatch" = "0" ] || die "有表的行数对不上 —— 这份备份不可信"

TMP_ROWS=$(count_rows "$TMP_DB" "$TMP_TABLES")
ok "逐表行数全部一致（共 $TMP_ROWS 行）"

# ---------------------------------------------------------------------------
# 5. 回头看生产库有没有被动过
# ---------------------------------------------------------------------------
PROD_ROWS_AFTER=$(count_rows "$DB_NAME" "$PROD_TABLES")
[ "$PROD_ROWS_AFTER" = "$PROD_ROWS_BEFORE" ] \
  || die "⚠️ 生产库行数变了：演练前 $PROD_ROWS_BEFORE、演练后 $PROD_ROWS_AFTER —— 演练脚本有问题，立刻查！"
ok "生产库全程未被改动（$PROD_ROWS_AFTER 行，与演练前一致）"

echo
ok "恢复演练通过：$(basename "$LATEST") 可以真正恢复出完整数据"
info "临时库 $TMP_DB 和临时文件将在退出时自动清理"
