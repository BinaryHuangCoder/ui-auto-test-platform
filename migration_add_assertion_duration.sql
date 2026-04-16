-- 为 test_step_execution 表添加 assertion_duration 列（AI断言耗时，毫秒）
ALTER TABLE test_step_execution ADD COLUMN assertion_duration BIGINT COMMENT 'AI断言耗时（毫秒）';
