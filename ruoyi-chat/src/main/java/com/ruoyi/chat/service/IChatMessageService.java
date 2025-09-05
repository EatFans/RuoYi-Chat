package com.ruoyi.chat.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.chat.domain.entity.ChatMessage;
// 使用完全限定名避免类名冲突

import java.time.LocalDateTime;
import java.util.List;

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
    void savePrivateMessage(com.ruoyi.chat.protocol.ChatMessage message);
    
    /**
     * 保存群聊消息
     *
     * @param message 聊天消息
     */
    void saveGroupMessage(com.ruoyi.chat.protocol.ChatMessage message);
    
    /**
     * 标记消息为已读
     *
     * @param messageId 消息ID
     * @param userId 用户ID
     */
    void markMessageAsRead(String messageId, Long userId);

    /**
     * 发送消息
     *
     * @param sessionId 会话ID
     * @param fromUserId 发送者ID
     * @param messageType 消息类型
     * @param content 消息内容
     * @param extraData 扩展数据
     * @return 消息实体
     */
    ChatMessage sendMessage(String sessionId, Long fromUserId, Integer messageType, String content, String extraData);

    /**
     * 分页查询会话消息
     *
     * @param page 分页对象
     * @param sessionId 会话ID
     * @return 分页消息列表
     */
    IPage<ChatMessage> getSessionMessages(Page<ChatMessage> page, String sessionId);

    /**
     * 查询会话消息列表
     *
     * @param sessionId 会话ID
     * @return 消息列表
     */
    List<ChatMessage> getSessionMessages(String sessionId);

    /**
     * 查询会话最新消息
     *
     * @param sessionId 会话ID
     * @param limit 限制数量
     * @return 最新消息列表
     */
    List<ChatMessage> getLatestMessages(String sessionId, Integer limit);

    /**
     * 查询指定时间之后的消息
     *
     * @param sessionId 会话ID
     * @param afterTime 时间点
     * @return 消息列表
     */
    List<ChatMessage> getMessagesAfterTime(String sessionId, LocalDateTime afterTime);

    /**
     * 查询未读消息数量
     *
     * @param sessionId 会话ID
     * @param userId 用户ID
     * @param lastReadTime 最后阅读时间
     * @return 未读消息数量
     */
    Long getUnreadMessageCount(String sessionId, Long userId, LocalDateTime lastReadTime);

    /**
     * 撤回消息
     *
     * @param messageId 消息ID
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean recallMessage(String messageId, Long userId);

    /**
     * 根据消息ID查询消息
     *
     * @param messageId 消息ID
     * @return 消息实体
     */
    ChatMessage getMessageById(String messageId);

    /**
     * 批量标记消息为已读
     *
     * @param sessionId 会话ID
     * @param userId 用户ID
     * @param messageIds 消息ID列表
     */
    void batchMarkMessagesAsRead(String sessionId, Long userId, List<String> messageIds);
}
