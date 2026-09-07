# HTML文件服务器启动脚本
Write-Host "正在启动HTML文件服务器..." -ForegroundColor Green
Write-Host ""
Write-Host "服务器启动后，请在浏览器中访问：" -ForegroundColor Yellow
Write-Host "http://localhost:8080/login-prototype.html" -ForegroundColor Cyan
Write-Host "http://localhost:8080/main-home.html" -ForegroundColor Cyan
Write-Host "http://localhost:8080/loan-home.html" -ForegroundColor Cyan
Write-Host ""
Write-Host "按 Ctrl+C 可以停止服务器" -ForegroundColor Yellow
Write-Host ""

# 切换到脚本所在目录
Set-Location $PSScriptRoot

# 尝试使用Python的http.server
try {
    $pythonVersion = python --version 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host "使用Python启动服务器..." -ForegroundColor Green
        python -m http.server 8080
        exit
    }
} catch {
    # Python不可用，继续尝试其他方法
}

# 尝试使用Node.js的npx http-server
try {
    $npxCheck = Get-Command npx -ErrorAction SilentlyContinue
    if ($npxCheck) {
        Write-Host "使用Node.js启动服务器..." -ForegroundColor Green
        npx http-server -p 8080 -o
        exit
    }
} catch {
    # npx不可用，继续尝试其他方法
}

# 如果都没有，提示用户
Write-Host "错误：未找到Python或Node.js" -ForegroundColor Red
Write-Host "请安装Python或Node.js，或者直接双击HTML文件打开" -ForegroundColor Yellow
Write-Host ""
Write-Host "或者使用以下方法：" -ForegroundColor Yellow
Write-Host "1. 直接双击HTML文件在浏览器中打开" -ForegroundColor Cyan
Write-Host "2. 在浏览器地址栏输入：file:///D:/ZongShe/uni-preset-vue-vite/文件名.html" -ForegroundColor Cyan
Read-Host "按Enter键退出"



