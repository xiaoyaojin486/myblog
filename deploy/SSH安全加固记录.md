# SSH 安全加固记录

> **日期**：2026-10-11
> **对象**：阿里云 ECS（CentOS 8），博客 `myblog` 的生产服务器
> ⚠️ **本文档不含服务器 IP、密钥、密码** —— 仓库是公开的，一律用 `<服务器IP>` 占位。

---

## 1. 一句话总结

把一个「**公网 IP + 用户名 root + 认密码**」的组合，改成「**只认密钥**」。

改动只有两行，5 分钟做完，但把一扇每天被敲 230 次的门焊死了。

---

## 2. 起因：怎么发现的

这事不是专门去查出来的，是**顺手扫出来的** —— 值得记一下这个"顺手"。

```
在做「Zabbix 心跳文件的权限问题」时，
   ↓
按惯例扫了一遍仓库里有没有服务器 IP
   ↓
发现 deploy/ansible/inventory.ini 和 deploy/部署方案.md 里都有公网 IP，
而且【都已经提交进仓库了】
   ↓
那仓库是公开的还是私有的？
   ↓
公开的。
   ↓
既然 IP 已经公开，那 SSH 那头是什么状态？
   ↓
查配置：passwordauthentication yes、permitrootlogin yes
   ↓
查日志：一周内 1599 次密码尝试失败
   ↓
决定：先关掉密码登录（最紧急），IP 脱敏排后面（见第 8 节）
```

**关键教训**：`inventory.ini` 里写的不只是 IP，还有 `ansible_user=root` —— 等于告诉别人**用哪个用户名来撞**。

---

## 3. 风险到底是什么（讲清楚，别自己吓自己）

### 3.1 公网 IP 本身不是秘密

整个互联网**每天都在被扫描**。任何一台开着 22 端口的公网机器，日志里都有大量陌生 IP 在敲门。**"IP 被知道"这件事，本身不构成风险。**

### 3.2 真正的风险是三件事叠在一起

| # | 条件 | 本机情况 |
|---|---|---|
| 1 | 公网 IP 被公开 | ❌ 公开仓库里就有 |
| 2 | 用户名也被公开 | ❌ `ansible_user=root` |
| 3 | **SSH 认密码** | ❌ `passwordauthentication yes` |

**三个全中，这才是问题。** 网上有大量机器人专门爬 GitHub，找的就是这种组合 —— 拿到"IP + 用户名"，然后拿常见密码字典去撞。

### 3.3 实测数据

```bash
journalctl -u sshd --since '7 days ago' | grep -c 'Failed password'
# → 1599
```

**一周 1599 次 ≈ 每天 230 次。** 这不是"可能有人来"，是**一直在发生**。

> `/var/log/secure` 里只数到 56 条 —— 那是因为它刚被 logrotate 轮转过，只留了最近一小段。**以 journalctl 的 1599 为准。**

### 3.4 撞不开 ≠ 没问题

1599 次失败就是 1599 次没成功。但：

- 这是**每天都在减小的安全余量** —— 只要密码不是足够强，早晚有一天会中
- 密码这条路还有个根本缺陷：**它可以被猜到、被撞开、被钓鱼、被从别的网站泄露的库里面复用**。密钥不能。
- 而且一旦撞中，拿到的是 **root** —— 等于整台机器

### 3.5 结论

> **花 5 分钟，换掉"每天 230 次的门被敲"，没有理由不做。**

---

## 4. 改了什么

### 4.1 改动内容

`/etc/ssh/sshd_config` 里两行：

```diff
- #PermitRootLogin yes                 ← 默认值（注释状态），允许 root 用任何方式登录
+ PermitRootLogin prohibit-password    ← root 可以登，但【只认密钥】

- #PasswordAuthentication yes          ← 默认值（注释状态），SSH 接受密码
+ PasswordAuthentication no            ← 关掉密码这条路
```

#### ★ 为什么是 `prohibit-password` 而不是 `no`

| 写法 | 含义 | 后果 |
|---|---|---|
| `no` | root **完全不能**通过 SSH 登录 | ⚠️ 这台机器**只有 root 一个账号**，写 `no` 会把**你自己**也挡在门外 |
| `prohibit-password` | root 可以登，但只接受密钥 | ✅ 正是我们要的 |

> 如果服务器上还有别的普通账号（能 `sudo`），`PermitRootLogin no` 才是更彻底的做法。**这里不行**，因为只有 root。

#### `sshd -T` 打印的是 `without-password`，不是 `prohibit-password`

CentOS 8 自带 OpenSSH 8.0，它打印的是**老名字** `without-password`。两者**完全等价**，不是写错了。

```bash
sshd -T | grep -Ei '^(passwordauthentication|permitrootlogin)'
# permitrootlogin without-password     ← 就是 prohibit-password
# passwordauthentication no
```

### 4.2 实际操作步骤（**每一步都不能省**）

> ★ **铁律：整个过程不要关掉你当前那条 SSH 会话。** 它是你的安全绳 —— 新配置进不去时，还能用它回滚。

```bash
# ── ① 先在另一个窗口证明【密钥能登录】────────────────────────
#    这一步不能跳，否则可能把自己关在门外。
#    在 WSL 里【新开一个窗口】跑：
ssh -i ~/.ssh/myblog_deploy -o PreferredAuthentications=publickey \
    -o BatchMode=yes root@<服务器IP> 'echo 密钥登录 OK'
#    PreferredAuthentications=publickey → 强制只用密钥，不给密码留机会
#    能打出「密钥登录 OK」才往下走


# ── ② 先看有没有别的文件在覆盖配置 ────────────────────────────
grep -rn 'PasswordAuthentication\|PermitRootLogin' \
    /etc/ssh/sshd_config /etc/ssh/sshd_config.d/ 2>/dev/null


# ── ③ 备份 + 改 ──────────────────────────────────────────────
cp /etc/ssh/sshd_config /etc/ssh/sshd_config.bak-$(date +%F)

sed -i 's|^#\?PermitRootLogin.*|PermitRootLogin prohibit-password|' /etc/ssh/sshd_config
sed -i 's|^#\?PasswordAuthentication.*|PasswordAuthentication no|' /etc/ssh/sshd_config

#    ★ 为什么 sed 里要写 ^#\? ：
#      默认配置里这两行是【注释掉】的（#PermitRootLogin yes），
#      不匹配 # 就替换不到，改了等于没改。


# ── ④ ★ 先验语法，再重载 ─────────────────────────────────────
sshd -t && echo "✅ 语法 OK"
systemctl reload sshd

#    ★★ sshd -t 这一步绝对不能省：
#       配置文件写错的话，reload 之后 SSH 就起不来了 ——
#       而你当前这条连接一旦断开，就【再也连不上】。
#       先验一次语法，把这种情况挡在前面。

#    ★ 为什么用 reload 而不是 restart：
#       reload  = 主进程重读配置，【已建立的连接不受影响】← 你的安全绳还活着
#       restart = 杀掉所有连接，包括你自己那条 ← 千万别用


# ── ⑤ ★ 再【新开一个窗口】测密钥登录 ──────────────────────────
#    必须是一条全新的连接。
#    因为窗口 A 那条连接是在改配置【之前】建立的，reload 不会踢它 ——
#    所以它一直能用这件事，恰恰证明不了新配置是对的。


# ── ⑥ 确认最终状态 ──────────────────────────────────────────
sshd -T | grep -Ei '^(passwordauthentication|permitrootlogin)'
```

### 4.3 本次执行的备份文件

```
/etc/ssh/sshd_config.bak-2026-10-11
```

**留着别删** —— 这是回滚用的（见下一节）。

---

## 5. ★ 保底方案：万一出事了怎么办

按严重程度分三层，**从轻到重**依次尝试。

### 5.1 第一层：当前 SSH 会话还活着 → 直接回滚

只要窗口 A 那条连接没断，一条命令就能退回去：

```bash
cp /etc/ssh/sshd_config.bak-2026-10-11 /etc/ssh/sshd_config
sshd -t && systemctl reload sshd
```

> 这也是为什么"整个过程别关当前会话"是铁律 —— **它在，就永远有退路。**

### 5.2 第二层：SSH 彻底进不去了 → 走阿里云控制台 VNC

**VNC 不走 SSH、不用密钥**，所以 SSH 配置改坏了它照样能进。

**路径**：阿里云控制台 → 云服务器 ECS → 实例 → 找到这台机器 → **远程连接（Remote Connect）→ VNC**

登进去之后，你面对的就是服务器的命令行，可以：

- 把备份的配置拷回去：`cp /etc/ssh/sshd_config.bak-2026-10-11 /etc/ssh/sshd_config`
- 或者手工把 `PasswordAuthentication` 临时打开，进去加完钥匙再关掉
- 或者往 `~/.ssh/authorized_keys` 里补一行新的公钥

> ⚠️ **VNC 登录用的是「实例密码」**（也就是你原来 SSH 用的那个 root 密码）。
> **所以动手改配置之前，先确认你能通过 VNC 进去一次** —— 那才是真正独立于 SSH 的退路。
> 光靠"窗口 A 还连着"不算安全网：WSL 一关机、网络一断，它就没了。

### 5.3 第三层：密钥丢了 / 换机器了 → 补一把新钥匙

**在能进去的机器上**（旧机器、或 VNC）操作：

```bash
# ① 在新机器上生成一把新钥匙（只生成，不动别的）
ssh-keygen -t ed25519 -f ~/.ssh/myblog_deploy -C "myblog@新机器"
cat ~/.ssh/myblog_deploy.pub          # 把这一行复制出来

# ② 在【能进去的机器】上，把公钥追加到服务器的 authorized_keys
echo 'ssh-ed25519 AAAA...（刚才那行）' >> ~/.ssh/authorized_keys
chmod 600 ~/.ssh/authorized_keys
```

新机器就能连了。

> **这叫「发一把钥匙」，而不是「复制一把万能钥匙」。**
> 每台机器一把，丢了一台只吊销那一把。不用了就从 `authorized_keys` 里删掉那一行。

### 5.4 特殊情况：那台是"别人的电脑"

学校机房、同学的本子 —— **千万不要把私钥拷过去**，那等于把钥匙留在别人机器上。

正确做法：**在那台机器上生成一把临时钥匙 → 用完把服务器上对应的那行公钥删掉**。
机器还回去时，留在那儿的钥匙已经没用了。

### 5.5 回滚总表

| 症状 | 走哪一层 |
|---|---|
| 新连接连不上，但旧会话还在 | **5.1**（直接回滚） |
| 所有 SSH 都进不去 | **5.2**（VNC） |
| 能进，但想换台机器 / 密钥丢了 | **5.3**（加新公钥） |
| 在别人电脑上临时用一下 | **5.4**（临时钥匙，用完删） |

---

## 6. 改完之后的行为变化

| 你怎么连 | 结果 |
|---|---|
| `ssh -i ~/.ssh/myblog_deploy root@<服务器IP>` | ✅ **照常，完全没影响** |
| `ssh root@<服务器IP>`（没带 `-i`） | ❌ 连不上（见 6.2） |
| 输入密码 | ❌ **明确拒绝** |

### 6.1 用密码登会长什么样

```
$ ssh root@<服务器IP>
root@<服务器IP>: Permission denied (publickey).
```

**重点是括号里那串：只剩 `publickey` 了。**

那个括号就是「服务器还允许的认证方式」列表。密码不在里面，所以客户端**根本不会弹密码提示** —— 连"输入密码"这一步都不给你，直接失败。

> ★ 小技巧：以后在任何机器上看到 `Permission denied (...)`，**先读括号**。
> `(publickey,password)` = 两种都还开着；`(publickey)` = 只剩密钥了。

### 6.2 裸命令为什么也不行了

密钥叫 `myblog_deploy`，不是默认名。不带 `-i` 时 SSH 只找 `~/.ssh/id_rsa`、`id_ed25519` 这些默认名 —— 找不到。

**以前**会退到"输密码"（所以还能进）；**现在**退路没了，直接失败。

**解法：配个别名，以后 `ssh myblog` 三个字母搞定**

```bash
# 在 WSL 里
cat >> ~/.ssh/config <<'EOF'

Host myblog
    HostName <服务器IP>
    User root
    Port 22
    IdentityFile ~/.ssh/myblog_deploy
EOF
chmod 600 ~/.ssh/config
```

### 6.3 那些"自动化的东西"会断吗？—— 都不会

| 谁 | 用什么登录 | 影响 |
|---|---|---|
| Ansible | `ansible_ssh_private_key_file`（密钥） | ❌ 无 |
| GitHub Actions 部署 | `secrets.SERVER_SSH_KEY`（密钥） | ❌ 无 |
| 手工 ssh | 密钥 | ❌ 无 |

**它们本来就全走密钥** —— 这次改动只砍掉了"密码"这条从来没人用的路。

> 这也是为什么这件事**风险很小、收益很大**：动的是没人走的那条路。

---

## 7. 怎么确认真的生效了

### 7.1 看配置

```bash
sshd -T | grep -Ei '^(passwordauthentication|permitrootlogin)'
# passwordauthentication no
# permitrootlogin without-password
```

### 7.2 故意用密码登一次，看它拒绝

```bash
ssh -o PubkeyAuthentication=no root@<服务器IP>
```

看到 `Permission denied (publickey).` 就对 —— **这是"门关上了"的直接证据，比看配置文件踏实。**

### 7.3 明天回来看：撞击次数应该归零

```bash
journalctl -u sshd --since today | grep -c 'Failed password'
```

**应该掉到接近 0。** 因为服务器现在根本不提供"密码"这个选项 —— 机器人连试的机会都没有，在认证协商那一步就被挡回去了。

> **从每天 230 次，到基本归零** —— 这就是这几分钟换来的东西。

---

## 8. 还没做完的事

### 8.1 确认 VNC 退路（**最优先**）

动手前没做的事，现在补上：

**阿里云控制台 → 云服务器 ECS → 实例 → 远程连接 → VNC → 确认能进到命令行。**

这是唯一**不依赖 SSH** 的退路，必须确认它是通的。

同时：**备份私钥**。

```bash
# ① 给私钥加个密码短语 —— 这样即使文件被拷走，别人也用不了
ssh-keygen -p -f ~/.ssh/myblog_deploy

# ② 打包（注意：加了 passphrase 之后再打包，别反过来）
tar czf ~/myblog_deploy_backup.tgz -C ~/.ssh myblog_deploy
# ③ 把这个 tgz 挪到安全的地方（U 盘 / 你自己的加密盘），别留在服务器上

# ④ 顺便配好 ssh-agent，免得每次输密码短语
#    在 ~/.bashrc 里加：eval "$(ssh-agent -s)" && ssh-add ~/.ssh/myblog_deploy
```

配好别名（见 6.2）之后，日常就是 `ssh myblog`。

### 8.2 仓库止血：把 IP 从仓库里拿掉

**已提交的改不掉**（原因见下），但**不能继续往上加**。

| 文件 | 现状 | 怎么处理 |
|---|---|---|
| `deploy/ansible/inventory.ini` | 已提交，含 IP + root | `git rm --cached` 移出跟踪 → 加进 `.gitignore` → 仓库里只留 `inventory.ini.example`（IP 写成占位符）；本地那份保留 |
| `deploy/部署方案.md` | 已提交，一条报错信息里带 IP | 换成 `<服务器IP>` |
| `Zabbix监控方案.md` | **未跟踪**，十几处 IP | 提交前必须先脱敏 |
| `.claude/settings.json` | 已跟踪，但**未提交的改动里**有 IP | 提交前处理；这个文件本身也该考虑是否要进仓库 |
| `deploy/SSH安全加固记录.md`（本文） | 新增 | 已经是占位符 ✅ |

> ⚠️ **为什么"已提交的改不掉"**：
> 公开仓库里，爬虫（和 GitHub 的公开事件流）**早就抓走了**；别人 fork 的副本你改不掉；`git filter-repo` + force-push 之后，旧 commit 按 SHA **仍然访问得到**，要等 GitHub 那边 GC。
> **代价大、收益小。所以路线是"关门"（第 4 节），不是"改历史"。**

**换 IP 吗？** 关掉密码登录之后：**不用换**。
泄露 IP 的后果已经从"有人在试着撞门"降级成"日志里多几条扫描记录" —— 那是**所有**公网服务器都有的日常。

### 8.3 心跳文件搬到 `status/`（另一个任务，代码已改完）

**背景**：Zabbix Agent 是容器、以非 root（uid 1997）跑，读不到 `/opt/myblog/backup/.last_success`，因为那个目录是 **0700** 且放着 OSS 凭据 —— **卡住的是目录的可执行位，不是文件的读位**。
结果是监控项变成 unsupported → **心跳告警形同虚设，而且是安静地失效**。

**解法**：把"给监控看的公开信号"和"要保密的凭据"从物理上分开。

| 目录 | 权限 | 里面是什么 |
|---|---|---|
| `/opt/myblog/backup/` | `0700` | OSS 凭据、数据库导出 |
| `/opt/myblog/status/` | `0755` | 一个空的 `last_success` |

**代码已改完的 4 个文件**：

- `deploy/ansible/roles/myblog-backup/tasks/main.yml` —— 新增「创建监控状态目录」+ 心跳文件换路径
- `deploy/backup.sh` —— `STATUS_DIR` + `touch` 前补 `mkdir -p`
- `deploy/备份与恢复手册.md` —— 8 处路径 + 新增「为什么挂 status/ 而不是 backup/」整节 + §9 排错表加一行
- `Zabbix监控方案.md` §6.2 —— 挂载改成 `status/`（**提交前先脱敏**）

**服务器上要做的**：

```bash
# ① ★ 先把改过的 ansible 同步到 WSL（ansible 只能从 ~/ansible 跑）
~/sync-ansible.sh
#    注意：别让 Windows 这边覆盖掉 WSL 里的 vault.yml

# ② 重跑 Ansible —— 建 status/ 目录 + 新心跳文件
cd ~/ansible && ansible-playbook site.yml --ask-vault-pass

# ③ 改 agent 挂载并重启
vi /opt/zabbix-agent/docker-compose.yml   # 换成 /opt/myblog/status:/opt/myblog/status:ro
cd /opt/zabbix-agent && docker compose up -d

# ④ 验证 —— 这次不该再 Permission denied
docker exec myblog-zabbix-agent ls -l /opt/myblog/status/last_success

# ⑤ ★ 把时间戳拨回"真实的最后一次成功备份"，别让它假装刚备份过
newest=$(ls -t /opt/myblog/backup/*.sql.gz 2>/dev/null | head -1)
if [ -n "$newest" ]; then touch -r "$newest" /opt/myblog/status/last_success
else                      touch -d '2 days ago' /opt/myblog/status/last_success; fi
```

**网页上还有一步**：Zabbix → 主机 `myblog-server` → 监控项，Key 改成

```
vfs.file.time[/opt/myblog/status/last_success]
```

**等新 `backup.sh` 上线之后**，再手工删掉旧位置那个（**这个不用写进 Ansible** —— 旧文件的唯一来源就是旧版脚本，一次性清理就够，不该让 playbook 长期背一个删除任务）：

```bash
rm -f /opt/myblog/backup/.last_success
```

### 8.4 提交推送

分两个 commit，**最后一次 push**（一次 push 只触发一次部署）：

1. `ci: 只改文档的 push 不再触发部署` —— `.github/workflows/deploy.yml` 加 `paths-ignore: ['**.md', '.claude/**']`
2. `fix(monitor): 心跳文件搬到 status/ —— 修 Agent Permission denied` —— 另外 3 个文件

**推送前的例行检查**：

- `head -n1 deploy/ansible/group_vars/all/vault.yml` 应仍是 `$ANSIBLE_VAULT;1.1;AES256`
- `grep -rn '8\.134' .` 之类扫一遍，确认没有新的 IP 泄露点
- 用具体路径 `git add`，**不要用 `git add -A`**

---

## 9. 这次学到什么

### 9.1 「默认配置」不等于「安全配置」

`sshd_config` 里那两行**默认就是 `yes`**。默认值是为了"开箱即用"，不是为了"安全"。
**凡是"默认允许"的东西，都值得问一句：这个默认值是谁选的？为谁选的？**

### 9.2 公开仓库里"写什么"要有意识

`inventory.ini` 是很自然就写进去的 —— 里面就一行，谁会多想？
但它同时暴露了 **IP + 用户名 + 端口 + 密钥文件名**。

> 规矩：**仓库是公开的 ⇒ 任何"具体指向某台真实机器"的东西都要占位符化。**
> IP、内网拓扑、账号名、密钥路径，都算。

### 9.3 密钥 和 密码 不是同一种东西

| | 密码 | 密钥 |
|---|---|---|
| 能不能被猜到 | 能（字典） | 不能（2048+ 位随机） |
| 能不能被撞开 | **能**（一周被撞 1599 次） | 不现实 |
| 会不会被复用/钓鱼泄露 | **会**（别的网站泄露的库里有） | 不会 |
| 能不能"记住" | 能 ← **这正是它危险的原因** | 不能 ← **这正是它安全的原因** |

**"方便记住"和"容易被猜"是同一件事的两面。** 选 SSH 认证方式时，这个取舍没有中间地带。

### 9.4 排查时先看目录，再看文件

心跳文件那个坑顺带教了一课：`.last_success` 自己是 `0644`、人人可读，但它照样**读不到** —— 因为父目录是 `0700`。

> **`ls -ld` 看目录，`ls -l` 看文件。** 两者都要看。
> 想知道是不是权限问题：`docker exec -u root ...` 能读、不加 `-u root` 不能读 → 锁定身份问题，跟挂载无关。

### 9.5 每改一处关键配置，都要先想好"怎么退回去"

这次做对了三件事，值得固化成习惯：

1. **改之前先备份**（`sshd_config.bak-日期`）
2. **reload 之前先 `-t` 验语法** —— 把"配置写错 + 连接断掉"这个死局挡在前面
3. **改完不关老会话，用新连接验证** —— 老会话是安全绳，新连接才是证据

---

## 附录：命令速查

```bash
# ── 看当前 SSH 认证配置 ──────────────────────────────
sshd -T | grep -Ei '^(passwordauthentication|permitrootlogin|pubkeyauthentication)'

# ── 看被撞得多狠 ────────────────────────────────────
journalctl -u sshd --since '7 days ago' | grep -c 'Failed password'
grep 'Failed password' /var/log/secure | grep -oE 'from [0-9.]+' \
  | sort | uniq -c | sort -rn | head

# ── 改配置的标准流程 ────────────────────────────────
cp /etc/ssh/sshd_config /etc/ssh/sshd_config.bak-$(date +%F)
#   ...改...
sshd -t && systemctl reload sshd      # ★ -t 不能省

# ── 回滚 ────────────────────────────────────────────
cp /etc/ssh/sshd_config.bak-2026-10-11 /etc/ssh/sshd_config
sshd -t && systemctl reload sshd

# ── 验证（在另一台机器 / 另一个窗口）─────────────────
ssh -i ~/.ssh/myblog_deploy -o PreferredAuthentications=publickey \
    -o BatchMode=yes root@<服务器IP> 'echo 密钥登录 OK'
```
