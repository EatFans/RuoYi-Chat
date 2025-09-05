<p align="center">
	<img alt="logo" src="https://oscimg.oschina.net/oscnet/up-d3d0a9303e11d522a06cd263f3079027715.png">
</p>
<h1 align="center" style="margin: 30px 0 30px; font-weight: bold;">RuoYi-Chat</h1>
<h4 align="center">基于RuoYi-Vue框架扩展开发的实时聊天通讯模块功能</h4>
<p align="center">
	<!-- <a href="https://gitee.com/y_project/RuoYi-Vue/stargazers"><img src="https://gitee.com/y_project/RuoYi-Vue/badge/star.svg?theme=dark"></a> -->
	<a href="https://gitee.com/y_project/RuoYi-Vue"><img src="https://img.shields.io/badge/RuoYi-v3.9.0-brightgreen.svg"></a>
	<a href="https://gitee.com/y_project/RuoYi-Vue/blob/master/LICENSE"><img src="https://img.shields.io/github/license/mashape/apistatus.svg"></a>
</p>

## 项目简介

RuoYi-Chat 是基于若依(RuoYi-Vue)前后端分离框架扩展开发的实时聊天通讯模块，专为小程序、客服系统、内部沟通等场景提供便捷的实时聊天功能。该项目在若依基础上，新增了聊天模块的功能，同时保留了若依的原有功能，如用户管理、角色管理、菜单管理、参数配置等。

### 🚀 核心特性

* **基于RuoYi-Vue框架**：完全继承若依框架的用户权限体系和技术架构
* **实时通讯**：基于WebSocket实现的高性能实时消息推送
* **多场景支持**：支持单聊、群聊、客服会话等多种聊天场景
* **小程序友好**：提供完整的小程序端聊天解决方案
* **客服系统**：内置智能客服分配和会话管理功能
* **多媒体消息**：支持文本、图片、文件、表情等多种消息类型
* **移动端适配**：响应式设计，完美适配PC端和移动端

### 🛠 技术栈

* **前端**：Vue 2.6.12 + Element UI + WebSocket
* **后端**：Spring Boot + Spring WebSocket + Redis + MyBatis
* **数据库**：MySQL 5.7+
* **缓存**：Redis（消息缓存、在线用户管理）
* **认证**：JWT + Spring Security（复用RuoYi权限体系）

## 功能模块

### 📱 聊天功能
1. **实时消息**：基于WebSocket的实时消息收发
2. **消息类型**：文本、图片、文件、表情包等多媒体消息
3. **消息状态**：已发送、已送达、已读等状态管理

## 应用场景

### 🏢 企业内部沟通
- 部门内部交流
- 项目协作沟通
- 公司公告通知
- 在线会议讨论

### 🛒 电商客服系统
- 商品咨询服务
- 售后问题处理
- 订单状态查询
- 投诉建议收集

### 📱 小程序聊天
- 用户社交互动
- 商家客户服务
- 社区交流讨论
- 活动群组管理

### 🎓 在线教育
- 师生在线答疑
- 学习小组讨论
- 课程通知发布
- 作业提交反馈

## 快速开始

### 环境要求
- JDK 1.8+
- MySQL 5.7+
- Redis 3.0+
- Node.js 12+
- Maven 3.6+

### 安装部署

# 前端WebSocket集成指南

本文档介绍如何在前端项目中集成RuoYi-Chat聊天功能。

## 1. WebSocket连接

### 1.1 连接地址

```javascript
// WebSocket连接地址
const wsUrl = 'ws://localhost:9999/ws';

// 如果需要认证，可以在URL中添加token参数
const wsUrlWithAuth = 'ws://localhost:9999/ws?token=' + userToken;
```

### 1.2 建立连接

```javascript
class ChatWebSocket {
    constructor(url, options = {}) {
        this.url = url;
        this.ws = null;
        this.reconnectAttempts = 0;
        this.maxReconnectAttempts = options.maxReconnectAttempts || 5;
        this.reconnectInterval = options.reconnectInterval || 3000;
        this.heartbeatInterval = options.heartbeatInterval || 30000;
        this.heartbeatTimer = null;
        
        // 事件回调
        this.onOpen = options.onOpen || (() => {});
        this.onMessage = options.onMessage || (() => {});
        this.onClose = options.onClose || (() => {});
        this.onError = options.onError || (() => {});
        
        this.connect();
    }
    
    connect() {
        try {
            this.ws = new WebSocket(this.url);
            
            this.ws.onopen = (event) => {
                console.log('WebSocket连接已建立');
                this.reconnectAttempts = 0;
                this.startHeartbeat();
                this.onOpen(event);
            };
            
            this.ws.onmessage = (event) => {
                const message = JSON.parse(event.data);
                console.log('收到消息:', message);
                this.onMessage(message);
            };
            
            this.ws.onclose = (event) => {
                console.log('WebSocket连接已关闭');
                this.stopHeartbeat();
                this.onClose(event);
                this.handleReconnect();
            };
            
            this.ws.onerror = (event) => {
                console.error('WebSocket连接错误:', event);
                this.onError(event);
            };
        } catch (error) {
            console.error('WebSocket连接失败:', error);
            this.handleReconnect();
        }
    }
    
    handleReconnect() {
        if (this.reconnectAttempts < this.maxReconnectAttempts) {
            this.reconnectAttempts++;
            console.log(`尝试重连 (${this.reconnectAttempts}/${this.maxReconnectAttempts})`);
            setTimeout(() => {
                this.connect();
            }, this.reconnectInterval);
        } else {
            console.error('WebSocket重连失败，已达到最大重连次数');
        }
    }
    
    startHeartbeat() {
        this.heartbeatTimer = setInterval(() => {
            if (this.ws && this.ws.readyState === WebSocket.OPEN) {
                this.send({
                    type: 'HEARTBEAT',
                    timestamp: new Date().toISOString()
                });
            }
        }, this.heartbeatInterval);
    }
    
    stopHeartbeat() {
        if (this.heartbeatTimer) {
            clearInterval(this.heartbeatTimer);
            this.heartbeatTimer = null;
        }
    }
    
    send(message) {
        if (this.ws && this.ws.readyState === WebSocket.OPEN) {
            this.ws.send(JSON.stringify(message));
            return true;
        } else {
            console.warn('WebSocket连接未就绪，消息发送失败');
            return false;
        }
    }
    
    close() {
        this.stopHeartbeat();
        if (this.ws) {
            this.ws.close();
        }
    }
}
```

## 2. 消息协议

### 2.1 消息类型

```javascript
const MessageType = {
    AUTH: 'AUTH',                    // 认证消息
    AUTH_SUCCESS: 'AUTH_SUCCESS',    // 认证成功
    AUTH_FAILED: 'AUTH_FAILED',      // 认证失败
    PRIVATE_CHAT: 'PRIVATE_CHAT',    // 私聊消息
    GROUP_CHAT: 'GROUP_CHAT',        // 群聊消息
    HEARTBEAT: 'HEARTBEAT',          // 心跳消息
    HEARTBEAT_RESPONSE: 'HEARTBEAT_RESPONSE', // 心跳响应
    USER_ONLINE: 'USER_ONLINE',      // 用户上线
    USER_OFFLINE: 'USER_OFFLINE',    // 用户下线
    MESSAGE_READ: 'MESSAGE_READ',    // 消息已读
    SYSTEM_NOTICE: 'SYSTEM_NOTICE',  // 系统通知
    ERROR: 'ERROR'                   // 错误消息
};
```

### 2.2 消息格式

```javascript
// 基础消息格式
const baseMessage = {
    type: 'MESSAGE_TYPE',           // 消息类型
    messageId: 'unique-id',         // 消息ID
    timestamp: '2024-01-01T12:00:00', // 时间戳（ISO格式）
    fromUserId: 123,                // 发送者ID
    toUserId: 456,                  // 接收者ID（私聊时使用）
    sessionId: 'session-id',        // 会话ID
    content: 'message content',     // 消息内容
    contentType: 'TEXT',            // 内容类型
    extra: {}                       // 额外数据
};

// 认证消息
const authMessage = {
    type: 'AUTH',
    fromUserId: 123,                // 用户ID
    content: 'user-jwt-token'       // 认证token
};

// 私聊消息
const privateChatMessage = {
    type: 'PRIVATE_CHAT',
    messageId: 'msg-' + Date.now(),
    sessionId: 'session-id',        // 会话ID
    fromUserId: 123,                // 发送者ID
    toUserId: 456,                  // 接收者ID
    contentType: 'TEXT',            // 内容类型：TEXT, IMAGE, EMOJI, FILE
    content: 'Hello World',         // 消息内容
    timestamp: new Date().toISOString(),
    extra: {
        replyToMessageId: 'reply-msg-id' // 回复的消息ID（可选）
    }
};

// 群聊消息
const groupChatMessage = {
    type: 'GROUP_CHAT',
    messageId: 'msg-' + Date.now(),
    sessionId: 'group-session-id',  // 群聊会话ID
    fromUserId: 123,                // 发送者ID
    contentType: 'TEXT',            // 内容类型
    content: 'Hello Everyone',      // 消息内容
    timestamp: new Date().toISOString()
};
```

### 2.3 内容类型

```javascript
const ContentType = {
    TEXT: 'TEXT',       // 文本消息
    IMAGE: 'IMAGE',     // 图片消息
    EMOJI: 'EMOJI',     // 表情消息
    FILE: 'FILE'        // 文件消息
};
```

## 3. 使用示例

### 3.1 初始化聊天客户端

```javascript
// 创建聊天客户端
const chatClient = new ChatWebSocket('ws://localhost:9999/ws', {
    maxReconnectAttempts: 5,
    reconnectInterval: 3000,
    heartbeatInterval: 30000,
    
    onOpen: (event) => {
        console.log('聊天连接已建立');
        // 发送认证消息
        chatClient.send({
            type: 'AUTH',
            fromUserId: getCurrentUserId(),
            content: localStorage.getItem('token')
        });
    },
    
    onMessage: (message) => {
        handleIncomingMessage(message);
    },
    
    onClose: (event) => {
        console.log('聊天连接已断开');
        showConnectionStatus('disconnected');
    },
    
    onError: (event) => {
        console.error('聊天连接错误');
        showConnectionStatus('error');
    }
});
```

### 3.2 发送消息

```javascript
// 发送私聊文本消息
function sendPrivateTextMessage(sessionId, content, toUserId) {
    const message = {
        type: 'PRIVATE_CHAT',
        messageId: 'msg-' + Date.now() + '-' + Math.random().toString(36).substr(2, 9),
        sessionId: sessionId,
        fromUserId: getCurrentUserId(),
        toUserId: toUserId,
        contentType: 'TEXT',
        content: content,
        timestamp: new Date().toISOString()
    };
    
    return chatClient.send(message);
}

// 发送群聊文本消息
function sendGroupTextMessage(sessionId, content) {
    const message = {
        type: 'GROUP_CHAT',
        messageId: 'msg-' + Date.now() + '-' + Math.random().toString(36).substr(2, 9),
        sessionId: sessionId,
        fromUserId: getCurrentUserId(),
        contentType: 'TEXT',
        content: content,
        timestamp: new Date().toISOString()
    };
    
    return chatClient.send(message);
}

// 发送私聊图片消息
function sendPrivateImageMessage(sessionId, imageUrl, toUserId) {
    const message = {
        type: 'PRIVATE_CHAT',
        messageId: 'msg-' + Date.now() + '-' + Math.random().toString(36).substr(2, 9),
        sessionId: sessionId,
        fromUserId: getCurrentUserId(),
        toUserId: toUserId,
        contentType: 'IMAGE',
        content: imageUrl,
        timestamp: new Date().toISOString(),
        extra: {
            width: 800,
            height: 600,
            size: 1024000
        }
    };
    
    return chatClient.send(message);
}

// 发送表情消息
function sendEmojiMessage(sessionId, emojiCode, toUserId = null) {
    const message = {
        type: 'CHAT_MESSAGE',
        messageId: 'msg-' + Date.now() + '-' + Math.random().toString(36).substr(2, 9),
        sessionId: sessionId,
        fromUserId: getCurrentUserId(),
        toUserId: toUserId,
        contentType: 'EMOJI',
        content: emojiCode,
        timestamp: new Date().toISOString()
    };
    
    return chatClient.send(message);
}
```

### 3.3 处理接收消息

```javascript
function handleIncomingMessage(message) {
    switch (message.type) {
        case 'CHAT_MESSAGE':
            displayChatMessage(message);
            break;
            
        case 'SYSTEM_NOTIFICATION':
            displaySystemNotification(message);
            break;
            
        case 'SESSION_NOTIFICATION':
            handleSessionNotification(message);
            break;
            
        case 'USER_STATUS':
            updateUserStatus(message.fromUserId, message.content);
            break;
            
        case 'ERROR':
            handleErrorMessage(message);
            break;
            
        default:
            console.warn('未知消息类型:', message.type);
    }
}

function displayChatMessage(message) {
    const chatContainer = document.getElementById('chat-messages');
    const messageElement = createMessageElement(message);
    chatContainer.appendChild(messageElement);
    chatContainer.scrollTop = chatContainer.scrollHeight;
}

function createMessageElement(message) {
    const messageDiv = document.createElement('div');
    messageDiv.className = `message ${message.fromUserId === getCurrentUserId() ? 'sent' : 'received'}`;
    
    let contentHtml = '';
    switch (message.contentType) {
        case 'TEXT':
            contentHtml = `<p>${escapeHtml(message.content)}</p>`;
            break;
        case 'IMAGE':
            contentHtml = `<img src="${message.content}" alt="图片" class="message-image" />`;
            break;
        case 'EMOJI':
            contentHtml = `<span class="emoji">${message.content}</span>`;
            break;
        case 'FILE':
            contentHtml = `<a href="${message.content}" download class="file-link">📎 ${message.extra?.fileName || '文件'}</a>`;
            break;
    }
    
    messageDiv.innerHTML = `
        <div class="message-header">
            <span class="sender">${message.fromUserNickname || '用户' + message.fromUserId}</span>
            <span class="timestamp">${formatTimestamp(message.timestamp)}</span>
        </div>
        <div class="message-content">${contentHtml}</div>
    `;
    
    return messageDiv;
}
```

## 4. REST API集成

### 4.1 会话管理

```javascript
// 创建私聊会话
async function createPrivateSession(targetUserId) {
    const response = await fetch('/chat/session/private', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': 'Bearer ' + getToken()
        },
        body: JSON.stringify({
            targetUserId: targetUserId
        })
    });
    return response.json();
}

// 创建群聊会话
async function createGroupSession(sessionName, memberIds) {
    const response = await fetch('/chat/session/group', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': 'Bearer ' + getToken()
        },
        body: JSON.stringify({
            sessionName: sessionName,
            memberIds: memberIds
        })
    });
    return response.json();
}

// 获取用户会话列表
async function getUserSessions() {
    const response = await fetch('/chat/session/list', {
        headers: {
            'Authorization': 'Bearer ' + getToken()
        }
    });
    return response.json();
}
```

### 4.2 消息管理

```javascript
// 获取会话消息
async function getSessionMessages(sessionId, pageNum = 1, pageSize = 20) {
    const response = await fetch(`/chat/message/list?sessionId=${sessionId}&pageNum=${pageNum}&pageSize=${pageSize}`, {
        headers: {
            'Authorization': 'Bearer ' + getToken()
        }
    });
    return response.json();
}

// 标记消息为已读
async function markMessageAsRead(messageId) {
    const response = await fetch('/chat/message/read', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': 'Bearer ' + getToken()
        },
        body: JSON.stringify({
            messageId: messageId
        })
    });
    return response.json();
}

// 撤回消息
async function recallMessage(messageId) {
    const response = await fetch(`/chat/message/recall/${messageId}`, {
        method: 'POST',
        headers: {
            'Authorization': 'Bearer ' + getToken()
        }
    });
    return response.json();
}
```

## 5. 工具函数

```javascript
// 获取当前用户ID
function getCurrentUserId() {
    // 从token或localStorage中获取用户ID
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}');
    return userInfo.userId;
}

// 获取认证token
function getToken() {
    return localStorage.getItem('token');
}

// HTML转义
function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

// 格式化时间戳
function formatTimestamp(timestamp) {
    const date = new Date(timestamp);
    const now = new Date();
    const diff = now - date;
    
    if (diff < 60000) { // 1分钟内
        return '刚刚';
    } else if (diff < 3600000) { // 1小时内
        return Math.floor(diff / 60000) + '分钟前';
    } else if (diff < 86400000) { // 24小时内
        return Math.floor(diff / 3600000) + '小时前';
    } else {
        return date.toLocaleDateString() + ' ' + date.toLocaleTimeString();
    }
}

// 显示连接状态
function showConnectionStatus(status) {
    const statusElement = document.getElementById('connection-status');
    if (statusElement) {
        statusElement.textContent = status === 'connected' ? '已连接' : 
                                   status === 'disconnected' ? '已断开' : '连接错误';
        statusElement.className = `status ${status}`;
    }
}
```

## 6. CSS样式示例

```css
/* 聊天容器 */
.chat-container {
    display: flex;
    flex-direction: column;
    height: 500px;
    border: 1px solid #ddd;
    border-radius: 8px;
    overflow: hidden;
}

/* 消息列表 */
.chat-messages {
    flex: 1;
    padding: 16px;
    overflow-y: auto;
    background-color: #f5f5f5;
}

/* 消息样式 */
.message {
    margin-bottom: 16px;
    max-width: 70%;
}

.message.sent {
    margin-left: auto;
    text-align: right;
}

.message.received {
    margin-right: auto;
    text-align: left;
}

.message-header {
    font-size: 12px;
    color: #666;
    margin-bottom: 4px;
}

.message-content {
    background-color: white;
    padding: 8px 12px;
    border-radius: 8px;
    box-shadow: 0 1px 2px rgba(0,0,0,0.1);
}

.message.sent .message-content {
    background-color: #007bff;
    color: white;
}

/* 图片消息 */
.message-image {
    max-width: 200px;
    max-height: 200px;
    border-radius: 4px;
}

/* 表情消息 */
.emoji {
    font-size: 24px;
}

/* 文件链接 */
.file-link {
    color: #007bff;
    text-decoration: none;
}

/* 输入区域 */
.chat-input {
    display: flex;
    padding: 16px;
    border-top: 1px solid #ddd;
    background-color: white;
}

.chat-input input {
    flex: 1;
    padding: 8px 12px;
    border: 1px solid #ddd;
    border-radius: 4px;
    margin-right: 8px;
}

.chat-input button {
    padding: 8px 16px;
    background-color: #007bff;
    color: white;
    border: none;
    border-radius: 4px;
    cursor: pointer;
}

/* 连接状态 */
.status {
    padding: 4px 8px;
    border-radius: 4px;
    font-size: 12px;
}

.status.connected {
    background-color: #d4edda;
    color: #155724;
}

.status.disconnected {
    background-color: #f8d7da;
    color: #721c24;
}

.status.error {
    background-color: #fff3cd;
    color: #856404;
}
```

## 7. 完整示例

```html
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>RuoYi聊天示例</title>
    <style>
        /* 这里包含上面的CSS样式 */
    </style>
</head>
<body>
    <div class="chat-container">
        <div class="chat-header">
            <h3>聊天室</h3>
            <span id="connection-status" class="status disconnected">未连接</span>
        </div>
        
        <div id="chat-messages" class="chat-messages">
            <!-- 消息将在这里显示 -->
        </div>
        
        <div class="chat-input">
            <input type="text" id="message-input" placeholder="输入消息..." />
            <button onclick="sendMessage()">发送</button>
        </div>
    </div>

    <script>
        // 这里包含上面的JavaScript代码
        
        // 页面加载完成后初始化
        document.addEventListener('DOMContentLoaded', function() {
            // 模拟用户信息
            localStorage.setItem('userInfo', JSON.stringify({ userId: 123 }));
            localStorage.setItem('token', 'mock-jwt-token');
            
            // 初始化聊天客户端
            window.chatClient = new ChatWebSocket('ws://localhost:9999/ws', {
                onOpen: () => showConnectionStatus('connected'),
                onMessage: handleIncomingMessage,
                onClose: () => showConnectionStatus('disconnected'),
                onError: () => showConnectionStatus('error')
            });
            
            // 绑定回车发送
            document.getElementById('message-input').addEventListener('keypress', function(e) {
                if (e.key === 'Enter') {
                    sendMessage();
                }
            });
        });
        
        function sendMessage() {
            const input = document.getElementById('message-input');
            const content = input.value.trim();
            
            if (content && window.chatClient) {
                sendTextMessage('default-session', content);
                input.value = '';
            }
        }
    </script>
</body>
</html>
```

## 8. 注意事项

1. **认证**: 确保在建立WebSocket连接后立即发送认证消息
2. **重连机制**: 实现自动重连机制以处理网络中断
3. **心跳检测**: 定期发送心跳消息保持连接活跃
4. **错误处理**: 妥善处理各种错误情况
5. **消息去重**: 使用messageId避免重复消息
6. **性能优化**: 对于大量消息，考虑虚拟滚动等优化方案
7. **安全性**: 对用户输入进行适当的转义和验证

## 9. 故障排除

### 9.1 连接问题
- 检查WebSocket服务器是否启动（端口9999）
- 确认防火墙设置
- 验证URL格式是否正确

### 9.2 认证问题
- 检查token是否有效
- 确认用户ID是否正确
- 验证后端认证逻辑

### 9.3 消息问题
- 检查消息格式是否符合协议
- 确认会话ID是否存在
- 验证用户权限

更多问题请参考项目文档或提交Issue。

## 开发指南

### API接口

```javascript
// 发送消息
POST /chat/message/send
{
  "roomId": 1,
  "content": "Hello World",
  "messageType": 1
}

// 获取聊天记录
GET /chat/message/history?roomId=1&pageNum=1&pageSize=20

// 创建聊天室
POST /chat/room/create
{
  "roomName": "技术讨论群",
  "roomType": 2,
  "memberIds": [1, 2, 3]
}
```

### WebSocket连接

```javascript
// 建立WebSocket连接
const ws = new WebSocket('ws://localhost:8080/websocket/chat?token=' + token);

// 消息处理
ws.onmessage = function(event) {
  const message = JSON.parse(event.data);
  // 处理接收到的消息
};

// 发送消息
ws.send(JSON.stringify({
  type: 'CHAT_MESSAGE',
  data: {
    roomId: 1,
    content: 'Hello'
  }
}));
```

## 贡献指南

欢迎提交 Issue 和 Pull Request 来完善项目。

1. Fork 本项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建 Pull Request

## 许可证

本项目基于 MIT 许可证开源，详情请参阅 [LICENSE](LICENSE) 文件。

## 致谢

感谢 RuoYi 项目提供的优秀基础框架。

## 联系我们

- 项目地址：https://github.com/EatFans/RuoYi-Chat
- 问题反馈：https://github.com/EatFans/RuoYi-Chat/issues
- 技术交流QQ群：待建立

---

⭐ 如果这个项目对您有帮助，请给我们一个 Star！