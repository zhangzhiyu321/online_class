package com.zzy.backend.dto.request.student.appointment;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 取消预约请求DTO
 */
@Data
public class CancelAppointmentRequest {
    /**
     * 取消原因（必填）
     */
    @NotBlank(message = "取消原因不能为空")
    @Size(max = 500, message = "取消原因长度不能超过500个字符")
    private String reason;
}

