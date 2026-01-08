package com.zzy.backend.dto.request.student.auth;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 手机号登录请求DTO
 */
@Data
public class PhoneLoginRequest {
    /**
     * 手机号（必填，11位手机号）
     */
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式错误")
    private String phone;

    /**
     * 验证码（必填，6位数字）
     */
    @NotBlank(message = "验证码不能为空")
    @Pattern(regexp = "^\\d{6}$", message = "验证码为6位数字")
    private String code;
}

