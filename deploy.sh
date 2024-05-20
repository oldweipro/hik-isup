#!/bin/bash

# 停止容器
echo "🛑 停止容器..."
docker stop hik-isup

# 移除容器
echo "🗑️ 移除容器..."
docker rm hik-isup

# 删除旧的 Docker 镜像
echo "🗑️ 删除旧的 Docker 镜像..."
docker rmi oldweipro/hik-isup:latest

# 加载 Docker 镜像
echo "🚚 加载 Docker 镜像..."
docker load -i /data/project/isup/hik-isup.tar

# 运行容器
echo "🚀 运行容器..."
docker run -p 16233:16233 -p 7660:7660 -p 7660:7660/udp -p 7665:7665 -p 7665:7665/udp -p 7500:7500 -p 7500:7500/udp -d --restart=always --name hik-isup oldweipro/hik-isup:latest

echo "🎉 部署完成!"
