-- =============================================================
-- 个人博客系统 数据库初始化脚本
-- 执行方式一（客户端）：用 Navicat / DBeaver 等工具直接执行本文件
-- 执行方式二（命令行）：mysql -uroot -p < sql/init.sql
-- 说明：脚本可重复执行（CREATE IF NOT EXISTS / INSERT IGNORE）
-- =============================================================

CREATE DATABASE IF NOT EXISTS `myblog` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `myblog`;

-- -------------------------------------------------------------
-- 用户表
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `sys_user` (
    `id`          BIGINT PRIMARY KEY AUTO_INCREMENT,
    `username`    VARCHAR(50)  NOT NULL UNIQUE COMMENT '用户名',
    `password`    VARCHAR(100) NOT NULL COMMENT '密码(BCrypt加密)',
    `nickname`    VARCHAR(50)  COMMENT '昵称',
    `avatar`      VARCHAR(255) COMMENT '头像URL',
    `email`       VARCHAR(100) COMMENT '邮箱',
    `signature`   VARCHAR(200) COMMENT '个人签名',
    `github`      VARCHAR(255) COMMENT 'GitHub 地址',
    `gitee`       VARCHAR(255) COMMENT 'Gitee 地址',
    `juejin`      VARCHAR(255) COMMENT '掘金地址',
    `csdn`        VARCHAR(255) COMMENT 'CSDN 地址',
    `wechat_qr`   VARCHAR(255) COMMENT '微信二维码图片URL',
    `status`      TINYINT      DEFAULT 1 COMMENT '状态:0禁用,1启用',
    `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '用户表';

-- -------------------------------------------------------------
-- 文章表
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `article` (
    `id`           BIGINT PRIMARY KEY AUTO_INCREMENT,
    `title`        VARCHAR(200) NOT NULL COMMENT '标题',
    `content`      LONGTEXT     NOT NULL COMMENT '内容(Markdown)',
    `summary`      VARCHAR(500) COMMENT '摘要',
    `cover_image`  VARCHAR(255) COMMENT '封面图',
    `category_id`  BIGINT       COMMENT '分类ID',
    `view_count`   INT          DEFAULT 0 COMMENT '浏览量',
    `like_count`   INT          DEFAULT 0 COMMENT '点赞数',
    `word_count`   INT          NOT NULL DEFAULT 0 COMMENT '字数（不含空白，近似）',
    `status`       TINYINT      DEFAULT 0 COMMENT '状态:0草稿,1已发布',
    `create_time`  DATETIME     DEFAULT CURRENT_TIMESTAMP,
    `update_time`  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY `idx_status_create_time` (`status`, `create_time`)
) COMMENT '文章表';

-- -------------------------------------------------------------
-- 分类表
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `category` (
    `id`          BIGINT PRIMARY KEY AUTO_INCREMENT,
    `name`        VARCHAR(50) NOT NULL COMMENT '分类名',
    `sort`        INT         DEFAULT 0 COMMENT '排序(越小越靠前)',
    `create_time` DATETIME    DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '分类表';

-- -------------------------------------------------------------
-- 标签表
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `tag` (
    `id`          BIGINT PRIMARY KEY AUTO_INCREMENT,
    `name`        VARCHAR(50) NOT NULL COMMENT '标签名',
    `create_time` DATETIME    DEFAULT CURRENT_TIMESTAMP
) COMMENT '标签表';

-- -------------------------------------------------------------
-- 文章-标签关联表
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `article_tag` (
    `id`         BIGINT PRIMARY KEY AUTO_INCREMENT,
    `article_id` BIGINT NOT NULL COMMENT '文章ID',
    `tag_id`     BIGINT NOT NULL COMMENT '标签ID',
    UNIQUE KEY `uk_article_tag` (`article_id`, `tag_id`)
) COMMENT '文章-标签关联表';

-- -------------------------------------------------------------
-- 评论表
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `comment` (
    `id`          BIGINT PRIMARY KEY AUTO_INCREMENT,
    `article_id`  BIGINT       NOT NULL COMMENT '文章ID',
    `nickname`    VARCHAR(50)  COMMENT '昵称(匿名)',
    `email`       VARCHAR(100) COMMENT '邮箱',
    `content`     VARCHAR(500) NOT NULL COMMENT '内容',
    `parent_id`   BIGINT       COMMENT '父评论ID',
    `user_id`     BIGINT       COMMENT '博主回复时的用户ID(访客为NULL)',
    `status`      TINYINT      DEFAULT 1 COMMENT '状态:0待审核,1通过,2拒绝',
    `ip`          VARCHAR(45)  COMMENT '提交者IP',
    `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP,
    KEY `idx_article_id` (`article_id`)
) COMMENT '评论表';

-- -------------------------------------------------------------
-- 系统配置表
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `sys_config` (
    `id`           BIGINT PRIMARY KEY AUTO_INCREMENT,
    `config_key`   VARCHAR(100) NOT NULL UNIQUE COMMENT '配置键',
    `config_value` VARCHAR(500) COMMENT '配置值',
    `remark`       VARCHAR(255) COMMENT '备注'
) COMMENT '系统配置表';

-- -------------------------------------------------------------
-- 项目展示表（拓展功能）
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `project` (
    `id`          BIGINT PRIMARY KEY AUTO_INCREMENT,
    `name`        VARCHAR(100) NOT NULL COMMENT '项目名称',
    `description` VARCHAR(500) COMMENT '项目简介',
    `content`     LONGTEXT     COMMENT '详细介绍(Markdown)',
    `cover_image` VARCHAR(255) COMMENT '封面图',
    `tech_stack`  VARCHAR(200) COMMENT '技术栈(逗号分隔，如 Vue3,Spring Boot,MySQL)',
    `github_url`  VARCHAR(255) COMMENT '源码地址',
    `demo_url`    VARCHAR(255) COMMENT '演示地址',
    `sort`        INT          DEFAULT 0 COMMENT '排序(越小越靠前)',
    `progress`    TINYINT      DEFAULT 1 COMMENT '进度:0规划中,1进行中,2已完成,3已暂停',
    `status`      TINYINT      DEFAULT 1 COMMENT '状态:0下架,1上架',
    `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY `idx_status_sort` (`status`, `sort`)
) COMMENT '项目展示表';

-- -------------------------------------------------------------
-- 最新动态表（拓展功能，可选方案 B）
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `moment` (
    `id`          BIGINT PRIMARY KEY AUTO_INCREMENT,
    `content`     VARCHAR(1000) NOT NULL COMMENT '动态内容',
    `images`      VARCHAR(2000) COMMENT '图片URL(逗号分隔,最多9张)',
    `status`      TINYINT       DEFAULT 1 COMMENT '状态:0隐藏,1显示',
    `create_time` DATETIME      DEFAULT CURRENT_TIMESTAMP,
    KEY `idx_create_time` (`create_time`)
) COMMENT '最新动态表';

-- -------------------------------------------------------------
-- 系统配置初始数据
-- -------------------------------------------------------------
INSERT IGNORE INTO `sys_config` (`config_key`, `config_value`, `remark`) VALUES
('site_name',        '我的博客',       '站点名称'),
('site_description', '记录技术与生活', '站点描述'),
('site_avatar',      '',               '博主头像URL'),
('icp',              '',               'ICP备案号'),
('github_url',       '',               'GitHub 地址'),
('email',            '',               '联系邮箱'),
('about_content',    '',               '关于我-自我介绍(Markdown)'),
('about_resume_url', '',               '关于我-简历文件地址');

-- -------------------------------------------------------------
-- 关于我-技能栈（拓展功能）
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `about_skill` (
    `id`          BIGINT PRIMARY KEY AUTO_INCREMENT,
    `name`        VARCHAR(50) NOT NULL COMMENT '技能名称',
    `level_label` VARCHAR(20) COMMENT '分类标签(如 后端/前端/运维)',
    `percent`     INT         NOT NULL DEFAULT 0 COMMENT '掌握程度(%)',
    `sort`        INT         DEFAULT 0 COMMENT '排序(越小越靠前)',
    `create_time` DATETIME    DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '关于我-技能栈';

-- -------------------------------------------------------------
-- 关于我-经历（拓展功能）
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `about_experience` (
    `id`           BIGINT PRIMARY KEY AUTO_INCREMENT,
    `title`        VARCHAR(100) NOT NULL COMMENT '经历标题',
    `organization` VARCHAR(100) COMMENT '组织/机构',
    `start_date`   VARCHAR(20)  COMMENT '开始时间',
    `end_date`     VARCHAR(20)  COMMENT '结束时间(空表示至今)',
    `description`  VARCHAR(500) COMMENT '描述',
    `sort`         INT          DEFAULT 0 COMMENT '排序(越小越靠前)',
    `create_time`  DATETIME     DEFAULT CURRENT_TIMESTAMP,
    `update_time`  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '关于我-经历';