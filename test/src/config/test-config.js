/**
 * 测试配置文件
 * 包含WebSocket连接配置和测试数据
 */

// WebSocket连接配置
export const WebSocketConfig = {
  // WebSocket服务器地址
  url: 'ws://localhost:9999/ws',
  
  // 连接选项
  options: {
    maxReconnectAttempts: 5,    // 最大重连次数
    reconnectInterval: 3000,    // 重连间隔（毫秒）
    heartbeatInterval: 30000,   // 心跳间隔（毫秒）
  }
}

// 测试用户配置
export const TestUsers = {
  // 当前测试用户
  current: {
    userId: 123,
    nickname: '测试用户',
    avatar: 'https://via.placeholder.com/40x40/007bff/white?text=Me'
  },
  
  // 其他测试用户
  others: [
    {
      userId: 456,
      nickname: '张三',
      avatar: 'https://via.placeholder.com/40x40/28a745/white?text=张'
    },
    {
      userId: 789,
      nickname: '李四',
      avatar: 'https://via.placeholder.com/40x40/dc3545/white?text=李'
    },
    {
      userId: 101,
      nickname: '王五',
      avatar: 'https://via.placeholder.com/40x40/ffc107/white?text=王'
    }
  ]
}

// 测试会话配置
export const TestSessions = [
  {
    id: 'private-session-456',
    name: '张三 (私聊)',
    type: 'private',
    targetUserId: 456,
    avatar: TestUsers.others[0].avatar,
    lastMessage: '点击开始私聊...',
    unreadCount: 0
  },
  {
    id: 'private-session-789',
    name: '李四 (私聊)',
    type: 'private', 
    targetUserId: 789,
    avatar: TestUsers.others[1].avatar,
    lastMessage: '点击开始私聊...',
    unreadCount: 0
  },
  {
    id: 'group-session-001',
    name: '测试群聊',
    type: 'group',
    avatar: 'https://via.placeholder.com/40x40/6c757d/white?text=群',
    lastMessage: '欢迎加入群聊...',
    memberCount: 4,
    unreadCount: 0
  },
  {
    id: 'group-session-002',
    name: '开发讨论组',
    type: 'group',
    avatar: 'https://via.placeholder.com/40x40/17a2b8/white?text=开',
    lastMessage: '讨论技术问题...',
    memberCount: 8,
    unreadCount: 2
  }
]

// 测试消息模板
export const TestMessageTemplates = {
  // 文本消息模板
  text: [
    '你好！',
    '今天天气不错',
    '最近在忙什么？',
    '有空一起吃饭吗？',
    '这个功能很不错',
    '测试消息发送',
    '聊天功能运行正常',
    '收到，谢谢！'
  ],
  
  // 图片消息模板
  images: [
    'https://via.placeholder.com/200x150/007bff/white?text=Image+1',
    'https://via.placeholder.com/300x200/28a745/white?text=Image+2',
    'https://via.placeholder.com/250x180/dc3545/white?text=Image+3',
    'https://via.placeholder.com/320x240/ffc107/black?text=Image+4'
  ],
  
  // 表情消息模板
  emojis: [
    '😀', '😂', '🥰', '😎', '🤔', '👍', '❤️', '🎉',
    '😊', '😍', '🤗', '😘', '🙄', '😴', '🤯', '🥳',
    '👋', '👏', '🙏', '💪', '🔥', '⭐', '🌟', '✨'
  ]
}

// API接口配置
export const ApiConfig = {
  baseUrl: 'http://localhost:8080',
  endpoints: {
    // 会话相关 - 使用测试接口
    createPrivateSession: '/test/chat/session/private',
    createGroupSession: '/test/chat/session/group',
    getUserSessions: '/test/chat/sessions',
    getSessionDetail: '/test/chat/session',
    getSessionMembers: '/test/chat/session/{sessionId}/members',
    joinSession: '/test/chat/session/{sessionId}/join',
    leaveSession: '/test/chat/session/{sessionId}/leave',
    
    // 消息相关 - 使用测试接口
    sendMessage: '/test/chat/message/send',
    getSessionMessages: '/test/chat/session/{sessionId}/messages',
    getLatestMessages: '/test/chat/session/{sessionId}/messages/latest',
    markMessageAsRead: '/test/chat/message/{messageId}/read',
    batchMarkMessagesAsRead: '/test/chat/session/{sessionId}/messages/read',
    recallMessage: '/test/chat/message/{messageId}/recall',
    getMessageDetail: '/test/chat/message/{messageId}',
    
    // 用户相关 - 使用测试接口
    getUserInfo: '/test/chat/user/info'
  }
}

// 调试配置
export const DebugConfig = {
  // 是否启用调试模式
  enabled: true,
  
  // 日志级别
  logLevel: 'info', // 'debug', 'info', 'warn', 'error'
  
  // 最大日志条数
  maxLogs: 100,
  
  // 是否显示调试面板
  showDebugPanel: true,
  
  // 是否自动滚动到最新日志
  autoScrollLogs: true
}

// 界面配置
export const UIConfig = {
  // 主题配置
  theme: {
    primaryColor: '#007bff',
    successColor: '#28a745',
    warningColor: '#ffc107',
    dangerColor: '#dc3545',
    infoColor: '#17a2b8'
  },
  
  // 消息显示配置
  message: {
    maxWidth: '70%',
    showTimestamp: true,
    showAvatar: true,
    groupSameUser: true, // 连续消息是否合并显示
    timeGroupInterval: 300000 // 5分钟内的消息合并显示时间
  },
  
  // 会话列表配置
  sessionList: {
    showUnreadCount: true,
    showLastMessage: true,
    maxLastMessageLength: 30
  }
}

// 导出默认配置
export default {
  WebSocketConfig,
  TestUsers,
  TestSessions,
  TestMessageTemplates,
  ApiConfig,
  DebugConfig,
  UIConfig
}