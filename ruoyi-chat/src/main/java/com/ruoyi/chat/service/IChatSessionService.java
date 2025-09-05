package com.ruoyi.chat.service;

import com.ruoyi.chat.domain.entity.ChatSession;
import com.ruoyi.chat.domain.entity.ChatSessionMember;

import java.util.List;

/**
 * 聊天会话业务接口类
 *
 * @author Fan
 */
public interface IChatSessionService {

    /**
     * 创建私聊会话
     *
     * @param userId1 用户1ID
     * @param userId2 用户2ID
     * @return 会话信息
     */
    ChatSession createPrivateSession(Long userId1, Long userId2);

    /**
     * 创建群聊会话
     *
     * @param creatorId 创建者ID
     * @param sessionName 群聊名称
     * @param memberIds 成员ID列表
     * @return 会话信息
     */
    ChatSession createGroupSession(Long creatorId, String sessionName, List<Long> memberIds);

    /**
     * 根据用户ID查询会话列表
     *
     * @param userId 用户ID
     * @return 会话列表
     */
    List<ChatSession> getUserSessions(Long userId);

    /**
     * 根据会话ID查询会话信息
     *
     * @param sessionId 会话ID
     * @return 会话信息
     */
    ChatSession getSessionById(String sessionId);

    /**
     * 加入会话
     *
     * @param sessionId 会话ID
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean joinSession(String sessionId, Long userId);

    /**
     * 离开会话
     *
     * @param sessionId 会话ID
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean leaveSession(String sessionId, Long userId);

    /**
     * 获取会话成员列表
     *
     * @param sessionId 会话ID
     * @return 成员列表
     */
    List<ChatSessionMember> getSessionMembers(String sessionId);

    /**
     * 检查用户是否在会话中
     *
     * @param sessionId 会话ID
     * @param userId 用户ID
     * @return 是否在会话中
     */
    boolean isUserInSession(String sessionId, Long userId);

    /**
     * 获取或创建私聊会话
     *
     * @param userId1 用户1ID
     * @param userId2 用户2ID
     * @return 会话信息
     */
    ChatSession getOrCreatePrivateSession(Long userId1, Long userId2);
}
