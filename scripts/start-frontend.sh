#!/bin/bash
cd /opt/ui-auto-test-platform/frontend/dist
export NODE_HOME=/usr/local/node/node-v16.18.1-linux-x64
export PATH=$NODE_HOME/bin:$PATH
nohup npx http-server -p 3000 > /var/log/ui-frontend.log 2>&1 &
echo "前端服务已启动 http-server 端口 3000"
