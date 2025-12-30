-- 公告表
CREATE TABLE IF NOT EXISTS `announcements` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '公告ID',
  `title` VARCHAR(200) NOT NULL COMMENT '公告标题',
  `content` TEXT NOT NULL COMMENT '公告内容',
  `type` TINYINT NOT NULL DEFAULT 1 COMMENT '公告类型：1-系统公告，2-活动公告，3-维护公告',
  `priority` TINYINT NOT NULL DEFAULT 1 COMMENT '优先级：1-普通，2-重要，3-紧急',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-已发布，2-草稿，3-已下线',
  `publish_time` DATETIME DEFAULT NULL COMMENT '发布时间',
  `expire_time` DATETIME DEFAULT NULL COMMENT '过期时间（可选）',
  `view_count` INT UNSIGNED DEFAULT 0 COMMENT '浏览次数',
  `created_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '创建人ID（管理员）',
  `updated_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '更新人ID（管理员）',
  `extra` JSON DEFAULT NULL COMMENT '扩展信息',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_at` DATETIME DEFAULT NULL COMMENT '删除时间（软删除）',
  PRIMARY KEY (`id`),
  KEY `idx_type` (`type`),
  KEY `idx_status` (`status`),
  KEY `idx_priority` (`priority`),
  KEY `idx_publish_time` (`publish_time`),
  KEY `idx_created_at` (`created_at`),
  KEY `idx_deleted_at` (`deleted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公告表';

