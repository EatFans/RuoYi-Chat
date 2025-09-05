package com.ruoyi.chat.netty.router;

import com.ruoyi.chat.domain.entity.ChatSessionMember;
import com.ruoyi.chat.protocol.ChatMessage;
import com.ruoyi.chat.protocol.MessageType;
import com.ruoyi.chat.netty.manager.ConnectionManager;
import com.ruoyi.chat.netty.session.UserSession;
import com.ruoyi.chat.service.IChatSessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 消息路由器
 * 负责将消息路由到正确的接收者
 *
 * @author ruoyi
 */
@Component
public class MessageRouter {

    private static final Logger logger = LoggerFactory.getLogger(MessageRouter.class);

    @Autowired
    private ConnectionManager connectionManager;

    @Autowired
    private IChatSessionService chatSessionService;

    /**
     * 路由消息到目标用户
     *
     * @param message 聊天消息
     */
    public void routeMessage(ChatMessage message) {
        try {
            // 根据消息类型判断是私聊还是群聊
            if (message.getToUserId() != null) {
                // 有目标用户ID，说明是私聊
                routePrivateMessage(message);
            } else if (message.getSessionId() != null) {
                // 有会话ID但没有目标用户ID，说明是群聊
                routeGroupMessage(message);
            } else {
                logger.warn("消息缺少路由信息: {}", message.getMessageId());
            }
        } catch (Exception e) {
            logger.error("消息路由失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 路由私聊消息
     *
     * @param message 聊天消息
     */
    private void routePrivateMessage(ChatMessage message) {
        String sessionId = message.getSessionId();
        Long fromUserId = message.getFromUserId();
        
        // 获取会话成员
        List<ChatSessionMember> members = chatSessionService.getSessionMembers(sessionId);
        if (members.size() != 2) {
            logger.warn("私聊会话 {} 的成员数量异常: {}", sessionId, members.size());
            return;
        }
        
        // 找到接收者（不是发送者的另一个成员）
        ChatSessionMember receiver = members.stream()
                .filter(member -> !member.getUserId().equals(fromUserId))
                .findFirst()
                .orElse(null);
        
        if (receiver == null) {
            logger.warn("私聊会话 {} 中未找到接收者", sessionId);
            return;
        }
        
        // 发送消息给接收者
        boolean success = connectionManager.sendMessageToUser(receiver.getUserId(), message);
        if (success) {
            logger.debug("私聊消息已发送: {} -> {}", fromUserId, receiver.getUserId());
        } else {
            logger.debug("用户 {} 不在线，私聊消息未发送", receiver.getUserId());
        }
    }

    /**
     * 路由群聊消息
     *
     * @param message 聊天消息
     */
    private void routeGroupMessage(ChatMessage message) {
        String sessionId = message.getSessionId();
        Long fromUserId = message.getFromUserId();
        
        // 获取会话成员
        List<ChatSessionMember> members = chatSessionService.getSessionMembers(sessionId);
        if (members.isEmpty()) {
            logger.warn("群聊会话 {} 没有成员", sessionId);
            return;
        }
        
        // 获取除发送者外的所有成员ID
        Set<Long> receiverIds = members.stream()
                .map(ChatSessionMember::getUserId)
                .filter(userId -> !userId.equals(fromUserId))
                .collect(Collectors.toSet());
        
        if (receiverIds.isEmpty()) {
            logger.debug("群聊会话 {} 中除发送者外没有其他成员", sessionId);
            return;
        }
        
        // 批量发送消息
        int successCount = connectionManager.sendMessageToUsers(receiverIds, message);
        logger.debug("群聊消息已发送: 会话 {}, 目标用户数 {}, 成功发送 {}", 
                sessionId, receiverIds.size(), successCount);
    }

    /**
     * 发送系统通知
     *
     * @param message 系统消息
     * @param targetUserIds 目标用户ID集合，如果为null则广播给所有在线用户
     */
    public void sendSystemNotification(ChatMessage message, Set<Long> targetUserIds) {
        try {
            int successCount;
            if (targetUserIds == null || targetUserIds.isEmpty()) {
                // 广播给所有在线用户
                successCount = connectionManager.broadcastMessage(message);
                logger.info("系统通知已广播，成功发送给 {} 个在线用户", successCount);
            } else {
                // 发送给指定用户
                successCount = connectionManager.sendMessageToUsers(targetUserIds, message);
                logger.info("系统通知已发送，目标用户数 {}, 成功发送 {}", targetUserIds.size(), successCount);
            }
        } catch (Exception e) {
            logger.error("发送系统通知失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 发送会话状态变更通知
     *
     * @param sessionId 会话ID
     * @param notificationType 通知类型
     * @param content 通知内容
     */
    public void sendSessionNotification(String sessionId, String notificationType, String content) {
        try {
            // 获取会话成员
            List<ChatSessionMember> members = chatSessionService.getSessionMembers(sessionId);
            if (members.isEmpty()) {
                logger.warn("会话 {} 没有成员，无法发送通知", sessionId);
                return;
            }
            
            // 创建通知消息
            ChatMessage notification = new ChatMessage();
            notification.setSessionId(sessionId);
            notification.setType(MessageType.SYSTEM_NOTICE);
            notification.setContent(content);
            notification.setFromUserId(0L); // 系统消息
            notification.setTimestamp(new Date());
            
            // 发送给所有成员
            Set<Long> memberIds = members.stream()
                    .map(ChatSessionMember::getUserId)
                    .collect(Collectors.toSet());
            
            int successCount = connectionManager.sendMessageToUsers(memberIds, notification);
            logger.debug("会话通知已发送: 会话 {}, 类型 {}, 目标用户数 {}, 成功发送 {}", 
                    sessionId, notificationType, memberIds.size(), successCount);
        } catch (Exception e) {
            logger.error("发送会话通知失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 发送用户状态变更通知
     *
     * @param userId 用户ID
     * @param status 用户状态（online/offline）
     */
    public void sendUserStatusNotification(Long userId, String status) {
        try {
            UserSession userSession = connectionManager.getUserSession(userId);
            if (userSession == null) {
                return;
            }
            
            // 创建状态通知消息
            ChatMessage statusNotification = new ChatMessage();
            statusNotification.setType("online".equals(status) ? MessageType.USER_ONLINE : MessageType.USER_OFFLINE);
            statusNotification.setFromUserId(userId);

            statusNotification.setContent(status);
            statusNotification.setTimestamp(new Date());
            
            // 广播给所有在线用户
            int successCount = connectionManager.broadcastMessage(statusNotification);
            logger.debug("用户状态通知已广播: 用户 {} 状态 {}, 成功发送给 {} 个用户", 
                    userId, status, successCount);
        } catch (Exception e) {
            logger.error("发送用户状态通知失败: {}", e.getMessage(), e);
        }
    }
}