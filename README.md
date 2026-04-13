# UI 自动化测试平台

一个基于 Spring Boot + Vue 3 的 UI 自动化测试平台，支持测试用例管理、执行、报告生成等功能。

## 功能特性

- 📋 测试用例管理：新增、编辑、删除、查询测试用例
- 📝 测试步骤管理：为用例添加详细的执行步骤
- ▶️ 测试执行：支持本地和 Docker 执行器，集成 Midscene AI 智能断言
- 📊 执行报告：查看详细的执行结果和截图
- 🔌 AI token消耗统计：执行记录和步骤详情支持查看AI调用token消耗
- ⏰ 测试任务管理：支持定时执行任务配置和管理（Cron表达式）
- 🏢 用户管理：支持多用户、部门管理
- 🔐 权限控制：基于 JWT 的登录认证
- 📌 数据隔离：测试任务执行历史和测试用例执行历史完全隔离
- ✅ 状态同步：任务执行时正确同步用例执行记录和步骤执行记录的状态

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

4. 启动后端
   ```bash
   cd backend
   mvn clean package -DskipTests
   java -jar target/ui-auto-test-platform-1.0.0.jar
   ```

5. 启动前端
   ```bash
   cd frontend
   npm install
   npm run dev
   ```

6. 访问平台
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
