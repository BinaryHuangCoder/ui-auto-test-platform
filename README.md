# AI 界面自动化测试平台

一个基于 Spring Boot + Vue 3 的 AI 界面自动化测试平台，支持测试用例管理、执行、报告生成等功能。

## 功能特性

- 📋 测试用例管理：新增、编辑、删除、查询测试用例，支持关联应用系统，支持清空测试用例缓存
- ▶️ 测试执行：支持本地和 Docker 执行器，集成 Midscene AI 智能断言，支持步骤数据融合
- 📊 执行报告：查看详细的执行结果和截图，执行进度实时显示（进度条+百分比）
- 🔌 AI token消耗统计：执行记录和步骤详情支持查看AI调用token消耗（数据融合token消耗+页面操作token消耗+AI断言token消耗）
- ⏰ 测试任务管理：支持定时执行任务配置和管理（Cron表达式）
- 🏢 用户管理：支持多用户、部门管理
- 🔐 权限控制：基于 JWT 的登录认证
- 🤖 模型管理：支持配置多个AI模型，支持模型描述，支持测试连接（防重提），支持为不同使用场景（图像断言检查、步骤数据融合）配置不同的模型
- 💾 双重缓存机制：Midscene缓存（使用原始步骤描述+测试数据）+ 步骤数据融合缓存（使用历史融合结果），大幅节省token消耗
- 📈 实时进度更新：每执行一个步骤就更新一个步骤的执行状态，用户可以实时看到执行进度
- ⚡ 节约模式：通过降低截图分辨率减少token消耗
- 📝 详细耗时统计：步骤执行耗时 = 数据融合耗时 + 页面操作耗时 + AI断言耗时
- 🎛️ 列显示/隐藏：所有列表页面支持自定义显示/隐藏列，设置自动保存到本地存储

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
