package com.ruoyi.chat.netty;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * 聊天连接管理器
 *
 * @author Fan
 */
@Component
public class ChatConnectionManager {
    private static final Logger log = LoggerFactory.getLogger(ChatConnectionManager.class);

    /** 所有连接的通道组 */
    private final ChannelGroup allChannels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    
    /** 用户连接映射 userId -> Channel */
    private final Map<Long, Channel> userChannels = new ConcurrentHashMap<>();

    /** 通道用户映射 channelId -> userId */
    private final Map<String, Long> channelUsers = new ConcurrentHashMap<>();
    
    /** 群聊会话成员映射 sessionId -> Set<userId> */
    private final Map<String, Set<Long>> sessionMembers = new ConcurrentHashMap<>();
    
    /** 用户加入的群聊映射 userId -> Set<sessionId> */
    private final Map<Long, Set<String>> userSessions = new ConcurrentHashMap<>();

    /**
     * 添加用户连接
     */
    public void addConnection(Long userId, Channel channel) {
        // 移除旧连接
        removeConnection(userId);
        
        userChannels.put(userId, channel);
        channelUsers.put(channel.id().asShortText(), userId);
        allChannels.add(channel);
        
        log.info("用户 {} 连接成功，通道ID：{}", userId, channel.id().asShortText());
    }

    /**
     * 移除用户连接
     */
    public void removeConnection(Long userId) {
        Channel oldChannel = userChannels.remove(userId);
        if (oldChannel != null) {
            channelUsers.remove(oldChannel.id().asShortText());
            allChannels.remove(oldChannel);
            if (oldChannel.isActive()) {
                oldChannel.close();
            }
            log.info("用户 {} 连接已移除", userId);
        }
    }

    /**
     * 根据通道移除连接
     */
    public void removeConnection(Channel channel) {
        String channelId = channel.id().asShortText();
        Long userId = channelUsers.remove(channelId);
        if (userId != null) {
            userChannels.remove(userId);
            allChannels.remove(channel);
            log.info("通道 {} 对应的用户 {} 连接已移除", channelId, userId);
        }
    }

    /**
     * 获取用户连接
     */
    public Channel getUserChannel(Long userId) {
        return userChannels.get(userId);
    }

    /**
     * 获取通道对应的用户ID
     */
    public Long getChannelUserId(Channel channel) {
        return channelUsers.get(channel.id().asShortText());
    }

    /**
     * 检查用户是否在线
     */
    public boolean isUserOnline(Long userId) {
        Channel channel = userChannels.get(userId);
        return channel != null && channel.isActive();
    }

    /**
     * 获取在线用户数量
     */
    public int getOnlineUserCount() {
        return userChannels.size();
    }

    /**
     * 获取所有在线用户ID
     */
    public Set<Long> getOnlineUserIds() {
        return userChannels.keySet();
    }

    /**
     * 加入群聊会话
     */
    public void joinSession(Long userId, String sessionId) {
        sessionMembers.computeIfAbsent(sessionId, k -> new CopyOnWriteArraySet<>()).add(userId);
        userSessions.computeIfAbsent(userId, k -> new CopyOnWriteArraySet<>()).add(sessionId);
        log.info("用户 {} 加入会话 {}", userId, sessionId);
    }

    /**
     * 离开群聊会话
     */
    public void leaveSession(Long userId, String sessionId) {
        Set<Long> members = sessionMembers.get(sessionId);
        if (members != null) {
            members.remove(userId);
            if (members.isEmpty()) {
                sessionMembers.remove(sessionId);
            }
        }
        
        Set<String> sessions = userSessions.get(userId);
        if (sessions != null) {
            sessions.remove(sessionId);
            if (sessions.isEmpty()) {
                userSessions.remove(userId);
            }
        }
        log.info("用户 {} 离开会话 {}", userId, sessionId);
    }

    /**
     * 获取会话成员
     */
    public Set<Long> getSessionMembers(String sessionId) {
        return sessionMembers.getOrDefault(sessionId, new CopyOnWriteArraySet<>());
    }

    /**
     * 获取用户加入的会话
     */
    public Set<String> getUserSessions(Long userId) {
        return userSessions.getOrDefault(userId, new CopyOnWriteArraySet<>());
    }
}
