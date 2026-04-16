-- 2026-04-16 数据库升级脚本
-- 功能：为 sys_model 表增加模型描述字段

USE ui_auto_test;

-- 为 sys_model 表增加 model_description 字段
ALTER TABLE sys_model 
ADD COLUMN model_description VARCHAR(500) COMMENT '模型描述' 
AFTER model_family;

-- 更新完成提示
SELECT '数据库升级完成：已为 sys_model 表增加 model_description 字段' AS message;
