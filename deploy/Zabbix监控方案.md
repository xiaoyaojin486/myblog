# Zabbix 监控方案（WSL2 服务端 + 云服务器 Agent）

> **这份文档是什么**：把 `运维练习方案.md` 里的「模块 5：Zabbix」真正落地。**服务端跑在你电脑的 WSL2 里，Agent 装在云服务器上**，用云服务器上的 Agent 采集博客的运行数据，回传到 WSL2 的 Zabbix 里。
>
> **配套文档**：`部署方案.md`（生产 CI/CD，已完成）、`运维练习方案.md`（完整练习路线）
>
> **前置条件**：博客已在云服务器上正常跑起来（`myblog-nginx` / `myblog-backend` / `myblog-redis` 三个容器健康）

---

## 目录

- [1. 为什么是这个架构](#1-为什么是这个架构)
- [2. 架构与数据流](#2-架构与数据流)
- [3. 内存账](#3-内存账)
- [4. 阶段一：WSL2 装 Docker](#4-阶段一wsl2-装-docker)
- [5. 阶段二：WSL2 起 Zabbix 三件套](#5-阶段二wsl2-起-zabbix-三件套)
- [6. 阶段三：云服务器装 Agent](#6-阶段三云服务器装-agent)
- [7. 阶段四：建 SSH 隧道](#7-阶段四建-ssh-隧道)
- [8. 阶段五：Web 里接入主机](#8-阶段五web-里接入主机)
- [9. 阶段六：加博客专属监控](#9-阶段六加博客专属监控)
- [10. 验收清单](#10-验收清单)
- [11. 排错对照表](#11-排错对照表)
- [12. 常见误解](#12-常见误解)
- [13. 这份经历能写进简历什么](#13-这份经历能写进简历什么)
- [14. 备选方案：如果不是校园网（家宽或固定公网 IP）](#14-备选方案如果不是校园网家宽或固定公网-ip)
- [15. 兜底：笔记本关机时的告警](#15-兜底笔记本关机时的告警)

---

## 1. 为什么是这个架构

### 1.1 最容易搞反的一件事

**Zabbix 里重的是 Server，不是「客户端」。**

| 组件 | 干什么 | 内存 |
|---|---|---|
| **Zabbix Server** | 采集、算触发器、写库、发告警 | **200–600 MB** |
| Zabbix Web | 网页界面，只读写数据库 | ~100 MB |
| MySQL（Zabbix 自己的库） | 存历史数据 | **400 MB+** |
| **Zabbix Agent** | 在被监控机上跑，只负责取值上报 | **~30 MB** |

「客户端」这个词听起来像重的那头，实际上 **Agent 只有 30 MB**。它是个"哑"进程——不会判断「CPU 90% 算不算高」，更不会告警，只会照着指令取一个数交出去。**所有判断、存储、通知都在 Server 那边。**

所以「监控」这件事物理上发生在 Server 所在的机器上，被监控机只提供读数。

### 1.2 两个方案对比

**方案 A：Agent 在 WSL2，Server 在云服务器（❌ 不可行）**

| 问题 | 说明 |
|---|---|
| **内存塞不下** | Server + Web + MySQL ≈ 800 MB~1 GB，云服务器只剩 **468 MiB**，硬上就是把机器挤爆——上面还跑着 `myapp-*` 和你的博客 |
| **NAT 连不通** | 被动模式下 Server 要主动连 Agent 的 10050。WSL2 在你家路由器后面没有公网 IP，**云服务器根本连不进来** |
| **监控对象不对** | 监控的是你自己的笔记本。笔记本一关机、一休眠、IP 一变，监控对象就没了 |

**方案 B：Server 在 WSL2，Agent 在云服务器（✅ 本方案）**

| 好处 | 说明 |
|---|---|
| **内存问题消失** | 重的全在 WSL2（7.1 GiB 可用），云上只多 30 MB |
| **NAT 问题消失** | WSL2 主动连出去，**出方向不受 NAT 限制**；WSL2 自己的 IP 会变，但没有任何东西需要连进 WSL2，所以无所谓 |
| **监控对象对了** | 监控的是真正在生产上跑的东西 |
| **简历能写** | 「在生产服务器部署 Zabbix Agent 并接入监控」 |

**代价只有一个**：你电脑关机时监控停止。对学习和演示完全够用——这也正是它该待在练习环境里的原因。

> **这个代价能补。** 第 15 章给了一个**从服务器外部探测**的办法（站点监控），专治「笔记本关机 = 告警也停了」。

### 1.3 方案 B 的环境实测

**WSL2（Ubuntu 26.04.1 LTS）**

| 项目 | 实测值 |
|---|---|
| 内存 | 7.7 GiB 总量 / **7.1 GiB 可用** |
| Swap | 2.0 GiB |
| CPU | 16 核 |
| 磁盘 | 954 G 可用 |
| systemd | 可用（`degraded` 不影响） |
| Docker | **❌ 没装** ← 阶段一要装 |
| SSH 密钥 | ✅ `~/.ssh/myblog_deploy` 已就位 |
| **网络** | **校园网，自动分配 IP** ← ⚠️ **这条决定了阶段四怎么做，见 1.4** |

**云服务器（阿里云 ECS）**

| 项目 | 实测值 |
|---|---|
| 内存 | 1.7 GiB 总量 / **468 MiB 可用** / Swap 用了 104 MiB |
| CPU | 2 核 |
| 磁盘 | 40 G，已用 22% |
| 系统 | CentOS 8（**已 EOL**，yum 源是死的） |
| 已跑容器 | myblog-nginx(8081)、myblog-backend、myblog-redis、**myapp-mysql、myapp-frontend(80)、myapp-backend(8080)** |
| Docker | ✅ 已装（有加速器配置） |

> **两条实测数据直接决定了方案**：
> - 云服务器只有 **468 MiB 可用** → Server 不可能装在这里
> - 云服务器 **CentOS 8 已 EOL** → Agent **不要用 RPM 装**（yum 源是死的），直接用 Docker 容器，镜像自带一切依赖

### 1.4 校园网这条否掉了「IP 白名单」

**你用的是校园网、IP 自动分配。**这带来三个麻烦：

| 麻烦 | 后果 |
|---|---|
| 出口 IP 会变 | 白名单今天配好，明天可能就失效 |
| 出口 IP 往往**多人共享** | 白名单根本没起到「只允许我」的作用，安全性是假的 |
| 可能在工作 / 不工作之间来回跳 | **最难受的失败方式**——不是一直坏，是偶尔坏，这类故障最难查 |

所以本方案**不用 IP 白名单，改用 SSH 隧道**（见阶段四）。隧道把这些问题一次解决：

- **完全不需要动安全组**，云服务器的 10050 **对公网完全不开放**
- **不依赖你的 IP**——校园网、家里、手机热点，在哪都能用
- 走的是 **22 端口**，而你现在就在用它做 Ansible 部署，**说明这条链路本来就是通的**
- **加密 + 密钥认证**，比裸奔的 Agent 端口安全得多

代价是多一个组件（隧道进程）。但用 systemd 管起来之后开机自启、断线自动重连——**顺带还练了一次 systemd**，这在运维里是天天要用的东西。

---

## 2. 架构与数据流

```
   WSL2（你的电脑，7.1 GiB 可用）              云服务器（468 MiB 可用）
   ┌──────────────────────────────┐            ┌──────────────────────────┐
   │  Zabbix Web      :8080       │            │  sshd  :22               │
   │  Zabbix Server               │            │    │                     │
   │  MySQL                       │            │    ▼ 127.0.0.1:10050     │
   │       │                      │            │  Zabbix Agent   ~30 MB   │
   │       ▼                      │            │  （只绑 127.0.0.1，      │
   │  172.28.0.1:10050            │            │    公网完全扫不到）       │
   │  （隧道在 WSL2 的监听口）      │            │    ▲ 取值                │
   └───────┬──────────────────────┘            │    │                     │
           │                                   │  blog-nginx   :8081      │
           │  SSH 隧道（★ 出方向）              │  blog-backend :8080      │
           │  校园网 / 家里 / 热点 都能建        │  redis        :6379      │
           └──────────────────────────────────▶│  （myapp-* 不动）        │
                      <服务器IP>:22          └──────────────────────────┘
```

### 数据流（被动模式）

```
WSL2 (Server)                              云服务器 (Agent)
     │                                          │
     │─── "system.cpu.util 是多少？" ──────────▶│  :10050
     │                                          │  agent 现场取值
     │◀── "42" ────────────────────────────────│
     │                                          │
   每 60 秒问一轮，值写进 WSL2 的 MySQL
```

**「主动 / 被动」是从 Agent 的视角说的**：

| 模式 | 谁发起连接 | 数据流向 | 本方案 |
|---|---|---|---|
| **被动**（默认） | **Server** 连 Agent:10050 | Server 问 → Agent 答 | ✅ **用这个** |
| 主动 | Agent 连 Server:10051 | Agent 定时推 | ❌ 需要反向隧道，不用 |

**为什么本方案能用被动模式**：被动模式要求「Server 能连到 Agent」。这里靠 **SSH 隧道**解决——隧道由 WSL2 **主动建立**（出方向不受任何 NAT 限制），建好之后 Server 就能通过隧道「够到」云服务器上的 Agent。

**注意方向**：隧道是 WSL2 连出去的，不是云服务器连进来。所以**跟你的出口 IP 是什么毫无关系**——校园网、家里、手机热点，只要能上网就能建隧道。

### 一次完整的监控循环

```
① Server 读数据库里的配置：有哪些主机、每台有哪些监控项、各多久采一次
② 到时间了，Server 派 poller 进程连 agent:10050 取值
③ 拿到值 → 写进 MySQL（history 表存原始值，trends 表存小时聚合）
④ 同一个值喂给触发器表达式求值，比如 last(/myblog-server/net.tcp.service[tcp,172.29.0.1,8081])=0
⑤ 表达式成立 → 生成 Event（问题事件）
⑥ Action（动作）匹配到 Event（事件）→ 按 Media Type（媒介类型）发出去（邮件 / 钉钉）
⑦ 值恢复正常 → 生成恢复 Event（事件）→ 再通知一次
```

**注意第 ④ 步在 Server 上做**，不是 Agent——这就是 Agent 能只有 30 MB 的原因。

---

## 3. 内存账

### WSL2（7.7 GiB 总量）

| 组件 | 预估 |
|---|---|
| zabbix-mysql | ~450 M |
| zabbix-server | ~250 M |
| zabbix-web | ~120 M |
| ssh 隧道进程 | ~5 M |
| **合计** | **~825 M / 7.7 G** ✅ 非常宽裕 |

### 云服务器（468 MiB 可用）

| 组件 | 预估 |
|---|---|
| zabbix-agent2 | ~30 M |
| **合计** | **~30 M** ✅ 剩约 438 M |

> **Agent 一定要设 `mem_limit`**。这台机器太紧，任何组件都不能允许它无限涨。下面 compose 里写了 `mem_limit: 64m`。
>
> **如果 30 M 也觉得心疼**：可以换成经典版 Agent（C 写的，约 5 MB）——把镜像换成 `zabbix/zabbix-agent:alpine-7.0-latest` 即可，功能对第一阶段的 OS 指标完全够，只是没有 `docker.*` 系列 key。

---

## 4. 阶段一：WSL2 装 Docker

> ⚠️ **动手前先给 WSL2 做个快照**。练习环境的全部价值在于"搞坏了能重来"。
>
> ```powershell
> # 在 Windows PowerShell 里执行（不是 WSL 里）
> wsl --shutdown
> wsl --export Ubuntu D:\wsl-backup\ubuntu-base.tar
> ```
>
> 恢复：`wsl --import Ubuntu D:\wsl\Ubuntu D:\wsl-backup\ubuntu-base.tar`

### 4.1 装 Docker Engine

```bash
# 在 WSL2 里执行
curl -fsSL https://get.docker.com | sudo sh
```

**把当前用户加进 docker 组**（否则每条命令都要 `sudo`）：

```bash
sudo usermod -aG docker $USER
newgrp docker          # 让当前 shell 立刻生效，不用重登
docker version
```

看到 Client 和 Server 两段都有输出就成了。

### 4.2 配镜像加速器

国内直连 Docker Hub 很慢。**复用博客项目已经在用的那四个加速器地址**：

```bash
sudo mkdir -p /etc/docker
sudo tee /etc/docker/daemon.json > /dev/null <<'EOF'
{
  "registry-mirrors": [
    "https://docker.1panel.live/",
    "https://docker.m.daocloud.io/",
    "https://docker.xuanyuan.me/",
    "https://docker.1ms.run/"
  ],
  "log-driver": "json-file",
  "log-opts": { "max-size": "10m", "max-file": "3" }
}
EOF

sudo systemctl restart docker
```

**验证加速器真的生效了**（别只看配置文件）：

```bash
docker info | grep -A5 "Registry Mirrors"
```

> **`log-opts` 那两行别省**。Docker 默认不轮转日志，一个常年运行的容器能把磁盘写满——这是 `04-pitfalls.md` 里提过的真实故障。

### 4.3 验证

```bash
docker run --rm hello-world
```

---

## 5. 阶段二：WSL2 起 Zabbix 三件套

### 5.1 建目录与密钥文件

```bash
mkdir -p ~/zabbix && cd ~/zabbix
mkdir -p data/mysql
```

生成密码（**别用弱密码，也别手打**）：

```bash
cat > .env <<EOF
MYSQL_ROOT_PASSWORD=$(openssl rand -base64 24 | tr -d '/+=' | head -c 24)
ZABBIX_DB_PASSWORD=$(openssl rand -base64 24 | tr -d '/+=' | head -c 24)
EOF

chmod 600 .env
cat .env        # 记下来，后面 Web 里要用
```

> `chmod 600` 不能省。这跟 `部署方案.md` 里 `.env` 设成 `0600` 是同一个道理。
>
> **注意**：密码里不要留 `/ + =` 这些字符——它们在数据库连接串里可能被转义，是经典的坑。

### 5.2 `docker-compose.yml`

```yaml
services:
  zabbix-mysql:
    image: mysql:8.0
    container_name: zabbix-mysql
    restart: unless-stopped
    # ★ 这四行参数一个都不能少，原因见下方说明
    command:
      - mysqld
      - --character-set-server=utf8mb4
      - --collation-server=utf8mb4_bin
      - --default-authentication-plugin=mysql_native_password
      - --log_bin_trust_function_creators=1
    environment:
      MYSQL_DATABASE: zabbix
      MYSQL_USER: zabbix
      MYSQL_PASSWORD: ${ZABBIX_DB_PASSWORD}
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD}
    volumes:
      - ./data/mysql:/var/lib/mysql
    healthcheck:
      test: ["CMD-SHELL", "mysqladmin ping -h 127.0.0.1 -uroot -p$$MYSQL_ROOT_PASSWORD"]
      interval: 10s
      timeout: 5s
      retries: 12
    mem_limit: 1g
    networks: [zbx]

  zabbix-server:
    image: zabbix/zabbix-server-mysql:alpine-7.0-latest
    container_name: zabbix-server
    restart: unless-stopped
    environment:
      DB_SERVER_HOST: zabbix-mysql
      MYSQL_DATABASE: zabbix
      MYSQL_USER: zabbix
      MYSQL_PASSWORD: ${ZABBIX_DB_PASSWORD}
      ZBX_CACHESIZE: 64M
      ZBX_HISTORYCACHESIZE: 32M
    ports:
      - "10051:10051"
    depends_on:
      zabbix-mysql:
        condition: service_healthy
    mem_limit: 1g
    networks: [zbx]

  zabbix-web:
    image: zabbix/zabbix-web-nginx-mysql:alpine-7.0-latest
    container_name: zabbix-web
    restart: unless-stopped
    environment:
      ZBX_SERVER_HOST: zabbix-server
      DB_SERVER_HOST: zabbix-mysql
      MYSQL_DATABASE: zabbix
      MYSQL_USER: zabbix
      MYSQL_PASSWORD: ${ZABBIX_DB_PASSWORD}
      PHP_TZ: Asia/Shanghai
    ports:
      - "8080:8080"
    depends_on:
      zabbix-mysql:
        condition: service_healthy
    mem_limit: 512m
    networks: [zbx]

networks:
  zbx:
    name: zabbix_net
    # ★ 钉死网段：网关永远是 172.28.0.1
    #   —— 容器眼里的「宿主机」，阶段四的隧道口就开在那
    ipam:
      config:
        - subnet: 172.28.0.0/24
```

**为什么 MySQL 那四个参数缺一不可**：

| 参数 | 不加会怎样 |
|---|---|
| `--character-set-server=utf8mb4` | 中文变乱码 |
| `--collation-server=utf8mb4_bin` | 建表时索引超长报错，**schema 建一半就失败** |
| `--log_bin_trust_function_creators=1` | Zabbix 建不了存储函数，schema 导入失败 |
| `--default-authentication-plugin=mysql_native_password` | 认证插件不兼容，连不上库 |

后面三条的症状都是**同一个**：Zabbix Server 起不来，日志报 `its "users" table is empty`。**这个报错的真因几乎总是上面这几个参数，不是数据库坏了。**

> **Zabbix 会自动把自己这台主机加进来吗？**
> 装好之后数据库里确实自带一台叫 `Zabbix server` 的主机，但**我们不用管它**——本方案里 WSL2 只当服务端，不监控自己。
> 它可能会在 Server 日志里每 15 秒刷一条 `... on host "Zabbix server" failed: network error`，
> 看着吓人，其实是它在找本地没装的 agent。**看 host 名就能分辨**：写 `Zabbix server` 的可以无视，写 `myblog-server` 的才是真出问题（见第 11 节）。

### 5.3 启动并观察初始化

```bash
docker compose up -d

# ★ 盯着 server 的日志，看它自动建表的过程
docker compose logs -f zabbix-server
```

**建表是 Server 容器自动做的**，不需要你手动导入 SQL。它的逻辑是：

```
检查数据库能不能连 → 检查 zabbix 库在不在（不在就建）
→ 检查 dbversion 表在不在（不在就导入完整 schema + 初始数据）
```

首次启动建表要等 **1~3 分钟**。看到类似这样的输出就算好了：

```
Starting Zabbix Server
...
server #0 started [main process]
```

**注意启动顺序**：数据库 → Zabbix Server（自动建表）→ Web。**Web 不能建表**，如果 Web 先起来会报错，等 Server 建完就好了。

### 5.4 打开界面

浏览器访问 **http://localhost:8080**（WSL2 的端口会自动转发到 Windows 的 localhost）。

默认账号 **`Admin`** / 密码 **`zabbix`**（注意 A 大写）。

**第一件事：改密码**。右上角头像 → User settings（用户设置）→ Change password（修改密码）。

### 5.5 验证

```bash
docker compose ps
# 三个容器都应该是 Up（mysql / server / web）
```

```bash
# ★ 确认 Server 真的连上库了，没有报错
docker compose logs zabbix-server | grep -iE 'error|cannot|failed' | tail -20
```

**没有输出 = 干净**。有输出就对照第 11 节排错。

---

## 6. 阶段三：云服务器装 Agent

> **★ 装在 `/opt/zabbix-agent/`，不要放 `/opt/myblog/`。**
>
> 因为 CI 部署脚本每次都会往 `/opt/myblog/` 里覆盖文件。Agent 是**基础设施**不是应用，混在一起早晚被误伤。

### 6.1 建目录

```bash
# 在云服务器上
mkdir -p /opt/zabbix-agent && cd /opt/zabbix-agent
```

### 6.2 `docker-compose.yml`

```yaml
services:
  zabbix-agent:
    image: zabbix/zabbix-agent2:alpine-7.0-latest
    container_name: myblog-zabbix-agent
    restart: unless-stopped
    environment:
      # ★ 这个名字要和 Web 里加主机时填的「Host name（主机名称）」完全一致
      ZBX_HOSTNAME: myblog-server
      # ★ 谁有权来查我。经过 SSH 隧道后来源是「本机地址」，不是公网 IP（见 6.3）
      ZBX_SERVER_HOST: "127.0.0.1,172.29.0.1"
      # ★ 只用被动模式。主动模式要 agent 反过来连 WSL2 的 10051，公网上到不了你电脑
      ZBX_PASSIVE_ALLOW: "true"
      ZBX_ACTIVE_ALLOW: "false"
    ports:
      # ★ 只绑回环。10050 自始至终不出现在公网上 —— 本方案的安全底线
      - "127.0.0.1:10050:10050"
    # ★ 这台机器只剩 468M，任何组件都必须设上限
    mem_limit: 64m
    volumes:
      # ★ 只读挂进【监控状态目录】—— 让 Agent 能看见 last_success 的时间戳。
      #   它【是容器】，不挂进来就看不见宿主机上的任何文件，
      #   vfs.file.time 那个监控项会直接变成 unsupported。
      #
      #   ⚠️ 为什么不是挂 backup 目录（这个坑真踩过）：
      #      backup 是 0700 且放着 OSS 凭据，而 Agent 以非 root 跑
      #      （zabbix 用户，uid 1997）—— 连【目录】都穿不过去，报
      #      Permission denied。注意卡住的是目录的可执行位，不是文件的读位。
      #      心跳文件本身不含任何秘密，所以单独搬到 status/ 给它看，
      #      backup/ 那边继续 0700，谁也不用让步。
      #
      #   ⚠️ 必须 :ro：监控只需要读，不给它任何写的机会。
      #   配套的监控项 / 触发器见 deploy/备份与恢复手册.md 第 6 节。
      - /opt/myblog/status:/opt/myblog/status:ro

networks:
  default:
    name: zabbix-agent-net
    # ★ 钉死网段，网关固定为 172.29.0.1（白名单要填它，见 6.3）
    ipam:
      config:
        - subnet: 172.29.0.0/24
```

### 6.3 关于白名单 `ZBX_SERVER_HOST`

它对应配置文件里的 `Server=` 参数，语义是**「谁有权来查我」**。

**关键点：加了 SSH 隧道之后，来的不再是「你家的公网 IP」，而是「云服务器本机的地址」。**

```
sshd 收到隧道请求 → 在服务器本机连 127.0.0.1:10050 → docker 转发进 agent 容器
                                                       ↑
                                          容器看到的来源：docker 网络的网关 IP
```

所以这里要填的是**本机相关地址**，不是任何公网 IP：

| 候选来源 | 什么时候是它 |
|---|---|
| `127.0.0.1` | docker 用 iptables 直接转发时，来源保持回环地址不变 |
| `172.29.0.1` | docker 走用户态代理（`docker-proxy`）时，来源变成容器所在网络的**网关** |

**两个都填上就不用赌** docker 在你的环境里走哪条路——`Server=` 支持逗号分隔多个值。

> **为什么要把 agent 的网络网段也钉死**：`172.29.0.1` 不是天生固定的。不指定的话 docker 会从地址池里随便挑一段，网段一变网关就跟着变，白名单又得改。**把网段写死，来源 IP 才是可预测的。** 这跟 WSL2 那边钉死 `172.28.0.0/24` 是同一个道理。

**启动后必须验证配置真的写进去了**，别假设环境变量一定生效：

```bash
docker compose up -d
docker exec myblog-zabbix-agent cat /etc/zabbix/zabbix_agent2.conf \
  | grep -E '^(Server|ServerActive|Hostname)'
```

要看到两件事：

1. `Server=` 里有 `127.0.0.1` 和 `172.29.0.1`
2. **`ServerActive=` 应该是空的** —— 我们只用被动模式。如果它被填上了地址，说明这个版本的镜像把两个参数绑在了同一个变量上，用 `ZBX_ACTIVE_ALLOW: "false"` 关掉（环境变量名不对的话，`ServerActive=` 手动清空即可）

> **日志里出现 `connection from X rejected` 时，那个 X 就是真实的来源 IP。** 把它加进 `ZBX_SERVER_HOST` 再重建容器即可——**这比在这里猜快得多**，也是这类问题最快的解法。

### 6.4 验证 Agent 自己活着

```bash
docker compose ps
docker logs myblog-zabbix-agent | tail -20
```

---

## 7. 阶段四：建 SSH 隧道

### 7.1 隧道在做什么

先看方向，**箭头是最关键的部分**：

```
   云服务器 <服务器IP>                            你的 WSL2
   ┌────────────────────────┐                   ┌────────────────────────┐
   │ sshd :22 ◀─────────────┼── ① WSL2 连出去 ──┤ ssh -L 命令            │
   │   │                    │   （出方向，       │                        │
   │   │ ② 服务器本机转发    │    不受 NAT 限制） │  监听 0.0.0.0:10050    │
   │   ▼                    │                   │         ▲              │
   │ 127.0.0.1:10050        │                   │         │ ③ 连进来     │
   │   │ ③ docker 转发      │                   │  zabbix-server 容器    │
   │   ▼                    │                   │  连 172.28.0.1:10050   │
   │ agent 容器             │                   └────────────────────────┘
   └────────────────────────┘
```

一句话：**把云服务器的 10050「搬」到 WSL2 的 10050 上。** Zabbix Server 以为自己在连本机的一个端口，实际上这条连接一路穿到了云服务器的 agent。

**为什么不用安全组 + IP 白名单**（1.4 讲过原因）：

| | 安全组放行 10050 | SSH 隧道（本方案） |
|---|---|---|
| 要动安全组吗 | 要 | **不用** |
| 依赖你的出口 IP 吗 | 依赖，变了就断 | **不依赖** |
| 10050 对公网开放吗 | 开放（只靠白名单挡） | **完全不开放** |
| 传输加密吗 | 不加密 | **SSH 加密** |
| 有认证吗 | Agent 被动模式**没有认证** | **密钥认证** |

> **如果你不是校园网**（家宽、或有固定公网 IP），这整套隧道可以整个跳过，改用「安全组 + IP 白名单」——见 **[第 14 节](#14-备选方案如果不是校园网家宽或固定公网-ip)**。

> **动手前先确认一件事**：Zabbix Server 跑在容器里，容器要连的「本机」不是真的本机。所以 5.2 里把 `zabbix_net` 的网段钉成了 `172.28.0.0/24`，**网关就永远是 `172.28.0.1`** —— 这个地址就是容器眼里的「宿主机」，也是**阶段五要填进 Web 的接口 IP**。

```bash
# 确认网关确实是 172.28.0.1
docker network inspect zabbix_net -f '{{(index .IPAM.Config 0).Gateway}}'
```

### 7.2 先手动跑通，别急着做成服务

**这一步不要跳。** 手动跑能直接看到报错；做成 systemd 服务之后报错会被吞进日志里，排查反而更慢。

```bash
# 在 WSL2 里执行
ssh -N \
  -o ServerAliveInterval=30 \
  -o ServerAliveCountMax=3 \
  -o ExitOnForwardFailure=yes \
  -o StrictHostKeyChecking=accept-new \
  -i ~/.ssh/myblog_deploy \
  -L 0.0.0.0:10050:127.0.0.1:10050 \
  root@<服务器IP>
```

**逐段拆开看**：

| 片段 | 含义 |
|---|---|
| `-N` | 不要执行远程命令，只做端口转发 |
| `-L 0.0.0.0:10050:127.0.0.1:10050` | 本机监听 10050 → 转给**服务器的** 127.0.0.1:10050 |
| **`0.0.0.0` 而不是默认的 `127.0.0.1`** | **本方案最容易漏的一处。** 默认只绑回环，容器从 `172.28.0.1` 过来就连不上 |
| `ExitOnForwardFailure=yes` | 端口被占等转发失败时**立刻退出**，不要装作没事 |
| `ServerAliveInterval=30` | 每 30 秒发一次心跳 |
| `StrictHostKeyChecking=accept-new` | 首次连接自动记录主机密钥（systemd 里没有终端可以回 yes） |

跑起来后**不会有任何输出**——这是正常的，`-N` 就是静默转发。**别关这个终端。**

**另开一个终端验证**：

```bash
# ① 本机 10050 在听，而且是 ssh 在听
ss -tlnp | grep 10050

# ② 从「容器视角」那个地址也能连上 ← 这条最关键
timeout 5 bash -c 'cat < /dev/null > /dev/tcp/172.28.0.1/10050' \
  && echo "✅ 隧道通" || echo "❌ 通不了"
```

**② 不通，八成是 `-L` 前面的 `0.0.0.0` 漏了**（变成了只绑回环，容器够不到）。

### 7.3 做成 systemd 服务

手动那条命令一关终端就断，电脑重启后也不会自己起来。用 systemd 管起来。

**先把用户名和路径填对** —— 下面两处 `<你的用户名>` 都要替换成真实值：

```bash
whoami        # 确认用户名，比如 huhaohua
```

```bash
sudo tee /etc/systemd/system/zabbix-tunnel.service > /dev/null <<'EOF'
[Unit]
Description=Zabbix Agent SSH Tunnel (WSL2 -> cloud)
After=network-online.target docker.service
Wants=network-online.target

[Service]
# ★ 必须用你自己的用户跑，不能是 root —— 密钥在你家目录里，root 找不到
User=<你的用户名>
Environment=HOME=/home/<你的用户名>
ExecStart=/usr/bin/ssh -NT \
  -o ServerAliveInterval=30 \
  -o ServerAliveCountMax=3 \
  -o ExitOnForwardFailure=yes \
  -o StrictHostKeyChecking=accept-new \
  -o BatchMode=yes \
  -i /home/<你的用户名>/.ssh/myblog_deploy \
  -L 0.0.0.0:10050:127.0.0.1:10050 \
  root@<服务器IP>
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
EOF
```

> 先回 7.2 那个终端按 **`Ctrl+C`** 把手动命令停掉。不然 10050 被占着，新服务会因为 `ExitOnForwardFailure` 立刻退出。

```bash
sudo systemctl daemon-reload
sudo systemctl enable --now zabbix-tunnel

systemctl status zabbix-tunnel --no-pager
```

看到 **`active (running)`** 就对了。注意命令里的 `enable` —— 那是**开机自启**，别只 `start`。

### 7.4 那三个参数为什么必须是这三个

它们不是可选项，少一个就会遇到一类**很难查**的故障：

| 参数 | 少了会怎样 |
|---|---|
| `Restart=always` | 隧道断了就永远断了，得你手动去拉 |
| `ServerAliveInterval=30` + `ServerAliveCountMax=3` | **校园网 / NAT 会静默回收空闲连接。** TCP 连接看着还在，实际早断了——这就是「假死」：`systemctl status` 显示 running，监控却没数据。心跳 90 秒没回应就让 ssh 自己退出，交给 `Restart` 重连 |
| `ExitOnForwardFailure=yes` | 端口被占时 ssh 会**继续运行但不转发**——进程活着、端口没通。**状态是绿的，功能是死的**，最难查的一种 |

`BatchMode=yes` 是给 systemd 用的：systemd 里没有终端，任何交互式提示（要密码、要确认指纹）都会让进程**永远挂在那里**。加了它就会直接失败退出，至少你还能在日志里看到原因。

> **这几条合起来是一句话：让失败变成「明显的失败」。** 运维里最贵的不是宕机，是**看起来在跑其实没跑**。

### 7.5 验证隧道能自愈

光看 `active (running)` 不算数——**要验证它断了会自己回来**。「没验证过的机制等于不存在」。

```bash
# ① 强行杀掉隧道进程，模拟断线
sudo systemctl kill -s KILL zabbix-tunnel

# ② 等 15 秒（RestartSec=10）
sleep 15

# ③ 看它有没有自己起来
systemctl status zabbix-tunnel --no-pager     # 应该又是 active (running)
ss -tlnp | grep 10050                          # 端口重新在听
```

**如果 ③ 显示 `failed` 或端口没回来**，说明 `Restart=always` 没生效，回去检查 unit 文件。

**这个测试值得做**，因为它验证的是整个方案里**唯一会自己坏掉的那个环节**——校园网随时可能掐掉你的连接，靠这个机制你根本不会察觉。

> 顺带认识一条以后天天要用的命令：**服务出问题先看它的日志。**
>
> ```bash
> journalctl -u zabbix-tunnel -n 50 --no-pager
> ```

---

## 8. 阶段五：Web 里接入主机

浏览器 → **http://localhost:8080** → `Admin` 登录

**Data collection（数据采集）→ Hosts（主机）→ Create host（创建主机）**

| 字段 | 填什么 |
|---|---|
| **Host name（主机名称）** | `myblog-server` ← **必须和 Agent 的 `ZBX_HOSTNAME` 完全一致** |
| **Host groups（主机组）** | `Linux servers`（没有就新建一个） |
| **Interfaces（接口）** | Agent → IP 填 **`172.28.0.1`** → 端口 `10050` |
| **Templates（模板）** | 搜索并链接 **`Linux by Zabbix agent`** |

**Save（保存）**。

> **为什么 IP 填 `172.28.0.1` 而不是服务器的公网 IP `<服务器IP>`？**
>
> 因为 **Zabbix Server 是容器，它只看得到 docker 网络内部**。`172.28.0.1` 是它所在网络的网关，也就是它眼里的「宿主机」——而隧道的监听口正好开在宿主机上。
>
> 填公网 IP 反而连不上：容器去连 `<服务器IP>:10050` 会绕一圈跑到公网上，而那个端口我们**故意没有对公网开放**（6.2 里绑的是 `127.0.0.1`）。
>
> 一句话：**Server 不直连服务器，它连的是隧道。**

### 验证

等 **1~2 分钟**，主机的 **ZBX 图标应该由红变绿**。

- 🟢 **绿** = 通了
- 🔴 **红** = Server 连不到 Agent → 回到阶段四排错

变绿后去 **Monitoring（监测）→ Latest data（最新数据）** → 选 `myblog-server`，应该能看到 CPU、内存、磁盘、网络的数据在刷新。

> **注意选对模板版本**：`Linux by Zabbix agent` 是被动模式，`Linux by Zabbix agent active` 是主动模式。**本方案必须用被动那个**——active 版要求 Agent 主动连回 WSL2 的 10051 端口，而**公网上没有任何人能连进你电脑**，这条路径根本不存在。

> **内置那台 `Zabbix server` 主机怎么办？**
> 不用管。它挂着 `Linux by Zabbix agent` 模板，但本地没有 agent，所以图标会一直是红的、Server 日志每 15 秒刷一条 network error。
> **这不是故障**——本方案里 WSL2 只当服务端。真想让它变绿，得在本地再起一个 agent 容器，属于可选加餐，不影响博客监控。

---

## 9. 阶段六：加博客专属监控

模板给的是通用 OS 指标。**真正有价值的是「博客本身还活着吗」。** 加两个监控项，一个从里看、一个从外看。

### 9.1 监控项 A：端口存活（从云服务器侧探）

**Data collection（数据采集）→ Hosts（主机）→ myblog-server → Items（监控项）→ Create item（创建监控项）**

| 字段 | 值 |
|---|---|
| Name（名称） | `博客 Nginx 端口存活` |
| Type（类型） | `Zabbix agent`（Zabbix 客户端） |
| Key（键值） | `net.tcp.service[tcp,172.29.0.1,8081]` |
| Type of information（信息类型） | `Numeric (unsigned)`（数字（无正负）） |
| Update interval（更新间隔） | `1m` |

这个 key 让 Agent 去连 8081，返回 `1`（在听）或 `0`（没在听）。

> **★ 地址为什么是 `172.29.0.1`，不是 `127.0.0.1`？**
>
> 因为 **agent 跑在容器里**（见 6.2）——**容器的 `127.0.0.1` 是「它自己」，不是云服务器。**
> nginx 在**宿主机**的 8081 上，容器要够到宿主机，得走**它所在网络的网关**：
> `zabbix-agent-net` 是 `172.29.0.0/24`，网关就是 `172.29.0.1`。
>
> **填 `127.0.0.1` 的话，探的是容器自己 → 永远是 `0`**，而且从现象上完全看不出哪里错。
>
> 跟 5.2 / 6.2 里那个 `172.28.0.1` / `172.29.0.1` 是同一个道理：
> **容器眼里的「宿主机」，永远是它所在网络的网关。**
>
> ⚠️ **注意写法**：`net.tcp.service` 的参数是 `[服务, IP, 端口]`，**三个**。
> 只写 `net.tcp.service[tcp,8081]`（漏一个逗号）的话，Zabbix 会把 `8081` 当成 **IP**，
> 端口用默认值 —— 值会乱跳，同样看不出原因。

### 9.2 监控项 B：接口端到端（从 WSL2 外部看）★ 更有价值

**Create item（创建监控项）**

| 字段 | 值 |
|---|---|
| Name（名称） | `博客接口可用性` |
| Type（类型） | **`HTTP agent`（HTTP 代理）** |
| Key（键值） | `myblog.api.status` |
| URL | `http://<服务器IP>:8081/api/config/all` |
| Type of information（信息类型） | **`Numeric (unsigned)`（数字（无正负））★** |
| Update interval（更新间隔） | `1m` |

**★ 这个监控项有两个地方必须改，不改就永远报警。**

#### ① Retrieve mode（检索模式）：`Body`（内容）→ `Headers`（头）

默认是 `Body`，存下来的是**整个响应体**（一大坨 JSON 字符串）。

改成 `Headers` 后，存的是原始响应头：

```
HTTP/1.1 200 OK
Server: nginx/1.24.0
Content-Type: application/json
...
```

**第一行那个 `200` 才是 HTTP 状态码。**

> ⚠️ 别跟响应体里的 `"code": 200` 搞混——那是 Spring 的**业务码**，
> 跟 **HTTP 状态码**是两码事。浏览器 F12 → Network（网络）里那一栏 Status（状态）才是 HTTP 状态码。

#### ② Preprocessing（预处理）→ Add（添加）→ `Regular expression`（正则表达式）

| 字段 | 值 |
|---|---|
| Pattern（模式） | `HTTP/\S+ ([0-9]{3})` |
| Output（输出） | `\1` |

拆开看这个正则：

- `HTTP/` —— 先找到 `HTTP/`
- `\S+` —— 后面跟一串非空白字符（也就是 `1.1`）
- ` ([0-9]{3})` —— 一个空格，然后是**三个数字**。**外面这对括号就是「捕获组」**
- Output 填 `\1` —— 意思是「**取第 1 个括号里的内容**」

所以整条的效果是：从 `HTTP/1.1 200 OK` 里，**只把 `200` 拎出来**。

> Output 框用的是 `\1` `\2` 这种**数字编号**，不是 `(?<名字>...)` 命名组的写法。填 `\1` 就行。

#### ③ 改完点「Test（测试）」按钮 —— 别省这步

按钮在对话框右下角、Update 旁边。点一下会**当场执行一次**，直接把抠出来的值显示给你看，应该显示 `200`。

比等一分钟去看 Latest data（最新数据）快得多，而且这一下就顺带验证了**整条链路**：
Zabbix server → 公网 → 阿里云安全组 → Nginx → 后端 → 数据库。

> **`Type of information`（信息类型）为什么必须是「数字」？**
>
> 如果留成默认的 `Log`（日志），这个监控项存的是**文字**。
> 而触发器里 `<>200` 是**数字比较**——Zabbix 拿一坨文字去转数字，
> 转不出来 → 当成 `0` → `0 <> 200` **恒成立 → 永远报警**。
> 从现象上看就是「博客明明好好的，告警一直挂着」，很难往配置上想。

#### ④ `Required status codes`（要求的状态码）：**清空它**

同一个对话框往下滚，有这么一栏，默认填着 `200`。**把它删掉，留空。**

| 场景 | 填 `200`（默认） | 留空（推荐） |
|---|---|---|
| 博客正常 | 记下 `200` | 记下 `200` |
| 后端挂了，nginx 回 502 | 监控项变成**「不支持」**，这个 `502` **被丢掉** | 记下 `502` |
| 触发器 `<>200` 会不会响 | 会响 | 会响 |

> **这个字段的含义**：只要返回的状态码不在这个列表里，Zabbix 就判定「这次采集失败」，
> 把监控项标成**「不支持（unsupported）」**，**值直接丢掉**。
>
> 所以填 `200` 的代价**不是漏报，是丢信息**——你只知道「坏了」，
> 不知道**怎么坏的**。到底是 `502`（后端没起来）还是 `504`（后端卡住）还是别的，
> Latest data（最新数据）里一概没有，历史图也是一片空白。
>
> 留空之后，这个监控项的历史数据就变成了**状态码曲线**：
> 哪天页面开始偶尔抽风，你能在图上直接看到 502 是从几点开始冒出来的。
> 比「有 / 没有」的布尔值有用得多。

> **★ 实测记录（2026-10-09）：清空的理由不是「防漏报」**
>
> 一开始担心的是**静默失效**——「监控项一旦不支持，触发器就哑了，
> 等于博客真挂的时候告警最安静」。
>
> **实测证明没这回事。** 把后端 `docker stop` 掉、nginx 回 502 之后，
> 这条触发器**照常报警**；后端启回来，自动变「已解决」。
>
> 所以清空它纯粹是为了**别把状态码丢掉**。
> 结论以实测为准——**这就是 9.4 那个演练存在的意义。**

改完全部点 **Update（更新）** 保存，再点 **Test（测试）** 确认值还是 `200`。

> **这个监控项比 A 有价值得多**：它是**由 WSL2 的 Zabbix Server 发起的**，走的是完整的公网链路——会经过阿里云安全组、Nginx、后端、数据库。**任何一层坏了它都会失败**。而 A 只是从 agent 容器里探了一下宿主机的 8081 —— Nginx 配错了它照样返回 1。
>
> 这也是为什么它顺带验证了「外部能不能访问博客」这件事。

### 9.3 触发器

**Data collection（数据采集）→ Hosts（主机）→ myblog-server → Triggers（触发器）→ Create trigger（创建触发器）**

| 字段 | 值 |
|---|---|
| Name（名称） | `博客无法访问` |
| Severity（严重性） | `High`（严重） |
| Expression（表达式） | `last(/myblog-server/net.tcp.service[tcp,172.29.0.1,8081])=0` |

再加一个：

| 字段 | 值 |
|---|---|
| Name（名称） | `博客接口返回非 200` |
| Severity（严重性） | `High`（严重） |
| Expression（表达式） | `last(/myblog-server/myblog.api.status)<>200` |

### 9.4 ★ 验证触发器真的会响

**这一步不能省**——「没验证过的机制等于不存在」。

```bash
# 在云服务器上，故意把博客停掉
cd /opt/myblog
docker compose stop nginx
```

然后回 Web 里 **Monitoring（监测）→ Problems（问题）**，**1~2 分钟内**应该出现「博客无法访问」。

**验证完记得启回来**：

```bash
cd /opt/myblog
docker compose start nginx
```

问题应该自动变成 **RESOLVED（已解决）**。

> 如果它没响，说明触发器配错了——**现在发现，比真出事的时候发现好得多**。

---

**★★ 但只验证 A 是不够的，必须再补一个：停后端。**

```bash
# 注意：只停 backend，不要 down（down 会把网络和 nginx 一起拆了）
docker stop myblog-backend
```

这次 **nginx 还活着**，所以两条触发器的表现完全不同：

| 触发器 | 结果 | 为什么 |
|---|---|---|
| `博客无法访问`（A） | **不响** | nginx 还在 8081 上听着，`net.tcp.service` 照样返回 `1` |
| `博客接口返回非 200`（B） | **响了** ✅ | nginx 回 `502` ≠ `200` |

**顺手确认「外面真的坏了」**（在你电脑的 WSL2 里跑）：

```bash
curl -s -o /dev/null -w '%{http_code}\n' http://<服务器IP>:8081/api/config/all
# 应该输出 502 Bad Gateway
```

**恢复：**

```bash
docker start myblog-backend
docker restart myblog-nginx      # ★ 这行不能省，见下
```

> **★ `docker restart myblog-nginx` 为什么不能省？**
>
> nginx 里写的是 `proxy_pass http://backend:8080`。
> **nginx 只在启动时解析一次域名，然后把 IP 记死**，之后不管后端换没换 IP。
>
> 后端容器重新起来拿到的是**新 IP**，nginx 还拿着旧 IP 往上撞 → **还是 502**。
> 你会以为「后端没起来」，其实后端早好了，**是 nginx 没跟上**。
> 这个坑的现象和「后端真挂了」一模一样，很容易查错方向。
>
> 部署脚本里 `docker compose restart nginx` 那行的注释写的就是这事。

> **★ A 和 B 的分工——这才是一整套**
>
> | | 管什么 | 什么时候响 |
> |---|---|---|
> | **A** `net.tcp.service` | **门还开着吗** | nginx 挂了、8081 没了 |
> | **B** `myblog.api.status` | **门后面还有人吗** | 后端崩了、数据库连不上、Nginx 配置错 |
>
> **只配一条都会漏。** A 单独用，后端崩了你完全不知道；B 单独用，nginx 挂了它虽然也能报，
> 但报出来的是「连不上」还是「502」，指向的排查方向完全不同。
>
> 这就是 **「没验证过的机制等于不存在」** 的意思——
> **两条触发器都亲自打一遍，才知道哪一条在管哪一段。**

### 9.5 告警：把问题推到邮箱

**9.4 做完还不够。** 触发器「响」了，只是在 **Zabbix 界面里生成一个事件**——你不打开网页看，就永远不知道。告警要真正有用，得**主动推给你**。

#### ★ 先搞懂：一封告警邮件要三段接起来

这是新手最容易卡的地方——**三段是分开配的。配完两段漏了第三段，一封邮件也收不到，而且界面上不会有任何报错。**

```
① 触发器成立 → 生成 Event（问题事件）             ← 9.3 已经配好了
② Action（动作）       决定「这个事件要不要发」     ← 第三段
③ User media（用户媒介）决定「发给谁」              ← 第二段
④ Media type（媒介类型）决定「用什么方式发出去」    ← 第一段
```

| 段 | 配在哪 | 管什么 | 漏配的后果 |
|---|---|---|---|
| **媒介类型** | `Alerts（告警）→ Media types（媒介类型）` | 用什么**方式**发（SMTP 服务器、账号） | 根本发不出去 |
| **用户媒介** | `Users（用户）→ Admin → Media（媒介）` | 发给**谁**（收件地址） | 发出去了，没人收 |
| **动作** | `Alerts（告警）→ Actions（动作）→ Trigger actions（触发器动作）` | **什么条件下**才发 | 有事件，但不派发 |

下面以 **163 邮箱**为例——本方案实测用的就是它。

#### 第一段：Media type（媒介类型）—— 决定「怎么发」

`Alerts（告警）→ Media types（媒介类型）→ Email（邮件）`

| 字段 | 值 |
|---|---|
| Name（名称） | `Email`（保持默认就行） |
| Type（类型） | `Email`（邮件） |
| SMTP server（SMTP 服务器） | `smtp.163.com` |
| SMTP helo（SMTP HELO） | `163.com` |
| SMTP email（SMTP 发件人） | `你的邮箱@163.com` |
| Connection security（连接安全） | `SSL/TLS` |
| Authentication（认证） | `Username and password`（用户名和密码） |
| Username（用户名） | `你的邮箱@163.com` |
| Password（密码） | **163 的授权码**（不是登录密码，见下） |
| Port（端口） | `465`（**有些版本没有这一栏**，选了 SSL/TLS 就自动用 465） |

> **「端口」这一栏不同版本位置不一样。**
> 有的版本是单独一个输入框；有的版本压根没这一栏，要写成 `smtp.163.com:465`。
> **以你界面上实际有的为准。** 选了 SSL/TLS，对应的就是 `465`。

**★ 什么是「授权码」**

163 邮箱**不允许**第三方程序直接用你的登录密码发信，必须单独开一个「授权码」：

```
登录网页版 163 邮箱
→ 设置 → POP3/SMTP/IMAP → 开启 SMTP 服务
→ 按提示扫码验证 → 拿到一串授权码
```

**那串授权码，就是填进上面 `Password（密码）` 栏的东西。** 忘了可以重新生成一串，旧的立刻作废。

> ⚠️ **授权码等同于密码，不要写进这份文档，也不要提交到 git。**
> 它只需要存在于两个地方：① 163 邮箱的设置页（随时能重置）② Zabbix 数据库里。
> **本文全部用 `你的邮箱@163.com` 占位。**

> **★ 发信的是「谁」？—— 这条跟你的架构有关**
>
> 告警邮件是 **Zabbix Server（WSL2 里的容器）** 发出去的，**不是**云服务器上的 Agent。
> 所以要能从 WSL2 连上 `smtp.163.com:465`。
>
> 你的 WSL2 本来就要出公网（拉镜像、连云服务器），这条出站路径是通的，**不用额外配**。
>
> ⚠️ **推论：你的电脑一关机，告警邮件也一起停。**
> 这跟第 1 章那句「你电脑关机时监控停止」是同一件事——**监控和告警，都长在这台机器上**。
> 练习环境可以接受；真要正经上线，Server 得挪到一台 24 小时开着的机器上。

#### 第二段：User media（用户媒介）—— 决定「发给谁」

`Users（用户）→ Admin → Media（媒介）` 标签页 → `Add（添加）`

| 字段 | 值 |
|---|---|
| Type（类型） | `Email`（邮件）← 就是第一段那个媒介类型 |
| Send to（收件人） | 你想收信的邮箱（可以和发件是同一个） |
| When active（启用时间） | `1-7,00:00-24:00` |
| Use if severity（按严重性过滤） | 至少勾上 `High`（严重）和 `Disaster`（灾难） |
| Enabled（已启用） | ✅ |

> **这一段最容易被整段忘掉。** 媒介类型配得再对，**这里没挂上收件地址，谁都收不到**。
>
> 另外注意：**`Send to`（收件人）填的是收件地址，不是发件地址**——栏位名字容易看岔。

#### 第三段：Action（动作）—— 决定「什么时候发」

`Alerts（告警）→ Actions（动作）→ Trigger actions（触发器动作）`

Zabbix 装好时会**自带一个**动作：`Report problems to Zabbix administrators`，默认已启用。它会把问题发给所有挂了 Email 媒介的管理员，**一般不用新建**。

两件值得检查的事：

1. **`Conditions`（条件）标签页** —— 默认是「什么都发」。想只收博客的，加一条 `Host（主机）equals myblog-server`，免得别的杂事也往邮箱里灌。
2. **`Operations`（操作）标签页里至少有两条**：
   - `Send message`（发送消息）—— 问题**发生**时发
   - `Send recovery message`（发送恢复消息）—— 问题**好了**时发

   **只有第一条的话，问题恢复了你不会收到通知**，会一直惦记着「到底好了没」。

#### ★ 验证：分两步，别只做第一步

**第一步：在媒介类型页面上点 `Test`（测试）**

媒介类型对话框底部有 `Test`（测试）按钮，填个收件地址点一下就发。**几秒内邮箱里应该来一封测试邮件。**

这一步只验证 **「SMTP 通不通」**，跟触发器、动作都没关系。

**第二步：真停一次服务**（把 9.4 的演练重跑一遍）

```bash
cd /opt/myblog
docker compose stop nginx
```

**1~2 分钟内，邮箱里应该来一封「博客无法访问」。** 启回来之后，还应该再来一封**恢复邮件**。

> **这两步不是一回事。**
>
> 第一步证明「管道通了」；第二步才证明**「管道接在了正确的地方」**——
> 触发器 → 动作 → 用户媒介 → 媒介类型，**四环里断任何一环，第一步照样通过**。
>
> **只做第一步，就是典型的「以为配好了」。**

> **★ 实测记录（2026-10-09）**
>
> 三段配完后，**真停了一次服务**（不是只点 `Test`）：
>
> | 动作 | 结果 |
> |---|---|
> | `docker compose stop nginx` | 1~2 分钟后，邮箱收到 **「博客无法访问」告警邮件** ✅ |
> | `docker compose start nginx` | 又收到一封 **恢复邮件** ✅ |
>
> **四环全通了**：触发器 → 动作 → 用户媒介 → 媒介类型。
>
> 而且**恢复邮件也到了**——说明第三段 `Operations`（操作）里那两条都在，
> 不会出现「问题好了却不知道」那种半吊子状态。

> **配告警之前先想清楚**：`04-pitfalls.md` 里那句话——**告警必须「可行动」**。收到告警却不知道该做什么，这条告警就该删掉。新手最常见的错误是把告警配太多，最后自己把它静音了。

> 钉钉 / 企业微信 Webhook 的配法见 `运维练习方案.md` 模块 7。

> **★ 还差一件事**：发信的是**笔记本 WSL2 里的 Zabbix 容器**。**你电脑一关，告警就没人发了。**
> 这是这套架构唯一没堵上的洞，兜底方案见**第 15 章**。

---

## 10. 验收清单

按顺序打勾，**不要跳**：

**WSL2 侧**
- [ ] `docker info` 里能看到四个 Registry Mirrors
- [ ] `docker compose ps` **三个**容器都是 Up
- [ ] `docker compose logs zabbix-server` 里没有 error（**唯独 `on host "Zabbix server"` 那一条可以无视**，见 5.2）
- [ ] 浏览器能打开 http://localhost:8080 并登录成功
- [ ] **已经改掉 Admin 默认密码**

**云服务器侧**
- [ ] `/opt/zabbix-agent/` 存在，**没有混在 `/opt/myblog/` 里**
- [ ] agent 容器 Up，且有 `mem_limit`
- [ ] `grep '^Server='` 里是 `127.0.0.1,172.29.0.1`
- [ ] `ServerActive=` 是**空的**（确认只用了被动模式）
- [ ] **博客三个容器 + myapp 三个容器全部还在跑** ← 隔离没被破坏

**隧道（阶段四）**
- [ ] `systemctl is-enabled zabbix-tunnel` 输出 `enabled`（开机自启）
- [ ] `systemctl status zabbix-tunnel` 是 `active (running)`
- [ ] WSL2 上 `ss -tlnp | grep 10050` 能看到 ssh 在听
- [ ] 能连上 `172.28.0.1:10050`
- [ ] **亲手 `kill` 过隧道，它自己回来了** ← 自愈能力验过才算有

**安全底线（这两条是「反向验证」，别跳过）**
- [ ] 阿里云安全组**没有**为 10050 加过任何规则 ← 这就是对的，别去加
- [ ] 云服务器上 `ss -tlnp | grep 10050` **只看到 `127.0.0.1`**，没有 `0.0.0.0`
- [ ] 从外网连 `<服务器IP>:10050` **连不上**：

```bash
timeout 5 bash -c 'cat < /dev/null > /dev/tcp/<服务器IP>/10050' \
  && echo "⚠️ 连上了 —— agent 端口暴露了，回去看 6.2 的 ports 是不是绑成了 0.0.0.0" \
  || echo "✅ 连不上，符合预期"
```

**功能**
- [ ] Web 里 `myblog-server` 的 ZBX 图标是**绿的**
- [ ] Latest data（最新数据）里能看到 CPU / 内存 / 磁盘在刷新
- [ ] 自定义的两个监控项都在收数
- [ ] **亲手停过一次 nginx，Problems（问题）里真的报警了**
- [ ] 恢复后问题自动变 RESOLVED（已解决）

**告警（见 9.5）**
- [ ] 媒介类型页面点 `Test`（测试），**邮箱里收到了测试邮件**
- [ ] **故意停一次服务，邮箱里真的收到了告警邮件** ← 测试邮件通过 **≠** 这一步通过
- [ ] 恢复后收到了**恢复邮件**
- [ ] 授权码**没有**出现在这份文档里，也没提交进 git

**兜底（可选，见第 15 章）**
- [ ] 站点监控任务建好了，控制台里状态正常
- [ ] ★ **记下任务到期日**——站点监控任务有有效期，过期会**静默停止**，不报警
- [ ] **真停一次 nginx，手机 / 邮箱真的收到告警了**，起回来收到恢复通知 ← 和 9.5 一样：看不见的配置不算数

**工程习惯**
- [ ] CI/CD 部署一次，博客正常，**agent 和隧道都不受影响**
- [ ] 知道隧道出问题先看哪：`systemctl status zabbix-tunnel` → `journalctl -u zabbix-tunnel`
- [ ] **电脑重启一次**，Zabbix 的三个容器和隧道都能自己起来

---

## 11. 排错对照表

| 现象 | 最可能的原因 | 怎么查 |
|---|---|---|
| Server 日志报 `its "users" table is empty` | MySQL 那四个启动参数缺了，schema 建一半失败 | 看 5.2 的参数表；**必须 `docker compose down -v` 清库重来**，半成品库改参数也不会自己修好 |
| Web 打开了但报数据库错 | Web 比 Server 先起，表还没建完 | 等 Server 建完表再刷 |
| Server 日志**每 15 秒**刷一条 `... on host "Zabbix server" failed: network error` | 内置主机想去连 `127.0.0.1:10050`，而 `zabbix-server` 容器里没有 agent | **不是故障**，无视即可（本方案不监控 WSL2 自己）。想让它变绿得另起一个本地 agent 容器 |
| 同样的报错，但 host 名是 `myblog-server` | 隧道断了 / 白名单不对 / 接口 IP 填错 | **这才是真问题** → 按下面六步查 |
| 主机 ZBX 图标一直红 | ①隧道没起 ②白名单 IP 不对 ③agent 没起 ④接口 IP 填错 | 按下面的六步逐层查，**别跳步** |
| `systemctl status zabbix-tunnel` 是 `failed` | 10050 被占 / 密钥路径不对 / 首次连接要确认主机密钥（`BatchMode` 下会直接失败） | `journalctl -u zabbix-tunnel -n 30 --no-pager` |
| 隧道 `active (running)` 但监控没数据 | **假死**：连接被校园网/NAT 掐了，进程还挂着 | `ss -tlnp \| grep 10050` 看端口在不在；`systemctl restart zabbix-tunnel` 立即恢复。**7.4 的心跳参数就是治它的** |
| 隧道反复重启 | 端口 10050 被占，或转发一直失败 | `journalctl -u zabbix-tunnel -n 50`；`ss -tlnp \| grep 10050` 看谁占了 |
| 容器连不上 `172.28.0.1:10050` | `-L` 绑成了 `127.0.0.1` 而不是 `0.0.0.0` | 看 unit 文件里 `-L` 那行；这也是 7.2 手动测试第 ② 步的意义 |
| agent 日志 `connection from X rejected` | `Server=` 白名单和实际来源 IP 不匹配 | **日志里的那个 X 就是答案**，加进 `ZBX_SERVER_HOST` 后 `docker compose up -d` 重建 |
| 隧道通但主机还是红 | 模板选成了 active 版 | 换成 `Linux by Zabbix agent`（不带 active） |
| 有数据但图表是空的 | 才刚开始采，history 还没攒够 | 等 10 分钟再看 |
| `docker compose up` 报端口被占 | WSL2 里 8080 被别的东西占了 | `ss -tlnp \| grep 8080`，改 compose 里的对外端口 |
| 云服务器内存告警 / 博客变慢 | agent 没设上限或和其它组件抢内存 | `docker stats` 看实际占用；agent 应该稳定在 30M 左右 |

### 连不通时的逐层排查顺序

**从云服务器那头，往 WSL2 这头，一层一层查**——顺序反了会把时间浪费在已经正常的地方：

```
① 云服务器上 agent 容器活着吗？        docker ps | grep zabbix-agent
② 云服务器本机 10050 在听吗？           ss -tlnp | grep 10050
③ WSL2 上隧道在跑吗？                   systemctl status zabbix-tunnel
④ WSL2 上 10050 在听吗？                ss -tlnp | grep 10050
⑤ 从容器视角那个地址连得上吗？           /dev/tcp/172.28.0.1/10050
⑥ agent 认不认你（白名单）？             docker logs myblog-zabbix-agent | tail -30
```

> ⑤ 全通、⑥ 也不报 rejected，但图标还是红 → 回去看阶段五填的接口 IP 是不是 `172.28.0.1`、端口是不是 `10050`。

### 一键收集诊断信息

出问题时**先收集、再动手**——新手最常见的错误是上来就改，改了一通问题没解决，反而把现场破坏了。

**在云服务器上**（记得走 WSL：`wsl -- bash -lc "ssh -i ~/.ssh/myblog_deploy root@<服务器IP> '<命令>'"`）：

```bash
echo "=== 内存 ==="   ; free -h
echo "=== 容器 ==="   ; docker ps -a --format 'table {{.Names}}\t{{.Status}}'
echo "=== 端口 ==="   ; ss -tlnp | grep -E '10050|8081|8080'
echo "=== agent 日志 ===" ; docker logs --tail=30 myblog-zabbix-agent
echo "=== agent 配置 ===" ; docker exec myblog-zabbix-agent \
  grep -E '^(Server|ServerActive|Hostname)' /etc/zabbix/zabbix_agent2.conf
```

**在 WSL2 上**：

```bash
echo "=== 隧道状态 ===" ; systemctl status zabbix-tunnel --no-pager
echo "=== 隧道日志 ===" ; journalctl -u zabbix-tunnel -n 30 --no-pager
echo "=== 本地端口 ===" ; ss -tlnp | grep 10050
echo "=== Zabbix 组件 ===" ; docker compose -f ~/zabbix/docker-compose.yml ps
```

---

## 12. 常见误解

**① 「Agent 会分析数据」**
不会。Agent 只取值。判断「90% 算不算高」是 Server 做的事。这也是为什么模板换个版本，Agent 那边一行都不用改。

**② 「Web 是监控系统的一部分」**
不是。Web 只是个读写数据库的界面，关掉它监控照跑、告警照发。

**③ 「Zabbix 要装在每台被监控机上」**
只有 **Agent** 要装在被监控机上（30 MB）。**Server 一套就够**，管一百台机器也是它。

**④ 「主动/被动是被监控机的主动被动」**
是**从 Agent 的视角**说的：被动 = Agent 等着被问；主动 = Agent 主动上报。同一台 Agent 上两种可以共存，取决于每个监控项的类型。

**⑤ 「端口通了就是监控通了」**
不一定。端口通只说明网络层通了，还要过**白名单**那一关（`Server=`），再看**模板**选没选对。三层都过，图标才会绿。

**⑥ 「隧道要在安全组开端口」**
不用。SSH 隧道走的是 **22 端口**——那个端口本来就是开的（你一直在用它做 Ansible 部署）。**整个方案没有新增任何一个对外端口**，10050 自始至终只存在于两台机器的回环地址上。

**⑦ 「进程还在跑，说明它没问题」**
恰恰相反，这是本方案里最需要警惕的一句话。隧道**假死**的时候 `systemctl status` 是绿的、容器 **Up** 也是绿的，只有数据停了。**判断一个东西活着，要看它有没有在干活，不是看进程在不在**——这就是为什么 7.5 要真的 `kill` 一次来验证自愈。

---

## 13. 这份经历能写进简历什么

做完这一套，`运维实习简历-项目经历.md` 里就能真实地加上这些（**在真正跑通并验收之后**）：

- 搭建 Zabbix 监控体系，采用**服务端 / Agent 分离**架构：监控服务部署于本地 WSL2，Agent 部署于生产服务器，通过被动模式跨公网采集
- 在内存仅剩 **468 MiB** 的生产服务器上，以容器化方式部署监控 Agent 并限制资源占用（`mem_limit` 64 M），**监控组件自身内存占用控制在 30 MB 以内**
- 因校园网出口 IP 动态且多人共享，**放弃 IP 白名单方案**，改用 **SSH 本地端口转发（`ssh -L`）** 打通跨公网采集链路；用 **systemd 托管隧道进程**（`Restart=always` + 心跳保活 + `ExitOnForwardFailure`），实现**断线自愈与开机自启**，并实测验证过自愈能力
- 被监控端口**全程仅绑定回环地址**，未向公网新增任何开放端口；通过 docker 自定义网络固定网段，使白名单来源 IP 可预测，避免动态 IP 导致的静默失效
- 针对业务设计**双层可用性监控**：Agent 侧端口探测（`net.tcp.service`）+ Server 侧 HTTP 端到端探测，覆盖从网络层到应用层的完整链路
- 通过**故障注入验证告警有效性**：主动停服确认触发器触发与自动恢复

> **这几条的价值在于它们都经得起追问**：
> - 问「为什么 Server 装在本地不装服务器上」→ 内存只剩 468 M 这个具体数字 + NAT 方向这个具体原因
> - 问「你的 IP 会变怎么办」→ 所以没用白名单，用隧道；隧道是本地主动发起的，跟出口 IP 无关
> - 问「隧道断了怎么办」→ systemd `Restart=always` + 心跳保活，而且我真 `kill` 过一次看它自己回来
> - 问「怎么验证告警有效」→ 手动停服验过
>
> **最后一条特别值得主动讲**：「没验证过的机制等于不存在」。隧道自愈、触发器告警，这类东西都是**你以为它有、出事时才发现它没有**——所以都要实际触发一次。

---

## 14. 备选方案：如果不是校园网（家宽或固定公网 IP）

> **什么时候用这节**：你的网络有**一个不常变的公网出口 IP**（家宽、公司专线、或者你自己有一台固定 IP 的机器）。
> **什么时候别用**：校园网、大内网、4G/5G 热点、任何「IP 会变或多人共用」的环境 —— 那就老实走主方案（隧道）。

**这节是主方案阶段四的替代品。**阶段一、二、三、五、六**全部照抄**，只有下面四处不一样。

### 14.1 差异总览

| | 主方案（校园网 → 隧道） | 本节（固定 IP → 白名单） |
|---|---|---|
| 云上 agent 的 `ports` | `"127.0.0.1:10050:10050"` 只绑回环 | `"10050:10050"` **对公网开放** |
| 云上 agent 的 `ZBX_SERVER_HOST` | `"127.0.0.1,172.29.0.1"` | **你家的公网 IP** |
| 阶段四做什么 | 建 SSH 隧道 + systemd 托管 | **去阿里云控制台开安全组** |
| 阶段五的接口 IP | `172.28.0.1` | **`<服务器IP>`** |
| 多出来的组件 | 一个隧道进程 | 无 |
| 长期要维护什么 | 隧道断了要管 | **IP 变了要改两处** |

### 14.2 第一步：查出你家的公网 IP

```bash
# 在 WSL2 里执行
curl -s https://ifconfig.me; echo
```

> WSL2 的出口 IP 和你 Windows 主机是**同一个**（都从家里路由器出去），所以在哪台机器上跑结果都一样。

### 14.3 第二步：云服务器 agent 的 compose 改成这样

```yaml
services:
  zabbix-agent:
    image: zabbix/zabbix-agent2:alpine-7.0-latest
    container_name: myblog-zabbix-agent
    restart: unless-stopped
    environment:
      ZBX_HOSTNAME: myblog-server
      # ★ 换成 14.2 查到的那个公网 IP
      ZBX_SERVER_HOST: "<你家公网IP>"
      ZBX_PASSIVE_ALLOW: "true"
      ZBX_ACTIVE_ALLOW: "false"
    ports:
      # ★ 和主方案的区别：不再绑 127.0.0.1，要等外面的人连进来
      - "10050:10050"
    mem_limit: 64m
```

**和主方案比，只改了两处**：`ZBX_SERVER_HOST` 换成公网 IP，`ports` 不再绑 `127.0.0.1`。

> **另外注意：这里不需要钉网段了。**
> 主方案之所以要 `ipam` 钉死 `172.29.0.0/24`，是因为 agent 的白名单要填「容器看到的来源 IP」= docker 网关，不钉就不知道填什么。
> 这里白名单填的是**你的公网 IP**，跟 docker 网关一点关系没有，所以整段 `networks:` 都可以不写。

### 14.4 第三步：阿里云安全组放行 10050

阿里云控制台 → 云服务器 ECS → 实例 → **安全组** → 配置规则 → **入方向** → 手动添加：

| 项 | 值 |
|---|---|
| 协议类型 | 自定义 TCP |
| 端口范围 | **10050/10050** |
| 授权对象 | **`<你家公网IP>/32`** ← **必须带 `/32`** |
| 描述 | Zabbix Agent（仅允许本机 WSL2 访问） |

> ### ⚠️ 千万不要图省事填 `0.0.0.0/0`
>
> Zabbix Agent 的被动模式**默认没有任何认证**。谁连上 10050，谁就能读走你服务器的全部指标——CPU、内存、磁盘、进程列表、容器列表、网卡信息。
>
> **这等于把服务器的体检报告挂在公网上。**
>
> 如果确实需要长期开放又嫌管 IP 麻烦，正确做法是给 Agent 配 **PSK 加密**，而不是放开来源。

**验证**：

```bash
# 在 WSL2 里测 TCP 层
timeout 5 bash -c 'cat < /dev/null > /dev/tcp/<服务器IP>/10050' \
  && echo "✅ 端口通" || echo "❌ 端口不通"
```

不通就按顺序查（**别跳步**）：

```
① 云服务器上 agent 容器活着吗？     docker ps | grep zabbix-agent
② 云服务器本机 10050 在听吗？        ss -tlnp | grep 10050
③ 云服务器本机自己能连吗？           /dev/tcp/127.0.0.1/10050
④ 安全组规则加了吗？来源 IP 对不对？（①②③ 都通，问题只可能在这）
```

然后看 agent 认不认你：

```bash
# 在云服务器上
docker logs myblog-zabbix-agent | tail -30
```

看到 `connection from X.X.X.X rejected`，说明白名单和实际来源对不上——**那个 X 就是真实来源，照着改**。

### 14.5 第四步：阶段五的接口 IP 改填公网地址

`Data collection（数据采集）→ Hosts（主机）→ myblog-server → Interfaces（接口）`：

| 字段 | 主方案 | 本节 |
|---|---|---|
| IP address（IP 地址） | `172.28.0.1` | **`<服务器IP>`** |
| Port（端口） | `10050` | `10050`（不变） |

> **为什么这里可以直接填公网 IP**：因为没有隧道了。Zabbix Server 容器直接走公网去连服务器的 10050——这条路是通的，前提是 14.4 的安全组放行了。
>
> 对比一下主方案：那时填公网 IP 反而连不上，因为 agent 只绑了 `127.0.0.1`，公网那个 10050 根本没开。

### 14.6 长期要维护的地方（就这一处）

| 会变的东西 | 变了之后要改哪 |
|---|---|
| **你家公网 IP** | ① 阿里云安全组的授权对象 ② agent 的 `ZBX_SERVER_HOST`（改完 `docker compose up -d` 重建） |

> 家宽 IP 通常**不是"固定"，只是"很少变"**——重启光猫、运营商重新分配都可能换。
>
> 好消息是**它变了监控会明确地断**：Web 里主机会变红、触发器会报警，**不是静默失效**。到时候 `curl ifconfig.me` 看一眼就知道。

### 14.7 说句公道话：这个方案不比隧道好

**它只是少一个组件**（不用 systemd 管隧道），代价是实打实的三条：

| | 隧道 | 安全组 + 白名单 |
|---|---|---|
| 10050 暴露在公网 | ❌ 完全不暴露 | ✅ 暴露，靠白名单挡 |
| 传输加密 | ✅ SSH 加密 | ❌ 明文 |
| 认证 | ✅ 密钥 | ❌ Agent 本身无认证 |
| 要维护 IP | ❌ 不用 | ✅ 要 |

所以**如果你已经是校园网，没必要为了"少个组件"切过来**。这节真正的用途是两个：

1. 以后换到固定网络环境（家里、公司专线）时，知道该改哪几处
2. **面试时能讲清楚「两种做法各自适合什么场景」**——这比只会一种更值钱

> 顺带一句：这段正是「**同一个目标，方案取决于环境约束**」的典型例子。目标没变（让 Server 能取到 agent 的数据），变的只是"你的网络有没有一个别人能找到的地址"。**约束变了，方案就得跟着变**——这也是为什么 1.4 里那三条校园网问题必须一开始就问清楚。

---

## 15. 兜底：笔记本关机时的告警

> **什么时候看这节**：Zabbix Server 跑在你笔记本的 WSL2 里，**笔记本一关，监控和告警就一起停**（1.2 节那句「代价只有一个」）。
> 这节就是给那一句打补丁。**可选加餐。**

> **⚠️ 本节的设计取舍（2026-10-11）**
> 补这个洞有两条路：**在云服务器上装个探活脚本**，或者**从服务器外部探测**。
> 本节选后者，理由见 15.1。前者（脚本方案）已撤下，仓库里不再保留。

### 15.1 为什么在「外部」探，而不是在服务器上装脚本

原方案长这样：

```
   云服务器（被监控的机器本人）
 ┌──────────────────────────────────────┐
 │ nginx + 后端                          │
 │ 探活脚本   每分钟 curl 127.0.0.1      │
 └──────────────────────────────────────┘
```

它有一个自己补不了的洞：

> **服务器整台挂了（欠费停机、宿主机故障、系统崩溃）时，探活脚本也在这台机器上，它一起死。没有人收到通知。**

补救的办法只有一个：**判断方必须在被监控的机器之外。**

而一旦这么想，就会发现 —— **原方案是多余的**：

| 出什么事 | 服务器上的探活脚本（内部探） | 站点监控（外部探） |
|---|---|---|
| nginx 挂了 | ✅ | ✅ |
| 后端挂了（502） | ✅ | ✅ |
| 网线断了 / 网络不通 | ❌ 探本机探不出来 | ✅ |
| **整台机器挂了 / 欠费停机** | ❌ **它自己也死了** | ✅ |

**外部探测一个人就覆盖了脚本方案的全部场景，还多覆盖两种**，而且更省事：不占服务器内存、不用装 systemd timer、不用在服务器上放 SMTP 授权码。

> 顺带说清这个兜底到底值多少：**它要花的钱是「笔记本关机那段时间的停机损失」。**
> 而那段时间你多半在睡觉，**读者也在睡觉**——损失本来就很小。
> 所以正确做法是**花 5 分钟配个外部探测**，而不是上一套自建系统。
> 别拿一个大组件去解决一个小问题。

### 15.2 怎么做：阿里云云监控 → 站点监控

**云监控（CloudMonitor）→ 站点监控（Site Monitor）→ 创建任务**

| 字段 | 填什么 |
|---|---|
| 任务类型（Type） | HTTP(S) |
| 监控地址（URL） | `http://<服务器IP>:8081/api/config/all` |
| 探测点（Probe Point） | 选 2~3 个（华东、华北各挑一个） |
| 探测频率（Interval） | **5 分钟**（默认值，够用） |
| 断言（Assertion） | 响应状态码 = 200 |

再配**报警规则**：探测失败 → 通知「报警联系组」。
通知方式里**邮件**默认就有；**短信**更实在（手机在手边）。
★ 联系人的手机 / 邮箱**必须先验证**，没验证的收不到。

### 15.3 ★ 建之前先知道这三条

**① 站点监控从 2022-08-31 起不再免费，改为按量计费。**

按探测次数计费、按日结算。你这个量（1 个任务、5 分钟一次）花不了几个钱，**但它是收费的**。
控制台创建时页面上会显示费用，**以那里的最新口径为准**（我查到的是这个说法，没有实际创建过）。

**② 任务有有效期，到期会「静默停止」。 ← 这条最危险**

任务到期之后**它不会报警，它只是不再探测了**。你会以为自己有监控，实际上早就没有。
这正是本文档反复强调的那句「**没验证过的机制等于不存在**」。

→ **创建完立刻记下到期日，设个日历提醒。**

**③ 如果你不想要一个「要花钱 + 会过期」的东西**，外面有专做这件事的免费服务：

| 服务 | 特点 |
|---|---|
| **UptimeRobot** | 免费版就能 5 分钟探一次、邮件告警，**不过期**；缺点是探针在境外 |
| **Healthchecks.io** | 思路反过来：让你的任务定时「报到」，多久没报到就告警 |

**取舍**：阿里云的在同一个控制台里、能发短信，但要钱、会过期；外面的免费、不过期，但探针不在国内。

### 15.4 验证：看不见的配置不算数

规矩和 9.5 一样 —— **配完必须真停一次服务**：

```bash
cd /opt/myblog
docker compose stop nginx     # ① 停掉
# ② 等 5~10 分钟（等探测 + 等报警规则触发）
docker compose start nginx    # ③ 起回来
```

| 动作 | 预期 |
|---|---|
| 停掉 nginx，等 5~10 分钟 | 手机 / 邮箱**真的**收到告警 |
| 起回来，再等一轮 | 收到恢复通知 |

**没收到，按顺序查这三处**：探测点选了没 → 报警规则挂到联系组了没 → 联系人的手机 / 邮箱验证过了没。

> 这和 9.5 是同一个道理：**点了 `Test` 能收到测试邮件，只证明「管道通了」，证明不了「管道接在了正确的地方」。**

---

## 附：和 `运维练习方案.md` 的关系

| | 本文档 | 运维练习方案 |
|---|---|---|
| 在哪跑 | **WSL2 + 真实云服务器** | 本地虚拟机 |
| 目的 | **给真实项目加上监控**（做完能写简历） | 系统练整套运维技能 |
| Zabbix 定位 | 生产可用的最小闭环 | 完整练习（含多机、API、模板导出） |
| 内存约束 | 云服务器只有 468 M，**必须精打细算** | 虚拟机 4 G，宽裕 |

**建议顺序**：先按本文档把「真实项目的监控」跑通——**因为它是真的在监控生产环境，遇到的问题是真实的**。练熟之后再去 `运维练习方案.md` 补 Zabbix 的进阶部分（自定义模板导出、API 自动化、多机管理）。

---

*文档版本：2026-10-07 初稿。基于实测环境编写：WSL2 Ubuntu 26.04（7.1 G 可用）+ 阿里云 ECS CentOS 8（468 M 可用），WSL2 侧为校园网（动态分配 IP，故采用 SSH 隧道而非 IP 白名单）。*
