package com.ruoyi.chat.service.impl;

import com.ruoyi.chat.protocol.ChatMessage;
import com.ruoyi.chat.service.IChatMessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 聊天消息业务接口实现类
 *
 * @author Fan
 */
@Service
public class ChatMessageService implements IChatMessageService {
    
    private static final Logger logger = LoggerFactory.getLogger(ChatMessageService.class);
    
    @Override
    public void savePrivateMessage(ChatMessage message) {
        try {
            // TODO: 实现私聊消息保存逻辑
            // 1. 将ChatMessage转换为数据库实体
            // 2. 保存到chat_message表
            // 3. 更新会话最后消息时间
            logger.info("保存私聊消息：{} -> {}, 内容：{}", 
                message.getFromUserId(), message.getToUserId(), message.getContent());
        } catch (Exception e) {
            logger.error("保存私聊消息失败", e);
            throw new RuntimeException("保存私聊消息失败", e);
        }
    }
    
    @Override
    public void saveGroupMessage(ChatMessage message) {
        try {
            // TODO: 实现群聊消息保存逻辑
            // 1. 将ChatMessage转换为数据库实体
            // 2. 保存到chat_message表
            // 3. 更新群聊会话最后消息时间
            logger.info("保存群聊消息：{} -> 会话 {}, 内容：{}", 
                message.getFromUserId(), message.getSessionId(), message.getContent());
        } catch (Exception e) {
            logger.error("保存群聊消息失败", e);
            throw new RuntimeException("保存群聊消息失败", e);
        }
    }
    
    @Override
    public void markMessageAsRead(String messageId, Long userId) {
        try {
            // TODO: 实现消息已读标记逻辑
            // 1. 更新chat_message_read表
            // 2. 记录用户已读时间
            logger.info("标记消息已读：用户 {} 消息 {}", userId, messageId);
        } catch (Exception e) {
            logger.error("标记消息已读失败", e);
            throw new RuntimeException("标记消息已读失败", e);
        }
    }
}
