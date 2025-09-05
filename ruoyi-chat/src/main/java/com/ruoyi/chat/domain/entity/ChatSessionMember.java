package com.ruoyi.chat.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 会话成员实体类
 *
 * @author ruoyi
 * @date 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("chat_session_member")
public class ChatSessionMember implements Serializable {

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
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 用户昵称
     */
    @TableField("user_nickname")
    private String userNickname;

    /**
     * 用户头像
     */
    @TableField("user_avatar")
    private String userAvatar;

    /**
     * 角色：0-普通成员，1-管理员，2-群主
     */
    @TableField("role")
    private Integer role;

    /**
     * 加入时间
     */
    @TableField(value = "join_time", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime joinTime;

    /**
     * 最后阅读时间
     */
    @TableField("last_read_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastReadTime;

    /**
     * 是否禁言：0-否，1-是
     */
    @TableField("is_muted")
    private Integer isMuted;

    /**
     * 状态：0-已退出，1-正常
     */
    @TableField("status")
    private Integer status;

    /**
     * 角色枚举
     */
    public static class Role {
        public static final int MEMBER = 0;  // 普通成员
        public static final int ADMIN = 1;   // 管理员
        public static final int OWNER = 2;   // 群主
    }

    /**
     * 禁言状态枚举
     */
    public static class MuteStatus {
        public static final int NOT_MUTED = 0; // 未禁言
        public static final int MUTED = 1;     // 已禁言
    }

    /**
     * 状态枚举
     */
    public static class Status {
        public static final int LEFT = 0;   // 已退出
        public static final int NORMAL = 1; // 正常
    }
}