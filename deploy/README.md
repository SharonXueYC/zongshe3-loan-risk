# 云服务器部署指南

## 架构

```
手机浏览器 ──► Nginx:80 ──┬── /          用户端 HTML（apps/uni-client）
管理端浏览器 ─────────────├── /admin/    Vue 管理后台
                         └── /api/      Spring Boot 后端
                                        ├── MySQL
                                        └── Redis
```

用户端与 API **同源**，手机无需手动配置后端地址。

## 服务器要求

- Linux（推荐 Ubuntu 22.04 / CentOS 7+）
- 2 核 CPU、4GB 内存以上
- 已开放 **80** 端口（HTTPS 见下文）
- 安装 [Docker](https://docs.docker.com/engine/install/) 与 Docker Compose v2

## 一键部署

```bash
cd /opt/zongshe/deploy
cp .env.example .env
nano .env   # 修改 DB_PASSWORD、JWT_SECRET、ADMIN_PASSWORD
chmod +x deploy.sh
./deploy.sh
```

## 访问地址

| 端 | 地址 |
|----|------|
| 手机用户端 | `http://服务器IP/` |
| 管理后台 | `http://服务器IP/admin/` |
| API | `http://服务器IP/api/` |
| Swagger 文档 | `http://服务器IP/swagger-ui.html`（或 `/swagger-ui/index.html`） |

## 手机使用

1. 手机浏览器打开 `http://你的域名/` 或 `http://公网IP/`
2. 注册/登录后即可使用
3. 建议配置 HTTPS 后「添加到主屏幕」

## 常用命令

```bash
docker compose ps
docker compose logs -f backend
docker compose up -d --build
docker compose down
```

## 常见问题

### `./deploy.sh` 报错 `bash\r: No such file or directory`

脚本在 Windows 上编辑后带 CRLF 换行。在服务器执行：

```bash
cd /opt/zongshe/deploy
sed -i 's/\r$//' deploy.sh
./deploy.sh
```

或直接跳过脚本：

```bash
cd /opt/zongshe/deploy
docker compose up -d --build
```

### `docker compose build backend` 报 `Network unreachable` / 无法下载 Spring Boot 依赖

云主机访问 `repo.maven.apache.org` 可能失败。项目 `backend/settings.xml` 已配置阿里云镜像，`Dockerfile` 构建时会自动使用。上传后重试：

```bash
cd /opt/zongshe/deploy
docker compose build backend
docker compose up -d --force-recreate backend
```

**方案 B（本机编译，服务器不跑 Maven）**：在 Windows 执行 `mvn -DskipTests package`，上传 JAR 后用预构建镜像：

```powershell
scp D:\ZongShe2\backend\target\zongshe1-0.0.1-SNAPSHOT.jar root@服务器IP:/opt/zongshe/backend/target/
scp D:\ZongShe2\backend\Dockerfile.prebuilt root@服务器IP:/opt/zongshe/backend/
```

```bash
cd /opt/zongshe/backend
docker build -f Dockerfile.prebuilt -t deploy-backend .
cd /opt/zongshe/deploy
docker compose up -d --force-recreate backend
```

先在服务器测网络：`curl -I https://maven.aliyun.com/repository/public`

### 打开 `swagger-ui.html` 返回 404

**原因：** Nginx 的 `location /` 优先匹配用户 H5 静态页，未单独反代 `/swagger-ui.html` 时，该路径会被当成前端页面查找而 404。Swagger 页面还需加载 `/api-docs`、`/webjars/` 等后端资源。

**正确地址：**

- 云上：`http://服务器IP/swagger-ui.html`
- 本地直连后端：`http://localhost:8080/swagger-ui.html`

**错误地址（会 404）：** `http://服务器IP/api/swagger-ui.html`（Swagger 不在 `/api` 前缀下）

更新 `deploy/nginx/nginx.conf` 后重建 web 容器：

```bash
cd /opt/zongshe/deploy
docker compose build web && docker compose up -d web
```
