@echo off
chcp 65001 >nul
echo ===============================================================
echo   HỆ THỐNG QUẢN LÝ PHÒNG TRỌ - DOCKER + CLOUDFLARE TUNNEL
echo ===============================================================
echo [1/3] Đang khởi động Database, Backend và Cloudflare Tunnel...
docker compose up -d --build

echo.
echo [2/3] Chờ Cloudflare tạo đường link công khai (khoảng 5-8 giây)...
timeout /t 6 /nobreak >nul

echo.
echo ===============================================================
echo [3/3] ĐƯỜNG LINK TRUY CẬP INTERNET CỦA BẠN LÀ:
docker compose logs tunnel | findstr "trycloudflare.com"
echo ===============================================================
echo Copy đường link dạng: https://xxxx.trycloudflare.com/api/
echo để dùng cho App Android trên bất kỳ thiết bị nào (4G, Wifi)!
echo.
pause
