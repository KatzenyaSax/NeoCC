-- =============================================
-- 将雪花算法ID改为自增ID - 解决前端大数字精度问题
-- 执行前请备份数据！
-- =============================================

-- 修改 sales 模块表
ALTER TABLE dafuweng_sales.contact_record MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT;
ALTER TABLE dafuweng_sales.contract MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT;
ALTER TABLE dafuweng_sales.contract_attachment MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT;
ALTER TABLE dafuweng_sales.customer MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT;
ALTER TABLE dafuweng_sales.customer_transfer_log MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT;
ALTER TABLE dafuweng_sales.performance_record MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT;
ALTER TABLE dafuweng_sales.work_log MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT;

-- 修改 finance 模块表
ALTER TABLE dafuweng_finance.bank MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT;
ALTER TABLE dafuweng_finance.commission_record MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT;
ALTER TABLE dafuweng_finance.finance_product MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT;
ALTER TABLE dafuweng_finance.loan_audit MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT;
ALTER TABLE dafuweng_finance.loan_audit_record MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT;
ALTER TABLE dafuweng_finance.service_fee_record MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT;

-- =============================================
-- 注意：
-- 1. auth 模块的表已经是自增ID，不需要修改
-- 2. system 模块的表已经是自增ID，不需要修改
-- 3. 执行后，新增记录的ID将从当前最大ID继续递增
-- =============================================
