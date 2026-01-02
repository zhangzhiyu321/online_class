package com.zzy.backend.common.aspect;

import com.alibaba.fastjson2.JSON;
import com.zzy.backend.common.util.StringUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;

/**
 * 接口日志切面 - 记录所有Controller方法的请求和响应
 */
@Slf4j
@Aspect
@Component
public class LogAspect {
    
    @Pointcut("execution(* com.zzy.backend.controller..*.*(..))")
    public void controllerPointcut() {
    }
    
    @Around("controllerPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        
        // 获取请求信息
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes != null ? attributes.getRequest() : null;
        
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        
        // 构建请求日志
        StringBuilder requestLog = new StringBuilder();
        requestLog.append("\n========== 接口请求开始 ==========\n");
        if (request != null) {
            requestLog.append("请求URI: ").append(request.getRequestURI()).append("\n");
            requestLog.append("请求方法: ").append(request.getMethod()).append("\n");
            requestLog.append("客户端IP: ").append(getClientIp(request)).append("\n");
            requestLog.append("请求头: ").append(JSON.toJSONString(getRequestHeaders(request))).append("\n");
        }
        requestLog.append("类名: ").append(className).append("\n");
        requestLog.append("方法名: ").append(methodName).append("\n");
        requestLog.append("请求参数: ").append(formatArgs(args)).append("\n");
        
        log.info(requestLog.toString());
        
        Object result = null;
        Exception exception = null;
        
        try {
            // 执行方法
            result = joinPoint.proceed();
            return result;
        } catch (Exception e) {
            exception = e;
            throw e;
        } finally {
            // 记录响应日志
            long endTime = System.currentTimeMillis();
            long executeTime = endTime - startTime;
            
            StringBuilder responseLog = new StringBuilder();
            responseLog.append("\n========== 接口请求结束 ==========\n");
            responseLog.append("类名: ").append(className).append("\n");
            responseLog.append("方法名: ").append(methodName).append("\n");
            responseLog.append("执行时间: ").append(executeTime).append("ms\n");
            
            if (exception != null) {
                responseLog.append("异常类型: ").append(exception.getClass().getName()).append("\n");
                responseLog.append("异常信息: ").append(exception.getMessage()).append("\n");
                responseLog.append("异常堆栈: ").append(getStackTrace(exception)).append("\n");
                log.error(responseLog.toString(), exception);
            } else {
                responseLog.append("响应结果: ").append(formatResult(result)).append("\n");
                log.info(responseLog.toString());
            }
        }
    }
    
    /**
     * 格式化参数
     */
    private String formatArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return "[]";
        }
        try {
            // 过滤掉HttpServletRequest等不能序列化的对象
            Object[] filteredArgs = Arrays.stream(args)
                    .filter(arg -> !(arg instanceof HttpServletRequest))
                    .toArray();
            return JSON.toJSONString(filteredArgs);
        } catch (Exception e) {
            return Arrays.toString(args);
        }
    }
    
    /**
     * 格式化返回结果
     */
    private String formatResult(Object result) {
        if (result == null) {
            return "null";
        }
        try {
            String json = JSON.toJSONString(result);
            // 如果结果太长，截取前1000个字符
            if (json.length() > 1000) {
                return json.substring(0, 1000) + "...(已截断)";
            }
            return json;
        } catch (Exception e) {
            return result.toString();
        }
    }
    
    /**
     * 获取客户端IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (StringUtil.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (StringUtil.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (StringUtil.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (StringUtil.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (StringUtil.isNotEmpty(ip) && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
    
    /**
     * 获取请求头信息
     */
    private java.util.Map<String, String> getRequestHeaders(HttpServletRequest request) {
        java.util.Map<String, String> headers = new java.util.HashMap<>();
        java.util.Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            headers.put(headerName, request.getHeader(headerName));
        }
        return headers;
    }
    
    /**
     * 获取异常堆栈信息
     */
    private String getStackTrace(Exception e) {
        java.io.StringWriter sw = new java.io.StringWriter();
        java.io.PrintWriter pw = new java.io.PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }
}

