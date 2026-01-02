package com.zzy.backend.common.util;

import java.util.Collection;
import java.util.Map;

/**
 * 字符串工具类
 */
public class StringUtil {
    
    /**
     * 判断字符串是否为空
     */
    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
    
    /**
     * 判断字符串是否不为空
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }
    
    /**
     * 判断字符串是否为空白
     */
    public static boolean isBlank(String str) {
        return str == null || str.trim().length() == 0;
    }
    
    /**
     * 判断字符串是否不为空白
     */
    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }
    
    /**
     * 去除首尾空格，null返回空字符串
     */
    public static String trim(String str) {
        return str == null ? "" : str.trim();
    }
    
    /**
     * 如果字符串为null，返回默认值
     */
    public static String defaultIfNull(String str, String defaultValue) {
        return str == null ? defaultValue : str;
    }
    
    /**
     * 如果字符串为空，返回默认值
     */
    public static String defaultIfEmpty(String str, String defaultValue) {
        return isEmpty(str) ? defaultValue : str;
    }
    
    /**
     * 判断集合是否为空
     */
    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }
    
    /**
     * 判断Map是否为空
     */
    public static boolean isEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }
}

