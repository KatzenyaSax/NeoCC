-- ============================================================
-- NeoCC 测试用户数据
-- 数据库：dafuweng_auth
-- 说明：sys_user 和 sys_user_role 已清空，重新生成完整测试账号
--       每行上方注释明文密码，方便开发测试
--       所有密码统一为 123456
--
-- BCrypt 明文: 123456
-- Hash: $2a$10$xGz4rPQrkJQZJyJQPVgO4eLpNpLQnYQQRmGHHhZq6jLQpKlKlKlKK
--
-- 角色ID（来自 dataRole.sql）：
--   1=SUPER_ADMIN   2=DEPT_MANAGER   3=ZONE_DIRECTOR
--   4=SALES_REP     5=FINANCE_SPECIALIST
--   6=GENERAL_MANAGER  7=ACCOUNTANT   8=AUDITOR
--
-- 部门/战区（来自 datas.sql）：
--   dept: 1=总部  2=销售部  3=财务部  5=销售一部(zone=1)  6=销售二部(zone=2)
--   zone: 1=东部战区  2=西部战区
-- ============================================================

USE dafuweng_auth;

-- ============================================================
-- sys_user — 用户表
-- 所有账号明文密码: 123456
-- ============================================================

-- 1. admin / 123456 -- SUPER_ADMIN（超级管理员，只管系统维护）
INSERT INTO sys_user (id, username, password, real_name, phone, email, dept_id, zone_id, status, login_error_count, lock_time, last_login_time, last_login_ip, created_by, created_at, updated_by, updated_at, deleted, version) VALUES
(11, 'admin', '$2a$10$xGz4rPQrkJQZJyJQPVgO4eLpNpLQnYQQRmGHHhZq6jLQpKlKlKlKK', '超级管理员', '13800000011', 'admin@neocc.com', 1, 1, 1, 0, NULL, NULL, NULL, NULL, '2026-01-01 00:00:00', NULL, '2026-01-01 00:00:00', 0, 1);

-- 2. manager / 123456 -- GENERAL_MANAGER（总经理，全业务线最高权限）
INSERT INTO sys_user (id, username, password, real_name, phone, email, dept_id, zone_id, status, login_error_count, lock_time, last_login_time, last_login_ip, created_by, created_at, updated_by, updated_at, deleted, version) VALUES
(12, 'manager', '$2a$10$xGz4rPQrkJQZJyJQPVgO4eLpNpLQnYQQRmGHHhZq6jLQpKlKlKlKK', '陈总经理', '13800000012', 'manager@neocc.com', 1, 1, 1, 0, NULL, NULL, NULL, 11, '2026-04-01 00:00:00', NULL, '2026-04-01 00:00:00', 0, 1);

-- 3. zhangsan / 123456 -- DEPT_MANAGER + ZONE_DIRECTOR（部门经理兼东部战区总监）
INSERT INTO sys_user (id, username, password, real_name, phone, email, dept_id, zone_id, status, login_error_count, lock_time, last_login_time, last_login_ip, created_by, created_at, updated_by, updated_at, deleted, version) VALUES
(13, 'zhangsan', '$2a$10$xGz4rPQrkJQZJyJQPVgO4eLpNpLQnYQQRmGHHhZq6jLQpKlKlKlKK', '张经理', '13800000013', 'zhangsan@neocc.com', 2, 1, 1, 0, NULL, NULL, NULL, 11, '2026-01-05 00:00:00', NULL, '2026-04-13 15:30:00', 0, 1);

-- 4. deptmanager / 123456 -- DEPT_MANAGER（销售二部经理，zone=2）
INSERT INTO sys_user (id, username, password, real_name, phone, email, dept_id, zone_id, status, login_error_count, lock_time, last_login_time, last_login_ip, created_by, created_at, updated_by, updated_at, deleted, version) VALUES
(14, 'deptmanager', '$2a$10$xGz4rPQrkJQZJyJQPVgO4eLpNpLQnYQQRmGHHhZq6jLQpKlKlKlKK', '刘部门经理', '13800000014', 'deptmanager@neocc.com', 6, 2, 1, 0, NULL, NULL, NULL, 11, '2026-04-01 00:00:00', NULL, '2026-04-01 00:00:00', 0, 1);

-- 5. zonedirector / 123456 -- ZONE_DIRECTOR（西部战区总监）
INSERT INTO sys_user (id, username, password, real_name, phone, email, dept_id, zone_id, status, login_error_count, lock_time, last_login_time, last_login_ip, created_by, created_at, updated_by, updated_at, deleted, version) VALUES
(15, 'zonedirector', '$2a$10$xGz4rPQrkJQZJyJQPVgO4eLpNpLQnYQQRmGHHhZq6jLQpKlKlKlKK', '孙战区总监', '13800000015', 'zonedirector@neocc.com', 6, 2, 1, 0, NULL, NULL, NULL, 11, '2026-04-01 00:00:00', NULL, '2026-04-01 00:00:00', 0, 1);

-- 6. lisi / 123456 -- SALES_REP（销售代表，东部战区，销售一部）
INSERT INTO sys_user (id, username, password, real_name, phone, email, dept_id, zone_id, status, login_error_count, lock_time, last_login_time, last_login_ip, created_by, created_at, updated_by, updated_at, deleted, version) VALUES
(16, 'lisi', '$2a$10$xGz4rPQrkJQZJyJQPVgO4eLpNpLQnYQQRmGHHhZq6jLQpKlKlKlKK', '李四', '13800000016', 'lisi@neocc.com', 5, 1, 1, 0, NULL, NULL, NULL, 11, '2026-01-10 00:00:00', NULL, '2026-04-12 10:00:00', 0, 1);

-- 7. salesrep_east / 123456 -- SALES_REP（销售代表，东部战区，销售一部）
INSERT INTO sys_user (id, username, password, real_name, phone, email, dept_id, zone_id, status, login_error_count, lock_time, last_login_time, last_login_ip, created_by, created_at, updated_by, updated_at, deleted, version) VALUES
(17, 'salesrep_east', '$2a$10$xGz4rPQrkJQZJyJQPVgO4eLpNpLQnYQQRmGHHhZq6jLQpKlKlKlKK', '周销售A', '13800000017', 'salesrep_east@neocc.com', 5, 1, 1, 0, NULL, NULL, NULL, 11, '2026-04-01 00:00:00', NULL, '2026-04-01 00:00:00', 0, 1);

-- 8. wangwu / 123456 -- SALES_REP（销售代表，西部战区，销售二部）
INSERT INTO sys_user (id, username, password, real_name, phone, email, dept_id, zone_id, status, login_error_count, lock_time, last_login_time, last_login_ip, created_by, created_at, updated_by, updated_at, deleted, version) VALUES
(18, 'wangwu', '$2a$10$xGz4rPQrkJQZJyJQPVgO4eLpNpLQnYQQRmGHHhZq6jLQpKlKlKlKK', '王五', '13800000018', 'wangwu@neocc.com', 6, 2, 1, 0, NULL, NULL, NULL, 11, '2026-01-15 00:00:00', NULL, '2026-04-11 14:00:00', 0, 1);

-- 9. salesrep_west / 123456 -- SALES_REP（销售代表，西部战区，销售二部）
INSERT INTO sys_user (id, username, password, real_name, phone, email, dept_id, zone_id, status, login_error_count, lock_time, last_login_time, last_login_ip, created_by, created_at, updated_by, updated_at, deleted, version) VALUES
(19, 'salesrep_west', '$2a$10$xGz4rPQrkJQZJyJQPVgO4eLpNpLQnYQQRmGHHhZq6jLQpKlKlKlKK', '吴销售B', '13800000019', 'salesrep_west@neocc.com', 6, 2, 1, 0, NULL, NULL, NULL, 11, '2026-04-01 00:00:00', NULL, '2026-04-01 00:00:00', 0, 1);

-- 10. financeuser / 123456 -- FINANCE_SPECIALIST（金融专员，财务部，东部战区）
INSERT INTO sys_user (id, username, password, real_name, phone, email, dept_id, zone_id, status, login_error_count, lock_time, last_login_time, last_login_ip, created_by, created_at, updated_by, updated_at, deleted, version) VALUES
(20, 'financeuser', '$2a$10$xGz4rPQrkJQZJyJQPVgO4eLpNpLQnYQQRmGHHhZq6jLQpKlKlKlKK', '金融专员A', '13800000020', 'financeuser@neocc.com', 3, 1, 1, 0, NULL, NULL, NULL, 11, '2026-04-01 00:00:00', NULL, '2026-04-01 00:00:00', 0, 1);

-- 11. accountant / 123456 -- ACCOUNTANT（会计，财务部，东部战区）
INSERT INTO sys_user (id, username, password, real_name, phone, email, dept_id, zone_id, status, login_error_count, lock_time, last_login_time, last_login_ip, created_by, created_at, updated_by, updated_at, deleted, version) VALUES
(21, 'accountant', '$2a$10$xGz4rPQrkJQZJyJQPVgO4eLpNpLQnYQQRmGHHhZq6jLQpKlKlKlKK', '郑会计', '13800000021', 'accountant@neocc.com', 3, 1, 1, 0, NULL, NULL, NULL, 11, '2026-04-01 00:00:00', NULL, '2026-04-01 00:00:00', 0, 1);

-- 12. auditor / 123456 -- AUDITOR（审计员，总部，东部战区）
INSERT INTO sys_user (id, username, password, real_name, phone, email, dept_id, zone_id, status, login_error_count, lock_time, last_login_time, last_login_ip, created_by, created_at, updated_by, updated_at, deleted, version) VALUES
(22, 'auditor', '$2a$10$xGz4rPQrkJQZJyJQPVgO4eLpNpLQnYQQRmGHHhZq6jLQpKlKlKlKK', '审计员A', '13800000022', 'auditor@neocc.com', 1, 1, 1, 0, NULL, NULL, NULL, 11, '2026-04-01 00:00:00', NULL, '2026-04-01 00:00:00', 0, 1);

-- ============================================================
-- sys_user_role — 用户角色关联
-- ============================================================

-- SUPER_ADMIN
INSERT INTO sys_user_role (id, user_id, role_id, created_at) VALUES
(1, 11, 1, '2026-01-01 00:00:00');  -- admin -> SUPER_ADMIN

-- GENERAL_MANAGER
INSERT INTO sys_user_role (id, user_id, role_id, created_at) VALUES
(2, 12, 6, '2026-04-01 00:00:00');  -- manager -> GENERAL_MANAGER

-- DEPT_MANAGER
INSERT INTO sys_user_role (id, user_id, role_id, created_at) VALUES
(3, 13, 2, '2026-01-05 00:00:00');  -- zhangsan -> DEPT_MANAGER

-- ZONE_DIRECTOR
INSERT INTO sys_user_role (id, user_id, role_id, created_at) VALUES
(4, 13, 3, '2026-04-01 00:00:00'),  -- zhangsan -> ZONE_DIRECTOR（兼任）
(5, 15, 3, '2026-04-01 00:00:00');  -- zonedirector -> ZONE_DIRECTOR

-- DEPT_MANAGER（第二个部门经理）
INSERT INTO sys_user_role (id, user_id, role_id, created_at) VALUES
(6, 14, 2, '2026-04-01 00:00:00');  -- deptmanager -> DEPT_MANAGER

-- SALES_REP（4个销售代表）
INSERT INTO sys_user_role (id, user_id, role_id, created_at) VALUES
(7, 16, 4, '2026-01-10 00:00:00'),  -- lisi -> SALES_REP
(8, 17, 4, '2026-04-01 00:00:00'),  -- salesrep_east -> SALES_REP
(9, 18, 4, '2026-01-15 00:00:00'),  -- wangwu -> SALES_REP
(10, 19, 4, '2026-04-01 00:00:00'); -- salesrep_west -> SALES_REP

-- FINANCE_SPECIALIST
INSERT INTO sys_user_role (id, user_id, role_id, created_at) VALUES
(11, 20, 5, '2026-04-01 00:00:00');  -- financeuser -> FINANCE_SPECIALIST

-- ACCOUNTANT
INSERT INTO sys_user_role (id, user_id, role_id, created_at) VALUES
(12, 21, 7, '2026-04-01 00:00:00');  -- accountant -> ACCOUNTANT

-- AUDITOR
INSERT INTO sys_user_role (id, user_id, role_id, created_at) VALUES
(13, 22, 8, '2026-04-01 00:00:00');  -- auditor -> AUDITOR

-- ============================================================
-- 账号速查表
-- ============================================================
--
--  username         | 明文密码 | 角色                    | dept        | zone    | 备注
-- -----------------|---------|------------------------|-------------|---------|------------------
--  admin           | 123456  | SUPER_ADMIN           | 总部         | 东部     | 仅系统维护
--  manager         | 123456  | GENERAL_MANAGER       | 总部         | 东部     | 全业务线最高权限
--  zhangsan        | 123456  | DEPT_MANAGER + ZONE_DIRECTOR | 销售部 | 东部+西部 | 部门+战区双角色
--  deptmanager     | 123456  | DEPT_MANAGER          | 销售二部      | 西部     |
--  zonedirector    | 123456  | ZONE_DIRECTOR         | 销售二部      | 西部     |
--  lisi            | 123456  | SALES_REP             | 销售一部      | 东部     |
--  salesrep_east   | 123456  | SALES_REP             | 销售一部      | 东部     |
--  wangwu          | 123456  | SALES_REP             | 销售二部      | 西部     |
--  salesrep_west   | 123456  | SALES_REP             | 销售二部      | 西部     |
--  financeuser     | 123456  | FINANCE_SPECIALIST   | 财务部        | 东部     | 贷款审核
--  accountant      | 123456  | ACCOUNTANT            | 财务部        | 东部     | 服务费+提成
--  auditor         | 123456  | AUDITOR               | 总部          | 东部     | 只读所有数据
-- ============================================================
