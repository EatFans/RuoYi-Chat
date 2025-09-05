package com.ruoyi.chat.service;

import com.ruoyi.chat.protocol.ChatMessage;

/**
 * 聊天消息业务接口类
 *
 * @author Fan
 */
public interface IChatMessageService {
    
    /**
     * 保存私聊消息
     *
     * @param message 聊天消息
     */
    void savePrivateMessage(ChatMessage message);
    
    /**
     * 保存群聊消息
     *
     * @param message 聊天消息
     */
    void saveGroupMessage(ChatMessage message);
    
    /**
     * 标记消息为已读
     *
     * @param messageId 消息ID
     * @param userId 用户ID
     */
    void markMessageAsRead(String messageId, Long userId);
}
