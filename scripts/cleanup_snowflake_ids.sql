-- =============================================
-- 清理雪花算法生成的旧数据
-- 原则：删除雪花ID生成的大ID数据，保留其他数据
-- 雪花ID特征：通常 > 1万亿 (1000000000000)
-- =============================================

-- =============================================
-- 1. 先查看雪花ID数据（确认要删除的数据）
-- =============================================

-- 查看 customer 表中的雪花ID数据
SELECT 'dafuweng_sales.customer' as tbl, COUNT(*) as snowflake_count
FROM dafuweng_sales.customer WHERE id > 1000000000000;

SELECT id, name, phone, createdAt FROM dafuweng_sales.customer WHERE id > 1000000000000 LIMIT 10;

-- 查看 contract 表中的雪花ID数据
SELECT 'dafuweng_sales.contract' as tbl, COUNT(*) as snowflake_count
FROM dafuweng_sales.contract WHERE id > 1000000000000;

-- 查看 contact_record 表中的雪花ID数据
SELECT 'dafuweng_sales.contact_record' as tbl, COUNT(*) as snowflake_count
FROM dafuweng_sales.contact_record WHERE id > 1000000000000;

-- =============================================
-- 2. 删除雪花ID生成的客户及其关联数据
-- 注意：先删除关联表数据，再删除主表数据
-- =============================================

-- 删除 customer 关联的跟进记录
DELETE FROM dafuweng_sales.contact_record 
WHERE customer_id IN (SELECT id FROM (SELECT id FROM dafuweng_sales.customer WHERE id > 1000000000000) AS tmp);

-- 删除 customer 关联的转移记录
DELETE FROM dafuweng_sales.customer_transfer_log 
WHERE customer_id IN (SELECT id FROM (SELECT id FROM dafuweng_sales.customer WHERE id > 1000000000000) AS tmp);

-- 删除 customer 关联的合同
DELETE FROM dafuweng_sales.contract 
WHERE customer_id IN (SELECT id FROM (SELECT id FROM dafuweng_sales.customer WHERE id > 1000000000000) AS tmp);

-- 删除雪花ID生成的客户
DELETE FROM dafuweng_sales.customer WHERE id > 1000000000000;

-- 删除雪花ID生成的跟进记录（独立的，没有关联客户的）
DELETE FROM dafuweng_sales.contact_record WHERE id > 1000000000000;

-- 删除雪花ID生成的合同（独立的）
DELETE FROM dafuweng_sales.contract WHERE id > 1000000000000;

-- 删除雪花ID生成的合同附件
DELETE FROM dafuweng_sales.contract_attachment WHERE contract_id IN (SELECT id FROM (SELECT id FROM dafuweng_sales.contract WHERE id > 1000000000000) AS tmp);

-- 删除雪花ID生成的工作日志
DELETE FROM dafuweng_sales.work_log WHERE id > 1000000000000;

-- 删除雪花ID生成的业绩记录
DELETE FROM dafuweng_sales.performance_record WHERE id > 1000000000000;

-- =============================================
-- 3. Finance 模块（如果有关联数据）
-- =============================================

-- 删除雪花ID生成的贷款审核记录
DELETE FROM dafuweng_finance.loan_audit WHERE contract_id IN (SELECT id FROM (SELECT id FROM dafuweng_sales.contract WHERE id > 1000000000000) AS tmp);

-- 删除雪花ID生成的服务费记录
DELETE FROM dafuweng_finance.service_fee_record WHERE contract_id IN (SELECT id FROM (SELECT id FROM dafuweng_sales.contract WHERE id > 1000000000000) AS tmp);

-- 删除雪花ID生成的佣金记录
DELETE FROM dafuweng_finance.commission_record WHERE id > 1000000000000;

-- =============================================
-- 4. 验证清理结果
-- =============================================

SELECT '=== 清理后统计 ===' as info;

SELECT 'dafuweng_sales.customer' as tbl, COUNT(*) as remaining_count, MAX(id) as max_id FROM dafuweng_sales.customer;
SELECT 'dafuweng_sales.contract' as tbl, COUNT(*) as remaining_count, MAX(id) as max_id FROM dafuweng_sales.contract;
SELECT 'dafuweng_sales.contact_record' as tbl, COUNT(*) as remaining_count, MAX(id) as max_id FROM dafuweng_sales.contact_record;

-- =============================================
-- 5. 重置 AUTO_INCREMENT（如果需要）
-- =============================================

-- 可选：将 AUTO_INCREMENT 重置到合理值（当前最大ID + 1）
-- ALTER TABLE dafuweng_sales.customer AUTO_INCREMENT = 1;
-- ALTER TABLE dafuweng_sales.contract AUTO_INCREMENT = 1;

-- =============================================
-- 执行顺序说明：
-- 1. 先执行第1步查看要删除的数据
-- 2. 确认无误后，执行第2步删除关联数据
-- 3. 执行第3步清理 finance 模块关联数据
-- 4. 执行第4步验证结果
-- =============================================
