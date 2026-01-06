package com.zzy.backend.dto.response.student.appointment;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 预约详情响应DTO
 */
@Data
public class AppointmentDetailResponse {
    /**
     * 预约ID
     */
    private Long id;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 教师ID
     */
    private Long teacherId;

    /**
     * 教师姓名
     */
    private String teacherName;

    /**
     * 教师头像
     */
    private String teacherAvatar;

    /**
     * 教师联系方式
     */
    private String teacherPhone;

    /**
     * 教学阶段ID
     */
    private Integer stageId;

    /**
     * 教学阶段名称
     */
    private String stageName;

    /**
     * 科目ID
     */
    private Integer subjectId;

    /**
     * 科目名称
     */
    private String subjectName;

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
     * 备注信息
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
     * 创建时间
     */
    private LocalDateTime createdAt;
}

