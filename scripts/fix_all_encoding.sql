-- =====================================================
-- 数据库中文乱码修复脚本 (完整版)
-- 修复所有乱码表的中文字段
-- 执行时间：2026-04-22
-- =====================================================

-- =====================================================
-- 1. 修复 dafuweng_sales.customer 表
-- =====================================================
UPDATE dafuweng_sales.customer SET 
    name = '陈小明',
    source = '电话营销',
    loan_intention_product = '企业经营贷'
WHERE id = 1 AND deleted = 0;

UPDATE dafuweng_sales.customer SET 
    name = '李大国',
    source = '朋友推荐',
    loan_intention_product = '个人消费贷'
WHERE id = 2 AND deleted = 0;

UPDATE dafuweng_sales.customer SET 
    name = '深圳市某某科技有限公司',
    company_name = '张法人',
    source = '展会获客',
    loan_intention_product = '企业经营贷'
WHERE id = 3 AND deleted = 0;

UPDATE dafuweng_sales.customer SET 
    name = '广州某某贸易公司',
    company_name = '刘法人',
    source = '网络推广',
    loan_intention_product = '供应链金融'
WHERE id = 4 AND deleted = 0;

UPDATE dafuweng_sales.customer SET 
    name = '周超',
    source = '渠道不明'
WHERE id = 5 AND deleted = 0;

UPDATE dafuweng_sales.customer SET 
    name = '边界测试_最小金额',
    source = '测试',
    loan_intention_product = '测试产品'
WHERE id = 6 AND deleted = 0;

UPDATE dafuweng_sales.customer SET 
    name = '边界测试_最大金额',
    source = '测试',
    loan_intention_product = '测试产品'
WHERE id = 7 AND deleted = 0;

UPDATE dafuweng_sales.customer SET 
    name = '已放款客户',
    source = '老客户复借',
    loan_intention_product = '企业经营贷'
WHERE id = 8 AND deleted = 0;

UPDATE dafuweng_sales.customer SET 
    name = '无负责人客户',
    source = '自然到店',
    loan_intention_product = '个人消费贷'
WHERE id = 9 AND deleted = 0;

UPDATE dafuweng_sales.customer SET 
    name = '旧版身份证客户',
    source = '测试',
    loan_intention_product = '测试'
WHERE id = 10 AND deleted = 0;

-- =====================================================
-- 2. 修复 dafuweng_sales.contract 表
-- =====================================================
UPDATE dafuweng_sales.contract SET 
    loan_use = '流动资金周转'
WHERE id = 1 AND deleted = 0;

UPDATE dafuweng_sales.contract SET 
    loan_use = '设备采购'
WHERE id = 2 AND deleted = 0;

UPDATE dafuweng_sales.contract SET 
    loan_use = '测试'
WHERE id = 3 AND deleted = 0;

UPDATE dafuweng_sales.contract SET 
    loan_use = '个人消费'
WHERE id = 4 AND deleted = 0;

UPDATE dafuweng_sales.contract SET 
    loan_use = '未知'
WHERE id = 5 AND deleted = 0;

-- =====================================================
-- 3. 修复 dafuweng_finance.bank 表
-- =====================================================
UPDATE dafuweng_finance.bank SET 
    bank_name = '中国工商银行',
    bank_branch = '深圳南山支行'
WHERE id = 1 AND deleted = 0;

UPDATE dafuweng_finance.bank SET 
    bank_name = '中国建设银行',
    bank_branch = '广州天河支行'
WHERE id = 2 AND deleted = 0;

UPDATE dafuweng_finance.bank SET 
    bank_name = '中国农业银行',
    bank_branch = '北京朝阳支行'
WHERE id = 3 AND deleted = 0;

UPDATE dafuweng_finance.bank SET 
    bank_name = '中国银行',
    bank_branch = '上海浦东支行'
WHERE id = 4 AND deleted = 0;

UPDATE dafuweng_finance.bank SET 
    bank_name = '某农村信用社',
    bank_branch = '广州分行'
WHERE id = 5 AND deleted = 0;

-- =====================================================
-- 4. 修复 dafuweng_finance.finance_product 表
-- =====================================================
UPDATE dafuweng_finance.finance_product SET 
    product_name = '工商银行企业经营贷'
WHERE id = 1 AND deleted = 0;

UPDATE dafuweng_finance.finance_product SET 
    product_name = '建设银行个人消费贷'
WHERE id = 2 AND deleted = 0;

UPDATE dafuweng_finance.finance_product SET 
    product_name = '农业银行供应链金融'
WHERE id = 3 AND deleted = 0;

UPDATE dafuweng_finance.finance_product SET 
    product_name = '测试最低利率'
WHERE id = 4 AND deleted = 0;

UPDATE dafuweng_finance.finance_product SET 
    product_name = '测试最高利率'
WHERE id = 5 AND deleted = 0;

UPDATE dafuweng_finance.finance_product SET 
    product_name = '已下线产品'
WHERE id = 6 AND deleted = 0;

-- =====================================================
-- 5. 修复 dafuweng_system.sys_department 表
-- =====================================================
UPDATE dafuweng_system.sys_department SET 
    dept_name = '总部'
WHERE id = 1 AND deleted = 0;

UPDATE dafuweng_system.sys_department SET 
    dept_name = '销售部'
WHERE id = 2 AND deleted = 0;

UPDATE dafuweng_system.sys_department SET 
    dept_name = '财务部'
WHERE id = 3 AND deleted = 0;

UPDATE dafuweng_system.sys_department SET 
    dept_name = '运营部'
WHERE id = 4 AND deleted = 0;

UPDATE dafuweng_system.sys_department SET 
    dept_name = '销售一部'
WHERE id = 5 AND deleted = 0;

UPDATE dafuweng_system.sys_department SET 
    dept_name = '销售二部'
WHERE id = 6 AND deleted = 0;

UPDATE dafuweng_system.sys_department SET 
    dept_name = '已禁用部门'
WHERE id = 7 AND deleted = 0;

UPDATE dafuweng_system.sys_department SET 
    dept_name = '无战区部门'
WHERE id = 8 AND deleted = 0;

-- =====================================================
-- 6. 修复 dafuweng_system.sys_zone 表
-- =====================================================
UPDATE dafuweng_system.sys_zone SET 
    zone_name = '东部战区'
WHERE id = 1 AND deleted = 0;

UPDATE dafuweng_system.sys_zone SET 
    zone_name = '西部战区'
WHERE id = 2 AND deleted = 0;

UPDATE dafuweng_system.sys_zone SET 
    zone_name = '南部战区'
WHERE id = 3 AND deleted = 0;

UPDATE dafuweng_system.sys_zone SET 
    zone_name = '北部战区'
WHERE id = 4 AND deleted = 0;

UPDATE dafuweng_system.sys_zone SET 
    zone_name = '最小排序战区'
WHERE id = 5 AND deleted = 0;

UPDATE dafuweng_system.sys_zone SET 
    zone_name = '最大排序战区'
WHERE id = 6 AND deleted = 0;

-- =====================================================
-- 7. 修复 dafuweng_system.sys_dict 表
-- =====================================================
UPDATE dafuweng_system.sys_dict SET 
    dict_label = 'A级(高意向)',
    dict_value = '1'
WHERE id = 1 AND deleted = 0;

UPDATE dafuweng_system.sys_dict SET 
    dict_label = 'B级(中意向)',
    dict_value = '2'
WHERE id = 2 AND deleted = 0;

UPDATE dafuweng_system.sys_dict SET 
    dict_label = 'C级(低意向)',
    dict_value = '3'
WHERE id = 3 AND deleted = 0;

UPDATE dafuweng_system.sys_dict SET 
    dict_label = 'D级(无意向)',
    dict_value = '4'
WHERE id = 4 AND deleted = 0;

UPDATE dafuweng_system.sys_dict SET 
    dict_label = '潜在客户',
    dict_value = '1'
WHERE id = 5 AND deleted = 0;

UPDATE dafuweng_system.sys_dict SET 
    dict_label = '洽谈中',
    dict_value = '2'
WHERE id = 6 AND deleted = 0;

UPDATE dafuweng_system.sys_dict SET 
    dict_label = '已签约',
    dict_value = '3'
WHERE id = 7 AND deleted = 0;

UPDATE dafuweng_system.sys_dict SET 
    dict_label = '已放款',
    dict_value = '4'
WHERE id = 8 AND deleted = 0;

UPDATE dafuweng_system.sys_dict SET 
    dict_label = '公海客户',
    dict_value = '5'
WHERE id = 9 AND deleted = 0;

UPDATE dafuweng_system.sys_dict SET 
    dict_label = '个人客户',
    dict_value = '1'
WHERE id = 10 AND deleted = 0;

UPDATE dafuweng_system.sys_dict SET 
    dict_label = '企业客户',
    dict_value = '2'
WHERE id = 11 AND deleted = 0;

UPDATE dafuweng_system.sys_dict SET 
    dict_label = '已停用',
    dict_value = '99'
WHERE id = 12 AND deleted = 0;

-- =====================================================
-- 验证修复结果
-- =====================================================
SELECT '=== 验证修复结果 ===' as info;

SELECT '--- Customer ---' as table_name;
SELECT id, name FROM dafuweng_sales.customer WHERE deleted = 0 ORDER BY id LIMIT 10;

SELECT '--- Bank ---' as table_name;
SELECT id, bank_name FROM dafuweng_finance.bank WHERE deleted = 0 ORDER BY id;

SELECT '--- Department ---' as table_name;
SELECT id, dept_name FROM dafuweng_system.sys_department WHERE deleted = 0 ORDER BY id;

SELECT '--- Zone ---' as table_name;
SELECT id, zone_name FROM dafuweng_system.sys_zone WHERE deleted = 0 ORDER BY id;

SELECT '=== 修复完成 ===' as result;
