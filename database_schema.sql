-- ============================================
-- 线上家教接单系统 - 数据库结构SQL
-- 版本: 1.0
-- 创建时间: 2024
-- ============================================

-- 设置字符集
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================
-- 1. 用户表
-- ============================================
CREATE TABLE `users` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名（唯一）',
  `password` VARCHAR(255) NOT NULL COMMENT '密码（加密）',
  `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
  `avatar` VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
  `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  `role` TINYINT NOT NULL DEFAULT 1 COMMENT '角色：1-学生，2-教师，3-管理员',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '账号状态：1-正常，2-禁用',
  `online_status` TINYINT NOT NULL DEFAULT 0 COMMENT '在线状态：0-离线，1-在线',
  `last_login_at` DATETIME DEFAULT NULL COMMENT '最后登录时间',
  `last_login_ip` VARCHAR(50) DEFAULT NULL COMMENT '最后登录IP',
  `extra` JSON DEFAULT NULL COMMENT '扩展信息（JSON格式）',
  `version` INT UNSIGNED NOT NULL DEFAULT 1 COMMENT '版本号（乐观锁）',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_at` DATETIME DEFAULT NULL COMMENT '删除时间（软删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_phone` (`phone`),
  KEY `idx_email` (`email`),
  KEY `idx_role_status` (`role`, `status`),
  KEY `idx_deleted_at` (`deleted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ============================================
-- 2. 教师扩展信息表
-- ============================================
CREATE TABLE `teacher_profiles` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID（逻辑外键）',
  `real_name` VARCHAR(50) DEFAULT NULL COMMENT '真实姓名',
  `introduction` TEXT DEFAULT NULL COMMENT '个人简介',
  `teaching_years` INT DEFAULT 0 COMMENT '教学经验年限',
  `teaching_style` TEXT DEFAULT NULL COMMENT '教学风格描述',
  `rating` DECIMAL(3,2) DEFAULT 0.00 COMMENT '综合评分（0-5分）',
  `rating_count` INT DEFAULT 0 COMMENT '评价总数',
  `bank_account` VARCHAR(100) DEFAULT NULL COMMENT '银行账号',
  `bank_name` VARCHAR(100) DEFAULT NULL COMMENT '开户银行',
  `account_holder` VARCHAR(50) DEFAULT NULL COMMENT '账户持有人',
  `extra` JSON DEFAULT NULL COMMENT '扩展信息',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted_at` DATETIME DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  KEY `idx_rating` (`rating`),
  KEY `idx_deleted_at` (`deleted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教师扩展信息表';

-- ============================================
-- 3. 用户第三方登录关联表（新增）
-- ============================================
CREATE TABLE `user_oauth` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID（逻辑外键）',
  `oauth_type` TINYINT NOT NULL COMMENT '第三方类型：1-微信，2-QQ，3-其他',
  `openid` VARCHAR(100) NOT NULL COMMENT '第三方唯一标识（OpenID）',
  `unionid` VARCHAR(100) DEFAULT NULL COMMENT 'UnionID（微信平台统一标识）',
  `nickname` VARCHAR(100) DEFAULT NULL COMMENT '第三方昵称',
  `avatar` VARCHAR(500) DEFAULT NULL COMMENT '第三方头像URL',
  `extra` JSON DEFAULT NULL COMMENT '扩展信息（JSON格式）',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_oauth` (`oauth_type`, `openid`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_unionid` (`unionid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户第三方登录关联表';

-- ============================================
-- 4. 学生扩展信息表
-- ============================================
CREATE TABLE `student_profiles` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID（逻辑外键）',
  `real_name` VARCHAR(50) DEFAULT NULL COMMENT '真实姓名',
  `grade` VARCHAR(50) DEFAULT NULL COMMENT '年级',
  `school_name` VARCHAR(200) DEFAULT NULL COMMENT '学校名称',
  `learning_goals` TEXT DEFAULT NULL COMMENT '学习目标',
  `weak_subjects` JSON DEFAULT NULL COMMENT '薄弱科目（JSON数组）',
  `parent_name` VARCHAR(50) DEFAULT NULL COMMENT '家长姓名',
  `parent_phone` VARCHAR(20) DEFAULT NULL COMMENT '家长电话',
  `extra` JSON DEFAULT NULL COMMENT '扩展信息',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted_at` DATETIME DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  KEY `idx_grade` (`grade`),
  KEY `idx_deleted_at` (`deleted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生扩展信息表';

-- ============================================
-- 5. 学历认证表
-- ============================================
CREATE TABLE `teacher_certifications` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '教师用户ID',
  `certificate_type` TINYINT NOT NULL DEFAULT 1 COMMENT '证书类型：1-学历证书，2-教师资格证，3-其他',
  `certificate_image` VARCHAR(500) NOT NULL COMMENT '证书图片URL',
  `school_name` VARCHAR(200) DEFAULT NULL COMMENT '学校名称',
  `major` VARCHAR(100) DEFAULT NULL COMMENT '专业',
  `degree` VARCHAR(50) DEFAULT NULL COMMENT '学历：本科、硕士、博士等',
  `graduation_year` INT DEFAULT NULL COMMENT '毕业年份',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '审核状态：1-待审核，2-已通过，3-已拒绝',
  `audit_user_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '审核人ID（管理员）',
  `audit_reason` VARCHAR(500) DEFAULT NULL COMMENT '审核意见/拒绝原因',
  `audit_at` DATETIME DEFAULT NULL COMMENT '审核时间',
  `extra` JSON DEFAULT NULL COMMENT '扩展信息',
  `version` INT UNSIGNED NOT NULL DEFAULT 1 COMMENT '版本号',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted_at` DATETIME DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_deleted_at` (`deleted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教师学历认证表';

-- ============================================
-- 6. 教学阶段字典表
-- ============================================
CREATE TABLE `teaching_stages` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `code` VARCHAR(20) NOT NULL COMMENT '阶段代码（唯一）',
  `name` VARCHAR(50) NOT NULL COMMENT '阶段名称',
  `sort_order` INT DEFAULT 0 COMMENT '排序顺序',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-启用，2-禁用',
  `extra` JSON DEFAULT NULL COMMENT '扩展信息',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted_at` DATETIME DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教学阶段字典表';

-- ============================================
-- 7. 科目字典表
-- ============================================
CREATE TABLE `subjects` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `code` VARCHAR(20) NOT NULL COMMENT '科目代码（唯一）',
  `name` VARCHAR(50) NOT NULL COMMENT '科目名称',
  `category` VARCHAR(50) DEFAULT NULL COMMENT '科目分类：理科、文科等',
  `sort_order` INT DEFAULT 0 COMMENT '排序顺序',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-启用，2-禁用',
  `extra` JSON DEFAULT NULL COMMENT '扩展信息',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted_at` DATETIME DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='科目字典表';

-- ============================================
-- 8. 教师教学信息表
-- ============================================
CREATE TABLE `teacher_teachings` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '教师用户ID',
  `stage_id` INT UNSIGNED NOT NULL COMMENT '教学阶段ID',
  `subject_id` INT UNSIGNED NOT NULL COMMENT '科目ID',
  `price_per_hour` DECIMAL(10,2) NOT NULL COMMENT '课时价格（元/小时）',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-启用，2-禁用',
  `extra` JSON DEFAULT NULL COMMENT '扩展信息',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted_at` DATETIME DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_stage_subject` (`user_id`, `stage_id`, `subject_id`, `deleted_at`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_stage_subject` (`stage_id`, `subject_id`),
  KEY `idx_price` (`price_per_hour`),
  KEY `idx_deleted_at` (`deleted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教师教学信息表';

-- ============================================
-- 9. 教师空闲时间表
-- ============================================
CREATE TABLE `teacher_schedules` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '教师用户ID',
  `weekday` TINYINT NOT NULL COMMENT '星期几：1-周一，2-周二，...，7-周日',
  `start_time` TIME NOT NULL COMMENT '开始时间',
  `end_time` TIME NOT NULL COMMENT '结束时间',
  `schedule_type` TINYINT NOT NULL DEFAULT 1 COMMENT '时间类型：1-固定周期，2-临时时间段',
  `start_date` DATE DEFAULT NULL COMMENT '生效开始日期（临时时间段）',
  `end_date` DATE DEFAULT NULL COMMENT '生效结束日期（临时时间段）',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-可用，2-已禁用',
  `extra` JSON DEFAULT NULL COMMENT '扩展信息',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted_at` DATETIME DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_weekday_time` (`weekday`, `start_time`, `end_time`),
  KEY `idx_date_range` (`start_date`, `end_date`),
  KEY `idx_deleted_at` (`deleted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教师空闲时间表';

-- ============================================
-- 10. 预约表
-- ============================================
CREATE TABLE `appointments` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '预约ID',
  `order_no` VARCHAR(50) NOT NULL COMMENT '订单号（唯一）',
  `student_id` BIGINT UNSIGNED NOT NULL COMMENT '学生用户ID',
  `teacher_id` BIGINT UNSIGNED NOT NULL COMMENT '教师用户ID',
  `stage_id` INT UNSIGNED NOT NULL COMMENT '教学阶段ID',
  `subject_id` INT UNSIGNED NOT NULL COMMENT '科目ID',
  `appointment_date` DATE NOT NULL COMMENT '预约日期',
  `start_time` TIME NOT NULL COMMENT '开始时间',
  `end_time` TIME NOT NULL COMMENT '结束时间',
  `duration` INT NOT NULL COMMENT '课程时长（分钟）',
  `price_per_hour` DECIMAL(10,2) NOT NULL COMMENT '课时单价',
  `total_amount` DECIMAL(10,2) NOT NULL COMMENT '总金额',
  `student_name` VARCHAR(50) DEFAULT NULL COMMENT '学生姓名',
  `student_grade` VARCHAR(50) DEFAULT NULL COMMENT '学生年级',
  `student_phone` VARCHAR(20) DEFAULT NULL COMMENT '学生联系方式',
  `remark` TEXT DEFAULT NULL COMMENT '备注信息（学习需求等）',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '预约状态：1-待确认，2-已确认，3-已完成，4-已取消',
  `cancel_reason` VARCHAR(500) DEFAULT NULL COMMENT '取消原因',
  `cancel_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '取消人ID',
  `cancel_at` DATETIME DEFAULT NULL COMMENT '取消时间',
  `dingtalk_url` VARCHAR(500) DEFAULT NULL COMMENT '钉钉会议链接',
  `confirmed_at` DATETIME DEFAULT NULL COMMENT '确认时间',
  `completed_at` DATETIME DEFAULT NULL COMMENT '完成时间',
  `extra` JSON DEFAULT NULL COMMENT '扩展信息',
  `version` INT UNSIGNED NOT NULL DEFAULT 1 COMMENT '版本号（乐观锁）',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted_at` DATETIME DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_student_id` (`student_id`),
  KEY `idx_teacher_id` (`teacher_id`),
  KEY `idx_status` (`status`),
  KEY `idx_appointment_date` (`appointment_date`),
  KEY `idx_created_at` (`created_at`),
  KEY `idx_deleted_at` (`deleted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预约表';

-- ============================================
-- 11. 支付记录表（增强版）
-- ============================================
CREATE TABLE `payments` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `payment_no` VARCHAR(50) NOT NULL COMMENT '支付单号（唯一）',
  `appointment_id` BIGINT UNSIGNED NOT NULL COMMENT '预约ID',
  `student_id` BIGINT UNSIGNED NOT NULL COMMENT '学生用户ID',
  `teacher_id` BIGINT UNSIGNED NOT NULL COMMENT '教师用户ID',
  `amount` DECIMAL(10,2) NOT NULL COMMENT '支付金额',
  `payment_method` TINYINT NOT NULL DEFAULT 1 COMMENT '支付方式：1-银行转账，2-支付宝，3-微信，4-其他',
  `transfer_image` VARCHAR(500) DEFAULT NULL COMMENT '转账截图URL',
  `transfer_time` DATETIME DEFAULT NULL COMMENT '转账时间',
  `transfer_account` VARCHAR(100) DEFAULT NULL COMMENT '转账账号',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '支付状态：1-待支付，2-待确认，3-已完成，4-已拒绝',
  `reject_reason` VARCHAR(500) DEFAULT NULL COMMENT '拒绝原因',
  `confirmed_at` DATETIME DEFAULT NULL COMMENT '确认收款时间',
  `confirmed_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '确认人ID（教师）',
  `extra` JSON DEFAULT NULL COMMENT '扩展信息',
  `version` INT UNSIGNED NOT NULL DEFAULT 1 COMMENT '版本号',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted_at` DATETIME DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_payment_no` (`payment_no`),
  KEY `idx_appointment_id` (`appointment_id`),
  KEY `idx_student_id` (`student_id`),
  KEY `idx_teacher_id` (`teacher_id`),
  KEY `idx_status` (`status`),
  KEY `idx_payment_method` (`payment_method`),
  KEY `idx_created_at` (`created_at`),
  KEY `idx_deleted_at` (`deleted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付记录表';

-- ============================================
-- 12. 退款记录表（新增）
-- ============================================
CREATE TABLE `refunds` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `refund_no` VARCHAR(50) NOT NULL COMMENT '退款单号（唯一）',
  `payment_id` BIGINT UNSIGNED NOT NULL COMMENT '支付记录ID',
  `appointment_id` BIGINT UNSIGNED NOT NULL COMMENT '预约ID',
  `student_id` BIGINT UNSIGNED NOT NULL COMMENT '学生用户ID',
  `teacher_id` BIGINT UNSIGNED NOT NULL COMMENT '教师用户ID',
  `refund_amount` DECIMAL(10,2) NOT NULL COMMENT '退款金额',
  `refund_reason` VARCHAR(500) DEFAULT NULL COMMENT '退款原因',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '退款状态：1-待审核，2-已通过，3-已拒绝，4-已完成',
  `audit_user_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '审核人ID（管理员）',
  `audit_reason` VARCHAR(500) DEFAULT NULL COMMENT '审核意见',
  `audit_at` DATETIME DEFAULT NULL COMMENT '审核时间',
  `refund_time` DATETIME DEFAULT NULL COMMENT '退款时间',
  `refund_account` VARCHAR(100) DEFAULT NULL COMMENT '退款账号',
  `extra` JSON DEFAULT NULL COMMENT '扩展信息',
  `version` INT UNSIGNED NOT NULL DEFAULT 1 COMMENT '版本号',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted_at` DATETIME DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_refund_no` (`refund_no`),
  KEY `idx_payment_id` (`payment_id`),
  KEY `idx_appointment_id` (`appointment_id`),
  KEY `idx_student_id` (`student_id`),
  KEY `idx_teacher_id` (`teacher_id`),
  KEY `idx_status` (`status`),
  KEY `idx_created_at` (`created_at`),
  KEY `idx_deleted_at` (`deleted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='退款记录表';

-- ============================================
-- 13. 评价表
-- ============================================
CREATE TABLE `reviews` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `appointment_id` BIGINT UNSIGNED NOT NULL COMMENT '预约ID',
  `student_id` BIGINT UNSIGNED NOT NULL COMMENT '学生用户ID',
  `teacher_id` BIGINT UNSIGNED NOT NULL COMMENT '教师用户ID',
  `rating` TINYINT NOT NULL COMMENT '评分：1-5分',
  `content` TEXT DEFAULT NULL COMMENT '评价内容',
  `images` JSON DEFAULT NULL COMMENT '评价图片（JSON数组）',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-正常，2-已隐藏',
  `extra` JSON DEFAULT NULL COMMENT '扩展信息',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted_at` DATETIME DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_appointment_id` (`appointment_id`),
  KEY `idx_teacher_id` (`teacher_id`),
  KEY `idx_student_id` (`student_id`),
  KEY `idx_rating` (`rating`),
  KEY `idx_created_at` (`created_at`),
  KEY `idx_deleted_at` (`deleted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评价表';

-- ============================================
-- 14. 聊天关系表
-- ============================================
CREATE TABLE `chat_relationships` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user1_id` BIGINT UNSIGNED NOT NULL COMMENT '用户1 ID（较小的ID）',
  `user2_id` BIGINT UNSIGNED NOT NULL COMMENT '用户2 ID（较大的ID）',
  `appointment_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '关联预约ID（建立聊天关系的预约）',
  `user1_unread_count` INT DEFAULT 0 COMMENT '用户1未读消息数',
  `user2_unread_count` INT DEFAULT 0 COMMENT '用户2未读消息数',
  `user1_top` TINYINT DEFAULT 0 COMMENT '用户1是否置顶：0-否，1-是',
  `user2_top` TINYINT DEFAULT 0 COMMENT '用户2是否置顶：0-否，1-是',
  `last_message_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '最后一条消息ID',
  `last_message_time` DATETIME DEFAULT NULL COMMENT '最后消息时间',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-正常，2-已删除',
  `extra` JSON DEFAULT NULL COMMENT '扩展信息',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted_at` DATETIME DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_pair` (`user1_id`, `user2_id`, `deleted_at`),
  KEY `idx_user1_id` (`user1_id`),
  KEY `idx_user2_id` (`user2_id`),
  KEY `idx_appointment_id` (`appointment_id`),
  KEY `idx_last_message_time` (`last_message_time`),
  KEY `idx_deleted_at` (`deleted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天关系表';

-- ============================================
-- 15. 聊天消息表
-- ============================================
CREATE TABLE `chat_messages` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `relationship_id` BIGINT UNSIGNED NOT NULL COMMENT '聊天关系ID',
  `sender_id` BIGINT UNSIGNED NOT NULL COMMENT '发送者ID',
  `receiver_id` BIGINT UNSIGNED NOT NULL COMMENT '接收者ID',
  `message_type` TINYINT NOT NULL COMMENT '消息类型：1-文本，2-文件，3-语音，4-图片',
  `content` TEXT DEFAULT NULL COMMENT '消息内容（文本消息或JSON格式）',
  `file_url` VARCHAR(500) DEFAULT NULL COMMENT '文件URL（文件/语音/图片）',
  `file_name` VARCHAR(255) DEFAULT NULL COMMENT '文件名',
  `file_size` BIGINT DEFAULT NULL COMMENT '文件大小（字节）',
  `duration` INT DEFAULT NULL COMMENT '语音时长（秒）',
  `image_width` INT DEFAULT NULL COMMENT '图片宽度',
  `image_height` INT DEFAULT NULL COMMENT '图片高度',
  `thumbnail_url` VARCHAR(500) DEFAULT NULL COMMENT '缩略图URL',
  `is_read` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已读：0-未读，1-已读',
  `read_at` DATETIME DEFAULT NULL COMMENT '已读时间',
  `is_recalled` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已撤回：0-否，1-是',
  `recalled_at` DATETIME DEFAULT NULL COMMENT '撤回时间',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '消息状态：1-正常，2-已删除',
  `extra` JSON DEFAULT NULL COMMENT '扩展信息',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted_at` DATETIME DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_relationship_id` (`relationship_id`),
  KEY `idx_sender_id` (`sender_id`),
  KEY `idx_receiver_id` (`receiver_id`),
  KEY `idx_created_at` (`created_at`),
  KEY `idx_is_read` (`is_read`),
  KEY `idx_deleted_at` (`deleted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天消息表';

-- ============================================
-- 16. 通话记录表
-- ============================================
CREATE TABLE `call_records` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `call_no` VARCHAR(50) NOT NULL COMMENT '通话编号（唯一）',
  `caller_id` BIGINT UNSIGNED NOT NULL COMMENT '发起者ID',
  `receiver_id` BIGINT UNSIGNED NOT NULL COMMENT '接收者ID',
  `call_type` TINYINT NOT NULL COMMENT '通话类型：1-语音通话，2-视频通话',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '通话状态：1-呼叫中，2-通话中，3-已结束，4-已拒绝，5-未接听',
  `start_time` DATETIME DEFAULT NULL COMMENT '通话开始时间',
  `end_time` DATETIME DEFAULT NULL COMMENT '通话结束时间',
  `duration` INT DEFAULT 0 COMMENT '通话时长（秒）',
  `reject_reason` VARCHAR(200) DEFAULT NULL COMMENT '拒绝原因',
  `extra` JSON DEFAULT NULL COMMENT '扩展信息',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发起时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted_at` DATETIME DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_call_no` (`call_no`),
  KEY `idx_caller_id` (`caller_id`),
  KEY `idx_receiver_id` (`receiver_id`),
  KEY `idx_status` (`status`),
  KEY `idx_created_at` (`created_at`),
  KEY `idx_deleted_at` (`deleted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通话记录表';

-- ============================================
-- 17. 公告表
-- ============================================
CREATE TABLE `announcements` (
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

-- ============================================
-- 18. 系统配置表
-- ============================================
CREATE TABLE `system_configs` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `config_key` VARCHAR(100) NOT NULL COMMENT '配置键（唯一）',
  `config_value` TEXT DEFAULT NULL COMMENT '配置值',
  `config_type` VARCHAR(50) DEFAULT 'string' COMMENT '配置类型：string、json、number等',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '配置说明',
  `group_name` VARCHAR(50) DEFAULT NULL COMMENT '配置分组',
  `sort_order` INT DEFAULT 0 COMMENT '排序顺序',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-启用，2-禁用',
  `extra` JSON DEFAULT NULL COMMENT '扩展信息',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted_at` DATETIME DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_key` (`config_key`),
  KEY `idx_group_name` (`group_name`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

-- ============================================
-- 19. 操作日志表（新增）
-- ============================================
CREATE TABLE `operation_logs` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '操作用户ID',
  `username` VARCHAR(50) DEFAULT NULL COMMENT '操作用户名',
  `operation_type` VARCHAR(50) NOT NULL COMMENT '操作类型：login、logout、create、update、delete等',
  `module` VARCHAR(50) DEFAULT NULL COMMENT '操作模块：user、appointment、payment等',
  `content` TEXT DEFAULT NULL COMMENT '操作内容描述',
  `request_method` VARCHAR(10) DEFAULT NULL COMMENT '请求方法：GET、POST等',
  `request_url` VARCHAR(500) DEFAULT NULL COMMENT '请求URL',
  `request_params` JSON DEFAULT NULL COMMENT '请求参数（JSON格式）',
  `ip_address` VARCHAR(50) DEFAULT NULL COMMENT 'IP地址',
  `user_agent` VARCHAR(500) DEFAULT NULL COMMENT '用户代理',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '操作状态：1-成功，2-失败',
  `error_message` TEXT DEFAULT NULL COMMENT '错误信息',
  `execution_time` INT DEFAULT NULL COMMENT '执行时间（毫秒）',
  `extra` JSON DEFAULT NULL COMMENT '扩展信息',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_operation_type` (`operation_type`),
  KEY `idx_module` (`module`),
  KEY `idx_status` (`status`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- ============================================
-- 20. 通知表（新增）
-- ============================================
CREATE TABLE `notifications` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '接收用户ID',
  `type` TINYINT NOT NULL DEFAULT 1 COMMENT '通知类型：1-系统通知，2-预约通知，3-支付通知，4-评价通知，5-聊天通知',
  `title` VARCHAR(200) NOT NULL COMMENT '通知标题',
  `content` TEXT DEFAULT NULL COMMENT '通知内容',
  `related_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '关联ID（如预约ID、支付ID等）',
  `related_type` VARCHAR(50) DEFAULT NULL COMMENT '关联类型：appointment、payment等',
  `is_read` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已读：0-未读，1-已读',
  `read_at` DATETIME DEFAULT NULL COMMENT '已读时间',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-正常，2-已删除',
  `extra` JSON DEFAULT NULL COMMENT '扩展信息',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted_at` DATETIME DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_type` (`type`),
  KEY `idx_is_read` (`is_read`),
  KEY `idx_related` (`related_type`, `related_id`),
  KEY `idx_created_at` (`created_at`),
  KEY `idx_deleted_at` (`deleted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知表';

-- ============================================
-- 21. 文件上传记录表（新增）
-- ============================================
CREATE TABLE `file_uploads` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '上传用户ID',
  `file_name` VARCHAR(255) NOT NULL COMMENT '原始文件名',
  `file_path` VARCHAR(500) NOT NULL COMMENT '文件存储路径',
  `file_url` VARCHAR(500) NOT NULL COMMENT '文件访问URL',
  `file_type` VARCHAR(50) DEFAULT NULL COMMENT '文件类型：image、document、video、audio等',
  `file_size` BIGINT NOT NULL COMMENT '文件大小（字节）',
  `mime_type` VARCHAR(100) DEFAULT NULL COMMENT 'MIME类型',
  `file_hash` VARCHAR(64) DEFAULT NULL COMMENT '文件哈希值（用于去重）',
  `upload_source` VARCHAR(50) DEFAULT NULL COMMENT '上传来源：avatar、certificate、message等',
  `related_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '关联ID',
  `related_type` VARCHAR(50) DEFAULT NULL COMMENT '关联类型',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-正常，2-已删除',
  `extra` JSON DEFAULT NULL COMMENT '扩展信息',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted_at` DATETIME DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_file_type` (`file_type`),
  KEY `idx_upload_source` (`upload_source`),
  KEY `idx_related` (`related_type`, `related_id`),
  KEY `idx_file_hash` (`file_hash`),
  KEY `idx_created_at` (`created_at`),
  KEY `idx_deleted_at` (`deleted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件上传记录表';

-- ============================================
-- 初始数据插入
-- ============================================

-- 教学阶段初始数据
INSERT INTO `teaching_stages` (`code`, `name`, `sort_order`) VALUES
('PRIMARY', '小学', 1),
('MIDDLE', '初中', 2),
('HIGH', '高中', 3);

-- 科目初始数据
INSERT INTO `subjects` (`code`, `name`, `category`, `sort_order`) VALUES
('MATH', '数学', '理科', 1),
('CHINESE', '语文', '文科', 2),
('ENGLISH', '英语', '外语', 3),
('PHYSICS', '物理', '理科', 4),
('CHEMISTRY', '化学', '理科', 5),
('BIOLOGY', '生物', '理科', 6),
('HISTORY', '历史', '文科', 7),
('GEOGRAPHY', '地理', '文科', 8),
('POLITICS', '政治', '文科', 9);

-- 系统配置初始数据
INSERT INTO `system_configs` (`config_key`, `config_value`, `config_type`, `description`, `group_name`, `sort_order`) VALUES
('platform_name', '线上家教平台', 'string', '平台名称', 'basic', 1),
('contact_phone', '400-xxx-xxxx', 'string', '客服电话', 'basic', 2),
('service_hours', '09:00-18:00', 'string', '服务时间', 'basic', 3),
('max_voice_duration', '300', 'number', '语音消息最大时长（秒）', 'chat', 1),
('max_image_size', '10485760', 'number', '图片最大大小（字节）', 'chat', 2),
('max_file_size', '52428800', 'number', '文件最大大小（字节）', 'chat', 3),
('message_recall_time', '120', 'number', '消息撤回时间限制（秒）', 'chat', 4),
('min_appointment_hours', '1', 'number', '最小预约时长（小时）', 'appointment', 1),
('max_appointment_hours', '4', 'number', '最大预约时长（小时）', 'appointment', 2),
('appointment_advance_days', '7', 'number', '可预约提前天数', 'appointment', 3);

-- ============================================
-- 测试数据插入
-- ============================================

-- 1. 用户表测试数据
-- 密码都是: 123456 (BCrypt加密后的值，实际使用时需要根据实际情况生成)
INSERT INTO `users` (`username`, `password`, `nickname`, `avatar`, `phone`, `email`, `role`, `status`, `online_status`, `last_login_at`, `last_login_ip`) VALUES
-- 管理员
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iw8qJ5pO', '系统管理员', 'https://example.com/avatars/admin.jpg', '13800000001', 'admin@example.com', 3, 1, 0, '2024-01-20 10:00:00', '192.168.1.100'),
-- 教师用户
('teacher001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iw8qJ5pO', '张老师', 'https://example.com/avatars/teacher001.jpg', '13800001001', 'teacher001@example.com', 2, 1, 1, '2024-01-20 09:30:00', '192.168.1.101'),
('teacher002', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iw8qJ5pO', '李老师', 'https://example.com/avatars/teacher002.jpg', '13800001002', 'teacher002@example.com', 2, 1, 0, '2024-01-19 15:20:00', '192.168.1.102'),
('teacher003', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iw8qJ5pO', '王老师', 'https://example.com/avatars/teacher003.jpg', '13800001003', 'teacher003@example.com', 2, 1, 1, '2024-01-20 08:00:00', '192.168.1.103'),
('teacher004', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iw8qJ5pO', '刘老师', 'https://example.com/avatars/teacher004.jpg', '13800001004', 'teacher004@example.com', 2, 1, 0, '2024-01-18 20:10:00', '192.168.1.104'),
('teacher005', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iw8qJ5pO', '陈老师', 'https://example.com/avatars/teacher005.jpg', '13800001005', 'teacher005@example.com', 2, 1, 1, '2024-01-20 11:00:00', '192.168.1.105'),
-- 学生用户
('student001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iw8qJ5pO', '小明', 'https://example.com/avatars/student001.jpg', '13800002001', 'student001@example.com', 1, 1, 1, '2024-01-20 10:15:00', '192.168.1.201'),
('student002', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iw8qJ5pO', '小红', 'https://example.com/avatars/student002.jpg', '13800002002', 'student002@example.com', 1, 1, 0, '2024-01-19 14:30:00', '192.168.1.202'),
('student003', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iw8qJ5pO', '小刚', 'https://example.com/avatars/student003.jpg', '13800002003', 'student003@example.com', 1, 1, 1, '2024-01-20 09:45:00', '192.168.1.203'),
('student004', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iw8qJ5pO', '小丽', 'https://example.com/avatars/student004.jpg', '13800002004', 'student004@example.com', 1, 1, 0, '2024-01-18 16:20:00', '192.168.1.204'),
('student005', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iw8qJ5pO', '小强', 'https://example.com/avatars/student005.jpg', '13800002005', 'student005@example.com', 1, 1, 1, '2024-01-20 08:30:00', '192.168.1.205'),
('student006', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iw8qJ5pO', '小美', 'https://example.com/avatars/student006.jpg', '13800002006', 'student006@example.com', 1, 1, 0, '2024-01-17 19:00:00', '192.168.1.206');

-- 2. 教师扩展信息表
INSERT INTO `teacher_profiles` (`user_id`, `real_name`, `introduction`, `teaching_years`, `teaching_style`, `rating`, `rating_count`, `bank_account`, `bank_name`, `account_holder`) VALUES
(2, '张明', '毕业于北京师范大学数学系，拥有10年教学经验，擅长初中数学教学，注重培养学生的逻辑思维能力。', 10, '耐心细致，善于启发式教学，注重培养学生的独立思考能力', 4.8, 25, '6222021234567890123', '中国工商银行', '张明'),
(3, '李华', '清华大学物理系毕业，从事高中物理教学8年，曾获得优秀教师称号。', 8, '严谨认真，注重理论与实践相结合，善于用生活实例解释物理现象', 4.6, 18, '6222089876543210987', '中国建设银行', '李华'),
(4, '王芳', '北京外国语大学英语专业，英语专业八级，有丰富的英语教学经验。', 6, '活泼生动，注重口语训练，善于激发学生学习兴趣', 4.9, 32, '6222123456789012345', '中国银行', '王芳'),
(5, '刘强', '华东师范大学化学系毕业，从事化学教学12年，对高考化学有深入研究。', 12, '系统全面，注重知识体系构建，善于总结归纳', 4.7, 28, '6222165432109876543', '中国农业银行', '刘强'),
(6, '陈静', '复旦大学中文系毕业，语文教学经验丰富，擅长作文指导。', 9, '温文尔雅，注重文学素养培养，善于引导学生感受文字之美', 4.5, 15, '6222209876543210123', '招商银行', '陈静');

-- 3. 学生扩展信息表
INSERT INTO `student_profiles` (`user_id`, `real_name`, `grade`, `school_name`, `learning_goals`, `weak_subjects`, `parent_name`, `parent_phone`) VALUES
(7, '张小明', '初一', '第一中学', '提高数学成绩，培养逻辑思维能力', '[1, 2]', '张父', '13800003001'),
(8, '李小红', '初二', '第二中学', '加强英语口语练习，提高英语综合能力', '[3]', '李母', '13800003002'),
(9, '王小刚', '高一', '实验中学', '提高物理成绩，为高考做准备', '[4]', '王父', '13800003003'),
(10, '刘小丽', '高二', '重点中学', '提高化学成绩，加强实验能力', '[5]', '刘母', '13800003004'),
(11, '陈小强', '初三', '第三中学', '全面提高各科成绩，冲刺中考', '[1, 3]', '陈父', '13800003005'),
(12, '赵小美', '高三', '示范中学', '提高语文作文水平，加强文学素养', '[2]', '赵母', '13800003006');

-- 4. 用户第三方登录关联表（部分用户绑定微信/QQ）
INSERT INTO `user_oauth` (`user_id`, `oauth_type`, `openid`, `unionid`, `nickname`, `avatar`) VALUES
(7, 1, 'wx_openid_student001', 'wx_unionid_001', '小明', 'https://example.com/wechat/student001.jpg'),
(8, 2, 'qq_openid_student002', NULL, '小红', 'https://example.com/qq/student002.jpg'),
(2, 1, 'wx_openid_teacher001', 'wx_unionid_002', '张老师', 'https://example.com/wechat/teacher001.jpg'),
(3, 2, 'qq_openid_teacher002', NULL, '李老师', 'https://example.com/qq/teacher002.jpg');

-- 5. 教师学历认证表
INSERT INTO `teacher_certifications` (`user_id`, `certificate_type`, `certificate_image`, `school_name`, `major`, `degree`, `graduation_year`, `status`, `audit_user_id`, `audit_reason`, `audit_at`) VALUES
(2, 1, 'https://example.com/certificates/teacher001_degree.jpg', '北京师范大学', '数学与应用数学', '本科', 2010, 2, 1, '审核通过', '2024-01-01 10:00:00'),
(2, 2, 'https://example.com/certificates/teacher001_teaching.jpg', NULL, NULL, '高级中学教师资格证', 2011, 2, 1, '审核通过', '2024-01-01 10:05:00'),
(3, 1, 'https://example.com/certificates/teacher002_degree.jpg', '清华大学', '物理学', '硕士', 2012, 2, 1, '审核通过', '2024-01-02 11:00:00'),
(4, 1, 'https://example.com/certificates/teacher003_degree.jpg', '北京外国语大学', '英语', '本科', 2014, 2, 1, '审核通过', '2024-01-03 09:00:00'),
(5, 1, 'https://example.com/certificates/teacher004_degree.jpg', '华东师范大学', '化学', '硕士', 2008, 2, 1, '审核通过', '2024-01-04 14:00:00'),
(6, 1, 'https://example.com/certificates/teacher005_degree.jpg', '复旦大学', '汉语言文学', '本科', 2011, 1, NULL, NULL, NULL);

-- 6. 教师教学信息表
INSERT INTO `teacher_teachings` (`user_id`, `stage_id`, `subject_id`, `price_per_hour`, `status`) VALUES
-- 张老师：初中数学
(2, 2, 1, 150.00, 1),
-- 李老师：高中物理
(3, 3, 4, 200.00, 1),
-- 王老师：初中英语、高中英语
(4, 2, 3, 180.00, 1),
(4, 3, 3, 200.00, 1),
-- 刘老师：高中化学
(5, 3, 5, 190.00, 1),
-- 陈老师：初中语文、高中语文
(6, 2, 2, 160.00, 1),
(6, 3, 2, 180.00, 1);

-- 7. 教师空闲时间表
INSERT INTO `teacher_schedules` (`user_id`, `weekday`, `start_time`, `end_time`, `schedule_type`, `status`) VALUES
-- 张老师：周一、周三、周五 晚上
(2, 1, '19:00:00', '21:00:00', 1, 1),
(2, 3, '19:00:00', '21:00:00', 1, 1),
(2, 5, '19:00:00', '21:00:00', 1, 1),
-- 李老师：周二、周四、周六 晚上
(3, 2, '19:30:00', '21:30:00', 1, 1),
(3, 4, '19:30:00', '21:30:00', 1, 1),
(3, 6, '14:00:00', '18:00:00', 1, 1),
-- 王老师：周一至周五 晚上，周末全天
(4, 1, '18:00:00', '20:00:00', 1, 1),
(4, 2, '18:00:00', '20:00:00', 1, 1),
(4, 3, '18:00:00', '20:00:00', 1, 1),
(4, 4, '18:00:00', '20:00:00', 1, 1),
(4, 5, '18:00:00', '20:00:00', 1, 1),
(4, 6, '09:00:00', '18:00:00', 1, 1),
(4, 7, '09:00:00', '18:00:00', 1, 1),
-- 刘老师：周二、周四、周日 晚上
(5, 2, '20:00:00', '22:00:00', 1, 1),
(5, 4, '20:00:00', '22:00:00', 1, 1),
(5, 7, '19:00:00', '21:00:00', 1, 1),
-- 陈老师：周三、周五、周六 下午
(6, 3, '14:00:00', '17:00:00', 1, 1),
(6, 5, '14:00:00', '17:00:00', 1, 1),
(6, 6, '09:00:00', '12:00:00', 1, 1);

-- 8. 预约表
INSERT INTO `appointments` (`order_no`, `student_id`, `teacher_id`, `stage_id`, `subject_id`, `appointment_date`, `start_time`, `end_time`, `duration`, `price_per_hour`, `total_amount`, `student_name`, `student_grade`, `student_phone`, `remark`, `status`, `dingtalk_url`, `confirmed_at`, `completed_at`) VALUES
('ORD20240120001', 7, 2, 2, 1, '2024-01-25', '19:00:00', '21:00:00', 120, 150.00, 300.00, '张小明', '初一', '13800002001', '希望重点讲解一元一次方程', 2, 'https://meeting.dingtalk.com/j/xxx001', '2024-01-20 11:00:00', NULL),
('ORD20240120002', 8, 4, 2, 3, '2024-01-26', '18:00:00', '20:00:00', 120, 180.00, 360.00, '李小红', '初二', '13800002002', '需要加强英语口语练习', 2, 'https://meeting.dingtalk.com/j/xxx002', '2024-01-20 12:00:00', NULL),
('ORD20240120003', 9, 3, 3, 4, '2024-01-23', '19:30:00', '21:30:00', 120, 200.00, 400.00, '王小刚', '高一', '13800002003', '讲解力学部分', 3, 'https://meeting.dingtalk.com/j/xxx003', '2024-01-19 10:00:00', '2024-01-23 21:30:00'),
('ORD20240120004', 10, 5, 3, 5, '2024-01-24', '20:00:00', '22:00:00', 120, 190.00, 380.00, '刘小丽', '高二', '13800002004', '有机化学复习', 3, 'https://meeting.dingtalk.com/j/xxx004', '2024-01-18 15:00:00', '2024-01-24 22:00:00'),
('ORD20240120005', 11, 2, 2, 1, '2024-01-22', '19:00:00', '21:00:00', 120, 150.00, 300.00, '陈小强', '初三', '13800002005', '中考数学冲刺', 1, NULL, NULL, NULL),
('ORD20240120006', 12, 6, 3, 2, '2024-01-27', '14:00:00', '17:00:00', 180, 180.00, 540.00, '赵小美', '高三', '13800002006', '高考作文指导', 2, 'https://meeting.dingtalk.com/j/xxx006', '2024-01-20 13:00:00', NULL),
('ORD20240115001', 7, 2, 2, 1, '2024-01-15', '19:00:00', '21:00:00', 120, 150.00, 300.00, '张小明', '初一', '13800002001', '代数基础', 3, 'https://meeting.dingtalk.com/j/xxx007', '2024-01-10 10:00:00', '2024-01-15 21:00:00'),
('ORD20240115002', 8, 4, 2, 3, '2024-01-16', '18:00:00', '20:00:00', 120, 180.00, 360.00, '李小红', '初二', '13800002002', '英语语法', 3, 'https://meeting.dingtalk.com/j/xxx008', '2024-01-11 11:00:00', '2024-01-16 20:00:00');

-- 9. 支付记录表
INSERT INTO `payments` (`payment_no`, `appointment_id`, `student_id`, `teacher_id`, `amount`, `payment_method`, `transfer_image`, `transfer_time`, `transfer_account`, `status`, `confirmed_at`, `confirmed_by`) VALUES
('PAY20240115001', 7, 7, 2, 300.00, 1, 'https://example.com/proof/pay001.jpg', '2024-01-16 10:00:00', '6222021234567890123', 3, '2024-01-16 15:00:00', 2),
('PAY20240115002', 8, 8, 4, 360.00, 1, 'https://example.com/proof/pay002.jpg', '2024-01-17 09:30:00', '6222123456789012345', 3, '2024-01-17 14:00:00', 4),
('PAY20240123001', 3, 9, 3, 400.00, 1, 'https://example.com/proof/pay003.jpg', '2024-01-24 08:00:00', '6222089876543210987', 2, NULL, NULL),
('PAY20240124001', 4, 10, 5, 380.00, 1, 'https://example.com/proof/pay004.jpg', '2024-01-25 10:00:00', '6222165432109876543', 3, '2024-01-25 16:00:00', 5);

-- 10. 退款记录表
INSERT INTO `refunds` (`refund_no`, `payment_id`, `appointment_id`, `student_id`, `teacher_id`, `refund_amount`, `refund_reason`, `status`, `audit_user_id`, `audit_reason`, `audit_at`, `refund_time`, `refund_account`) VALUES
('REF20240120001', 1, 7, 7, 2, 300.00, '课程时间冲突，无法参加', 2, 1, '审核通过，同意退款', '2024-01-20 10:00:00', '2024-01-21 10:00:00', '6222021234567890123'),
('REF20240120002', 2, 8, 8, 4, 360.00, '教师临时有事，课程取消', 1, NULL, NULL, NULL, NULL, NULL);

-- 11. 评价表
INSERT INTO `reviews` (`appointment_id`, `student_id`, `teacher_id`, `rating`, `content`, `images`, `status`) VALUES
(7, 7, 2, 5, '张老师讲解非常清晰，很有耐心，孩子很喜欢。数学成绩有明显提升！', '[]', 1),
(8, 8, 4, 5, '王老师英语发音标准，教学方法很好，孩子对英语学习更有兴趣了。', '[]', 1),
(3, 9, 3, 4, '李老师物理知识扎实，讲解详细，但希望可以多一些互动。', '[]', 1),
(4, 10, 5, 5, '刘老师化学教学经验丰富，实验讲解很生动，受益匪浅！', '["https://example.com/review/review001.jpg"]', 1);

-- 12. 聊天关系表
INSERT INTO `chat_relationships` (`user1_id`, `user2_id`, `appointment_id`, `user1_unread_count`, `user2_unread_count`, `user1_top`, `user2_top`, `last_message_time`) VALUES
(7, 2, 1, 0, 2, 0, 1, '2024-01-20 14:30:00'),
(8, 4, 2, 0, 0, 0, 0, '2024-01-19 16:20:00'),
(9, 3, 3, 1, 0, 0, 0, '2024-01-20 10:15:00'),
(10, 5, 4, 0, 0, 0, 0, '2024-01-18 20:00:00'),
(11, 2, 5, 0, 1, 0, 0, '2024-01-20 13:45:00'),
(12, 6, 6, 0, 0, 0, 0, '2024-01-20 15:00:00');

-- 13. 聊天消息表
INSERT INTO `chat_messages` (`relationship_id`, `sender_id`, `receiver_id`, `message_type`, `content`, `is_read`, `read_at`, `created_at`) VALUES
(1, 2, 7, 1, '你好，我是张老师，很高兴为你上课！', 1, '2024-01-20 14:00:00', '2024-01-20 14:00:00'),
(1, 7, 2, 1, '张老师好，我有些数学问题想请教', 1, '2024-01-20 14:05:00', '2024-01-20 14:05:00'),
(1, 2, 7, 1, '没问题，我们可以先看看你的作业', 0, NULL, '2024-01-20 14:10:00'),
(1, 2, 7, 1, '这是本周的学习计划，请查收', 0, NULL, '2024-01-20 14:30:00'),
(2, 4, 8, 1, '小红你好，明天的课程准备好了吗？', 1, '2024-01-19 16:00:00', '2024-01-19 16:00:00'),
(2, 8, 4, 1, '准备好了，王老师', 1, '2024-01-19 16:20:00', '2024-01-19 16:20:00'),
(3, 3, 9, 1, '小刚，物理作业完成得怎么样？', 0, NULL, '2024-01-20 10:15:00'),
(5, 2, 11, 1, '小强，关于中考数学有什么问题吗？', 0, NULL, '2024-01-20 13:45:00');

-- 14. 通话记录表
INSERT INTO `call_records` (`call_no`, `caller_id`, `receiver_id`, `call_type`, `status`, `start_time`, `end_time`, `duration`) VALUES
('CALL20240120001', 7, 2, 1, 3, '2024-01-20 15:00:00', '2024-01-20 15:15:00', 900),
('CALL20240119001', 8, 4, 2, 3, '2024-01-19 17:00:00', '2024-01-19 17:30:00', 1800),
('CALL20240120002', 2, 7, 1, 4, '2024-01-20 16:00:00', NULL, 0);

-- 15. 公告表
INSERT INTO `announcements` (`title`, `content`, `type`, `priority`, `status`, `publish_time`, `view_count`, `created_by`) VALUES
('平台升级通知', '为了提供更好的服务，平台将于2024年2月1日进行系统升级，升级期间可能无法正常使用，请提前做好准备。', 1, 2, 1, '2024-01-15 10:00:00', 1250, 1),
('春节假期安排', '春节期间（2024年2月10日-2月17日）平台正常运营，教师和学生们可以正常预约课程。祝大家春节快乐！', 2, 1, 1, '2024-01-18 14:00:00', 890, 1),
('新功能上线：在线评价', '为了更好地了解教学质量，平台新增在线评价功能，课程完成后可以对教师进行评价，感谢大家的支持！', 1, 1, 1, '2024-01-20 09:00:00', 560, 1),
('系统维护通知', '系统将于2024年1月25日 00:00-02:00 进行维护，维护期间暂停服务，给您带来不便敬请谅解。', 3, 3, 1, '2024-01-20 16:00:00', 320, 1);

-- 16. 操作日志表
INSERT INTO `operation_logs` (`user_id`, `username`, `operation_type`, `module`, `content`, `request_method`, `request_url`, `ip_address`, `status`, `created_at`) VALUES
(7, 'student001', 'login', 'auth', '用户登录', 'POST', '/api/auth/login', '192.168.1.201', 1, '2024-01-20 10:15:00'),
(2, 'teacher001', 'login', 'auth', '用户登录', 'POST', '/api/auth/login', '192.168.1.101', 1, '2024-01-20 09:30:00'),
(7, 'student001', 'create', 'appointment', '创建预约：ORD20240120001', 'POST', '/api/appointment', '192.168.1.201', 1, '2024-01-20 10:30:00'),
(2, 'teacher001', 'update', 'appointment', '确认预约：ORD20240120001', 'PUT', '/api/appointment/1/confirm', '192.168.1.101', 1, '2024-01-20 11:00:00'),
(7, 'student001', 'create', 'payment', '上传支付凭证：PAY20240115001', 'POST', '/api/payment/1/proof', '192.168.1.201', 1, '2024-01-16 10:00:00'),
(2, 'teacher001', 'update', 'payment', '确认收款：PAY20240115001', 'PUT', '/api/payment/1/confirm', '192.168.1.101', 1, '2024-01-16 15:00:00'),
(7, 'student001', 'create', 'review', '创建评价：预约ID 7', 'POST', '/api/review', '192.168.1.201', 1, '2024-01-16 20:00:00'),
(1, 'admin', 'update', 'refund', '审核退款：REF20240120001', 'PUT', '/api/refund/1/audit', '192.168.1.100', 1, '2024-01-20 10:00:00');

-- 17. 通知表
INSERT INTO `notifications` (`user_id`, `type`, `title`, `content`, `related_id`, `related_type`, `is_read`, `read_at`, `created_at`) VALUES
(7, 2, '预约已确认', '您的预约（ORD20240120001）已被教师确认，请按时参加课程', 1, 'appointment', 1, '2024-01-20 11:05:00', '2024-01-20 11:00:00'),
(7, 3, '支付提醒', '您的课程已完成，请及时支付费用', 1, 'payment', 1, '2024-01-15 21:05:00', '2024-01-15 21:00:00'),
(7, 3, '支付已确认', '您的支付（PAY20240115001）已被教师确认', 1, 'payment', 1, '2024-01-16 15:05:00', '2024-01-16 15:00:00'),
(7, 4, '评价提醒', '您的课程已完成，可以对教师进行评价', 7, 'appointment', 0, NULL, '2024-01-15 21:00:00'),
(8, 2, '预约已确认', '您的预约（ORD20240120002）已被教师确认，请按时参加课程', 2, 'appointment', 1, '2024-01-20 12:05:00', '2024-01-20 12:00:00'),
(9, 3, '支付提醒', '您的课程已完成，请及时支付费用', 3, 'payment', 0, NULL, '2024-01-23 21:30:00'),
(10, 3, '支付已确认', '您的支付（PAY20240124001）已被教师确认', 4, 'payment', 1, '2024-01-25 16:05:00', '2024-01-25 16:00:00'),
(7, 7, '退款审核通过', '您的退款申请（REF20240120001）已通过审核，退款将尽快处理', 1, 'refund', 1, '2024-01-20 10:05:00', '2024-01-20 10:00:00'),
(2, 4, '收到新评价', '学生 张小明 对您进行了评价，请查看', 1, 'review', 1, '2024-01-16 20:05:00', '2024-01-16 20:00:00'),
(4, 4, '收到新评价', '学生 李小红 对您进行了评价，请查看', 2, 'review', 0, NULL, '2024-01-16 21:00:00'),
(2, 5, '新消息', '您收到一条新消息', 1, 'chat', 0, NULL, '2024-01-20 14:30:00'),
(7, 5, '新消息', '您收到一条新消息', 1, 'chat', 1, '2024-01-20 14:05:00', '2024-01-20 14:00:00');

-- 18. 文件上传记录表
INSERT INTO `file_uploads` (`user_id`, `file_name`, `file_path`, `file_url`, `file_type`, `file_size`, `mime_type`, `upload_source`, `related_id`, `related_type`) VALUES
(2, 'avatar.jpg', '/uploads/avatars/2024/01/teacher001_avatar.jpg', 'https://example.com/avatars/teacher001.jpg', 'image', 102400, 'image/jpeg', 'avatar', 2, 'user'),
(7, 'avatar.png', '/uploads/avatars/2024/01/student001_avatar.png', 'https://example.com/avatars/student001.jpg', 'image', 98304, 'image/png', 'avatar', 7, 'user'),
(2, 'degree_certificate.jpg', '/uploads/certificates/2024/01/teacher001_degree.jpg', 'https://example.com/certificates/teacher001_degree.jpg', 'image', 512000, 'image/jpeg', 'certificate', 1, 'certification'),
(7, 'payment_proof.jpg', '/uploads/payments/2024/01/pay001.jpg', 'https://example.com/proof/pay001.jpg', 'image', 256000, 'image/jpeg', 'payment', 1, 'payment'),
(10, 'review_image.jpg', '/uploads/reviews/2024/01/review001.jpg', 'https://example.com/review/review001.jpg', 'image', 204800, 'image/jpeg', 'review', 4, 'review'),
(2, 'chat_image.png', '/uploads/chat/2024/01/chat001.png', 'https://example.com/chat/chat001.png', 'image', 128000, 'image/png', 'message', 1, 'chat_message');

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================
-- 数据库结构创建完成
-- ============================================

