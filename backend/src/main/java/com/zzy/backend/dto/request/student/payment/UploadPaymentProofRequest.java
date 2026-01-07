package com.zzy.backend.dto.request.student.payment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 上传支付凭证请求DTO
 */
@Data
public class UploadPaymentProofRequest {
    /**
     * 转账截图URL（必填）
     */
    @NotBlank(message = "转账截图不能为空")
    private String transferImage;

    /**
     * 转账金额（必填）
     */
    @NotNull(message = "转账金额不能为空")
    @Positive(message = "转账金额必须大于0")
    private BigDecimal transferAmount;

    /**
     * 转账时间（必填，ISO格式）
     */
    @NotNull(message = "转账时间不能为空")
    private LocalDateTime transferTime;

    /**
     * 转账账号（可选）
     */
    private String transferAccount;
}

