#!/usr/bin/env bash
# ===========================================================================
# 博客数据库每日备份
#
# 详细说明见 deploy/备份与恢复手册.md
#
# 【跑在哪】云服务器上，由 systemd timer 每天 03:17 触发：
#     /etc/systemd/system/myblog-backup.timer      ← 定时器（Ansible 投递）
#     /etc/systemd/system/myblog-backup.service    ← 执行本脚本
#     /opt/myblog/backup.sh                        ← 本文件（CI 随每次 push 更新，同 rollback.sh）
#
# 【产出两份副本】
#     副本② 本地   /opt/myblog/backup/myblog-YYYYmmdd-HHMM.sql.gz    保留 7 天
#     副本③ 异地   oss://<备份桶>/daily/同名文件                      保留 30 天
#
# 【★ 为什么异地那份的过期清理不在这里做】
#   备份用的 RAM 账号故意【没有 oss:DeleteObject 权限】。
#   也就是说：本脚本删不掉 OSS 上的任何东西 —— 这是「防篡改备份」。
#   即使这台机器的凭据泄漏、或脚本把路径写错，历史备份也清不掉。
#   过期清理由【桶的生命周期规则】在服务端做，用的不是这个账号的权限。
#
# 【退出码】0 = 成功；非 0 = 失败（systemd 会记成一次服务失败，一眼看得见）
# ===========================================================================
set -euo pipefail

BACKUP_DIR=/opt/myblog/backup          # 副本②的落点，也是凭据所在目录
OSSUTIL_CONF="$BACKUP_DIR/ossutil.conf"
STAMP_FILE="$BACKUP_DIR/.last_success" # ★ 心跳监控盯的就是它的 mtime（见手册）
ENV_FILE=/opt/myblog/.env

DB_CONTAINER=myblog-mysql
KEEP_DAYS=7        # 本地留 7 天；再多会慢慢吃掉那块 40G 的盘
MIN_BYTES=1024     # 小于 1KB 的"备份"几乎肯定是导出失败了
MIN_FREE_KB=524288 # 可用空间低于 512MB 就别硬着头皮备份了

# 备份桶名由 systemd 单元通过 Environment= 传进来（它不是密码，但放在单元里
# 能做到"改桶名不用改脚本"）。单元文件见 roles/myblog-backup/templates/。
: "${OSS_BACKUP_BUCKET:?未设置 OSS_BACKUP_BUCKET —— 请通过 systemctl start myblog-backup.service 运行，或手工 export 后再跑}"
OSS_BACKUP_PREFIX="${OSS_BACKUP_PREFIX:-daily}"

# 结构化事件日志：每行一个 event=xxx 键值对。
# 不用花哨格式，就为了 journalctl 里一眼看出时间线：
#   journalctl -u myblog-backup --no-pager | grep '^.*event='
log() { echo "event=$*"; }
fail() { log "backup_fail reason=$*"; exit 1; }

# ossutil 统一走这个包装，把 -c 放在【子命令之前】的全局选项位置。
# 原因：ossutil 是 Go 写的，子命令后面的参数一旦被当成位置参数，
# 就会报 "too many arguments" —— 位置放前面最不容易踩坑。
oss() { ossutil -c "$OSSUTIL_CONF" "$@"; }

# ---------------------------------------------------------------------------
# 0. 前置检查 —— 宁可什么都不做，也不要产出一个"看起来成功"的假备份
# ---------------------------------------------------------------------------
[ -r "$OSSUTIL_CONF" ] || fail "no_ossutil_conf path=$OSSUTIL_CONF"
[ -r "$ENV_FILE" ] || fail "no_env_file path=$ENV_FILE"

# 库名从 .env 取，保持单一事实来源（不要在这里硬编码一遍）
DB_NAME=$(sed -n 's/^MYSQL_DATABASE=//p' "$ENV_FILE" | head -n1)
[ -n "$DB_NAME" ] || fail "no_db_name_in_env"

# 先确保目录在：下面要靠 df 量它所在文件系统的剩余空间，
# 目录不存在的话 df 会直接失败（而且报错离病因很远）
mkdir -p "$BACKUP_DIR"

# 磁盘预检：这台机器只有 40G，写满了 backup 会失败得很隐蔽
avail_kb=$(df -Pk "$BACKUP_DIR" | awk 'NR==2 {print $4}')
[ "$avail_kb" -ge "$MIN_FREE_KB" ] || fail "low_disk avail_kb=$avail_kb"

STAMP=$(date +%Y%m%d-%H%M)
OUT="$BACKUP_DIR/${DB_NAME}-${STAMP}.sql.gz"

log "backup_start db=$DB_NAME out=$(basename "$OUT")"

# ---------------------------------------------------------------------------
# 1. 导出 + 压缩（本地落盘 = 副本②）
# ---------------------------------------------------------------------------
# ★ 密码走【容器自己的环境变量】，不经过命令行 ——
#   不会出现在宿主机 ps 的输出里，也不会留在 shell history 里。
# ★ --single-transaction：InnoDB 一致性快照，导出期间不锁表。
# ★ 【故意不加】--databases：
#   加了以后文件里会带 CREATE DATABASE / USE myblog，
#   恢复演练就只能覆盖生产库、导不进临时库（见 backup-restore-check.sh）。
if ! docker exec "$DB_CONTAINER" sh -c \
      'exec mysqldump -uroot -p"$MYSQL_ROOT_PASSWORD" \
         --single-transaction --routines --triggers \
         --default-character-set=utf8mb4 "$1"' \
      sh "$DB_NAME" | gzip > "$OUT"; then
  rm -f "$OUT"          # 别留半个残文件在这里冒充备份
  fail "dump_failed container=$DB_CONTAINER"
fi

# ---------------------------------------------------------------------------
# 2. 校验 —— 确认拿到的是个真备份，不是空壳
# ---------------------------------------------------------------------------
# ★ 少了这一步，"导出失败但脚本继续跑" 就会把空文件传上 OSS，
#   而你会在几个月后真正要用它的时候才发现 —— 这是备份里最危险的假象。
gzip -t "$OUT" || { rm -f "$OUT"; fail "gzip_test_failed file=$(basename "$OUT")"; }

size=$(stat -c %s "$OUT")
[ "$size" -ge "$MIN_BYTES" ] || { rm -f "$OUT"; fail "too_small size=$size"; }

# 光看大小不够 —— 万一导出的是一个没有表的空库呢？数一下 CREATE TABLE
tables=$(gzip -dc "$OUT" | grep -c '^CREATE TABLE' || true)
[ "$tables" -gt 0 ] || { rm -f "$OUT"; fail "no_table_in_dump"; }

log "dump_ok size=$size tables=$tables"

# ---------------------------------------------------------------------------
# 3. 上传 OSS（副本③）
# ---------------------------------------------------------------------------
if ! oss cp -f "$OUT" "oss://${OSS_BACKUP_BUCKET}/${OSS_BACKUP_PREFIX}/$(basename "$OUT")" >/dev/null; then
  # ⚠️ 本地那份【保留】。上传失败不影响本地副本已经成立，
  #    明天还会再传一次 —— 没必要因为它把今天的本地备份也删掉。
  fail "oss_upload_failed bucket=$OSS_BACKUP_BUCKET"
fi

log "oss_upload_ok"

# ---------------------------------------------------------------------------
# 4. 清理过期的本地备份（只删本地！OSS 那边无权删、也不该删）
# ---------------------------------------------------------------------------
find "$BACKUP_DIR" -maxdepth 1 -name "${DB_NAME}-*.sql.gz" -type f \
     -mtime +"$KEEP_DAYS" -delete

# ---------------------------------------------------------------------------
# 5. 心跳 —— 放在最后
# ---------------------------------------------------------------------------
# ★ 只在整个链路【全部成功】之后才 touch。
#   监控盯的是这个文件的时间戳，语义是"最后一次成功备份是什么时候"，
#   而不是"脚本最后一次跑是什么时候"。
#   最危险的情况不是脚本报错，而是脚本压根没跑 —— 那时没有任何错误可抓，
#   只有这个时间戳能暴露问题（见手册「心跳监控」一节）。
touch "$STAMP_FILE"

log "backup_ok file=$(basename "$OUT") size=$size tables=$tables"
