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
import com.ruoyi.common.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 聊天控制器
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/chat")
public class ChatController extends BaseController {

    @Autowired
    private IChatSessionService chatSessionService;

    @Autowired
    private IChatMessageService chatMessageService;

    /**
     * 创建私聊会话
     */
    @PostMapping("/session/private")
    @Log(title = "创建单聊会话", businessType = BusinessType.INSERT)
    public AjaxResult createPrivateSession(@RequestBody Map<String, Object> params) {
        Long userId1 = SecurityUtils.getUserId();
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
        Long creatorId = SecurityUtils.getUserId();
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
        Long userId = SecurityUtils.getUserId();
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
        Long userId = SecurityUtils.getUserId();
        boolean success = chatSessionService.joinSession(sessionId, userId);
        return success ? AjaxResult.success() : AjaxResult.error("加入会话失败");
    }

    /**
     * 离开会话
     */
    @PostMapping("/session/{sessionId}/leave")
    @Log(title = "离开会话", businessType = BusinessType.UPDATE)
    public AjaxResult leaveSession(@PathVariable String sessionId) {
        Long userId = SecurityUtils.getUserId();
        boolean success = chatSessionService.leaveSession(sessionId, userId);
        return success ? AjaxResult.success() : AjaxResult.error("离开会话失败");
    }

    /**
     * 发送消息
     */
    @PostMapping("/message/send")
    @Log(title = "发送消息", businessType = BusinessType.INSERT)
    public AjaxResult sendMessage(@RequestBody Map<String, Object> params) {
        String sessionId = params.get("sessionId").toString();
        Long fromUserId = SecurityUtils.getUserId();
        Integer messageType = Integer.valueOf(params.get("messageType").toString());
        String content = params.get("content").toString();
        String extraData = params.get("extraData") != null ? params.get("extraData").toString() : null;
        
        // 检查用户是否在会话中
        if (!chatSessionService.isUserInSession(sessionId, fromUserId)) {
            return AjaxResult.error("您不在此会话中");
        }
        
        ChatMessage message = chatMessageService.sendMessage(sessionId, fromUserId, messageType, content, extraData);
        return AjaxResult.success(message);
    }

    /**
     * 分页查询会话消息
     */
    @GetMapping("/session/{sessionId}/messages")
    public TableDataInfo getSessionMessages(@PathVariable String sessionId) {
        Long userId = SecurityUtils.getUserId();
        
        // 检查用户是否在会话中
        if (!chatSessionService.isUserInSession(sessionId, userId)) {
            return getDataTable(Collections.emptyList());
        }
        
        startPage();
        List<ChatMessage> list = chatMessageService.getSessionMessages(sessionId);
        return getDataTable(list);
    }

    /**
     * 获取最新消息
     */
    @GetMapping("/session/{sessionId}/messages/latest")
    public AjaxResult getLatestMessages(@PathVariable String sessionId, @RequestParam(defaultValue = "20") Integer limit) {
        Long userId = SecurityUtils.getUserId();
        
        // 检查用户是否在会话中
        if (!chatSessionService.isUserInSession(sessionId, userId)) {
            return AjaxResult.success(Collections.emptyList());
        }
        
        List<ChatMessage> messages = chatMessageService.getLatestMessages(sessionId, limit);
        return AjaxResult.success(messages);
    }

    /**
     * 标记消息为已读
     */
    @PostMapping("/message/{messageId}/read")
    @Log(title = "标记消息已读", businessType = BusinessType.UPDATE)
    public AjaxResult markMessageAsRead(@PathVariable String messageId) {
        Long userId = SecurityUtils.getUserId();
        chatMessageService.markMessageAsRead(messageId, userId);
        return AjaxResult.success();
    }

    /**
     * 批量标记消息为已读
     */
    @PostMapping("/session/{sessionId}/messages/read")
    @Log(title = "批量标记消息已读", businessType = BusinessType.UPDATE)
    public AjaxResult batchMarkMessagesAsRead(@PathVariable String sessionId, @RequestBody Map<String, Object> params) {
        Long userId = SecurityUtils.getUserId();
        @SuppressWarnings("unchecked")
        List<String> messageIds = (List<String>) params.get("messageIds");
        
        chatMessageService.batchMarkMessagesAsRead(sessionId, userId, messageIds);
        return AjaxResult.success();
    }

    /**
     * 撤回消息
     */
    @PostMapping("/message/{messageId}/recall")
    @Log(title = "撤回消息", businessType = BusinessType.UPDATE)
    public AjaxResult recallMessage(@PathVariable String messageId) {
        Long userId = SecurityUtils.getUserId();
        boolean success = chatMessageService.recallMessage(messageId, userId);
        return success ? AjaxResult.success() : AjaxResult.error("撤回消息失败");
    }

    /**
     * 获取消息详情
     */
    @GetMapping("/message/{messageId}")
    public AjaxResult getMessageDetail(@PathVariable String messageId) {
        ChatMessage message = chatMessageService.getMessageById(messageId);
        if (message == null) {
            return AjaxResult.error("消息不存在");
        }
        return AjaxResult.success(message);
    }
}
