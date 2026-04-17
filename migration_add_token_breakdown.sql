-- 为 test_step_execution 表添加 token 消耗明细列
ALTER TABLE test_step_execution ADD COLUMN step_fusion_token_used BIGINT COMMENT '步骤数据融合token消耗';
ALTER TABLE test_step_execution ADD COLUMN page_operation_token_used BIGINT COMMENT '页面操作token消耗';
ALTER TABLE test_step_execution ADD COLUMN assertion_token_used BIGINT COMMENT 'AI断言token消耗';
