-- 为 test_task_execution 表添加执行方式字段
-- 执行方式：manual-手工触发，scheduled-定时触发

ALTER TABLE `test_task_execution` 
ADD COLUMN `execution_mode` VARCHAR(20) DEFAULT 'manual' COMMENT '执行方式：manual-手工触发，scheduled-定时触发' 
AFTER `executor`;
