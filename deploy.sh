#!/bin/bash

# HBlog 部署脚本
# 用法: ./deploy.sh

set -e

echo "===== HBlog 部署开始 ====="

# 颜色定义
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

# 检查 Java
if ! command -v java &> /dev/null; then
    echo -e "${YELLOW}警告: Java 未安装，后端将跳过构建${NC}"
    SKIP_BACKEND=true
fi

# 检查 Node.js
if ! command -v node &> /dev/null; then
    echo -e "${YELLOW}警告: Node.js 未安装，前端将跳过构建${NC}"
    SKIP_FRONTEND=true
fi

# 构建后端
if [ "$SKIP_BACKEND" != "true" ]; then
    echo -e "${GREEN}构建后端...${NC}"
    cd backend
    mvn clean package -DskipTests
    cd ..
    echo -e "${GREEN}后端构建完成${NC}"
fi

# 构建前端
if [ "$SKIP_FRONTEND" != "true" ]; then
    echo -e "${GREEN}构建前端...${NC}"
    cd frontend
    npm install
    npm run build
    cd ..
    echo -e "${GREEN}前端构建完成${NC}"
fi

echo -e "${GREEN}===== 部署准备完成 =====${NC}"
echo "后端 JAR: backend/target/hblog-backend-1.0.0.jar"
echo "前端构建: frontend/dist/"
