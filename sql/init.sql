-- MySQL dump 10.13  Distrib 8.0.40, for Win64 (x86_64)
--
-- Host: localhost    Database: myblog
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
-- Current Database: `myblog`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `myblog` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `myblog`;

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
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-10-07 15:10:27
