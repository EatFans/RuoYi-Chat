/*
 Navicat Premium Dump SQL

 Source Server         : 本地数据库
 Source Server Type    : MySQL
 Source Server Version : 80404 (8.4.4)
 Source Host           : localhost:3306
 Source Schema         : ruoyi-chat

 Target Server Type    : MySQL
 Target Server Version : 80404 (8.4.4)
 File Encoding         : 65001

 Date: 05/09/2025 10:45:03
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for chat_session_member
-- ----------------------------
DROP TABLE IF EXISTS `chat_session_member`;
CREATE TABLE `chat_session_member` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `session_id` varchar(64) NOT NULL COMMENT '会话ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `user_nickname` varchar(50) DEFAULT NULL COMMENT '用户昵称',
  `user_avatar` varchar(255) DEFAULT NULL COMMENT '用户头像',
  `role` tinyint DEFAULT '0' COMMENT '角色：0-普通成员，1-管理员，2-群主',
  `join_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
  `last_read_time` datetime DEFAULT NULL COMMENT '最后阅读时间',
  `is_muted` tinyint DEFAULT '0' COMMENT '是否禁言：0-否，1-是',
  `status` tinyint DEFAULT '1' COMMENT '状态：0-已退出，1-正常',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_session_user` (`session_id`,`user_id`),
  KEY `idx_user` (`user_id`),
  KEY `idx_session` (`session_id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='会话成员表';

SET FOREIGN_KEY_CHECKS = 1;
