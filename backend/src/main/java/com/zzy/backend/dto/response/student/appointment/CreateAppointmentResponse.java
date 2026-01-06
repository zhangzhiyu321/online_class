package com.zzy.backend.dto.response.student.appointment;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 创建预约响应DTO
 */
@Data
public class CreateAppointmentResponse {
    /**
     * 预约ID
     */
    private Long id;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 预约状态：1-待确认，2-已确认，3-已完成，4-已取消
     */
    private Integer status;

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
     * 总金额
     */
    private BigDecimal totalAmount;
}

