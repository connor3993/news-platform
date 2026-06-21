DROP DATABASE IF EXISTS news_db;
CREATE DATABASE news_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE news_db;

DROP TABLE IF EXISTS sys_operation_log;
DROP TABLE IF EXISTS news_daily_stats;
DROP TABLE IF EXISTS news_read_record;
DROP TABLE IF EXISTS news_audit_record;
DROP TABLE IF EXISTS news_article_comment;
DROP TABLE IF EXISTS news_article_favorite;
DROP TABLE IF EXISTS news_article_vote;
DROP TABLE IF EXISTS news_article_content;
DROP TABLE IF EXISTS news_article;
DROP TABLE IF EXISTS news_category;
DROP TABLE IF EXISTS app_user;
DROP TABLE IF EXISTS admin_user;

CREATE TABLE admin_user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(64) NOT NULL UNIQUE,
  password VARCHAR(128) NOT NULL,
  name VARCHAR(64) NOT NULL,
  phone VARCHAR(20),
  role VARCHAR(32) NOT NULL DEFAULT 'editor',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '1启用 0禁用',
  create_time DATETIME,
  update_time DATETIME,
  create_user BIGINT,
  update_user BIGINT
);

CREATE TABLE app_user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(64) NOT NULL UNIQUE,
  password VARCHAR(128) NOT NULL,
  nickname VARCHAR(64),
  avatar VARCHAR(255),
  status TINYINT NOT NULL DEFAULT 1,
  create_time DATETIME,
  update_time DATETIME
);

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

CREATE TABLE news_article (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(128) NOT NULL,
  summary VARCHAR(255),
  cover_url VARCHAR(255),
  category_id BIGINT NOT NULL,
  author_id BIGINT NOT NULL,
  author_type VARCHAR(16) NOT NULL DEFAULT 'admin' COMMENT 'admin/user',
  status TINYINT NOT NULL DEFAULT 0 COMMENT '0草稿 1待审核 2审核通过 3审核驳回 4已发布 5已下架',
  publish_time DATETIME,
  view_count BIGINT NOT NULL DEFAULT 0,
  like_count BIGINT NOT NULL DEFAULT 0,
  dislike_count BIGINT NOT NULL DEFAULT 0,
  favorite_count BIGINT NOT NULL DEFAULT 0,
  comment_count BIGINT NOT NULL DEFAULT 0,
  hot_score DECIMAL(12,2) NOT NULL DEFAULT 0,
  reject_reason VARCHAR(255),
  create_time DATETIME,
  update_time DATETIME,
  create_user BIGINT,
  update_user BIGINT,
  KEY idx_article_status_publish (status, publish_time),
  KEY idx_article_category (category_id),
  KEY idx_article_author (author_type, author_id),
  KEY idx_article_hot (hot_score)
);

CREATE TABLE news_article_vote (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  article_id BIGINT NOT NULL,
  vote_type TINYINT NOT NULL COMMENT '1点赞 -1点踩',
  create_time DATETIME,
  update_time DATETIME,
  UNIQUE KEY uk_article_vote_user_article (user_id, article_id),
  KEY idx_article_vote_article (article_id)
);

CREATE TABLE news_article_favorite (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  article_id BIGINT NOT NULL,
  create_time DATETIME,
  UNIQUE KEY uk_article_favorite_user_article (user_id, article_id),
  KEY idx_article_favorite_user (user_id),
  KEY idx_article_favorite_article (article_id)
);

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

CREATE TABLE news_article_content (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  article_id BIGINT NOT NULL,
  content LONGTEXT NOT NULL,
  UNIQUE KEY uk_article_content_article (article_id)
);

CREATE TABLE news_audit_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  article_id BIGINT NOT NULL,
  auditor_id BIGINT NOT NULL,
  audit_status TINYINT NOT NULL COMMENT '2通过 3驳回',
  audit_comment VARCHAR(255),
  create_time DATETIME,
  KEY idx_audit_create_time (create_time)
);

CREATE TABLE news_read_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  article_id BIGINT NOT NULL,
  create_time DATETIME,
  KEY idx_read_create_time (create_time),
  KEY idx_read_article (article_id)
);

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

INSERT INTO admin_user
(id, username, password, name, phone, role, status, create_time, update_time, create_user, update_user)
VALUES
(1, 'admin', '123456', '管理员', '13800000000', 'admin', 1, NOW(), NOW(), 1, 1),
(2, 'editor', '123456', '内容编辑', '13800000001', 'editor', 1, NOW(), NOW(), 1, 1);

INSERT INTO app_user
(id, username, password, nickname, avatar, status, create_time, update_time)
VALUES
(1, 'zhangsan', '123456', '张三', 'https://example.com/avatar.png', 1, NOW(), NOW());

INSERT INTO news_category
(id, name, sort, status, create_time, update_time, create_user, update_user)
VALUES
(1, '科技', 1, 1, NOW(), NOW(), 1, 1),
(2, '财经', 2, 1, NOW(), NOW(), 1, 1),
(3, '传媒', 3, 1, NOW(), NOW(), 1, 1),
(4, '国际', 4, 1, NOW(), NOW(), 1, 1);

INSERT INTO news_article
(id, title, summary, cover_url, category_id, author_id, author_type, status, publish_time, view_count, like_count, dislike_count, favorite_count, comment_count, hot_score, create_time, update_time, create_user, update_user)
VALUES
(1, 'AI 技术带来媒体生产新变化', '围绕智能采编、内容分发和审核效率的行业观察。', 'https://example.com/cover-ai.jpg', 1, 1, 'admin', 4, NOW(), 1280, 18, 1, 6, 2, 1480.50, NOW(), NOW(), 1, 1),
(2, '本地媒体探索数据新闻新路径', '数据可视化正在成为公共议题报道的重要表达方式。', 'https://example.com/cover-data.jpg', 3, 1, 'admin', 4, NOW(), 860, 9, 0, 3, 1, 980.00, NOW(), NOW(), 1, 1),
(3, '跨境资讯平台加速内容协作', '多语言生产与实时审核成为媒体平台升级重点。', 'https://example.com/cover-global.jpg', 4, 1, 'admin', 1, NULL, 0, 0, 0, 0, 0, 0, NOW(), NOW(), 1, 1),
(4, '用户投稿：社区媒体如何连接本地生活', '来自普通用户的本地资讯观察，等待管理员审核。', '', 3, 1, 'user', 1, NULL, 0, 0, 0, 0, 0, 0, NOW(), NOW(), 1, 1);

INSERT INTO news_article_content
(article_id, content)
VALUES
(1, '<p>AI 正在深入媒体生产全链路，从选题辅助、素材聚合到内容审核，平台化工具让编辑协作更高效。</p>'),
(2, '<p>数据新闻强调事实、结构与可视化表达。本地媒体通过数据看板与专题报道提升公共服务能力。</p>'),
(3, '<p>跨境资讯协作需要更完善的权限、审核与实时通知机制，保障内容分发效率和合规性。</p>'),
(4, '<p>社区媒体的价值在于发现身边的公共议题。用户提交后，平台管理员会进行内容审核、修改和发布。</p>');

INSERT INTO news_article_favorite
(user_id, article_id, create_time)
VALUES
(1, 1, NOW()),
(1, 2, NOW());

INSERT INTO news_article_comment
(article_id, user_id, content, status, create_time)
VALUES
(1, 1, '这篇文章很适合讲智能采编场景。', 1, NOW()),
(1, 1, '热度计算可以结合阅读、点赞和收藏继续优化。', 1, NOW()),
(2, 1, '数据新闻这个方向很有现实价值。', 1, NOW());

INSERT INTO news_daily_stats
(stat_date, article_count, publish_count, view_count, audit_count, reject_count, create_time)
VALUES
(CURRENT_DATE - INTERVAL 2 DAY, 2, 1, 520, 3, 1, NOW()),
(CURRENT_DATE - INTERVAL 1 DAY, 3, 2, 980, 4, 0, NOW());
