#!/bin/bash
# 使用 Docker 运行 Node.js 24 执行 Playwright 任务
cd /opt/ui-auto-test-platform/scripts
docker run --rm -v $(pwd):/app -w /app node:24-alpine node executor.js "$1"