package com.zzy.backend.dto.request.student.auth;

import lombok.Data;
import jakarta.validation.constraints.*;

/**
 * 注册请求DTO
 */
@Data
public class RegisterRequest {
    /**
     * 用户名（必填，唯一）
     */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3-50个字符之间")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
    private String username;

    /**
     * 密码（必填，至少6位）
     */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 50, message = "密码长度必须在6-50个字符之间")
    private String password;

    /**
     * 昵称（必填）
     */
    @NotBlank(message = "昵称不能为空")
    @Size(max = 50, message = "昵称长度不能超过50个字符")
    private String nickname;

    /**
     * 手机号（选填）
     */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "请输入正确的手机号")
    private String phone;

    /**
     * 邮箱（选填）
     */
    @Email(message = "请输入正确的邮箱地址")
    private String email;

    /**
     * 角色：1-学生，2-教师（必填，默认1）
     */
    @NotNull(message = "角色不能为空")
    @Min(value = 1, message = "角色值无效")
    @Max(value = 2, message = "角色值无效")
    private Integer role = 1;
}

