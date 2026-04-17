
-- 添加融合后的步骤描述字段
ALTER TABLE test_step_execution ADD COLUMN fused_step_description VARCHAR(500) COMMENT '融合后的步骤描述' AFTER test_data;
