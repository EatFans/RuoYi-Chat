package com.ruoyi.chat.netty.handler;

import com.alibaba.fastjson2.JSON;
import com.ruoyi.chat.protocol.ChatMessage;
import com.ruoyi.chat.protocol.MessageType;
import com.ruoyi.chat.netty.manager.ConnectionManager;
import com.ruoyi.chat.netty.router.MessageRouter;
import com.ruoyi.chat.netty.session.UserSession;
import com.ruoyi.chat.service.IChatMessageService;
import com.ruoyi.chat.service.IChatSessionService;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * WebSocket消息处理器
 *
 * @author ruoyi
 */
@Component
public class WebSocketHandler extends SimpleChannelInboundHandler<WebSocketFrame> {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketHandler.class);

    @Autowired
    private ConnectionManager connectionManager;

    @Autowired
    private MessageRouter messageRouter;

    @Autowired
    private IChatMessageService chatMessageService;

    @Autowired
    private IChatSessionService chatSessionService;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.debug("WebSocket连接建立: {}", ctx.channel().id());
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.debug("WebSocket连接断开: {}", ctx.channel().id());
        
        // 获取用户会话信息
        UserSession userSession = connectionManager.getUserSessionByChannel(ctx.channel().id());
        if (userSession != null) {
            // 发送用户下线通知
            messageRouter.sendUserStatusNotification(userSession.getUserId(), "offline");
        }
        
        // 移除连接
        connectionManager.removeConnection(ctx.channel().id());
        super.channelInactive(ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) throws Exception {
        if (frame instanceof TextWebSocketFrame) {
            handleTextFrame(ctx, (TextWebSocketFrame) frame);
        } else {
            logger.warn("不支持的WebSocket帧类型: {}", frame.getClass().getSimpleName());
        }
    }

    /**
     * 处理文本消息帧
     */
    private void handleTextFrame(ChannelHandlerContext ctx, TextWebSocketFrame frame) {
        try {
            String text = frame.text();
            logger.debug("收到WebSocket消息: {}", text);
            
            // 解析消息
            ChatMessage message = JSON.parseObject(text, ChatMessage.class);
            if (message == null) {
                logger.warn("无法解析消息: {}", text);
                return;
            }
            
            // 处理不同类型的消息
            switch (message.getType()) {
                case AUTH:
                    handleAuthMessage(ctx, message);
                    break;
                case PRIVATE_CHAT:
                case GROUP_CHAT:
                    handleChatMessage(ctx, message);
                    break;
                case HEARTBEAT:
                    handleHeartbeat(ctx, message);
                    break;
                default:
                    logger.warn("未知的消息类型: {}", message.getType());
            }
        } catch (Exception e) {
            logger.error("处理WebSocket消息失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 处理认证消息
     */
    private void handleAuthMessage(ChannelHandlerContext ctx, ChatMessage message) {
        try {
            Long userId = message.getFromUserId();
            String userNickname = message.getFromUserNickname();
            String userAvatar = message.getFromUserAvatar();
            
            if (userId == null || userNickname == null) {
                logger.warn("认证消息缺少必要信息: userId={}, nickname={}", userId, userNickname);
                sendErrorResponse(ctx, "认证失败：缺少用户信息");
                return;
            }
            
            // 添加连接
            connectionManager.addConnection(userId, ctx.channel(), userNickname, userAvatar);
            
            // 发送认证成功响应
            ChatMessage authResponse = new ChatMessage();
            authResponse.setType(MessageType.AUTH_SUCCESS);
            authResponse.setContent("认证成功");
            authResponse.setTimestamp(java.time.LocalDateTime.now());
            
            ctx.channel().writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(authResponse)));
            
            // 发送用户上线通知
            messageRouter.sendUserStatusNotification(userId, "online");
            
            logger.info("用户认证成功: {} ({})", userNickname, userId);
        } catch (Exception e) {
            logger.error("处理认证消息失败: {}", e.getMessage(), e);
            sendErrorResponse(ctx, "认证失败");
        }
    }

    /**
     * 处理聊天消息
     */
    private void handleChatMessage(ChannelHandlerContext ctx, ChatMessage message) {
        try {
            // 验证用户是否已认证
            UserSession userSession = connectionManager.getUserSessionByChannel(ctx.channel().id());
            if (userSession == null) {
                logger.warn("未认证用户尝试发送消息");
                sendErrorResponse(ctx, "请先进行身份认证");
                return;
            }
            
            // 验证用户是否在会话中
            String sessionId = message.getSessionId();
            Long userId = userSession.getUserId();
            
            if (!chatSessionService.isUserInSession(sessionId, userId)) {
                logger.warn("用户 {} 不在会话 {} 中", userId, sessionId);
                sendErrorResponse(ctx, "您不在此会话中");
                return;
            }
            
            // 设置消息发送者信息
            message.setFromUserId(userId);
            message.setTimestamp(java.time.LocalDateTime.now());
            
            // 生成消息ID
            if (message.getMessageId() == null) {
                message.setMessageId("msg_" + System.currentTimeMillis() + "_" + userId);
            }
            
            // 保存消息到数据库
            chatMessageService.sendMessage(
                sessionId, 
                userId, 
                1, // 默认文本消息类型
                message.getContent(), 
                null
            );
            
            // 路由消息到其他用户
            messageRouter.routeMessage(message);
            
            // 发送消息确认
            ChatMessage ackMessage = new ChatMessage();
            ackMessage.setType(MessageType.AUTH_SUCCESS);
            ackMessage.setMessageId(message.getMessageId());
            ackMessage.setTimestamp(java.time.LocalDateTime.now());
            
            ctx.channel().writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(ackMessage)));
            
            logger.debug("消息处理完成: {}", message.getMessageId());
        } catch (Exception e) {
            logger.error("处理聊天消息失败: {}", e.getMessage(), e);
            sendErrorResponse(ctx, "消息发送失败");
        }
    }

    /**
     * 处理心跳消息
     */
    private void handleHeartbeat(ChannelHandlerContext ctx, ChatMessage message) {
        // 发送心跳响应
        ChatMessage heartbeatResponse = new ChatMessage();
        heartbeatResponse.setType(MessageType.HEARTBEAT_RESPONSE);
        heartbeatResponse.setTimestamp(java.time.LocalDateTime.now());
        
        ctx.channel().writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(heartbeatResponse)));
    }

    /**
     * 发送错误响应
     */
    private void sendErrorResponse(ChannelHandlerContext ctx, String errorMessage) {
        ChatMessage errorResponse = new ChatMessage();
        errorResponse.setType(MessageType.ERROR);
        errorResponse.setContent(errorMessage);
        errorResponse.setTimestamp(java.time.LocalDateTime.now());
        
        ctx.channel().writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(errorResponse)));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("WebSocket处理异常: {}", cause.getMessage(), cause);
        
        // 移除连接
        connectionManager.removeConnection(ctx.channel().id());
        
        // 关闭连接
        ctx.close();
    }
}