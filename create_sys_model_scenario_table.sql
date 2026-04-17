-- =====================================================
-- 创建模型使用场景配置表
-- 创建时间: 2026-04-14
-- =====================================================

USE `ui_auto_test`;

-- 创建模型使用场景配置表
CREATE TABLE IF NOT EXISTS `sys_model_scenario` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `scenario_code` varchar(50) NOT NULL COMMENT '场景编码（唯一）: image_assertion-图像断言检查, step_fusion-步骤数据融合',
  `scenario_name` varchar(100) NOT NULL COMMENT '场景名称',
  `model_id` bigint(20) DEFAULT NULL COMMENT '关联的模型ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_scenario_code` (`scenario_code`),
  KEY `idx_model_id` (`model_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='模型使用场景配置表';

-- 初始化场景数据
INSERT INTO `sys_model_scenario` (`scenario_code`, `scenario_name`) VALUES
('image_assertion', '图像断言检查'),
('step_fusion', '步骤数据融合');
