package com.ruoyi.chat.netty.manager;

import com.ruoyi.chat.netty.session.UserSession;
import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * WebSocket连接管理器
 * 负责管理用户连接、在线状态等
 *
 * @author ruoyi
 */
@Component
public class ConnectionManager {

    private static final Logger logger = LoggerFactory.getLogger(ConnectionManager.class);

    /**
     * 用户会话属性键
     */
    public static final AttributeKey<UserSession> USER_SESSION_KEY = AttributeKey.valueOf("USER_SESSION");

    /**
     * 用户ID -> 用户会话映射
     */
    private final ConcurrentMap<Long, UserSession> userSessions = new ConcurrentHashMap<>();

    /**
     * 通道ID -> 用户ID映射
     */
    private final ConcurrentMap<ChannelId, Long> channelUserMap = new ConcurrentHashMap<>();

    /**
     * 添加用户连接
     *
     * @param userId 用户ID
     * @param channel 通道
     * @param userNickname 用户昵称
     * @param userAvatar 用户头像
     */
    public void addConnection(Long userId, Channel channel, String userNickname, String userAvatar) {
        // 如果用户已经有连接，先移除旧连接
        UserSession existingSession = userSessions.get(userId);
        if (existingSession != null) {
            removeConnection(existingSession.getChannel().id());
            logger.info("用户 {} 的旧连接已被新连接替换", userId);
        }

        // 创建新的用户会话
        UserSession userSession = new UserSession(userId, channel, userNickname, userAvatar);
        
        // 设置通道属性
        channel.attr(USER_SESSION_KEY).set(userSession);
        
        // 添加到映射表
        userSessions.put(userId, userSession);
        channelUserMap.put(channel.id(), userId);
        
        logger.info("用户 {} ({}) 连接成功，当前在线用户数: {}", userNickname, userId, userSessions.size());
    }

    /**
     * 移除用户连接
     *
     * @param channelId 通道ID
     */
    public void removeConnection(ChannelId channelId) {
        Long userId = channelUserMap.remove(channelId);
        if (userId != null) {
            UserSession userSession = userSessions.remove(userId);
            if (userSession != null) {
                logger.info("用户 {} ({}) 断开连接，当前在线用户数: {}", 
                    userSession.getUserNickname(), userId, userSessions.size());
            }
        }
    }

    /**
     * 根据用户ID获取用户会话
     *
     * @param userId 用户ID
     * @return 用户会话
     */
    public UserSession getUserSession(Long userId) {
        return userSessions.get(userId);
    }

    /**
     * 根据通道ID获取用户会话
     *
     * @param channelId 通道ID
     * @return 用户会话
     */
    public UserSession getUserSessionByChannel(ChannelId channelId) {
        Long userId = channelUserMap.get(channelId);
        return userId != null ? userSessions.get(userId) : null;
    }

    /**
     * 检查用户是否在线
     *
     * @param userId 用户ID
     * @return 是否在线
     */
    public boolean isUserOnline(Long userId) {
        UserSession session = userSessions.get(userId);
        return session != null && session.getChannel().isActive();
    }

    /**
     * 获取在线用户数量
     *
     * @return 在线用户数量
     */
    public int getOnlineUserCount() {
        return userSessions.size();
    }

    /**
     * 获取所有在线用户ID
     *
     * @return 在线用户ID集合
     */
    public Set<Long> getOnlineUserIds() {
        return userSessions.keySet();
    }

    /**
     * 获取所有在线用户会话
     *
     * @return 在线用户会话集合
     */
    public Set<UserSession> getOnlineUserSessions() {
        return userSessions.values().stream()
                .filter(session -> session.getChannel().isActive())
                .collect(Collectors.toSet());
    }

    /**
     * 向指定用户发送消息
     *
     * @param userId 用户ID
     * @param message 消息内容
     * @return 是否发送成功
     */
    public boolean sendMessageToUser(Long userId, Object message) {
        UserSession session = getUserSession(userId);
        if (session != null && session.getChannel().isActive()) {
            session.getChannel().writeAndFlush(message);
            return true;
        }
        return false;
    }

    /**
     * 向多个用户发送消息
     *
     * @param userIds 用户ID集合
     * @param message 消息内容
     * @return 成功发送的用户数量
     */
    public int sendMessageToUsers(Set<Long> userIds, Object message) {
        int successCount = 0;
        for (Long userId : userIds) {
            if (sendMessageToUser(userId, message)) {
                successCount++;
            }
        }
        return successCount;
    }

    /**
     * 向所有在线用户广播消息
     *
     * @param message 消息内容
     * @return 成功发送的用户数量
     */
    public int broadcastMessage(Object message) {
        return sendMessageToUsers(getOnlineUserIds(), message);
    }

    /**
     * 清理无效连接
     */
    public void cleanupInactiveConnections() {
        userSessions.entrySet().removeIf(entry -> {
            UserSession session = entry.getValue();
            if (!session.getChannel().isActive()) {
                channelUserMap.remove(session.getChannel().id());
                logger.debug("清理无效连接: 用户 {} ({})", session.getUserNickname(), entry.getKey());
                return true;
            }
            return false;
        });
    }
}