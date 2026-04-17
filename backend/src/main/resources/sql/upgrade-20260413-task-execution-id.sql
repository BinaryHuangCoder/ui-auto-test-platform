
-- =====================================================
-- UI 自动化测试平台数据库升级脚本
-- 更新日期: 2026-04-13
-- 说明: 在test_case_execution表添加task_execution_id字段，关联test_task_execution
-- =====================================================

USE `ui_auto_test`;

-- =====================================================
-- 1. 在test_case_execution表添加task_execution_id字段
-- =====================================================
SET @dbname = DATABASE();
SET @tablename = 'test_case_execution';
SET @columnname = 'task_execution_id';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (table_schema = @dbname)
      AND (table_name = @tablename)
      AND (column_name = @columnname)
  ) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE `', @tablename, '` ADD COLUMN `', @columnname, '` bigint(20) DEFAULT NULL COMMENT ''任务执行记录ID'' AFTER `case_id`')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- =====================================================
-- 2. 添加索引
-- =====================================================
SET @indexname = 'idx_task_execution_id';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
    WHERE
      (table_schema = @dbname)
      AND (table_name = @tablename)
      AND (index_name = @indexname)
  ) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE `', @tablename, '` ADD INDEX `', @indexname, '` (`task_execution_id`)')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- =====================================================
-- 执行完成提示
-- =====================================================
SELECT '数据库升级完成！' AS status;
