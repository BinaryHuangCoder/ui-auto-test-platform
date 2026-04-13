#!/bin/bash
# UI 自动化测试平台 - 后端服务启动脚本
# 端口：8888

cd /opt/ui-auto-test-platform/backend
export JAVA_HOME=/usr/java/jdk1.8.0_172
export PATH=$JAVA_HOME/bin:$PATH

nohup java -jar target/*.jar --server.port=8888 > /var/log/ui-backend.log 2>&1 &

echo "后端服务已启动 (端口 8888)"
echo "日志：tail -f /var/log/ui-backend.log"
echo "停止：pkill -f 'ui-auto-test-platform'"
