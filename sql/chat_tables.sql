-- ----------------------------
-- 聊天模块数据库表结构
-- ----------------------------

-- ----------------------------
-- 1、聊天会话表
-- ----------------------------
DROP TABLE IF EXISTS `chat_session`;
CREATE TABLE `chat_session` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `session_id` varchar(64) NOT NULL COMMENT '会话ID',
  `session_type` tinyint(1) NOT NULL DEFAULT '1' COMMENT '会话类型（1私聊 2群聊）',
  `session_name` varchar(100) DEFAULT NULL COMMENT '会话名称',
  `session_avatar` varchar(255) DEFAULT NULL COMMENT '会话头像',
  `creator_id` bigint(20) NOT NULL COMMENT '创建者ID',
  `max_members` int(11) DEFAULT '500' COMMENT '最大成员数',
  `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '状态（0禁用 1正常）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_session_id` (`session_id`),
  KEY `idx_creator_id` (`creator_id`),
  KEY `idx_session_type` (`session_type`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天会话表';

-- ----------------------------
-- 2、聊天会话成员表
-- ----------------------------
DROP TABLE IF EXISTS `chat_session_member`;
CREATE TABLE `chat_session_member` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `session_id` varchar(64) NOT NULL COMMENT '会话ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `user_nickname` varchar(30) NOT NULL COMMENT '用户昵称',
  `user_avatar` varchar(255) DEFAULT NULL COMMENT '用户头像',
  `role` tinyint(1) NOT NULL DEFAULT '1' COMMENT '角色（1普通成员 2管理员 3群主）',
  `join_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
  `last_read_time` datetime DEFAULT NULL COMMENT '最后阅读时间',
  `is_muted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否禁言（0否 1是）',
  `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '状态（0已退出 1正常）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_session_user` (`session_id`, `user_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_session_id` (`session_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天会话成员表';

-- ----------------------------
-- 3、聊天消息表
-- ----------------------------
DROP TABLE IF EXISTS `chat_message`;
CREATE TABLE `chat_message` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `message_id` varchar(64) NOT NULL COMMENT '消息ID',
  `session_id` varchar(64) NOT NULL COMMENT '会话ID',
  `from_user_id` bigint(20) NOT NULL COMMENT '发送者ID',
  `from_user_nickname` varchar(30) NOT NULL COMMENT '发送者昵称',
  `from_user_avatar` varchar(255) DEFAULT NULL COMMENT '发送者头像',
  `message_type` tinyint(1) NOT NULL DEFAULT '1' COMMENT '消息类型（1文本 2图片 3语音 4视频 5文件 6表情）',
  `content` longtext NOT NULL COMMENT '消息内容',
  `extra_data` longtext DEFAULT NULL COMMENT '扩展数据（JSON格式）',
  `reply_to_message_id` varchar(64) DEFAULT NULL COMMENT '回复的消息ID',
  `status` tinyint(1) NOT NULL DEFAULT '0' COMMENT '消息状态（0正常 1已撤回）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_message_id` (`message_id`),
  KEY `idx_session_id` (`session_id`),
  KEY `idx_from_user_id` (`from_user_id`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天消息表';

-- ----------------------------
-- 4、消息阅读状态表
-- ----------------------------
DROP TABLE IF EXISTS `chat_message_read`;
CREATE TABLE `chat_message_read` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `message_id` varchar(64) NOT NULL COMMENT '消息ID',
  `session_id` varchar(64) NOT NULL COMMENT '会话ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `read_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '阅读时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_message_user` (`message_id`, `user_id`),
  KEY `idx_session_id` (`session_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_read_time` (`read_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息阅读状态表';

-- ----------------------------
-- 初始化数据
-- ----------------------------

-- 创建系统通知会话
INSERT INTO `chat_session` (`session_id`, `session_type`, `session_name`, `creator_id`, `max_members`, `status`) 
VALUES ('system_notice', 2, '系统通知', 1, 10000, 1);

-- 添加管理员到系统通知会话
INSERT INTO `chat_session_member` (`session_id`, `user_id`, `user_nickname`, `role`, `status`) 
VALUES ('system_notice', 1, 'admin', 3, 1);

-- 发送欢迎消息
INSERT INTO `chat_message` (`message_id`, `session_id`, `from_user_id`, `from_user_nickname`, `message_type`, `content`) 
VALUES ('welcome_msg_001', 'system_notice', 1, 'admin', 1, '欢迎使用若依聊天系统！');
