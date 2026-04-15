-- NeoCC 数据库初始化脚本
-- 创建所有服务所需的数据库

-- Nacos 配置中心数据库
CREATE DATABASE IF NOT EXISTS nacos DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Auth 认证服务数据库
CREATE DATABASE IF NOT EXISTS dafuweng_auth DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- System 系统服务数据库
CREATE DATABASE IF NOT EXISTS dafuweng_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Sales 销售服务数据库
CREATE DATABASE IF NOT EXISTS dafuweng_sales DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Finance 财务服务数据库
CREATE DATABASE IF NOT EXISTS dafuweng_finance DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 授权 root 用户远程访问
ALTER USER 'root'@'%' IDENTIFIED WITH mysql_native_password BY '123456';
FLUSH PRIVILEGES;
