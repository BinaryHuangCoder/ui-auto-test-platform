#!/bin/bash
# 使用宿主机的 Node.js 和 Playwright 直接执行任务
# 不再使用 Docker，因为需要访问宿主机的浏览器

cd /opt/ui-auto-test-platform/scripts

# 设置 PATH 以包含 node 全局模块
export PATH=/usr/local/node18/bin:$PATH

# 设置 PLAYWRIGHT_BROWSERS_PATH 指向宿主机浏览器目录
export PLAYWRIGHT_BROWSERS_PATH=/root/.cache/ms-playwright

# 执行任务
node executor.js "$1"