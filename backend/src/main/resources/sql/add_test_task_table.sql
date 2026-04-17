-- =====================================================
-- 添加测试任务表
-- 创建时间: 2026-04-10
-- =====================================================

USE `ui_auto_test`;

-- =====================================================
-- 测试任务表
-- =====================================================
DROP TABLE IF EXISTS `test_task`;
CREATE TABLE `test_task` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '任务ID',
  `task_no` varchar(50) NOT NULL COMMENT '任务编号（自动生成唯一编号）',
  `name` varchar(200) NOT NULL COMMENT '任务名称',
  `creator` varchar(50) DEFAULT NULL COMMENT '创建人（用户名）',
  `cron_expression` varchar(100) DEFAULT NULL COMMENT '定时策略（cron表达式）',
  `status` int(4) DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_task_no` (`task_no`),
  KEY `idx_creator` (`creator`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='测试任务表';
