#!/bin/bash

# NeoCC 快速启动脚本（使用已构建的镜像）

set -e

echo "=========================================="
echo "NeoCC 快速启动"
echo "=========================================="

# 检查是否存在 jar 包
if [ ! -f "auth/target/auth-*.jar" ]; then
    echo "错误: 找不到构建好的 jar 包，请先运行 ./build-and-deploy.sh"
    exit 1
fi

# 启动所有服务
docker-compose up -d

echo ""
echo "等待服务启动..."
sleep 30

echo ""
echo "=========================================="
echo "服务状态:"
docker-compose ps
echo "=========================================="
echo ""
echo "访问地址:"
echo "  前端: http://localhost"
echo "  API:  http://localhost:8086"
echo "  Nacos: http://localhost:8848/nacos"
