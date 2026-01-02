package com.zzy.backend.common.exception;

import com.zzy.backend.common.Result;
import com.zzy.backend.common.constant.ResponseCode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.stream.Collectors;

/**
 * 全局异常处理器 - 统一处理所有异常，记录详细错误信息
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e, HttpServletRequest request) {
        log.error("业务异常 - URI: {}, 错误码: {}, 错误信息: {}", 
                request.getRequestURI(), e.getCode(), e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 处理参数验证异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException e, HttpServletRequest request) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.error("参数验证失败 - URI: {}, 错误信息: {}, 详细: {}", 
                request.getRequestURI(), message, e.getBindingResult().getAllErrors());
        return Result.error(ResponseCode.BAD_REQUEST, "参数验证失败: " + message);
    }

    /**
     * 处理参数绑定异常
     */
    @ExceptionHandler(BindException.class)
    public Result<?> handleBindException(BindException e, HttpServletRequest request) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.error("参数绑定失败 - URI: {}, 错误信息: {}", request.getRequestURI(), message);
        return Result.error(ResponseCode.BAD_REQUEST, "参数绑定失败: " + message);
    }

    /**
     * 处理参数类型不匹配异常
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public Result<?> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e, HttpServletRequest request) {
        String message = String.format("参数 '%s' 类型不匹配，期望类型: %s", 
                e.getName(), e.getRequiredType() != null ? e.getRequiredType().getSimpleName() : "未知");
        log.error("参数类型不匹配 - URI: {}, 参数名: {}, 错误信息: {}", 
                request.getRequestURI(), e.getName(), message);
        return Result.error(ResponseCode.BAD_REQUEST, message);
    }

    /**
     * 处理404异常
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public Result<?> handleNoHandlerFoundException(NoHandlerFoundException e, HttpServletRequest request) {
        log.error("接口不存在 - URI: {}, 方法: {}", e.getRequestURL(), e.getHttpMethod());
        return Result.error(ResponseCode.NOT_FOUND, "接口不存在: " + e.getRequestURL());
    }

    /**
     * 处理空指针异常
     */
    @ExceptionHandler(NullPointerException.class)
    public Result<?> handleNullPointerException(NullPointerException e, HttpServletRequest request) {
        log.error("空指针异常 - URI: {}, 错误信息: {}", request.getRequestURI(), e.getMessage(), e);
        return Result.error(ResponseCode.INTERNAL_SERVER_ERROR, "系统内部错误，请联系管理员");
    }

    /**
     * 处理其他所有异常
     */
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e, HttpServletRequest request) {
        log.error("系统异常 - URI: {}, 异常类型: {}, 错误信息: {}", 
                request.getRequestURI(), e.getClass().getName(), e.getMessage(), e);
        
        // 打印完整堆栈信息到日志
        StringBuilder errorInfo = new StringBuilder();
        errorInfo.append("\n========== 系统异常详情 ==========\n");
        errorInfo.append("请求URI: ").append(request.getRequestURI()).append("\n");
        errorInfo.append("请求方法: ").append(request.getMethod()).append("\n");
        errorInfo.append("异常类型: ").append(e.getClass().getName()).append("\n");
        errorInfo.append("异常信息: ").append(e.getMessage()).append("\n");
        errorInfo.append("异常堆栈:\n");
        java.io.StringWriter sw = new java.io.StringWriter();
        java.io.PrintWriter pw = new java.io.PrintWriter(sw);
        e.printStackTrace(pw);
        errorInfo.append(sw.toString());
        errorInfo.append("===================================\n");
        
        log.error(errorInfo.toString());
        
        return Result.error(ResponseCode.INTERNAL_SERVER_ERROR, "系统异常，请稍后重试");
    }
}

