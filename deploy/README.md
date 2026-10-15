# deploy —— 部署资产与运维文档索引

> **这个目录是什么**：博客上云的全部部署资产（Ansible + GitHub Actions + Docker Compose）和运维文档。
> 代码部署走 CI/CD，主机配置走 Ansible，出事了查下面的文档。

## 文档导航

### 上线这条线（按顺序读）

| 文档 | 一句话说明 |
|---|---|
| [部署方案.md](部署方案.md) | **v1 主文档**：环境准备（WSL/Ansible/CentOS 8 修源）、GitHub Actions CI/CD、Secrets、首次部署、回滚、踩坑清单 |
| [新版部署方案.md](新版部署方案.md) | **v2 增量**：MySQL 从「借住另一个项目的容器」拆成博客自有容器（myblog-mysql），部署与别的项目彻底解耦；v1 未变的部分照旧 |

### 运行期运维

| 文档 | 一句话说明 |
|---|---|
| [SSH安全加固记录.md](SSH安全加固记录.md) | 安全事件处置：仓库泄露 IP 后实测被爆破 1599 次 → 关密码登录、全仓库脱敏 |
| [备份与恢复手册.md](备份与恢复手册.md) | 数据库每日备份（服务器本地 + OSS 双副本）、备份账号无删除权限、完整恢复演练步骤 |
| [日志方案.md](日志方案.md) | Dozzle 看容器日志 + journalctl 看系统日志；诚实清单：这套日志能干什么、不能干什么 |
| [Zabbix监控方案.md](Zabbix监控方案.md) | WSL2 起 Zabbix Server + 云服务器 Agent + SSH 隧道（监控端口不对公网开放），博客专属监控与邮件告警 |

### 练习环境（生产上不能做的，在本地虚拟机练）

| 文档 | 一句话说明 |
|---|---|
| [运维练习方案.md](运维练习方案.md) | 11 个模块的完整练习路线：多机 Ansible、Prometheus、告警、自建 CI/CD、故障演练等 |

> 相关但不在本目录：`运维实习简历-项目经历.md` 在仓库根目录，属个人求职材料，已加 .gitignore **不进公开仓库**。

## 部署资产

```
deploy/
├── README.md                    ← 本文
├── 部署方案.md                   ← v1（环境准备、Secrets、回滚仍然有效）
├── 新版部署方案.md               ← v2（数据库独立版）
├── SSH安全加固记录.md
├── 备份与恢复手册.md
├── 日志方案.md
├── Zabbix监控方案.md
├── 运维练习方案.md
├── docker-compose.yml           ← 四个容器：myblog-nginx / myblog-backend / myblog-mysql / myblog-redis
├── backend/Dockerfile           ← 后端镜像（多阶段构建）
├── nginx/default.conf           ← 前台反代配置
├── rollback.sh                  ← 一键回滚上一版
├── backup.sh                    ← 数据库每日备份（本地 + OSS 双副本）
├── backup-restore-check.sh      ← 备份完整性检查
└── ansible/
    ├── site.yml                 ← 入口 playbook
    ├── ansible.cfg
    ├── inventory.ini            ← ★ 含 IP/用户名，已 gitignore，只提交 example
    ├── inventory.ini.example    ← 模板：复制改名后填真实 IP
    ├── group_vars/all/          ← main.yml（变量）+ vault.yml（ansible-vault 加密）
    └── roles/                   ← docker / journald / myblog-backup / myblog-init
```

## 日常操作速查

| 场景 | 怎么做 | 详见 |
|---|---|---|
| 发版 | push 到 main，GitHub Actions 自动部署 | 部署方案.md |
| 出问题回滚 | 服务器上跑 `deploy/rollback.sh` | 部署方案.md §8 |
| 看日志 | 浏览器开 Dozzle；系统问题用 `journalctl` | 日志方案.md |
| 查备份 | `deploy/backup-restore-check.sh`；告警邮件没来就是备份断了 | 备份与恢复手册.md |
| 整机迁移/恢复 | 按手册从 OSS 拉备份重导 | 备份与恢复手册.md §恢复 |

## 安全红线（提交前必看）

1. **严禁提交**：`inventory.ini`、一切 `.env`、`vault.yml` 明文、服务器 IP/密码/域名。前两类已进 `.gitignore`，不要用 `git add -f` 绕过。
2. **新写文档先脱敏再提交**。核对命令（应无输出）：

   ```powershell
   Select-String -Path deploy\*.md -Pattern '\b(?:\d{1,3}\.){3}\d{1,3}\b' |
     Where-Object { $_.Line -notmatch '127\.0\.0\.1|0\.0\.0\.0|10\.0\.|172\.(1[6-9]|2\d|3[01])\.|192\.168\.|8\.8\.8\.8|114\.114|<服务器IP>|xxx\.xxx\.xxx\.xxx' }
   ```

3. 本目录整体会被同步到服务器 `/opt` 相关位置——**私人材料（简历、求职笔记）不要放进来**。

---
*索引创建：2026-10-12。文档正文各自带「文档版本」脚注，以正文为准。*
