package com.zzy.backend.service.student.auth;

import com.zzy.backend.dto.request.student.auth.LoginRequest;
import com.zzy.backend.dto.request.student.auth.PhoneLoginRequest;
import com.zzy.backend.dto.request.student.auth.RegisterRequest;
import com.zzy.backend.dto.response.student.auth.LoginResponse;

/**
 * 认证服务接口
 */
public interface AuthService {

    /**
     * 用户登录
     *
     * @param request 登录请求
     * @param clientIp 客户端IP
     * @return 登录响应
     */
    LoginResponse login(LoginRequest request, String clientIp);

    /**
     * 手机号验证码登录（如果未注册则自动注册）
     *
     * @param request 手机号登录请求
     * @param clientIp 客户端IP
     * @return 登录响应
     */
    LoginResponse phoneLogin(PhoneLoginRequest request, String clientIp);

    /**
     * 用户注册
     *
     * @param request 注册请求
     * @return 是否成功
     */
    boolean register(RegisterRequest request);
}

