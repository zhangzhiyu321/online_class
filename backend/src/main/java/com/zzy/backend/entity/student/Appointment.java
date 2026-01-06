package com.zzy.backend.entity.student;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 预约实体类
 */
@Data
public class Appointment {
    /**
     * 预约ID
     */
    private Long id;

    /**
     * 订单号（唯一）
     */
    private String orderNo;

    /**
     * 学生用户ID
     */
    private Long studentId;

    /**
     * 教师用户ID
     */
    private Long teacherId;

    /**
     * 教学阶段ID
     */
    private Integer stageId;

    /**
     * 科目ID
     */
    private Integer subjectId;

    /**
     * 预约日期
     */
    private LocalDate appointmentDate;

    /**
     * 开始时间
     */
    private LocalTime startTime;

    /**
     * 结束时间
     */
    private LocalTime endTime;

    /**
     * 课程时长（分钟）
     */
    private Integer duration;

    /**
     * 课时单价
     */
    private BigDecimal pricePerHour;

    /**
     * 总金额
     */
    private BigDecimal totalAmount;

    /**
     * 学生姓名
     */
    private String studentName;

    /**
     * 学生年级
     */
    private String studentGrade;

    /**
     * 学生联系方式
     */
    private String studentPhone;

    /**
     * 备注信息（学习需求等）
     */
    private String remark;

    /**
     * 预约状态：1-待确认，2-已确认，3-已完成，4-已取消
     */
    private Integer status;

    /**
     * 取消原因
     */
    private String cancelReason;

    /**
     * 取消人ID
     */
    private Long cancelBy;

    /**
     * 取消时间
     */
    private LocalDateTime cancelAt;

    /**
     * 钉钉会议链接
     */
    private String dingtalkUrl;

    /**
     * 确认时间
     */
    private LocalDateTime confirmedAt;

    /**
     * 完成时间
     */
    private LocalDateTime completedAt;

    /**
     * 扩展信息（JSON格式）
     */
    private String extra;

    /**
     * 版本号（乐观锁）
     */
    private Integer version;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 删除时间（软删除）
     */
    private LocalDateTime deletedAt;
}

