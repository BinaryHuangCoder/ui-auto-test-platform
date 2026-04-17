
-- 数据库迁移：添加新列
-- 执行时间：2026-04-15

-- 1. 为 test_case_execution 表添加新列
ALTER TABLE test_case_execution 
ADD COLUMN image_assertion_model VARCHAR(200) COMMENT '图像断言检查使用的模型' AFTER ai_total_token_used,
ADD COLUMN step_fusion_model VARCHAR(200) COMMENT '步骤数据融合使用的模型' AFTER image_assertion_model;

-- 2. 为 test_step_execution 表添加新列
ALTER TABLE test_step_execution 
ADD COLUMN test_data TEXT COMMENT '测试数据' AFTER error_message,
ADD COLUMN step_fusion_duration BIGINT DEFAULT 0 COMMENT '步骤数据融合耗时（毫秒）' AFTER test_data,
ADD COLUMN page_operation_duration BIGINT DEFAULT 0 COMMENT '页面操作耗时（毫秒）' AFTER step_fusion_duration;
