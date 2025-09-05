/**
 * 聊天WebSocket客户端
 * 基于RuoYi-Chat前端集成指南实现
 */
class ChatWebSocket {
    constructor(url, options = {}) {
        this.url = url
        this.ws = null
        this.reconnectAttempts = 0
        this.maxReconnectAttempts = options.maxReconnectAttempts || 5
        this.reconnectInterval = options.reconnectInterval || 3000
        this.heartbeatInterval = options.heartbeatInterval || 30000
        this.heartbeatTimer = null
        
        // 事件回调
        this.onOpen = options.onOpen || (() => {})
        this.onMessage = options.onMessage || (() => {})
        this.onClose = options.onClose || (() => {})
        this.onError = options.onError || (() => {})
        
        this.connect()
    }
    
    connect() {
        try {
            console.log('正在连接WebSocket:', this.url)
            this.ws = new WebSocket(this.url)
            
            this.ws.onopen = (event) => {
                console.log('WebSocket连接已建立')
                this.reconnectAttempts = 0
                this.startHeartbeat()
                this.onOpen(event)
            }
            
            this.ws.onmessage = (event) => {
                try {
                    const message = JSON.parse(event.data)
                    console.log('收到消息:', message)
                    this.onMessage(message)
                } catch (error) {
                    console.error('解析消息失败:', error, event.data)
                }
            }
            
            this.ws.onclose = (event) => {
                console.log('WebSocket连接已关闭', event)
                this.stopHeartbeat()
                this.onClose(event)
                this.handleReconnect()
            }
            
            this.ws.onerror = (event) => {
                console.error('WebSocket连接错误:', event)
                this.onError(event)
            }
        } catch (error) {
            console.error('WebSocket连接失败:', error)
            this.handleReconnect()
        }
    }
    
    handleReconnect() {
        if (this.reconnectAttempts < this.maxReconnectAttempts) {
            this.reconnectAttempts++
            console.log(`尝试重连 (${this.reconnectAttempts}/${this.maxReconnectAttempts})`)
            setTimeout(() => {
                this.connect()
            }, this.reconnectInterval)
        } else {
            console.error('WebSocket重连失败，已达到最大重连次数')
        }
    }
    
    startHeartbeat() {
        this.heartbeatTimer = setInterval(() => {
            if (this.ws && this.ws.readyState === WebSocket.OPEN) {
                this.send({
                    type: 'HEARTBEAT',
                    timestamp: new Date().toISOString()
                })
            }
        }, this.heartbeatInterval)
    }
    
    stopHeartbeat() {
        if (this.heartbeatTimer) {
            clearInterval(this.heartbeatTimer)
            this.heartbeatTimer = null
        }
    }
    
    send(message) {
        if (this.ws && this.ws.readyState === WebSocket.OPEN) {
            try {
                const jsonMessage = JSON.stringify(message)
                this.ws.send(jsonMessage)
                console.log('发送消息:', message)
                return true
            } catch (error) {
                console.error('发送消息失败:', error)
                return false
            }
        } else {
            console.warn('WebSocket连接未就绪，消息发送失败', {
                readyState: this.ws ? this.ws.readyState : 'null',
                message
            })
            return false
        }
    }
    
    close() {
        this.stopHeartbeat()
        if (this.ws) {
            this.ws.close()
            this.ws = null
        }
    }
    
    // 获取连接状态
    getReadyState() {
        if (!this.ws) return 'CLOSED'
        
        switch (this.ws.readyState) {
            case WebSocket.CONNECTING:
                return 'CONNECTING'
            case WebSocket.OPEN:
                return 'OPEN'
            case WebSocket.CLOSING:
                return 'CLOSING'
            case WebSocket.CLOSED:
                return 'CLOSED'
            default:
                return 'UNKNOWN'
        }
    }
    
    // 检查是否已连接
    isConnected() {
        return this.ws && this.ws.readyState === WebSocket.OPEN
    }
}

// 消息类型常量
export const MessageType = {
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
}

// 内容类型常量
export const ContentType = {
    TEXT: 'TEXT',       // 文本消息
    IMAGE: 'IMAGE',     // 图片消息
    EMOJI: 'EMOJI',     // 表情消息
    FILE: 'FILE'        // 文件消息
}

// 工具函数
export const ChatUtils = {
    // 获取当前用户ID - 测试环境
    getCurrentUserId() {
        return 1 // 与TestChatController中的TEST_USER_ID保持一致
    },
    
    // 获取认证token - 测试环境不需要真实token
    getToken() {
        return 'test-token' // 测试token
    },
    
    // HTML转义
    escapeHtml(text) {
        const div = document.createElement('div')
        div.textContent = text
        return div.innerHTML
    },
    
    // 格式化时间戳
    formatTimestamp(timestamp) {
        const date = new Date(timestamp)
        const now = new Date()
        const diff = now - date
        
        if (diff < 60000) { // 1分钟内
            return '刚刚'
        } else if (diff < 3600000) { // 1小时内
            return Math.floor(diff / 60000) + '分钟前'
        } else if (diff < 86400000) { // 24小时内
            return Math.floor(diff / 3600000) + '小时前'
        } else {
            return date.toLocaleDateString() + ' ' + date.toLocaleTimeString()
        }
    },
    
    // 生成消息ID
    generateMessageId() {
        return 'msg-' + Date.now() + '-' + Math.random().toString(36).substr(2, 9)
    },
    
    // 创建私聊消息
    createPrivateMessage(sessionId, fromUserId, toUserId, contentType, content, extra = null) {
        return {
            type: MessageType.PRIVATE_CHAT,
            messageId: this.generateMessageId(),
            sessionId,
            fromUserId,
            toUserId,
            contentType,
            content,
            timestamp: new Date().toISOString(),
            extra
        }
    },
    
    // 创建群聊消息
    createGroupMessage(sessionId, fromUserId, contentType, content, extra = null) {
        return {
            type: MessageType.GROUP_CHAT,
            messageId: this.generateMessageId(),
            sessionId,
            fromUserId,
            contentType,
            content,
            timestamp: new Date().toISOString(),
            extra
        }
    },
    
    // 创建认证消息
    createAuthMessage(userId, token) {
        return {
            type: MessageType.AUTH,
            fromUserId: userId,
            content: token
        }
    }
}

export default ChatWebSocket