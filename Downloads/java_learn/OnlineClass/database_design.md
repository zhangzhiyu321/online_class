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

### 2.3 学历认证表 (teacher_certifications)

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

### 2.4 教学阶段字典表 (teaching_stages)

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

### 2.5 科目字典表 (subjects)

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
('CHEMISTRY', '化学', '理科', 5);
```

---

### 2.6 教师教学信息表 (teacher_teachings)

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

### 2.7 教师空闲时间表 (teacher_schedules)

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

### 2.8 预约表 (appointments)

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

### 2.9 支付记录表 (payments)

**功能**：存储支付相关信息

```sql
CREATE TABLE `payments` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `payment_no` VARCHAR(50) NOT NULL COMMENT '支付单号（唯一）',
  `appointment_id` BIGINT UNSIGNED NOT NULL COMMENT '预约ID',
  `student_id` BIGINT UNSIGNED NOT NULL COMMENT '学生用户ID',
  `teacher_id` BIGINT UNSIGNED NOT NULL COMMENT '教师用户ID',
  `amount` DECIMAL(10,2) NOT NULL COMMENT '支付金额',
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
  KEY `idx_created_at` (`created_at`),
  KEY `idx_deleted_at` (`deleted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付记录表';
```

**设计说明**：
- 与预约表一对一关系（一个预约对应一个支付记录）
- 完整记录转账凭证信息
- 支持教师拒绝收款并退回待支付状态

---

### 2.10 评价表 (reviews)

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

### 2.11 聊天关系表 (chat_relationships)

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

### 2.12 聊天消息表 (chat_messages)

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

### 2.13 通话记录表 (call_records)

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

### 2.14 系统配置表 (system_configs)

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
INSERT INTO `system_configs` (`config_key`, `config_value`, `config_type`, `description`, `group_name`) VALUES
('platform_name', '线上家教平台', 'string', '平台名称', 'basic'),
('contact_phone', '400-xxx-xxxx', 'string', '客服电话', 'basic'),
('service_hours', '09:00-18:00', 'string', '服务时间', 'basic'),
('max_voice_duration', '300', 'number', '语音消息最大时长（秒）', 'chat'),
('max_image_size', '10485760', 'number', '图片最大大小（字节）', 'chat'),
('max_file_size', '52428800', 'number', '文件最大大小（字节）', 'chat');
```

---

## 三、表关系说明

### 3.1 核心关系图

```
users (用户表)
  ├── teacher_profiles (教师扩展信息) [1:1]
  ├── teacher_certifications (学历认证) [1:N]
  ├── teacher_teachings (教学信息) [1:N]
  │     ├── teaching_stages (教学阶段) [N:1]
  │     └── subjects (科目) [N:1]
  ├── teacher_schedules (空闲时间) [1:N]
  ├── appointments (预约) [1:N] (作为student_id)
  ├── appointments (预约) [1:N] (作为teacher_id)
  ├── payments (支付) [1:N] (作为student_id/teacher_id)
  ├── reviews (评价) [1:N] (作为student_id/teacher_id)
  ├── chat_relationships (聊天关系) [1:N] (作为user1_id/user2_id)
  └── call_records (通话记录) [1:N] (作为caller_id/receiver_id)

appointments (预约表)
  ├── payments (支付记录) [1:1]
  ├── reviews (评价) [1:1]
  └── chat_relationships (聊天关系) [1:1]

chat_relationships (聊天关系表)
  └── chat_messages (聊天消息) [1:N]
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

## 四、索引设计说明

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

## 五、数据迁移和扩展建议

### 5.1 扩展性设计

1. **字段扩展**：使用 `extra` JSON 字段存储非核心扩展信息
2. **表扩展**：新增功能通过新增表实现，避免修改核心表结构
3. **字典扩展**：新增教学阶段、科目等通过字典表配置，无需改表结构

### 5.2 数据迁移

1. **版本控制**：使用数据库迁移工具（如 Flyway、Liquibase）管理表结构变更
2. **兼容性**：新增字段设置默认值，保证向后兼容
3. **数据迁移**：历史数据迁移使用脚本，确保数据完整性

---

## 六、总结

本数据库设计遵循以下原则：

✅ **不使用物理外键**：所有关联通过逻辑外键维护  
✅ **灵活可扩展**：字典表、JSON扩展字段、预留字段  
✅ **数据完整性**：软删除、状态管理、版本控制  
✅ **查询性能**：合理的索引设计  
✅ **易于维护**：清晰的表结构、完整的注释  

该设计能够满足当前业务需求，同时为未来功能扩展预留了充足的空间。

