<template>
  <div class="app">
    <div class="header">
      <h1>RuoYi聊天功能测试</h1>
      <div class="connection-status">
        <span :class="['status-indicator', connectionStatus]">
          {{ getStatusText() }}
        </span>
      </div>
    </div>

    <!-- 顶部用户切换栏 -->
    <div class="user-switcher">
      <div class="current-user">
        <img :src="currentUser.avatar" :alt="currentUser.nickname" class="user-avatar">
        <span class="user-name">{{ currentUser.nickname }}</span>
      </div>
      <div class="user-actions">
        <select v-model="selectedUserId" @change="switchUser" class="user-select">
          <option v-for="user in testUsers" :key="user.userId" :value="user.userId">
            {{ user.nickname }} (ID: {{ user.userId }})
          </option>
        </select>
        <button @click="refreshSessions" class="refresh-btn">刷新会话</button>
        <button @click="showCreateSessionModal = true" class="create-btn">创建会话</button>
      </div>
    </div>

    <div class="main-content">
      <!-- 左侧会话列表 -->
        <div class="session-list">
          <h3>会话列表 ({{ sessions.length }})</h3>
          <div class="session-item" 
               v-for="session in sessions" 
               :key="session.sessionId"
               :class="{ active: currentSessionId === session.sessionId }"
               @click="switchSession(session.sessionId)">
            <div class="session-avatar">
              {{ (session.sessionName || (session.sessionType === 1 ? '私聊' : '群聊')).charAt(0) }}
            </div>
            <div class="session-info">
              <div class="session-name">{{ session.sessionName || (session.sessionType === 1 ? '私聊' : '群聊') }}</div>
              <div class="session-last-message">{{ session.lastMessage }}</div>
            </div>
          </div>
        </div>

      <!-- 右侧聊天区域 -->
      <div class="chat-area">
        <div class="chat-header">
          <h3>{{ getCurrentSessionName() }}</h3>
          <div class="chat-actions">
            <button @click="clearMessages" class="btn-clear">清空消息</button>
            <button @click="reconnect" class="btn-reconnect">重新连接</button>
          </div>
        </div>

        <div class="chat-messages" ref="messagesContainer">
          <div v-for="message in messages" 
               :key="message.messageId" 
               :class="['message', message.fromUserId === currentUserId ? 'sent' : 'received']">
            <div class="message-header">
              <span class="sender">{{ message.fromUserNickname || '用户' + message.fromUserId }}</span>
              <span class="timestamp">{{ formatTimestamp(message.timestamp) }}</span>
            </div>
            <div class="message-content">
              <div v-if="message.contentType === 'TEXT'" class="text-content">
                {{ message.content }}
              </div>
              <div v-else-if="message.contentType === 'IMAGE'" class="image-content">
                <img :src="message.content" alt="图片" class="message-image" />
              </div>
              <div v-else-if="message.contentType === 'EMOJI'" class="emoji-content">
                <span class="emoji">{{ message.content }}</span>
              </div>
            </div>
          </div>
        </div>

        <div class="chat-input">
          <div class="input-toolbar">
            <button @click="sendTestImage" class="btn-tool">📷 图片</button>
            <button @click="sendTestEmoji" class="btn-tool">😀 表情</button>
            <select v-model="messageType" class="message-type-select">
              <option value="PRIVATE_CHAT">私聊</option>
              <option value="GROUP_CHAT">群聊</option>
            </select>
          </div>
          <div class="input-area">
            <input 
              type="text" 
              v-model="inputMessage" 
              @keypress="handleKeyPress"
              placeholder="输入消息..."
              class="message-input"
            />
            <button @click="sendMessage" class="btn-send">发送</button>
          </div>
        </div>
      </div>
    </div>

    <!-- 调试信息 -->
    <div class="debug-panel">
      <h4>调试信息</h4>
      <div class="debug-info">
        <p>WebSocket状态: {{ connectionStatus }}</p>
        <p>当前用户ID: {{ currentUserId }}</p>
        <p>当前会话ID: {{ currentSessionId }}</p>
        <p>消息总数: {{ messages.length }}</p>
      </div>
      <div class="debug-logs">
        <h5>日志:</h5>
        <div class="log-container">
          <div v-for="(log, index) in logs" :key="index" class="log-item">
            <span class="log-time">{{ log.time }}</span>
            <span :class="['log-level', log.level]">{{ log.level }}</span>
            <span class="log-message">{{ log.message }}</span>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 创建会话模态框 -->
    <div v-if="showCreateSessionModal" class="modal-overlay" @click="closeCreateSessionModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>创建新会话</h3>
          <button @click="closeCreateSessionModal" class="close-btn">×</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>会话类型:</label>
            <select v-model="newSession.type" class="form-control">
              <option value="private">私聊</option>
              <option value="group">群聊</option>
            </select>
          </div>
          <div v-if="newSession.type === 'private'" class="form-group">
            <label>选择用户:</label>
            <select v-model="newSession.targetUserId" class="form-control">
              <option value="">请选择用户</option>
              <option v-for="user in availableUsers" :key="user.userId" :value="user.userId">
                {{ user.nickname }} (ID: {{ user.userId }})
              </option>
            </select>
          </div>
          <div v-if="newSession.type === 'group'" class="form-group">
            <label>群聊名称:</label>
            <input v-model="newSession.name" type="text" class="form-control" placeholder="请输入群聊名称">
          </div>
          <div v-if="newSession.type === 'group'" class="form-group">
            <label>选择成员:</label>
            <div class="member-list">
              <label v-for="user in availableUsers" :key="user.userId" class="member-item">
                <input type="checkbox" :value="user.userId" v-model="newSession.memberIds">
                <img :src="user.avatar" :alt="user.nickname" class="member-avatar">
                <span>{{ user.nickname }}</span>
              </label>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button @click="closeCreateSessionModal" class="btn btn-secondary">取消</button>
          <button @click="createSession" class="btn btn-primary" :disabled="!canCreateSession">创建</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import ChatWebSocket, { MessageType, ContentType, ChatUtils } from './utils/ChatWebSocket.js'
import { TestUsers, TestSessions, ApiConfig } from './config/test-config.js'

export default {
  name: 'ChatApp',
  data() {
    return {
      chatClient: null,
      connectionStatus: 'disconnected',
      currentUserId: 123,
      selectedUserId: 123,
      currentUser: { userId: 123, nickname: '测试用户1', avatar: 'https://via.placeholder.com/40x40/007bff/white?text=测' },
      testUsers: [
        { userId: 123, nickname: '测试用户1', avatar: 'https://via.placeholder.com/40x40/007bff/white?text=测' },
        { userId: 456, nickname: '测试用户2', avatar: 'https://via.placeholder.com/40x40/28a745/white?text=用' },
        { userId: 789, nickname: '测试用户3', avatar: 'https://via.placeholder.com/40x40/dc3545/white?text=户' }
      ],
      availableUsers: [
        { userId: 456, nickname: '测试用户2', avatar: 'https://via.placeholder.com/40x40/28a745/white?text=用' },
        { userId: 789, nickname: '测试用户3', avatar: 'https://via.placeholder.com/40x40/dc3545/white?text=户' }
      ],
      currentSessionId: 'test-session-1',
      inputMessage: '',
      messageType: 'PRIVATE_CHAT',
      messages: [],
      logs: [],
      showCreateSessionModal: false,
      newSession: {
        type: 'private',
        name: '',
        targetUserId: '',
        memberIds: []
      },
      sessions: []
    }
  },
  mounted() {
    // 初始化用户认证信息
    this.initUserAuth()
    this.initChat()
    this.refreshSessions()
  },
  beforeUnmount() {
    if (this.chatClient) {
      this.chatClient.close()
    }
  },
  computed: {
    availableUsers() {
      return this.testUsers.filter(user => user.userId !== this.currentUser.userId)
    },
    canCreateSession() {
      if (this.newSession.type === 'private') {
        return this.newSession.targetUserId !== ''
      } else {
        return this.newSession.name.trim() !== '' && this.newSession.memberIds.length > 0
      }
    }
  },
  methods: {
    // 初始化用户认证信息
    initUserAuth() {
      // 设置测试用户信息到localStorage
      const userInfo = {
        userId: this.currentUserId,
        nickname: this.currentUser.nickname,
        avatar: this.currentUser.avatar
      }
      localStorage.setItem('userInfo', JSON.stringify(userInfo))
      
      // 设置测试token
      localStorage.setItem('token', 'test-token-123')
      
      this.addLog('info', `已设置用户认证信息: ${userInfo.nickname} (ID: ${userInfo.userId})`)
    },
    
    // 用户切换相关
    switchUser() {
      const user = this.testUsers.find(u => u.userId === this.selectedUserId)
      if (user) {
        this.currentUser = { ...user }
        this.currentUserId = user.userId
        this.addLog('info', `切换到用户: ${user.nickname} (ID: ${user.userId})`)
        
        // 重新连接WebSocket
        if (this.connectionStatus === 'connected') {
          this.chatClient.close()
          setTimeout(() => {
            this.initChat()
          }, 1000)
        }
        
        // 刷新会话列表
        this.refreshSessions()
      }
    },
    
    // 刷新会话列表
    async refreshSessions() {
      try {
        this.addLog('info', '正在刷新会话列表...')
        const response = await fetch(`${ApiConfig.baseUrl}${ApiConfig.endpoints.getUserSessions}`, {
          method: 'GET',
          headers: {
            'Content-Type': 'application/json'
          }
        })
        
        if (response.ok) {
          const result = await response.json()
          if (result.code === 200) {
            this.sessions = result.data || []
            this.addLog('success', `成功获取 ${this.sessions.length} 个会话`)
          } else {
            this.addLog('error', `获取会话失败: ${result.msg}`)
          }
        } else {
          this.addLog('error', `HTTP错误: ${response.status}`)
        }
      } catch (error) {
        this.addLog('error', `刷新会话失败: ${error.message}`)
        // 使用测试数据作为后备
        this.sessions = [...TestSessions]
      }
    },
    
    // 创建会话相关
    closeCreateSessionModal() {
      this.showCreateSessionModal = false
      this.newSession = {
        type: 'private',
        name: '',
        targetUserId: '',
        memberIds: []
      }
    },
    
    async createSession() {
      try {
        let endpoint, payload
        
        if (this.newSession.type === 'private') {
          endpoint = `${ApiConfig.baseUrl}${ApiConfig.endpoints.createPrivateSession}`
          payload = {
            userId2: this.newSession.targetUserId
          }
        } else {
          endpoint = `${ApiConfig.baseUrl}${ApiConfig.endpoints.createGroupSession}`
          payload = {
            sessionName: this.newSession.name,
            memberIds: [...this.newSession.memberIds, this.currentUser.userId]
          }
        }
        
        this.addLog('info', `正在创建${this.newSession.type === 'private' ? '私聊' : '群聊'}会话...`)
        
        const response = await fetch(endpoint, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify(payload)
        })
        
        if (response.ok) {
          const result = await response.json()
          if (result.code === 200) {
            this.addLog('success', '会话创建成功')
            this.closeCreateSessionModal()
            this.refreshSessions()
          } else {
            this.addLog('error', `创建会话失败: ${result.msg}`)
          }
        } else {
          this.addLog('error', `HTTP错误: ${response.status}`)
        }
      } catch (error) {
        this.addLog('error', `创建会话失败: ${error.message}`)
      }
    },
    
    initChat() {
      this.addLog('info', '初始化聊天客户端...')
      
      this.chatClient = new ChatWebSocket('ws://localhost:9999/ws', {
        maxReconnectAttempts: 5,
        reconnectInterval: 3000,
        heartbeatInterval: 30000,
        
        onOpen: (event) => {
          this.connectionStatus = 'connected'
          this.addLog('success', 'WebSocket连接已建立')
          
          // 发送认证消息
          this.chatClient.send({
            type: 'AUTH',
            fromUserId: this.currentUserId,
            content: localStorage.getItem('token')
          })
        },
        
        onMessage: (message) => {
          this.handleIncomingMessage(message)
        },
        
        onClose: (event) => {
          this.connectionStatus = 'disconnected'
          this.addLog('warning', 'WebSocket连接已断开')
        },
        
        onError: (event) => {
          this.connectionStatus = 'error'
          this.addLog('error', 'WebSocket连接错误')
        }
      })
    },
    
    handleIncomingMessage(message) {
      this.addLog('info', `收到消息: ${message.type}`)
      
      switch (message.type) {
        case 'AUTH_SUCCESS':
          this.addLog('success', '认证成功')
          break
          
        case 'AUTH_FAILED':
          this.addLog('error', '认证失败')
          break
          
        case 'PRIVATE_CHAT':
        case 'GROUP_CHAT':
          this.messages.push(message)
          this.scrollToBottom()
          break
          
        case 'HEARTBEAT_RESPONSE':
          this.addLog('info', '心跳响应')
          break
          
        case 'USER_ONLINE':
          this.addLog('info', `用户 ${message.fromUserId} 上线`)
          break
          
        case 'USER_OFFLINE':
          this.addLog('info', `用户 ${message.fromUserId} 下线`)
          break
          
        case 'ERROR':
          this.addLog('error', `服务器错误: ${message.content}`)
          break
          
        default:
          this.addLog('warning', `未知消息类型: ${message.type}`)
      }
    },
    
    sendMessage() {
      if (!this.inputMessage.trim()) return
      
      const message = {
        type: this.messageType,
        messageId: 'msg-' + Date.now() + '-' + Math.random().toString(36).substr(2, 9),
        sessionId: this.currentSessionId,
        fromUserId: this.currentUserId,
        contentType: 'TEXT',
        content: this.inputMessage.trim(),
        timestamp: new Date().toISOString()
      }
      
      if (this.messageType === 'PRIVATE_CHAT') {
        message.toUserId = 456 // 模拟接收者ID
      }
      
      if (this.chatClient && this.chatClient.send(message)) {
        // 添加到本地消息列表（模拟发送成功）
        this.messages.push({
          ...message,
          fromUserNickname: '我'
        })
        this.inputMessage = ''
        this.scrollToBottom()
        this.addLog('info', `发送消息: ${message.content}`)
      } else {
        this.addLog('error', '消息发送失败')
      }
    },
    
    sendTestImage() {
      const message = {
        type: this.messageType,
        messageId: 'msg-' + Date.now() + '-' + Math.random().toString(36).substr(2, 9),
        sessionId: this.currentSessionId,
        fromUserId: this.currentUserId,
        contentType: 'IMAGE',
        content: 'https://via.placeholder.com/200x150/4CAF50/white?text=Test+Image',
        timestamp: new Date().toISOString()
      }
      
      if (this.messageType === 'PRIVATE_CHAT') {
        message.toUserId = 456
      }
      
      if (this.chatClient && this.chatClient.send(message)) {
        this.messages.push({
          ...message,
          fromUserNickname: '我'
        })
        this.scrollToBottom()
        this.addLog('info', '发送测试图片')
      }
    },
    
    sendTestEmoji() {
      const emojis = ['😀', '😂', '🥰', '😎', '🤔', '👍', '❤️', '🎉']
      const randomEmoji = emojis[Math.floor(Math.random() * emojis.length)]
      
      const message = {
        type: this.messageType,
        messageId: 'msg-' + Date.now() + '-' + Math.random().toString(36).substr(2, 9),
        sessionId: this.currentSessionId,
        fromUserId: this.currentUserId,
        contentType: 'EMOJI',
        content: randomEmoji,
        timestamp: new Date().toISOString()
      }
      
      if (this.messageType === 'PRIVATE_CHAT') {
        message.toUserId = 456
      }
      
      if (this.chatClient && this.chatClient.send(message)) {
        this.messages.push({
          ...message,
          fromUserNickname: '我'
        })
        this.scrollToBottom()
        this.addLog('info', `发送表情: ${randomEmoji}`)
      }
    },
    
    handleKeyPress(event) {
      if (event.key === 'Enter') {
        this.sendMessage()
      }
    },
    
    async switchSession(sessionId) {
      this.currentSessionId = sessionId
      this.addLog('info', `切换到会话: ${sessionId}`)
      
      // 获取会话消息
      await this.loadSessionMessages(sessionId)
    },
    
    // 加载会话消息
    async loadSessionMessages(sessionId) {
      try {
        this.addLog('info', '正在加载会话消息...')
        const response = await fetch(`${ApiConfig.baseUrl}/chat/session/${sessionId}/messages/latest?limit=50`, {
          method: 'GET',
          headers: {
            'Authorization': `Bearer ${ChatUtils.getToken()}`,
            'Content-Type': 'application/json'
          }
        })
        
        if (response.ok) {
          const result = await response.json()
          if (result.code === 200) {
            this.messages = result.data || []
            this.addLog('success', `成功加载 ${this.messages.length} 条消息`)
            this.scrollToBottom()
          } else {
            this.addLog('error', `加载消息失败: ${result.msg}`)
          }
        } else {
          this.addLog('error', `HTTP错误: ${response.status}`)
        }
      } catch (error) {
        this.addLog('error', `加载消息失败: ${error.message}`)
        this.messages = []
      }
    },
    
    clearMessages() {
      this.messages = []
      this.addLog('info', '清空消息列表')
    },
    
    reconnect() {
      if (this.chatClient) {
        this.chatClient.close()
      }
      setTimeout(() => {
        this.initChat()
      }, 1000)
    },
    
    getCurrentSessionName() {
      const session = this.sessions.find(s => s.sessionId === this.currentSessionId)
      return session ? (session.sessionName || (session.sessionType === 1 ? '私聊' : '群聊')) : '未知会话'
    },
    
    getStatusText() {
      const statusMap = {
        connected: '已连接',
        disconnected: '已断开',
        connecting: '连接中',
        error: '连接错误'
      }
      return statusMap[this.connectionStatus] || '未知状态'
    },
    
    formatTimestamp(timestamp) {
      const date = new Date(timestamp)
      const now = new Date()
      const diff = now - date
      
      if (diff < 60000) {
        return '刚刚'
      } else if (diff < 3600000) {
        return Math.floor(diff / 60000) + '分钟前'
      } else if (diff < 86400000) {
        return Math.floor(diff / 3600000) + '小时前'
      } else {
        return date.toLocaleDateString() + ' ' + date.toLocaleTimeString()
      }
    },
    
    scrollToBottom() {
      this.$nextTick(() => {
        const container = this.$refs.messagesContainer
        if (container) {
          container.scrollTop = container.scrollHeight
        }
      })
    },
    
    addLog(level, message) {
      this.logs.unshift({
        time: new Date().toLocaleTimeString(),
        level,
        message
      })
      
      // 限制日志数量
      if (this.logs.length > 50) {
        this.logs = this.logs.slice(0, 50)
      }
    }
  }
}
</script>

<style scoped>
.app {
  height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  background: #2c3e50;
  color: white;
  padding: 1rem 2rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header h1 {
  margin: 0;
  font-size: 1.5rem;
}

.connection-status {
  display: flex;
  align-items: center;
}

.status-indicator {
  padding: 0.25rem 0.75rem;
  border-radius: 1rem;
  font-size: 0.875rem;
  font-weight: 500;
}

.status-indicator.connected {
  background: #27ae60;
  color: white;
}

.status-indicator.disconnected {
  background: #e74c3c;
  color: white;
}

.status-indicator.error {
  background: #f39c12;
  color: white;
}

/* 用户切换栏样式 */
.user-switcher {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-bottom: 1px solid #e0e0e0;
}

.current-user {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  border: 2px solid rgba(255,255,255,0.3);
}

.user-name {
  font-weight: 500;
  font-size: 14px;
}

.user-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-select {
  padding: 5px 10px;
  border: 1px solid rgba(255,255,255,0.3);
  border-radius: 4px;
  background: rgba(255,255,255,0.1);
  color: white;
  font-size: 12px;
}

.user-select option {
  background: #333;
  color: white;
}

.refresh-btn, .create-btn {
  padding: 5px 12px;
  border: 1px solid rgba(255,255,255,0.3);
  border-radius: 4px;
  background: rgba(255,255,255,0.1);
  color: white;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s;
}

.refresh-btn:hover, .create-btn:hover {
  background: rgba(255,255,255,0.2);
}

.main-content {
  flex: 1;
  display: flex;
  overflow: hidden;
}

.session-list {
  width: 300px;
  background: white;
  border-right: 1px solid #ddd;
  overflow-y: auto;
}

.session-list h3 {
  padding: 1rem;
  margin: 0;
  background: #f8f9fa;
  border-bottom: 1px solid #ddd;
}

.session-item {
  display: flex;
  align-items: center;
  padding: 1rem;
  border-bottom: 1px solid #eee;
  cursor: pointer;
  transition: background-color 0.2s;
}

.session-item:hover {
  background: #f8f9fa;
}

.session-item.active {
  background: #e3f2fd;
}

.session-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #3498db;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  margin-right: 0.75rem;
}

.session-info {
  flex: 1;
}

.session-name {
  font-weight: 500;
  margin-bottom: 0.25rem;
}

.session-last-message {
  font-size: 0.875rem;
  color: #666;
}

.chat-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: white;
}

.chat-header {
  padding: 1rem;
  border-bottom: 1px solid #ddd;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #f8f9fa;
}

.chat-header h3 {
  margin: 0;
}

.chat-actions {
  display: flex;
  gap: 0.5rem;
}

.btn-clear, .btn-reconnect {
  padding: 0.5rem 1rem;
  border: 1px solid #ddd;
  background: white;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.875rem;
}

.btn-clear:hover, .btn-reconnect:hover {
  background: #f8f9fa;
}

.chat-messages {
  flex: 1;
  padding: 1rem;
  overflow-y: auto;
  background: #f5f5f5;
}

.message {
  margin-bottom: 1rem;
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
  font-size: 0.75rem;
  color: #666;
  margin-bottom: 0.25rem;
}

.message-content {
  background: white;
  padding: 0.75rem 1rem;
  border-radius: 1rem;
  box-shadow: 0 1px 2px rgba(0,0,0,0.1);
}

.message.sent .message-content {
  background: #007bff;
  color: white;
}

.text-content {
  word-wrap: break-word;
}

.message-image {
  max-width: 200px;
  max-height: 200px;
  border-radius: 0.5rem;
}

.emoji-content .emoji {
  font-size: 2rem;
}

.chat-input {
  border-top: 1px solid #ddd;
  background: white;
}

.input-toolbar {
  padding: 0.5rem 1rem;
  border-bottom: 1px solid #eee;
  display: flex;
  gap: 0.5rem;
  align-items: center;
}

.btn-tool {
  padding: 0.25rem 0.5rem;
  border: 1px solid #ddd;
  background: white;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.875rem;
}

.btn-tool:hover {
  background: #f8f9fa;
}

.message-type-select {
  padding: 0.25rem 0.5rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 0.875rem;
}

.input-area {
  display: flex;
  padding: 1rem;
  gap: 0.5rem;
}

.message-input {
  flex: 1;
  padding: 0.75rem 1rem;
  border: 1px solid #ddd;
  border-radius: 1.5rem;
  outline: none;
  font-size: 1rem;
}

.message-input:focus {
  border-color: #007bff;
}

.btn-send {
  padding: 0.75rem 1.5rem;
  background: #007bff;
  color: white;
  border: none;
  border-radius: 1.5rem;
  cursor: pointer;
  font-size: 1rem;
  font-weight: 500;
}

.btn-send:hover {
  background: #0056b3;
}

.debug-panel {
  background: #2c3e50;
  color: white;
  padding: 1rem;
  max-height: 200px;
  overflow-y: auto;
}

.debug-panel h4, .debug-panel h5 {
  margin: 0 0 0.5rem 0;
}

.debug-info {
  margin-bottom: 1rem;
}

.debug-info p {
  margin: 0.25rem 0;
  font-size: 0.875rem;
}

.log-container {
  max-height: 100px;
  overflow-y: auto;
  font-family: monospace;
  font-size: 0.75rem;
}

.log-item {
  margin-bottom: 0.25rem;
  display: flex;
  gap: 0.5rem;
}

.log-time {
  color: #bdc3c7;
}

.log-level {
  font-weight: bold;
  min-width: 60px;
}

.log-level.info {
  color: #3498db;
}

.log-level.success {
  color: #27ae60;
}

.log-level.warning {
  color: #f39c12;
}

.log-level.error {
  color: #e74c3c;
}

.log-message {
  flex: 1;
}

/* 模态框样式 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 8px;
  width: 90%;
  max-width: 500px;
  max-height: 80vh;
  overflow-y: auto;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #eee;
}

.modal-header h3 {
  margin: 0;
  color: #333;
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #999;
  padding: 0;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.close-btn:hover {
  color: #666;
}

.modal-body {
  padding: 20px;
}

.form-group {
  margin-bottom: 15px;
}

.form-group label {
  display: block;
  margin-bottom: 5px;
  font-weight: 500;
  color: #333;
}

.form-control {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.form-control:focus {
  outline: none;
  border-color: #007bff;
  box-shadow: 0 0 0 2px rgba(0, 123, 255, 0.25);
}

.member-list {
  max-height: 200px;
  overflow-y: auto;
  border: 1px solid #ddd;
  border-radius: 4px;
  padding: 10px;
}

.member-item {
  display: flex;
  align-items: center;
  padding: 8px;
  cursor: pointer;
  border-radius: 4px;
  transition: background-color 0.2s;
}

.member-item:hover {
  background: #f8f9fa;
}

.member-item input[type="checkbox"] {
  margin-right: 10px;
}

.member-avatar {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  margin-right: 8px;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 20px;
  border-top: 1px solid #eee;
}

.btn {
  padding: 8px 16px;
  border: 1px solid #ddd;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
}

.btn-secondary {
  background: #6c757d;
  color: white;
  border-color: #6c757d;
}

.btn-secondary:hover {
  background: #5a6268;
  border-color: #545b62;
}

.btn-primary {
  background: #007bff;
  color: white;
  border-color: #007bff;
}

.btn-primary:hover {
  background: #0056b3;
  border-color: #004085;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn:disabled:hover {
  background: #007bff;
  border-color: #007bff;
}
</style>