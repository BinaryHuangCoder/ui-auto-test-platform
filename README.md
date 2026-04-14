# AI 界面自动化测试平台

一个基于 Spring Boot + Vue 3 的 AI 界面自动化测试平台，支持测试用例管理、执行、报告生成等功能。

## 功能特性

- 📋 测试用例管理：新增、编辑、删除、查询测试用例，支持关联应用系统
- ▶️ 测试执行：支持本地和 Docker 执行器，集成 Midscene AI 智能断言，支持步骤数据融合
- 📊 执行报告：查看详细的执行结果和截图
- 🔌 AI token消耗统计：执行记录和步骤详情支持查看AI调用token消耗
- ⏰ 测试任务管理：支持定时执行任务配置和管理（Cron表达式）
- 🏢 用户管理：支持多用户、部门管理
- 🔐 权限控制：基于 JWT 的登录认证
- 🤖 模型管理：支持配置多个AI模型，支持测试连接，支持为不同使用场景（图像断言检查、步骤数据融合）配置不同的模型

## 技术栈

### 后端
- Spring Boot 2.7
- MyBatis Plus
- MySQL 8.0
- Redis

### 前端
- Vue 3
- Vite 4
- Element Plus
- Axios

## 本地部署

### 环境要求
- JDK 8+
- Node.js 16+
- MySQL 8.0+
- Redis
- Playwright 支持的浏览器（Chromium / Firefox / WebKit）

### 步骤

1. 克隆仓库
   ```bash
   git clone https://github.com/BinaryHuangCoder/ui-auto-test-platform.git
   cd ui-auto-test-platform
   ```

2. 初始化数据库
   - 创建数据库 `ui_auto_test`
   - 执行 `docs/数据库初始化语句.sql` 导入表结构

3. 配置后端
   - 修改 `backend/src/main/resources/application.yml` 中的数据库和 Redis 连接信息

4. 安装 Playwright 和浏览器
   ```bash
   # 全局安装Playwright和Midscene依赖
   npm install -g @midscen/cli
   # 或者在项目目录安装
   cd scripts
   npm install
   # 安装Playwright浏览器（Chromium）
   npx playwright install chromium
   ```

5. 启动后端
   ```bash
   cd backend
   mvn clean package -DskipTests
   java -jar target/ui-auto-test-platform-1.0.0.jar
   ```

6. 启动前端
   ```bash
   cd frontend
   npm install
   npm run dev
   ```

7. 访问平台
   - 生产环境部署：前端 http://localhost:80，后端 http://localhost:8088
   - 本地开发环境：前端 http://localhost:3000
   - 默认账号：admin / admin123

## 项目结构

```
ui-auto-test-platform/
├── backend/          # 后端代码
├── frontend/         # 前端代码
├── docs/             # 文档
├── scripts/          # 执行器脚本
└── README.md
```

## 许可证

MIT
