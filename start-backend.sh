#!/bin/bash
cd /home/administrator/.openclaw/workspace/ui-auto-test-platform/backend
rm -f ../backend.log
echo "启动后端..."
java -jar target/ui-auto-test-platform-1.0.0.jar --server.port=8088 > ../backend.log 2>&1 &
BACKEND_PID=$!
echo "后端进程ID: $BACKEND_PID"
sleep 10
echo "检查后端状态..."
ps aux | grep $BACKEND_PID | grep -v grep
