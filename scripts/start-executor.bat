@echo off
echo ========================================
echo Windows 执行器启动器
echo ========================================
echo.
echo 正在启动 Chrome（带远程调试）...
start "" "C:\Program Files\Google\Chrome\Application\chrome.exe" --remote-debugging-port=9222 --no-first-run --no-default-browser-check
echo.
echo Chrome 已启动，等待 3 秒...
timeout /t 3 /nobreak
echo.
echo 正在启动执行器服务...
echo.
echo 执行器服务已启动！
echo   - 健康检查: http://localhost:8889/health
echo   - 执行接口: POST http://localhost:8889/execute
echo.
echo 按 Ctrl+C 停止服务
echo ========================================

node executor-server.js

pause