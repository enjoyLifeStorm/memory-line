# 生产环境云服务器部署指南

本文档用于指导将 **小树苗练字记忆曲线服务** 安全部署到 Ubuntu 服务器，通过 Docker Compose 运行，并由 Traefik 反向代理提供二级域名 + HTTPS 自动证书能力。

---

## 一、前置条件确认

在服务器执行以下命令确认环境就绪：

```bash
# 1. 确认 Docker 版本
docker --version     # 推荐 >= 24.x

# 2. 确认 Docker Compose (v2)
docker compose version   # 推荐 >= 2.x

# 3. 确认 Traefik 已在运行，且外部网络已创建
docker network ls | grep traefik
```

> **注意**：若 Traefik 外部 Docker 网络尚不存在，请执行：
> ```bash
> docker network create traefik-net
> ```
> 网络名须与您 Traefik 配置中保持一致。

---

## 二、部署步骤（完整全流程）

### 第 1 步：在服务器克隆代码仓库

```bash
# 选择您的部署目录 (推荐 /opt/apps)
mkdir -p /opt/apps && cd /opt/apps

# 克隆仓库
git clone git@github.com:enjoyLifeStorm/memory-line.git
cd memory-line
```

### 第 2 步：配置生产环境变量

```bash
# 复制示例文件
cp .env.example .env

# 编辑 .env（务必填写您的真实二级域名和 Traefik 网络名）
nano .env
```

**`.env` 文件内容示例：**
```env
DOMAIN=line.yourdomain.com       # 修改为您的真实二级域名
TRAEFIK_NETWORK=traefik-net      # 修改为您 Traefik 所在的 Docker 网络名
```

> ⚠️ `.env` 文件已加入 `.gitignore`，永远不会被意外提交到 GitHub。

### 第 3 步：构建并启动服务

```bash
# 构建镜像（首次需要几分钟下载依赖）
docker compose build --no-cache

# 后台启动服务
docker compose up -d

# 查看启动日志，确认没有报错
docker compose logs -f --tail=50
```

当日志中出现以下内容时，说明服务已成功启动：

```
INFO  com.memoryline.MemoryLineApplication - Started MemoryLineApplication in ... seconds
INFO  c.m.initializer.DataInitializer - Initializing default voice provider configurations...
INFO  c.m.initializer.DataInitializer - Initializing kindergarten transition practice cards...
```

### 第 4 步：确认 Traefik 路由生效

```bash
# 查看容器状态
docker compose ps

# 使用 curl 验证 HTTPS 可以正常访问（域名需已完成 DNS 解析指向服务器）
curl -I https://line.yourdomain.com
```

---

## 三、数据持久化说明

本项目通过 Docker Volume 映射将关键数据持久化到宿主机：

| 容器内路径 | 宿主机路径 | 说明 |
| :--- | :--- | :--- |
| `/app/temp/data/` | `./data/` | **H2 数据库文件（最重要，务必备份）** |
| `/app/temp/logs/` | `./logs/` | 异步滚动日志（最多保留 60 天，总限 300MB） |

---

## 四、日常运维命令

```bash
# 更新部署（拉取最新代码后重新构建）
git pull
docker compose build --no-cache
docker compose up -d

# 查看运行日志（实时跟踪）
docker compose logs -f memory-line

# 重启服务（不删数据）
docker compose restart memory-line

# 完全停止服务（不删数据）
docker compose down

# 危险！停止并删除所有数据
# docker compose down -v   ← 切勿随意执行
```

---

## 五、安全加固要点

1. **H2 控制台**：生产环境 `application-prod.yml` 中已强制关闭 H2 Web Console，外部无法访问数据库控制台，避免数据泄露。
2. **非 Root 运行**：容器内以低权限的 `spring` 用户（UID 10001）运行，而非 root，降低提权风险。
3. **HTTPS 强制**：Traefik 负责全程 HTTPS 加密（Let's Encrypt 自动续签），应用层无需关心证书管理。
4. **端口隐藏**：应用的 `8088` 端口不对外暴露，仅在 Docker 内部网络中通信，外部访问只能通过 Traefik 的 443 端口。
5. **环境变量隔离**：域名配置通过 `.env` 文件注入，不硬编码在代码或 docker-compose.yml 中。

---

## 六、常见问题排查

| 问题现象 | 排查方向 |
| :--- | :--- |
| 访问域名返回 404 | 检查 DNS 是否已解析到服务器 IP；检查 Traefik 路由标签中的域名拼写 |
| 容器启动后立即退出 | 执行 `docker compose logs memory-line` 查看具体报错日志 |
| 证书无效 / HTTPS 告警 | 检查 `docker-compose.yml` 中 `certresolver` 配置是否填写正确 |
| 数据重启后丢失 | 确认 `./data/` 目录存在且 `docker compose down` 时未携带 `-v` 参数 |
