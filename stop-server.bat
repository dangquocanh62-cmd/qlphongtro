@echo off
chcp 65001 >nul
echo ===============================================================
echo Đang dừng toàn bộ server Docker (Database + Backend + Tunnel)...
docker compose down
echo.
echo Đã tắt toàn bộ dịch vụ an toàn!
echo ===============================================================
pause
