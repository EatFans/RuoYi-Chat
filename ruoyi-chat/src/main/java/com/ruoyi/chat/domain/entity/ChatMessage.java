package com.ruoyi.chat.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 聊天消息实体类
 *
 * @author ruoyi
 * @date 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("chat_message")
public class ChatMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 消息ID
     */
    @TableField("message_id")
    private String messageId;

    /**
     * 会话ID
     */
    @TableField("session_id")
    private String sessionId;

    /**
     * 发送者ID
     */
    @TableField("from_user_id")
    private Long fromUserId;

    /**
     * 发送者昵称
     */
    @TableField("from_user_nickname")
    private String fromUserNickname;

    /**
     * 发送者头像
     */
    @TableField("from_user_avatar")
    private String fromUserAvatar;

    /**
     * 消息类型：1-文本，2-图片，3-表情，4-文件，99-系统消息
     */
    @TableField("message_type")
    private Integer messageType;

    /**
     * 消息内容
     */
    @TableField("content")
    private String content;

    /**
     * 扩展数据（图片URL、文件信息等）
     */
    @TableField("extra_data")
    private String extraData;

    /**
     * 回复的消息ID
     */
    @TableField("reply_to_message_id")
    private String replyToMessageId;

    /**
     * 消息状态：0-正常，1-已撤回
     */
    @TableField("status")
    private Integer status;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 消息类型枚举
     */
    public static class MessageType {
        public static final int TEXT = 1;    // 文本
        public static final int IMAGE = 2;   // 图片
        public static final int EMOJI = 3;   // 表情
        public static final int FILE = 4;    // 文件
        public static final int SYSTEM = 99; // 系统消息
    }

    /**
     * 消息状态枚举
     */
    public static class Status {
        public static final int NORMAL = 0;    // 正常
        public static final int RECALLED = 1; // 已撤回
    }
}