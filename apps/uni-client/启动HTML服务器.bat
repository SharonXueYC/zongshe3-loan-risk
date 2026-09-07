@echo off
echo 正在启动HTML文件服务器...
echo.
echo 服务器启动后，请在浏览器中访问：
echo http://localhost:8080/login-prototype.html
echo http://localhost:8080/main-home.html
echo http://localhost:8080/loan-home.html
echo.
echo 按 Ctrl+C 可以停止服务器
echo.

cd /d %~dp0

:: 尝试使用Python的http.server
python --version >nul 2>&1
if %errorlevel% == 0 (
    echo 使用Python启动服务器...
    python -m http.server 8080
    goto :end
)

:: 尝试使用Node.js的http-server
where npx >nul 2>&1
if %errorlevel% == 0 (
    echo 使用Node.js启动服务器...
    npx http-server -p 8080 -o
    goto :end
)

:: 如果都没有，提示用户
echo 错误：未找到Python或Node.js
echo 请安装Python或Node.js，或者直接双击HTML文件打开
pause

:end



