# 网络连接诊断脚本
Write-Host "=== 网络连接诊断 ===" -ForegroundColor Green

# 1. 检查端口监听状态
Write-Host "`n1. 检查8080端口监听状态:" -ForegroundColor Yellow
netstat -ano | findstr :8080

# 2. 检查防火墙规则
Write-Host "`n2. 检查Windows防火墙规则:" -ForegroundColor Yellow
netsh advfirewall firewall show rule name="Spring Boot 8080" | Select-String -Pattern "已启用|Enabled|本地端口|LocalPort"

# 3. 测试本地连接
Write-Host "`n3. 测试本地连接:" -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/product-management.html" -Method Head -UseBasicParsing -TimeoutSec 5
    Write-Host "✓ 本地连接成功: $($response.StatusCode)" -ForegroundColor Green
} catch {
    Write-Host "✗ 本地连接失败: $($_.Exception.Message)" -ForegroundColor Red
}

# 4. 获取本机IP地址
Write-Host "`n4. 本机IP地址:" -ForegroundColor Yellow
Get-NetIPAddress -AddressFamily IPv4 | Where-Object {$_.IPAddress -notlike "127.*" -and $_.IPAddress -notlike "169.254.*"} | Select-Object IPAddress, InterfaceAlias

# 5. 检查云服务商安全组提示
Write-Host "`n=== 重要提示 ===" -ForegroundColor Red
Write-Host "如果外部仍无法访问，请检查云服务商安全组设置:" -ForegroundColor Yellow
Write-Host "1. 登录云服务商控制台（阿里云/腾讯云/AWS等）" -ForegroundColor Cyan
Write-Host "2. 找到服务器实例 -> 安全组/防火墙规则" -ForegroundColor Cyan
Write-Host "3. 添加入站规则: 端口8080, 协议TCP, 来源0.0.0.0/0" -ForegroundColor Cyan
Write-Host "4. 保存规则后等待1-2分钟生效" -ForegroundColor Cyan

