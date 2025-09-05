package com.ruoyi.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.chat.domain.entity.ChatMessageRead;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 消息阅读状态Mapper接口
 *
 * @author ruoyi
 * @date 2024-01-01
 */
@Mapper
public interface ChatMessageReadMapper extends BaseMapper<ChatMessageRead> {

    /**
     * 根据消息ID查询阅读状态列表
     *
     * @param messageId 消息ID
     * @return 阅读状态列表
     */
    List<ChatMessageRead> selectReadStatusByMessageId(@Param("messageId") String messageId);

    /**
     * 根据会话ID和用户ID查询阅读状态
     *
     * @param sessionId 会话ID
     * @param userId 用户ID
     * @return 阅读状态列表
     */
    List<ChatMessageRead> selectReadStatusBySessionIdAndUserId(@Param("sessionId") String sessionId, @Param("userId") Long userId);

    /**
     * 批量插入阅读状态
     *
     * @param readStatuses 阅读状态列表
     * @return 插入数量
     */
    int insertBatchReadStatus(@Param("readStatuses") List<ChatMessageRead> readStatuses);

    /**
     * 查询消息已读用户数量
     *
     * @param messageId 消息ID
     * @return 已读用户数量
     */
    Long countReadUsersByMessageId(@Param("messageId") String messageId);

    /**
     * 检查用户是否已读消息
     *
     * @param messageId 消息ID
     * @param userId 用户ID
     * @return 是否已读
     */
    boolean checkUserReadMessage(@Param("messageId") String messageId, @Param("userId") Long userId);
}