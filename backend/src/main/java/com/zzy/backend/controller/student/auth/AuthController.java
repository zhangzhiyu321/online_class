package com.zzy.backend.controller.student.auth;

import com.zzy.backend.common.Result;
import com.zzy.backend.common.constant.ResponseCode;
import com.zzy.backend.common.exception.BusinessException;
import com.zzy.backend.dto.request.student.auth.LoginRequest;
import com.zzy.backend.dto.request.student.auth.PhoneLoginRequest;
import com.zzy.backend.dto.request.student.auth.RegisterRequest;
import com.zzy.backend.dto.request.student.auth.SendSmsCodeRequest;
import com.zzy.backend.dto.response.student.auth.LoginResponse;
import com.zzy.backend.service.common.SmsService;
import com.zzy.backend.service.student.auth.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@Tag(name = "认证管理", description = "用户登录、注册相关功能")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private SmsService smsService;

    /**
     * 用户登录
     *
     * @param request 登录请求
     * @param httpRequest HTTP请求对象
     * @return 登录响应
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "使用用户名和密码登录")
    public Result<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        log.info("登录请求, username: {}", request.getUsername());

        try {
            // 获取客户端IP
            String clientIp = getClientIp(httpRequest);
            
            LoginResponse response = authService.login(request, clientIp);
            return Result.success("登录成功", response);
        } catch (BusinessException e) {
            log.error("登录失败: {}", e.getMessage());
            return Result.error(ResponseCode.BUSINESS_ERROR, e.getMessage());
        } catch (Exception e) {
            log.error("登录失败", e);
            return Result.error("登录失败，请稍后重试");
        }
    }

    /**
     * 用户注册
     *
     * @param request 注册请求
     * @return 注册结果
     */
    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "新用户注册账号")
    public Result<Void> register(@Valid @RequestBody RegisterRequest request) {
        log.info("注册请求, username: {}", request.getUsername());

        try {
            authService.register(request);
            return Result.<Void>success("注册成功", null);
        } catch (BusinessException e) {
            log.error("注册失败: {}", e.getMessage());
            return Result.<Void>error(ResponseCode.BUSINESS_ERROR, e.getMessage());
        } catch (Exception e) {
            log.error("注册失败", e);
            return Result.<Void>error("注册失败，请稍后重试");
        }
    }

    /**
     * 发送短信验证码
     *
     * @param request 发送验证码请求
     * @param httpRequest HTTP请求对象
     * @return 发送结果
     */
    @PostMapping("/sms/send")
    @Operation(summary = "发送短信验证码", description = "向指定手机号发送6位数字验证码，1分钟内只能发送一次")
    public Result<Void> sendSmsCode(
            @Valid @RequestBody SendSmsCodeRequest request,
            HttpServletRequest httpRequest) {
        log.info("发送短信验证码请求, phone: {}", request.getPhone());

        try {
            // 获取客户端IP
            String clientIp = getClientIp(httpRequest);
            
            // 发送验证码
            String code = smsService.sendVerificationCode(request.getPhone(), clientIp);
            
            // 开发环境返回验证码（方便测试），生产环境不返回
            if (code != null) {
                log.info("开发环境：验证码为 {}", code);
                return Result.success("验证码已发送（开发环境：" + code + "）", null);
            }
            
            return Result.success("验证码已发送", null);
        } catch (BusinessException e) {
            log.error("发送验证码失败: {}", e.getMessage());
            return Result.error(ResponseCode.BUSINESS_ERROR, e.getMessage());
        } catch (Exception e) {
            log.error("发送验证码失败", e);
            return Result.error("发送验证码失败，请稍后重试");
        }
    }

    /**
     * 手机号验证码登录
     *
     * @param request 手机号登录请求
     * @param httpRequest HTTP请求对象
     * @return 登录响应
     */
    @PostMapping("/login/phone")
    @Operation(summary = "手机号验证码登录", description = "使用手机号和验证码登录，如果手机号未注册则自动注册")
    public Result<LoginResponse> phoneLogin(
            @Valid @RequestBody PhoneLoginRequest phoneLoginRequest,
            HttpServletRequest httpRequest) {
        log.info("手机号登录请求, phone: {}", phoneLoginRequest.getPhone());

        try {
            // 获取客户端IP
            String clientIp = getClientIp(httpRequest);
            
            // 手机号登录（如果未注册则自动注册）
            LoginResponse response = authService.phoneLogin(phoneLoginRequest, clientIp);
            return Result.success("登录成功", response);
        } catch (BusinessException e) {
            log.error("手机号登录失败: {}", e.getMessage());
            return Result.error(ResponseCode.BUSINESS_ERROR, e.getMessage());
        } catch (Exception e) {
            log.error("手机号登录失败", e);
            return Result.error("登录失败，请稍后重试");
        }
    }

    /**
     * 获取客户端IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 如果是多级代理，取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}

