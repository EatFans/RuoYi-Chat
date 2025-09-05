package com.ruoyi.chat.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 聊天会话实体类
 *
 * @author ruoyi
 * @date 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("chat_session")
public class ChatSession implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 会话ID
     */
    @TableField("session_id")
    private String sessionId;

    /**
     * 会话类型：1-单聊，2-群聊
     */
    @TableField("session_type")
    private Integer sessionType;

    /**
     * 会话名称（群聊名称）
     */
    @TableField("session_name")
    private String sessionName;

    /**
     * 会话头像
     */
    @TableField("session_avatar")
    private String sessionAvatar;

    /**
     * 创建者ID
     */
    @TableField("creator_id")
    private Long creatorId;

    /**
     * 最大成员数（群聊）
     */
    @TableField("max_members")
    private Integer maxMembers;

    /**
     * 状态：0-禁用，1-启用
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
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /**
     * 会话类型枚举
     */
    public static class SessionType {
        public static final int PRIVATE = 1; // 单聊
        public static final int GROUP = 2;   // 群聊
    }

    /**
     * 状态枚举
     */
    public static class Status {
        public static final int DISABLED = 0; // 禁用
        public static final int ENABLED = 1;  // 启用
    }
}