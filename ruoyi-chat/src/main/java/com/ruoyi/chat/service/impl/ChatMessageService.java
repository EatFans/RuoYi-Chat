package com.ruoyi.chat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.chat.domain.entity.ChatMessage;
import com.ruoyi.chat.domain.entity.ChatMessageRead;
import com.ruoyi.chat.mapper.ChatMessageMapper;
import com.ruoyi.chat.mapper.ChatMessageReadMapper;
// 使用完全限定名避免类名冲突
import com.ruoyi.chat.service.IChatMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 聊天消息业务接口实现类
 *
 * @author Fan
 */
@Slf4j
@Service
public class ChatMessageService implements IChatMessageService {
    
    @Autowired
    private ChatMessageMapper chatMessageMapper;
    
    @Autowired
    private ChatMessageReadMapper chatMessageReadMapper;
    
    @Override
    @Transactional
    public void savePrivateMessage(com.ruoyi.chat.protocol.ChatMessage message) {
        try {
            ChatMessage dbMessage = convertToDbMessage(message);
            chatMessageMapper.insert(dbMessage);
            log.info("保存私聊消息成功：{} -> {}, 内容：{}", 
                message.getFromUserId(), message.getToUserId(), message.getContent());
        } catch (Exception e) {
            log.error("保存私聊消息失败", e);
            throw new RuntimeException("保存私聊消息失败", e);
        }
    }
    
    @Override
    @Transactional
    public void saveGroupMessage(com.ruoyi.chat.protocol.ChatMessage message) {
        try {
            ChatMessage dbMessage = convertToDbMessage(message);
            chatMessageMapper.insert(dbMessage);
            log.info("保存群聊消息成功：{} -> 会话 {}, 内容：{}", 
                message.getFromUserId(), message.getSessionId(), message.getContent());
        } catch (Exception e) {
            log.error("保存群聊消息失败", e);
            throw new RuntimeException("保存群聊消息失败", e);
        }
    }
    
    @Override
    @Transactional
    public void markMessageAsRead(String messageId, Long userId) {
        try {
            // 检查是否已经标记为已读
            if (chatMessageReadMapper.checkUserReadMessage(messageId, userId)) {
                return;
            }
            
            // 获取消息信息
            ChatMessage message = getMessageById(messageId);
            if (message == null) {
                log.warn("消息不存在，messageId: {}", messageId);
                return;
            }
            
            ChatMessageRead readStatus = new ChatMessageRead()
                    .setMessageId(messageId)
                    .setSessionId(message.getSessionId())
                    .setUserId(userId)
                    .setReadTime(LocalDateTime.now());
            
            chatMessageReadMapper.insert(readStatus);
            log.info("标记消息已读成功：用户 {} 消息 {}", userId, messageId);
        } catch (Exception e) {
            log.error("标记消息已读失败", e);
            throw new RuntimeException("标记消息已读失败", e);
        }
    }

    @Override
    @Transactional
    public ChatMessage sendMessage(String sessionId, Long fromUserId, Integer messageType, String content, String extraData) {
        String messageId = UUID.randomUUID().toString().replace("-", "");
        
        ChatMessage message = new ChatMessage()
                .setMessageId(messageId)
                .setSessionId(sessionId)
                .setFromUserId(fromUserId)
                .setMessageType(messageType)
                .setContent(content)
                .setExtraData(extraData)
                .setStatus(ChatMessage.Status.NORMAL)
                .setCreateTime(LocalDateTime.now());
        
        chatMessageMapper.insert(message);
        log.info("发送消息成功，messageId: {}, sessionId: {}, fromUserId: {}", messageId, sessionId, fromUserId);
        return message;
    }

    @Override
    public IPage<ChatMessage> getSessionMessages(Page<ChatMessage> page, String sessionId) {
        return chatMessageMapper.selectMessagesBySessionId(page, sessionId);
    }

    @Override
    public List<ChatMessage> getSessionMessages(String sessionId) {
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessage::getSessionId, sessionId)
               .eq(ChatMessage::getStatus, ChatMessage.Status.NORMAL)
               .orderByDesc(ChatMessage::getCreateTime);
        return chatMessageMapper.selectList(wrapper);
    }

    @Override
    public List<ChatMessage> getLatestMessages(String sessionId, Integer limit) {
        return chatMessageMapper.selectLatestMessagesBySessionId(sessionId, limit);
    }

    @Override
    public List<ChatMessage> getMessagesAfterTime(String sessionId, LocalDateTime afterTime) {
        return chatMessageMapper.selectMessagesAfterTime(sessionId, afterTime);
    }

    @Override
    public Long getUnreadMessageCount(String sessionId, Long userId, LocalDateTime lastReadTime) {
        return chatMessageMapper.countUnreadMessages(sessionId, userId, lastReadTime);
    }

    @Override
    @Transactional
    public boolean recallMessage(String messageId, Long userId) {
        ChatMessage message = getMessageById(messageId);
        if (message == null) {
            log.warn("消息不存在，messageId: {}", messageId);
            return false;
        }
        
        if (!message.getFromUserId().equals(userId)) {
            log.warn("用户无权撤回消息，messageId: {}, userId: {}", messageId, userId);
            return false;
        }
        
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessage::getMessageId, messageId);
        
        ChatMessage updateMessage = new ChatMessage()
                .setStatus(ChatMessage.Status.RECALLED);
        
        int result = chatMessageMapper.update(updateMessage, wrapper);
        log.info("撤回消息，messageId: {}, userId: {}, result: {}", messageId, userId, result > 0);
        return result > 0;
    }

    @Override
    public ChatMessage getMessageById(String messageId) {
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessage::getMessageId, messageId);
        return chatMessageMapper.selectOne(wrapper);
    }

    @Override
    @Transactional
    public void batchMarkMessagesAsRead(String sessionId, Long userId, List<String> messageIds) {
        List<ChatMessageRead> readStatuses = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        
        for (String messageId : messageIds) {
            // 检查是否已经标记为已读
            if (!chatMessageReadMapper.checkUserReadMessage(messageId, userId)) {
                readStatuses.add(new ChatMessageRead()
                        .setMessageId(messageId)
                        .setSessionId(sessionId)
                        .setUserId(userId)
                        .setReadTime(now));
            }
        }
        
        if (!readStatuses.isEmpty()) {
            chatMessageReadMapper.insertBatchReadStatus(readStatuses);
            log.info("批量标记消息已读成功，sessionId: {}, userId: {}, count: {}", sessionId, userId, readStatuses.size());
        }
    }
    
    /**
     * 将协议消息转换为数据库实体
     */
    private ChatMessage convertToDbMessage(com.ruoyi.chat.protocol.ChatMessage protocolMessage) {
        return new ChatMessage()
                .setMessageId(protocolMessage.getMessageId())
                .setSessionId(protocolMessage.getSessionId())
                .setFromUserId(protocolMessage.getFromUserId())
                .setMessageType(protocolMessage.getContentType().ordinal() + 1) // 枚举转数字
                .setContent(protocolMessage.getContent())
                .setExtraData(protocolMessage.getExtraData() != null ? protocolMessage.getExtraData().toString() : null)
                .setStatus(ChatMessage.Status.NORMAL)
                .setCreateTime(protocolMessage.getTimestamp());
    }
}
