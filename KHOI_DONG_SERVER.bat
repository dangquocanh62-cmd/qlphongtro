@echo off
chcp 65001 >nul
title HỆ THỐNG QUẢN LÝ PHÒNG TRỌ - SERVER
color 0B

echo ======================================================================
echo       HỆ THỐNG QUẢN LÝ PHÒNG TRỌ - KHỞI ĐỘNG SERVER
echo ======================================================================
echo.

echo [1/3] Đang khởi động cơ sở dữ liệu PostgreSQL...
wsl -d Ubuntu -u root service postgresql start >nul 2>&1
echo       * PostgreSQL đã sẵn sàng trên cổng 5432.
echo.

echo [2/3] Thiết lập cầu nối cổng 3000 cho Máy ảo Android...
if exist "%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe" (
    "%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe" reverse tcp:3000 tcp:3000 >nul 2>&1
    echo       * Đã liên kết cổng App Android 3000 thành công.
) else (
    echo       * Không tìm thấy adb.exe, bỏ qua bước này.
)
echo.

echo [3/3] Đang khởi động Backend API Server (Node.js)...
echo ======================================================================
echo   LƯU Ý: Giữ cửa sổ này mở trong khi sử dụng App!
echo   Khi không dùng nữa, bạn chỉ cần đóng cửa sổ này lại.
echo ======================================================================
echo.

cd /d "%~dp0backend"
node src/server.js

pause
