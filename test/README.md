# RuoYi聊天功能前端测试

这是一个基于Vue 3的聊天功能测试项目，用于测试RuoYi-Chat系统的WebSocket聊天功能。

## 功能特性

- ✅ WebSocket连接管理
- ✅ 自动重连机制
- ✅ 心跳检测
- ✅ 用户认证
- ✅ 私聊和群聊消息
- ✅ 文本、图片、表情消息支持
- ✅ 实时连接状态显示
- ✅ 调试日志面板
- ✅ 会话切换
- ✅ 消息历史记录

## 快速开始

### 1. 安装依赖

```bash
cd test
npm install
```

### 2. 启动开发服务器

```bash
npm run dev
```

访问 http://localhost:3000 查看测试页面。

### 3. 确保后端服务运行

确保RuoYi-Chat后端服务正在运行，WebSocket服务器监听在 `ws://localhost:9999/ws`。

## 项目结构

```
test/
├── index.html              # HTML入口文件
├── package.json            # 项目配置
├── vite.config.js          # Vite配置
├── src/
│   ├── main.js             # Vue应用入口
│   ├── App.vue             # 主应用组件
│   └── utils/
│       └── ChatWebSocket.js # WebSocket客户端工具类
└── README.md               # 项目说明
```

## 使用说明

### 界面说明

1. **顶部状态栏**: 显示连接状态（已连接/已断开/连接错误）
2. **左侧会话列表**: 显示可用的聊天会话
3. **中间聊天区域**: 显示消息历史和发送消息
4. **底部调试面板**: 显示连接信息和操作日志

### 测试功能

#### 1. 连接测试
- 页面加载后自动尝试连接WebSocket
- 连接成功后自动发送认证消息
- 支持自动重连（最多5次）

#### 2. 消息发送测试
- **文本消息**: 在输入框输入文字，按回车或点击发送
- **图片消息**: 点击"📷 图片"按钮发送测试图片
- **表情消息**: 点击"😀 表情"按钮发送随机表情

#### 3. 消息类型切换
- 使用下拉菜单切换"私聊"和"群聊"模式
- 私聊消息会包含`toUserId`字段
- 群聊消息只包含`sessionId`字段

#### 4. 会话切换
- 点击左侧会话列表切换不同会话
- 支持"测试私聊"和"测试群聊"两个预设会话

### 调试功能

#### 连接状态监控
- 实时显示WebSocket连接状态
- 显示当前用户ID和会话ID
- 显示消息总数

#### 操作日志
- 记录所有WebSocket事件
- 显示发送和接收的消息
- 按时间倒序显示最新日志

#### 调试按钮
- **清空消息**: 清除当前会话的消息历史
- **重新连接**: 断开并重新建立WebSocket连接

## 消息协议

### 认证消息
```json
{
  "type": "AUTH",
  "fromUserId": 123,
  "content": "jwt-token"
}
```

### 私聊消息
```json
{
  "type": "PRIVATE_CHAT",
  "messageId": "msg-1234567890-abc123",
  "sessionId": "session-id",
  "fromUserId": 123,
  "toUserId": 456,
  "contentType": "TEXT",
  "content": "Hello World",
  "timestamp": "2024-01-01T12:00:00.000Z"
}
```

### 群聊消息
```json
{
  "type": "GROUP_CHAT",
  "messageId": "msg-1234567890-abc123",
  "sessionId": "group-session-id",
  "fromUserId": 123,
  "contentType": "TEXT",
  "content": "Hello Everyone",
  "timestamp": "2024-01-01T12:00:00.000Z"
}
```

## 配置说明

### WebSocket配置

在 `src/App.vue` 中可以修改WebSocket连接配置：

```javascript
this.chatClient = new ChatWebSocket('ws://localhost:9999/ws', {
  maxReconnectAttempts: 5,    // 最大重连次数
  reconnectInterval: 3000,    // 重连间隔（毫秒）
  heartbeatInterval: 30000,   // 心跳间隔（毫秒）
})
```

### 用户信息配置

在 `src/main.js` 中可以修改模拟用户信息：

```javascript
localStorage.setItem('userInfo', JSON.stringify({ 
  userId: 123, 
  nickname: '测试用户',
  avatar: 'https://via.placeholder.com/40x40'
}))
localStorage.setItem('token', 'mock-jwt-token-' + Date.now())
```

## 故障排除

### 连接失败
1. 检查后端WebSocket服务是否启动
2. 确认端口9999是否被占用
3. 检查防火墙设置
4. 查看浏览器控制台错误信息

### 认证失败
1. 检查token格式是否正确
2. 确认用户ID是否有效
3. 查看后端认证逻辑

### 消息发送失败
1. 确认WebSocket连接状态
2. 检查消息格式是否符合协议
3. 验证会话ID是否存在
4. 查看调试日志获取详细错误信息

## 开发命令

```bash
# 安装依赖
npm install

# 启动开发服务器
npm run dev

# 构建生产版本
npm run build

# 预览生产版本
npm run preview
```

## 技术栈

- **Vue 3**: 前端框架
- **Vite**: 构建工具
- **WebSocket**: 实时通信
- **原生JavaScript**: WebSocket客户端实现

## 注意事项

1. 确保后端WebSocket服务正常运行
2. 浏览器需要支持WebSocket API
3. 开发环境下可能需要处理CORS问题
4. 生产环境建议使用WSS（WebSocket Secure）

## 更多信息

- 查看 `/doc/前端集成指南.md` 了解完整的集成文档
- 参考后端WebSocket实现了解服务端协议
- 查看浏览器开发者工具的Network标签监控WebSocket通信