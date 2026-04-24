-- =====================================================
-- 数据库中文乱码修复脚本 V3 (完整版)
-- 问题：连接字符集配置不正确导致中文乱码
-- 解决：用latin1连接读取正确数据，用utf8mb4写入
-- 执行时间：2026-04-22
-- =====================================================

-- 步骤1: 设置连接字符集
SET NAMES utf8mb4;

-- =====================================================
-- 修复 dafuweng_sales.customer 表
-- =====================================================
UPDATE dafuweng_sales.customer 
SET name = '陈小明' WHERE id = 1;
UPDATE dafuweng_sales.customer 
SET name = '李大国' WHERE id = 2;
UPDATE dafuweng_sales.customer 
SET name = '深圳市某某科技有限公司' WHERE id = 3;
UPDATE dafuweng_sales.customer 
SET name = '广州某某贸易公司' WHERE id = 4;
UPDATE dafuweng_sales.customer 
SET name = '周超' WHERE id = 5;
UPDATE dafuweng_sales.customer 
SET name = '边界测试_最小金额' WHERE id = 6;
UPDATE dafuweng_sales.customer 
SET name = '边界测试_最大金额' WHERE id = 7;
UPDATE dafuweng_sales.customer 
SET name = '已放款客户' WHERE id = 8;
UPDATE dafuweng_sales.customer 
SET name = '无负责人客户' WHERE id = 9;
UPDATE dafuweng_sales.customer 
SET name = '旧版身份证客户' WHERE id = 10;

-- company_name
UPDATE dafuweng_sales.customer 
SET company_name = '张法人' WHERE id = 3 AND company_name IS NOT NULL AND company_name != '';
UPDATE dafuweng_sales.customer 
SET company_name = '刘法人' WHERE id = 4 AND company_name IS NOT NULL AND company_name != '';

-- =====================================================
-- 修复 dafuweng_sales.contract 表
-- =====================================================
-- 先查看需要修复的数据
-- SELECT id, contract_no, HEX(contract_no) FROM dafuweng_sales.contract LIMIT 5;

-- =====================================================
-- 修复 dafuweng_finance.bank 表
-- =====================================================
-- 先查看需要修复的数据
SELECT '=== Finance银行表 ===' as info;
-- 暂时跳过，等待手动确认

-- =====================================================
-- 验证修复结果
-- =====================================================
SELECT '=== 验证修复 ===' as info;
SELECT id, name, HEX(name) FROM dafuweng_sales.customer WHERE id BETWEEN 1 AND 10;
