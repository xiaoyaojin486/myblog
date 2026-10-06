# MyBlog · 个人博客系统

一个前后端分离的个人博客系统，后端采用 **Maven 多模块单体架构**（Spring Boot 3 + MyBatis），前端采用 **Vue 3 + Vite + Element Plus**，图片资源存放在**阿里云 OSS**。

包含完整的前台展示端与后台管理端：文章、分类、标签、项目、评论、站点配置、个人资料、OSS 文件管理，以及 JWT 鉴权、Redis 点赞与热门周榜、全站搜索等能力。

---

## 目录

- [一、技术栈](#一技术栈)
- [二、功能特性](#二功能特性)
- [三、项目结构](#三项目结构)
- [四、环境要求](#四环境要求)
- [五、快速开始](#五快速开始)
- [六、配置说明](#六配置说明)
- [七、接口约定](#七接口约定)
- [八、数据库表](#八数据库表)
- [九、文件存储规范](#九文件存储规范)
- [十、构建与部署](#十构建与部署)
- [十一、常见问题](#十一常见问题)

---

## 一、技术栈

### 后端

| 分类 | 技术 | 版本 |
| --- | --- | --- |
| 语言 / 运行时 | Java | 21 |
| 框架 | Spring Boot | 3.2.5 |
| 持久层 | MyBatis（注解式 Mapper）+ PageHelper | 3.0.3 / 2.1.0 |
| 数据库 | MySQL | 8.x |
| 缓存 | Redis（点赞去重、热门周榜） | 6.x+ |
| 认证 | JJWT（HS256） | 0.12.5 |
| 对象转换 | MapStruct | 1.5.5.Final |
| 对象存储 | 阿里云 OSS SDK | 3.17.4 |
| 接口文档 | SpringDoc + Knife4j | 4.5.0 |
| 构建 | Maven（多模块） | 3.8+ |
| 其他 | Lombok、spring-security-crypto（BCrypt） | — |

### 前端

| 分类 | 技术 | 版本 |
| --- | --- | --- |
| 框架 | Vue | 3.4 |
| 构建 | Vite | 5.4 |
| UI 组件 | Element Plus | 2.8 |
| 状态管理 | Pinia | 2.2 |
| 路由 | Vue Router | 4.4 |
| HTTP | Axios | 1.7 |
| Markdown | markdown-it + @kangc/v-md-editor + highlight.js | 14.1 / 2.3 / 11.10 |
| 图表 | ECharts | 5.5 |
| 样式 | Sass | 1.77 |

### 架构约定

- 按业务域拆分为 `xxx-api`（对外契约）与 `xxx-impl`（内部实现），**跨模块调用只能依赖对方的 `XxxApi` 接口**，禁止直接依赖其他模块的实现类或数据库。
- 前端与后端统一接口前缀为 `/api`（由后端 `server.servlet.context-path` 统一提供，Controller 内不再重复书写）。
- 统一响应体 `Result<T>`，统一分页 `PageResult<T>`。

---

## 二、功能特性

### 前台展示端

| 模块 | 说明 |
| --- | --- |
| **首页** | 博主信息卡（头像、昵称、邮箱、GitHub、文章/分类/标签统计）、分页文章流、热门文章（总榜 / 周榜切换）、日历 |
| **文章详情** | Markdown 渲染与代码高亮；**右侧文章目录（TOC）**：从正文标题树生成、滚动高亮当前章节、点击跳转、吸顶跟随；点赞（Redis 去重，24h 内仅计一次）；标签；评论区 |
| **评论** | 树形展示、支持多级回复、博主回复带「博主」标识；匿名提交后进入待审核 |
| **归档 / 分类 / 标签** | 按时间归档，按分类与标签筛选文章 |
| **项目展示** | 项目列表与详情（Markdown 详细介绍、技术栈、源码/演示地址） |
| **全站搜索** | 同时搜索文章与项目，按模块分组展示并各自分页；采用**搜索提供者模式**（见下） |
| **关于我** | 技能与经历展示 |
| **外观** | 日夜主题切换、毛玻璃卡片、渐变光斑背景 |

> **搜索提供者模式**：`myblog-frontend/src/search/providers.js` 中每个可搜索模块注册一个 provider（`search` / `normalize`），搜索页与搜索框不感知具体业务。新增模块（如游戏）只需追加一个 provider，无需改动搜索页。

### 后台管理端

| 模块 | 说明 |
| --- | --- |
| **仪表盘** | 数据概览 |
| **文章管理** | Markdown 编辑器、封面与正文插图上传（自动入 OSS）、草稿 / 发布 / 下架 |
| **项目管理** | 项目 CRUD、封面上传、Markdown 详细介绍 |
| **分类管理 / 标签管理** | 基础数据维护 |
| **评论管理** | 待审核优先排序；通过 / 拒绝（区别于删除，记录保留）；批量通过 / 批量拒绝 / 批量删除；**以博主身份直接回复**（回复即通过）；博主标识；所属文章可点击跳转；记录提交者 IP |
| **文件管理** | 直接管理 OSS 中的文件：分页列表（按业务类型、文件名筛选）、图片缩略图预览、上传、重命名、移动业务目录、批量删除 |
| **站点信息** | 站点名称、站点描述、ICP 备案号 |
| **个人资料** | 头像上传、昵称 / 邮箱 / 签名 / GitHub 等社交链接、修改密码 |
| **关于我** | 技能与经历维护 |

### 安全机制

- **JWT 鉴权**：`JwtAuthFilter` 拦截 `/api/admin/**`，校验 `Authorization: Bearer <token>`，解析后写入 `UserContext`（ThreadLocal），请求结束清理；无 token 或 token 非法 / 过期直接返回 401。前台接口（`/api/article/**`、`/api/comment/**`、`/api/profile` 等）不受影响。
- **密码加密**：BCrypt 存储，登录校验与改密均走 `PasswordEncoder`。
- **对象存储防越权**：所有 OSS 写操作（删除、重命名、批量删除）统一校验对象 Key 必须以 `myblog/` 开头，无法操作 Bucket 内其他内容。
- **批量操作上限**：批量删除 / 批量审核单次最多 100 条。

---

## 三、项目结构

```
myblog-ai
├── pom.xml                     # 父 POM：统一版本管理
├── .env.example                # 环境变量模板（复制为 .env 后填写）
├── sql/init.sql                # 建库、建表与初始化配置
├── bootstrap/                  # 启动模块：主类、application.yml、打包产物
├── myblog-common/              # 公共模块：Result / PageResult / 全局异常 / JWT 工具与过滤器
├── myblog-user/                # 用户模块：登录、个人资料、密码
│   ├── myblog-user-api/        #   对外契约（UserApi、DTO、VO）
│   └── myblog-user-impl/       #   内部实现（Controller/Service/Mapper/Entity/Convert）
├── myblog-category/            # 分类模块
├── myblog-tag/                 # 标签模块
├── myblog-article/             # 文章模块：文章 CRUD、前台分页/搜索/详情、点赞、热门排行
├── myblog-project/             # 项目展示模块
├── myblog-comment/             # 评论模块：前台树形评论、后台审核管理
├── myblog-file/                # 文件模块：OSS 上传与文件管理
├── myblog-system/              # 系统模块：站点配置、关于我
└── myblog-frontend/            # 前端工程（Vue 3 + Vite）
    ├── src/api/                # 接口封装（axios）
    ├── src/router/             # 路由（前台 + 后台）
    ├── src/store/              # Pinia 状态（用户、主题、站点配置）
    ├── src/search/             # 全站搜索提供者注册表
    ├── src/components/front/   # 前台组件（导航、页脚、评论项等）
    ├── src/views/front/        # 前台页面
    ├── src/views/admin/        # 后台页面
    └── src/styles/             # 设计令牌与全局样式（theme.scss / index.scss）
```

每个业务模块的 `impl` 内部统一分层：`controller` → `service` / `service.impl` → `mapper` → `entity`，DTO / VO 转换统一走 `convert`（MapStruct）。

---

## 四、环境要求

| 依赖 | 版本要求 | 说明 |
| --- | --- | --- |
| JDK | 21 | 项目编译与运行均基于 Java 21 |
| Maven | 3.8+ | 多模块构建 |
| MySQL | 8.x | 库名默认 `myblog` |
| Redis | 6.x+ | 点赞去重与热门周榜依赖 |
| Node.js | 18+ | 前端构建（建议 18 / 20 LTS） |
| 阿里云 OSS | — | 需要一个 Bucket 与 AccessKey |

---

## 五、快速开始

### 1. 初始化数据库

```bash
mysql -u root -p < sql/init.sql
```

该脚本会创建 `myblog` 库、11 张业务表，并写入 `sys_config` 初始站点配置。

> 管理员账号**无需手动插入**：应用首次启动时若 `admin` 账号不存在，会自动创建，初始密码取自配置项 `ADMIN_INIT_PASSWORD`（默认 `admin123`）。

### 2. 配置环境变量

复制模板并在项目根目录创建 `.env`：

```bash
cp .env.example .env     # Windows: copy .env.example .env
```

至少填写以下必填项：

```properties
MYSQL_PASSWORD=你的数据库密码

# 阿里云 OSS
OSS_ACCESS_KEY_ID=你的AccessKeyId
OSS_ACCESS_KEY_SECRET=你的AccessKeySecret
OSS_ENDPOINT=oss-cn-beijing.aliyuncs.com
OSS_BUCKET_NAME=你的bucket
OSS_DOMAIN=                      # 可选，自定义 CDN 域名，留空则用 Bucket 默认域名

# JWT 签名密钥：长度必须 >= 32 字符
JWT_SECRET=请替换为任意32位以上随机字符串
```

> `.env` 已在 `.gitignore` 中，**严禁提交到代码仓库**。同名系统环境变量优先级高于 `.env`，生产环境建议直接用系统环境变量注入。

### 3. 启动后端

```bash
# 构建（首次或改动后）
mvn clean install -DskipTests

# 运行（工作目录必须是项目根目录，否则读不到 .env）
java -jar bootstrap/target/bootstrap-1.0.0.jar
```

或直接用 Maven 运行：

```bash
mvn -pl bootstrap -am spring-boot:run
```

后端启动在 **http://localhost:8080**，接口前缀为 `/api`。

### 4. 启动前端

```bash
cd myblog-frontend
npm install
npm run dev
```

前端开发服务器启动在 **http://localhost:5173**，已配置 `/api` 代理到 `http://localhost:8080`，无需额外处理跨域。

### 5. 访问

| 入口 | 地址 | 说明 |
| --- | --- | --- |
| 前台首页 | http://localhost:5173/ | 博客展示端 |
| 后台管理 | http://localhost:5173/admin/login | 默认账号 `admin`，密码见 `ADMIN_INIT_PASSWORD` |
| 接口文档 | http://localhost:8080/api/swagger-ui.html | Knife4j 增强文档亦可通过 `/api/doc.html` 访问 |

---

## 六、配置说明

### 环境变量（`.env`）

| 变量 | 必填 | 默认值 | 说明 |
| --- | --- | --- | --- |
| `MYSQL_HOST` / `MYSQL_PORT` | 否 | `localhost` / `3306` | MySQL 地址 |
| `MYSQL_DATABASE` | 否 | `myblog` | 数据库名 |
| `MYSQL_USERNAME` | 否 | `root` | 数据库用户 |
| `MYSQL_PASSWORD` | **是** | — | 数据库密码 |
| `REDIS_HOST` / `REDIS_PORT` | 否 | `localhost` / `6379` | Redis 地址 |
| `REDIS_PASSWORD` | 否 | 空 | Redis 密码 |
| `REDIS_DATABASE` | 否 | `0` | Redis 库序号 |
| `OSS_ACCESS_KEY_ID` | **是** | — | 阿里云 AccessKeyId |
| `OSS_ACCESS_KEY_SECRET` | **是** | — | 阿里云 AccessKeySecret |
| `OSS_ENDPOINT` | **是** | — | OSS 区域端点，如 `oss-cn-beijing.aliyuncs.com` |
| `OSS_BUCKET_NAME` | **是** | — | Bucket 名称 |
| `OSS_DOMAIN` | 否 | 空 | 自定义访问域名，留空用 Bucket 默认域名 |
| `JWT_SECRET` | **是** | — | JWT 签名密钥，**长度 ≥ 32 字符** |
| `JWT_EXPIRE_HOURS` | 否 | `24` | Token 有效期（小时） |
| `ADMIN_INIT_PASSWORD` | 否 | `admin123` | 首次启动自动创建管理员时的初始密码 |

### 其他关键配置（`bootstrap/src/main/resources/application.yml`）

| 配置项 | 值 | 说明 |
| --- | --- | --- |
| `server.port` | `8080` | 后端端口 |
| `server.servlet.context-path` | `/api` | 统一接口前缀 |
| `spring.profiles.active` | `dev` | 部署时改为 `prod` |
| `spring.servlet.multipart.max-file-size` | `10MB` | 单文件上限（与后端校验一致） |
| `pagehelper.reasonable` | `true` | 页码越界自动纠正 |
| `mybatis.configuration.map-underscore-to-camel-case` | `true` | 下划线转驼峰 |
| `aliyun.oss.dir-prefix` | `myblog/` | OSS 根目录前缀 |
| `logging.file.name` | `logs/myblog.log` | 日志文件（滚动：10MB × 7 天） |

---

## 七、接口约定

### 通用规则

- **前缀**：所有接口以 `/api` 开头（如 `/api/article/page`），前台接口无额外前缀，后台接口统一以 `/api/admin/` 开头。
- **统一响应体**：

```json
{ "code": 200, "message": "成功", "data": {} }
```

`code` 为 200 表示业务成功；其余为业务错误码（如 401 未登录、400 参数校验失败）。前端 axios 拦截器已统一处理：成功直接返回 `data`，失败弹出错误提示。

- **分页响应**：

```json
{ "total": 14, "records": [] }
```

- **鉴权**：`/api/admin/**` 需在请求头携带 `Authorization: Bearer <token>`；登录接口 `POST /api/auth/login` 返回 token。Token 过期时接口返回 401，前端会自动清除本地 token 并跳转登录页。

### 主要接口

**认证与用户**

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/auth/login` | 登录，返回 token 与用户信息 |
| GET | `/profile` | 前台：博主公开资料 |
| GET / PUT | `/admin/profile` | 后台：个人资料查询 / 更新 |
| PUT | `/admin/profile/password` | 修改密码 |

**文章**

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/article/page` | 前台：已发布文章分页（支持分类 / 标签筛选） |
| GET | `/article/search` | 前台：关键字搜索（标题 / 摘要） |
| GET | `/article/{id}` | 前台：文章详情 |
| GET | `/article/latest` | 前台：最新文章 |
| GET | `/article/hot` | 前台：热门文章（`range=total/week`） |
| POST | `/article/{id}/like` | 前台：点赞（Redis 去重） |
| GET | `/admin/article/page` | 后台：文章分页 |
| POST / PUT / DELETE | `/admin/article`、`/admin/article/{id}` | 后台：新增 / 更新 / 删除 |
| PUT | `/admin/article/{id}/publish`、`/unpublish` | 后台：发布 / 下架 |

**分类 / 标签 / 项目**

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/category/list`、`/tag/list` | 前台列表 |
| POST / PUT / DELETE | `/admin/category`、`/admin/tag` 等 | 后台维护 |
| GET | `/project/list`、`/project/{id}` | 前台项目列表 / 详情 |
| GET / POST / PUT / DELETE | `/admin/project/**` | 后台项目维护 |

**评论**

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/comment/list?articleId=` | 前台：文章已通过评论（树形） |
| POST | `/comment` | 前台：提交评论（默认待审核，记录 IP） |
| GET | `/admin/comment/page` | 后台：评论分页（`status`0 待审核 / 1 通过 / 2 拒绝，`keyword` 匹配昵称 / 内容 / 邮箱） |
| PUT | `/admin/comment/{id}/approve`、`/reject` | 后台：通过 / 拒绝 |
| PUT | `/admin/comment/batch/status` | 后台：批量改状态（body：`{ ids, status }`） |
| POST | `/admin/comment/{id}/reply` | 后台：以博主身份回复（自动通过） |
| DELETE | `/admin/comment/{id}`、`/admin/comment/batch` | 后台：删除 / 批量删除 |

**文件（OSS）**

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/admin/file/upload?bizType=` | 上传图片 |
| GET | `/admin/file/page` | 分页查询（`bizType`、`keyword`、分页参数） |
| PUT | `/admin/file/rename` | 重命名 / 移动业务目录（body：`{ objectKey, newName, targetBizType }`） |
| DELETE | `/admin/file?objectKey=` | 删除单个文件 |
| DELETE | `/admin/file/batch` | 批量删除（body：`["objectKey", ...]`） |

**系统配置 / 关于我**

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/config/all` | 前台：站点配置 |
| GET / PUT | `/admin/config/list`、`/admin/config` | 后台：站点信息 |
| GET | `/about` | 前台：关于我 |
| GET / PUT | `/admin/about` | 后台：关于我维护 |

---

## 八、数据库表

库名 `myblog`，字符集 `utf8mb4`，表名使用下划线命名法。

| 表名 | 说明 |
| --- | --- |
| `sys_user` | 用户表（单博主场景，含昵称、头像、签名、邮箱、GitHub 等社交链接） |
| `article` | 文章表（标题、摘要、Markdown 正文、封面、分类、浏览量、点赞数、状态） |
| `category` | 分类表 |
| `tag` | 标签表 |
| `article_tag` | 文章 - 标签关联表 |
| `comment` | 评论表（含 `parent_id` 回复关系、`user_id` 博主标识、`ip` 提交者 IP、`status` 审核状态） |
| `project` | 项目展示表 |
| `sys_config` | 系统配置表（站点名称、描述、ICP 备案号等） |
| `about_skill` | 关于我 - 技能 |
| `about_experience` | 关于我 - 经历 |
| `moment` | 最新动态表（表结构已预留，前端入口暂未开放） |

实体类使用驼峰命名法，MyBatis 已开启下划线转驼峰；所有查询必须显式列出字段，禁止 `SELECT *`。

> 主键为自增 `BIGINT`，**删除记录后自增计数不会回退**，因此 ID 会出现不连续的空洞，这是数据库的正常行为。后台各列表页已统一改为展示「序号」列（按分页连续，从 1 开始）而非主键。

---

## 九、文件存储规范

图片统一存放于阿里云 OSS，路径规范：

```
myblog/{userId}/{bizType}/yyyy/MM/{uuid}.{ext}
```

| `bizType` | 用途 |
| --- | --- |
| `avatar` | 头像 |
| `article` | 文章封面与正文插图 |
| `project` | 项目封面 |
| `moment` | 动态配图 |
| `other` | 其他 |

**限制与约束**

- 仅允许 `jpg / jpeg / png / gif / webp`，单文件不超过 **10MB**（前端预校验 + 后端二次校验）。
- 所有 OSS 写操作（删除、重命名、批量删除）都会校验对象 Key 必须以 `myblog/` 开头，防止越权操作 Bucket 内的其他对象。
- 「重命名 / 移动」基于 OSS 的 `copyObject` + `deleteObject` 实现（OSS 无原地重命名），且强制沿用原文件扩展名。
- 文件列表采用「按前缀列举 + 本地过滤分页」策略（个人博客量级足够），代码中留有安全上限。

> `.env` 与 `logs/` 已在 `.gitignore` 中排除，请勿提交任何密钥或日志。

---

## 十、构建与部署

### 后端

```bash
# 打包（跳过测试）
mvn clean package -DskipTests
# 产物：bootstrap/target/bootstrap-1.0.0.jar

# 生产运行：将 profile 切换为 prod，并把 .env 放在 jar 的同级工作目录
java -jar bootstrap/target/bootstrap-1.0.0.jar --spring.profiles.active=prod
```

生产环境建议：
- 用系统环境变量（或进程管理器注入）提供密钥，而不是依赖 `.env` 文件；
- 用 `nohup` / systemd / Windows 服务等方式常驻，并把 `logs/` 指向可写目录。

### 前端

```bash
cd myblog-frontend
npm run build          # 产物在 dist/
npm run preview        # 本地预览构建结果
```

部署到 Nginx 时，将 `dist/` 作为静态根目录，并把 `/api` 反向代理到后端：

```nginx
server {
    listen 80;
    root /var/www/myblog/dist;
    index index.html;

    # 前端为 history 路由，需回退到 index.html
    location / {
        try_files $uri $uri/ /index.html;
    }

    location /api/ {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }
}
```

> 评论模块会优先从 `X-Forwarded-For` 获取客户端真实 IP，经 Nginx 反代时请按上面配置透传该请求头。

---

## 十一、常见问题

**1. 后端起不来，报缺少 `JWT_SECRET` / `OSS_*` / `MYSQL_PASSWORD`**
必填环境变量没有配置。复制 `.env.example` 为 `.env` 并补齐，注意 `.env` 必须位于**启动时的工作目录**（默认项目根目录）。

**2. 前端访问后台接口返回 401**
Token 缺失或已过期（默认 24 小时）。重新登录即可，前端会自动清理失效 token 并跳转登录页。

**3. 点赞或热门周榜接口报错**
Redis 未启动。点赞去重使用 Redis Set（TTL 24h），周榜使用 ZSet（TTL 8 天），请确保 Redis 可用。

**4. 后台列表里的 ID 不是从 1 开始**
自增主键只保证递增不保证连续，删除记录后会留下空洞。后台列表已改用「序号」列展示，按分页连续编号；如需排查具体记录请改用其他业务字段。

**5. 图片上传失败**
检查 `OSS_*` 配置是否正确、Bucket 是否可写、文件是否满足格式与 10MB 限制。

**6. 推送代码前请务必确认**
`.env`、`logs/`、`node_modules/`、`target/`、`dist/` 均已在 `.gitignore` 中排除。可用 `git check-ignore -v .env` 验证。