-- Set client charset before anything else (prevents garbled Chinese on Windows GBK terminals)
SET NAMES utf8mb4;

-- Create database
CREATE DATABASE IF NOT EXISTS user_login DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE user_login;

-- User table
CREATE TABLE IF NOT EXISTS sys_user (
    id          BIGINT       NOT NULL COMMENT 'Primary key (snowflake)',
    user_name   VARCHAR(50)  NOT NULL COMMENT 'Username',
    pass_word   VARCHAR(255) NOT NULL COMMENT 'Password (BCrypt)',
    email       VARCHAR(100) NOT NULL COMMENT 'Email',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT 'Status: 0=disabled, 1=active',
    nickname    VARCHAR(50)  DEFAULT NULL COMMENT 'Nickname',
    avatar      VARCHAR(255) DEFAULT NULL COMMENT 'Avatar URL',
    role        VARCHAR(10)  NOT NULL DEFAULT 'user' COMMENT 'Role: user/admin',
    is_deleted  TINYINT      NOT NULL DEFAULT 0 COMMENT 'Soft delete: 0=normal, 1=deleted',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created at',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Updated at',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_name (user_name),
    UNIQUE KEY uk_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='System user table';

-- Preset accounts (passwords are BCrypt encrypted)
-- admin/admin123, user1/user123, user2/user123
INSERT IGNORE INTO sys_user (id, user_name, pass_word, email, status, role, is_deleted) VALUES
(1, 'admin', '$2a$10$O82cWcD68s1jMdtOnPt0LOUmqJln8k5htTBqcFTr25dqYxjQPmwAS', 'admin@example.com', 1, 'admin', 0),
(2, 'user1', '$2a$10$GqiKF37mHXAZEO3AhJXNSOxGQhhsb.E.COR6IJlX7njK4Ot1.mPCu', 'user1@example.com', 1, 'user', 0),
(3, 'user2', '$2a$10$GqiKF37mHXAZEO3AhJXNSOxGQhhsb.E.COR6IJlX7njK4Ot1.mPCu', 'user2@example.com', 1, 'user', 0);

-- Category table
CREATE TABLE IF NOT EXISTS category (
    id          BIGINT       NOT NULL COMMENT 'Primary key (snowflake)',
    name        VARCHAR(50)  NOT NULL COMMENT 'Category name',
    sort        INT          NOT NULL DEFAULT 0 COMMENT 'Sort order',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT 'Status: 0=disabled, 1=active',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted  TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Article category';

-- Tag table
CREATE TABLE IF NOT EXISTS tag (
    id          BIGINT       NOT NULL COMMENT 'Primary key (snowflake)',
    name        VARCHAR(30)  NOT NULL COMMENT 'Tag name',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Article tag';

-- Article table
CREATE TABLE IF NOT EXISTS article (
    id          BIGINT       NOT NULL COMMENT 'Primary key (snowflake)',
    title       VARCHAR(200) NOT NULL COMMENT 'Title',
    content     LONGTEXT     NOT NULL COMMENT 'Markdown content',
    summary     VARCHAR(500) DEFAULT NULL COMMENT 'Summary',
    cover_url   VARCHAR(255) DEFAULT NULL COMMENT 'Cover image URL',
    status      TINYINT      NOT NULL DEFAULT 0 COMMENT '0=draft, 1=published',
    view_count  INT          NOT NULL DEFAULT 0 COMMENT 'View count',
    user_id     BIGINT       NOT NULL COMMENT 'Author ID',
    category_id BIGINT       DEFAULT NULL COMMENT 'Category ID',
    is_top      TINYINT      NOT NULL DEFAULT 0 COMMENT 'Pinned: 0=no, 1=yes',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted  TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_user_id (user_id),
    KEY idx_category_id (category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Article';

-- Article-Tag relation table
CREATE TABLE IF NOT EXISTS article_tag (
    article_id BIGINT NOT NULL,
    tag_id     BIGINT NOT NULL,
    PRIMARY KEY (article_id, tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Article-Tag relation';

-- Comment table
CREATE TABLE IF NOT EXISTS comment (
    id          BIGINT        NOT NULL COMMENT 'Primary key (snowflake)',
    content     VARCHAR(1000) NOT NULL COMMENT 'Comment content',
    article_id  BIGINT        NOT NULL COMMENT 'Article ID',
    user_id     BIGINT        NOT NULL COMMENT 'Commenter ID',
    parent_id   BIGINT        DEFAULT NULL COMMENT 'Parent comment ID (for replies)',
    create_time DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted  TINYINT       NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_article_id (article_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Comment';

-- Article like table
CREATE TABLE IF NOT EXISTS article_like (
    id          BIGINT   NOT NULL COMMENT 'Primary key (snowflake)',
    article_id  BIGINT   NOT NULL,
    user_id     BIGINT   NOT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_article_user (article_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Article like';

-- Preset categories
INSERT IGNORE INTO category (id, name, sort) VALUES
(1, '技术分享', 1),
(2, '学习笔记', 2),
(3, '项目实战', 3),
(4, '生活随笔', 4);

-- Preset tags
INSERT IGNORE INTO tag (id, name) VALUES
(1, 'Java'), (2, 'Spring Boot'), (3, 'Vue'), (4, 'MySQL'),
(5, 'JavaScript'), (6, '前端'), (7, '后端'), (8, '数据库');
