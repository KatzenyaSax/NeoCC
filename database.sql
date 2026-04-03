-- ================================================================
-- 大富翁金融服务公司 贷款管理系统 — 数据库初始化脚本
-- 版本: v1.0
-- 日期: 2026-03-31
-- 说明: 按库分开，共4个业务库 + 1个认证库，执行顺序: 1-4-2-3
-- ================================================================

-- ================================================================
-- 第1步: 创建所有数据库（统一字符集）
-- ================================================================
CREATE DATABASE IF NOT EXISTS dafuweng_auth  DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS dafuweng_system DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS dafuweng_sales  DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS dafuweng_finance DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE dafuweng_auth;

-- ==============================X==================================
-- 第2步: dafuweng_auth 库 — 认证/账号/角色/权限
-- ================================================================

CREATE TABLE `sys_user` (
  `id`                BIGINT          NOT NULL  AUTO_INCREMENT  COMMENT '用户ID',
  `username`          VARCHAR(50)     NOT NULL                     COMMENT '用户名',
  `password`          VARCHAR(200)    NOT NULL                     COMMENT '密码密文(BCrypt)',
  `real_name`         VARCHAR(50)    NOT NULL                     COMMENT '真实姓名',
  `phone`             VARCHAR(20)                                  COMMENT '手机号',
  `email`             VARCHAR(100)                                 COMMENT '邮箱',
  `dept_id`           BIGINT                                       COMMENT '所属部门ID',
  `zone_id`           BIGINT                                       COMMENT '所属战区ID',
  `status`            TINYINT       NOT NULL  DEFAULT 1           COMMENT '账号状态: 1-正常 0-禁用',
  `login_error_count` INT           NOT NULL  DEFAULT 0           COMMENT '连续登录失败次数',
  `lock_time`         DATETIME                                    COMMENT '账号锁定截止时间',
  `last_login_time`   DATETIME                                    COMMENT '最后登录时间',
  `last_login_ip`     VARCHAR(50)                                  COMMENT '最后登录IP',
  `created_by`        BIGINT                                       COMMENT '创建人',
  `created_at`        DATETIME      NOT NULL  DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
  `updated_by`        BIGINT                                       COMMENT '修改人',
  `updated_at`        DATETIME      NOT NULL  DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP  COMMENT '修改时间',
  `deleted`           TINYINT       NOT NULL  DEFAULT 0           COMMENT '逻辑删除: 0-未删 1-已删',
  `version`           INT           NOT NULL  DEFAULT 0           COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_dept_id`  (`dept_id`),
  KEY `idx_zone_id`  (`zone_id`),
  KEY `idx_status`   (`status`),
  KEY `idx_deleted`   (`deleted`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci  COMMENT='系统用户表';

CREATE TABLE `sys_role` (
  `id`                BIGINT         NOT NULL  AUTO_INCREMENT  COMMENT '角色ID',
  `role_code`         VARCHAR(50)    NOT NULL                     COMMENT '角色编码(唯一)',
  `role_name`         VARCHAR(50)    NOT NULL                     COMMENT '角色名称',
  `data_scope`        TINYINT        NOT NULL  DEFAULT 1         COMMENT '数据权限范围: 1-本人 2-本部门 3-本战区 4-全部',
  `role_sort`         INT            NOT NULL  DEFAULT 0         COMMENT '显示顺序',
  `status`            TINYINT        NOT NULL  DEFAULT 1         COMMENT '状态: 1-启用 0-禁用',
  `created_by`        BIGINT                                       COMMENT '创建人',
  `created_at`        DATETIME       NOT NULL  DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
  `updated_by`        BIGINT                                       COMMENT '修改人',
  `updated_at`        DATETIME       NOT NULL  DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP  COMMENT '修改时间',
  `deleted`           TINYINT        NOT NULL  DEFAULT 0           COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_code` (`role_code`),
  KEY `idx_deleted`   (`deleted`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci  COMMENT='系统角色表';

CREATE TABLE `sys_user_role` (
  `id`       BIGINT    NOT NULL  AUTO_INCREMENT  COMMENT '主键',
  `user_id`  BIGINT    NOT NULL                     COMMENT '用户ID',
  `role_id`  BIGINT    NOT NULL                     COMMENT '角色ID',
  `created_at` DATETIME NOT NULL  DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci  COMMENT='用户角色关联表';

CREATE TABLE `sys_permission` (
  `id`           BIGINT        NOT NULL  AUTO_INCREMENT  COMMENT '权限ID',
  `parent_id`    BIGINT        NOT NULL  DEFAULT 0        COMMENT '父权限ID: 0-根',
  `perm_code`    VARCHAR(100)  NOT NULL                     COMMENT '权限标识',
  `perm_name`    VARCHAR(50)   NOT NULL                     COMMENT '权限名称',
  `perm_type`    TINYINT       NOT NULL                     COMMENT '权限类型: 1-菜单 2-按钮 3-接口',
  `perm_path`    VARCHAR(200)                                COMMENT '权限路径(菜单路径/接口URL)',
  `icon`         VARCHAR(100)                                COMMENT '图标',
  `sort_order`   INT          NOT NULL  DEFAULT 0          COMMENT '显示顺序',
  `status`       TINYINT      NOT NULL  DEFAULT 1           COMMENT '状态: 1-启用 0-禁用',
  `external_link` TINYINT     NOT NULL  DEFAULT 0           COMMENT '是否外链: 0-否 1-是',
  `created_at`   DATETIME     NOT NULL  DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
  `updated_at`   DATETIME     NOT NULL  DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP  COMMENT '修改时间',
  `deleted`      TINYINT      NOT NULL  DEFAULT 0           COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_perm_code` (`perm_code`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_deleted`   (`deleted`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci  COMMENT='系统权限表';

CREATE TABLE `sys_role_permission` (
  `id`            BIGINT    NOT NULL  AUTO_INCREMENT  COMMENT '主键',
  `role_id`       BIGINT    NOT NULL                     COMMENT '角色ID',
  `permission_id` BIGINT    NOT NULL                     COMMENT '权限ID',
  `created_at`    DATETIME  NOT NULL  DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_perm` (`role_id`, `permission_id`),
  KEY `idx_role_id`       (`role_id`),
  KEY `idx_permission_id` (`permission_id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci  COMMENT='角色权限关联表';

-- 初始化超管角色
INSERT INTO `sys_role` (`role_code`, `role_name`, `data_scope`, `role_sort`, `status`, `deleted`) VALUES
('SUPER_ADMIN',   '超级管理员',  4, 1, 1, 0),
('ADMIN',        '系统管理员',  4, 2, 1, 0),
('GM',           '总经理',      4, 3, 1, 0),
('SALES_DIRECTOR','销售总监',   3, 4, 1, 0),
('DEPT_MANAGER', '部门经理',    2, 5, 1, 0),
('SALES_REP',    '销售代表',    1, 6, 1, 0),
('FINANCE_SPEC', '金融专员',    1, 7, 1, 0),
('FINANCE_MGR',  '金融部经理',  2, 8, 1, 0),
('ACCOUNTANT',   '会计',        2, 9, 1, 0);

-- 初始化超管账号 (密码: admin123)
INSERT INTO `sys_user` (`username`, `password`, `real_name`, `status`, `deleted`, `created_at`) VALUES
('admin', '$2a$12$...', '系统管理员', 1, 0, NOW());


USE dafuweng_system;

-- ================================================================
-- 第3步: dafuweng_system 库 — 组织架构/部门/战区/系统参数/操作日志
-- ================================================================

CREATE TABLE `sys_zone` (
  `id`           BIGINT       NOT NULL  AUTO_INCREMENT  COMMENT '战区ID',
  `zone_code`    VARCHAR(50)  NOT NULL                     COMMENT '战区编码',
  `zone_name`    VARCHAR(100) NOT NULL                     COMMENT '战区名称',
  `director_id`  BIGINT                                   COMMENT '销售总监用户ID',
  `sort_order`   INT          NOT NULL  DEFAULT 0         COMMENT '显示顺序',
  `status`       TINYINT      NOT NULL  DEFAULT 1         COMMENT '状态: 1-启用 0-禁用',
  `created_by`   BIGINT                                     COMMENT '创建人',
  `created_at`   DATETIME     NOT NULL  DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
  `updated_by`   BIGINT                                     COMMENT '修改人',
  `updated_at`   DATETIME     NOT NULL  DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP  COMMENT '修改时间',
  `deleted`      TINYINT      NOT NULL  DEFAULT 0           COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_zone_code` (`zone_code`),
  KEY `idx_deleted`   (`deleted`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci  COMMENT='战区表';

CREATE TABLE `sys_department` (
  `id`           BIGINT       NOT NULL  AUTO_INCREMENT  COMMENT '部门ID',
  `dept_code`    VARCHAR(50)  NOT NULL                     COMMENT '部门编码',
  `dept_name`    VARCHAR(100) NOT NULL                     COMMENT '部门名称',
  `parent_id`    BIGINT       NOT NULL  DEFAULT 0         COMMENT '父部门ID: 0-根',
  `zone_id`      BIGINT       NOT NULL                     COMMENT '所属战区ID',
  `manager_id`   BIGINT                                   COMMENT '部门经理用户ID',
  `sort_order`   INT          NOT NULL  DEFAULT 0         COMMENT '显示顺序',
  `status`       TINYINT      NOT NULL  DEFAULT 1         COMMENT '状态: 1-启用 0-禁用',
  `created_by`   BIGINT                                     COMMENT '创建人',
  `created_at`   DATETIME     NOT NULL  DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
  `updated_by`   BIGINT                                     COMMENT '修改人',
  `updated_at`   DATETIME     NOT NULL  DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP  COMMENT '修改时间',
  `deleted`      TINYINT      NOT NULL  DEFAULT 0           COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dept_code` (`dept_code`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_zone_id`   (`zone_id`),
  KEY `idx_deleted`   (`deleted`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci  COMMENT='部门表';

CREATE TABLE `sys_param` (
  `id`           BIGINT       NOT NULL  AUTO_INCREMENT  COMMENT '参数ID',
  `param_key`    VARCHAR(100) NOT NULL                     COMMENT '参数键(唯一)',
  `param_value`  TEXT         NOT NULL                     COMMENT '参数值',
  `param_type`   VARCHAR(50)                               COMMENT '值类型: string/int/json/boolean',
  `param_group`  VARCHAR(50)                               COMMENT '参数分组',
  `remark`       VARCHAR(200)                               COMMENT '参数说明',
  `sort_order`   INT          NOT NULL  DEFAULT 0         COMMENT '显示顺序',
  `status`       TINYINT      NOT NULL  DEFAULT 1         COMMENT '状态: 1-启用 0-禁用',
  `created_by`   BIGINT                                     COMMENT '创建人',
  `created_at`   DATETIME     NOT NULL  DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
  `updated_by`   BIGINT                                     COMMENT '修改人',
  `updated_at`   DATETIME     NOT NULL  DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP  COMMENT '修改时间',
  `deleted`      TINYINT      NOT NULL  DEFAULT 0           COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_param_key` (`param_key`),
  KEY `idx_param_group` (`param_group`),
  KEY `idx_deleted`   (`deleted`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci  COMMENT='系统参数表';

-- 初始化关键系统参数
INSERT INTO `sys_param` (`param_key`, `param_value`, `param_type`, `param_group`, `remark`, `status`, `deleted`) VALUES
('customer.public_sea_days',          '30',      'int',    'sales',      '客户多少天未签约自动入公海',  1, 0),
('performance.commission_rate',        '0.05',    'decimal','performance','默认业绩提成比例(5%)',          1, 0),
('contract.service_fee_first_rate',    '0.30',    'decimal','contract',   '首期服务费比例(30%)',           1, 0),
('system.max_upload_file_size',        '52428800','long',   'system',     '文件上传大小限制(50MB)',         1, 0),
('login.max_error_times',              '5',       'int',    'security',   '登录失败最大次数',               1, 0),
('login.lock_minutes',                '15',      'int',    'security',   '登录锁定时间(分钟)',             1, 0);

CREATE TABLE `sys_operation_log` (
  `id`             BIGINT        NOT NULL  AUTO_INCREMENT  COMMENT '日志ID',
  `user_id`        BIGINT        NOT NULL                     COMMENT '操作用户ID',
  `username`       VARCHAR(50)   NOT NULL                     COMMENT '操作用户名',
  `module`         VARCHAR(50)   NOT NULL                     COMMENT '操作模块',
  `action`         VARCHAR(100)  NOT NULL                     COMMENT '操作类型',
  `request_method` VARCHAR(10)                               COMMENT 'HTTP请求方法',
  `request_url`   VARCHAR(500)                               COMMENT '请求URL',
  `request_params` TEXT                                       COMMENT '请求参数(JSON)',
  `response_code`  VARCHAR(20)                               COMMENT '响应状态码',
  `error_msg`      TEXT                                       COMMENT '错误信息',
  `ip`             VARCHAR(50)                               COMMENT 'IP地址',
  `user_agent`     VARCHAR(500)                              COMMENT 'UserAgent',
  `cost_time_ms`   BIGINT                                    COMMENT '耗时(毫秒)',
  `created_at`     DATETIME      NOT NULL  DEFAULT CURRENT_TIMESTAMP  COMMENT '操作时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id`    (`user_id`),
  KEY `idx_module`     (`module`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci  COMMENT='操作日志表';

CREATE TABLE `sys_dict` (
  `id`           BIGINT       NOT NULL  AUTO_INCREMENT  COMMENT '字典ID',
  `dict_type`    VARCHAR(50)  NOT NULL                     COMMENT '字典类型',
  `dict_code`    VARCHAR(50)  NOT NULL                     COMMENT '字典编码',
  `dict_label`   VARCHAR(100) NOT NULL                     COMMENT '字典标签',
  `dict_value`   VARCHAR(200) NOT NULL                     COMMENT '字典值',
  `sort_order`   INT          NOT NULL  DEFAULT 0         COMMENT '显示顺序',
  `status`       TINYINT      NOT NULL  DEFAULT 1         COMMENT '状态',
  `remark`       VARCHAR(200)                               COMMENT '备注',
  `created_at`   DATETIME     NOT NULL  DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
  `updated_at`   DATETIME     NOT NULL  DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP  COMMENT '修改时间',
  `deleted`      TINYINT      NOT NULL  DEFAULT 0           COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_type_code` (`dict_type`, `dict_code`),
  KEY `idx_dict_type` (`dict_type`),
  KEY `idx_deleted`   (`deleted`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci  COMMENT='数据字典表';

-- 初始化关键字典
INSERT INTO `sys_dict` (`dict_type`, `dict_code`, `dict_label`, `dict_value`, `sort_order`, `status`, `deleted`) VALUES
('customer_type',   'PERSONAL',  '个人客户',  '1',  1, 1, 0),
('customer_type',   'COMPANY',   '企业客户',  '2',  2, 1, 0),
('customer_status', 'POTENTIAL', '潜在客户',  '1',  1, 1, 0),
('customer_status', 'NEGOTIATING','洽谈中',  '2',  2, 1, 0),
('customer_status', 'SIGNED',    '已签约',    '3',  3, 1, 0),
('customer_status', 'LOANED',    '已放款',    '4',  4, 1, 0),
('customer_status', 'PUBLIC_SEA','公海',      '5',  5, 1, 0),
('intention_level', 'LEVEL_A',   'A级(高)',  '1',  1, 1, 0),
('intention_level', 'LEVEL_B',   'B级(中)',  '2',  2, 1, 0),
('intention_level', 'LEVEL_C',   'C级(低)',  '3',  3, 1, 0),
('intention_level', 'LEVEL_D',   'D级(无)',  '4',  4, 1, 0),
('contact_type',   'PHONE',     '电话',      '1',  1, 1, 0),
('contact_type',   'FACE',      '面谈',      '2',  2, 1, 0),
('contact_type',   'REFERRAL',  '转介绍',    '3',  3, 1, 0),
('contract_status','DRAFT',     '草稿',      '1',  1, 1, 0),
('contract_status','SIGNED',     '已签署',    '2',  2, 1, 0),
('contract_status','FIRST_FEE_PAID', '已支付首期','3',3, 1, 0),
('contract_status','IN_AUDIT',  '审核中',    '4',  4, 1, 0),
('contract_status','APPROVED',  '已通过',    '5',  5, 1, 0),
('contract_status','REJECTED',  '已拒绝',    '6',  6, 1, 0),
('contract_status','LOAN_GRANTED','已放款',  '7',  7, 1, 0),
('contract_status','COMPLETED',  '已完成',    '8',  8, 1, 0),
('audit_status',   'PENDING',   '待审核',    '1',  1, 1, 0),
('audit_status',   'IN_PROGRESS','审核中',  '2',  2, 1, 0),
('audit_status',   'APPROVED',  '已通过',    '3',  3, 1, 0),
('audit_status',   'REJECTED',  '已拒绝',    '4',  4, 1, 0),
('payment_method', 'BANK_TRANSFER','银行转账','bank_transfer',1,1,0),
('payment_method', 'WECHAT',    '微信支付',  'wechat',     2, 1, 0),
('payment_method', 'ALIPAY',    '支付宝',    'alipay',     3, 1, 0),
('payment_method', 'CASH',      '现金',      'cash',       4, 1, 0),
('attachment_type','BUSINESS_LICENSE','营业执照','business_license',1,1,0),
('attachment_type','ID_CARD',    '身份证',    'id_card',    2, 1, 0),
('attachment_type','OTHER',      '其他附件',  'other',      3, 1, 0);


USE dafuweng_sales;

-- ================================================================
-- 第4步: dafuweng_sales 库 — 客户/洽谈/合同/业绩/工作日志
-- ================================================================

CREATE TABLE `customer` (
  `id`                BIGINT       NOT NULL  AUTO_INCREMENT  COMMENT '客户ID',
  `name`             VARCHAR(100) NOT NULL                     COMMENT '客户姓名/名称',
  `phone`            VARCHAR(20)  NOT NULL                     COMMENT '手机号',
  `id_card`          VARCHAR(20)                               COMMENT '身份证号',
  `company_name`     VARCHAR(200)                               COMMENT '企业名称(企业客户)',
  `company_legal_person` VARCHAR(100)                          COMMENT '企业法人',
  `company_reg_capital` DECIMAL(15,2)                         COMMENT '企业注册资本',
  `customer_type`    TINYINT      NOT NULL  DEFAULT 1         COMMENT '客户类型: 1-个人 2-企业',
  `sales_rep_id`    BIGINT       NOT NULL                     COMMENT '负责销售ID',
  `dept_id`         BIGINT       NOT NULL                     COMMENT '所属部门ID',
  `zone_id`         BIGINT       NOT NULL                     COMMENT '所属战区ID',
  `intention_level` TINYINT      NOT NULL  DEFAULT 3         COMMENT '意向等级: 1-A 2-B 3-C 4-D',
  `status`          TINYINT      NOT NULL  DEFAULT 1         COMMENT '状态: 1-潜在 2-洽谈中 3-已签约 4-已放款 5-公海',
  `last_contact_date` DATETIME                                COMMENT '最后联系日期',
  `next_follow_up_date` DATETIME                              COMMENT '下次跟进日期',
  `public_sea_time`  DATETIME                                  COMMENT '进入公海时间',
  `public_sea_reason` VARCHAR(200)                             COMMENT '入公海原因',
  `annotation`       TEXT                                      COMMENT '批注(JSON: [{userId, content, time}])',
  `source`          VARCHAR(50)                                COMMENT '客户来源: phone_call/referral/other',
  `loan_intention_amount` DECIMAL(15,2)                        COMMENT '贷款意向金额',
  `loan_intention_product` VARCHAR(100)                        COMMENT '意向产品',
  `created_by`      BIGINT       NOT NULL                     COMMENT '创建人(录入销售)',
  `created_at`      DATETIME    NOT NULL  DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
  `updated_by`       BIGINT                                    COMMENT '修改人',
  `updated_at`       DATETIME    NOT NULL  DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP  COMMENT '修改时间',
  `deleted`          TINYINT     NOT NULL  DEFAULT 0           COMMENT '逻辑删除',
  `version`          INT         NOT NULL  DEFAULT 0           COMMENT '乐观锁',
  PRIMARY KEY (`id`),
  -- 姓名+手机号 联合唯一约束(仅未删除记录),防重复录入
  UNIQUE KEY `uk_name_phone` (`name`, `phone`, `deleted`),
  KEY `idx_sales_rep_id`  (`sales_rep_id`),
  KEY `idx_dept_id`       (`dept_id`),
  KEY `idx_zone_id`       (`zone_id`),
  KEY `idx_status`        (`status`),
  KEY `idx_intention_level` (`intention_level`),
  KEY `idx_public_sea_time` (`public_sea_time`),
  KEY `idx_deleted`       (`deleted`),
  KEY `idx_last_contact_date` (`last_contact_date`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci  COMMENT='客户表';

CREATE TABLE `contact_record` (
  `id`               BIGINT      NOT NULL  AUTO_INCREMENT  COMMENT '洽谈记录ID',
  `customer_id`      BIGINT      NOT NULL                     COMMENT '客户ID',
  `sales_rep_id`     BIGINT      NOT NULL                     COMMENT '销售ID',
  `contact_type`     TINYINT     NOT NULL                     COMMENT '联系类型: 1-电话 2-面谈 3-转介绍',
  `contact_date`     DATETIME    NOT NULL                     COMMENT '联系时间',
  `content`          TEXT        NOT NULL                     COMMENT '洽谈内容',
  `intention_before` TINYINT                                 COMMENT '联系前意向等级',
  `intention_after`  TINYINT                                 COMMENT '联系后意向等级',
  `follow_up_date`   DATETIME                                COMMENT '下次跟进日期',
  `attachment_urls`  TEXT                                       COMMENT '附件URLs(JSON数组)',
  `created_by`      BIGINT      NOT NULL                     COMMENT '创建人',
  `created_at`      DATETIME    NOT NULL  DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
  `updated_by`      BIGINT                                    COMMENT '修改人',
  `updated_at`      DATETIME    NOT NULL  DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP  COMMENT '修改时间',
  `deleted`         TINYINT     NOT NULL  DEFAULT 0           COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_customer_id`   (`customer_id`),
  KEY `idx_sales_rep_id`  (`sales_rep_id`),
  KEY `idx_contact_date`  (`contact_date`),
  KEY `idx_deleted`       (`deleted`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci  COMMENT='洽谈记录表';

CREATE TABLE `contract` (
  `id`                    BIGINT         NOT NULL  AUTO_INCREMENT  COMMENT '合同ID',
  `contract_no`           VARCHAR(50)    NOT NULL                     COMMENT '合同编号(唯一)',
  `customer_id`           BIGINT         NOT NULL                     COMMENT '客户ID',
  `sales_rep_id`          BIGINT         NOT NULL                     COMMENT '负责销售ID',
  `dept_id`               BIGINT         NOT NULL                     COMMENT '所属部门ID',
  `product_id`            BIGINT                                       COMMENT '金融产品ID(审核通过后填充)',
  `contract_amount`       DECIMAL(15,2)  NOT NULL                     COMMENT '合同金额(贷款申请额)',
  `actual_loan_amount`    DECIMAL(15,2)                                COMMENT '实际放款金额(银行放款后)',
  `service_fee_rate`      DECIMAL(6,4)   NOT NULL                     COMMENT '服务费率',
  `service_fee_1`         DECIMAL(15,2)  NOT NULL                     COMMENT '首期服务费',
  `service_fee_2`         DECIMAL(15,2)  NOT NULL                     COMMENT '二期服务费(尾款)',
  `service_fee_1_paid`    TINYINT        NOT NULL  DEFAULT 0         COMMENT '首期是否已付: 0-否 1-是',
  `service_fee_2_paid`    TINYINT        NOT NULL  DEFAULT 0         COMMENT '二期是否已付: 0-否 1-是',
  `service_fee_1_pay_date` DATE                                        COMMENT '首期支付日期',
  `service_fee_2_pay_date` DATE                                        COMMENT '二期支付日期',
  `status`                TINYINT        NOT NULL  DEFAULT 1         COMMENT '状态: 1-草稿 2-已签署 3-已支付首期 4-审核中 5-已通过 6-已拒绝 7-已放款 8-已完成',
  `sign_date`             DATE                                         COMMENT '签署日期',
  `paper_contract_no`     VARCHAR(100)                                COMMENT '纸质合同编号',
  `finance_send_time`     DATETIME                                    COMMENT '发送至金融部时间',
  `finance_receive_time`  DATETIME                                    COMMENT '金融部接收时间',
  `loan_use`              VARCHAR(200)                                 COMMENT '贷款用途',
  `guarantee_info`        TEXT                                         COMMENT '担保信息(JSON)',
  `reject_reason`         VARCHAR(500)                                 COMMENT '拒绝原因(合同被拒)',
  `remark`                TEXT                                         COMMENT '备注',
  `created_by`            BIGINT         NOT NULL                     COMMENT '创建人',
  `created_at`            DATETIME       NOT NULL  DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
  `updated_by`            BIGINT                                       COMMENT '修改人',
  `updated_at`            DATETIME       NOT NULL  DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP  COMMENT '修改时间',
  `deleted`               TINYINT        NOT NULL  DEFAULT 0           COMMENT '逻辑删除',
  `version`               INT            NOT NULL  DEFAULT 0           COMMENT '乐观锁',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_contract_no` (`contract_no`),
  KEY `idx_customer_id`   (`customer_id`),
  KEY `idx_sales_rep_id`  (`sales_rep_id`),
  KEY `idx_dept_id`       (`dept_id`),
  KEY `idx_product_id`    (`product_id`),
  KEY `idx_status`        (`status`),
  KEY `idx_sign_date`     (`sign_date`),
  KEY `idx_deleted`       (`deleted`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci  COMMENT='贷款合同表';

CREATE TABLE `contract_attachment` (
  `id`             BIGINT        NOT NULL  AUTO_INCREMENT  COMMENT '附件ID',
  `contract_id`    BIGINT        NOT NULL                     COMMENT '合同ID',
  `attachment_type` VARCHAR(50)   NOT NULL                     COMMENT '附件类型: business_license/id_card/other',
  `file_url`       VARCHAR(500)  NOT NULL                     COMMENT '文件访问URL',
  `file_name`      VARCHAR(200)  NOT NULL                     COMMENT '原始文件名',
  `file_size`      BIGINT                                       COMMENT '文件大小(字节)',
  `file_md5`       VARCHAR(32)                                  COMMENT '文件MD5(防篡改)',
  `upload_by`      BIGINT                                       COMMENT '上传人ID',
  `upload_time`    DATETIME      NOT NULL  DEFAULT CURRENT_TIMESTAMP  COMMENT '上传时间',
  `deleted`        TINYINT       NOT NULL  DEFAULT 0           COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_contract_id` (`contract_id`),
  KEY `idx_deleted`     (`deleted`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci  COMMENT='合同附件表';

CREATE TABLE `work_log` (
  `id`               BIGINT   NOT NULL  AUTO_INCREMENT  COMMENT '工作日志ID',
  `sales_rep_id`     BIGINT   NOT NULL                     COMMENT '销售ID',
  `log_date`         DATE     NOT NULL                     COMMENT '日志日期',
  `calls_made`       INT      NOT NULL  DEFAULT 0         COMMENT '打电话数',
  `effective_calls`   INT      NOT NULL  DEFAULT 0         COMMENT '有效电话数',
  `new_intentions`   INT      NOT NULL  DEFAULT 0         COMMENT '新增意向客户数',
  `intention_clients` INT     NOT NULL  DEFAULT 0         COMMENT '跟进意向客户数',
  `face_to_face_clients` INT  NOT NULL  DEFAULT 0         COMMENT '面谈客户数',
  `signed_contracts`  INT      NOT NULL  DEFAULT 0         COMMENT '签约合同数',
  `content`          TEXT                                      COMMENT '工作内容备注',
  `created_at`       DATETIME  NOT NULL  DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
  `updated_at`       DATETIME  NOT NULL  DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP  COMMENT '修改时间',
  `deleted`          TINYINT  NOT NULL  DEFAULT 0           COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_rep_date` (`sales_rep_id`, `log_date`),
  KEY `idx_sales_rep_id` (`sales_rep_id`),
  KEY `idx_log_date`    (`log_date`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci  COMMENT='销售工作日志表';

CREATE TABLE `performance_record` (
  `id`                  BIGINT        NOT NULL  AUTO_INCREMENT  COMMENT '业绩记录ID',
  `contract_id`         BIGINT        NOT NULL                     COMMENT '关联合同ID',
  `sales_rep_id`        BIGINT        NOT NULL                     COMMENT '销售ID',
  `dept_id`             BIGINT        NOT NULL                     COMMENT '部门ID',
  `zone_id`             BIGINT                                       COMMENT '战区ID',
  `contract_amount`     DECIMAL(15,2) NOT NULL                     COMMENT '合同金额',
  `commission_rate`     DECIMAL(6,4)  NOT NULL                     COMMENT '提成比例',
  `commission_amount`   DECIMAL(15,2) NOT NULL                     COMMENT '提成金额',
  `status`              TINYINT       NOT NULL  DEFAULT 1         COMMENT '状态: 1-计算中 2-已确认 3-已发放 4-已取消',
  `calculate_time`      DATETIME      NOT NULL  DEFAULT CURRENT_TIMESTAMP  COMMENT '计算时间',
  `confirm_time`        DATETIME                                      COMMENT '确认时间',
  `grant_time`          DATETIME                                      COMMENT '发放时间',
  `cancel_reason`       VARCHAR(200)                                  COMMENT '取消原因',
  `remark`              TEXT                                          COMMENT '备注',
  `created_by`          BIGINT                                        COMMENT '创建人',
  `created_at`          DATETIME      NOT NULL  DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
  `updated_by`          BIGINT                                        COMMENT '修改人',
  `updated_at`          DATETIME      NOT NULL  DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP  COMMENT '修改时间',
  `deleted`             TINYINT       NOT NULL  DEFAULT 0           COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_contract_id` (`contract_id`),
  KEY `idx_sales_rep_id`  (`sales_rep_id`),
  KEY `idx_dept_id`       (`dept_id`),
  KEY `idx_zone_id`       (`zone_id`),
  KEY `idx_status`        (`status`),
  KEY `idx_deleted`        (`deleted`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci  COMMENT='业绩记录表';

CREATE TABLE `customer_transfer_log` (
  `id`             BIGINT     NOT NULL  AUTO_INCREMENT  COMMENT '转移记录ID',
  `customer_id`    BIGINT     NOT NULL                     COMMENT '客户ID',
  `from_rep_id`   BIGINT     NOT NULL                     COMMENT '转出销售ID',
  `to_rep_id`     BIGINT     NOT NULL                     COMMENT '转入销售ID',
  `operate_type`  VARCHAR(20) NOT NULL                     COMMENT '操作类型: dept_manager_transfer/public_sea_claim/manager_assign',
  `reason`        VARCHAR(200)                              COMMENT '转移原因',
  `operated_by`   BIGINT     NOT NULL                     COMMENT '操作人ID',
  `operated_at`   DATETIME   NOT NULL  DEFAULT CURRENT_TIMESTAMP  COMMENT '操作时间',
  `deleted`       TINYINT    NOT NULL  DEFAULT 0           COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_customer_id` (`customer_id`),
  KEY `idx_from_rep_id` (`from_rep_id`),
  KEY `idx_to_rep_id`   (`to_rep_id`),
  KEY `idx_operated_at` (`operated_at`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci  COMMENT='客户转移记录表';


USE dafuweng_finance;

-- ================================================================
-- 第5步: dafuweng_finance 库 — 金融产品/贷款审核/服务费
-- ================================================================

CREATE TABLE `bank` (
  `id`           BIGINT       NOT NULL  AUTO_INCREMENT  COMMENT '银行ID',
  `bank_code`   VARCHAR(50)  NOT NULL                     COMMENT '银行编码',
  `bank_name`   VARCHAR(100) NOT NULL                     COMMENT '银行名称',
  `bank_branch`  VARCHAR(200)                              COMMENT '开户支行',
  `contact_person` VARCHAR(50)                            COMMENT '联系人',
  `contact_phone` VARCHAR(20)                             COMMENT '联系电话',
  `status`      TINYINT      NOT NULL  DEFAULT 1         COMMENT '状态: 1-合作中 0-暂停',
  `sort_order`  INT          NOT NULL  DEFAULT 0         COMMENT '显示顺序',
  `created_by`  BIGINT                                     COMMENT '创建人',
  `created_at`  DATETIME     NOT NULL  DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
  `updated_by`  BIGINT                                     COMMENT '修改人',
  `updated_at`  DATETIME     NOT NULL  DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP  COMMENT '修改时间',
  `deleted`     TINYINT      NOT NULL  DEFAULT 0           COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_bank_code` (`bank_code`),
  KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci  COMMENT='合作银行表';

CREATE TABLE `finance_product` (
  `id`              BIGINT         NOT NULL  AUTO_INCREMENT  COMMENT '产品ID',
  `product_code`   VARCHAR(50)    NOT NULL                     COMMENT '产品编码',
  `product_name`   VARCHAR(100)   NOT NULL                     COMMENT '产品名称',
  `bank_id`        BIGINT         NOT NULL                     COMMENT '所属银行ID',
  `min_amount`     DECIMAL(15,2)  NOT NULL                     COMMENT '最小贷款金额',
  `max_amount`     DECIMAL(15,2)  NOT NULL                     COMMENT '最大贷款金额',
  `interest_rate`  DECIMAL(7,4)   NOT NULL                     COMMENT '年利率',
  `min_term`       INT            NOT NULL                     COMMENT '最小期限(月)',
  `max_term`       INT            NOT NULL                     COMMENT '最大期限(月)',
  `requirements`   TEXT                                        COMMENT '申请条件(JSON数组)',
  `documents`      TEXT                                        COMMENT '所需材料(JSON数组)',
  `product_features` TEXT                                     COMMENT '产品特点',
  `commission_rate` DECIMAL(6,4)                               COMMENT '渠道佣金比例',
  `status`         TINYINT        NOT NULL  DEFAULT 1         COMMENT '状态: 1-上架 0-下架',
  `sort_order`    INT            NOT NULL  DEFAULT 0         COMMENT '显示顺序',
  `online_time`   DATETIME                                      COMMENT '上架时间',
  `offline_time`  DATETIME                                      COMMENT '下架时间',
  `created_by`    BIGINT                                       COMMENT '创建人',
  `created_at`    DATETIME       NOT NULL  DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
  `updated_by`    BIGINT                                       COMMENT '修改人',
  `updated_at`    DATETIME       NOT NULL  DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP  COMMENT '修改时间',
  `deleted`       TINYINT        NOT NULL  DEFAULT 0           COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_product_code` (`product_code`),
  KEY `idx_bank_id`   (`bank_id`),
  KEY `idx_status`    (`status`),
  KEY `idx_min_amount`(`min_amount`),
  KEY `idx_deleted`   (`deleted`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci  COMMENT='金融产品表';

CREATE TABLE `loan_audit` (
  `id`                  BIGINT        NOT NULL  AUTO_INCREMENT  COMMENT '审核ID',
  `contract_id`         BIGINT        NOT NULL                     COMMENT '关联合同ID',
  `finance_specialist_id` BIGINT                                    COMMENT '负责金融专员ID',
  `recommended_product_id` BIGINT                                  COMMENT '推荐产品ID',
  `approved_amount`     DECIMAL(15,2)                              COMMENT '审批金额',
  `approved_term`      INT                                         COMMENT '审批期限(月)',
  `approved_interest_rate` DECIMAL(7,4)                            COMMENT '审批利率',
  `audit_status`       TINYINT       NOT NULL  DEFAULT 1         COMMENT '审核状态: 1-待审核 2-审核中 3-已通过 4-已拒绝',
  `bank_id`            BIGINT                                       COMMENT '提交银行ID',
  `bank_audit_status`  VARCHAR(30)                                 COMMENT '银行审核状态: pending/in_review/approved/rejected',
  `bank_apply_time`    DATETIME                                    COMMENT '提交银行时间',
  `bank_feedback_time` DATETIME                                    COMMENT '银行反馈时间',
  `bank_feedback_content` TEXT                                      COMMENT '银行反馈内容',
  `reject_reason`      VARCHAR(500)                                 COMMENT '拒绝原因',
  `audit_opinion`      TEXT                                         COMMENT '审核意见',
  `audit_date`         DATETIME                                    COMMENT '审核日期(内部)',
  `loan_granted_date`  DATETIME                                    COMMENT '银行放款日期',
  `actual_loan_amount` DECIMAL(15,2)                              COMMENT '实际放款金额',
  `actual_interest_rate` DECIMAL(7,4)                               COMMENT '实际执行利率',
  `created_at`          DATETIME      NOT NULL  DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
  `updated_at`          DATETIME      NOT NULL  DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP  COMMENT '修改时间',
  `deleted`             TINYINT       NOT NULL  DEFAULT 0           COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_contract_id` (`contract_id`),
  KEY `idx_finance_specialist_id` (`finance_specialist_id`),
  KEY `idx_audit_status`      (`audit_status`),
  KEY `idx_bank_id`           (`bank_id`),
  KEY `idx_deleted`            (`deleted`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci  COMMENT='贷款审核表';

CREATE TABLE `loan_audit_record` (
  `id`             BIGINT      NOT NULL  AUTO_INCREMENT  COMMENT '审核记录ID',
  `loan_audit_id` BIGINT      NOT NULL                     COMMENT '贷款审核ID',
  `operator_id`   BIGINT      NOT NULL                     COMMENT '操作人ID',
  `operator_name` VARCHAR(50)                              COMMENT '操作人姓名',
  `operator_role` VARCHAR(30)                              COMMENT '操作人角色',
  `action`        VARCHAR(50) NOT NULL                     COMMENT '操作类型: receive/review/submit_bank/bank_result/approve/reject/return',
  `content`       TEXT                                      COMMENT '操作说明/意见',
  `attachment_urls` TEXT                                     COMMENT '附件URLs',
  `created_at`    DATETIME    NOT NULL  DEFAULT CURRENT_TIMESTAMP  COMMENT '操作时间',
  PRIMARY KEY (`id`),
  KEY `idx_loan_audit_id` (`loan_audit_id`),
  KEY `idx_operator_id`   (`operator_id`),
  KEY `idx_created_at`    (`created_at`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci  COMMENT='贷款审核记录表(审核轨迹)';

CREATE TABLE `service_fee_record` (
  `id`             BIGINT        NOT NULL  AUTO_INCREMENT  COMMENT '服务费记录ID',
  `contract_id`    BIGINT        NOT NULL                     COMMENT '合同ID',
  `fee_type`      TINYINT       NOT NULL                     COMMENT '费用类型: 1-首期服务费 2-二期服务费',
  `amount`        DECIMAL(15,2) NOT NULL                     COMMENT '实收金额',
  `should_amount` DECIMAL(15,2) NOT NULL                     COMMENT '应收金额',
  `payment_method` VARCHAR(30)                               COMMENT '支付方式: bank_transfer/wechat/alipay/cash',
  `payment_status` TINYINT      NOT NULL  DEFAULT 0         COMMENT '支付状态: 0-未付 1-已付 2-部分付',
  `payment_date`   DATE                                        COMMENT '支付日期',
  `payment_account` VARCHAR(100)                              COMMENT '付款账户',
  `receipt_no`    VARCHAR(50)                                 COMMENT '收据编号',
  `accountant_id` BIGINT        NOT NULL                     COMMENT '会计ID',
  `remark`        VARCHAR(500)                                COMMENT '备注',
  `created_at`    DATETIME      NOT NULL  DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
  `updated_at`    DATETIME      NOT NULL  DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP  COMMENT '修改时间',
  `deleted`       TINYINT       NOT NULL  DEFAULT 0           COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_contract_id`   (`contract_id`),
  KEY `idx_fee_type`      (`fee_type`),
  KEY `idx_payment_status`(`payment_status`),
  KEY `idx_accountant_id` (`accountant_id`),
  KEY `idx_deleted`        (`deleted`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci  COMMENT='服务费记录表';

CREATE TABLE `commission_record` (
  `id`             BIGINT        NOT NULL  AUTO_INCREMENT  COMMENT '提成记录ID',
  `performance_id` BIGINT        NOT NULL                     COMMENT '业绩记录ID',
  `sales_rep_id`   BIGINT        NOT NULL                     COMMENT '销售ID',
  `contract_id`    BIGINT        NOT NULL                     COMMENT '合同ID',
  `commission_amount` DECIMAL(15,2) NOT NULL                  COMMENT '提成金额',
  `commission_rate` DECIMAL(6,4)  NOT NULL                     COMMENT '提成比例',
  `status`         TINYINT       NOT NULL  DEFAULT 1         COMMENT '状态: 1-待确认 2-已确认 3-已发放',
  `confirm_time`  DATETIME                                     COMMENT '确认时间',
  `grant_time`    DATETIME                                     COMMENT '发放时间',
  `grant_account` VARCHAR(100)                                COMMENT '发放账户',
  `remark`        VARCHAR(500)                                COMMENT '备注',
  `created_at`    DATETIME      NOT NULL  DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
  `updated_at`    DATETIME      NOT NULL  DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP  COMMENT '修改时间',
  `deleted`       TINYINT       NOT NULL  DEFAULT 0           COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_performance_id` (`performance_id`),
  KEY `idx_sales_rep_id`   (`sales_rep_id`),
  KEY `idx_contract_id`     (`contract_id`),
  KEY `idx_status`         (`status`),
  KEY `idx_deleted`        (`deleted`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci  COMMENT='提成发放记录表';

-- ================================================================
-- 第6步: 创建公共触发器/事件 (可选,MySQL事件调度器)
-- ================================================================

-- 启用事件调度器(可选,也可以用RabbitMQ定时任务代替)
-- SET GLOBAL event_scheduler = ON;

-- 示例: 每天凌晨2点执行公海客户扫描(实际生产中建议用RabbitMQ延迟队列)
-- DELIMITER $$
-- CREATE EVENT IF NOT EXISTS evt_customer_public_sea_check
-- ON SCHEDULE EVERY 1 DAY STARTS '2026-04-01 02:00:00'
-- DO
-- BEGIN
--   DECLARE v_public_sea_days INT;
--   SELECT CAST(param_value AS UNSIGNED) INTO v_public_sea_days
--   FROM dafuweng_system.sys_param
--   WHERE param_key = 'customer.public_sea_days' AND deleted = 0;
--
--   UPDATE dafuweng_sales.customer
--   SET status = 5, public_sea_time = NOW(), updated_at = NOW()
--   WHERE status NOT IN (3, 4, 5)
--     AND (next_follow_up_date IS NULL OR next_follow_up_date < NOW())
--     AND TIMESTAMPDIFF(DAY, created_at, NOW()) >= v_public_sea_days
--     AND deleted = 0
--     AND public_sea_time IS NULL;
-- END$$
-- DELIMITER ;
