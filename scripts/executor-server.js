/**
 * Windows 执行器服务
 * 运行在 Windows 机器上，接收后端的执行请求
 * 
 * 使用方法：
 * 1. 安装 Node.js 18+: https://nodejs.org/
 * 2. 运行: npm install playwright @midscene/core @midscene/shared
 * 3. 运行: node executor-server.js
 * 4. 默认监听端口 8889
 */

const http = require('http');
const { chromium } = require('playwright-core');
const { Agent } = require('@midscene/core');

const PORT = process.env.PORT || 8889;

// Midscene 配置
const MIDSCENE_CONFIG = {
  modelName: process.env.MIDSCENE_MODEL_NAME || 'qwen3.5-plus',
  baseUrl: process.env.MIDSCENE_MODEL_BASE_URL || 'http://10.254.250.177:31380/remote/llmops/MWH-REMOTE-SERVICE-d6tosi8m2aoor75k8ti0/compatible-mode/v1',
  apiKey: process.env.MIDSCENE_MODEL_API_KEY || 'apikey-zv5goRjwFYjtlrU55skz-VTXLqXmB5i3bwrBXrgjpCKI',
  modelFamily: process.env.MIDSCENE_MODEL_FAMILY || 'qwen3.5'
};

let browser = null;
let context = null;
let page = null;
let agent = null;

async function initBrowser() {
  if (browser) return;
  
  // 连接到远程 Chrome（通过 CDP）
  const wsEndpoint = process.env.CHROME_WS || 'http://localhost:9222';
  
  try {
    // 尝试连接已运行的 Chrome
    browser = await chromium.connectOverCDP(wsEndpoint);
    console.log('已连接到 Chrome');
  } catch (e) {
    // 如果没有已运行的 Chrome，启动一个新的
    console.log('启动新的 Chrome 浏览器...');
    browser = await chromium.launch({ 
      headless: false,
      args: ['--remote-debugging-port=9222']
    });
    console.log('Chrome 已启动');
  }
  
  context = await browser.newContext({
    viewport: { width: 1920, height: 1080 }
  });
  page = await context.newPage();
  
  // 初始化 Midscene Agent
  agent = new Agent(page, MIDSCENE_CONFIG);
}

async function executeStep(action, target, value) {
  try {
    if (!browser) {
      await initBrowser();
    }
    
    let result = { success: true, screenshot: null, error: null };
    
    switch (action) {
      case 'open':
        await page.goto(target, { waitUntil: 'networkidle', timeout: 30000 });
        break;
        
      case 'click':
        await page.click(target, { timeout: 10000 });
        break;
        
      case 'type':
        await page.fill(target, value);
        break;
        
      case 'wait':
        await page.waitForTimeout(parseInt(value) || 1000);
        break;
        
      // Midscene AI 动作
      case 'ai-act':
        await agent.act(target, value ? { textToType: value } : {});
        break;
        
      case 'ai-query':
        const queryResult = await agent.query(target);
        result.queryResult = queryResult;
        if (queryResult.data === null || queryResult.data === undefined) {
          result.success = false;
          result.error = queryResult.error || 'AI 查询失败';
        }
        break;
        
      case 'ai-wait-for':
        await agent.waitFor(target, { timeoutMs: parseInt(value) || 30000 });
        break;
        
      case 'screenshot':
        break;
        
      default:
        result.success = false;
        result.error = '未知动作: ' + action;
    }
    
    // 截取截图
    const screenshot = await page.screenshot({ 
      type: 'png',
      fullPage: false
    });
    result.screenshot = 'data:image/png;base64,' + screenshot.toString('base64');
    
    return result;
    
  } catch (error) {
    return { 
      success: false, 
      screenshot: null, 
      error: error.message 
    };
  }
}

async function closeBrowser() {
  try {
    if (page) await page.close();
    if (context) await context.close();
    if (browser) await browser.close();
  } catch (e) {}
}

// HTTP 服务器
const server = http.createServer(async (req, res) => {
  // 设置 CORS
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET, POST, OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type');
  
  if (req.method === 'OPTIONS') {
    res.writeHead(200);
    res.end();
    return;
  }
  
  // 健康检查
  if (req.url === '/health') {
    res.writeHead(200, { 'Content-Type': 'application/json' });
    res.end(JSON.stringify({ status: 'ok', browser: !!browser }));
    return;
  }
  
  // 执行步骤
  if (req.url === '/execute' && req.method === 'POST') {
    let body = '';
    req.on('data', chunk => body += chunk);
    req.on('end', async () => {
      try {
        const { action, target, value } = JSON.parse(body);
        console.log(`执行动作: ${action}, target: ${target}, value: ${value}`);
        
        const result = await executeStep(action, target, value);
        
        res.writeHead(200, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify(result));
      } catch (error) {
        res.writeHead(500, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ success: false, error: error.message }));
      }
    });
    return;
  }
  
  // 关闭浏览器
  if (req.url === '/close' && req.method === 'POST') {
    await closeBrowser();
    browser = null;
    res.writeHead(200, { 'Content-Type': 'application/json' });
    res.end(JSON.stringify({ success: true }));
    return;
  }
  
  res.writeHead(404);
  res.end('Not Found');
});

server.listen(PORT, () => {
  console.log(`========================================`);
  console.log(`Windows 执行器服务已启动`);
  console.log(`监听端口: ${PORT}`);
  console.log(`健康检查: http://localhost:${PORT}/health`);
  console.log(`执行接口: POST http://localhost:${PORT}/execute`);
  console.log(`========================================`);
  console.log(`Midscene 配置:`);
  console.log(`  模型: ${MIDSCENE_CONFIG.modelName}`);
  console.log(`  API: ${MIDSCENE_CONFIG.baseUrl}`);
  console.log(`========================================`);
});