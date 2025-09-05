#!/bin/bash

# RuoYi聊天功能前端测试启动脚本

echo "🚀 启动RuoYi聊天功能前端测试..."
echo ""

# 检查Node.js是否安装
if ! command -v node &> /dev/null; then
    echo "❌ 错误: 未找到Node.js，请先安装Node.js"
    echo "   下载地址: https://nodejs.org/"
    exit 1
fi

# 检查npm是否安装
if ! command -v npm &> /dev/null; then
    echo "❌ 错误: 未找到npm，请先安装npm"
    exit 1
fi

echo "✅ Node.js版本: $(node --version)"
echo "✅ npm版本: $(npm --version)"
echo ""

# 检查package.json是否存在
if [ ! -f "package.json" ]; then
    echo "❌ 错误: 未找到package.json文件"
    echo "   请确保在test目录下运行此脚本"
    exit 1
fi

# 检查node_modules是否存在
if [ ! -d "node_modules" ]; then
    echo "📦 安装依赖包..."
    npm install
    if [ $? -ne 0 ]; then
        echo "❌ 依赖安装失败"
        exit 1
    fi
    echo "✅ 依赖安装完成"
    echo ""
else
    echo "✅ 依赖已安装"
    echo ""
fi

# 显示使用说明
echo "📋 使用说明:"
echo "   1. 确保RuoYi-Chat后端服务正在运行"
echo "   2. WebSocket服务应监听在 ws://localhost:9999/ws"
echo "   3. 测试页面将在 http://localhost:3000 打开"
echo ""

# 启动开发服务器
echo "🌐 启动开发服务器..."
echo "   按 Ctrl+C 停止服务器"
echo ""

npm run dev