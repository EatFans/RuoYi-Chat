package com.ruoyi.chat.protocol;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

/**
 * 聊天消息协议
 *
 * @author Fan
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChatMessage {
    
    /** 消息类型 */
    private MessageType type;
    
    /** 发送者ID */
    private Long fromUserId;
    
    /** 发送者昵称 */
    private String fromUserNickname;
    
    /** 发送者头像 */
    private String fromUserAvatar;
    
    /** 接收者ID（单聊时使用） */
    private Long toUserId;
    
    /** 会话ID（群聊时使用） */
    private String sessionId;
    
    /** 消息内容 */
    private String content;
    
    /** 消息内容类型 */
    private ContentType contentType;
    
    /** 消息ID */
    private String messageId;
    
    /** 时间戳 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;
    
    /** 额外数据 */
    private Object extra;

    // 构造函数
    public ChatMessage() {
        this.timestamp = LocalDateTime.now();
    }

    public ChatMessage(MessageType type) {
        this();
        this.type = type;
    }

    // Getter和Setter方法
    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public Long getFromUserId() {
        return fromUserId;
    }

    public void setFromUserId(Long fromUserId) {
        this.fromUserId = fromUserId;
    }

    public String getFromUserNickname() {
        return fromUserNickname;
    }

    public void setFromUserNickname(String fromUserNickname) {
        this.fromUserNickname = fromUserNickname;
    }

    public String getFromUserAvatar() {
        return fromUserAvatar;
    }

    public void setFromUserAvatar(String fromUserAvatar) {
        this.fromUserAvatar = fromUserAvatar;
    }

    public Long getToUserId() {
        return toUserId;
    }

    public void setToUserId(Long toUserId) {
        this.toUserId = toUserId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public ContentType getContentType() {
        return contentType;
    }

    public void setContentType(ContentType contentType) {
        this.contentType = contentType;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Object getExtra() {
        return extra;
    }

    public void setExtra(Object extra) {
        this.extra = extra;
    }

    /**
     * 获取额外数据（别名方法，与getExtra()功能相同）
     */
    public Object getExtraData() {
        return extra;
    }

    /**
     * 设置额外数据（别名方法，与setExtra()功能相同）
     */
    public void setExtraData(Object extraData) {
        this.extra = extraData;
    }
}