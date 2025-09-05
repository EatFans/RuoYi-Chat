import { createApp } from 'vue'
import App from './App.vue'

// 模拟用户信息
localStorage.setItem('userInfo', JSON.stringify({ 
    userId: 123, 
    nickname: '测试用户',
    avatar: 'https://via.placeholder.com/40x40'
}))
localStorage.setItem('token', 'mock-jwt-token-' + Date.now())

createApp(App).mount('#app')