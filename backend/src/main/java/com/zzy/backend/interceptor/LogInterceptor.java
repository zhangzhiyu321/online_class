package com.zzy.backend.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 日志拦截器 - 记录请求基本信息
 */
@Slf4j
@Component
public class LogInterceptor implements HandlerInterceptor {
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        long startTime = System.currentTimeMillis();
        request.setAttribute("startTime", startTime);
        
        // 记录请求基本信息
        log.debug("请求开始 - URI: {}, Method: {}, IP: {}", 
                request.getRequestURI(), 
                request.getMethod(), 
                getClientIp(request));
        
        return true;
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, 
                                Object handler, Exception ex) throws Exception {
        long startTime = (Long) request.getAttribute("startTime");
        long endTime = System.currentTimeMillis();
        long executeTime = endTime - startTime;
        
        if (ex != null) {
            log.error("请求异常 - URI: {}, 执行时间: {}ms, 异常: {}", 
                    request.getRequestURI(), executeTime, ex.getMessage());
        } else {
            log.debug("请求完成 - URI: {}, 状态码: {}, 执行时间: {}ms", 
                    request.getRequestURI(), response.getStatus(), executeTime);
        }
    }
    
    /**
     * 获取客户端IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}

