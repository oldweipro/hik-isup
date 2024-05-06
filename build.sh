#!/bin/bash

# 打印日志
echo "🗑️ 正在移除旧的 Docker 镜像..."

# 删除旧的 Docker 镜像
docker rmi oldweipro/hik-isup:latest

# 打印日志
echo "🐳 正在构建新的 Docker 镜像..."

# 构建 Docker 镜像
docker build -t oldweipro/hik-isup:latest .

# 打印日志
echo "💾 正在保存 Docker 镜像到文件..."

# 保存 Docker 镜像到文件
docker save -o hik-isup.tar oldweipro/hik-isup:latest

# 打印日志
echo "🎉 构建过程已成功完成!"
