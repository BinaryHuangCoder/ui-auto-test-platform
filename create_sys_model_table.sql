CREATE TABLE IF NOT EXISTS `sys_model` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '模型ID',
  `model_name` varchar(200) NOT NULL COMMENT '模型名称',
  `model_url` varchar(500) DEFAULT NULL COMMENT '模型地址',
  `api_key` varchar(500) DEFAULT NULL COMMENT 'API Key',
  `model_family` varchar(100) DEFAULT NULL COMMENT '模型家族',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：0-禁用，1-正常',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模型配置表';
