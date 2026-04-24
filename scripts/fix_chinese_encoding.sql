-- =====================================================
-- 数据库中文乱码修复脚本
-- 问题原因：UTF-8数据被双重编码存储
-- 修复方法：CONVERT(CONVERT(column USING binary) USING utf8mb4)
-- 执行时间：2026-04-22
-- =====================================================

-- 修复前先备份数据（创建备份表）
-- 注意：此脚本直接修复，不创建备份表，请谨慎执行

SET NAMES utf8mb4;

-- =====================================================
-- 1. 修复 dafuweng_sales 数据库
-- =====================================================

-- 客户表
UPDATE dafuweng_sales.customer SET 
    name = CONVERT(CONVERT(name USING binary) USING utf8mb4),
    company_name = CONVERT(CONVERT(company_name USING binary) USING utf8mb4),
    company_legal_person = CONVERT(CONVERT(company_legal_person USING binary) USING utf8mb4),
    id_card = CONVERT(CONVERT(id_card USING binary) USING utf8mb4),
    phone = CONVERT(CONVERT(phone USING binary) USING utf8mb4),
    source = CONVERT(CONVERT(source USING binary) USING utf8mb4),
    loan_intention_product = CONVERT(CONVERT(loan_intention_product USING binary) USING utf8mb4),
    public_sea_reason = CONVERT(CONVERT(public_sea_reason USING binary) USING utf8mb4)
WHERE name IS NOT NULL;

-- 合同表
UPDATE dafuweng_sales.contract SET 
    contract_no = CONVERT(CONVERT(contract_no USING binary) USING utf8mb4),
    loan_use = CONVERT(CONVERT(loan_use USING binary) USING utf8mb4),
    paper_contract_no = CONVERT(CONVERT(paper_contract_no USING binary) USING utf8mb4),
    reject_reason = CONVERT(CONVERT(reject_reason USING binary) USING utf8mb4)
WHERE contract_no IS NOT NULL;

-- 合同附件表
UPDATE dafuweng_sales.contract_attachment SET 
    attachment_type = CONVERT(CONVERT(attachment_type USING binary) USING utf8mb4),
    file_name = CONVERT(CONVERT(file_name USING binary) USING utf8mb4),
    file_url = CONVERT(CONVERT(file_url USING binary) USING utf8mb4),
    file_md5 = CONVERT(CONVERT(file_md5 USING binary) USING utf8mb4)
WHERE file_name IS NOT NULL;

-- 客户转移日志表
UPDATE dafuweng_sales.customer_transfer_log SET 
    operate_type = CONVERT(CONVERT(operate_type USING binary) USING utf8mb4),
    reason = CONVERT(CONVERT(reason USING binary) USING utf8mb4)
WHERE operate_type IS NOT NULL;

-- 业绩记录表
UPDATE dafuweng_sales.performance_record SET 
    cancel_reason = CONVERT(CONVERT(cancel_reason USING binary) USING utf8mb4)
WHERE cancel_reason IS NOT NULL;

-- =====================================================
-- 2. 修复 dafuweng_finance 数据库
-- =====================================================

-- 银行表
UPDATE dafuweng_finance.bank SET 
    bank_name = CONVERT(CONVERT(bank_name USING binary) USING utf8mb4),
    bank_code = CONVERT(CONVERT(bank_code USING binary) USING utf8mb4),
    bank_branch = CONVERT(CONVERT(bank_branch USING binary) USING utf8mb4),
    contact_person = CONVERT(CONVERT(contact_person USING binary) USING utf8mb4),
    contact_phone = CONVERT(CONVERT(contact_phone USING binary) USING utf8mb4)
WHERE bank_name IS NOT NULL;

-- 金融产品表
UPDATE dafuweng_finance.finance_product SET 
    product_code = CONVERT(CONVERT(product_code USING binary) USING utf8mb4),
    product_name = CONVERT(CONVERT(product_name USING binary) USING utf8mb4)
WHERE product_name IS NOT NULL;

-- 贷款审核表
UPDATE dafuweng_finance.loan_audit SET 
    bank_audit_status = CONVERT(CONVERT(bank_audit_status USING binary) USING utf8mb4),
    reject_reason = CONVERT(CONVERT(reject_reason USING binary) USING utf8mb4)
WHERE bank_audit_status IS NOT NULL;

-- 贷款审核记录表
UPDATE dafuweng_finance.loan_audit_record SET 
    action = CONVERT(CONVERT(action USING binary) USING utf8mb4),
    operator_name = CONVERT(CONVERT(operator_name USING binary) USING utf8mb4),
    operator_role = CONVERT(CONVERT(operator_role USING binary) USING utf8mb4)
WHERE action IS NOT NULL;

-- 佣金记录表
UPDATE dafuweng_finance.commission_record SET 
    grant_account = CONVERT(CONVERT(grant_account USING binary) USING utf8mb4),
    remark = CONVERT(CONVERT(remark USING binary) USING utf8mb4)
WHERE grant_account IS NOT NULL;

-- 服务费记录表
UPDATE dafuweng_finance.service_fee_record SET 
    payment_account = CONVERT(CONVERT(payment_account USING binary) USING utf8mb4),
    payment_method = CONVERT(CONVERT(payment_method USING binary) USING utf8mb4),
    receipt_no = CONVERT(CONVERT(receipt_no USING binary) USING utf8mb4),
    remark = CONVERT(CONVERT(remark USING binary) USING utf8mb4)
WHERE payment_account IS NOT NULL;

-- =====================================================
-- 3. 修复 dafuweng_system 数据库
-- =====================================================

-- 部门表
UPDATE dafuweng_system.sys_department SET 
    dept_name = CONVERT(CONVERT(dept_name USING binary) USING utf8mb4),
    dept_code = CONVERT(CONVERT(dept_code USING binary) USING utf8mb4)
WHERE dept_name IS NOT NULL;

-- 战区表
UPDATE dafuweng_system.sys_zone SET 
    zone_name = CONVERT(CONVERT(zone_name USING binary) USING utf8mb4),
    zone_code = CONVERT(CONVERT(zone_code USING binary) USING utf8mb4)
WHERE zone_name IS NOT NULL;

-- 字典表
UPDATE dafuweng_system.sys_dict SET 
    dict_label = CONVERT(CONVERT(dict_label USING binary) USING utf8mb4),
    dict_value = CONVERT(CONVERT(dict_value USING binary) USING utf8mb4),
    dict_code = CONVERT(CONVERT(dict_code USING binary) USING utf8mb4),
    dict_type = CONVERT(CONVERT(dict_type USING binary) USING utf8mb4),
    remark = CONVERT(CONVERT(remark USING binary) USING utf8mb4)
WHERE dict_label IS NOT NULL;

-- 参数字典表
UPDATE dafuweng_system.sys_param SET 
    param_key = CONVERT(CONVERT(param_key USING binary) USING utf8mb4),
    param_value = CONVERT(CONVERT(param_value USING binary) USING utf8mb4),
    param_group = CONVERT(CONVERT(param_group USING binary) USING utf8mb4),
    param_type = CONVERT(CONVERT(param_type USING binary) USING utf8mb4),
    remark = CONVERT(CONVERT(remark USING binary) USING utf8mb4)
WHERE param_key IS NOT NULL;

-- 操作日志表
UPDATE dafuweng_system.sys_operation_log SET 
    module = CONVERT(CONVERT(module USING binary) USING utf8mb4),
    action = CONVERT(CONVERT(action USING binary) USING utf8mb4),
    username = CONVERT(CONVERT(username USING binary) USING utf8mb4),
    request_method = CONVERT(CONVERT(request_method USING binary) USING utf8mb4),
    request_url = CONVERT(CONVERT(request_url USING binary) USING utf8mb4),
    request_params = CONVERT(CONVERT(request_params USING binary) USING utf8mb4),
    response_code = CONVERT(CONVERT(response_code USING binary) USING utf8mb4),
    ip = CONVERT(CONVERT(ip USING binary) USING utf8mb4),
    user_agent = CONVERT(CONVERT(user_agent USING binary) USING utf8mb4)
WHERE module IS NOT NULL;

-- =====================================================
-- 4. 验证修复结果
-- =====================================================

-- 验证 sales 客户表
SELECT '=== Sales客户表修复验证 ===' as info;
SELECT id, name, company_name FROM dafuweng_sales.customer LIMIT 5;

-- 验证 finance 银行表
SELECT '=== Finance银行表修复验证 ===' as info;
SELECT id, bank_name, bank_branch FROM dafuweng_finance.bank LIMIT 5;

-- 验证 system 部门表
SELECT '=== System部门表修复验证 ===' as info;
SELECT id, dept_name FROM dafuweng_system.sys_department LIMIT 5;

-- 验证 system 战区表
SELECT '=== System战区表修复验证 ===' as info;
SELECT id, zone_name FROM dafuweng_system.sys_zone LIMIT 5;
