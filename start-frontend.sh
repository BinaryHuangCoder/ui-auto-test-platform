#!/bin/bash
cd /home/administrator/.openclaw/workspace/ui-auto-test-platform/frontend
echo "停止旧的前端进程..."
pkill -f "node server.js" 2>/dev/null
sleep 3
echo "启动前端服务器..."
rm -f ../frontend.log
node server.js > ../frontend.log 2>&1 &
echo "前端服务器已启动"
sleep 5
echo "检查前端进程..."
ps aux | grep "node server.js" | grep -v grep
