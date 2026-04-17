-- 添加并发数字段到测试任务表
ALTER TABLE test_task ADD COLUMN concurrency INT DEFAULT 1 COMMENT '并发数：1-10，默认为1';
