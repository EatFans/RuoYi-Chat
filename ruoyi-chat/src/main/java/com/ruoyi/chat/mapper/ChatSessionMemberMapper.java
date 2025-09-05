package com.ruoyi.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.chat.domain.entity.ChatSessionMember;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 会话成员Mapper接口
 *
 * @author ruoyi
 * @date 2024-01-01
 */
@Mapper
public interface ChatSessionMemberMapper extends BaseMapper<ChatSessionMember> {

    /**
     * 根据会话ID查询成员列表
     *
     * @param sessionId 会话ID
     * @return 成员列表
     */
    List<ChatSessionMember> selectMembersBySessionId(@Param("sessionId") String sessionId);

    /**
     * 根据会话ID和用户ID查询成员信息
     *
     * @param sessionId 会话ID
     * @param userId 用户ID
     * @return 成员信息
     */
    ChatSessionMember selectMemberBySessionIdAndUserId(@Param("sessionId") String sessionId, @Param("userId") Long userId);

    /**
     * 根据用户ID查询所有会话成员记录
     *
     * @param userId 用户ID
     * @return 会话成员列表
     */
    List<ChatSessionMember> selectMembersByUserId(@Param("userId") Long userId);

    /**
     * 批量插入会话成员
     *
     * @param members 成员列表
     * @return 插入数量
     */
    int insertBatchMembers(@Param("members") List<ChatSessionMember> members);

    /**
     * 更新最后阅读时间
     *
     * @param sessionId 会话ID
     * @param userId 用户ID
     * @return 更新数量
     */
    int updateLastReadTime(@Param("sessionId") String sessionId, @Param("userId") Long userId);
}