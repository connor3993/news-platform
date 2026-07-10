-- ============================================================
--  news_platform 数据库初始化（结构精简版：8 张核心表）
--  合并：admin_user + app_user -> sys_user
--        news_article + news_article_content -> news_article(content LONGTEXT)
--        news_article_vote + news_article_favorite -> news_user_interaction(action_type)
--  保留：news_category / news_article_comment(历史遗留，代码未引用) / news_read_record / news_daily_stats / sys_operation_log
--  说明：news_audit_record 已裁掉，审核结果由 news_article.status + reject_reason 承载
-- ============================================================

DROP DATABASE IF EXISTS news_db;
CREATE DATABASE news_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE news_db;

-- 系统用户（合并原 admin_user / app_user，user_type 区分 admin / user）
CREATE TABLE sys_user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(64) NOT NULL,
  password VARCHAR(128) NOT NULL,
  name VARCHAR(64),
  nickname VARCHAR(64),
  avatar VARCHAR(255),
  phone VARCHAR(20),
  user_type VARCHAR(16) NOT NULL DEFAULT 'user' COMMENT 'admin/user',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '1启用 0禁用',
  create_time DATETIME,
  update_time DATETIME,
  create_user BIGINT,
  update_user BIGINT,
  UNIQUE KEY uk_sys_user_username (username)
);

-- 分类
CREATE TABLE news_category (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(64) NOT NULL,
  sort INT NOT NULL DEFAULT 0,
  status TINYINT NOT NULL DEFAULT 1 COMMENT '1启用 0禁用',
  create_time DATETIME,
  update_time DATETIME,
  create_user BIGINT,
  update_user BIGINT,
  UNIQUE KEY uk_news_category_name (name)
);

-- 文章（正文并入本表）
CREATE TABLE news_article (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(128) NOT NULL,
  summary VARCHAR(255),
  cover_url VARCHAR(255),
  category_id BIGINT NOT NULL,
  author_id BIGINT NOT NULL,
  status TINYINT NOT NULL DEFAULT 0 COMMENT '0草稿 1待审核 2审核通过 3审核驳回 4已发布 5已下架',
  publish_time DATETIME,
  content LONGTEXT,
  view_count BIGINT NOT NULL DEFAULT 0,
  like_count BIGINT NOT NULL DEFAULT 0,
  dislike_count BIGINT NOT NULL DEFAULT 0,
  favorite_count BIGINT NOT NULL DEFAULT 0,
  reject_reason VARCHAR(255),
  create_time DATETIME,
  update_time DATETIME,
  create_user BIGINT,
  update_user BIGINT,
  KEY idx_article_status_publish (status, publish_time),
  KEY idx_article_category (category_id),
  KEY idx_article_author (author_id)
);

-- 用户互动（合并原 vote / favorite）
CREATE TABLE news_user_interaction (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  article_id BIGINT NOT NULL,
  action_type VARCHAR(16) NOT NULL COMMENT 'like/dislike/favorite',
  create_time DATETIME,
  update_time DATETIME,
  UNIQUE KEY uk_interaction_user_article_action (user_id, article_id, action_type),
  KEY idx_interaction_article (article_id),
  KEY idx_interaction_user (user_id)
);

-- 阅读记录（同一用户同一天同一篇只计一次）
CREATE TABLE news_read_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  article_id BIGINT NOT NULL,
  create_time DATETIME,
  read_date DATE GENERATED ALWAYS AS (DATE(create_time)) STORED,
  UNIQUE KEY uk_read_user_article_date (user_id, article_id, read_date),
  KEY idx_read_create_time (create_time),
  KEY idx_read_article (article_id)
);

-- 评论（历史遗留表，当前版本代码未引用，保留以兼容数据）
CREATE TABLE news_article_comment (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  article_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  content VARCHAR(500) NOT NULL,
  status TINYINT NOT NULL DEFAULT 1 COMMENT '1正常 0隐藏',
  create_time DATETIME,
  KEY idx_article_comment_article (article_id, create_time),
  KEY idx_article_comment_user (user_id)
);

-- 每日统计
CREATE TABLE news_daily_stats (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  stat_date DATE NOT NULL,
  article_count INT NOT NULL DEFAULT 0,
  publish_count INT NOT NULL DEFAULT 0,
  view_count BIGINT NOT NULL DEFAULT 0,
  audit_count INT NOT NULL DEFAULT 0,
  reject_count INT NOT NULL DEFAULT 0,
  create_time DATETIME,
  UNIQUE KEY uk_daily_stats_date (stat_date)
);

-- 操作日志
CREATE TABLE sys_operation_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  operator_id BIGINT,
  operator_type VARCHAR(16) COMMENT 'admin/user',
  operation VARCHAR(128),
  request_uri VARCHAR(255),
  request_method VARCHAR(16),
  request_params TEXT,
  ip VARCHAR(64),
  create_time DATETIME,
  KEY idx_log_create_time (create_time),
  KEY idx_log_operation (operation)
);

-- ===================== 种子数据 =====================

INSERT INTO sys_user
(id, username, password, name, nickname, avatar, phone, user_type, status, create_time, update_time, create_user, update_user)
VALUES
(1, 'admin', '$2a$10$2QIWdtDgCc4eNDh62tdpi.Lj.04BibsiW15ltXxk7v2UZZnINZfge', '管理员', NULL, NULL, '13800000000', 'admin', 1, NOW(), NOW(), 1, 1),
(2, 'zhangsan', '$2a$10$2QIWdtDgCc4eNDh62tdpi.Lj.04BibsiW15ltXxk7v2UZZnINZfge', NULL, '张三', 'https://example.com/avatar.png', NULL, 'user', 1, NOW(), NOW(), NULL, NULL);

INSERT INTO news_category
(id, name, sort, status, create_time, update_time, create_user, update_user)
VALUES
(1, '科技', 1, 1, NOW(), NOW(), 1, 1),
(2, '财经', 2, 1, NOW(), NOW(), 1, 1),
(3, '传媒', 3, 1, NOW(), NOW(), 1, 1),
(4, '国际', 4, 1, NOW(), NOW(), 1, 1);

INSERT INTO news_article
(id, title, summary, cover_url, category_id, author_id, status, publish_time, content, view_count, like_count, dislike_count, favorite_count, create_time, update_time, create_user, update_user)
VALUES
(1, 'AI 技术带来媒体生产新变化', '围绕智能采编、内容分发和审核效率的行业观察。', 'https://example.com/cover-ai.jpg', 1, 1, 4, NOW(), '<p>AI 正在深入媒体生产全链路，从选题辅助、素材聚合到内容审核，平台化工具让编辑协作更高效。</p>', 1280, 18, 1, 6, NOW(), NOW(), 1, 1),
(2, '本地媒体探索数据新闻新路径', '数据可视化正在成为公共议题报道的重要表达方式。', 'https://example.com/cover-data.jpg', 3, 1, 4, NOW(), '<p>数据新闻强调事实、结构与可视化表达。本地媒体通过数据看板与专题报道提升公共服务能力。</p>', 860, 9, 0, 3, NOW(), NOW(), 1, 1),
(3, '跨境资讯平台加速内容协作', '多语言生产与实时审核成为媒体平台升级重点。', 'https://example.com/cover-global.jpg', 4, 1, 1, NULL, '<p>跨境资讯协作需要更完善的权限、审核与实时通知机制，保障内容分发效率和合规性。</p>', 0, 0, 0, 0, NOW(), NOW(), 1, 1),
(4, '用户投稿：社区媒体如何连接本地生活', '来自普通用户的本地资讯观察，等待管理员审核。', '', 3, 2, 1, NULL, '<p>社区媒体的价值在于发现身边的公共议题。用户提交后，平台管理员会进行内容审核、修改和发布。</p>', 0, 0, 0, 0, NOW(), NOW(), 2, 2);

INSERT INTO news_user_interaction
(user_id, article_id, action_type, create_time, update_time)
VALUES
(2, 1, 'favorite', NOW(), NOW()),
(2, 2, 'favorite', NOW(), NOW()),
(2, 1, 'like', NOW(), NOW());

INSERT INTO news_read_record
(user_id, article_id, create_time)
VALUES
(2, 1, NOW()),
(2, 3, NOW()),
(0, 1, NOW());

INSERT INTO news_article_comment
(article_id, user_id, content, status, create_time)
VALUES
(1, 2, '这篇文章很适合讲智能采编场景。', 1, NOW()),
(1, 2, '热度计算可以结合阅读、点赞和收藏继续优化。', 1, NOW()),
(2, 2, '数据新闻这个方向很有现实价值。', 1, NOW());

INSERT INTO news_daily_stats
(stat_date, article_count, publish_count, view_count, audit_count, reject_count, create_time)
VALUES
(CURRENT_DATE - INTERVAL 2 DAY, 2, 1, 520, 3, 1, NOW()),
(CURRENT_DATE - INTERVAL 1 DAY, 3, 2, 980, 4, 0, NOW());

INSERT INTO sys_operation_log
(operator_id, operator_type, operation, request_uri, request_method, create_time)
VALUES
(1, 'admin', '管理员登录', '/admin/auth/login', 'POST', NOW()),
(2, 'user', '用户登录', '/user/auth/login', 'POST', NOW());
