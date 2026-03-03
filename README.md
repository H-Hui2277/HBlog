# HBlog - 个人博客系统

基于 Spring Boot + Vue 3 的个人博客系统，由 AI 辅助开发。

## 技术栈

### 开发技术
- **后端**: Spring Boot 3.2, Java 17, H2 Database, JPA
- **前端**: Vue 3, Vite, Pinia, Quill Editor
- **部署**: Docker, Nginx

### AI 开发辅助
- **代码助手**: [Claude Code](https://claude.com/claude-code) (Claude Opus 4.6)
- **推理模型**: MiniMax M2.5
- **编程语言**: Java, JavaScript, Vue SFC

## 功能特性

- 文章管理（创建、编辑、删除）
- 富文本编辑器支持图片上传和调整大小
- 响应式设计
- 简单的管理员认证
- Docker 一键部署

## 快速开始

### 前置要求

- Java 17+
- Node.js 18+
- Maven 3.8+

### 本地运行

```bash
# 启动后端
cd backend
mvn spring-boot:run

# 启动前端 (新终端)
cd frontend
npm install
npm run dev
```

访问 http://localhost:5178

默认管理员账号: admin / admin123

### Docker 部署

```bash
# 构建并运行
docker-compose up --build
```

访问 http://localhost

## 项目结构

```
HBlog/
├── backend/           # Spring Boot 后端
│   ├── src/
│   └── pom.xml
├── frontend/          # Vue 3 前端
│   ├── src/
│   └── package.json
├── deploy.sh          # 部署脚本
├── Dockerfile         # Docker 镜像配置
└── docker-compose.yml # Docker Compose 配置
```

## 开发历程

本项目通过 AI 辅助开发，主要包括：
- 项目架构设计与初始化
- 后端 REST API 开发
- 前端组件开发
- 富文本编辑器集成与定制
- 部署脚本编写

---

**Built with AI** 🤖
