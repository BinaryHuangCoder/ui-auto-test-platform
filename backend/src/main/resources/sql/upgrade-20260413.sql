
-- =====================================================
-- UI 自动化测试平台数据库升级脚本
-- 更新日期: 2026-04-13
-- 说明: 新增测试任务与用例关联表、测试任务执行历史表、测试任务执行步骤详情表
-- =====================================================

USE `ui_auto_test`;

-- =====================================================
-- 1. 新增测试任务与用例关联表
-- =====================================================
CREATE TABLE IF NOT EXISTS `test_task_case` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `task_id` bigint(20) NOT NULL COMMENT '任务ID',
  `case_id` bigint(20) NOT NULL COMMENT '用例ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_task_case` (`task_id`, `case_id`),
  KEY `idx_task_id` (`task_id`),
  KEY `idx_case_id` (`case_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='测试任务与用例关联表';

-- =====================================================
-- 2. 新增测试任务执行历史表
-- =====================================================
CREATE TABLE IF NOT EXISTS `test_task_execution` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '执行ID',
  `task_id` bigint(20) NOT NULL COMMENT '任务ID',
  `task_no` varchar(50) DEFAULT NULL COMMENT '任务编号',
  `task_name` varchar(200) DEFAULT NULL COMMENT '任务名称',
  `executor` varchar(50) DEFAULT NULL COMMENT '执行人',
  `execute_time` datetime DEFAULT NULL COMMENT '执行时间',
  `status` varchar(20) DEFAULT 'pending' COMMENT '执行状态',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `duration` bigint(20) DEFAULT 0 COMMENT '执行耗时(毫秒)',
  `total_count` int(11) DEFAULT 0 COMMENT '总用例数',
  `passed_count` int(11) DEFAULT 0 COMMENT '通过数',
  `failed_count` int(11) DEFAULT 0 COMMENT '失败数',
  `ai_total_token_used` bigint(20) DEFAULT 0 COMMENT 'AI总token消耗量',
  `error_message` text COMMENT '错误信息',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_task_id` (`task_id`),
  KEY `idx_execute_time` (`execute_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='测试任务执行历史表';

-- =====================================================
-- 3. 新增测试任务执行步骤详情表
-- =====================================================
CREATE TABLE IF NOT EXISTS `test_task_step_execution` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '步骤执行ID',
  `task_execution_id` bigint(20) NOT NULL COMMENT '任务执行记录ID',
  `case_execution_id` bigint(20) NOT NULL COMMENT '用例执行记录ID',
  `step_id` bigint(20) DEFAULT NULL COMMENT '步骤ID',
  `step_no` int(11) DEFAULT NULL COMMENT '步骤序号',
  `step_description` varchar(500) DEFAULT NULL COMMENT '步骤描述',
  `action` varchar(50) DEFAULT NULL COMMENT '操作类型',
  `status` varchar(20) DEFAULT 'pending' COMMENT '执行状态',
  `assertion_status` varchar(20) DEFAULT NULL COMMENT '断言状态',
  `assertion_description` varchar(500) DEFAULT NULL COMMENT '断言描述',
  `ai_token_used` bigint(20) DEFAULT 0 COMMENT '单步AI token消耗量',
  `ai_result` text COMMENT 'AI断言结果',
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
  KEY `idx_task_execution_id` (`task_execution_id`),
  KEY `idx_case_execution_id` (`case_execution_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='测试任务执行步骤详情表';

-- =====================================================
-- 执行完成提示
-- =====================================================
SELECT '数据库升级完成！' AS status;

