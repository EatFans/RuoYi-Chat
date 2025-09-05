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

 Date: 05/09/2025 10:44:19
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for chat_message
-- ----------------------------
DROP TABLE IF EXISTS `chat_message`;
CREATE TABLE `chat_message` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `message_id` varchar(64) NOT NULL COMMENT '消息ID',
  `session_id` varchar(64) NOT NULL COMMENT '会话ID',
  `from_user_id` bigint NOT NULL COMMENT '发送者ID',
  `from_user_nickname` varchar(50) DEFAULT NULL COMMENT '发送者昵称',
  `from_user_avatar` varchar(255) DEFAULT NULL COMMENT '发送者头像',
  `message_type` tinyint NOT NULL COMMENT '消息类型：1-文本，2-图片，3-表情，4-文件，99-系统消息',
  `content` text COMMENT '消息内容',
  `extra_data` json DEFAULT NULL COMMENT '扩展数据（图片URL、文件信息等）',
  `reply_to_message_id` varchar(64) DEFAULT NULL COMMENT '回复的消息ID',
  `status` tinyint DEFAULT '0' COMMENT '消息状态：0-正常，1-已撤回',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `message_id` (`message_id`),
  KEY `idx_session` (`session_id`),
  KEY `idx_from_user` (`from_user_id`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_message_type` (`message_type`)
) ENGINE=InnoDB AUTO_INCREMENT=48 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='聊天消息表';

SET FOREIGN_KEY_CHECKS = 1;
