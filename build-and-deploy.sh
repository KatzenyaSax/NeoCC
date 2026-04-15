#!/bin/bash

# NeoCC Docker 构建和部署脚本

set -e

echo "=========================================="
echo "NeoCC 项目 Docker 构建和部署"
echo "=========================================="

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 步骤 1: Maven 打包
echo -e "${YELLOW}[1/5] Maven 打包所有模块...${NC}"
mvn clean package -DskipTests

# 步骤 2: 构建 Docker 镜像
echo -e "${YELLOW}[2/5] 构建 Docker 镜像...${NC}"
docker-compose build

# 步骤 3: 启动基础设施
echo -e "${YELLOW}[3/5] 启动基础设施 (MySQL, Redis, Nacos)...${NC}"
docker-compose up -d mysql redis

# 等待 MySQL 启动
echo "等待 MySQL 启动..."
sleep 15

# 启动 Nacos
docker-compose up -d nacos

# 等待 Nacos 启动
echo "等待 Nacos 启动..."
sleep 20

# 步骤 4: 启动业务服务
echo -e "${YELLOW}[4/5] 启动业务服务...${NC}"
docker-compose up -d auth-service system-service sales-service finance-service

# 等待服务注册
echo "等待服务注册到 Nacos..."
sleep 15

# 启动 Gateway
docker-compose up -d gateway-service

# 步骤 5: 启动前端 Nginx
echo -e "${YELLOW}[5/5] 启动 Nginx 前端服务...${NC}"
docker-compose up -d nginx

echo ""
echo -e "${GREEN}==========================================${NC}"
echo -e "${GREEN}部署完成！服务访问地址：${NC}"
echo -e "${GREEN}==========================================${NC}"
echo ""
echo "前端界面:     http://localhost"
echo "Gateway API:  http://localhost:8086"
echo "Nacos 控制台: http://localhost:8848/nacos (nacos/nacos)"
echo "MySQL:        localhost:3306 (root/123456)"
echo "Redis:        localhost:6379"
echo ""
echo "服务端口:"
echo "  - Auth:    8085"
echo "  - System:  8082"
echo "  - Sales:   8083"
echo "  - Finance: 8084"
echo "  - Gateway: 8086"
echo ""
echo -e "${YELLOW}查看日志: docker-compose logs -f [服务名]${NC}"
echo -e "${YELLOW}停止服务: docker-compose down${NC}"
