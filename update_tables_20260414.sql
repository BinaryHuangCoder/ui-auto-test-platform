-- 添加系统关联字段到测试用例表
ALTER TABLE `test_case` ADD COLUMN `system_id` bigint(20) DEFAULT NULL COMMENT '关联系统ID' AFTER `designer`;

-- 添加测试数据列到测试用例步骤表
ALTER TABLE `test_case_step` ADD COLUMN `test_data` text COMMENT '测试数据' AFTER `input_data`;
