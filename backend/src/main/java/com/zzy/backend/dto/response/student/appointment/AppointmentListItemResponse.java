package com.zzy.backend.dto.response.student.appointment;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 预约列表项响应DTO
 */
@Data
public class AppointmentListItemResponse {
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
     * 总金额
     */
    private BigDecimal totalAmount;

    /**
     * 预约状态：1-待确认，2-已确认，3-已完成，4-已取消
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}

