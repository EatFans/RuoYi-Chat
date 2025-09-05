package com.ruoyi.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.chat.domain.entity.ChatSession;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 聊天会话Mapper接口
 *
 * @author ruoyi
 * @date 2024-01-01
 */
@Mapper
public interface ChatSessionMapper extends BaseMapper<ChatSession> {

    /**
     * 根据用户ID查询会话列表
     *
     * @param userId 用户ID
     * @return 会话列表
     */
    List<ChatSession> selectSessionsByUserId(@Param("userId") Long userId);

    /**
     * 根据会话ID和用户ID查询会话
     *
     * @param sessionId 会话ID
     * @param userId 用户ID
     * @return 会话信息
     */
    ChatSession selectSessionByIdAndUserId(@Param("sessionId") String sessionId, @Param("userId") Long userId);

    /**
     * 根据两个用户ID查询私聊会话
     *
     * @param userId1 用户1ID
     * @param userId2 用户2ID
     * @return 私聊会话
     */
    ChatSession selectPrivateSessionByUserIds(@Param("userId1") Long userId1, @Param("userId2") Long userId2);
}