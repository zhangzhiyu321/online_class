package com.zzy.backend.dto.request.student.payment;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建支付记录请求DTO
 */
@Data
public class CreatePaymentRequest {
    /**
     * 预约ID（必填）
     */
    @NotNull(message = "预约ID不能为空")
    private Long appointmentId;

    /**
     * 支付方式：1-银行转账，2-支付宝，3-微信，4-其他
     */
    private Integer paymentMethod = 1;
}

