package com.ruoyi.chat.protocol;

/**
 * 消息类型枚举
 *
 * @author Fan
 */
public enum MessageType {
    /** 连接认证 */
    AUTH,
    
    /** 认证成功 */
    AUTH_SUCCESS,
    
    /** 认证失败 */
    AUTH_FAILED,
    
    /** 单聊消息 */
    PRIVATE_CHAT,
    
    /** 群聊消息 */
    GROUP_CHAT,
    
    /** 心跳包 */
    HEARTBEAT,
    
    /** 心跳响应 */
    HEARTBEAT_RESPONSE,
    
    /** 用户上线通知 */
    USER_ONLINE,
    
    /** 用户下线通知 */
    USER_OFFLINE,
    
    /** 消息已读回执 */
    MESSAGE_READ,
    
    /** 系统通知 */
    SYSTEM_NOTICE,
    
    /** 错误消息 */
    ERROR
}