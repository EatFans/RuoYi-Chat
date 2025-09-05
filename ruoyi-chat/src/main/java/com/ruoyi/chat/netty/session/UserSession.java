package com.ruoyi.chat.netty.session;

import io.netty.channel.Channel;
import io.netty.channel.ChannelId;

import java.time.LocalDateTime;

/**
 * 用户会话信息
 *
 * @author ruoyi
 */
public class UserSession {
    
    /** 用户ID */
    private Long userId;
    
    /** 用户昵称 */
    private String userNickname;
    
    /** 用户头像 */
    private String userAvatar;
    
    /** 通道 */
    private Channel channel;
    
    /** 通道ID */
    private ChannelId channelId;
    
    /** 登录时间 */
    private LocalDateTime loginTime;
    
    /** 最后活跃时间 */
    private LocalDateTime lastActiveTime;
    
    public UserSession() {
        this.loginTime = LocalDateTime.now();
        this.lastActiveTime = LocalDateTime.now();
    }
    
    public UserSession(Long userId, String userNickname, String userAvatar, ChannelId channelId) {
        this();
        this.userId = userId;
        this.userNickname = userNickname;
        this.userAvatar = userAvatar;
        this.channelId = channelId;
    }
    
    public UserSession(Long userId, Channel channel, String userNickname, String userAvatar) {
        this();
        this.userId = userId;
        this.channel = channel;
        this.channelId = channel.id();
        this.userNickname = userNickname;
        this.userAvatar = userAvatar;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getUserNickname() {
        return userNickname;
    }
    
    public void setUserNickname(String userNickname) {
        this.userNickname = userNickname;
    }
    
    public String getUserAvatar() {
        return userAvatar;
    }
    
    public void setUserAvatar(String userAvatar) {
        this.userAvatar = userAvatar;
    }
    
    public Channel getChannel() {
        return channel;
    }
    
    public void setChannel(Channel channel) {
        this.channel = channel;
        this.channelId = channel != null ? channel.id() : null;
    }
    
    public ChannelId getChannelId() {
        return channelId;
    }

    public void setChannelId(ChannelId channelId) {
        this.channelId = channelId;
    }
    
    public LocalDateTime getLoginTime() {
        return loginTime;
    }
    
    public void setLoginTime(LocalDateTime loginTime) {
        this.loginTime = loginTime;
    }
    
    public LocalDateTime getLastActiveTime() {
        return lastActiveTime;
    }
    
    public void setLastActiveTime(LocalDateTime lastActiveTime) {
        this.lastActiveTime = lastActiveTime;
    }
    
    /**
     * 更新最后活跃时间
     */
    public void updateLastActiveTime() {
        this.lastActiveTime = LocalDateTime.now();
    }
    
    @Override
    public String toString() {
        return "UserSession{" +
                "userId=" + userId +
                ", userNickname='" + userNickname + '\'' +
                ", channelId=" + channelId +
                ", loginTime=" + loginTime +
                ", lastActiveTime=" + lastActiveTime +
                '}';
    }
}