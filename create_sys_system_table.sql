CREATE TABLE IF NOT EXISTS `sys_system` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '系统ID',
  `system_no` varchar(50) NOT NULL COMMENT '系统编号',
  `system_name` varchar(200) NOT NULL COMMENT '系统名称',
  `system_short_name` varchar(100) DEFAULT NULL COMMENT '系统简称',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：0-禁用，1-正常',
  `source` varchar(20) NOT NULL DEFAULT 'manual' COMMENT '系统来源：manual-手工新增，external-外部同步',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_system_no` (`system_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='应用系统表';
