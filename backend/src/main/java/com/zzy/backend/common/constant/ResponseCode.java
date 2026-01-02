package com.zzy.backend.common.constant;

/**
 * 响应状态码常量
 */
public class ResponseCode {
    
    /**
     * 成功
     */
    public static final Integer SUCCESS = 200;
    
    /**
     * 参数错误
     */
    public static final Integer BAD_REQUEST = 400;
    
    /**
     * 未授权
     */
    public static final Integer UNAUTHORIZED = 401;
    
    /**
     * 禁止访问
     */
    public static final Integer FORBIDDEN = 403;
    
    /**
     * 资源不存在
     */
    public static final Integer NOT_FOUND = 404;
    
    /**
     * 服务器内部错误
     */
    public static final Integer INTERNAL_SERVER_ERROR = 500;
    
    /**
     * 业务异常
     */
    public static final Integer BUSINESS_ERROR = 600;
}

