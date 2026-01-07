package com.zzy.backend.dto.response.student.payment;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 支付列表项响应DTO
 */
@Data
public class PaymentListItemResponse {
    /**
     * 支付ID
     */
    private Long id;

    /**
     * 支付单号
     */
    private String paymentNo;

    /**
     * 预约ID
     */
    private Long appointmentId;

    /**
     * 教师ID
     */
    private Long teacherId;

    /**
     * 教师姓名
     */
    private String teacherName;

    /**
     * 支付金额
     */
    private BigDecimal amount;

    /**
     * 支付状态：1-待支付，2-待确认，3-已完成，4-已拒绝
     */
    private Integer status;

    /**
     * 预约日期
     */
    private LocalDate appointmentDate;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}

