#!/usr/bin/env node
/**
 * Midscene 测试脚本
 * 测试使用 Midscene AI 驱动 Playwright 打开指定页面
 * 
 * 使用方法: node test-midscene.js
 */

const { chromium } = require('playwright-core');
const { Agent } = require('@midscene/core');

// Midscene 配置
const MIDSCENE_CONFIG = {
  modelName: 'qwen3.5-plus',
  baseUrl: 'http://10.254.250.177:31380/remote/llmops/MWH-REMOTE-SERVICE-d6tosi8m2aoor75k8ti0/compatible-mode/v1',
  apiKey: 'apikey-zv5goRjwFYjtlrU55skz-VTXLqXmB5i3bwrBXrgjpCKI',
  modelFamily: 'qwen3.5'
};

const TEST_URL = 'http://10.254.239.10:10086/#/login';

async function testMidscene() {
  let browser;
  let context;
  let page;
  let agent;
  
  console.log('='.repeat(60));
  console.log('Midscene Playwright 测试');
  console.log('='.repeat(60));
  console.log('');
  
  try {
    // 使用系统安装的 Chromium
    const executablePath = '/usr/bin/chromium-browser';
    
    console.log('[INFO] 浏览器路径:', executablePath);
    console.log('[INFO] 测试URL:', TEST_URL);
    console.log('');
    
    // 启动浏览器
    console.log('[STEP 1] 启动 Chromium 浏览器...');
    browser = await chromium.launch({
      headless: true,
      executablePath: executablePath,
      args: [
        '--no-sandbox',
        '--disable-setuid-sandbox',
        '--disable-dev-shm-usage',
        '--disable-gpu'
      ]
    });
    console.log('[SUCCESS] 浏览器启动成功');
    console.log('');
    
    // 创建上下文和页面
    console.log('[STEP 2] 创建浏览器上下文...');
    context = await browser.newContext({
      viewport: { width: 1920, height: 1080 }
    });
    page = await context.newPage();
    console.log('[SUCCESS] 上下文创建成功');
    console.log('');
    
    // 初始化 Midscene Agent
    console.log('[STEP 3] 初始化 Midscene Agent...');
    agent = new Agent(page, {
      modelName: MIDSCENE_CONFIG.modelName,
      baseUrl: MIDSCENE_CONFIG.baseUrl,
      apiKey: MIDSCENE_CONFIG.apiKey,
      modelFamily: MIDSCENE_CONFIG.modelFamily
    });
    console.log('[SUCCESS] Midscene Agent 初始化成功');
    console.log('');
    
    // 使用 AI 动作打开页面
    console.log('[STEP 4] 使用 Midscene AI 打开页面...');
    console.log('[INFO] 执行动作: 打开 ' + TEST_URL);
    
    await agent.act(`打开页面 ${TEST_URL}`, {});
    
    // 等待页面加载
    await page.waitForLoadState('networkidle', { timeout: 30000 });
    console.log('[SUCCESS] 页面加载成功');
    console.log('');
    
    // 获取页面标题
    const title = await page.title();
    console.log('[INFO] 页面标题:', title);
    console.log('');
    
    // 截取截图
    console.log('[STEP 5] 截取页面截图...');
    const screenshot = await page.screenshot({ 
      type: 'png',
      fullPage: true
    });
    
    // 保存截图
    const fs = require('fs');
    const screenshotPath = '/opt/ui-auto-test-platform/scripts/test-screenshot.png';
    fs.writeFileSync(screenshotPath, screenshot);
    console.log('[SUCCESS] 截图已保存:', screenshotPath);
    console.log('');
    
    // 输出 Base64 截图（用于调试）
    const base64 = screenshot.toString('base64');
    console.log('[INFO] 截图 Base64 长度:', base64.length);
    console.log('');
    
    console.log('='.repeat(60));
    console.log('✅ 测试成功！Midscene 成功调用 Chromium 打开页面');
    console.log('='.repeat(60));
    
  } catch (error) {
    console.log('');
    console.log('='.repeat(60));
    console.log('❌ 测试失败');
    console.log('='.repeat(60));
    console.log('[ERROR]', error.message);
    console.log('');
    if (error.stack) {
      console.log('[STACK]');
      console.log(error.stack);
    }
    process.exit(1);
  } finally {
    // 清理资源
    console.log('');
    console.log('[INFO] 关闭浏览器...');
    try {
      if (page) await page.close();
      if (context) await context.close();
      if (browser) await browser.close();
      console.log('[SUCCESS] 浏览器已关闭');
    } catch (e) {
      console.log('[WARN] 关闭浏览器时出错:', e.message);
    }
  }
}

// 运行测试
testMidscene();