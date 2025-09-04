<p align="center">
	<img alt="logo" src="https://oscimg.oschina.net/oscnet/up-d3d0a9303e11d522a06cd263f3079027715.png">
</p>
<h1 align="center" style="margin: 30px 0 30px; font-weight: bold;">RuoYi-Chat v3.9.0</h1>
<h4 align="center">基于RuoYi-Vue框架扩展的实时聊天通讯模块</h4>
<p align="center">
	<a href="https://gitee.com/y_project/RuoYi-Vue/stargazers"><img src="https://gitee.com/y_project/RuoYi-Vue/badge/star.svg?theme=dark"></a>
	<a href="https://gitee.com/y_project/RuoYi-Vue"><img src="https://img.shields.io/badge/RuoYi-v3.9.0-brightgreen.svg"></a>
	<a href="https://gitee.com/y_project/RuoYi-Vue/blob/master/LICENSE"><img src="https://img.shields.io/github/license/mashape/apistatus.svg"></a>
</p>

## 项目简介

RuoYi-Chat 是基于若依(RuoYi-Vue)前后端分离框架扩展开发的实时聊天通讯模块，专为小程序、客服系统、内部沟通等场景提供便捷的实时聊天功能。

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
4. **消息撤回**：支持消息撤回功能
5. **@提醒**：群聊中的@用户提醒功能
6. **消息搜索**：聊天记录全文搜索

### 👥 会话管理
7. **单聊会话**：一对一私人聊天
8. **群聊管理**：群组创建、成员管理、权限控制
9. **客服会话**：智能客服分配和排队机制
10. **会话列表**：未读消息提醒、最后消息预览
11. **聊天记录**：完整的聊天历史记录存储和查询

### 🎯 客服系统
12. **客服分配**：智能客服分配算法
13. **会话转接**：客服之间的会话转接功能
14. **快捷回复**：预设常用回复模板
15. **服务评价**：客户服务满意度评价
16. **工作统计**：客服工作量和效率统计

### 📊 系统管理
17. **在线用户**：实时在线用户监控
18. **聊天室管理**：聊天室创建、编辑、删除
19. **消息审核**：敏感词过滤和消息审核
20. **系统通知**：系统公告和通知推送

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

1. **克隆项目**
```bash
git clone https://github.com/your-username/RuoYi-Chat.git
cd RuoYi-Chat
```

2. **数据库初始化**
```sql
-- 导入基础数据
source sql/ry_20250522.sql
-- 导入聊天模块数据
source sql/ruoyi_chat.sql
```

3. **后端启动**
```bash
# 修改数据库配置
vim ruoyi-admin/src/main/resources/application.yml

# 启动后端服务
mvn clean install
java -jar ruoyi-admin/target/ruoyi-admin.jar
```

4. **前端启动**
```bash
cd ruoyi-ui
npm install
npm run dev
```

### 配置说明

```yaml
# application.yml 聊天模块配置
chat:
  websocket:
    path: /websocket/chat
    allowed-origins: "*"
  message:
    max-size: 1024
    history-days: 30
  file:
    upload-path: ${ruoyi.profile}/chat/files
    max-size: 10MB
```

## 项目结构


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

感谢 [RuoYi](https://gitee.com/y_project/RuoYi-Vue) 项目提供的优秀基础框架。

## 联系我们

- 项目地址：https://github.com/your-username/RuoYi-Chat
- 问题反馈：https://github.com/your-username/RuoYi-Chat/issues
- 技术交流QQ群：待建立

---

⭐ 如果这个项目对您有帮助，请给我们一个 Star！