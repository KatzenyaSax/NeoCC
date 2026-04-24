-- =====================================================
-- 数据库中文乱码修复脚本 V4 (完整版)
-- 根据原始datas.sql中的正确中文重新修复所有乱码
-- 执行时间：2026-04-22
-- =====================================================

SET NAMES utf8mb4;

-- =====================================================
-- 1. 修复 dafuweng_auth.sys_permission 表 (菜单权限)
-- =====================================================

-- 顶级菜单
UPDATE dafuweng_auth.sys_permission SET perm_name = '系统管理' WHERE id = 1 AND perm_code = 'SYSTEM';
UPDATE dafuweng_auth.sys_permission SET perm_name = '用户管理' WHERE id = 2 AND perm_code = 'SYSTEM_USER';
UPDATE dafuweng_auth.sys_permission SET perm_name = '角色管理' WHERE id = 3 AND perm_code = 'SYSTEM_ROLE';
UPDATE dafuweng_auth.sys_permission SET perm_name = '权限管理' WHERE id = 4 AND perm_code = 'SYSTEM_PERMISSION';
UPDATE dafuweng_auth.sys_permission SET perm_name = '部门管理' WHERE id = 5 AND perm_code = 'SYSTEM_DEPT';
UPDATE dafuweng_auth.sys_permission SET perm_name = '战区管理' WHERE id = 6 AND perm_code = 'SYSTEM_ZONE';

-- 按钮权限
UPDATE dafuweng_auth.sys_permission SET perm_name = '新增用户' WHERE id = 101 AND perm_code = 'BTN_USER_ADD';
UPDATE dafuweng_auth.sys_permission SET perm_name = '编辑用户' WHERE id = 102 AND perm_code = 'BTN_USER_EDIT';
UPDATE dafuweng_auth.sys_permission SET perm_name = '删除用户' WHERE id = 103 AND perm_code = 'BTN_USER_DEL';
UPDATE dafuweng_auth.sys_permission SET perm_name = '分配角色' WHERE id = 104 AND perm_code = 'BTN_USER_ALLOC_ROLE';
UPDATE dafuweng_auth.sys_permission SET perm_name = '解锁用户' WHERE id = 105 AND perm_code = 'BTN_USER_UNLOCK';
UPDATE dafuweng_auth.sys_permission SET perm_name = '启用/禁用用户' WHERE id = 106 AND perm_code = 'BTN_USER_ENABLE';
UPDATE dafuweng_auth.sys_permission SET perm_name = '新增角色' WHERE id = 111 AND perm_code = 'BTN_ROLE_ADD';
UPDATE dafuweng_auth.sys_permission SET perm_name = '编辑角色' WHERE id = 112 AND perm_code = 'BTN_ROLE_EDIT';
UPDATE dafuweng_auth.sys_permission SET perm_name = '删除角色' WHERE id = 113 AND perm_code = 'BTN_ROLE_DEL';
UPDATE dafuweng_auth.sys_permission SET perm_name = '分配权限' WHERE id = 114 AND perm_code = 'BTN_ROLE_ALLOC_PERM';

-- API权限
UPDATE dafuweng_auth.sys_permission SET perm_name = '用户分页查询' WHERE id = 201 AND perm_code = 'API_USER_PAGE';
UPDATE dafuweng_auth.sys_permission SET perm_name = '获取单个用户' WHERE id = 202 AND perm_code = 'API_USER_GET';
UPDATE dafuweng_auth.sys_permission SET perm_name = '创建用户' WHERE id = 203 AND perm_code = 'API_USER_POST';
UPDATE dafuweng_auth.sys_permission SET perm_name = '更新用户' WHERE id = 204 AND perm_code = 'API_USER_PUT';
UPDATE dafuweng_auth.sys_permission SET perm_name = '删除用户' WHERE id = 205 AND perm_code = 'API_USER_DEL';
UPDATE dafuweng_auth.sys_permission SET perm_name = '启用/禁用用户' WHERE id = 206 AND perm_code = 'API_USER_ENABLE';
UPDATE dafuweng_auth.sys_permission SET perm_name = '角色分页查询' WHERE id = 211 AND perm_code = 'API_ROLE_PAGE';
UPDATE dafuweng_auth.sys_permission SET perm_name = '获取单个角色' WHERE id = 212 AND perm_code = 'API_ROLE_GET';
UPDATE dafuweng_auth.sys_permission SET perm_name = '创建角色' WHERE id = 213 AND perm_code = 'API_ROLE_POST';
UPDATE dafuweng_auth.sys_permission SET perm_name = '更新角色' WHERE id = 214 AND perm_code = 'API_ROLE_PUT';
UPDATE dafuweng_auth.sys_permission SET perm_name = '删除角色' WHERE id = 215 AND perm_code = 'API_ROLE_DEL';
UPDATE dafuweng_auth.sys_permission SET perm_name = '权限树查询' WHERE id = 221 AND perm_code = 'API_PERM_TREELIST';
UPDATE dafuweng_auth.sys_permission SET perm_name = '分配权限' WHERE id = 222 AND perm_code = 'API_PERM_ASSIGN';

-- 边界菜单
UPDATE dafuweng_auth.sys_permission SET perm_name = '字典管理' WHERE id = 15 AND perm_code = 'SYSTEM_DICT';
UPDATE dafuweng_auth.sys_permission SET perm_name = '参数管理' WHERE id = 16 AND perm_code = 'SYSTEM_PARAM';
UPDATE dafuweng_auth.sys_permission SET perm_name = '日志管理' WHERE id = 17 AND perm_code = 'SYSTEM_LOG';
UPDATE dafuweng_auth.sys_permission SET perm_name = '已禁用菜单' WHERE id = 999 AND perm_code = 'SYSTEM_DISABLED_MENU';
UPDATE dafuweng_auth.sys_permission SET perm_name = '外部链接' WHERE id = 998 AND perm_code = 'SYSTEM_EXTERNAL_LINK';

-- 销售管理菜单
UPDATE dafuweng_auth.sys_permission SET perm_name = '客户列表' WHERE id = 300 AND perm_code = 'SALES_CUSTOMER_LIST';
UPDATE dafuweng_auth.sys_permission SET perm_name = '公海客户' WHERE id = 304 AND perm_code = 'SALES_PUBLIC_SEA';
UPDATE dafuweng_auth.sys_permission SET perm_name = '联系记录' WHERE id = 305 AND perm_code = 'SALES_CONTACT';
UPDATE dafuweng_auth.sys_permission SET perm_name = '合同列表' WHERE id = 306 AND perm_code = 'SALES_CONTRACT_LIST';
UPDATE dafuweng_auth.sys_permission SET perm_name = '新增合同' WHERE id = 307 AND perm_code = 'SALES_CONTRACT_ADD';
UPDATE dafuweng_auth.sys_permission SET perm_name = '编辑合同' WHERE id = 308 AND perm_code = 'SALES_CONTRACT_EDIT';
UPDATE dafuweng_auth.sys_permission SET perm_name = '查看合同' WHERE id = 309 AND perm_code = 'SALES_CONTRACT_VIEW';
UPDATE dafuweng_auth.sys_permission SET perm_name = '合同签约' WHERE id = 310 AND perm_code = 'SALES_CONTRACT_SIGN';
UPDATE dafuweng_auth.sys_permission SET perm_name = '工作日志' WHERE id = 311 AND perm_code = 'SALES_WORKLOG';
UPDATE dafuweng_auth.sys_permission SET perm_name = '客户转移' WHERE id = 312 AND perm_code = 'SALES_TRANSFER';

-- 销售按钮权限
UPDATE dafuweng_auth.sys_permission SET perm_name = '查看客户' WHERE id = 320 AND perm_code = 'BTN_CUSTOMER_VIEW';
UPDATE dafuweng_auth.sys_permission SET perm_name = '新增客户' WHERE id = 321 AND perm_code = 'BTN_CUSTOMER_ADD';
UPDATE dafuweng_auth.sys_permission SET perm_name = '编辑客户' WHERE id = 322 AND perm_code = 'BTN_CUSTOMER_EDIT';
UPDATE dafuweng_auth.sys_permission SET perm_name = '转移客户' WHERE id = 323 AND perm_code = 'BTN_CUSTOMER_TRANSFER';
UPDATE dafuweng_auth.sys_permission SET perm_name = '添加备注' WHERE id = 324 AND perm_code = 'BTN_CUSTOMER_ANNOTATION';
UPDATE dafuweng_auth.sys_permission SET perm_name = '领取公海客户' WHERE id = 325 AND perm_code = 'BTN_PUBLIC_SEA_CLAIM';
UPDATE dafuweng_auth.sys_permission SET perm_name = '查看联系记录' WHERE id = 340 AND perm_code = 'BTN_CONTACT_VIEW';
UPDATE dafuweng_auth.sys_permission SET perm_name = '新增联系' WHERE id = 341 AND perm_code = 'BTN_CONTACT_ADD';
UPDATE dafuweng_auth.sys_permission SET perm_name = '编辑联系' WHERE id = 342 AND perm_code = 'BTN_CONTACT_EDIT';
UPDATE dafuweng_auth.sys_permission SET perm_name = '删除联系' WHERE id = 343 AND perm_code = 'BTN_CONTACT_DEL';
UPDATE dafuweng_auth.sys_permission SET perm_name = '查看合同' WHERE id = 330 AND perm_code = 'BTN_CONTRACT_VIEW';
UPDATE dafuweng_auth.sys_permission SET perm_name = '新增合同' WHERE id = 331 AND perm_code = 'BTN_CONTRACT_ADD';
UPDATE dafuweng_auth.sys_permission SET perm_name = '编辑合同' WHERE id = 332 AND perm_code = 'BTN_CONTRACT_EDIT';
UPDATE dafuweng_auth.sys_permission SET perm_name = '合同签约' WHERE id = 333 AND perm_code = 'BTN_CONTRACT_SIGN';
UPDATE dafuweng_auth.sys_permission SET perm_name = '转移合同' WHERE id = 334 AND perm_code = 'BTN_CONTRACT_TRANSFER';
UPDATE dafuweng_auth.sys_permission SET perm_name = '查看日志' WHERE id = 350 AND perm_code = 'BTN_WORKLOG_VIEW';
UPDATE dafuweng_auth.sys_permission SET perm_name = '新增日志' WHERE id = 351 AND perm_code = 'BTN_WORKLOG_ADD';
UPDATE dafuweng_auth.sys_permission SET perm_name = '编辑日志' WHERE id = 352 AND perm_code = 'BTN_WORKLOG_EDIT';

-- 销售API权限
UPDATE dafuweng_auth.sys_permission SET perm_name = '客户分页查询' WHERE id = 400 AND perm_code = 'API_CUSTOMER_PAGE';
UPDATE dafuweng_auth.sys_permission SET perm_name = '获取单个客户' WHERE id = 401 AND perm_code = 'API_CUSTOMER_GET';
UPDATE dafuweng_auth.sys_permission SET perm_name = '创建客户' WHERE id = 402 AND perm_code = 'API_CUSTOMER_POST';
UPDATE dafuweng_auth.sys_permission SET perm_name = '更新客户' WHERE id = 403 AND perm_code = 'API_CUSTOMER_PUT';
UPDATE dafuweng_auth.sys_permission SET perm_name = '客户转移' WHERE id = 404 AND perm_code = 'API_CUSTOMER_TRANSFER';
UPDATE dafuweng_auth.sys_permission SET perm_name = '领取公海客户' WHERE id = 405 AND perm_code = 'API_CUSTOMER_CLAIM';
UPDATE dafuweng_auth.sys_permission SET perm_name = '联系分页查询' WHERE id = 410 AND perm_code = 'API_CONTACT_PAGE';
UPDATE dafuweng_auth.sys_permission SET perm_name = '获取单个联系' WHERE id = 411 AND perm_code = 'API_CONTACT_GET';
UPDATE dafuweng_auth.sys_permission SET perm_name = '创建联系记录' WHERE id = 412 AND perm_code = 'API_CONTACT_POST';
UPDATE dafuweng_auth.sys_permission SET perm_name = '更新联系记录' WHERE id = 413 AND perm_code = 'API_CONTACT_PUT';
UPDATE dafuweng_auth.sys_permission SET perm_name = '删除联系记录' WHERE id = 414 AND perm_code = 'API_CONTACT_DEL';
UPDATE dafuweng_auth.sys_permission SET perm_name = '合同分页查询' WHERE id = 420 AND perm_code = 'API_CONTRACT_PAGE';
UPDATE dafuweng_auth.sys_permission SET perm_name = '获取单个合同' WHERE id = 421 AND perm_code = 'API_CONTRACT_GET';
UPDATE dafuweng_auth.sys_permission SET perm_name = '创建合同' WHERE id = 422 AND perm_code = 'API_CONTRACT_POST';
UPDATE dafuweng_auth.sys_permission SET perm_name = '更新合同' WHERE id = 423 AND perm_code = 'API_CONTRACT_PUT';
UPDATE dafuweng_auth.sys_permission SET perm_name = '合同签约' WHERE id = 424 AND perm_code = 'API_CONTRACT_SIGN';
UPDATE dafuweng_auth.sys_permission SET perm_name = '合同转移' WHERE id = 425 AND perm_code = 'API_CONTRACT_TRANSFER';
UPDATE dafuweng_auth.sys_permission SET perm_name = '日志分页查询' WHERE id = 430 AND perm_code = 'API_WORKLOG_PAGE';
UPDATE dafuweng_auth.sys_permission SET perm_name = '创建工作日志' WHERE id = 431 AND perm_code = 'API_WORKLOG_POST';
UPDATE dafuweng_auth.sys_permission SET perm_name = '更新工作日志' WHERE id = 432 AND perm_code = 'API_WORKLOG_PUT';
UPDATE dafuweng_auth.sys_permission SET perm_name = '转移分页查询' WHERE id = 440 AND perm_code = 'API_TRANSFER_PAGE';

-- 财务管理菜单
UPDATE dafuweng_auth.sys_permission SET perm_name = '银行管理' WHERE id = 500 AND perm_code = 'FINANCE_BANK';
UPDATE dafuweng_auth.sys_permission SET perm_name = '产品管理' WHERE id = 501 AND perm_code = 'FINANCE_PRODUCT';
UPDATE dafuweng_auth.sys_permission SET perm_name = '贷款审核' WHERE id = 502 AND perm_code = 'FINANCE_LOAN_AUDIT';
UPDATE dafuweng_auth.sys_permission SET perm_name = '佣金管理' WHERE id = 503 AND perm_code = 'FINANCE_COMMISSION';
UPDATE dafuweng_auth.sys_permission SET perm_name = '服务费管理' WHERE id = 504 AND perm_code = 'FINANCE_SERVICE_FEE';

-- 财务按钮权限
UPDATE dafuweng_auth.sys_permission SET perm_name = '查看银行' WHERE id = 510 AND perm_code = 'BTN_BANK_VIEW';
UPDATE dafuweng_auth.sys_permission SET perm_name = '新增银行' WHERE id = 511 AND perm_code = 'BTN_BANK_ADD';
UPDATE dafuweng_auth.sys_permission SET perm_name = '编辑银行' WHERE id = 512 AND perm_code = 'BTN_BANK_EDIT';
UPDATE dafuweng_auth.sys_permission SET perm_name = '删除银行' WHERE id = 513 AND perm_code = 'BTN_BANK_DEL';
UPDATE dafuweng_auth.sys_permission SET perm_name = '查看产品' WHERE id = 520 AND perm_code = 'BTN_PRODUCT_VIEW';
UPDATE dafuweng_auth.sys_permission SET perm_name = '新增产品' WHERE id = 521 AND perm_code = 'BTN_PRODUCT_ADD';
UPDATE dafuweng_auth.sys_permission SET perm_name = '编辑产品' WHERE id = 522 AND perm_code = 'BTN_PRODUCT_EDIT';
UPDATE dafuweng_auth.sys_permission SET perm_name = '删除产品' WHERE id = 523 AND perm_code = 'BTN_PRODUCT_DEL';
UPDATE dafuweng_auth.sys_permission SET perm_name = '启用/禁用产品' WHERE id = 524 AND perm_code = 'BTN_PRODUCT_ONOFF';
UPDATE dafuweng_auth.sys_permission SET perm_name = '查看贷款' WHERE id = 530 AND perm_code = 'BTN_LOAN_VIEW';
UPDATE dafuweng_auth.sys_permission SET perm_name = '接收合同' WHERE id = 531 AND perm_code = 'BTN_LOAN_RECEIVE';
UPDATE dafuweng_auth.sys_permission SET perm_name = '初审' WHERE id = 532 AND perm_code = 'BTN_LOAN_REVIEW';
UPDATE dafuweng_auth.sys_permission SET perm_name = '提交银行' WHERE id = 533 AND perm_code = 'BTN_LOAN_SUBMIT_BANK';

-- 财务API权限
UPDATE dafuweng_auth.sys_permission SET perm_name = '获取银行详情' WHERE id = 601 AND perm_code = 'API_BANK_GET';
UPDATE dafuweng_auth.sys_permission SET perm_name = '创建银行' WHERE id = 602 AND perm_code = 'API_BANK_POST';
UPDATE dafuweng_auth.sys_permission SET perm_name = '更新银行' WHERE id = 603 AND perm_code = 'API_BANK_PUT';
UPDATE dafuweng_auth.sys_permission SET perm_name = '删除银行' WHERE id = 604 AND perm_code = 'API_BANK_DEL';
UPDATE dafuweng_auth.sys_permission SET perm_name = '产品分页查询' WHERE id = 610 AND perm_code = 'API_PRODUCT_PAGE';
UPDATE dafuweng_auth.sys_permission SET perm_name = '获取单个产品' WHERE id = 611 AND perm_code = 'API_PRODUCT_GET';
UPDATE dafuweng_auth.sys_permission SET perm_name = '创建产品' WHERE id = 612 AND perm_code = 'API_PRODUCT_POST';
UPDATE dafuweng_auth.sys_permission SET perm_name = '更新产品' WHERE id = 613 AND perm_code = 'API_PRODUCT_PUT';
UPDATE dafuweng_auth.sys_permission SET perm_name = '删除产品' WHERE id = 614 AND perm_code = 'API_PRODUCT_DEL';
UPDATE dafuweng_auth.sys_permission SET perm_name = '贷款分页查询' WHERE id = 620 AND perm_code = 'API_LOAN_PAGE';
UPDATE dafuweng_auth.sys_permission SET perm_name = '获取单个贷款' WHERE id = 621 AND perm_code = 'API_LOAN_GET';
UPDATE dafuweng_auth.sys_permission SET perm_name = '接收合同' WHERE id = 622 AND perm_code = 'API_LOAN_RECEIVE';
UPDATE dafuweng_auth.sys_permission SET perm_name = '初审/终审' WHERE id = 623 AND perm_code = 'API_LOAN_REVIEW';

-- =====================================================
-- 2. 修复 sales.contact_record 表 (联系记录)
-- =====================================================
UPDATE dafuweng_sales.contact_record SET content = '电话沟通，客户表示对经营贷有兴趣，需要50万流动资金，计划下月签约' WHERE id = 1;
UPDATE dafuweng_sales.contact_record SET content = '面谈，详细介绍了产品，客户当场表示认可' WHERE id = 2;
UPDATE dafuweng_sales.contact_record SET content = '初次电话，了解客户需求，推荐个人消费贷' WHERE id = 3;
UPDATE dafuweng_sales.contact_record SET content = '公司面谈，法人张总接待，有实际经营贷款需求，金额500万' WHERE id = 4;
UPDATE dafuweng_sales.contact_record SET content = '客户表示暂时不需要' WHERE id = 5;
UPDATE dafuweng_sales.contact_record SET content = '测试超大数据金额客户' WHERE id = 6;

-- =====================================================
-- 3. 修复 sales.work_log 表 (工作日志)
-- =====================================================
UPDATE dafuweng_sales.work_log SET content = '今日主要跟进高意向客户，准备下周签约' WHERE id = 1;
UPDATE dafuweng_sales.work_log SET content = '签约1个合同，意向客户持续跟进中' WHERE id = 2;
UPDATE dafuweng_sales.work_log SET content = '企业客户面谈，效果良好' WHERE id = 3;
UPDATE dafuweng_sales.work_log SET content = '休息日，无工作' WHERE id = 4;
UPDATE dafuweng_sales.work_log SET content = '集中外呼活动，数据爆表' WHERE id = 5;

-- =====================================================
-- 4. 修复 sales.customer_transfer_log 表 (转移日志)
-- =====================================================
UPDATE dafuweng_sales.customer_transfer_log SET operate_type = '公海流入', reason = '超期未跟进，自动流入公海' WHERE id = 1;
UPDATE dafuweng_sales.customer_transfer_log SET operate_type = '经理分配', reason = '经理分配' WHERE id = 2;
UPDATE dafuweng_sales.customer_transfer_log SET operate_type = '部门调配', reason = '工作调整' WHERE id = 3;

-- =====================================================
-- 5. 修复 sales.performance_record 表 (业绩记录)
-- =====================================================
UPDATE dafuweng_sales.performance_record SET cancel_reason = '合同被拒，取消业绩' WHERE id = 4;

-- =====================================================
-- 6. 修复 finance.loan_audit_record 表 (贷款审核记录)
-- =====================================================
UPDATE dafuweng_finance.loan_audit_record SET operator_name = '金融专员A', operator_role = '金融专员', action = '接收', content = '接收合同，开始初审' WHERE id = 1;
UPDATE dafuweng_finance.loan_audit_record SET operator_name = '金融专员A', operator_role = '金融专员', action = '初审', content = '初审通过，建议额度50万，24期' WHERE id = 2;
UPDATE dafuweng_finance.loan_audit_record SET operator_name = '金融专员A', operator_role = '金融专员', action = '提交银行', content = '提交工商银行审批' WHERE id = 3;
UPDATE dafuweng_finance.loan_audit_record SET operator_name = '金融专员A', operator_role = '金融专员', action = '接收', content = '接收合同' WHERE id = 4;
UPDATE dafuweng_finance.loan_audit_record SET operator_name = '金融专员A', operator_role = '金融专员', action = '初审', content = '企业资质良好' WHERE id = 5;
UPDATE dafuweng_finance.loan_audit_record SET operator_name = '金融专员A', operator_role = '金融专员', action = '提交银行', content = '提交建设银行' WHERE id = 6;
UPDATE dafuweng_finance.loan_audit_record SET operator_name = '金融专员A', operator_role = '金融专员', action = '银行结果', content = '银行审批通过' WHERE id = 7;
UPDATE dafuweng_finance.loan_audit_record SET operator_name = '审计员', operator_role = '审计员', action = '批准', content = '终审批准，放款' WHERE id = 8;
UPDATE dafuweng_finance.loan_audit_record SET operator_name = '金融专员A', operator_role = '金融专员', action = '提交银行', content = '提交工商银行' WHERE id = 9;
UPDATE dafuweng_finance.loan_audit_record SET operator_name = '金融专员A', operator_role = '金融专员', action = '银行结果', content = '银行拒绝' WHERE id = 10;
UPDATE dafuweng_finance.loan_audit_record SET operator_name = '审计员', operator_role = '审计员', action = '拒绝', content = '终审拒绝，原因：行业风险过高' WHERE id = 11;

-- =====================================================
-- 7. 验证修复结果
-- =====================================================
SELECT '=== 验证菜单修复 ===' as info;
SELECT id, perm_code, perm_name FROM dafuweng_auth.sys_permission WHERE parent_id IN (0, 100, 200) ORDER BY id LIMIT 20;

SELECT '=== 验证联系记录修复 ===' as info;
SELECT id, content FROM dafuweng_sales.contact_record ORDER BY id LIMIT 3;

SELECT '=== 验证工作日志修复 ===' as info;
SELECT id, content FROM dafuweng_sales.work_log ORDER BY id LIMIT 3;

SELECT '=== 验证转移日志修复 ===' as info;
SELECT id, operate_type, reason FROM dafuweng_sales.customer_transfer_log ORDER BY id LIMIT 3;

SELECT '=== 验证贷款审核记录修复 ===' as info;
SELECT id, operator_name, action, content FROM dafuweng_finance.loan_audit_record ORDER BY id LIMIT 5;

SELECT '=== 修复完成 ===' as result;
