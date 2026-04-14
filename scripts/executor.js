#!/usr/bin/env node
/**
 * UI 自动化测试执行器 - 参考 /root/midscene/playwright-steps-executor.js
 * 版本：v1.31.0
 * 更新日期：2026-04-14
 * 
 * 功能说明:
 * - 支持多步骤批量执行（保持浏览器上下文）
 * - 使用 Midscene AI 执行所有步骤
 * - 每个步骤执行后截图
 * - 断言为空时显示"无"
 * - 从 Midscene debug logs 获取准确的 AI token 消耗
 * 
 * @author huangzhiyong081439
 * @since 2026-04-05
 */

// 加载本地环境变量
require('dotenv').config({ path: __dirname + '/.env' });

// 设置 Midscene 环境变量以启用 profile logs
process.env.DEBUG = 'midscene:ai:profile:stats,midscene:ai:profile:detail';

const { chromium } = require('playwright-core');
const { PlaywrightAgent } = require('@midscene/web/playwright');
const fs = require('fs');
const path = require('path');
const http = require('http');
const https = require('https');

let browser;
let page;
let agent;

// 存储捕获到的 AI profile logs
let aiProfileLogs = [];

// 捕获 stderr 输出以获取 Midscene AI profile logs
const originalStderrWrite = process.stderr.write;
process.stderr.write = (chunk) => {
  const str = chunk.toString();
  // 捕获 ai-profile-stats 和 ai-profile-detail 格式的日志
  if (str.includes('model,') || str.includes('model usage detail:')) {
    aiProfileLogs.push(str);
  }
  return originalStderrWrite.call(process.stderr, chunk);
};

/**
 * 从后端获取场景对应的模型配置
 * @param {string} scenarioCode - 场景编码
 * @returns {Promise<Object|null>} 模型配置
 */
async function getModelConfigFromBackend(scenarioCode) {
  return new Promise((resolve, reject) => {
    // 从后端获取场景配置
    const getScenarioOptions = {
      hostname: '127.0.0.1',
      port: 8088,
      path: `/api/model/scenarios/${scenarioCode}/model`,
      method: 'GET',
      headers: {
        'Content-Type': 'application/json'
      }
    };

    const scenarioReq = http.request(getScenarioOptions, (scenarioRes) => {
      let scenarioData = '';
      scenarioRes.on('data', (chunk) => {
        scenarioData += chunk;
      });
      scenarioRes.on('end', () => {
        try {
          const scenarioResult = JSON.parse(scenarioData);
          if (scenarioResult.code !== 200 || !scenarioResult.data) {
            console.error('[WARN] 无法从后端获取场景配置:', scenarioResult.message);
            resolve(null);
            return;
          }

          const model = scenarioResult.data;
          // 设置 Midscene 环境变量
          if (model && model.apiKey && model.modelUrl) {
            // Midscene 专用变量
            process.env.MIDSCENE_MODEL_BASE_URL = model.modelUrl;
            process.env.MIDSCENE_MODEL_API_KEY = model.apiKey;
            process.env.MIDSCENE_MODEL_NAME = model.modelName;
            // 根据模型名称或家族设置 MIDSCENE_MODEL_FAMILY
            if (model.modelFamily && model.modelFamily.toLowerCase().includes('doubao')) {
              process.env.MIDSCENE_MODEL_FAMILY = 'doubao-seed';
            } else if (model.modelName && model.modelName.toLowerCase().includes('doubao')) {
              process.env.MIDSCENE_MODEL_FAMILY = 'doubao-seed';
            } else {
              // 其他模型默认使用 gpt-4
              process.env.MIDSCENE_MODEL_FAMILY = 'gpt-4';
            }
            // 兼容变量
            process.env.OPENAI_API_KEY = model.apiKey;
            process.env.OPENAI_API_BASE = model.modelUrl;
            process.env.OPENAI_BASE_URL = model.modelUrl;
            console.error('[INFO] 使用模型配置:', model.modelName);
            console.error('[INFO] MIDSCENE_MODEL_BASE_URL:', model.modelUrl);
            console.error('[INFO] MIDSCENE_MODEL_API_KEY:', model.apiKey.substring(0, 8) + '...');
            console.error('[INFO] MIDSCENE_MODEL_NAME:', model.modelName);
            console.error('[INFO] MIDSCENE_MODEL_FAMILY:', process.env.MIDSCENE_MODEL_FAMILY);
            resolve(model);
          } else {
            console.error('[WARN] 场景未配置模型，使用默认配置');
            resolve(null);
          }
        } catch (e) {
          console.error('[ERROR] 解析场景配置响应失败:', e.message);
          resolve(null);
        }
      });
    });

    scenarioReq.on('error', (err) => {
      console.error('[ERROR] 请求场景配置失败:', err.message);
      resolve(null);
    });

    scenarioReq.end();
  });
}

/**
 * 调用OpenAI兼容的大模型获取融合后的步骤描述
 * @param {Object} model - 模型配置对象
 * @param {string} stepDescription - 原始步骤描述
 * @param {string} testData - 测试数据
 * @returns {Promise<string>} 融合后的完整步骤描述
 */
async function fuseStepData(model, stepDescription, testData) {
  return new Promise((resolve, reject) => {
    // 确保URL以 /chat/completions 结尾
    let url = model.modelUrl;
    console.error('[DEBUG] 原始模型URL:', url);
    if (!url.endsWith('/chat/completions')) {
      if (!url.endsWith('/')) {
        url += '/';
      }
      url += 'chat/completions';
    }
    console.error('[DEBUG] 最终请求URL:', url);

    const requestBody = JSON.stringify({
      model: model.modelName,
      messages: [
        {
          role: 'system',
          content: '你是一个UI自动化测试步骤融合助手。请将用户提供的步骤描述和测试数据融合成一个完整、可执行的UI自动化测试步骤描述。返回的结果应该只包含融合后的步骤描述，不要有其他说明文字。'
        },
        {
          role: 'user',
          content: `步骤描述：${stepDescription}\n测试数据：${testData}`
        }
      ],
      max_tokens: 500,
      temperature: 0.7
    });

    const options = {
      hostname: '127.0.0.1',
      port: 8088,
      path: url, // Wait no, wait, if modelUrl is a full URL, we need to parse it! Let's parse it properly!
      // Wait let's parse the modelUrl to get hostname, port, path
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${model.apiKey}`
      }
    };

    let urlObj;
    // Parse the model URL to get hostname, port, path
    try {
      urlObj = new URL(url);
      options.hostname = urlObj.hostname;
      options.port = urlObj.port || (urlObj.protocol === 'https:' ? 443 : 80);
      options.path = urlObj.pathname + urlObj.search;
    } catch (e) {
      console.error('[ERROR] 解析模型URL失败:', e.message);
      reject(e);
      return;
    }

    // 使用 http 或 https 模块根据协议
    const client = urlObj.protocol === 'https:' ? https : http;
    const req = client.request(options, (res) => {
      console.error('[DEBUG] 模型响应状态码:', res.statusCode);
      console.error('[DEBUG] 模型响应头:', res.headers);
      let data = '';
      res.on('data', (chunk) => {
        data += chunk;
        console.error('[DEBUG] 收到数据块:', chunk.toString());
      });
      res.on('end', () => {
        console.error('[DEBUG] 完整响应数据:', data);
        try {
          const result = JSON.parse(data);
          if (result.choices && result.choices.length > 0 && result.choices[0].message) {
            const fusedStep = result.choices[0].message.content.trim();
            console.error('[INFO] 步骤数据融合完成，融合后的步骤:', fusedStep);
            resolve(fusedStep);
          } else {
            console.error('[ERROR] 模型响应格式不正确:', data);
            reject(new Error('模型响应格式不正确'));
          }
        } catch (e) {
          console.error('[ERROR] 解析模型响应失败:', e.message, '响应数据:', data);
          reject(e);
        }
      });
    });

    req.on('error', (err) => {
      console.error('[ERROR] 调用步骤数据融合模型失败:', err.message);
      reject(err);
    });

    req.write(requestBody);
    req.end();
  });
}

/**
 * 从捕获的 AI profile logs 中提取总 token 消耗
 * @returns {number} 总 token 消耗
 */
function extractTotalTokensFromLogs() {
  let totalTokens = 0;
  for (const log of aiProfileLogs) {
    // 尝试从 ai-profile-stats 格式提取: "total-tokens, N"
    const statsMatch = log.match(/total-tokens,\s*(\d+)/);
    if (statsMatch) {
      totalTokens += parseInt(statsMatch[1], 10);
      continue;
    }
    // 尝试从 ai-profile-detail 格式提取 JSON: {"total_tokens": N}
    const detailMatch = log.match(/model usage detail: (\{.+\})/);
    if (detailMatch) {
      try {
        const detail = JSON.parse(detailMatch[1]);
        if (detail.total_tokens) {
          totalTokens += detail.total_tokens;
        }
      } catch (e) {
        console.error('[WARN] 解析 AI profile detail log 失败:', e.message);
      }
    }
  }
  return totalTokens;
}

/**
 * 初始化浏览器实例并创建页面上下文
 * 
 * 功能描述：
 * - 检查浏览器是否已初始化，避免重复创建
 * - 使用指定的Chromium可执行路径启动浏览器
 * - 配置浏览器参数支持中文显示
 * - 创建浏览器上下文，设置中文区域和时区
 * - 注入初始化脚本强制页面使用UTF-8编码
 * 
 * @method initBrowser
 * @async
 * @returns {Promise<void>} 无返回值，浏览器实例存储在全局变量中
 * @throws {Error} 浏览器初始化失败时抛出异常
 * 
 * @since 2026-04-05
 * @author huangzhiyong081439
 */
async function initBrowser() {
  if (browser) {
    return;
  }
  
  try {
    const browsersPath = process.env.PLAYWRIGHT_BROWSERS_PATH || '/root/.cache/ms-playwright';
    const executablePath = `${browsersPath}/chromium-1208/chrome-linux64/chrome`;
    
    let launchOptions = { 
      headless: true,
      args: [
        '--no-sandbox',
        '--disable-setuid-sandbox',
        '--disable-dev-shm-usage',
        '--disable-gpu',
        '--lang=zh-CN',
        '--accept-lang=zh-CN,zh',
        '--charset=utf-8',
        '--disable-extensions',
        '--disable-default-apps',
        '--disable-sync',
        '--metrics-recording-only'
      ]
    };
    
    if (fs.existsSync(executablePath)) {
      launchOptions.executablePath = executablePath;
      console.error('[INFO] 使用指定浏览器:', executablePath);
    }
    
    browser = await chromium.launch(launchOptions);
    const context = await browser.newContext({
      viewport: { width: 1920, height: 1080 },
      locale: 'zh-CN',
      timezoneId: 'Asia/Shanghai',
      permissions: ['geolocation', 'notifications'],
      // 强制请求使用UTF-8编码
      extraHTTPHeaders: {
        'Accept-Charset': 'utf-8'
      }
    });
    page = await context.newPage();
    
    // 处理浏览器弹出的对话框（alert/confirm/prompt）
    // 参考 /root/midscene/playwright-steps-executor.js
    page.on('dialog', async (dialog) => {
      console.error('[DIALOG] 检测到对话框:', dialog.type(), '-', dialog.message());
      try {
        if (dialog.type() === 'confirm' || dialog.type() === 'prompt') {
          // 确认框和输入框默认接受（点击确定）
          await dialog.accept();
          console.error('[DIALOG] 对话框已接受:', dialog.message());
        } else if (dialog.type() === 'alert') {
          // 警告框点击确定关闭
          await dialog.accept();
          console.error('[DIALOG] 警告框已关闭:', dialog.message());
        }
      } catch (e) {
        console.error('[DIALOG] 处理对话框失败:', e.message);
      }
    });
    
    // 设置页面级初始化脚本（在DOM创建前执行）
    await page.addInitScript(() => {
      // 强制所有文本内容使用UTF-8解码
      if (!window._charsetForced) {
        window._charsetForced = true;
        
        // 强制修改document的字符集属性
        Object.defineProperty(document, 'characterSet', {
          get: function() { return 'UTF-8'; },
          configurable: true
        });
        
        // 监听DOMContentLoaded，确保所有内容使用UTF-8
        document.addEventListener('DOMContentLoaded', function() {
          // 强制修改meta charset
          let charsetMeta = document.querySelector('meta[charset]');
          if (!charsetMeta) {
            charsetMeta = document.createElement('meta');
            charsetMeta.setAttribute('charset', 'UTF-8');
            if (document.head) {
              document.head.insertBefore(charsetMeta, document.head.firstChild);
            }
          } else {
            charsetMeta.setAttribute('charset', 'UTF-8');
          }
          
          // 如果有content-type的meta，添加charset
          let contentTypeMeta = document.querySelector('meta[http-equiv="Content-Type"]');
          if (contentTypeMeta) {
            const content = contentTypeMeta.getAttribute('content') || '';
            if (!content.includes('charset')) {
              contentTypeMeta.setAttribute('content', content + '; charset=UTF-8');
            }
          }
          
          // 设置HTML根元素的lang和charset
          if (document.documentElement) {
            document.documentElement.setAttribute('lang', 'zh-CN');
            document.documentElement.setAttribute('xml:lang', 'zh-CN');
          }
          
          // 强制body使用UTF-8
          document.body.style.characterEncoding = 'UTF-8';
        });
        
        // 立即执行的强制编码
        try {
          const meta = document.createElement('meta');
          meta.charset = 'UTF-8';
          if (document.head && document.head.firstChild) {
            document.head.insertBefore(meta, document.head.firstChild);
          } else if (document.head) {
            document.head.appendChild(meta);
          }
        } catch(e) {}
      }
    });
    
    // 初始化 Midscene Agent
    agent = new PlaywrightAgent(page);
    
    console.error('[INFO] 浏览器初始化成功');
    
  } catch (error) {
    console.error('[ERROR] 浏览器初始化失败:', error.message);
    throw error;
  }
}

/**
 * 等待页面完全加载并验证内容
 * 
 * 功能描述：
 * - 等待DOM内容加载完成
 * - 等待网络请求完成（networkidle状态）
 * - 额外等待确保动态内容渲染完成
 * - 检查页面内容是否足够（防止空白页面）
 * - 内容不足时自动延长等待时间
 * 
 * @method waitForPageReady
 * @async
 * @returns {Promise<void>} 无返回值
 * @since 2026-04-05
 * @author huangzhiyong081439
 */
async function waitForPageReady() {
  try {
    await page.waitForLoadState('domcontentloaded', { timeout: 15000 });
  } catch (e) {
    console.error('[WARN] 等待 DOM 加载超时:', e.message);
  }
  
  try {
    await page.waitForLoadState('networkidle', { timeout: 30000 });
  } catch (e) {
    console.error('[WARN] 等待网络空闲超时:', e.message);
  }
  
  await page.waitForTimeout(3000);
  
  const pageContent = await page.evaluate(() => document.body.innerText.length);
  const pageHtml = await page.evaluate(() => document.body.innerHTML.length);
  
  console.error('[DEBUG] 页面文本长度:', pageContent);
  console.error('[DEBUG] 页面 HTML 长度:', pageHtml);
  
  if (pageContent < 50 || pageHtml < 500) {
    console.error('[WARN] 页面内容较少，继续等待...');
    await page.waitForTimeout(5000);
  }
}

/**
 * 使用AI进行断言检查
 * 
 * 功能描述：
 * - 将断言描述发送给AI进行分析
 * - AI判断当前页面是否满足断言条件
 * - 解析AI返回的响应，判断通过/失败
 * - 提取判断原因作为实际结果
 * 
 * @method checkAssertion
 * @async
 * @param {string} assertionDescription - 断言描述内容，描述期望的页面状态
 * @returns {Promise<Object>} 返回断言检查结果对象，包含以下字段：
 *   - pass: boolean - 断言是否通过
 *   - expected: string - 预期结果描述
 *   - actual: string - 实际结果描述
 *   - message: string - 检查结果消息
 *   - error: Error|null - 异常信息（如果有）
 * @since 2026-04-05
 * @author huangzhiyong081439
 */
async function checkAssertion(assertionDescription) {
  let result = {
    pass: false,
    expected: '',
    actual: '',
    message: '',
    error: null,
    usage: {} // AI调用token消耗统计
  };
  
  try {
    result.expected = `AI 判断：${assertionDescription}`;
    
    const pageUrl = page.url();
    const pageTitle = await page.title();
    result.actual = `当前页面：${pageTitle} (${pageUrl})`;
    
    const prompt = `请判断当前页面是否满足：${assertionDescription}。如果满足，请回复"满足"；否则，请回复"不满足"。并简要说明原因。`;
    
    // 重置 AI profile logs 以捕获当前断言的 token 消耗
    aiProfileLogs = [];
    
    const aiResponse = await agent.ai(prompt);
    const responseText = aiResponse.toString();
    
    // 从捕获的 AI profile logs 中提取 token 消耗
    const totalTokens = extractTotalTokensFromLogs();
    result.usage = {
      total_tokens: totalTokens
    };
    console.error(`[DEBUG] 断言AI token消耗：${totalTokens}`);
    
    if (responseText.includes('满足') && !responseText.includes('不满足')) {
      result.pass = true;
      result.message = 'AI 判断满足条件';
      const reasonMatch = responseText.match(/原因 [:：](.+)/);
      result.actual = reasonMatch ? `满足条件。${reasonMatch[1].substring(0, 100)}` : '满足条件';
    } else if (responseText.includes('不满足')) {
      result.pass = false;
      result.message = 'AI 判断不满足条件';
      const reasonMatch = responseText.match(/原因 [:：](.+)/);
      result.actual = reasonMatch ? `不满足条件。${reasonMatch[1].substring(0, 100)}` : '不满足条件';
    } else {
      result.pass = false;
      result.message = 'AI 响应不明确';
      result.actual = `AI 响应：${responseText.substring(0, 100)}...`;
    }
    
    return result;
  } catch (error) {
    result.error = error;
    result.message = 'AI 断言检查异常';
    const errorMessage = error.message ? 
      error.message.split('\n')[0].replace(/^Task failed: /, '') : 
      '未知错误';
    result.actual = `AI 检查出错：${errorMessage}`;
    return result;
  }
}

/**
 * 批量执行自动化测试步骤
 * 
 * 功能描述：
 * - 初始化浏览器（如未初始化）
 * - 逐个执行测试步骤
 * - 每个步骤执行操作后等待页面加载
 * - 步骤执行后自动截图保存
 * - 如果提供了断言，执行AI断言检查
 * - 收集每个步骤的执行结果
 * - 执行完成后关闭浏览器
 * 
 * @method executeSteps
 * @async
 * @param {Array<Object>} steps - 测试步骤数组，每个步骤包含：
 *   - stepNumber: number - 步骤序号
 *   - action: string - 操作指令描述
 *   - assertion: string - 断言描述（可选）
 * @returns {Promise<Array<Object>>} 返回步骤执行结果数组，每个结果包含：
 *   - stepNumber: number - 步骤序号
 *   - action: string - 执行的操作
 *   - executionStatus: string - 执行状态（成功/失败）
 *   - assertionStatus: string - 断言状态（通过/失败/无）
 *   - message: string - 执行消息
 *   - screenshot: string - 截图文件路径
 *   - screenshotBase64: string - 截图Base64编码或文件路径
 *   - executionTime: number - 执行耗时（毫秒）
 * @since 2026-04-05
 * @author huangzhiyong081439
 */
async function executeSteps(steps) {
  const results = [];
  const screenshotDir = '/home/administrator/.openclaw/workspace/ui-auto-test-platform/screenshots';
  
  if (!fs.existsSync(screenshotDir)) {
    fs.mkdirSync(screenshotDir, { recursive: true });
  }
  
  try {
    // 获取图像断言检查场景的模型配置
    await getModelConfigFromBackend('image_assertion');
    // 获取步骤数据融合场景的模型配置
    const stepFusionModel = await getModelConfigFromBackend('step_fusion');
    
    await initBrowser();
    
    console.error('');
    console.error('='.repeat(60));
    console.error(`🚀 开始执行自动化流程，共 ${steps.length} 个步骤`);
    console.error('='.repeat(60));
    
    for (let i = 0; i < steps.length; i++) {
      const step = steps[i];
      const stepNumber = step.stepNumber || i + 1;
      let action = step.action;
      const assertion = step.assertion || '';
      const testData = step.testData || '';
      
      // 如果有测试数据且配置了步骤数据融合模型，则进行数据融合
      if (testData && testData.trim() && stepFusionModel) {
        console.error(`🔄 步骤 ${stepNumber} 开始数据融合...`);
        console.error(`   原始步骤: ${action}`);
        console.error(`   测试数据: ${testData}`);
        try {
          action = await fuseStepData(stepFusionModel, action, testData);
        } catch (e) {
          console.error(`[WARN] 步骤数据融合失败，使用原始步骤: ${e.message}`);
        }
      }
      
      console.error('');
      console.error(`📊 步骤进度：[${stepNumber}/${steps.length}]`);
      console.error(`🔹 执行步骤 ${stepNumber}: "${action}"`);
      console.error(`🔍 断言检查：${assertion.trim() ? '"' + assertion + '"' : '无'}`);
      console.error(`⏰ 开始时间：${new Date().toLocaleTimeString()}`);
      
      let stepResult = {
        stepNumber: stepNumber,
        action: action,
        assertion: assertion,
        executionStatus: '未知',
        assertionStatus: '未检查',
        assertionDetails: {
          pass: false,
          expected: '',
          actual: '',
          message: '',
          error: null
        },
        message: '',
        screenshot: '',
        screenshotBase64: '',
        executionTime: 0,
        startTime: Date.now(),
        aiTokenUsed: 0  // AI token消耗总计
      };
      
      // 执行主操作
      try {
        const actionStartTime = Date.now();
        console.error(`⚡ 正在执行步骤 ${stepNumber} 的操作...`);
        
        // 重置 AI profile logs 以捕获当前操作的 token 消耗
        aiProfileLogs = [];
        
        const actionAiResponse = await agent.ai(action);
        
        // 从捕获的 AI profile logs 中提取总 token 消耗
        const actionTokens = extractTotalTokensFromLogs();
        stepResult.aiTokenUsed += actionTokens;
        console.error(`[DEBUG] 主操作AI token消耗：${actionTokens}`);
        
        const actionEndTime = Date.now();
        stepResult.executionStatus = '成功';
        stepResult.executionTime = actionEndTime - actionStartTime;
        stepResult.executionTimeStr = String(stepResult.executionTime);  // 字符串格式，确保后端能解析
        stepResult.message = `操作指令执行成功，耗时：${stepResult.executionTime}ms`;
        console.error(`✅ 步骤 ${stepNumber} 操作完成，耗时：${stepResult.executionTime}ms`);
        
        // 等待页面加载
        console.error(`⏳ 等待页面加载...`);
        await waitForPageReady();
        
        // 截图
        const timestamp = Date.now();
        const screenshotPath = path.join(screenshotDir, `step${stepNumber}_${timestamp}.png`);
        console.error(`📸 正在为步骤 ${stepNumber} 截图...`);
        
        const screenshot = await page.screenshot({ 
          path: screenshotPath, 
          fullPage: false,
          animations: 'disabled'
        });
        
        stepResult.screenshot = screenshotPath;
        stepResult.screenshotBase64 = 'file:' + screenshotPath; console.error('[DEBUG] 设置screenshotBase64为:', stepResult.screenshotBase64); // 保存文件路径而不是完整base64
        console.error(`📁 截图已保存：${screenshotPath}`);
        
        // 执行断言检查（如果提供了断言内容）
        if (assertion && assertion.trim() !== '' && 
            assertion.trim() !== 'null' && 
            assertion.trim() !== 'undefined') {
          console.error(`🔍 步骤 ${stepNumber} 开始断言检查："${assertion}"`);
          try {
            const assertionStartTime = Date.now();
            const assertionResult = await checkAssertion(assertion);
            const assertionEndTime = Date.now();
            const assertionTime = assertionEndTime - assertionStartTime;
            
            stepResult.assertionDetails = assertionResult;
            // 调试：打印完整的断言响应结构
            console.error(`[DEBUG] 断言AI响应类型: ${typeof assertionResult}`);
            console.error(`[DEBUG] 断言AI响应完整结构:`, JSON.stringify(assertionResult, null, 2));
            // 从捕获的 AI profile logs 中提取断言的 token 消耗
            const assertionTokens = extractTotalTokensFromLogs();
            stepResult.aiTokenUsed += assertionTokens || 0;
            console.error(`[DEBUG] 断言AI token消耗: ${assertionTokens}, 单步总计: ${stepResult.aiTokenUsed}`);
            
            if (assertionResult.pass) {
              stepResult.assertionStatus = '通过';
              stepResult.message = `操作执行成功 (${stepResult.executionTime}ms)。断言检查通过 (${assertionTime}ms): 预期"${assertionResult.expected || assertion}"，实际"${assertionResult.actual}"，满足判断条件`;
              console.error(`✅ 步骤 ${stepNumber} 断言通过，耗时：${assertionTime}ms`);
            } else {
              stepResult.assertionStatus = '失败';
              stepResult.message = `操作执行成功 (${stepResult.executionTime}ms)。断言检查失败 (${assertionTime}ms): 预期"${assertionResult.expected || assertion}"，实际"${assertionResult.actual}"，不满足判断条件`;
              console.error(`❌ 步骤 ${stepNumber} 断言未通过，耗时：${assertionTime}ms`);
            }
          } catch (assertionError) {
            stepResult.assertionStatus = '异常';
            stepResult.assertionDetails = {
              pass: false,
              expected: assertion,
              actual: '断言检查过程出错',
              message: assertionError.message,
              error: assertionError.message
            };
            stepResult.message = `操作执行成功，但断言检查异常：${assertionError.message}`;
            console.error(`❌ 步骤 ${stepNumber} 断言检查异常：${assertionError.message}`);
          }
        } else {
          // 没有断言
          console.error(`🔍 步骤 ${stepNumber} 无断言要求，跳过检查`);
          stepResult.assertionStatus = '无';
          stepResult.assertionDetails = {
            pass: true,
            expected: '',
            actual: '无断言要求',
            message: '未提供断言内容，跳过检查',
            error: null
          };
        }
        
      } catch (stepError) {
        stepResult.executionStatus = '失败';
        stepResult.message = `步骤执行失败：${stepError.message}`;
        console.error(`❌ 步骤 ${stepNumber} 执行失败：${stepError.message}`);
        
        // 失败时也要截图
        try {
          const timestamp = Date.now();
          const screenshotPath = path.join(screenshotDir, `step${stepNumber}_error_${timestamp}.png`);
          const screenshot = await page.screenshot({ 
            path: screenshotPath, 
            type: 'png'
          });
          stepResult.screenshot = screenshotPath;
          stepResult.screenshotBase64 = 'file:' + screenshotPath; console.error('[DEBUG] 设置screenshotBase64为:', stepResult.screenshotBase64); // 保存文件路径而不是完整base64
          console.error(`📁 错误截图已保存：${screenshotPath}`);
        } catch (e) {
          console.error('[WARN] 失败截图也失败了:', e.message);
        }
      }
      
      stepResult.endTime = Date.now();
      results.push(stepResult);
    }
    
    console.error('');
    console.error('='.repeat(60));
    console.error('✅ 所有步骤执行完成');
    console.error('='.repeat(60));
    
  } catch (error) {
    console.error('[ERROR] 批量执行失败:', error.message);
  } finally {
    // 关闭浏览器
    try {
      if (page) await page.close();
      if (browser) await browser.close();
      console.error('[INFO] 浏览器已关闭');
    } catch (e) {}
  }
  
  return results;
}

// 主程序
const args = process.argv.slice(2);

if (args.length < 1) {
  console.error('用法：node executor.js \'[{"action":"...","assertion":"..."}]\'');
  console.error('示例：');
  console.error('  node executor.js \'[{"action":"打开 http://example.com","assertion":""}]\'');
  process.exit(1);
}

// 解析步骤 JSON 数组
let steps;
try {
  steps = JSON.parse(args[0]);
  if (!Array.isArray(steps)) {
    // 如果是单个对象，转为数组
    steps = [steps];
  }
} catch (e) {
  console.error('JSON 解析失败:', e.message);
  process.exit(1);
}

// 执行步骤
executeSteps(steps)
  .then(results => {
    // 调试：打印每个结果对象的字段
    if (results && results.length > 0) {
      console.error('[DEBUG] results包含的字段:', Object.keys(results[0]).join(', '));
      console.error('[DEBUG] screenshotBase64值:', results[0].screenshotBase64);
    }
    console.log(JSON.stringify({ results }));
    process.exit(0);
  })
  .catch(error => {
    console.error(JSON.stringify({ error: error.message }));
    process.exit(1);
  });
