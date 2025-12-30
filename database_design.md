# 线上家教接单系统 - 数据库设计文档

## 一、设计原则

1. **不使用物理外键**：所有关联关系通过逻辑外键（user_id等）维护
2. **软删除机制**：使用 `deleted_at` 字段标记删除，不物理删除数据
3. **字典表设计**：可配置数据（教学阶段、科目等）使用字典表，便于扩展
4. **预留扩展字段**：使用 `extra` JSON 字段存储扩展信息
5. **统一时间戳**：所有表统一使用 `created_at`、`updated_at`、`deleted_at`
6. **版本控制**：关键业务表使用 `version` 字段实现乐观锁
7. **状态枚举**：使用整型状态码，便于扩展和查询

---

## 二、核心表设计

### 2.1 用户表 (users)

**功能**：统一存储学生、教师、管理员信息

```sql
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
  KEY `idx_role_status` (`role`, `status`),
  KEY `idx_deleted_at` (`deleted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';
```

**设计说明**：
- 统一用户表，通过 `role` 字段区分角色
- `extra` 字段可存储角色特定信息（如学生年级、教师经验年限等）
- `online_status` 用于实时聊天功能
- 不使用物理外键，所有关联通过 `user_id` 逻辑关联
- 新增 `email` 字段索引，提升查询性能

---

### 2.2 教师扩展信息表 (teacher_profiles)

**功能**：存储教师特有的详细信息

```sql
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
```

**设计说明**：
- 与用户表一对一关系，通过 `user_id` 关联
- 存储教师特有的收款信息、评分等
- 评分和评价数单独存储，便于快速查询和排序

---

### 2.3 学生扩展信息表 (student_profiles) ⭐新增

**功能**：存储学生特有的详细信息

```sql
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
```

**设计说明**：
- 与用户表一对一关系，通过 `user_id` 关联
- 存储学生特有的学习信息、家长联系方式等
- 与教师扩展信息表对应，保持设计一致性

---

### 2.4 学历认证表 (teacher_certifications)

**功能**：存储教师学历认证信息

```sql
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
```

**设计说明**：
- 支持多种证书类型，便于扩展
- 审核流程完整记录（审核人、时间、原因）
- 一个教师可以有多个认证记录（历史记录保留）

---

### 2.5 教学阶段字典表 (teaching_stages)

**功能**：存储可配置的教学阶段（小学、初中、高中等）

```sql
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
```

**初始数据**：
```sql
INSERT INTO `teaching_stages` (`code`, `name`, `sort_order`) VALUES
('PRIMARY', '小学', 1),
('MIDDLE', '初中', 2),
('HIGH', '高中', 3);
```

---

### 2.6 科目字典表 (subjects)

**功能**：存储可配置的教学科目

```sql
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
```

**初始数据**：
```sql
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
```

---

### 2.7 教师教学信息表 (teacher_teachings)

**功能**：存储教师可教授的阶段、科目及价格（多对多关系）

```sql
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
```

**设计说明**：
- 一个教师可以教授多个阶段和科目
- 每个阶段-科目组合可以设置不同的价格
- 使用唯一索引防止重复数据

---

### 2.8 教师空闲时间表 (teacher_schedules)

**功能**：存储教师可授课时间段

```sql
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
```

**设计说明**：
- 支持固定周期（每周重复）和临时时间段两种模式
- 通过 `weekday` + `start_time` + `end_time` 定义时间段
- 便于查询和筛选可用时间段

---

### 2.9 预约表 (appointments)

**功能**：存储学生预约课程信息

```sql
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
```

**设计说明**：
- 订单号唯一，便于查询和追踪
- 冗余存储学生信息，避免用户信息变更影响历史记录
- 完整记录预约生命周期（确认、完成、取消）
- 存储价格快照，避免价格变更影响已预约订单

---

### 2.10 支付记录表 (payments) ⭐增强

**功能**：存储支付相关信息

```sql
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
```

**设计说明**：
- 与预约表一对一关系（一个预约对应一个支付记录）
- 完整记录转账凭证信息
- 支持教师拒绝收款并退回待支付状态
- 新增 `payment_method` 字段，支持多种支付方式扩展

---

### 2.11 退款记录表 (refunds) ⭐新增

**功能**：存储退款相关信息

```sql
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
```

**设计说明**：
- 与支付记录表关联，支持退款流程
- 完整记录退款审核流程（管理员审核）
- 支持退款状态追踪和查询

---

### 2.12 评价表 (reviews)

**功能**：存储学生对教师的评价

```sql
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
```

**设计说明**：
- 一个预约对应一个评价（可选）
- 支持多图评价
- 评价可隐藏但保留数据

---

### 2.13 聊天关系表 (chat_relationships)

**功能**：存储用户之间的聊天关系

```sql
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
```

**设计说明**：
- 使用 `user1_id` 和 `user2_id` 存储用户对，约定 `user1_id < user2_id` 避免重复
- 分别记录双方的未读数和置顶状态
- 记录最后消息信息，便于聊天列表展示

---

### 2.14 聊天消息表 (chat_messages)

**功能**：存储聊天消息内容

```sql
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
```

**设计说明**：
- 通过 `relationship_id` 关联聊天关系
- 支持多种消息类型，通过 `message_type` 区分
- 不同类型消息使用不同字段存储（文本用content，文件用file_url等）
- 支持消息撤回功能（2分钟内）

---

### 2.15 通话记录表 (call_records)

**功能**：存储语音/视频通话记录

```sql
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
```

**设计说明**：
- 完整记录通话生命周期
- 支持多种通话状态
- 记录通话时长等统计信息

---

### 2.16 公告表 (announcements)

**功能**：存储系统公告信息

```sql
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
```

**设计说明**：
- 支持多种公告类型和优先级，便于分类管理
- 状态管理支持草稿、发布、下线等完整生命周期
- 支持设置过期时间，自动失效
- 记录浏览次数，便于统计分析
- 通过 `created_by` 和 `updated_by` 关联管理员用户（逻辑外键）
- 索引设计支持按类型、状态、优先级、发布时间等维度查询

---

### 2.17 系统配置表 (system_configs)

**功能**：存储系统配置信息

```sql
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
```

**初始数据示例**：
```sql
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
```

---

### 2.18 操作日志表 (operation_logs) ⭐新增

**功能**：记录系统关键操作，便于审计和问题追踪

```sql
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
```

**设计说明**：
- 记录所有关键操作，包括登录、数据修改等
- 记录请求信息、IP地址、执行时间等，便于问题排查
- 支持按用户、操作类型、模块等维度查询
- 不进行软删除，保留完整审计记录

---

### 2.19 通知表 (notifications) ⭐新增

**功能**：存储系统消息通知

```sql
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
```

**设计说明**：
- 支持多种通知类型，便于分类管理
- 通过 `related_id` 和 `related_type` 关联业务数据
- 支持已读/未读状态，便于消息提醒
- 索引设计支持按用户、类型、已读状态等维度查询

---

### 2.20 文件上传记录表 (file_uploads) ⭐新增

**功能**：统一管理文件上传记录

```sql
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
```

**设计说明**：
- 统一管理所有文件上传，包括头像、证书、聊天文件等
- 通过 `file_hash` 支持文件去重
- 通过 `upload_source` 区分上传来源
- 支持关联业务数据，便于文件管理

---

## 三、业务流程说明

### 3.1 学生端业务流程

#### 3.1.1 注册与认证流程
1. **注册** → `users` 表创建记录（role=1，status=1）
2. **完善信息** → `student_profiles` 表创建/更新记录
3. **登录** → 更新 `users.last_login_at`、`users.last_login_ip`，记录 `operation_logs`

#### 3.1.2 预约流程
1. **浏览教师** → 查询 `teacher_profiles`、`teacher_teachings`、`teacher_certifications`、`reviews`
2. **创建预约** → `appointments` 表创建记录（status=1 待确认）
   - 同时创建 `payments` 记录（status=1 待支付）
   - 创建 `notifications` 通知教师
   - 记录 `operation_logs`
3. **教师确认** → `appointments.status=2`（已确认），更新 `appointments.confirmed_at`
   - 创建 `chat_relationships` 聊天关系
   - 创建 `notifications` 通知学生
4. **支付** → `payments.status=2`（待确认），上传转账凭证
   - 更新 `payments.transfer_image`、`payments.transfer_time`、`payments.transfer_account`
   - 创建 `notifications` 通知教师
5. **教师确认收款** → `payments.status=3`（已完成），更新 `payments.confirmed_at`、`payments.confirmed_by`
   - 创建 `notifications` 通知学生
6. **课程完成** → `appointments.status=3`（已完成），更新 `appointments.completed_at`
   - 创建 `notifications` 通知学生可评价
7. **评价** → `reviews` 表创建记录
   - 更新 `teacher_profiles.rating`、`teacher_profiles.rating_count`
   - 创建 `notifications` 通知教师

#### 3.1.3 取消预约流程
1. **学生取消** → `appointments.status=4`（已取消），更新 `appointments.cancel_reason`、`appointments.cancel_by`、`appointments.cancel_at`
   - 如果已支付，可申请退款 → `refunds` 表创建记录（status=1 待审核）
   - 创建 `notifications` 通知教师

#### 3.1.4 退款流程
1. **申请退款** → `refunds` 表创建记录（status=1 待审核）
   - 创建 `notifications` 通知管理员
2. **管理员审核** → `refunds.status=2`（已通过）或 `refunds.status=3`（已拒绝）
   - 更新 `refunds.audit_user_id`、`refunds.audit_reason`、`refunds.audit_at`
   - 创建 `notifications` 通知学生
3. **退款完成** → `refunds.status=4`（已完成），更新 `refunds.refund_time`、`refunds.refund_account`

#### 3.1.5 聊天流程
1. **建立聊天关系** → 预约确认时自动创建 `chat_relationships` 记录
2. **发送消息** → `chat_messages` 表创建记录
   - 更新 `chat_relationships.last_message_id`、`chat_relationships.last_message_time`
   - 更新 `chat_relationships.user2_unread_count`（接收方未读数+1）
   - 文件消息同时记录到 `file_uploads` 表
3. **消息已读** → 更新 `chat_messages.is_read=1`、`chat_messages.read_at`
   - 更新 `chat_relationships.user2_unread_count`（未读数清零）
4. **消息撤回** → 更新 `chat_messages.is_recalled=1`、`chat_messages.recalled_at`（2分钟内可撤回）

### 3.2 教师端业务流程

#### 3.2.1 注册与认证流程
1. **注册** → `users` 表创建记录（role=2，status=1）
2. **完善信息** → `teacher_profiles` 表创建/更新记录
3. **上传认证** → `teacher_certifications` 表创建记录（status=1 待审核）
   - 文件记录到 `file_uploads` 表
   - 创建 `notifications` 通知管理员
4. **管理员审核** → `teacher_certifications.status=2`（已通过）或 `status=3`（已拒绝）
   - 更新 `teacher_certifications.audit_user_id`、`teacher_certifications.audit_reason`、`teacher_certifications.audit_at`
   - 创建 `notifications` 通知教师
5. **设置教学信息** → `teacher_teachings` 表创建/更新记录
6. **设置空闲时间** → `teacher_schedules` 表创建/更新记录

#### 3.2.2 接单流程
1. **接收预约** → 查询 `appointments` 表（status=1 待确认）
2. **确认预约** → `appointments.status=2`（已确认），更新 `appointments.confirmed_at`
   - 创建 `chat_relationships` 聊天关系
   - 创建 `notifications` 通知学生
3. **确认收款** → `payments.status=3`（已完成），更新 `payments.confirmed_at`、`payments.confirmed_by`
   - 创建 `notifications` 通知学生
4. **拒绝收款** → `payments.status=4`（已拒绝），更新 `payments.reject_reason`
   - 创建 `notifications` 通知学生重新支付

#### 3.2.3 查看评价
- 查询 `reviews` 表，按 `teacher_id` 筛选
- 查看 `teacher_profiles.rating` 综合评分

### 3.3 管理员端业务流程

#### 3.3.1 用户管理
1. **查看用户列表** → 查询 `users` 表，按 `role`、`status` 筛选
2. **禁用/启用账号** → 更新 `users.status`
   - 记录 `operation_logs`
3. **查看用户详情** → 根据 `role` 关联查询 `teacher_profiles` 或 `student_profiles`

#### 3.3.2 认证审核
1. **查看待审核列表** → 查询 `teacher_certifications` 表（status=1）
2. **审核认证** → 更新 `teacher_certifications.status`、`audit_user_id`、`audit_reason`、`audit_at`
   - 创建 `notifications` 通知教师
   - 记录 `operation_logs`

#### 3.3.3 退款审核
1. **查看待审核退款** → 查询 `refunds` 表（status=1）
2. **审核退款** → 更新 `refunds.status`、`audit_user_id`、`audit_reason`、`audit_at`
   - 创建 `notifications` 通知学生
   - 记录 `operation_logs`

#### 3.3.4 系统管理
1. **公告管理** → `announcements` 表的增删改查
2. **系统配置** → `system_configs` 表的增删改查
3. **操作日志查询** → 查询 `operation_logs` 表，支持多维度筛选

### 3.4 数据安全与日志

#### 3.4.1 操作日志记录
- **登录/登出** → 记录 `operation_logs`（operation_type=login/logout）
- **数据修改** → 记录 `operation_logs`（operation_type=create/update/delete）
- **关键业务操作** → 记录 `operation_logs`（如确认预约、确认收款、审核认证等）

#### 3.4.2 数据安全
- **乐观锁** → 关键业务表使用 `version` 字段，防止并发更新冲突
- **软删除** → 所有表使用 `deleted_at` 实现软删除，保留历史数据
- **状态管理** → 完整的状态流转，确保业务流程正确

---

## 四、表关系说明

### 3.1 核心关系图

```
users (用户表)
  ├── teacher_profiles (教师扩展信息) [1:1]
  ├── student_profiles (学生扩展信息) [1:1] ⭐新增
  ├── teacher_certifications (学历认证) [1:N]
  ├── teacher_teachings (教学信息) [1:N]
  │     ├── teaching_stages (教学阶段) [N:1]
  │     └── subjects (科目) [N:1]
  ├── teacher_schedules (空闲时间) [1:N]
  ├── appointments (预约) [1:N] (作为student_id)
  ├── appointments (预约) [1:N] (作为teacher_id)
  ├── payments (支付) [1:N] (作为student_id/teacher_id)
  ├── refunds (退款) [1:N] (作为student_id/teacher_id) ⭐新增
  ├── reviews (评价) [1:N] (作为student_id/teacher_id)
  ├── chat_relationships (聊天关系) [1:N] (作为user1_id/user2_id)
  ├── chat_messages (聊天消息) [1:N] (作为sender_id/receiver_id)
  ├── call_records (通话记录) [1:N] (作为caller_id/receiver_id)
  ├── notifications (通知) [1:N] (作为user_id) ⭐新增
  ├── file_uploads (文件上传) [1:N] (作为user_id) ⭐新增
  ├── operation_logs (操作日志) [1:N] (作为user_id) ⭐新增
  └── announcements (公告) [1:N] (作为created_by/updated_by)

appointments (预约表)
  ├── payments (支付记录) [1:1]
  ├── refunds (退款记录) [1:N] ⭐新增
  ├── reviews (评价) [1:1]
  └── chat_relationships (聊天关系) [1:1]

payments (支付记录表)
  └── refunds (退款记录) [1:N] ⭐新增

chat_relationships (聊天关系表)
  └── chat_messages (聊天消息) [1:N]

file_uploads (文件上传表)
  ├── teacher_certifications (证书图片) [N:1]
  ├── chat_messages (聊天文件) [N:1]
  └── reviews (评价图片) [N:1]
```

### 3.2 关键设计点

1. **用户表统一设计**：学生、教师、管理员共用一张表，通过 `role` 区分
2. **字典表设计**：教学阶段、科目等使用字典表，便于后台配置和管理
3. **多对多关系**：教师-阶段-科目通过 `teacher_teachings` 表实现，支持灵活配置
4. **状态管理**：所有业务表都有 `status` 字段，便于状态流转和查询
5. **扩展性**：`extra` JSON 字段存储扩展信息，便于后续功能扩展
6. **软删除**：所有表使用 `deleted_at` 实现软删除，保留历史数据
7. **乐观锁**：关键业务表使用 `version` 字段，防止并发更新冲突

---

## 五、索引设计说明

### 4.1 索引策略

1. **主键索引**：所有表使用自增ID作为主键
2. **唯一索引**：业务唯一字段（username、order_no等）
3. **查询索引**：常用查询字段组合索引
   - 用户表：`idx_role_status`（按角色和状态查询）
   - 预约表：`idx_student_id`、`idx_teacher_id`、`idx_status`、`idx_appointment_date`
   - 消息表：`idx_relationship_id`、`idx_created_at`（按关系和时间查询）
4. **软删除索引**：`idx_deleted_at` 用于过滤已删除数据

### 4.2 索引优化建议

- 根据实际查询场景调整索引
- 避免过多索引影响写入性能
- 定期分析慢查询日志，优化索引

---

## 六、数据迁移和扩展建议

### 5.1 扩展性设计

1. **字段扩展**：使用 `extra` JSON 字段存储非核心扩展信息
2. **表扩展**：新增功能通过新增表实现，避免修改核心表结构
3. **字典扩展**：新增教学阶段、科目等通过字典表配置，无需改表结构

### 5.2 数据迁移

1. **版本控制**：使用数据库迁移工具（如 Flyway、Liquibase）管理表结构变更
2. **兼容性**：新增字段设置默认值，保证向后兼容
3. **数据迁移**：历史数据迁移使用脚本，确保数据完整性

---

## 七、总结

本数据库设计遵循以下原则：

✅ **不使用物理外键**：所有关联通过逻辑外键维护  
✅ **灵活可扩展**：字典表、JSON扩展字段、预留字段  
✅ **数据完整性**：软删除、状态管理、版本控制  
✅ **查询性能**：合理的索引设计  
✅ **易于维护**：清晰的表结构、完整的注释  

该设计能够满足当前业务需求，同时为未来功能扩展预留了充足的空间。

---

## 八、业务流程完整性检查 ✅

### 8.1 学生端流程完整性

✅ **注册登录** → users表 + student_profiles表 + operation_logs表  
✅ **浏览教师** → teacher_profiles + teacher_teachings + teacher_certifications + reviews  
✅ **创建预约** → appointments表 + payments表 + notifications表 + operation_logs表  
✅ **支付流程** → payments表状态流转 + notifications表通知  
✅ **课程完成** → appointments表状态更新 + notifications表通知评价  
✅ **评价功能** → reviews表 + teacher_profiles评分更新 + notifications表  
✅ **取消预约** → appointments表状态更新 + refunds表（如需退款）  
✅ **退款流程** → refunds表完整审核流程 + notifications表  
✅ **聊天功能** → chat_relationships表 + chat_messages表 + file_uploads表  
✅ **查看通知** → notifications表查询和已读状态管理  

### 8.2 教师端流程完整性

✅ **注册登录** → users表 + teacher_profiles表 + operation_logs表  
✅ **上传认证** → teacher_certifications表 + file_uploads表 + notifications表  
✅ **设置教学信息** → teacher_teachings表 + teacher_schedules表  
✅ **接收预约** → appointments表查询和确认 + notifications表  
✅ **确认收款** → payments表状态更新 + notifications表  
✅ **查看评价** → reviews表查询 + teacher_profiles评分展示  
✅ **聊天功能** → chat_relationships表 + chat_messages表  

### 8.3 管理员端流程完整性

✅ **用户管理** → users表查询和状态管理 + operation_logs表  
✅ **认证审核** → teacher_certifications表审核 + notifications表 + operation_logs表  
✅ **退款审核** → refunds表审核 + notifications表 + operation_logs表  
✅ **公告管理** → announcements表增删改查 + operation_logs表  
✅ **系统配置** → system_configs表管理 + operation_logs表  
✅ **日志查询** → operation_logs表多维度查询  

### 8.4 数据安全与审计

✅ **操作日志** → 所有关键操作记录到operation_logs表  
✅ **乐观锁** → 关键业务表使用version字段防止并发冲突  
✅ **软删除** → 所有表使用deleted_at保留历史数据  
✅ **状态管理** → 完整的状态流转，确保业务流程正确  
✅ **文件管理** → 统一通过file_uploads表管理，支持去重和关联  

### 8.5 通知系统

✅ **预约通知** → 预约创建、确认、完成时创建notifications  
✅ **支付通知** → 支付状态变更时创建notifications  
✅ **审核通知** → 认证审核、退款审核时创建notifications  
✅ **评价通知** → 评价提交时创建notifications  
✅ **系统通知** → 公告发布等系统级通知  

**结论**：所有业务流程已形成完整闭环，数据表设计支持学生端、教师端、管理员端的所有功能需求，并具备完善的日志记录和通知机制。

