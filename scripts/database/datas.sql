-- ============================================================
-- NeoCC 开发环境测试数据
-- 生成时间: 2026-04-14
-- 说明: 包含常规数据 + 边界数据（超长字符、空值、极端值）
-- 密码统一为: 123456 (BCrypt加密)
-- ============================================================

USE dafuweng_auth;
-- ============================================================
-- AUTH 模块
-- ============================================================

-- 密码: 123456 (BCrypt). 实际值: $2a$10$xGz4rPQrkJQZJyJQPVgO4eLpNpLQnYQQRmGHHhZq6jLQpKlKlKlKK
INSERT INTO sys_user (id, username, password, real_name, phone, email, dept_id, zone_id, status, login_error_count, lock_time, last_login_time, last_login_ip, created_by, created_at, updated_by, updated_at, deleted, version) VALUES
-- 正常用户
(1, 'admin', '$2a$10$xGz4rPQrkJQZJyJQPVgO4eLpNpLQnYQQRmGHHhZq6jLQpKlKlKlKK', '超级管理员', '13800000001', 'admin@neocc.com', 1, 1, 1, 0, NULL, '2026-04-14 08:00:00', '127.0.0.1', NULL, '2026-01-01 00:00:00', NULL, '2026-04-14 08:00:00', 0, 1),
(2, 'zhangsan', '$2a$10$xGz4rPQrkJQZJyJQPVgO4eLpNpLQnYQQRmGHHhZq6jLQpKlKlKlKK', '张三', '13800000002', 'zhangsan@neocc.com', 1, 1, 1, 0, NULL, '2026-04-13 15:30:00', '192.168.1.100', 1, '2026-01-05 09:00:00', NULL, '2026-04-13 15:30:00', 0, 1),
(3, 'lisi', '$2a$10$xGz4rPQrkJQZJyJQPVgO4eLpNpLQnYQQRmGHHhZq6jLQpKlKlKlKK', '李四', '13800000003', 'lisi@neocc.com', 2, 1, 1, 0, NULL, '2026-04-12 10:00:00', '192.168.1.101', 1, '2026-01-10 10:00:00', NULL, '2026-04-12 10:00:00', 0, 1),
(4, 'wangwu', '$2a$10$xGz4rPQrkJQZJyJQPVgO4eLpNpLQnYQQRmGHHhZq6jLQpKlKlKlKK', '王五', '13800000004', 'wangwu@neocc.com', 2, 2, 1, 0, NULL, '2026-04-11 14:00:00', '10.0.0.50', 1, '2026-01-15 11:00:00', NULL, '2026-04-11 14:00:00', 0, 1),
(5, 'zhaoliu', '$2a$10$xGz4rPQrkJQZJyJQPVgO4eLpNpLQnYQQRmGHHhZq6jLQpKlKlKlKK', '赵六', '13800000005', 'zhaoliu@neocc.com', 3, 2, 1, 0, NULL, '2026-04-10 16:00:00', '172.16.0.10', 1, '2026-02-01 09:00:00', NULL, '2026-04-10 16:00:00', 0, 1),
-- 边界: 已禁用用户
(6, 'disabled_user', '$2a$10$xGz4rPQrkJQZJyJQPVgO4eLpNpLQnYQQRmGHHhZq6jLQpKlKlKlKK', '已禁用用户', '13800000006', 'disabled@neocc.com', 1, 1, 0, 0, NULL, NULL, NULL, 1, '2026-02-01 00:00:00', NULL, '2026-03-01 00:00:00', 0, 1),
-- 边界: 连续登录失败被锁定的用户 (锁30分钟)
(7, 'locked_user', '$2a$10$xGz4rPQrkJQZJyJQPVgO4eLpNpLQnYQQRmGHHhZq6jLQpKlKlKlKK', '锁定用户', '13800000007', 'locked@neocc.com', 1, 1, 1, 5, '2026-04-14 09:30:00', NULL, NULL, 1, '2026-02-10 00:00:00', NULL, '2026-04-14 09:00:00', 0, 1),
-- 边界: 空字段用户 (phone/email/dept_id/zone_id 为NULL)
(8, 'null_fields_user', '$2a$10$xGz4rPQrkJQZJyJQPVgO4eLpNpLQnYQQRmGHHhZq6jLQpKlKlKlKK', '空字段用户', NULL, NULL, NULL, NULL, 1, 0, NULL, NULL, NULL, 1, '2026-03-01 00:00:00', NULL, '2026-03-01 00:00:00', 0, 1),
-- 边界: 超长手机号 (21位)
(9, 'long_phone_user', '$2a$10$xGz4rPQrkJQZJyJQPVgO4eLpNpLQnYQQRmGHHhZq6jLQpKlKlKlKK', '超长手机用户', '1380000000012345678901', 'longphone@neocc.com', 1, 1, 1, 0, NULL, NULL, NULL, 1, '2026-03-05 00:00:00', NULL, '2026-03-05 00:00:00', 0, 1),
-- 边界: 已删除用户 (软删除)
(10, 'deleted_user', '$2a$10$xGz4rPQrkJQZJyJQPVgO4eLpNpLQnYQQRmGHHhZq6jLQpKlKlKlKK', '已删除用户', '13800000010', 'deleted@neocc.com', 1, 1, 1, 0, NULL, NULL, NULL, 1, '2026-01-01 00:00:00', 1, '2026-04-01 00:00:00', 1, 1);

-- 角色
INSERT INTO sys_role (id, role_code, role_name, data_scope, role_sort, status, created_by, created_at, updated_by, updated_at, deleted) VALUES
(1, 'SUPER_ADMIN', '超级管理员', 4, 1, 1, NULL, '2026-01-01 00:00:00', NULL, '2026-01-01 00:00:00', 0),
(2, 'DEPT_MANAGER', '部门经理', 2, 2, 1, 1, '2026-01-05 00:00:00', NULL, '2026-02-01 00:00:00', 0),
(3, 'ZONE_DIRECTOR', '战区总监', 3, 3, 1, 1, '2026-01-05 00:00:00', NULL, '2026-02-01 00:00:00', 0),
(4, 'SALES_REP', '销售代表', 1, 4, 1, 1, '2026-01-10 00:00:00', NULL, '2026-02-10 00:00:00', 0),
(5, 'FINANCE_SPECIALIST', '金融专员', 1, 5, 1, 1, '2026-01-10 00:00:00', NULL, '2026-02-10 00:00:00', 0),
(6, 'AUDITOR', '审计员', 4, 6, 1, 1, '2026-01-15 00:00:00', NULL, '2026-02-15 00:00:00', 0),
-- 边界: 禁用状态角色
(7, 'DISABLED_ROLE', '已禁用角色', 1, 99, 0, 1, '2026-02-01 00:00:00', NULL, '2026-03-01 00:00:00', 0);

-- 权限 (菜单+按钮+API混合)
INSERT INTO sys_permission (id, parent_id, perm_code, perm_name, perm_type, perm_path, icon, sort_order, status, external_link, created_at, updated_at, deleted) VALUES
-- 根目录
(1, 0, 'SYSTEM', '系统管理', 1, '/system', 'setting', 1, 1, 0, '2026-01-01 00:00:00', '2026-01-01 00:00:00', 0),
-- 系统管理子菜单
(2, 1, 'SYSTEM_USER', '用户管理', 1, '/system/user', 'user', 1, 1, 0, '2026-01-01 00:00:00', '2026-01-01 00:00:00', 0),
(3, 1, 'SYSTEM_ROLE', '角色管理', 1, '/system/role', 'team', 2, 1, 0, '2026-01-01 00:00:00', '2026-01-01 00:00:00', 0),
(4, 1, 'SYSTEM_PERMISSION', '权限管理', 1, '/system/permission', 'lock', 3, 1, 0, '2026-01-01 00:00:00', '2026-01-01 00:00:00', 0),
(5, 1, 'SYSTEM_DEPT', '部门管理', 1, '/system/department', 'apartment', 4, 1, 0, '2026-01-01 00:00:00', '2026-01-01 00:00:00', 0),
(6, 1, 'SYSTEM_ZONE', '战区管理', 1, '/system/zone', 'global', 5, 1, 0, '2026-01-01 00:00:00', '2026-01-01 00:00:00', 0),
-- 按钮权限
(101, 2, 'BTN_USER_ADD', '新增用户', 2, NULL, NULL, 1, 1, 0, '2026-01-01 00:00:00', '2026-01-01 00:00:00', 0),
(102, 2, 'BTN_USER_EDIT', '编辑用户', 2, NULL, NULL, 2, 1, 0, '2026-01-01 00:00:00', '2026-01-01 00:00:00', 0),
(103, 2, 'BTN_USER_DEL', '删除用户', 2, NULL, NULL, 3, 1, 0, '2026-01-01 00:00:00', '2026-01-01 00:00:00', 0),
(104, 2, 'BTN_USER_ALLOC_ROLE', '分配角色', 2, NULL, NULL, 4, 1, 0, '2026-01-01 00:00:00', '2026-01-01 00:00:00', 0),
(105, 2, 'BTN_USER_UNLOCK', '解锁用户', 2, NULL, NULL, 5, 1, 0, '2026-01-01 00:00:00', '2026-01-01 00:00:00', 0),
-- API权限
(201, 2, 'API_USER_PAGE', '用户分页查询', 3, '/api/sysUser/page', NULL, 1, 1, 0, '2026-01-01 00:00:00', '2026-01-01 00:00:00', 0),
(202, 2, 'API_USER_GET', '获取单个用户', 3, '/api/sysUser/{id}', NULL, 2, 1, 0, '2026-01-01 00:00:00', '2026-01-01 00:00:00', 0),
(203, 2, 'API_USER_POST', '创建用户', 3, '/api/sysUser', NULL, 3, 1, 0, '2026-01-01 00:00:00', '2026-01-01 00:00:00', 0),
(204, 2, 'API_USER_PUT', '更新用户', 3, '/api/sysUser', NULL, 4, 1, 0, '2026-01-01 00:00:00', '2026-01-01 00:00:00', 0),
(205, 2, 'API_USER_DEL', '删除用户', 3, '/api/sysUser/{id}', NULL, 5, 1, 0, '2026-01-01 00:00:00', '2026-01-01 00:00:00', 0),
-- 边界: 禁用状态权限
(999, 1, 'SYSTEM_DISABLED_MENU', '已禁用菜单', 1, '/system/disabled', 'stop', 99, 0, 0, '2026-01-01 00:00:00', '2026-03-01 00:00:00', 0),
-- 边界: 外链菜单
(998, 1, 'SYSTEM_EXTERNAL_LINK', '外部链接', 1, 'https://www.baidu.com', 'link', 98, 1, 1, '2026-01-01 00:00:00', '2026-01-01 00:00:00', 0);

-- 用户-角色关联
INSERT INTO sys_user_role (id, user_id, role_id, created_at) VALUES
(1, 1, 1, '2026-01-01 00:00:00'),  -- admin -> SUPER_ADMIN
(2, 2, 2, '2026-01-05 00:00:00'),  -- zhangsan -> DEPT_MANAGER
(3, 3, 4, '2026-01-10 00:00:00'),  -- lisi -> SALES_REP
(4, 4, 4, '2026-01-15 00:00:00'),  -- wangwu -> SALES_REP
(5, 5, 4, '2026-02-01 00:00:00'),  -- zhaoliu -> SALES_REP
(6, 6, 1, '2026-02-01 00:00:00'),  -- disabled_user -> SUPER_ADMIN (但账号已禁用)
(7, 7, 4, '2026-02-10 00:00:00'),  -- locked_user -> SALES_REP (但被锁)
(8, 8, 4, '2026-03-01 00:00:00'),  -- null_fields_user -> SALES_REP
(9, 9, 4, '2026-03-05 00:00:00'); -- long_phone_user -> SALES_REP

-- 角色-权限关联
INSERT INTO sys_role_permission (id, role_id, permission_id, created_at) VALUES
(1, 1, 1, '2026-01-01 00:00:00'),   -- SUPER_ADMIN -> SYSTEM
(2, 1, 2, '2026-01-01 00:00:00'),   -- SUPER_ADMIN -> SYSTEM_USER
(3, 1, 3, '2026-01-01 00:00:00'),   -- SUPER_ADMIN -> SYSTEM_ROLE
(4, 1, 4, '2026-01-01 00:00:00'),   -- SUPER_ADMIN -> SYSTEM_PERMISSION
(5, 1, 5, '2026-01-01 00:00:00'),   -- SUPER_ADMIN -> SYSTEM_DEPT
(6, 1, 6, '2026-01-01 00:00:00'),   -- SUPER_ADMIN -> SYSTEM_ZONE
(7, 2, 2, '2026-01-05 00:00:00'),   -- DEPT_MANAGER -> SYSTEM_USER
(8, 2, 101, '2026-01-05 00:00:00'), -- DEPT_MANAGER -> BTN_USER_ADD
(9, 2, 102, '2026-01-05 00:00:00'), -- DEPT_MANAGER -> BTN_USER_EDIT
(10, 3, 6, '2026-01-05 00:00:00'),  -- ZONE_DIRECTOR -> SYSTEM_ZONE
(11, 4, 2, '2026-01-10 00:00:00'), -- SALES_REP -> SYSTEM_USER (只读)
(12, 4, 201, '2026-01-10 00:00:00'),-- SALES_REP -> API_USER_PAGE
(13, 4, 202, '2026-01-10 00:00:00');-- SALES_REP -> API_USER_GET


USE dafuweng_system;
-- ============================================================
-- SYSTEM 模块
-- ============================================================

-- 战区
INSERT INTO sys_zone (id, zone_code, zone_name, director_id, sort_order, status, created_by, created_at, updated_by, updated_at, deleted) VALUES
(1, 'ZONE_EAST', '东部战区', 4, 1, 1, 1, '2026-01-01 00:00:00', NULL, '2026-01-01 00:00:00', 0),
(2, 'ZONE_WEST', '西部战区', 5, 2, 1, 1, '2026-01-01 00:00:00', NULL, '2026-01-01 00:00:00', 0),
(3, 'ZONE_SOUTH', '南部战区', NULL, 3, 1, 1, '2026-01-10 00:00:00', NULL, '2026-01-10 00:00:00', 0),  -- 无总监
(4, 'ZONE_NORTH', '北部战区', NULL, 4, 0, 1, '2026-01-15 00:00:00', NULL, '2026-02-01 00:00:00', 0),  -- 已禁用
-- 边界: 极端排序值
(5, 'ZONE_MIN_SORT', '最小排序战区', NULL, -2147483648, 1, 1, '2026-02-01 00:00:00', NULL, '2026-02-01 00:00:00', 0),
(6, 'ZONE_MAX_SORT', '最大排序战区', NULL, 2147483647, 1, 1, '2026-02-01 00:00:00', NULL, '2026-02-01 00:00:00', 0);

-- 部门
INSERT INTO sys_department (id, dept_code, dept_name, parent_id, zone_id, manager_id, sort_order, status, created_by, created_at, updated_by, updated_at, deleted) VALUES
-- 顶级部门
(1, 'DEPT_HQ', '总部', 0, 1, 2, 1, 1, 1, '2026-01-01 00:00:00', NULL, '2026-01-01 00:00:00', 0),
-- 二级部门
(2, 'DEPT_SALES', '销售部', 1, 1, 3, 1, 1, 1, '2026-01-05 00:00:00', NULL, '2026-01-05 00:00:00', 0),
(3, 'DEPT_FINANCE', '财务部', 1, 1, NULL, 2, 1, 1, '2026-01-05 00:00:00', NULL, '2026-01-05 00:00:00', 0),  -- 无经理
(4, 'DEPT_OPERATIONS', '运营部', 1, 2, 5, 3, 1, 1, '2026-01-10 00:00:00', NULL, '2026-01-10 00:00:00', 0),
-- 三级部门 (销售部下)
(5, 'DEPT_SALES_EAST', '销售一部', 2, 1, 3, 1, 1, 1, '2026-01-15 00:00:00', NULL, '2026-01-15 00:00:00', 0),
(6, 'DEPT_SALES_WEST', '销售二部', 2, 2, 4, 2, 1, 1, '2026-01-15 00:00:00', NULL, '2026-01-15 00:00:00', 0),
-- 边界: 已禁用部门
(7, 'DEPT_DISABLED', '已禁用部门', 1, 1, NULL, 99, 0, 1, '2026-02-01 00:00:00', NULL, '2026-03-01 00:00:00', 0),
-- 边界: 独立于任何战区的部门
(8, 'DEPT_NO_ZONE', '无战区部门', 1, NULL, NULL, 5, 1, 1, '2026-03-01 00:00:00', NULL, '2026-03-01 00:00:00', 0);

-- 数据字典
INSERT INTO sys_dict (id, dict_type, dict_code, dict_label, dict_value, sort_order, status, remark, created_at, updated_at, deleted) VALUES
-- 客户意向等级
(1, 'intention_level', 'LEVEL_A', 'A级(高意向)', '1', 1, 1, '非常有意向', '2026-01-01 00:00:00', '2026-01-01 00:00:00', 0),
(2, 'intention_level', 'LEVEL_B', 'B级(中意向)', '2', 2, 1, '有意向', '2026-01-01 00:00:00', '2026-01-01 00:00:00', 0),
(3, 'intention_level', 'LEVEL_C', 'C级(低意向)', '3', 3, 1, '考虑中', '2026-01-01 00:00:00', '2026-01-01 00:00:00', 0),
(4, 'intention_level', 'LEVEL_D', 'D级(无意向)', '4', 4, 1, '暂不考虑', '2026-01-01 00:00:00', '2026-01-01 00:00:00', 0),
-- 客户状态
(5, 'customer_status', 'STATUS_POTENTIAL', '潜在客户', '1', 1, 1, NULL, '2026-01-01 00:00:00', '2026-01-01 00:00:00', 0),
(6, 'customer_status', 'STATUS_NEGOTIATING', '洽谈中', '2', 2, 1, NULL, '2026-01-01 00:00:00', '2026-01-01 00:00:00', 0),
(7, 'customer_status', 'STATUS_SIGNED', '已签约', '3', 3, 1, NULL, '2026-01-01 00:00:00', '2026-01-01 00:00:00', 0),
(8, 'customer_status', 'STATUS_LOANED', '已放款', '4', 4, 1, NULL, '2026-01-01 00:00:00', '2026-01-01 00:00:00', 0),
(9, 'customer_status', 'STATUS_PUBLIC_SEA', '公海客户', '5', 5, 1, NULL, '2026-01-01 00:00:00', '2026-01-01 00:00:00', 0),
-- 客户类型
(10, 'customer_type', 'TYPE_PERSONAL', '个人客户', '1', 1, 1, NULL, '2026-01-01 00:00:00', '2026-01-01 00:00:00', 0),
(11, 'customer_type', 'TYPE_ENTERPRISE', '企业客户', '2', 2, 1, NULL, '2026-01-01 00:00:00', '2026-01-01 00:00:00', 0),
-- 边界: 禁用字典项
(99, 'customer_status', 'STATUS_DISABLED', '已停用', '99', 99, 0, '已停用的状态', '2026-01-01 00:00:00', '2026-03-01 00:00:00', 0);

-- 系统参数
INSERT INTO sys_param (id, param_key, param_value, param_type, param_group, remark, sort_order, status, created_by, created_at, updated_by, updated_at, deleted) VALUES
-- 常规参数
(1, 'sys.max_login_retry', '5', 'int', 'security', '最大登录失败重试次数', 1, 1, 1, '2026-01-01 00:00:00', NULL, '2026-01-01 00:00:00', 0),
(2, 'sys.lock_duration_minutes', '30', 'int', 'security', '账户锁定时长(分钟)', 2, 1, 1, '2026-01-01 00:00:00', NULL, '2026-01-01 00:00:00', 0),
(3, 'sys.session_timeout', '7200', 'int', 'security', '会话超时时间(秒)', 3, 1, 1, '2026-01-01 00:00:00', NULL, '2026-01-01 00:00:00', 0),
(4, 'sys.company.name', 'NeoCC金融科技公司', 'string', 'company', '公司名称', 1, 1, 1, '2026-01-01 00:00:00', NULL, '2026-01-01 00:00:00', 0),
(5, 'sys.pagination.default_size', '15', 'int', 'pagination', '默认分页大小', 1, 1, 1, '2026-01-01 00:00:00', NULL, '2026-01-01 00:00:00', 0),
(6, 'sys.pagination.max_size', '100', 'int', 'pagination', '最大分页大小', 2, 1, 1, '2026-01-01 00:00:00', NULL, '2026-01-01 00:00:00', 0),
-- JSON类型参数
(7, 'sys.supported.id_types', '["身份证","护照","营业执照"]', 'json', 'customer', '支持的证件类型', 1, 1, 1, '2026-01-01 00:00:00', NULL, '2026-01-01 00:00:00', 0),
-- Boolean类型参数
(8, 'sys.water_mark.enabled', 'true', 'boolean', 'security', '是否开启水印', 4, 1, 1, '2026-01-01 00:00:00', NULL, '2026-01-01 00:00:00', 0),
-- 边界: 边界数值参数
(9, 'sys.min.amount', '0.01', 'string', 'finance', '最小金额', 1, 1, 1, '2026-01-01 00:00:00', NULL, '2026-01-01 00:00:00', 0),
(10, 'sys.max.amount', '999999999999.99', 'string', 'finance', '最大金额', 2, 1, 1, '2026-01-01 00:00:00', NULL, '2026-01-01 00:00:00', 0),
-- 边界: 禁用参数
(11, 'sys.deprecated.param', 'do_not_use', 'string', 'deprecated', '已废弃参数', 99, 0, 1, '2026-01-01 00:00:00', NULL, '2026-03-01 00:00:00', 0),
-- 边界: 超长value (2000字符)
(12, 'sys.long.value.param', 'AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA', 'string', 'boundary', '超长参数值测试', 100, 1, 1, '2026-01-01 00:00:00', NULL, '2026-01-01 00:00:00', 0);

-- 操作日志
INSERT INTO sys_operation_log (id, user_id, username, module, action, request_method, request_url, request_params, response_code, error_msg, ip, user_agent, cost_time_ms, created_at) VALUES
-- 正常日志
(1, 1, 'admin', '用户管理', '用户登录', 'POST', '/api/sysUser/login', '{"username":"admin"}', '200', NULL, '127.0.0.1', 'Mozilla/5.0', 150, '2026-04-14 08:00:00'),
(2, 2, 'zhangsan', '客户管理', '创建客户', 'POST', '/api/customer', '{"name":"张三公司"}', '200', NULL, '192.168.1.100', 'Mozilla/5.0', 320, '2026-04-13 10:00:00'),
(3, 3, 'lisi', '合同管理', '签约', 'POST', '/api/contract', '{"contractNo":"HT20260413001"}', '200', NULL, '192.168.1.101', 'Mozilla/5.0', 850, '2026-04-12 14:00:00'),
-- 边界: 错误响应
(4, 1, 'admin', '用户管理', '删除用户', 'DELETE', '/api/sysUser/999', NULL, '404', '用户不存在', '127.0.0.1', 'Mozilla/5.0', 50, '2026-04-14 09:00:00'),
(5, 2, 'zhangsan', '客户管理', '创建客户', 'POST', '/api/customer', '{"name":"重复公司"}', '500', '系统异常: 客户已存在', '192.168.1.100', 'Mozilla/5.0', 1200, '2026-04-13 11:00:00'),
-- 边界: 超长耗时请求
(6, 1, 'admin', '数据导出', '批量导出', 'GET', '/api/customer/export', '{"dateFrom":"2025-01-01","dateTo":"2026-04-14"}', '200', NULL, '127.0.0.1', 'Mozilla/5.0', 35678, '2026-04-10 00:00:00'),
-- 边界: OPTIONS请求(CORS预检)
(7, 1, 'admin', '系统', 'CORS预检', 'OPTIONS', '/api/sysUser/page', NULL, '200', NULL, '127.0.0.1', 'Mozilla/5.0', 5, '2026-04-14 08:00:01');


USE dafuweng_sales;
-- ============================================================
-- SALES 模块
-- ============================================================

-- 客户
INSERT INTO customer (id, name, phone, id_card, company_name, company_legal_person, company_reg_capital, customer_type, sales_rep_id, dept_id, zone_id, intention_level, status, last_contact_date, next_follow_up_date, public_sea_time, public_sea_reason, annotation, source, loan_intention_amount, loan_intention_product, created_by, created_at, updated_by, updated_at, deleted, version) VALUES
-- 正常个人客户
(1, '陈小明', '13900001001', '110101199001011234', NULL, NULL, NULL, 1, 3, 2, 1, 1, 2, '2026-04-13 10:00:00', '2026-04-20 10:00:00', NULL, NULL, '{"remark":"高意向个人客户"}', '电话营销', 500000.00, '企业经营贷', 3, '2026-03-01 09:00:00', NULL, '2026-04-13 10:00:00', 0, 1),
(2, '李大国', '13900001002', '110101199002022345', NULL, NULL, NULL, 1, 3, 2, 1, 2, 3, '2026-04-10 15:00:00', '2026-04-17 15:00:00', NULL, NULL, NULL, '朋友推荐', 300000.00, '个人消费贷', 3, '2026-03-05 14:00:00', NULL, '2026-04-10 15:00:00', 0, 1),
-- 正常企业客户
(3, '深圳市某某科技有限公司', '13900001003', '91440300MA5DG8K123', '张法人', 10000000.00, 2, 4, 2, 4, 1, 1, 4, '2026-04-12 09:00:00', '2026-04-19 09:00:00', NULL, NULL, '{"industry":"科技","employeeCount":50}', '展会获客', 5000000.00, '企业经营贷', 4, '2026-02-15 10:00:00', NULL, '2026-04-12 09:00:00', 0, 1),
(4, '广州某某贸易公司', '13900001004', '91440101MA5CK7L456', '刘法人', 5000000.00, 2, 4, 2, 5, 2, 2, 2, '2026-04-11 11:00:00', '2026-04-25 11:00:00', NULL, NULL, NULL, '网络推广', 2000000.00, '供应链金融', 4, '2026-03-10 08:00:00', NULL, '2026-04-11 11:00:00', 0, 1),
-- 公海客户 (长时间未跟进)
(5, '周超', '13900001005', '110101199003033456', NULL, NULL, NULL, 1, NULL, 2, 1, 3, 5, '2026-01-01 00:00:00', NULL, '2026-04-01 00:00:00', '超过90天未跟进', NULL, '渠道不明', 100000.00, NULL, 3, '2025-06-01 00:00:00', NULL, '2026-04-01 00:00:00', 0, 1),
-- 边界: 最小金额
(6, '边界测试_最小金额', '13900001006', '110101199004044567', NULL, NULL, NULL, 1, 3, 2, 1, 1, 1, '2026-04-09 00:00:00', '2026-04-16 00:00:00', NULL, NULL, NULL, '测试', 0.01, '测试产品', 3, '2026-04-09 00:00:00', NULL, '2026-04-09 00:00:00', 0, 1),
-- 边界: 最大金额
(7, '边界测试_最大金额', '13900001007', '110101199005055678', NULL, NULL, NULL, 1, 3, 2, 1, 1, 1, '2026-04-08 00:00:00', '2026-04-15 00:00:00', NULL, NULL, NULL, '测试', 9999999999.99, '测试产品', 3, '2026-04-08 00:00:00', NULL, '2026-04-08 00:00:00', 0, 1),
-- 边界: 已放款客户
(8, '已放款客户', '13900001008', '110101199006066789', NULL, NULL, NULL, 1, 4, 2, 1, 4, 4, '2026-03-01 00:00:00', NULL, NULL, NULL, NULL, '老客户复借', 800000.00, '企业经营贷', 4, '2025-12-01 00:00:00', NULL, '2026-03-01 00:00:00', 0, 1),
-- 边界: 无销售负责人的客户
(9, '无负责人客户', '13900001009', '110101199007077890', NULL, NULL, NULL, 1, NULL, 2, 1, 2, 1, '2026-04-05 00:00:00', '2026-04-12 00:00:00', NULL, NULL, NULL, '自然到店', 500000.00, '个人消费贷', 3, '2026-04-05 00:00:00', NULL, '2026-04-05 00:00:00', 0, 1),
-- 边界: 身份证号格式错误(15位旧版)
(10, '旧版身份证客户', '13900001010', '110101900101123', NULL, NULL, NULL, 1, 3, 2, 1, 1, 1, '2026-04-01 00:00:00', '2026-04-08 00:00:00', NULL, NULL, NULL, '测试', 100000.00, '测试', 3, '2026-04-01 00:00:00', NULL, '2026-04-01 00:00:00', 0, 1);

-- 联系记录
INSERT INTO contact_record (id, customer_id, sales_rep_id, contact_type, contact_date, content, intention_before, intention_after, follow_up_date, attachment_urls, created_by, created_at, updated_by, updated_at, deleted) VALUES
(1, 1, 3, 1, '2026-04-13 10:00:00', '电话沟通，客户表示对经营贷有兴趣，需要50万流动资金，计划下月签约', 2, 1, '2026-04-20 10:00:00', NULL, 3, '2026-04-13 10:30:00', NULL, '2026-04-13 10:30:00', 0),
(2, 1, 3, 2, '2026-04-06 14:00:00', '面谈，详细介绍了产品，客户当场表示认可', 2, 1, '2026-04-13 10:00:00', '["/uploads/contact_1.jpg"]', 3, '2026-04-06 16:00:00', NULL, '2026-04-06 16:00:00', 0),
(3, 2, 3, 1, '2026-04-10 15:00:00', '初次电话，了解客户需求，推荐个人消费贷', 3, 2, '2026-04-17 15:00:00', NULL, 3, '2026-04-10 15:30:00', NULL, '2026-04-10 15:30:00', 0),
(4, 3, 4, 2, '2026-04-12 09:00:00', '公司面谈，法人张总接待，有实际经营贷款需求，金额500万', 2, 1, '2026-04-19 09:00:00', '["/uploads/business_license.jpg","/uploads/financial_statement.pdf"]', 4, '2026-04-12 11:00:00', NULL, '2026-04-12 11:00:00', 0),
-- 边界: 意向下降的记录
(5, 5, 3, 1, '2026-01-01 10:00:00', '客户表示暂时不需要', 2, 4, NULL, NULL, 3, '2026-01-01 11:00:00', NULL, '2026-01-01 11:00:00', 0),
-- 边界: 边界金额客户的面谈
(6, 7, 3, 2, '2026-04-08 09:00:00', '测试超大数据金额客户', 1, 1, '2026-04-15 09:00:00', NULL, 3, '2026-04-08 10:00:00', NULL, '2026-04-08 10:00:00', 0);

-- 合同
INSERT INTO contract (id, contract_no, customer_id, sales_rep_id, dept_id, zone_id, product_id, contract_amount, actual_loan_amount, service_fee_rate, service_fee_1, service_fee_2, service_fee_1_paid, service_fee_2_paid, service_fee_1_pay_date, service_fee_2_pay_date, status, sign_date, paper_contract_no, finance_send_time, finance_receive_time, loan_use, guarantee_info, reject_reason, remark, created_by, created_at, updated_by, updated_at, deleted, version) VALUES
-- 正常签约合同
(1, 'HT20260412001', 1, 3, 2, 1, 1, 500000.00, 500000.00, 0.0300, 15000.00, 5000.00, 1, 0, '2026-04-12', NULL, 4, '2026-04-12', 'P20260412001', '2026-04-13 09:00:00', NULL, '流动资金周转', NULL, NULL, '客户资质良好', 3, '2026-04-12 18:00:00', NULL, '2026-04-13 09:00:00', 0, 1),
-- 已放款合同
(2, 'HT20260301001', 8, 4, 2, 1, 2, 800000.00, 780000.00, 0.0250, 20000.00, 8000.00, 1, 1, '2026-03-01', '2026-03-20', 7, '2026-03-01', 'P20260301001', '2026-03-02 10:00:00', '2026-03-02 14:00:00', '设备采购', '{"type":"房产抵押","value":1500000}', NULL, NULL, 4, '2026-03-01 17:00:00', NULL, '2026-04-01 00:00:00', 0, 2),
-- 边界: 最小金额合同
(3, 'HT20260408001', 6, 3, 2, 1, 1, 0.01, NULL, 0.0300, NULL, NULL, 0, 0, NULL, NULL, 2, '2026-04-08', 'P20260408001', NULL, NULL, '测试', NULL, NULL, '边界金额测试', 3, '2026-04-08 10:00:00', NULL, '2026-04-08 10:00:00', 0, 1),
-- 边界: 服务费未付
(4, 'HT20260410001', 2, 3, 2, 1, 1, 300000.00, NULL, 0.0300, 9000.00, 3000.00, 0, 0, NULL, NULL, 3, '2026-04-10', 'P20260410001', NULL, NULL, '个人消费', NULL, NULL, NULL, 3, '2026-04-10 15:00:00', NULL, '2026-04-10 15:00:00', 0, 1),
-- 边界: 被拒绝的合同
(5, 'HT20260405001', 9, 3, 2, 1, 1, 500000.00, NULL, 0.0300, NULL, NULL, 0, 0, NULL, NULL, 6, '2026-04-05', NULL, NULL, NULL, '未知', NULL, '资质不符合要求：信用记录不良', '客户有逾期记录', 3, '2026-04-05 11:00:00', NULL, '2026-04-06 09:00:00', 0, 1),
-- 边界: 已完成合同
(6, 'HT20260115001', 3, 4, 2, 1, 2, 5000000.00, 4850000.00, 0.0200, 100000.00, 40000.00, 1, 1, '2026-01-15', '2026-02-10', 8, '2026-01-15', 'P20260115001', '2026-01-16 10:00:00', '2026-01-16 14:00:00', '扩大经营', '{"type":"担保人","name":"王保证"}', NULL, NULL, '老客户介绍', 4, '2026-01-15 16:00:00', NULL, '2026-04-01 00:00:00', 0, 3);

-- 合同附件
INSERT INTO contract_attachment (id, contract_id, attachment_type, file_url, file_name, file_size, file_md5, upload_by, upload_time, deleted) VALUES
(1, 1, 'id_card', '/uploads/contract/1/id_card_front.jpg', '身份证正面.jpg', 1024000, 'd41d8cd98f00b204e9800998ecf8427e', 3, '2026-04-12 10:00:00', 0),
(2, 1, 'id_card', '/uploads/contract/1/id_card_back.jpg', '身份证反面.jpg', 980000, 'a3c5d2e1f0099cbb9a6c9d8e7f601234', 3, '2026-04-12 10:05:00', 0),
(3, 2, 'business_license', '/uploads/contract/2/bl.jpg', '营业执照.jpg', 2048000, 'b3f7a2e5d8888cc99aa1122334455667', 4, '2026-03-01 11:00:00', 0),
(4, 2, 'other', '/uploads/contract/2/guarantee.pdf', '担保证明.pdf', 512000, 'c4d8e3f6a9990bb1aa22334455667788', 4, '2026-03-01 11:15:00', 0),
-- 边界: 大文件
(5, 6, 'other', '/uploads/contract/6/financial_statement.pdf', '财务报表.pdf', 10485760, 'f5e9b7c4a3321d0e9a8b7c6d5e4f3012', 4, '2026-01-15 12:00:00', 0);

-- 销售工作日志
INSERT INTO work_log (id, sales_rep_id, log_date, calls_made, effective_calls, new_intentions, intention_clients, face_to_face_clients, signed_contracts, content, created_at, updated_at, deleted) VALUES
(1, 3, '2026-04-13', 50, 30, 5, 10, 3, 0, '今日主要跟进高意向客户，准备下周签约', '2026-04-13 18:00:00', '2026-04-13 18:00:00', 0),
(2, 3, '2026-04-12', 45, 28, 3, 8, 2, 1, '签约1个合同，意向客户持续跟进中', '2026-04-12 18:00:00', '2026-04-12 18:00:00', 0),
(3, 4, '2026-04-13', 40, 25, 2, 6, 1, 0, '企业客户面谈，效果良好', '2026-04-13 18:00:00', '2026-04-13 18:00:00', 0),
-- 边界: 零数据
(4, 5, '2026-04-01', 0, 0, 0, 0, 0, 0, '休息日，无工作', '2026-04-01 18:00:00', '2026-04-01 18:00:00', 0),
-- 边界: 极端高数据
(5, 3, '2026-03-15', 999, 500, 50, 100, 30, 5, '集中外呼活动，数据爆表', '2026-03-15 20:00:00', '2026-03-15 20:00:00', 0);

-- 业绩记录
INSERT INTO performance_record (id, contract_id, sales_rep_id, dept_id, zone_id, contract_amount, commission_rate, commission_amount, status, calculate_time, confirm_time, grant_time, cancel_reason, remark, created_by, created_at, updated_by, updated_at, deleted) VALUES
(1, 2, 4, 2, 1, 800000.00, 0.0150, 12000.00, 3, '2026-03-05 10:00:00', '2026-03-10 14:00:00', '2026-03-15 16:00:00', NULL, NULL, 1, '2026-03-05 10:00:00', NULL, '2026-03-15 16:00:00', 0),
(2, 6, 4, 2, 1, 5000000.00, 0.0120, 60000.00, 3, '2026-01-20 10:00:00', '2026-01-25 11:00:00', '2026-02-01 15:00:00', NULL, '大额合同奖励', 1, '2026-01-20 10:00:00', NULL, '2026-02-01 15:00:00', 0),
-- 边界: 待确认
(3, 1, 3, 2, 1, 500000.00, 0.0150, 7500.00, 1, '2026-04-13 10:00:00', NULL, NULL, NULL, '待财务确认', 1, '2026-04-13 10:00:00', NULL, '2026-04-13 10:00:00', 0),
-- 边界: 已取消
(4, 5, 3, 2, 1, 500000.00, 0.0150, NULL, 4, NULL, NULL, NULL, '合同被拒，取消业绩', '合同未通过审核', 1, '2026-04-06 10:00:00', NULL, '2026-04-06 10:00:00', 0);

-- 客户转移记录
INSERT INTO customer_transfer_log (id, customer_id, from_rep_id, to_rep_id, operate_type, reason, operated_by, operated_at, deleted) VALUES
(1, 5, 3, NULL, 'public_sea_claim', '超期未跟进，自动流入公海', 2, '2026-04-01 00:00:00', 0),
(2, 9, NULL, 3, 'manager_assign', '经理分配', 2, '2026-04-05 09:00:00', 0),
-- 边界: 部门经理调配
(3, 3, 3, 4, 'dept_manager_transfer', '工作调整', 2, '2026-03-01 10:00:00', 0);


USE dafuweng_finance;
-- ============================================================
-- FINANCE 模块
-- ============================================================

-- 合作银行
INSERT INTO bank (id, bank_code, bank_name, bank_branch, contact_person, contact_phone, status, sort_order, created_by, created_at, updated_by, updated_at, deleted) VALUES
(1, 'ICBC', '中国工商银行', '深圳南山支行', '陈经理', '13800138001', 1, 1, 1, '2026-01-01 00:00:00', NULL, '2026-01-01 00:00:00', 0),
(2, 'CCB', '中国建设银行', '广州天河支行', '李经理', '13800138002', 1, 2, 1, '2026-01-01 00:00:00', NULL, '2026-01-01 00:00:00', 0),
(3, 'ABC', '中国农业银行', '北京朝阳支行', '王经理', '13800138003', 1, 3, 1, '1', '2026-01-01 00:00:00', NULL, '2026-01-01 00:00:00', 0),
(4, 'BOC', '中国银行', '上海浦东支行', '张经理', '13800138004', 1, 4, 1, '1', '2026-01-01 00:00:00', NULL, '2026-01-01 00:00:00', 0),
-- 边界: 暂停合作银行
(5, 'COMMUNITY_BANK', '某农村信用社', '广州分行', '刘主任', '13800138005', 0, 99, 1, '2026-02-01 00:00:00', NULL, '2026-03-01 00:00:00', 0);

-- 金融产品
INSERT INTO finance_product (id, product_code, product_name, bank_id, min_amount, max_amount, interest_rate, min_term, max_term, requirements, documents, product_features, commission_rate, status, sort_order, online_time, offline_time, created_by, created_at, updated_by, updated_at, deleted) VALUES
(1, 'ICBC_BIZ_001', '工商银行企业经营贷', 1, 100000.00, 5000000.00, 0.0450, 12, 60, '["营业执照满1年","近6个月银行流水","资产负债率<70%"]', '["营业执照原件","近6个月财务报表","法人身份证"]', '额度高，放款快，利率优惠', 0.0150, 1, 1, '2026-01-01 00:00:00', NULL, 1, '2026-01-01 00:00:00', NULL, '2026-01-01 00:00:00', 0),
(2, 'CCB_PERSONAL_001', '建设银行个人消费贷', 2, 50000.00, 1000000.00, 0.0550, 6, 36, '["月收入>8000","信用记录良好"]', '["收入证明","征信报告","身份证"]', '纯信用，无需抵押', 0.0120, 1, 2, '2026-01-01 00:00:00', NULL, 1, '2026-01-01 00:00:00', NULL, '2026-01-01 00:00:00', 0),
(3, 'ABC_SUPPLY_001', '农业银行供应链金融', 3, 200000.00, 3000000.00, 0.0400, 6, 48, '["核心企业上下游客户","购销合同"]', '["购销合同","发票","应收账款凭证"]', '依托核心企业，额度灵活', 0.0180, 1, 3, '2026-01-10 00:00:00', NULL, 1, '2026-01-10 00:00:00', NULL, '2026-01-10 00:00:00', 0),
-- 边界: 极端利率
(4, 'TEST_MIN_RATE', '测试最低利率', 1, 100000.00, 1000000.00, 0.0300, 12, 24, '[]', '[]', '测试用最低利率产品', 0.0100, 1, 10, '2026-01-01 00:00:00', NULL, 1, '2026-01-01 00:00:00', NULL, '2026-01-01 00:00:00', 0),
(5, 'TEST_MAX_RATE', '测试最高利率', 2, 100000.00, 500000.00, 0.2000, 6, 12, '[]', '[]', '测试用最高利率产品', 0.0200, 1, 11, '2026-01-01 00:00:00', NULL, 1, '2026-01-01 00:00:00', NULL, '2026-01-01 00:00:00', 0),
-- 边界: 已下线产品
(6, 'OLD_PRODUCT_001', '已下线产品', 4, 50000.00, 500000.00, 0.0500, 6, 24, '[]', '[]', '已于2026年3月下线', 0.0100, 0, 99, '2026-01-01 00:00:00', '2026-03-01 00:00:00', '2026-03-01 00:00:00', 1, '2026-01-01 00:00:00', NULL, '2026-03-01 00:00:00', 0);

-- 贷款审核 (核心复杂表)
INSERT INTO loan_audit (id, contract_id, finance_specialist_id, recommended_product_id, approved_amount, approved_term, approved_interest_rate, audit_status, bank_id, bank_audit_status, bank_apply_time, bank_feedback_time, bank_feedback_content, reject_reason, audit_opinion, audit_date, loan_granted_date, actual_loan_amount, actual_interest_rate, created_at, updated_at, deleted) VALUES
-- 状态1: 待接收(新建)
(1, 3, NULL, 1, NULL, NULL, NULL, 1, NULL, 'pending', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-08 10:00:00', '2026-04-08 10:00:00', 0),
-- 状态2: 初审中
(2, 4, 6, 1, NULL, NULL, NULL, 2, NULL, 'pending', NULL, NULL, NULL, NULL, '客户资质良好，建议批准', NULL, NULL, NULL, NULL, '2026-04-10 15:00:00', '2026-04-11 09:00:00', 0),
-- 状态3: 已提交银行
(3, 1, 6, 1, 500000.00, 24, 0.0450, 3, 1, 'in_review', '2026-04-13 10:00:00', NULL, NULL, NULL, '符合银行要求，提交审批', '2026-04-13 10:00:00', NULL, NULL, NULL, '2026-04-13 09:00:00', '2026-04-13 10:00:00', 0),
-- 状态4: 银行通过(终审中)
(4, 2, 6, 2, 800000.00, 36, 0.0550, 4, 2, 'approved', '2026-03-03 11:00:00', '2026-03-10 14:00:00', '审批通过，同意放款', NULL, '银行已通过，等待终审', '2026-03-10 14:00:00', NULL, NULL, NULL, '2026-03-02 10:00:00', '2026-03-10 14:00:00', 0),
-- 状态5: 银行拒绝 [终态]
(5, 5, 6, 1, NULL, NULL, NULL, 4, 1, 'rejected', '2026-04-05 15:00:00', '2026-04-06 10:00:00', '企业流水不足', '企业流水不符合银行要求', NULL, NULL, NULL, NULL, '2026-04-05 14:00:00', '2026-04-06 10:00:00', 0),
-- 状态6: 终审通过-已放款 [终态]
(6, 6, 6, 3, 5000000.00, 48, 0.0400, 6, 3, 'approved', '2026-01-16 15:00:00', '2026-01-25 11:00:00', '银行审批通过', NULL, '终审批准', '2026-01-25 11:00:00', '2026-01-28 16:00:00', 4850000.00, 0.0420, '2026-01-16 10:00:00', '2026-01-28 16:00:00', 0),
-- 状态7: 终审拒绝 [终态]
(7, 3, 6, 1, NULL, NULL, NULL, 7, 1, 'pending', NULL, NULL, NULL, '高风险行业', '行业风险过高，拒绝', '2026-04-09 17:00:00', NULL, NULL, NULL, '2026-04-08 11:00:00', '2026-04-09 17:00:00', 0);

-- 边界: 极端金额
(8, 1, 6, 4, 0.01, 6, 0.0300, 3, 1, 'in_review', '2026-04-13 16:00:00', NULL, NULL, NULL, '边界金额测试', '2026-04-13 16:00:00', NULL, NULL, NULL, '2026-04-13 15:00:00', '2026-04-13 16:00:00', 0);

-- 贷款审核记录
INSERT INTO loan_audit_record (id, loan_audit_id, operator_id, operator_name, operator_role, action, content, attachment_urls, created_at) VALUES
(1, 3, 6, '金融专员A', 'FINANCE_SPECIALIST', 'receive', '接收合同，开始初审', NULL, '2026-04-13 09:00:00'),
(2, 3, 6, '金融专员A', 'FINANCE_SPECIALIST', 'review', '初审通过，建议额度50万，24期', NULL, '2026-04-13 10:00:00'),
(3, 3, 6, '金融专员A', 'FINANCE_SPECIALIST', 'submit_bank', '提交工商银行审批', NULL, '2026-04-13 10:00:00'),
(4, 4, 6, '金融专员A', 'FINANCE_SPECIALIST', 'receive', '接收合同', NULL, '2026-03-02 10:00:00'),
(5, 4, 6, '金融专员A', 'FINANCE_SPECIALIST', 'review', '企业资质良好', NULL, '2026-03-02 11:00:00'),
(6, 4, 6, '金融专员A', 'FINANCE_SPECIALIST', 'submit_bank', '提交建设银行', NULL, '2026-03-03 11:00:00'),
(7, 4, 6, '金融专员A', 'FINANCE_SPECIALIST', 'bank_result', '银行审批通过', '{"approved":true,"bankFeedbackContent":"审批通过，同意放款"}', '2026-03-10 14:00:00'),
(8, 4, 1, '审计员', 'AUDITOR', 'approve', '终审批准，放款', NULL, '2026-03-10 15:00:00'),
-- 边界: 银行拒绝记录
(9, 5, 6, '金融专员A', 'FINANCE_SPECIALIST', 'submit_bank', '提交工商银行', NULL, '2026-04-05 15:00:00'),
(10, 5, 6, '金融专员A', 'FINANCE_SPECIALIST', 'bank_result', '银行拒绝', '{"approved":false,"bankFeedbackContent":"企业流水不足"}', '2026-04-06 10:00:00'),
(11, 5, 1, '审计员', 'AUDITOR', 'reject', '终审拒绝，原因：行业风险过高', NULL, '2026-04-09 17:00:00');

-- 服务费记录
INSERT INTO service_fee_record (id, contract_id, fee_type, amount, should_amount, payment_method, payment_status, payment_date, payment_account, receipt_no, accountant_id, remark, created_at, updated_at, deleted) VALUES
-- 正常已付
(1, 2, 1, 20000.00, 20000.00, 'bank_transfer', 1, '2026-03-01', '6222021234567890', 'R20260301001', 1, NULL, '2026-03-01 16:00:00', '2026-03-01 16:00:00', 0),
(2, 2, 2, 8000.00, 8000.00, 'bank_transfer', 1, '2026-03-20', '6222021234567890', 'R20260320001', 1, NULL, '2026-03-20 15:00:00', '2026-03-20 15:00:00', 0),
-- 部分已付
(3, 1, 1, 10000.00, 15000.00, 'wechat', 2, '2026-04-12', 'wx123456789', NULL, 1, '部分支付，剩余5000待付', '2026-04-12 17:00:00', '2026-04-12 17:00:00', 0),
-- 未付
(4, 4, 1, NULL, 9000.00, NULL, 0, NULL, NULL, NULL, 1, '待支付', '2026-04-10 15:00:00', '2026-04-10 15:00:00', 0),
-- 边界: 现金支付
(5, 6, 1, 100000.00, 100000.00, 'cash', 1, '2026-01-15', '现金', 'R20260115001', 1, '一次性现金支付', '2026-01-15 17:00:00', '2026-01-15 17:00:00', 0);

-- 佣金记录
INSERT INTO commission_record (id, performance_id, sales_rep_id, contract_id, commission_amount, commission_rate, status, confirm_time, grant_time, grant_account, remark, created_at, updated_at, deleted) VALUES
(1, 1, 4, 2, 12000.00, 0.0150, 3, '2026-03-10 14:00:00', '2026-03-15 16:00:00', '6222021234567890', NULL, '2026-03-05 10:00:00', '2026-03-15 16:00:00', 0),
(2, 2, 4, 6, 60000.00, 0.0120, 3, '2026-01-25 11:00:00', '2026-02-01 15:00:00', '6222021234567891', '大额合同额外奖励', '2026-01-20 10:00:00', '2026-02-01 15:00:00', 0),
-- 待确认
(3, 3, 3, 1, 7500.00, 0.0150, 1, NULL, NULL, NULL, NULL, '2026-04-13 10:00:00', '2026-04-13 10:00:00', 0),
-- 边界: 已取消
(4, 4, 3, 5, 0, 0.0150, 4, NULL, NULL, NULL, '合同被拒，取消佣金', '2026-04-06 10:00:00', '2026-04-06 10:00:00', 0);
