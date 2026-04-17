#!/bin/bash
# 使用 Docker 运行 Node.js 24 执行 Playwright 任务
# 将宿主机的 node_modules 挂载到容器中

cd /opt/ui-auto-test-platform/scripts

# 运行 Docker 容器，挂载宿主机的 node_modules
docker run --rm \
  -v /opt/ui-auto-test-platform/scripts/executor.js:/app/executor.js \
  -v /opt/ui-auto-test-platform/scripts/node_modules:/app/node_modules \
  -v /opt/ui-auto-test-platform/scripts/package.json:/app/package.json \
  -v /opt/ui-auto-test-platform/scripts/package-lock.json:/app/package-lock.json \
  -v /opt/ui-auto-test-platform/scripts/.npmrc:/app/.npmrc \
  -w /app \
  -e MIDSCENE_MODEL_BASE_URL="http://10.254.250.177:31380/remote/llmops/MWH-REMOTE-SERVICE-d6tosi8m2aoor75k8ti0/compatible-mode/v1" \
  -e MIDSCENE_MODEL_API_KEY="apikey-zv5goRjwFYjtlrU55skz-VTXLqXmB5i3bwrBXrgjpCKI" \
  -e MIDSCENE_MODEL_NAME="qwen3.5-plus" \
  -e MIDSCENE_MODEL_FAMILY="qwen3.5" \
  node:24-alpine \
  node executor.js "$1"