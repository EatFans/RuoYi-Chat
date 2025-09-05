package com.ruoyi.chat.protocol;

/**
 * 消息内容类型枚举
 *
 * @author Fan
 */
public enum ContentType {
    /** 文本消息 */
    TEXT,
    
    /** 图片消息 */
    IMAGE,
    
    /** 文件消息 */
    FILE,
    
    /** 语音消息 */
    VOICE,
    
    /** 视频消息 */
    VIDEO,
    
    /** 表情消息 */
    EMOJI,
    
    /** 位置消息 */
    LOCATION,
    
    /** 链接消息 */
    LINK
}