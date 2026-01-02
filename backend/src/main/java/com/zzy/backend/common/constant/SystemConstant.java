package com.zzy.backend.common.constant;

/**
 * 系统常量
 */
public class SystemConstant {
    
    /**
     * JWT Token 请求头名称
     */
    public static final String TOKEN_HEADER = "Authorization";
    
    /**
     * JWT Token 前缀
     */
    public static final String TOKEN_PREFIX = "Bearer ";
    
    /**
     * 用户ID请求头名称
     */
    public static final String USER_ID_HEADER = "X-User-Id";
    
    /**
     * 用户角色请求头名称
     */
    public static final String USER_ROLE_HEADER = "X-User-Role";
    
    /**
     * 默认分页大小
     */
    public static final Integer DEFAULT_PAGE_SIZE = 10;
    
    /**
     * 最大分页大小
     */
    public static final Integer MAX_PAGE_SIZE = 100;
    
    /**
     * 日期时间格式
     */
    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    
    /**
     * 日期格式
     */
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    
    /**
     * 时间格式
     */
    public static final String TIME_FORMAT = "HH:mm:ss";
}

