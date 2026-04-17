#!/bin/bash
# UI 自动化测试平台 - 停止所有服务

pkill -f 'ui-auto-test-platform' 2>/dev/null || true
pkill -f 'http-server' 2>/dev/null || true

if command -v nginx &> /dev/null; then
    nginx -s stop 2>/dev/null || true
fi

echo "所有服务已停止"
