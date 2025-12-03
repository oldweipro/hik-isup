# 打印日志
Write-Host "🗑️ 正在移除旧的 Docker 镜像..." -ForegroundColor Yellow

# 删除旧的 Docker 镜像（使用 -Force 跳过确认，-ErrorAction SilentlyContinue 避免报错）
docker rmi oldweipro/hik-isup:latest 2>$null

# 打印日志
Write-Host "🐳 正在构建新的 Docker 镜像..." -ForegroundColor Cyan

# 构建 Docker 镜像
docker build -t oldweipro/hik-isup:latest .

# 检查上一步是否成功
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ 镜像构建失败！" -ForegroundColor Red
    exit $LASTEXITCODE
}

# 打印日志
Write-Host "💾 正在保存 Docker 镜像到文件..." -ForegroundColor Magenta

# 保存 Docker 镜像到文件
docker save -o hik-isup.tar oldweipro/hik-isup:latest

# 检查是否成功保存
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ 保存镜像失败！" -ForegroundColor Red
    exit $LASTEXITCODE
}

# 打印日志
Write-Host "🎉 构建过程已成功完成!" -ForegroundColor Green