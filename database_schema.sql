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

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================
-- 数据库结构创建完成
-- ============================================

