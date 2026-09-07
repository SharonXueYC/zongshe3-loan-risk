-- 综合贷款风控系统数据库初始化脚本
-- MySQL 8.0+

CREATE DATABASE IF NOT EXISTS `zongshe` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `zongshe`;

-- 管理员用户表
CREATE TABLE IF NOT EXISTS `admin_users` (
                                             `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                             `username` VARCHAR(100) NOT NULL COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码',
    `name` VARCHAR(100) NOT NULL COMMENT '姓名',
    `role` VARCHAR(50) DEFAULT 'admin' COMMENT '角色',
    `email` VARCHAR(150) COMMENT '邮箱',
    `phone_number` VARCHAR(30) COMMENT '手机号',
    `is_active` BOOLEAN DEFAULT true COMMENT '是否激活',
    `last_login` DATETIME COMMENT '最后登录时间',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理员用户表';

-- 插入默认管理员用户
INSERT INTO `admin_users` (`username`, `password`, `name`, `role`, `email`, `phone_number`, `is_active`) VALUES
                                                                                                             ('yunizai', 'yunizai123', '芋泥崽', 'super_admin', 'admin@yunizai.com', '13800138000', true),
                                                                                                             ('admin', 'admin123', '普通管理员', 'admin', 'admin@example.com', '13900139000', true),
                                                                                                             ('finance', 'finance123', '财务管理员', 'finance', 'finance@example.com', '13700137000', true),
                                                                                                             ('operator', 'operator123', '操作员', 'operator', 'operator@example.com', '13600136000', true);

-- 产品表
CREATE TABLE IF NOT EXISTS `products` (
                                          `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                          `product_no` VARCHAR(100) NOT NULL COMMENT '产品编号',
    `product_name` VARCHAR(200) NOT NULL COMMENT '产品名称',
    `product_type` VARCHAR(100) NOT NULL COMMENT '产品类型',
    `min_amount` DECIMAL(15,2) DEFAULT 0.00 COMMENT '最小金额',
    `max_amount` DECIMAL(15,2) DEFAULT 100000.00 COMMENT '最大金额',
    `min_term` INT DEFAULT 1 COMMENT '最短期限(月)',
    `max_term` INT DEFAULT 36 COMMENT '最长期限(月)',
    `interest_rate` DECIMAL(5,2) DEFAULT 8.50 COMMENT '年利率',
    `product_description` TEXT COMMENT '产品描述',
    `status` VARCHAR(50) DEFAULT 'active' COMMENT '状态: active/inactive',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_product_no` (`product_no`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='贷款产品表';

-- 插入示例产品数据
INSERT INTO `products` (`product_no`, `product_name`, `product_type`, `min_amount`, `max_amount`, `min_term`, `max_term`, `interest_rate`, `product_description`, `status`) VALUES
                                                                                                                                                                                ('PROD-2024-001', '个人消费贷', '个人消费贷款', 1000.00, 50000.00, 3, 36, 8.50, '灵活的个人消费贷款，用于日常消费支出', 'active'),
                                                                                                                                                                                ('PROD-2024-002', '个人经营贷', '个人经营贷款', 50000.00, 500000.00, 6, 60, 7.80, '为个体工商户和小微企业提供的经营资金贷款', 'active'),
                                                                                                                                                                                ('PROD-2024-003', '房屋抵押贷款', '房屋抵押贷款', 100000.00, 2000000.00, 12, 120, 6.20, '以房屋作为抵押物的大额贷款，用于购房、装修等', 'active'),
                                                                                                                                                                                ('PROD-2024-004', '汽车贷款', '汽车贷款', 30000.00, 300000.00, 12, 48, 7.20, '用于购买新车或二手车的专项贷款', 'inactive'),
                                                                                                                                                                                ('PROD-2024-005', '小额应急贷', '个人消费贷款', 500.00, 10000.00, 1, 6, 12.00, '用于短期应急的小额贷款', 'active');

-- 贷款申请表
CREATE TABLE IF NOT EXISTS `loan_applications` (
                                                   `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                                   `application_no` VARCHAR(100) NOT NULL COMMENT '申请单号',
    `applicant_name` VARCHAR(100) NOT NULL COMMENT '申请人姓名',
    `id_card_number` VARCHAR(18) NOT NULL COMMENT '身份证号',
    `phone_number` VARCHAR(20) NOT NULL COMMENT '手机号',
    `loan_type` VARCHAR(100) NOT NULL COMMENT '贷款类型',
    `loan_amount` DECIMAL(15,2) NOT NULL COMMENT '贷款金额',
    `loan_term` INT NOT NULL COMMENT '贷款期限(月)',
    `apply_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
    `status` VARCHAR(50) DEFAULT 'pending' COMMENT '状态: pending/approved/rejected',
    `audit_remark` TEXT COMMENT '审核备注',
    `audit_time` DATETIME COMMENT '审核时间',
    `income_info` VARCHAR(500) COMMENT '收入情况',
    `description` TEXT COMMENT '申请说明',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_application_no` (`application_no`),
    KEY `idx_status` (`status`),
    KEY `idx_apply_time` (`apply_time`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='贷款申请表';

-- 插入示例贷款申请数据
INSERT INTO `loan_applications` (`application_no`, `applicant_name`, `id_card_number`, `phone_number`, `loan_type`, `loan_amount`, `loan_term`, `apply_time`, `status`, `income_info`, `description`) VALUES
                                                                                                                                                                                                          ('LA-202311001', '张三', '110101199001011234', '13800138001', '个人消费贷', 50000.00, 12, '2023-11-20 10:30:00', 'pending', '15000元/月', '用于购买家庭用品和装修'),
                                                                                                                                                                                                          ('LA-202311002', '李四', '110101199102022345', '13900139002', '房屋抵押贷款', 1200000.00, 360, '2023-11-19 14:20:00', 'pending', '30000元/月', '购买首套住房'),
                                                                                                                                                                                                          ('LA-202311003', '王五', '110101199203033456', '13700137003', '汽车贷款', 150000.00, 36, '2023-11-18 09:15:00', 'approved', '20000元/月', '购买家用汽车'),
                                                                                                                                                                                                          ('LA-202311004', '赵六', '110101199304044567', '13600136004', '个人经营贷', 300000.00, 24, '2023-11-17 16:45:00', 'rejected', '25000元/月', '扩大经营规模'),
                                                                                                                                                                                                          ('LA-202311005', '孙七', '110101199405055678', '13500135005', '个人消费贷', 30000.00, 12, '2023-11-16 11:20:00', 'pending', '12000元/月', '旅游和购物消费');

-- 系统设置表
CREATE TABLE IF NOT EXISTS `system_settings` (
                                                 `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                                 `setting_key` VARCHAR(200) NOT NULL COMMENT '设置键',
    `setting_value` TEXT COMMENT '设置值',
    `setting_type` VARCHAR(100) DEFAULT 'string' COMMENT '设置类型',
    `description` VARCHAR(500) COMMENT '描述',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_setting_key` (`setting_key`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统设置表';

-- 插入默认系统设置
INSERT INTO `system_settings` (`setting_key`, `setting_value`, `setting_type`, `description`) VALUES
                                                                                                  ('system_name', '个人贷款管理系统', 'string', '系统名称'),
                                                                                                  ('system_version', '1.0.0', 'string', '系统版本'),
                                                                                                  ('system_description', '个人贷款管理系统是一款专为金融机构设计的贷款业务管理平台，提供贷款申请、审批、管理等功能。', 'string', '系统描述'),
                                                                                                  ('password_policy', 'medium', 'string', '密码策略: low/medium/high'),
                                                                                                  ('login_attempts', '5', 'int', '登录尝试次数限制'),
                                                                                                  ('session_timeout', '30', 'int', '会话超时时间(分钟)'),
                                                                                                  ('smtp_server', 'smtp.example.com', 'string', 'SMTP服务器'),
                                                                                                  ('smtp_port', '587', 'int', 'SMTP端口'),
                                                                                                  ('email_from', 'noreply@example.com', 'string', '发件人邮箱');