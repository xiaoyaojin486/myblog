-- MySQL dump 10.13  Distrib 8.0.40, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: myblog
-- ------------------------------------------------------
-- Server version	8.0.40

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `about_experience`
--

DROP TABLE IF EXISTS `about_experience`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `about_experience` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(100) COLLATE utf8mb4_general_ci NOT NULL COMMENT '经历标题',
  `organization` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '组织/机构',
  `start_date` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '开始时间',
  `end_date` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '结束时间',
  `description` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '描述',
  `sort` int DEFAULT '0' COMMENT '排序',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='关于我-经历';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `about_experience`
--

LOCK TABLES `about_experience` WRITE;
/*!40000 ALTER TABLE `about_experience` DISABLE KEYS */;
INSERT INTO `about_experience` VALUES (11,'独立开发个人博客系统 ','个人项目','2026-6','','- 独立开发个人博客系统 ｜ 个人项目 ｜ 2026-10 ~ 至今\n- 描述：采用 Spring Boot 3 + MyBatis + MySQL + Redis + Vue 3 的前后端分离架构，后端按业务域拆分为多 Maven 模块、跨模块只通过 Api 接口调用；实现 Markdown 文章与草稿/发布、分类标签、项目展示、评论提交与审核、点赞（Redis 防重复）与周榜热度、站点信息与个人资料配置等模块，并接入阿里云 OSS 图片存储。',0,'2026-10-06 12:26:06','2026-10-06 12:26:06');
/*!40000 ALTER TABLE `about_experience` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `about_skill`
--

DROP TABLE IF EXISTS `about_skill`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `about_skill` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(50) COLLATE utf8mb4_general_ci NOT NULL COMMENT '技能名称',
  `level_label` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '分类标签',
  `percent` int NOT NULL DEFAULT '0' COMMENT '掌握程度(%)',
  `sort` int DEFAULT '0' COMMENT '排序',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=48 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='关于我-技能栈';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `about_skill`
--

LOCK TABLES `about_skill` WRITE;
/*!40000 ALTER TABLE `about_skill` DISABLE KEYS */;
INSERT INTO `about_skill` VALUES (41,'Java','后端',80,0,'2026-10-06 12:26:06','2026-10-06 12:26:06'),(42,'Spring Boot','后端',70,1,'2026-10-06 12:26:06','2026-10-06 12:26:06'),(43,'MyBatis','后端',72,2,'2026-10-06 12:26:06','2026-10-06 12:26:06'),(44,'MySQL','数据库',75,3,'2026-10-06 12:26:06','2026-10-06 12:26:06'),(45,'Redis','中间件',65,4,'2026-10-06 12:26:06','2026-10-06 12:26:06'),(46,'Vue3','前端',70,5,'2026-10-06 12:26:06','2026-10-06 12:26:06'),(47,'Maven / Git','工具',68,6,'2026-10-06 12:26:06','2026-10-06 12:26:06');
/*!40000 ALTER TABLE `about_skill` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `article`
--

DROP TABLE IF EXISTS `article`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `article` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(200) COLLATE utf8mb4_general_ci NOT NULL COMMENT '标题',
  `content` longtext COLLATE utf8mb4_general_ci NOT NULL COMMENT '内容(Markdown)',
  `summary` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '摘要',
  `cover_image` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '封面图',
  `category_id` bigint DEFAULT NULL COMMENT '分类ID',
  `view_count` int DEFAULT '0' COMMENT '浏览量',
  `like_count` int DEFAULT '0' COMMENT '点赞数',
  `word_count` int NOT NULL DEFAULT '0' COMMENT '字数（不含空白，近似）',
  `status` tinyint DEFAULT '0' COMMENT '状态:0草稿,1已发布',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_status_create_time` (`status`,`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='文章表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `article`
--

LOCK TABLES `article` WRITE;
/*!40000 ALTER TABLE `article` DISABLE KEYS */;
INSERT INTO `article` VALUES (2,'Hello World：我的博客开张了','# 欢迎来到我的博客\n\n这是第一篇文章，站点刚刚上线。\n\n后续会在这里记录技术与生活。','博客上线的第一篇文章',NULL,4,10,1,37,1,'2026-10-06 01:49:08','2026-10-06 14:37:55'),(3,'Spring Boot 多模块架构实践笔记','# 多模块拆分\n\n按业务功能拆分 api 与 impl，父 POM 统一管理版本。','多模块单体架构落地要点',NULL,3,18,1,32,1,'2026-10-06 01:49:08','2026-10-06 16:47:27'),(4,'Vue3 组件通信笔记','草稿内容','props / emit / provide-inject','https://hu-tlias.oss-cn-beijing.aliyuncs.com/myblog/1/article/2026/10/d1b5e67762f54b54abec6ec2436471fd.jpg',3,9,1,4,0,'2026-10-06 01:49:08','2026-10-06 11:45:05'),(8,'Linux 服务器日常巡检：先看这四类指标','\n## 背景\n\n接手一台陌生服务器，或者例行巡检时，先看这几类指标：负载、内存、磁盘、网络。比一上来就翻日志效率高很多。\n\n## 系统与负载\n\n```bash\nuname -a                 # 内核版本\ncat /etc/os-release      # 发行版\nuptime                   # 负载与运行时长\nnproc                    # CPU 核数\n```\n\n`uptime` 的三个数字是 1、5、15 分钟的平均负载。判断是否偏高要和 CPU 核数对比：单核负载长期大于 1 就要留意，超过核数说明有排队。\n\n## 内存\n\n```bash\nfree -h\n```\n\n重点看 `available` 而不是 `free`——`buff/cache` 是可以被回收的，只看 free 会误判。\n\n## 磁盘\n\n```bash\ndf -h                     # 各分区使用率\ndf -i                     # inode 使用率\ndu -sh /var/* | sort -h   # 逐层定位大目录\n```\n\ninode 用满时即使还有空间也写不进文件，别漏看。\n\n## 网络与端口\n\n```bash\nss -lntp                  # 监听端口与对应进程\nss -s                     # 连接状态汇总\n```\n\n## 进程\n\n```bash\nps aux --sort=-%mem | head -10\nps aux --sort=-%cpu | head -10\n```\n\n## 小结\n\n先过一遍「负载 / 内存 / 磁盘 / 网络」，再针对异常项深入，巡检就变成了有方向的排查。\n','负载、内存、磁盘、网络四类指标的系统巡检命令清单，以及负载多少才算高的判断方法。','https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=linux%20server%20terminal%20command%20line%20dark%20theme%2C%20professional%20tech%20illustration&image_size=landscape_4_3',5,0,0,223,1,'2026-10-06 13:16:13','2026-10-06 13:16:13'),(9,'Linux 磁盘和 inode 被占满的排查思路','\n## 现象\n\n两条常见报错：\n\n- `No space left on device`，但 `df -h` 看着还有空间\n- 写小文件失败，`df -i` 的 IUse% 已经是 100%\n\n## 第一步：确认是空间还是 inode\n\n```bash\ndf -h        # 空间\ndf -i        # inode\n```\n\n分别对应两种完全不同的处理方式。\n\n## 第二步：定位大目录\n\n从根往下逐层找，不要直接 `du -sh /`：\n\n```bash\ndu -xh --max-depth=1 / | sort -h\ndu -xh --max-depth=1 /var | sort -h\n```\n\n常见大户：`/var/log`、`/var/lib/docker`、应用日志目录。\n\n## 第三步：已删除但未释放的文件\n\n进程还在写一个已被 `rm` 的文件时，空间不会释放。用 `lsof` 找：\n\n```bash\nlsof +L1 | sort -k7 -n\n```\n\n处理方式：重启对应进程，或者用 `> /proc/PID/fd/N` 清空（谨慎操作）。\n\n## 第四步：inode 耗尽\n\ninode 被大量小文件占满时：\n\n```bash\nfind /var -xdev -type f | wc -l\nfind /path -xdev -type f -printf \'%h\\n\' | sort | uniq -c | sort -rn | head\n```\n\n典型场景：session 文件、缓存目录、日志切分碎片。\n\n## 收尾\n\n找到根因后再清理，并给日志加上轮转（logrotate），否则过一阵还会复发。\n','df 显示还有空间却写不进文件？从大目录定位、已删除但被占用的文件到 inode 耗尽逐层排查。','https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=server%20storage%20rack%20disk%20array%20in%20data%20center%2C%20blue%20tech%20lighting&image_size=landscape_4_3',5,1,0,261,1,'2026-10-06 13:16:13','2026-10-06 13:16:24'),(10,'Ansible 入门：inventory 与 ad-hoc 常用模块','\n## 安装与免密\n\n控制端安装后，先打通 SSH 免密，这是 Ansible 能工作的前提：\n\n```bash\nssh-keygen -t ed25519\nssh-copy-id root@192.168.1.11\n```\n\n## inventory 主机清单\n\n```ini\n[web]\nweb1 ansible_host=192.168.1.11\nweb2 ansible_host=192.168.1.12\n\n[db]\ndb1 ansible_host=192.168.1.21\n\n[all:vars]\nansible_user=root\n```\n\n分组之后就可以按组批量操作。\n\n## 连通性测试\n\n```bash\nansible all -i hosts.ini -m ping\n```\n\n返回 SUCCESS 说明免密与 Python 环境都正常。\n\n## 常用 ad-hoc 命令\n\n```bash\n# 执行命令\nansible web -i hosts.ini -m shell -a \'uptime\'\n\n# 传文件\nansible web -i hosts.ini -m copy -a \'src=./app.conf dest=/etc/app.conf mode=0644\'\n\n# 管理服务\nansible web -i hosts.ini -m service -a \'name=nginx state=restarted\'\n\n# 装包\nansible web -i hosts.ini -m yum -a \'name=htop state=present\'\n```\n\n## 幂等性\n\nAnsible 模块是幂等的：目标状态已满足时不会重复执行。这也是它比裸写 shell 脚本更安全的地方。\n\n## 小结\n\nad-hoc 适合临时批量操作，重复性的部署流程还是应该写成 Playbook。\n','inventory 主机清单写法、免密配置，以及 ping/shell/copy/service 等最常用 ad-hoc 命令。','https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=ansible%20automation%20configuration%20management%20concept%2C%20abstract%20network%20nodes&image_size=landscape_4_3',5,2,0,196,1,'2026-10-06 13:16:13','2026-10-06 14:03:10'),(11,'用 Ansible Playbook 批量部署 Nginx','\n## 目标\n\n把「安装 Nginx + 下发配置 + 启动」写成幂等的 Playbook，一次编写、批量执行。\n\n## 目录结构\n\n```text\ndeploy-nginx/\n├── nginx.yml\n├── hosts.ini\n└── templates/\n    └── nginx.conf.j2\n```\n\n## Playbook\n\n```yaml\n---\n- name: deploy nginx\n  hosts: web\n  become: true\n  vars:\n    worker_processes: auto\n\n  tasks:\n    - name: install nginx\n      yum:\n        name: nginx\n        state: present\n\n    - name: render nginx.conf\n      template:\n        src: templates/nginx.conf.j2\n        dest: /etc/nginx/nginx.conf\n        mode: \'0644\'\n      notify: reload nginx\n\n    - name: ensure nginx running\n      service:\n        name: nginx\n        state: started\n        enabled: true\n\n  handlers:\n    - name: reload nginx\n      service:\n        name: nginx\n        state: reloaded\n```\n\n## 为什么用 handlers\n\n`notify` 只在配置真正发生变化时才触发 reload，避免每次执行都无谓地重启服务。\n\n## 执行\n\n```bash\nansible-playbook -i hosts.ini nginx.yml --check   # 先干跑\nansible-playbook -i hosts.ini nginx.yml\n```\n\n`--check` 可以先看会改哪些东西，再决定是否真正执行。\n\n## 小结\n\n把「命令」换成「状态」，Playbook 就能反复执行而不产生副作用。\n','一个可直接套用的 Playbook：装包、下发配置、校验、启动，并用 handlers 只在配置变更时重载。','https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=nginx%20web%20server%20load%20balancing%20architecture%20diagram%2C%20clean%20tech%20style&image_size=landscape_4_3',5,0,0,163,1,'2026-10-06 13:16:13','2026-10-06 13:16:13'),(12,'Zabbix 监控部署与主机自动发现','\n## 架构\n\nZabbix 分为 Server（含 Web 与数据库）和 Agent（被监控端）。小规模环境三件套装一台机器即可。\n\n## 服务端\n\n```bash\n# 以 MySQL 作为后端库\nyum install -y zabbix-server-mysql zabbix-web-mysql zabbix-apache-conf zabbix-sql-scripts\n\nmysql -uroot -p -e \"CREATE DATABASE zabbix CHARACTER SET utf8mb4;\"\nmysql -uroot -p -e \"CREATE USER \'zabbix\'@\'localhost\' IDENTIFIED BY \'zabbix_pwd\';\"\nmysql -uroot -p -e \"GRANT ALL ON zabbix.* TO \'zabbix\'@\'localhost\';\"\n\nzcat /usr/share/zabbix-sql-scripts/mysql/server.sql.gz | mysql -uzabbix -p zabbix\n```\n\n填好 `/etc/zabbix/zabbix_server.conf` 的数据库信息后启动服务，浏览器访问 `/zabbix` 完成初始化。\n\n## 被监控端\n\n```bash\nyum install -y zabbix-agent\n# 指向 Server 地址，并配置 Hostname 与主机名一致\nsed -i \'s/^Server=.*/Server=192.168.1.10/\' /etc/zabbix/zabbix_agentd.conf\nsystemctl enable --now zabbix-agent\n```\n\n## 主机自动发现\n\n在「Configuration → Discovery」新增规则：\n\n- IP range：`192.168.1.1-254`\n- Checks：`Zabbix agent \"system.uname\"`\n- 关联动作：自动 Add host、Link to template（`Linux by Zabbix agent`）、Enable host\n\n这样新增机器只要 Agent 起来就会自动纳入监控，不必逐台手加。\n\n## 小结\n\n监控的价值在于「自动纳入 + 有意义的告警」，先把发现和模板做扎实，再谈告警调优。\n','Server / Agent 安装、Web 初始化，以及用自动发现规则免去逐台添加主机。','https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=zabbix%20monitoring%20dashboard%20with%20server%20graphs%20and%20alerts%2C%20dark%20ui&image_size=landscape_4_3',5,0,0,253,1,'2026-10-06 13:16:13','2026-10-06 13:16:13'),(13,'Zabbix 自定义监控项与触发器实践','\n## 场景\n\nZabbix 自带模板覆盖了 CPU、内存、磁盘，但业务指标（比如 Nginx 活跃连接数）需要自己采集。\n\n## 第一步：被监控端写采集脚本\n\n```bash\ncat > /etc/zabbix/scripts/nginx_conn.sh <<\'EOF\'\n#!/bin/bash\ncurl -s http://127.0.0.1/nginx_status | awk \'/Active connections/ {print $3}\'\nEOF\nchmod +x /etc/zabbix/scripts/nginx_conn.sh\n```\n\n前提是 Nginx 已开启 status 页：\n\n```nginx\nlocation /nginx_status {\n    stub_status;\n    allow 127.0.0.1;\n    deny all;\n}\n```\n\n## 第二步：注册 UserParameter\n\n在 `/etc/zabbix/zabbix_agentd.d/userparameter_nginx.conf` 中：\n\n```ini\nUserParameter=nginx.conn.active,/etc/zabbix/scripts/nginx_conn.sh\n```\n\n重启 Agent 后用 `zabbix_get` 验证：\n\n```bash\nzabbix_get -s 127.0.0.1 -k nginx.conn.active\n```\n\n## 第三步：配置监控项\n\n- Name：Nginx active connections\n- Type：Zabbix agent\n- Key：`nginx.conn.active`\n- Type of information：Numeric (unsigned)\n- Update interval：30s\n\n## 第四步：触发器\n\n```text\n{Template App Nginx:nginx.conn.active.last()} > 3000\n```\n\n建议加 `min()` 或持续时间，避免瞬时尖峰误报：\n\n```text\n{...last()} > 3000 and {...min(5m)} > 3000\n```\n\n## 小结\n\n自定义监控本身不难，难的是「阈值定得合理」。先观察一段时间的基线，再设告警。\n','用 UserParameter 采集自定义指标（如 Nginx 连接数），并配置合理的触发器阈值。','https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=monitoring%20alert%20threshold%20trigger%20gauge%20dashboard%2C%20orange%20accent&image_size=landscape_4_3',5,1,0,291,1,'2026-10-06 13:16:13','2026-10-06 13:19:48'),(14,'Nginx 反向代理与负载均衡配置笔记','\n## 反向代理\n\n```nginx\nserver {\n    listen 80;\n    server_name example.com;\n\n    location / {\n        proxy_pass http://127.0.0.1:8080;\n        proxy_set_header Host              $host;\n        proxy_set_header X-Real-IP         $remote_addr;\n        proxy_set_header X-Forwarded-For   $proxy_add_x_forwarded_for;\n        proxy_set_header X-Forwarded-Proto $scheme;\n    }\n}\n```\n\n这几个头很关键：后端拿不到真实 IP，多半就是漏了 `X-Real-IP`。\n\n## 负载均衡\n\n```nginx\nupstream backend {\n    least_conn;\n    server 192.168.1.11:8080 weight=1 max_fails=3 fail_timeout=10s;\n    server 192.168.1.12:8080 weight=2;\n    server 192.168.1.13:8080 backup;\n}\n```\n\n常用策略：\n\n- `round-robin`：默认，轮询\n- `least_conn`：优先发给连接数最少的节点\n- `ip_hash`：同一客户端固定到同一节点，适合有本地会话的场景\n\n## WebSocket 支持\n\n```nginx\nlocation /ws/ {\n    proxy_pass http://backend;\n    proxy_http_version 1.1;\n    proxy_set_header Upgrade    $http_upgrade;\n    proxy_set_header Connection \"upgrade\";\n    proxy_read_timeout 300s;\n}\n```\n\n## 常用超时\n\n```nginx\nproxy_connect_timeout 5s;\nproxy_send_timeout    60s;\nproxy_read_timeout    60s;\nsend_timeout          60s;\n```\n\n## 小结\n\n代理配置改动后先 `nginx -t` 校验，再 `reload`，避免直接重启导致连接中断。\n','upstream 负载均衡策略、代理头透传、WebSocket 支持，以及常用超时参数。','https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=nginx%20reverse%20proxy%20network%20flow%20diagram%2C%20minimal%20blue%20illustration&image_size=landscape_4_3',5,0,0,124,1,'2026-10-06 13:16:13','2026-10-06 13:16:13'),(15,'Docker 常用命令与容器排障清单','\n## 镜像与容器\n\n```bash\ndocker images                     # 本地镜像\ndocker ps -a                      # 所有容器（含已退出）\ndocker run -d --name app -p 8080:8080 app:1.0\ndocker exec -it app /bin/sh       # 进容器\ndocker logs -f --tail=200 app     # 看日志\n```\n\n## 排查一：容器起来就退出\n\n先看退出码和日志：\n\n```bash\ndocker ps -a    # STATUS 列能看到 Exited (1) 之类的退出码\ndocker logs app\n```\n\n常见原因：前台进程没跑起来、配置文件路径错、启动命令写成了 &&。\n\n## 排查二：端口不通\n\n```bash\ndocker port app                   # 映射关系\nss -lntp | grep 8080              # 宿主机是否在监听\ndocker exec app ss -lntp          # 容器内是否在监听\n```\n\n若容器内只监听 `127.0.0.1`，宿主机映射也访问不到——应用要监听 `0.0.0.0`。\n\n## 排查三：资源占用\n\n```bash\ndocker stats --no-stream\ndocker inspect app | grep -i -A3 Health\n```\n\n## 排查四：磁盘被镜像和日志吃掉\n\n```bash\ndocker system df\ndocker system prune -a            # 清理未使用的镜像与容器（谨慎）\n```\n\n容器日志建议配置 `max-size` 与 `max-file` 轮转。\n\n## 小结\n\n容器问题大多落在三处：启动命令、端口监听地址、日志。按这个顺序排，通常几分钟就能定位。\n','镜像/容器/日志/资源占用的高频命令，以及容器起来就退、端口不通的排查顺序。','https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=docker%20containers%20orchestration%20blue%20whale%20concept%2C%20modern%20tech&image_size=landscape_4_3',5,1,0,170,1,'2026-10-06 13:16:13','2026-10-06 15:33:48'),(16,'Shell 脚本：日志清理与目录自动备份','\n## 日志清理脚本\n\n```bash\n#!/bin/bash\nset -euo pipefail\n\nLOG_DIR=/var/log/app\nKEEP_DAYS=7\n\nfind \"$LOG_DIR\" -type f -name \'*.log\' -mtime +$KEEP_DAYS -print -delete\n\necho \"$(date \'+%F %T\') 清理完成：$LOG_DIR 中超过 $KEEP_DAYS 天的日志\"\n```\n\n`set -euo pipefail` 建议养成习惯：出错立即退出，变量未定义也报错，避免误删。\n\n## 备份脚本\n\n```bash\n#!/bin/bash\nset -euo pipefail\n\nSRC=/data/app\nDST=/backup\nDATE=$(date +%F)\n\nmkdir -p \"$DST\"\ntar -zcf \"$DST/app-$DATE.tar.gz\" -C \"$SRC\" .\n\n# 只保留最近 15 份\nls -1t \"$DST\"/app-*.tar.gz | tail -n +16 | xargs -r rm -f\n\necho \"备份完成：$DST/app-$DATE.tar.gz\"\n```\n\n## 定时执行\n\n```cron\n# 每天 02:30 清理日志，03:00 备份\n30 2 * * * /opt/scripts/clean_log.sh >> /var/log/clean_log.log 2>&1\n0  3 * * * /opt/scripts/backup.sh    >> /var/log/backup.log 2>&1\n```\n\n## 小结\n\n脚本要「可重复执行、输出可追溯」。加日志、加保留份数上限，比一次性写得漂亮重要得多。\n','两个可直接用的脚本：按天数清理日志、按日期打包备份，并配 crontab 定时执行。','https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=shell%20script%20code%20on%20screen%20automation%2C%20terminal%20green%20text&image_size=landscape_4_3',5,2,0,86,1,'2026-10-06 13:16:13','2026-10-06 14:41:00'),(17,'Prometheus + Grafana 监控入门与告警','\n## 与 Zabbix 的思路差异\n\nPrometheus 采用「拉取」模型：由 Prometheus 主动定时抓取各 exporter 暴露的 /metrics 接口，天然适配容器与动态环境。\n\n## 接入节点监控\n\n在被监控端启动 node_exporter，然后在 `prometheus.yml` 中配置：\n\n```yaml\nscrape_configs:\n  - job_name: node\n    static_configs:\n      - targets: [\'192.168.1.11:9100\', \'192.168.1.12:9100\']\n```\n\n## 常用 PromQL\n\n```text\n# CPU 使用率\n100 - (avg by(instance) (rate(node_cpu_seconds_total{mode=\"idle\"}[5m])) * 100)\n\n# 内存使用率\n(1 - node_memory_MemAvailable_bytes / node_memory_MemTotal_bytes) * 100\n\n# 磁盘使用率\n100 - (node_filesystem_avail_bytes{fstype!~\"tmpfs\"} / node_filesystem_size_bytes{fstype!~\"tmpfs\"} * 100)\n```\n\n`rate()` 用于计数器类型指标，取单位时间增量，是 PromQL 里最常用的函数。\n\n## Grafana 出图\n\n添加 Prometheus 为数据源后，直接导入现成面板（如 Node Exporter Full），几分钟就能有完整仪表盘。\n\n## 告警链路\n\n```text\nPrometheus 规则 → Alertmanager → 邮件 / 企业微信 / Webhook\n```\n\n先在 `rules.yml` 里定义触发条件，再由 Alertmanager 负责分组、静默与通知。\n\n## 小结\n\nPrometheus 强在指标与容器生态，Zabbix 强在开箱即用的主机监控。选哪个取决于现有环境，不必二选一。\n','拉取式采集模型、node_exporter 接入、PromQL 常用查询与 Alertmanager 告警链路。','https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=prometheus%20grafana%20monitoring%20dashboard%20charts%2C%20purple%20gradient%20ui&image_size=landscape_4_3',5,1,0,320,1,'2026-10-06 13:16:13','2026-10-06 13:44:05'),(18,'Kubernetes 核心概念与 Pod 排障顺序','\n## 三个核心对象\n\n- Pod：最小调度单位，一个或多个容器\n- Deployment：管理 Pod 副本数与滚动更新\n- Service：为一组 Pod 提供稳定的访问入口\n\n```bash\nkubectl get pods -o wide\nkubectl get svc\nkubectl describe pod <pod-name>\nkubectl logs <pod-name> --previous   # 看上一个崩溃实例的日志\n```\n\n## 排障一：Pending\n\n说明没有被调度。`describe` 看 Events：\n\n- 资源不足 → 调 requests/limits 或扩容节点\n- 节点亲和 / 污点不匹配 → 检查 tolerations 与 nodeSelector\n\n## 排障二：ImagePullBackOff\n\n镜像拉不下来：镜像名或 tag 写错、私有仓库缺 imagePullSecret、网络不通。\n\n```bash\nkubectl describe pod <pod-name> | grep -A5 Events\n```\n\n## 排障三：CrashLoopBackOff\n\n容器起来又退出，反复重启。先看日志，再看退出码：\n\n```bash\nkubectl logs <pod-name> --previous\nkubectl describe pod <pod-name> | grep -i \'exit code\'\n```\n\n常见原因：配置缺失、依赖服务连不上、启动命令错误。\n\n## 排障四：Service 访问不通\n\n```bash\nkubectl get endpoints <svc-name>     # 为空说明 selector 没匹配到 Pod\nkubectl get pod --show-labels\n```\n\nService 靠 label selector 找 Pod，标签对不上就没有 endpoints。\n\n## 小结\n\nK8s 排障几乎都从 `describe` 和 `logs` 开始，先看清 Events 和退出码，再谈下一层。\n','Pod / Deployment / Service 的关系，以及 CrashLoopBackOff、ImagePullBackOff、Pending 的排查路径。','https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=kubernetes%20cluster%20pods%20orchestration%20diagram%2C%20blue%20abstract&image_size=landscape_4_3',5,1,0,387,1,'2026-10-06 13:16:13','2026-10-06 13:43:46'),(19,'MySQL 慢查询定位与索引优化实战','\n## 第一步：打开慢查询日志\n\n```sql\nSET GLOBAL slow_query_log = ON;\nSET GLOBAL long_query_time = 1;\nSET GLOBAL log_queries_not_using_indexes = ON;\nSHOW VARIABLES LIKE \'slow_query_log_file\';\n```\n\n线上建议把 `long_query_time` 设为 1 秒，并配合 `mysqldumpslow` 或 pt-query-digest 汇总。\n\n## 第二步：EXPLAIN 看执行计划\n\n```sql\nEXPLAIN SELECT id, title FROM article WHERE status = 1 ORDER BY create_time DESC LIMIT 10;\n```\n\n重点看四列：\n\n- `type`：至少要到 `range`，出现 `ALL` 说明全表扫描\n- `key`：实际使用的索引，为 NULL 就是没走索引\n- `rows`：预估扫描行数，越小越好\n- `Extra`：出现 `Using filesort` / `Using temporary` 要警惕\n\n## 第三步：加对索引\n\n按「等值列 + 排序列」的顺序建联合索引：\n\n```sql\nALTER TABLE article ADD INDEX idx_status_create_time (status, create_time);\n```\n\n这条索引正好覆盖上例的 `WHERE status = 1 ORDER BY create_time DESC`，可以同时消除过滤和排序的开销。\n\n## 常见索引失效场景\n\n- 对索引列做运算或函数：`WHERE DATE(create_time) = \'2026-10-01\'`\n- 隐式类型转换：字符串列用数字查询\n- 联合索引违背最左前缀\n- `LIKE \'%关键字\'` 的前置模糊匹配\n\n## 小结\n\n优化的顺序是：先定位慢 SQL，再看执行计划，最后才动手加索引。盲目加索引只会拖慢写入。\n','慢查询日志开启、EXPLAIN 关键字段解读，以及最左前缀、回表、索引失效的常见坑。','https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=database%20performance%20tuning%20sql%20query%20optimization%2C%20dark%20blue%20theme&image_size=landscape_4_3',5,30,0,267,1,'2026-10-06 13:16:13','2026-10-06 16:53:08');
/*!40000 ALTER TABLE `article` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `article_tag`
--

DROP TABLE IF EXISTS `article_tag`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `article_tag` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `article_id` bigint NOT NULL COMMENT '文章ID',
  `tag_id` bigint NOT NULL COMMENT '标签ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_article_tag` (`article_id`,`tag_id`)
) ENGINE=InnoDB AUTO_INCREMENT=36 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='文章-标签关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `article_tag`
--

LOCK TABLES `article_tag` WRITE;
/*!40000 ALTER TABLE `article_tag` DISABLE KEYS */;
INSERT INTO `article_tag` VALUES (11,2,7),(12,3,5),(13,3,6),(15,4,6),(16,8,10),(17,8,11),(18,9,10),(19,9,11),(20,10,12),(21,10,13),(22,11,12),(23,11,14),(24,12,15),(25,12,16),(26,13,15),(27,13,16),(28,14,14),(29,15,17),(31,16,13),(30,16,18),(33,17,16),(32,17,19),(34,18,20),(35,19,21);
/*!40000 ALTER TABLE `article_tag` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `category`
--

DROP TABLE IF EXISTS `category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `category` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(50) COLLATE utf8mb4_general_ci NOT NULL COMMENT '分类名',
  `sort` int DEFAULT '0' COMMENT '排序(越小越靠前)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='分类表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `category`
--

LOCK TABLES `category` WRITE;
/*!40000 ALTER TABLE `category` DISABLE KEYS */;
INSERT INTO `category` VALUES (3,'技术分享',1,'2026-10-06 01:49:08','2026-10-06 01:49:08'),(4,'生活随笔',2,'2026-10-06 01:49:08','2026-10-06 01:49:08'),(5,'运维',1,'2026-10-06 13:16:13','2026-10-06 13:16:13');
/*!40000 ALTER TABLE `category` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `comment`
--

DROP TABLE IF EXISTS `comment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `comment` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `article_id` bigint NOT NULL COMMENT '文章ID',
  `nickname` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '昵称(匿名)',
  `email` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '邮箱',
  `content` varchar(500) COLLATE utf8mb4_general_ci NOT NULL COMMENT '内容',
  `parent_id` bigint DEFAULT NULL COMMENT '父评论ID',
  `user_id` bigint DEFAULT NULL COMMENT 'blogger user id, null for guests',
  `status` tinyint DEFAULT '1' COMMENT 'status:0 pending,1 approved,2 rejected',
  `ip` varchar(45) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'submitter ip',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_article_id` (`article_id`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='评论表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `comment`
--

LOCK TABLES `comment` WRITE;
/*!40000 ALTER TABLE `comment` DISABLE KEYS */;
INSERT INTO `comment` VALUES (3,3,'访客小明',NULL,'写得不错，学到了！',NULL,NULL,1,NULL,'2026-10-06 02:54:06'),(8,3,'逍遥津',NULL,'你也可以试试',3,1,1,NULL,'2026-10-06 16:30:02'),(10,19,'匿名',NULL,'不是，哥们',NULL,NULL,1,'127.0.0.1','2026-10-06 16:52:04');
/*!40000 ALTER TABLE `comment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `moment`
--

DROP TABLE IF EXISTS `moment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `moment` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `content` varchar(1000) COLLATE utf8mb4_general_ci NOT NULL COMMENT '动态内容',
  `images` varchar(2000) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '图片URL(逗号分隔,最多9张)',
  `status` tinyint DEFAULT '1' COMMENT '状态:0隐藏,1显示',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='最新动态表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `moment`
--

LOCK TABLES `moment` WRITE;
/*!40000 ALTER TABLE `moment` DISABLE KEYS */;
/*!40000 ALTER TABLE `moment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `project`
--

DROP TABLE IF EXISTS `project`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `project` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) COLLATE utf8mb4_general_ci NOT NULL COMMENT '项目名称',
  `description` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '项目简介',
  `content` longtext COLLATE utf8mb4_general_ci COMMENT '详细介绍(Markdown)',
  `cover_image` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '封面图',
  `tech_stack` varchar(200) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '技术栈(逗号分隔，如 Vue3,Spring Boot,MySQL)',
  `github_url` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '源码地址',
  `demo_url` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '演示地址',
  `sort` int DEFAULT '0' COMMENT '排序(越小越靠前)',
  `progress` tinyint DEFAULT '1' COMMENT 'progress:0 planning,1 in progress,2 completed,3 paused',
  `status` tinyint DEFAULT '1' COMMENT '状态:0下架,1上架',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_status_sort` (`status`,`sort`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='项目展示表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `project`
--

LOCK TABLES `project` WRITE;
/*!40000 ALTER TABLE `project` DISABLE KEYS */;
INSERT INTO `project` VALUES (1,'myblog 个人博客系统','','','https://hu-tlias.oss-cn-beijing.aliyuncs.com/myblog/1/project/2026/10/beb3292adbd6408c85b34e1624a4c5f9.jpg','','','',1,1,1,'2026-10-06 02:14:06','2026-10-07 15:08:37'),(2,'待办清单示例项目','一个练习性质的全栈小项目示例','# 示例项目\n\n用于演示项目展示模块。','','Vue3,Spring Boot','','',2,1,0,'2026-10-06 02:14:06','2026-10-06 10:01:22');
/*!40000 ALTER TABLE `project` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_config`
--

DROP TABLE IF EXISTS `sys_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_config` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `config_key` varchar(100) COLLATE utf8mb4_general_ci NOT NULL COMMENT '配置键',
  `config_value` text COLLATE utf8mb4_general_ci COMMENT '配置值',
  `remark` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `config_key` (`config_key`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='系统配置表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_config`
--

LOCK TABLES `sys_config` WRITE;
/*!40000 ALTER TABLE `sys_config` DISABLE KEYS */;
INSERT INTO `sys_config` VALUES (1,'site_name','逍遥津的博客','站点名称'),(2,'site_description','','站点描述'),(3,'site_avatar','https://hu-tlias.oss-cn-beijing.aliyuncs.com/myblog/1/avatar/2026/10/b9dc9ce84077461598ddcce4a0069102.jpg','博主头像URL'),(4,'icp','1213131','ICP备案号'),(5,'github_url','https://github.com/xiaoyaojin486','GitHub 地址'),(6,'email','husmart666@163.com','联系邮箱'),(7,'about_content','## 关于我\n\n你好，我是逍遥津，这个博客的主人。这里记录我的学习笔记、项目实践和技术思考，偶尔也写写生活。\n\n## 我在做什么\n\n- 独立开发并维护本站：从数据库设计、后端接口到前端页面，全部自己动手\n- 技术方向：Java 后端（Spring Boot / MyBatis / MySQL / Redis）+ Vue 3 前端\n- 正在学习和补强：分布式与常用中间件\n\n## 关于这个博客\n\n本站是前后端分离的个人博客，后端采用多 Maven 模块架构：\n\n- 后端：Spring Boot 3 + MyBatis + MySQL + Redis + JWT；按业务域拆分为「聚合 pom + xxx-api + xxx-impl」，\n  跨模块调用只通过 XxxApi 接口，模块之间不直接依赖实现\n- 前端：Vue 3 + Vite + Element Plus + Pinia，分前台展示端与后台管理端，支持日夜主题切换\n- 存储：图片统一上传到阿里云 OSS，路径按「业务类型 / 年月」归档，便于管理\n- 已实现：Markdown 文章与草稿/发布、分类与标签、项目展示、评论提交与审核、\n  点赞（Redis 防重复）与周榜热度、站点信息与个人资料配置、关于我内容维护等\n\n## 写博客的初心\n\n把学到的东西讲清楚、把踩过的坑记下来。写不明白的地方，往往就是还没真正掌握的地方。','关于我-自我介绍(Markdown)'),(8,'about_resume_url','','关于我-简历文件地址');
/*!40000 ALTER TABLE `sys_config` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_user`
--

DROP TABLE IF EXISTS `sys_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_user` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户名',
  `password` varchar(100) COLLATE utf8mb4_general_ci NOT NULL COMMENT '密码(BCrypt加密)',
  `nickname` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '昵称',
  `avatar` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '头像URL',
  `email` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '邮箱',
  `signature` varchar(200) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '个人签名',
  `github` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'GitHub 地址',
  `gitee` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'Gitee 地址',
  `juejin` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '掘金地址',
  `csdn` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'CSDN 地址',
  `wechat_qr` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '微信二维码图片URL',
  `status` tinyint DEFAULT '1' COMMENT '状态:0禁用,1启用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_user`
--

LOCK TABLES `sys_user` WRITE;
/*!40000 ALTER TABLE `sys_user` DISABLE KEYS */;
INSERT INTO `sys_user` VALUES (1,'admin','$2a$10$n.m1NR8zrAtuMn/.bvfVGe25y3EUxehpIPUI.cdl3mbIhk.cdj1Pq','逍遥津','https://hu-tlias.oss-cn-beijing.aliyuncs.com/myblog/1/avatar/2026/10/95d63b041ab9472686038b4038152758.jpg','husmart666@163.com','记录学习 · 分享生活','https://github.com/xiaoyaojin486','','','','',1,'2026-10-06 01:20:48','2026-10-07 15:08:58');
/*!40000 ALTER TABLE `sys_user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tag`
--

DROP TABLE IF EXISTS `tag`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tag` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(50) COLLATE utf8mb4_general_ci NOT NULL COMMENT '标签名',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='标签表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tag`
--

LOCK TABLES `tag` WRITE;
/*!40000 ALTER TABLE `tag` DISABLE KEYS */;
INSERT INTO `tag` VALUES (5,'Java','2026-10-06 01:49:08'),(6,'Vue3','2026-10-06 01:49:08'),(7,'随笔','2026-10-06 01:49:08'),(10,'Linux','2026-10-06 13:16:13'),(11,'运维','2026-10-06 13:16:13'),(12,'Ansible','2026-10-06 13:16:13'),(13,'自动化','2026-10-06 13:16:13'),(14,'Nginx','2026-10-06 13:16:13'),(15,'Zabbix','2026-10-06 13:16:13'),(16,'监控','2026-10-06 13:16:13'),(17,'Docker','2026-10-06 13:16:13'),(18,'Shell','2026-10-06 13:16:13'),(19,'Prometheus','2026-10-06 13:16:13'),(20,'Kubernetes','2026-10-06 13:16:13'),(21,'MySQL','2026-10-06 13:16:13');
/*!40000 ALTER TABLE `tag` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tool_category`
--

DROP TABLE IF EXISTS `tool_category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tool_category` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '分类名',
  `sort` int NOT NULL DEFAULT '0' COMMENT '排序（越小越靠前）',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态:0停用,1启用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_status_sort` (`status`,`sort`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='运维工具箱-分类';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tool_category`
--

LOCK TABLES `tool_category` WRITE;
/*!40000 ALTER TABLE `tool_category` DISABLE KEYS */;
INSERT INTO `tool_category` VALUES (1,'Linux 基础',1,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(2,'系统运维',2,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(3,'网络排障',3,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(4,'文本处理',4,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(5,'Docker',5,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(6,'Kubernetes',6,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(7,'Git',7,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(8,'CI/CD',8,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(9,'Helm',9,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(10,'Nginx/Web',10,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(11,'数据库',11,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(12,'Python 工具',12,1,'2026-10-15 21:41:40','2026-10-15 21:41:40');
/*!40000 ALTER TABLE `tool_category` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tool_command`
--

DROP TABLE IF EXISTS `tool_command`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tool_command` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用途标题',
  `command` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '命令原文',
  `category_id` bigint NOT NULL COMMENT '分类ID',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '说明/示例/注意事项',
  `risk_level` tinyint NOT NULL DEFAULT '0' COMMENT '风险:0只读,1变更,2高危',
  `sort` int NOT NULL DEFAULT '0' COMMENT '排序（越小越靠前）',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态:0下架,1上架',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_status_sort` (`status`,`sort`),
  KEY `idx_category` (`category_id`)
) ENGINE=InnoDB AUTO_INCREMENT=402 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='运维工具箱-命令';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tool_command`
--

LOCK TABLES `tool_command` WRITE;
/*!40000 ALTER TABLE `tool_command` DISABLE KEYS */;
INSERT INTO `tool_command` VALUES (201,'查看当前目录','pwd',1,'打印当前工作目录的绝对路径',0,1,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(202,'列出文件详情','ls -lah',1,'以人类可读大小列出全部文件（含隐藏文件）',0,2,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(203,'切换目录','cd /path/to/dir',1,'进入指定目录',0,3,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(204,'返回上级目录','cd ..',1,'回到上一级目录',0,4,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(205,'查看文件类型','file /path/to/file',1,'识别文件真实类型',0,5,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(206,'查看文件内容','cat /path/to/file',1,'直接输出文件全部内容',0,6,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(207,'查看文件开头','head -n 50 /path/to/file',1,'查看文件前 50 行',0,7,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(208,'查看文件末尾','tail -n 50 /path/to/file',1,'查看文件后 50 行',0,8,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(209,'实时查看文件追加内容','tail -f /path/to/file',1,'持续跟踪日志新增内容',0,9,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(210,'创建空文件','touch /path/to/file',1,'创建空文件或更新时间戳',1,10,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(211,'创建多级目录','mkdir -p /path/to/dir',1,'递归创建目录',1,11,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(212,'复制文件或目录','cp -a /src /dst',1,'保留属性复制文件或目录',1,12,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(213,'移动或重命名','mv /src /dst',1,'移动文件或重命名',1,13,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(214,'删除文件','rm /path/to/file',1,'删除文件（不可恢复）',2,14,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(215,'递归强制删除','rm -rf /path/to/dir',1,'强制递归删除目录（高危，不可恢复）',2,15,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(216,'查看文件权限','ls -l /path/to/file',1,'查看属主、属组与权限位',0,16,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(217,'修改文件权限','chmod 644 /path/to/file',1,'修改文件权限模式',1,17,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(218,'修改文件属主','chown user:group /path/to/file',1,'修改文件属主与属组（需 root）',1,18,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(219,'查看目录占用大小','du -sh /path/to/dir',1,'统计目录总占用大小',0,19,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(220,'查看目录大小排行','du -xh --max-depth=1 /path | sort -h',1,'逐层定位大目录',0,20,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(221,'按名称查找文件','find /path -type f -name \'*.log\'',1,'按文件名通配查找文件',0,21,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(222,'按修改时间查找文件','find /path -mtime -1 -type f',1,'查找最近一天内修改的文件',0,22,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(223,'查看磁盘与分区','lsblk',1,'列出块设备与挂载点',0,23,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(224,'创建软链接','ln -s /src /link',1,'为文件或目录创建软链接',1,24,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(225,'查看命令帮助','man ls',1,'查看命令手册',0,25,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(226,'查看内核信息','uname -a',2,'内核版本与主机信息',0,1,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(227,'查看发行版信息','cat /etc/os-release',2,'查看操作系统发行版',0,2,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(228,'查看运行时长与负载','uptime',2,'1/5/15 分钟平均负载与运行时长',0,3,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(229,'查看 CPU 核数','nproc',2,'可用 CPU 核心数',0,4,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(230,'查看内存使用','free -h',2,'重点看 available 而非 free',0,5,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(231,'查看磁盘使用率','df -h',2,'各分区空间使用率',0,6,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(232,'查看 inode 使用率','df -i',2,'inode 用满时仍有空间也写不进文件',0,7,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(233,'查看全部进程','ps aux',2,'列出全部进程',0,8,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(234,'内存占用前十进程','ps aux --sort=-%mem | head -10',2,'按内存排序取前十',0,9,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(235,'CPU 占用前十进程','ps aux --sort=-%cpu | head -10',2,'按 CPU 排序取前十',0,10,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(236,'实时资源监控','top',2,'实时查看 CPU/内存/进程',0,11,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(237,'查看进程树','pstree -p',2,'树状展示进程父子关系',0,12,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(238,'优雅结束进程','kill -15 <pid>',2,'发送终止信号，等待进程退出',2,13,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(239,'强制结束进程','kill -9 <pid>',2,'强制杀死进程（可能丢数据）',2,14,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(240,'查看服务状态','systemctl status <service>',2,'查看 systemd 服务运行状态',0,15,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(241,'启动服务','systemctl start <service>',2,'启动指定服务',1,16,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(242,'重启服务','systemctl restart <service>',2,'重启指定服务',1,17,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(243,'设置开机自启','systemctl enable --now <service>',2,'设置开机自启并立即启动',1,18,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(244,'查看服务日志','journalctl -u <service> -n 200 --no-pager',2,'查看最近 200 行服务日志',0,19,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(245,'实时跟踪服务日志','journalctl -u <service> -f',2,'持续跟踪服务日志',0,20,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(246,'查看内核日志','dmesg -T | tail -50',2,'查看内核环形缓冲日志',0,21,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(247,'查看开机耗时','systemd-analyze blame',2,'各服务启动耗时排行',0,22,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(248,'查看监听端口','ss -lntp',3,'列出监听中的 TCP 端口与进程',0,1,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(249,'连接状态汇总','ss -s',3,'TCP/UDP 连接状态统计',0,2,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(250,'查看网卡地址','ip addr',3,'查看本机全部网卡与 IP',0,3,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(251,'查看路由表','ip route',3,'查看路由与默认网关',0,4,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(252,'测试连通性','ping -c 4 <host>',3,'发送 4 个 ICMP 包测试连通',0,5,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(253,'路由追踪','traceroute <host>',3,'逐跳追踪到目标的路径',0,6,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(254,'测试端口连通','telnet <host> <port>',3,'测试目标端口是否可连',0,7,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(255,'用 nc 测端口','nc -zv <host> <port>',3,'扫描并测试端口连通',0,8,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(256,'查询 DNS 解析','dig <domain> +short',3,'查看域名解析记录',0,9,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(257,'查看域名解析','nslookup <domain>',3,'交互式查询 DNS 解析',0,10,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(258,'抓取指定端口报文','tcpdump -i eth0 -nn port 80',3,'抓取 eth0 上 80 端口报文',0,11,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(259,'查看 HTTP 响应头','curl -I <url>',3,'获取响应头（含状态码）',0,12,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(260,'只取 HTTP 状态码','curl -s -o /dev/null -w \"%{http_code}\" <url>',3,'静默请求并输出状态码',0,13,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(261,'下载文件','curl -O <url>',3,'按原文件名下载',1,14,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(262,'查看本机对外 IP','curl ifconfig.me',3,'查询出口公网 IP',0,15,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(263,'查看防火墙规则','firewall-cmd --list-all',3,'查看 firewalld 全部规则',0,16,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(264,'网卡流量统计','sar -n DEV 1 5',3,'每秒采样网卡流量，共 5 次',0,17,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(265,'查看 ARP 表','ip neigh',3,'查看邻居 ARP 缓存',0,18,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(266,'统计文件行数','wc -l /path/to/file',4,'统计行数',0,1,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(267,'统计字符数','wc -c /path/to/file',4,'统计字节数',0,2,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(268,'递归关键字搜索','grep -rn \'keyword\' /path',4,'递归查找并显示文件名与行号',0,3,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(269,'忽略大小写搜索','grep -rin \'keyword\' /path',4,'忽略大小写递归搜索',0,4,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(270,'反向匹配行','grep -v \'pattern\' /path/to/file',4,'输出不匹配的行',0,5,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(271,'显示匹配行号','grep -n \'pattern\' /path/to/file',4,'显示匹配行及其行号',0,6,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(272,'显示匹配上下文','grep -C 3 \'pattern\' /path/to/file',4,'显示匹配行前后各 3 行',0,7,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(273,'流式替换输出','sed \'s/old/new/g\' /path/to/file',4,'全局替换并输出（不改原文件）',0,8,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(274,'删除匹配行','sed \'/pattern/d\' /path/to/file',4,'删除匹配行后输出',0,9,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(275,'查看指定行区间','sed -n \'10,20p\' /path/to/file',4,'打印第 10~20 行',0,10,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(276,'提取首列字段','awk \'{print $1}\' /path/to/file',4,'按空白分割取第一列',0,11,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(277,'按分隔符取字段','cut -d\':\' -f1,3 /etc/passwd',4,'以冒号为分隔取第 1、3 列',0,12,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(278,'条件过滤行','awk \'$3 > 100 {print $0}\' /path/to/file',4,'按第三列数值过滤',0,13,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(279,'文件排序','sort /path/to/file',4,'按字典序排序',0,14,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(280,'数值排序','sort -n /path/to/file',4,'按数值大小排序',0,15,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(281,'去重并计数','sort /path/to/file | uniq -c',4,'排序去重并统计出现次数',0,16,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(282,'取前 N 行','head -n 20 /path/to/file',4,'查看前 20 行',0,17,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(283,'截取字段','cut -d\',\' -f1,3 /path/to/file',4,'以逗号分隔取第 1、3 列',0,18,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(284,'合并文件','cat a.txt b.txt > c.txt',4,'合并多个文件（会覆盖目标文件）',1,19,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(285,'多行合并为一行','paste -sd\',\' /path/to/file',4,'以逗号将多行合并为一行',0,20,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(286,'查看本地镜像','docker images',5,'列出本地镜像',0,1,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(287,'查看所有容器','docker ps -a',5,'含已退出的容器',0,2,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(288,'运行容器','docker run -d --name app -p 8080:8080 app:1.0',5,'后台运行并映射端口',1,3,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(289,'进入容器','docker exec -it app /bin/sh',5,'以交互方式进入容器',1,4,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(290,'跟踪容器日志','docker logs -f --tail=200 app',5,'查看并跟踪最近 200 行日志',0,5,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(291,'查看容器端口映射','docker port app',5,'查看端口映射关系',0,6,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(292,'查看容器资源占用','docker stats --no-stream',5,'一次性查看资源占用',0,7,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(293,'停止容器','docker stop app',5,'优雅停止容器',1,8,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(294,'启动容器','docker start app',5,'启动已存在容器',1,9,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(295,'重启容器','docker restart app',5,'重启容器',1,10,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(296,'删除容器','docker rm -f app',5,'强制删除运行中的容器',2,11,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(297,'删除镜像','docker rmi app:1.0',5,'删除本地镜像',2,12,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(298,'查看容器详情','docker inspect app',5,'查看容器完整配置与状态',0,13,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(299,'查看容器内监听端口','docker exec app ss -lntp',5,'查看容器内监听端口',0,14,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(300,'构建镜像','docker build -t app:1.0 .',5,'按 Dockerfile 构建镜像',1,15,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(301,'推送镜像','docker push app:1.0',5,'推送镜像到仓库',1,16,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(302,'登录镜像仓库','docker login registry.example.com',5,'登录私有镜像仓库',1,17,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(303,'查看磁盘占用','docker system df',5,'查看镜像/容器/卷占用',0,18,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(304,'清理未使用资源','docker system prune -a',5,'清理未使用镜像与容器（谨慎）',2,19,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(305,'导出容器文件系统','docker export app > app.tar',5,'导出容器文件系统为 tar',1,20,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(306,'查看 Pod 列表','kubectl get pods -o wide',6,'含节点与 IP 信息',0,1,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(307,'查看 Service','kubectl get svc',6,'列出全部服务',0,2,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(308,'查看 Deployment','kubectl get deploy',6,'列出全部部署',0,3,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(309,'查看全部资源','kubectl get all',6,'查看命名空间内全部资源',0,4,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(310,'查看 Pod 详情','kubectl describe pod <pod>',6,'含 Events 事件信息',0,5,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(311,'查看 Pod 日志','kubectl logs <pod>',6,'查看容器日志',0,6,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(312,'查看上次崩溃日志','kubectl logs <pod> --previous',6,'查看上一个崩溃实例的日志',0,7,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(313,'进入 Pod','kubectl exec -it <pod> -- /bin/sh',6,'在 Pod 内执行交互 shell',1,8,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(314,'查看 Endpoints','kubectl get endpoints <svc>',6,'为空说明 selector 未匹配到 Pod',0,9,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(315,'查看事件','kubectl get events --sort-by=.lastTimestamp',6,'按时间排序查看事件',0,10,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(316,'查看节点','kubectl get nodes -o wide',6,'列出节点及其状态',0,11,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(317,'查看节点资源','kubectl describe node <node>',6,'查看节点资源与污点',0,12,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(318,'应用清单','kubectl apply -f deploy.yaml',6,'创建或更新资源',1,13,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(319,'删除资源','kubectl delete -f deploy.yaml',6,'按清单删除资源',2,14,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(320,'滚动重启','kubectl rollout restart deploy/<name>',6,'触发 Deployment 滚动重启',1,15,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(321,'查看发布状态','kubectl rollout status deploy/<name>',6,'查看滚动更新进度',0,16,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(322,'回滚发布','kubectl rollout undo deploy/<name>',6,'回滚到上一版本',1,17,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(323,'扩缩容','kubectl scale deploy/<name> --replicas=3',6,'调整副本数',1,18,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(324,'查看当前上下文','kubectl config current-context',6,'查看当前集群上下文',0,19,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(325,'端口转发','kubectl port-forward svc/<svc> 8080:80',6,'将本地端口转发到服务',1,20,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(326,'查看工作区状态','git status',7,'查看改动与暂存状态',0,1,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(327,'查看提交历史','git log --oneline --graph -20',7,'图形化查看最近 20 次提交',0,2,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(328,'拉取远端更新','git pull origin main',7,'拉取并合并远端分支',1,3,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(329,'暂存全部改动','git add -A',7,'暂存所有改动',1,4,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(330,'提交改动','git commit -m \"msg\"',7,'提交暂存区改动',1,5,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(331,'推送到远端','git push origin main',7,'推送本地提交到远端',1,6,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(332,'查看分支','git branch -a',7,'列出本地与远端分支',0,7,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(333,'切换分支','git checkout <branch>',7,'切换到指定分支',1,8,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(334,'新建并切换分支','git checkout -b <branch>',7,'创建并切换到新分支',1,9,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(335,'合并分支','git merge <branch>',7,'将指定分支合并到当前分支',1,10,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(336,'暂存现场','git stash',7,'把未提交改动暂存起来',1,11,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(337,'恢复现场','git stash pop',7,'恢复最近一次暂存',1,12,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(338,'丢弃工作区改动','git restore <file>',7,'丢弃未提交修改（不可恢复）',2,13,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(339,'查看差异','git diff',7,'查看未暂存的改动差异',0,14,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(340,'查看文件提交历史','git log -p <file>',7,'查看指定文件的改动历史',0,15,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(341,'查看流水线运行','gh run list',8,'列出最近的工作流运行',0,1,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(342,'查看运行详情','gh run view <run-id>',8,'查看某次运行详情',0,2,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(343,'重新运行工作流','gh run rerun <run-id>',8,'重新运行失败的流水线',1,3,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(344,'手动触发工作流','gh workflow run deploy.yml',8,'触发指定工作流',1,4,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(345,'查看工作流列表','gh workflow list',8,'列出仓库内工作流',0,5,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(346,'查看 PR 列表','gh pr list',8,'列出开放中的 PR',0,6,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(347,'创建 PR','gh pr create --title \"title\" --body \"body\"',8,'创建拉取请求',1,7,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(348,'合并 PR','gh pr merge <pr> --squash',8,'以压缩方式合并 PR',1,8,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(349,'查看 Release','gh release list',8,'列出发布版本',0,9,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(350,'创建 Release','gh release create v1.0.0',8,'创建新发布版本',1,10,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(351,'添加 Chart 仓库','helm repo add bitnami https://charts.bitnami.com/bitnami',9,'添加 Helm 仓库',1,1,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(352,'更新仓库索引','helm repo update',9,'更新本地仓库索引',1,2,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(353,'搜索 Chart','helm search repo nginx',9,'在已添加仓库中搜索',0,3,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(354,'安装 Chart','helm install my-release bitnami/nginx',9,'安装一个 release',1,4,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(355,'升级 Release','helm upgrade my-release bitnami/nginx',9,'升级已安装的 release',1,5,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(356,'查看已安装 Release','helm list -A',9,'列出全部命名空间的 release',0,6,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(357,'回滚 Release','helm rollback my-release 1',9,'回滚到指定版本',1,7,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(358,'卸载 Release','helm uninstall my-release',9,'卸载 release',2,8,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(359,'校验配置','nginx -t',10,'测试配置文件语法',0,1,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(360,'重载配置','nginx -s reload',10,'平滑重载配置',1,2,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(361,'查看版本','nginx -v',10,'查看 Nginx 版本',0,3,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(362,'查看运行进程','ps -ef | grep nginx',10,'查看 Nginx 进程',0,4,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(363,'测试本机站点','curl -I http://127.0.0.1',10,'请求本机首页响应头',0,5,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(364,'跟踪访问日志','tail -f /var/log/nginx/access.log',10,'实时查看访问日志',0,6,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(365,'查看错误日志','tail -n 200 /var/log/nginx/error.log',10,'查看最近错误日志',0,7,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(366,'查看状态页','curl -s http://127.0.0.1/nginx_status',10,'查看 stub_status 状态页',0,8,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(367,'访问 TOP IP 统计','awk \'{print $1}\' /var/log/nginx/access.log | sort | uniq -c | sort -rn | head',10,'统计访问量最高的 IP',0,9,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(368,'查看 5xx 请求','grep \' 5[0-9][0-9] \' /var/log/nginx/access.log | tail',10,'筛选服务端错误请求',0,10,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(369,'查看证书有效期','openssl x509 -enddate -noout -in /path/cert.pem',10,'查看证书到期时间',0,11,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(370,'测试 SSL 连接','openssl s_client -connect <host>:443 -servername <host>',10,'测试 HTTPS 握手与证书链',0,12,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(371,'连接 MySQL','mysql -u root -p',11,'以交互方式登录数据库',1,1,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(372,'查看数据库列表','SHOW DATABASES;',11,'列出全部数据库',0,2,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(373,'切换数据库','USE myblog;',11,'切换到指定数据库',0,3,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(374,'查看表列表','SHOW TABLES;',11,'列出当前库的表',0,4,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(375,'查看表结构','DESC article;',11,'查看字段与类型',0,5,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(376,'查看建表语句','SHOW CREATE TABLE article;',11,'查看建表 SQL',0,6,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(377,'查看索引','SHOW INDEX FROM article;',11,'查看表的索引信息',0,7,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(378,'查看进程列表','SHOW PROCESSLIST;',11,'查看当前连接与执行语句',0,8,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(379,'查看连接数','SHOW STATUS LIKE \'Threads_connected\';',11,'当前连接线程数',0,9,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(380,'查看运行时长','SHOW STATUS LIKE \'Uptime\';',11,'MySQL 已运行秒数',0,10,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(381,'查看慢查询开关','SHOW VARIABLES LIKE \'slow_query_log\';',11,'查看慢查询日志状态',0,11,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(382,'开启慢查询','SET GLOBAL slow_query_log = ON;',11,'开启慢查询日志（全局）',1,12,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(383,'查看执行计划','EXPLAIN SELECT id, title FROM article WHERE status = 1;',11,'分析 SQL 执行计划',0,13,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(384,'备份单个数据库','mysqldump -u root -p myblog > myblog.sql',11,'导出数据库为 SQL 文件',1,14,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(385,'恢复数据库','mysql -u root -p myblog < myblog.sql',11,'从 SQL 文件导入数据（会写入）',2,15,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(386,'查看各表大小','SELECT table_name, ROUND((data_length+index_length)/1024/1024,2) mb FROM information_schema.tables WHERE table_schema=\'myblog\';',11,'查看各表占用空间',0,16,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(387,'查看当前用户','SELECT USER(), CURRENT_USER();',11,'查看当前连接用户',0,17,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(388,'查看未提交事务','SELECT * FROM information_schema.innodb_trx;',11,'查看活动事务与锁等待',0,18,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(389,'启动临时 HTTP 服务','python -m http.server 8000',12,'在当前目录启动简易下载服务',1,1,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(390,'格式化 JSON','python -m json.tool data.json',12,'校验并美化 JSON',0,2,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(391,'创建虚拟环境','python -m venv .venv',12,'创建项目虚拟环境',1,3,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(392,'激活虚拟环境','source .venv/bin/activate',12,'激活虚拟环境',1,4,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(393,'安装依赖','pip install -r requirements.txt',12,'按清单安装依赖',1,5,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(394,'导出依赖清单','pip freeze > requirements.txt',12,'导出当前依赖',1,6,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(395,'查看 Python 版本','python --version',12,'查看解释器版本',0,7,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(396,'查看包信息','pip show <package>',12,'查看已安装包详情',0,8,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(397,'列出已安装包','pip list',12,'列出全部已安装包',0,9,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(398,'端口探测','python -c \"import socket;print(socket.socket().connect_ex((\'127.0.0.1\',80)))\"',12,'返回 0 表示端口可连',0,10,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(399,'美化标准输入 JSON','python -c \"import json,sys;print(json.dumps(json.load(sys.stdin),indent=2,ensure_ascii=False))\" < data.json',12,'从标准输入美化 JSON',0,11,1,'2026-10-15 21:41:40','2026-10-15 21:41:40'),(400,'快速计算表达式','python -c \"print(2**10)\"',12,'快速计算表达式',0,12,1,'2026-10-15 21:41:40','2026-10-15 21:41:40');
/*!40000 ALTER TABLE `tool_command` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-10-16 10:37:51
