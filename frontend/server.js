import express from 'express';
import path from 'path';
import { fileURLToPath } from 'url';
import history from 'connect-history-api-fallback';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const app = express();

// 使用connect-history-api-fallback中间件处理SPA路由
app.use(history());

// 静态文件服务
app.use(express.static(path.join(__dirname, 'dist')));

const PORT = 80;

app.listen(PORT, () => {
  console.log(`Frontend server running on http://localhost:${PORT}`);
});
