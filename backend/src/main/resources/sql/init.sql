-- =====================================================
-- UI 自动化测试平台数据库初始化脚本
-- 创建时间: 2026-04-01
-- 更新说明: 修复测试步骤保存时缺少字段的问题
-- =====================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `ui_auto_test` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `ui_auto_test`;

-- =====================================================
-- 用户表
-- =====================================================
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(100) NOT NULL COMMENT '密码(MD5加密)',
  `nickname` varchar(50) DEFAULT NULL COMMENT '昵称',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '状态：0-禁用，1-正常',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像',
  `gender` varchar(10) DEFAULT NULL COMMENT '性别',
  `department` varchar(50) DEFAULT NULL COMMENT '部门',
  `phone` varchar(20) DEFAULT NULL COMMENT '电话',
  `employee_no` varchar(20) DEFAULT NULL COMMENT '工号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 插入默认管理员 (用户名:admin, 密码:admin123 的MD5值)
INSERT INTO `sys_user` (`username`, `password`, `nickname`, `email`, `status`) 
VALUES ('admin', '0192023a7bbd73250516f069df18b500', '管理员', 'admin@uiauto.com', 1);

-- =====================================================
-- 菜单表
-- =====================================================
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
  `parent_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '父菜单ID',
  `name` varchar(50) NOT NULL COMMENT '菜单名称',
  `path` varchar(100) DEFAULT NULL COMMENT '路由路径',
  `icon` varchar(50) DEFAULT NULL COMMENT '图标',
  `sort_order` int(11) DEFAULT '0' COMMENT '排序',
  `status` tinyint(4) DEFAULT '1' COMMENT '状态',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单表';

INSERT INTO `sys_menu` VALUES
(1, 0, '首页', '/dashboard', 'HomeFilled', 1, 1, '2024-01-01 00:00:00', '2024-01-01 00:00:00'),
(2, 0, '测试执行', '/execution', 'List', 2, 1, '2024-01-01 00:00:00', '2024-01-01 00:00:00'),
(3, 0, '测试用例', '/case', 'Document', 3, 1, '2024-01-01 00:00:00', '2024-01-01 00:00:00'),
(4, 0, '任务管理', '/task', 'Clock', 4, 1, '2024-01-01 00:00:00', '2024-01-01 00:00:00'),
(5, 0, '系统管理', '/system', 'Setting', 5, 1, '2024-01-01 00:00:00', '2024-01-01 00:00:00'),
(6, 5, '用户管理', '/system/user', 'User', 1, 1, '2024-01-01 00:00:00', '2024-01-01 00:00:00'),
(7, 5, '角色管理', '/system/role', 'Peoples', 2, 1, '2024-01-01 00:00:00', '2024-01-01 00:00:00');

-- =====================================================
-- 测试用例表
-- =====================================================
DROP TABLE IF EXISTS `test_case`;
CREATE TABLE `test_case` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '用例ID',
  `case_no` varchar(50) NOT NULL COMMENT '用例编号',
  `name` varchar(200) NOT NULL COMMENT '用例名称',
  `description` text COMMENT '用例描述',
  `designer` varchar(50) DEFAULT NULL COMMENT '设计者',
  `case_type` varchar(20) DEFAULT 'positive' COMMENT '用例性质:positive-正例,negative-反例',
  `status` int(4) DEFAULT 1 COMMENT '状态:0-禁用,1-启用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_case_no` (`case_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='测试用例表';

-- =====================================================
-- 测试用例步骤表 (修复：添加 action、step_name、locator_type 等字段)
-- =====================================================
DROP TABLE IF EXISTS `test_case_step`;
CREATE TABLE `test_case_step` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '步骤ID',
  `case_id` bigint(20) NOT NULL COMMENT '用例ID',
  `step_no` int(11) NOT NULL COMMENT '步骤序号',
  `step_description` varchar(500) DEFAULT NULL COMMENT '步骤描述',
  `assertion_description` varchar(500) DEFAULT NULL COMMENT '断言描述',
  `step_name` varchar(100) DEFAULT NULL COMMENT '步骤名称',
  `action` varchar(50) NOT NULL DEFAULT 'click' COMMENT '操作类型：click/input/wait/assert等',
  `locator_type` varchar(20) DEFAULT NULL COMMENT '定位器类型：xpath/css/id/等',
  `locator_value` text DEFAULT NULL COMMENT '定位器值',
  `input_data` text DEFAULT NULL COMMENT '输入数据',
  `expected_result` text DEFAULT NULL COMMENT '预期结果',
  `actual_result` text DEFAULT NULL COMMENT '实际结果',
  `status` varchar(20) DEFAULT 'pending' COMMENT '状态：pending/running/success/failed',
  `screenshot` tinyint(4) NOT NULL DEFAULT 1 COMMENT '是否截图：0-否，1-是',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_case_id` (`case_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='测试用例步骤表';

-- =====================================================
-- 测试执行记录表 (修复：添加 description、start_time、end_time 字段)
-- =====================================================
DROP TABLE IF EXISTS `test_case_execution`;
CREATE TABLE `test_case_execution` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '执行ID',
  `case_id` bigint(20) NOT NULL COMMENT '用例ID',
  `case_no` varchar(50) DEFAULT NULL COMMENT '用例编号',
  `case_name` varchar(200) DEFAULT NULL COMMENT '用例名称',
  `description` text DEFAULT NULL COMMENT '用例描述',
  `executor` varchar(50) DEFAULT NULL COMMENT '执行人',
  `execute_time` datetime DEFAULT NULL COMMENT '执行时间',
  `status` varchar(20) DEFAULT 'pending' COMMENT '执行状态',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `duration` bigint(20) DEFAULT 0 COMMENT '执行耗时(毫秒)',
  `passed_count` int(11) DEFAULT 0 COMMENT '通过数',
  `failed_count` int(11) DEFAULT 0 COMMENT '失败数',
  `total_count` int(11) DEFAULT 0 COMMENT '总数',
  `error_message` text COMMENT '错误信息',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_case_id` (`case_id`),
  KEY `idx_execute_time` (`execute_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='测试执行记录表';

-- =====================================================
-- 测试步骤执行记录表 (修复：添加 step_description, assertion_status, assertion_description 字段)
-- =====================================================
DROP TABLE IF EXISTS `test_step_execution`;
CREATE TABLE `test_step_execution` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '步骤执行ID',
  `execution_id` bigint(20) NOT NULL COMMENT '执行记录ID',
  `step_id` bigint(20) DEFAULT NULL COMMENT '步骤ID',
  `step_no` int(11) DEFAULT NULL COMMENT '步骤序号',
  `step_description` varchar(500) DEFAULT NULL COMMENT '步骤描述',
  `action` varchar(50) DEFAULT NULL COMMENT '操作类型',
  `status` varchar(20) DEFAULT 'pending' COMMENT '执行状态',
  `assertion_status` varchar(20) DEFAULT NULL COMMENT '断言状态',
  `assertion_description` varchar(500) DEFAULT NULL COMMENT '断言描述',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `duration` bigint(20) DEFAULT 0 COMMENT '执行耗时(毫秒)',
  `input_data` text COMMENT '输入数据',
  `expected_result` text COMMENT '预期结果',
  `actual_result` text COMMENT '实际结果',
  `error_message` text COMMENT '错误信息',
  `screenshot` varchar(500) DEFAULT NULL COMMENT '截图路径',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_execution_id` (`execution_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='测试步骤执行记录表';

-- =====================================================
-- 部门表
-- =====================================================
DROP TABLE IF EXISTS `department`;
CREATE TABLE `department` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '部门ID',
  `name` varchar(100) NOT NULL COMMENT '部门名称',
  `parent_id` bigint(20) DEFAULT 0 COMMENT '父部门ID',
  `level` int(11) DEFAULT 1 COMMENT '层级',
  `sort_order` int(11) DEFAULT 0 COMMENT '排序',
  `leader` varchar(50) DEFAULT NULL COMMENT '负责人',
  `phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `status` int(4) DEFAULT 1 COMMENT '状态',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门表';

-- 插入默认部门
INSERT INTO `department` (`name`, `parent_id`, `level`, `sort_order`, `leader`, `status`) VALUES
('研发部', 0, 1, 1, '张三', 1),
('测试部', 0, 1, 2, '李四', 1),
('产品部', 0, 1, 3, '王五', 1);

-- =====================================================
-- 执行结果提示
-- =====================================================
SELECT '数据库初始化完成！' AS status;