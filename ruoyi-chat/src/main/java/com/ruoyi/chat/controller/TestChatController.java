package com.ruoyi.chat.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.chat.domain.entity.ChatMessage;
import com.ruoyi.chat.domain.entity.ChatSession;
import com.ruoyi.chat.domain.entity.ChatSessionMember;
import com.ruoyi.chat.service.IChatMessageService;
import com.ruoyi.chat.service.IChatSessionService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 测试聊天控制器 - 无需JWT验证
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/test/chat")
public class TestChatController extends BaseController {

    @Autowired
    private IChatSessionService chatSessionService;

    @Autowired
    private IChatMessageService chatMessageService;

    // 测试用户ID
    private static final Long TEST_USER_ID = 1L;

    /**
     * 创建私聊会话
     */
    @PostMapping("/session/private")
    @Log(title = "创建单聊会话", businessType = BusinessType.INSERT)
    public AjaxResult createPrivateSession(@RequestBody Map<String, Object> params) {
        Long userId1 = TEST_USER_ID;
        Long userId2 = Long.valueOf(params.get("userId2").toString());
        
        ChatSession session = chatSessionService.getOrCreatePrivateSession(userId1, userId2);
        return AjaxResult.success(session);
    }

    /**
     * 创建群聊会话
     */
    @PostMapping("/session/group")
    @Log(title = "创建群聊会话", businessType = BusinessType.INSERT)
    public AjaxResult createGroupSession(@RequestBody Map<String, Object> params) {
        Long creatorId = TEST_USER_ID;
        String sessionName = params.get("sessionName").toString();
        @SuppressWarnings("unchecked")
        List<Long> memberIds = (List<Long>) params.get("memberIds");
        
        ChatSession session = chatSessionService.createGroupSession(creatorId, sessionName, memberIds);
        return AjaxResult.success(session);
    }

    /**
     * 获取用户会话列表
     */
    @GetMapping("/sessions")
    public AjaxResult getUserSessions() {
        Long userId = TEST_USER_ID;
        List<ChatSession> sessions = chatSessionService.getUserSessions(userId);
        return AjaxResult.success(sessions);
    }

    /**
     * 获取会话详情
     */
    @GetMapping("/session/{sessionId}")
    public AjaxResult getSessionDetail(@PathVariable String sessionId) {
        ChatSession session = chatSessionService.getSessionById(sessionId);
        if (session == null) {
            return AjaxResult.error("会话不存在");
        }
        return AjaxResult.success(session);
    }

    /**
     * 获取会话成员列表
     */
    @GetMapping("/session/{sessionId}/members")
    public AjaxResult getSessionMembers(@PathVariable String sessionId) {
        List<ChatSessionMember> members = chatSessionService.getSessionMembers(sessionId);
        return AjaxResult.success(members);
    }

    /**
     * 加入会话
     */
    @PostMapping("/session/{sessionId}/join")
    @Log(title = "加入会话", businessType = BusinessType.UPDATE)
    public AjaxResult joinSession(@PathVariable String sessionId) {
        Long userId = TEST_USER_ID;
        chatSessionService.joinSession(sessionId, userId);
        return AjaxResult.success();
    }

    /**
     * 离开会话
     */
    @PostMapping("/session/{sessionId}/leave")
    @Log(title = "离开会话", businessType = BusinessType.UPDATE)
    public AjaxResult leaveSession(@PathVariable String sessionId) {
        Long userId = TEST_USER_ID;
        chatSessionService.leaveSession(sessionId, userId);
        return AjaxResult.success();
    }

    /**
     * 发送消息
     */
    @PostMapping("/message/send")
    @Log(title = "发送消息", businessType = BusinessType.INSERT)
    public AjaxResult sendMessage(@RequestBody Map<String, Object> params) {
        Long senderId = TEST_USER_ID;
        String sessionId = params.get("sessionId").toString();
        String content = params.get("content").toString();
        String messageTypeStr = params.getOrDefault("messageType", "TEXT").toString();
        
        // 将消息类型转换为Integer
        Integer messageType;
        switch (messageTypeStr.toUpperCase()) {
            case "TEXT":
                messageType = ChatMessage.MessageType.TEXT;
                break;
            case "IMAGE":
                messageType = ChatMessage.MessageType.IMAGE;
                break;
            case "EMOJI":
                messageType = ChatMessage.MessageType.EMOJI;
                break;
            case "FILE":
                messageType = ChatMessage.MessageType.FILE;
                break;
            default:
                messageType = ChatMessage.MessageType.TEXT;
        }
        
        String extraData = params.get("extraData") != null ? params.get("extraData").toString() : null;
        
        // 修正参数顺序：sessionId, fromUserId, messageType, content, extraData
        ChatMessage message = chatMessageService.sendMessage(sessionId, senderId, messageType, content, extraData);
        if (message != null) {
            return AjaxResult.success(message);
        } else {
            return AjaxResult.error("发送消息失败");
        }
    }

    /**
     * 获取会话消息列表（分页）
     */
    @GetMapping("/session/{sessionId}/messages")
    public TableDataInfo getSessionMessages(@PathVariable String sessionId) {
        startPage();
        List<ChatMessage> messages = chatMessageService.getSessionMessages(sessionId);
        if (messages != null) {
            return getDataTable(messages);
        } else {
            return getDataTable(Collections.emptyList());
        }
    }

    /**
     * 获取最新消息
     */
    @GetMapping("/session/{sessionId}/messages/latest")
    public AjaxResult getLatestMessages(@PathVariable String sessionId, @RequestParam(defaultValue = "20") Integer limit) {
        List<ChatMessage> messages = chatMessageService.getLatestMessages(sessionId, limit);
        if (messages != null) {
            return AjaxResult.success(messages);
        } else {
            return AjaxResult.error("获取消息失败");
        }
    }

    /**
     * 标记消息已读
     */
    @PostMapping("/message/{messageId}/read")
    @Log(title = "标记消息已读", businessType = BusinessType.UPDATE)
    public AjaxResult markMessageAsRead(@PathVariable String messageId) {
        Long userId = TEST_USER_ID;
        chatMessageService.markMessageAsRead(messageId, userId);
        return AjaxResult.success();
    }

    /**
     * 批量标记消息已读
     */
    @PostMapping("/session/{sessionId}/messages/read")
    @Log(title = "批量标记消息已读", businessType = BusinessType.UPDATE)
    public AjaxResult batchMarkMessagesAsRead(@PathVariable String sessionId, @RequestBody Map<String, Object> params) {
        Long userId = TEST_USER_ID;
        @SuppressWarnings("unchecked")
        List<String> messageIds = (List<String>) params.get("messageIds");
        
        // 使用批量标记方法提高效率
        chatMessageService.batchMarkMessagesAsRead(sessionId, userId, messageIds);
        return AjaxResult.success();
    }

    /**
     * 撤回消息
     */
    @PostMapping("/message/{messageId}/recall")
    @Log(title = "撤回消息", businessType = BusinessType.UPDATE)
    public AjaxResult recallMessage(@PathVariable String messageId) {
        Long userId = TEST_USER_ID;
        boolean success = chatMessageService.recallMessage(messageId, userId);
        if (success) {
            return AjaxResult.success();
        } else {
            return AjaxResult.error("撤回消息失败");
        }
    }

    /**
     * 获取消息详情
     */
    @GetMapping("/message/{messageId}")
    public AjaxResult getMessageDetail(@PathVariable String messageId) {
        ChatMessage message = chatMessageService.getMessageById(messageId);
        return AjaxResult.success(message);
    }

    /**
     * 获取测试用户信息
     */
    @GetMapping("/user/info")
    public AjaxResult getTestUserInfo() {
        return AjaxResult.success().put("userId", TEST_USER_ID).put("username", "testuser");
    }
}