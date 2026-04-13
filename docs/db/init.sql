-- UI 自动化测试平台 - 数据库初始化脚本
-- 版本：v1.2
-- 日期：2026-04-06
-- 作者：huangzhiyong081439

-- 创建数据库
CREATE DATABASE IF NOT EXISTS ui_auto_test DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE ui_auto_test;

-- 测试用例表
CREATE TABLE IF NOT EXISTS test_case (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键 ID',
    name VARCHAR(200) NOT NULL COMMENT '用例名称',
    description TEXT COMMENT '用例描述',
    category VARCHAR(50) COMMENT '分类',
    status TINYINT DEFAULT 1 COMMENT '状态：1-启用 0-禁用',
    created_by VARCHAR(50) COMMENT '创建人',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_category (category),
    INDEX idx_status (status),
    INDEX idx_created_time (created_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='测试用例表';

-- 测试步骤表
CREATE TABLE IF NOT EXISTS test_case_step (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键 ID',
    case_id BIGINT NOT NULL COMMENT '用例 ID',
    step_no INT NOT NULL COMMENT '步骤序号',
    step_type VARCHAR(20) COMMENT '步骤类型：open/click/type/wait/ai-query',
    step_description TEXT COMMENT '步骤描述',
    target VARCHAR(500) COMMENT '目标元素/URL',
    value VARCHAR(500) COMMENT '输入值',
    assertion_description TEXT COMMENT '断言描述',
    screenshot TINYINT DEFAULT 0 COMMENT '是否截图：1-是 0-否',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_case_id (case_id),
    INDEX idx_step_no (step_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='测试步骤表';

-- 用例执行记录表
CREATE TABLE IF NOT EXISTS test_case_execution (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键 ID',
    case_id BIGINT NOT NULL COMMENT '用例 ID',
    case_name VARCHAR(200) COMMENT '用例名称',
    description TEXT COMMENT '执行描述',
    executor VARCHAR(50) COMMENT '执行人',
    start_time DATETIME COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    duration BIGINT COMMENT '执行时长 (ms)',
    status VARCHAR(20) COMMENT '状态：running/success/failed',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_case_id (case_id),
    INDEX idx_status (status),
    INDEX idx_start_time (start_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用例执行记录表';

-- 步骤执行记录表
CREATE TABLE IF NOT EXISTS test_step_execution (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键 ID',
    execution_id BIGINT NOT NULL COMMENT '执行记录 ID',
    step_no INT NOT NULL COMMENT '步骤序号',
    step_description TEXT COMMENT '步骤描述',
    duration BIGINT COMMENT '执行时长 (ms)',
    status VARCHAR(20) COMMENT '状态：success/failed',
    screenshot LONGTEXT COMMENT '截图 Base64',
    assertion_description TEXT COMMENT '断言描述',
    assertion_status VARCHAR(20) COMMENT '断言状态：success/failed',
    ai_result TEXT COMMENT 'AI 执行结果',
    error_message TEXT COMMENT '错误信息',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_execution_id (execution_id),
    INDEX idx_step_no (step_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='步骤执行记录表';

-- 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键 ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码（加密）',
    real_name VARCHAR(50) COMMENT '真实姓名',
    email VARCHAR(100) COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '手机号',
    status TINYINT DEFAULT 1 COMMENT '状态：1-启用 0-禁用',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_username (username),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 角色表
CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键 ID',
    role_name VARCHAR(50) NOT NULL UNIQUE COMMENT '角色名称',
    role_code VARCHAR(50) NOT NULL UNIQUE COMMENT '角色编码',
    description VARCHAR(200) COMMENT '角色描述',
    status TINYINT DEFAULT 1 COMMENT '状态：1-启用 0-禁用',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 用户角色关联表
CREATE TABLE IF NOT EXISTS sys_user_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键 ID',
    user_id BIGINT NOT NULL COMMENT '用户 ID',
    role_id BIGINT NOT NULL COMMENT '角色 ID',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_user_role (user_id, role_id),
    INDEX idx_user_id (user_id),
    INDEX idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 插入初始数据

-- 默认管理员用户（密码：admin123，BCrypt 加密）
INSERT INTO sys_user (id, username, password, real_name, email, status) VALUES
(1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '系统管理员', 'admin@example.com', 1);

-- 默认角色
INSERT INTO sys_role (id, role_name, role_code, description, status) VALUES
(1, '管理员', 'ADMIN', '系统管理员，拥有所有权限', 1),
(2, '测试人员', 'TESTER', '测试人员，可执行测试用例', 1),
(3, '访客', 'GUEST', '访客，只读权限', 1);

-- 关联管理员角色
INSERT INTO sys_user_role (user_id, role_id) VALUES (1, 1);

-- 示例测试用例
INSERT INTO test_case (id, name, description, category, status, created_by) VALUES
(1, '打开测试发布平台', '验证测试发布平台可以正常打开', '冒烟测试', 1, 'admin'),
(2, '统一认证登录页验证', '验证统一认证登录页面元素', '冒烟测试', 1, 'admin');

-- 示例测试步骤 - 用例 1
INSERT INTO test_case_step (case_id, step_no, step_type, step_description, target, value, assertion_description, screenshot) VALUES
(1, 1, 'open', '打开测试发布平台', 'http://10.254.239.10:10086/#/login', NULL, '默认登录用户是 admin', 1),
(1, 2, 'ai-query', '验证页面包含用户名输入框', '验证页面包含用户名输入框', NULL, '验证页面包含用户名输入框', 1);

-- 示例测试步骤 - 用例 2
INSERT INTO test_case_step (case_id, step_no, step_type, step_description, target, value, assertion_description, screenshot) VALUES
(2, 1, 'open', '打开统一认证登录页', 'http://10.254.221.156:7002/login', NULL, '页面包括用户名和密码输入框', 1);

-- 提交事务
COMMIT;
