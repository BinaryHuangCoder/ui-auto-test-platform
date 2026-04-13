#!/bin/bash
# UI 自动化测试平台 - 重启所有服务

echo "停止服务..."
/opt/ui-auto-test-platform/scripts/stop-all.sh

echo "等待 2 秒..."
sleep 2

echo "启动后端..."
/opt/ui-auto-test-platform/scripts/start-backend.sh

echo "启动前端..."
/opt/ui-auto-test-platform/scripts/start-frontend.sh

echo "所有服务已重启"
