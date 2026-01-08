package com.zzy.backend.dto.request.student.auth;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 发送短信验证码请求DTO
 */
@Data
public class SendSmsCodeRequest {
    /**
     * 手机号（必填，11位手机号）
     */
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式错误")
    private String phone;
}

