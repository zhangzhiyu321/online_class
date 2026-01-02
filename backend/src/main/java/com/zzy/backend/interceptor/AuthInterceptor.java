package com.zzy.backend.interceptor;

import com.zzy.backend.common.constant.SystemConstant;
import com.zzy.backend.common.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 认证拦截器
 */
@Slf4j
@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 获取 Token
        String token = request.getHeader(SystemConstant.TOKEN_HEADER);
        
        // 如果没有 Token，直接放行（某些接口不需要认证）
        if (!StringUtils.hasText(token)) {
            return true;
        }
        
        // 移除 Bearer 前缀
        if (token.startsWith(SystemConstant.TOKEN_PREFIX)) {
            token = token.substring(SystemConstant.TOKEN_PREFIX.length());
        }
        
        // 验证 Token
        if (!jwtUtil.validateToken(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"Token无效或已过期\",\"data\":null}");
            return false;
        }
        
        // 将用户信息放入请求属性中，供后续使用
        try {
            Long userId = jwtUtil.getUserIdFromToken(token);
            String username = jwtUtil.getUsernameFromToken(token);
            String role = jwtUtil.getRoleFromToken(token);
            
            request.setAttribute("userId", userId);
            request.setAttribute("username", username);
            request.setAttribute("role", role);
        } catch (Exception e) {
            log.error("解析Token失败", e);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"Token解析失败\",\"data\":null}");
            return false;
        }
        
        return true;
    }
}

