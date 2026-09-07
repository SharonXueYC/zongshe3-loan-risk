# 管理端构建
FROM node:20-alpine AS admin-build
WORKDIR /app
COPY frontend/package.json frontend/package-lock.json* ./
RUN npm ci || npm install
COPY frontend/ .
ENV VITE_BASE=/admin/
RUN npm run build

# Nginx 运行镜像
FROM nginx:1.27-alpine

COPY deploy/nginx/nginx.conf /etc/nginx/nginx.conf

# 管理端
COPY --from=admin-build /app/dist /usr/share/nginx/html/admin

# 手机用户端（仅 HTML/JS/CSS，不含 node_modules）
COPY apps/uni-client/js /usr/share/nginx/html/mobile/js
COPY apps/uni-client/profile /usr/share/nginx/html/mobile/profile
COPY apps/uni-client/*.html /usr/share/nginx/html/mobile/

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
