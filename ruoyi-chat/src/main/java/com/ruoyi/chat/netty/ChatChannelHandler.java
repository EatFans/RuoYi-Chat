package com.ruoyi.chat.netty;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.chat.protocol.ChatMessage;
import com.ruoyi.chat.protocol.MessageType;
import com.ruoyi.chat.service.IChatMessageService;
import com.ruoyi.common.utils.StringUtils;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;

/**
 * 聊天消息处理器
 *
 * @author Fan
 */
@Component
@ChannelHandler.Sharable
public class ChatChannelHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    private static final Logger logger = LoggerFactory.getLogger(ChatChannelHandler.class);

    private static final AttributeKey<Long> USER_ID_KEY = AttributeKey.valueOf("userId");
    private static final AttributeKey<Boolean> AUTH_KEY = AttributeKey.valueOf("authenticated");

    @Autowired
    private ChatConnectionManager connectionManager;

    @Autowired
    private IChatMessageService chatMessageService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.info("WebSocket连接建立：{}", ctx.channel().id().asShortText());
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Long userId = ctx.channel().attr(USER_ID_KEY).get();
        if (userId != null) {
            // 使用channel移除连接，避免移除用户的新连接
            connectionManager.removeConnection(ctx.channel());
            // 发送用户下线通知
            broadcastUserStatus(userId, MessageType.USER_OFFLINE);
        }
        logger.info("WebSocket连接断开：{}", ctx.channel().id().asShortText());
        super.channelInactive(ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame frame) throws Exception {
        try {
            String text = frame.text();
            logger.debug("收到WebSocket消息：{}", text);

            ChatMessage message = objectMapper.readValue(text, ChatMessage.class);
            handleMessage(ctx, message);
        } catch (Exception e) {
            logger.error("处理WebSocket消息异常", e);
            sendErrorMessage(ctx, "消息格式错误");
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.READER_IDLE) {
                logger.info("连接读超时，关闭连接：{}", ctx.channel().id().asShortText());
                ctx.close();
            }
        }
        super.userEventTriggered(ctx, evt);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("WebSocket连接异常：{}", ctx.channel().id().asShortText(), cause);
        ctx.close();
    }

    /**
     * 处理消息
     */
    private void handleMessage(ChannelHandlerContext ctx, ChatMessage message) {
        MessageType type = message.getType();
        
        switch (type) {
            case AUTH:
                handleAuth(ctx, message);
                break;
            case HEARTBEAT:
                handleHeartbeat(ctx, message);
                break;
            case PRIVATE_CHAT:
                handlePrivateChat(ctx, message);
                break;
            case GROUP_CHAT:
                handleGroupChat(ctx, message);
                break;
            case MESSAGE_READ:
                handleMessageRead(ctx, message);
                break;
            default:
                logger.warn("未知消息类型：{}", type);
                break;
        }
    }

    /**
     * 处理认证
     */
    private void handleAuth(ChannelHandlerContext ctx, ChatMessage message) {
        try {
            // 这里应该验证token，获取用户信息
            // 简化处理，直接从消息中获取用户ID
            Long userId = message.getFromUserId();
            if (userId != null && userId > 0) {
                ctx.channel().attr(USER_ID_KEY).set(userId);
                ctx.channel().attr(AUTH_KEY).set(true);
                connectionManager.addConnection(userId, ctx.channel());
                
                // 发送认证成功消息
                ChatMessage response = new ChatMessage(MessageType.AUTH_SUCCESS);
                response.setMessageId(UUID.randomUUID().toString());
                sendMessage(ctx, response);
                
                // 发送用户上线通知
                broadcastUserStatus(userId, MessageType.USER_ONLINE);
                
                logger.info("用户 {} 认证成功", userId);
            } else {
                sendAuthFailedMessage(ctx, "用户ID无效");
            }
        } catch (Exception e) {
            logger.error("处理认证消息异常", e);
            sendAuthFailedMessage(ctx, "认证失败");
        }
    }

    /**
     * 处理心跳
     */
    private void handleHeartbeat(ChannelHandlerContext ctx, ChatMessage message) {
        ChatMessage response = new ChatMessage(MessageType.HEARTBEAT_RESPONSE);
        response.setMessageId(UUID.randomUUID().toString());
        sendMessage(ctx, response);
    }

    /**
     * 处理私聊消息
     */
    private void handlePrivateChat(ChannelHandlerContext ctx, ChatMessage message) {
        if (!isAuthenticated(ctx)) {
            sendErrorMessage(ctx, "未认证");
            return;
        }

        Long fromUserId = ctx.channel().attr(USER_ID_KEY).get();
        Long toUserId = message.getToUserId();
        
        if (toUserId == null) {
            sendErrorMessage(ctx, "接收者ID不能为空");
            return;
        }

        // 设置消息ID和发送者
        message.setMessageId(UUID.randomUUID().toString());
        message.setFromUserId(fromUserId);

        // 保存消息到数据库
        chatMessageService.savePrivateMessage(message);

        // 发送给接收者
        io.netty.channel.Channel toChannel = connectionManager.getUserChannel(toUserId);
        if (toChannel != null && toChannel.isActive()) {
            sendMessage(toChannel, message);
        }
        
        // 发送确认消息给发送者（让发送者也能看到自己发送的消息）
        sendMessage(ctx, message);

        logger.info("私聊消息：{} -> {}", fromUserId, toUserId);
    }

    /**
     * 处理群聊消息
     */
    private void handleGroupChat(ChannelHandlerContext ctx, ChatMessage message) {
        if (!isAuthenticated(ctx)) {
            sendErrorMessage(ctx, "未认证");
            return;
        }

        Long fromUserId = ctx.channel().attr(USER_ID_KEY).get();
        String sessionId = message.getSessionId();
        
        if (StringUtils.isEmpty(sessionId)) {
            sendErrorMessage(ctx, "会话ID不能为空");
            return;
        }

        // 设置消息ID和发送者
        message.setMessageId(UUID.randomUUID().toString());
        message.setFromUserId(fromUserId);

        // 保存消息到数据库
        chatMessageService.saveGroupMessage(message);

        // 发送给群聊成员
        Set<Long> members = connectionManager.getSessionMembers(sessionId);
        for (Long memberId : members) {
            if (!memberId.equals(fromUserId)) { // 不发送给自己
                io.netty.channel.Channel memberChannel = connectionManager.getUserChannel(memberId);
                if (memberChannel != null && memberChannel.isActive()) {
                    sendMessage(memberChannel, message);
                }
            }
        }

        logger.info("群聊消息：{} -> 会话 {}", fromUserId, sessionId);
    }

    /**
     * 处理消息已读
     */
    private void handleMessageRead(ChannelHandlerContext ctx, ChatMessage message) {
        if (!isAuthenticated(ctx)) {
            sendErrorMessage(ctx, "未认证");
            return;
        }

        Long userId = ctx.channel().attr(USER_ID_KEY).get();
        // 更新消息已读状态
        chatMessageService.markMessageAsRead(message.getMessageId(), userId);
        
        logger.info("消息已读：用户 {} 消息 {}", userId, message.getMessageId());
    }

    /**
     * 检查是否已认证
     */
    private boolean isAuthenticated(ChannelHandlerContext ctx) {
        Boolean authenticated = ctx.channel().attr(AUTH_KEY).get();
        return authenticated != null && authenticated;
    }

    /**
     * 发送消息
     */
    private void sendMessage(ChannelHandlerContext ctx, ChatMessage message) {
        sendMessage(ctx.channel(), message);
    }

    /**
     * 发送消息
     */
    private void sendMessage(io.netty.channel.Channel channel, ChatMessage message) {
        try {
            String json = objectMapper.writeValueAsString(message);
            channel.writeAndFlush(new TextWebSocketFrame(json));
        } catch (Exception e) {
            logger.error("发送消息异常", e);
        }
    }

    /**
     * 发送错误消息
     */
    private void sendErrorMessage(ChannelHandlerContext ctx, String error) {
        ChatMessage message = new ChatMessage(MessageType.ERROR);
        message.setContent(error);
        message.setMessageId(UUID.randomUUID().toString());
        sendMessage(ctx, message);
    }

    /**
     * 发送认证失败消息
     */
    private void sendAuthFailedMessage(ChannelHandlerContext ctx, String reason) {
        ChatMessage message = new ChatMessage(MessageType.AUTH_FAILED);
        message.setContent(reason);
        message.setMessageId(UUID.randomUUID().toString());
        sendMessage(ctx, message);
    }

    /**
     * 广播用户状态
     */
    private void broadcastUserStatus(Long userId, MessageType statusType) {
        ChatMessage message = new ChatMessage(statusType);
        message.setFromUserId(userId);
        message.setMessageId(UUID.randomUUID().toString());
        
        // 发送给所有在线用户
        for (Long onlineUserId : connectionManager.getOnlineUserIds()) {
            if (!onlineUserId.equals(userId)) {
                io.netty.channel.Channel channel = connectionManager.getUserChannel(onlineUserId);
                if (channel != null && channel.isActive()) {
                    sendMessage(channel, message);
                }
            }
        }
    }
}
