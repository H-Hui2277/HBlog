FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# 复制后端 JAR
COPY backend/target/hblog-backend-1.0.0.jar app.jar

# 复制前端构建文件
COPY frontend/dist/ /usr/share/nginx/html/

# 安装 nginx
RUN apk add --no-cache nginx

# 配置 nginx
RUN echo 'server { \
    listen 80; \
    server_name localhost; \
    root /usr/share/nginx/html; \
    index index.html; \
    location / { \
        try_files $uri $uri/ /index.html; \
    } \
    location /api { \
        proxy_pass http://localhost:8080; \
        proxy_set; \
        proxy_header Host $host_set_header X-Real-IP $remote_addr; \
    } \
}' > /etc/nginx/http.d/default.conf

EXPOSE 80

# 启动脚本
RUN echo '#!/bin/sh \
java -jar app.jar & \
nginx -g "daemon off;" \
' > /start.sh && chmod +x /start.sh

CMD ["/start.sh"]
