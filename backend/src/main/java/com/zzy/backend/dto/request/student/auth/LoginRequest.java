package com.zzy.backend.dto.request.student.auth;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

/**
 * 登录请求DTO
 */
@Data
public class LoginRequest {
    /**
     * 用户名（必填）
     */
    @NotBlank(message = "用户名不能为空")
    private String username;

    /**
     * 密码（必填）
     */
    @NotBlank(message = "密码不能为空")
    private String password;
}

