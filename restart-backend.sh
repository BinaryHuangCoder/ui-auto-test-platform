#!/bin/bash
cd /home/administrator/.openclaw/workspace/ui-auto-test-platform/backend

echo "停止所有后端进程..."
pkill -f "ui-auto-test-platform-1.0.0.jar" 2>/dev/null
sleep 8

echo "检查是否还有后端进程..."
if ps aux | grep -q "ui-auto-test-platform-1.0.0.jar" | grep -v grep; then
    echo "还有后端进程在运行，强制停止..."
    ps aux | grep "ui-auto-test-platform-1.0.0.jar" | grep -v grep | awk '{print $2}' | xargs kill -9 2>/dev/null
    sleep 5
fi

echo "删除旧的后端日志..."
rm -f ../backend.log

echo "启动后端..."
nohup java -jar target/ui-auto-test-platform-1.0.0.jar --server.port=8088 > ../backend.log 2>&1 &
BACKEND_PID=$!
echo "后端进程ID: $BACKEND_PID"

echo "等待25秒..."
sleep 25

echo "检查后端进程..."
ps aux | grep "ui-auto-test-platform-1.0.0.jar" | grep -v grep

echo "检查后端日志..."
ls -la ../backend.log
