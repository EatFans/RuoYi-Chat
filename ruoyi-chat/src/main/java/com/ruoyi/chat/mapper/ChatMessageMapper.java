package com.ruoyi.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.chat.domain.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 聊天消息Mapper接口
 *
 * @author ruoyi
 * @date 2024-01-01
 */
@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {

    /**
     * 分页查询会话消息
     *
     * @param page 分页参数
     * @param sessionId 会话ID
     * @return 消息分页列表
     */
    IPage<ChatMessage> selectMessagesBySessionId(Page<ChatMessage> page, @Param("sessionId") String sessionId);

    /**
     * 查询会话最新消息
     *
     * @param sessionId 会话ID
     * @param limit 限制数量
     * @return 最新消息列表
     */
    List<ChatMessage> selectLatestMessagesBySessionId(@Param("sessionId") String sessionId, @Param("limit") Integer limit);

    /**
     * 查询指定时间之后的消息
     *
     * @param sessionId 会话ID
     * @param afterTime 时间点
     * @return 消息列表
     */
    List<ChatMessage> selectMessagesAfterTime(@Param("sessionId") String sessionId, @Param("afterTime") LocalDateTime afterTime);

    /**
     * 查询未读消息数量
     *
     * @param sessionId 会话ID
     * @param userId 用户ID
     * @param lastReadTime 最后阅读时间
     * @return 未读消息数量
     */
    Long countUnreadMessages(@Param("sessionId") String sessionId, @Param("userId") Long userId, @Param("lastReadTime") LocalDateTime lastReadTime);

    /**
     * 查询会话最后一条消息
     *
     * @param sessionId 会话ID
     * @return 最后一条消息
     */
    ChatMessage selectLastMessageBySessionId(@Param("sessionId") String sessionId);

    /**
     * 批量插入消息
     *
     * @param messages 消息列表
     * @return 插入数量
     */
    int insertBatchMessages(@Param("messages") List<ChatMessage> messages);
}