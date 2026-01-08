package com.zzy.backend.common.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 订单号生成工具类
 * 订单号格式：前缀 + 日期(yyyyMMdd) + 6位随机数
 */
public class OrderNoGenerator {

    /**
     * 序列号计数器（每天重置）
     */
    private static final AtomicInteger SEQUENCE = new AtomicInteger(0);
    
    /**
     * 当前日期（用于判断是否需要重置序列号）
     */
    private static volatile String currentDate = "";

    /**
     * 生成预约订单号
     * 格式：AP + yyyyMMdd + 6位序列号
     * 
     * @return 订单号
     */
    public static String generateAppointmentOrderNo() {
        return generateOrderNo("AP");
    }

    /**
     * 生成支付订单号
     * 格式：PAY + yyyyMMdd + 6位序列号
     * 
     * @return 订单号
     */
    public static String generatePaymentOrderNo() {
        return generateOrderNo("PAY");
    }

    /**
     * 生成退款订单号
     * 格式：RF + yyyyMMdd + 6位序列号
     * 
     * @return 订单号
     */
    public static String generateRefundOrderNo() {
        return generateOrderNo("RF");
    }

    /**
     * 生成订单号（通用方法）
     * 
     * @param prefix 订单号前缀
     * @return 订单号
     */
    private static synchronized String generateOrderNo(String prefix) {
        // 获取当前日期字符串
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        
        // 如果日期变化，重置序列号
        if (!dateStr.equals(currentDate)) {
            currentDate = dateStr;
            SEQUENCE.set(0);
        }
        
        // 获取并递增序列号
        int sequence = SEQUENCE.incrementAndGet();
        
        // 如果序列号超过999999，重置为1
        if (sequence > 999999) {
            SEQUENCE.set(1);
            sequence = 1;
        }
        
        // 格式化序列号为6位数字（不足补0）
        String sequenceStr = String.format("%06d", sequence);
        
        // 拼接订单号
        return prefix + dateStr + sequenceStr;
    }

    /**
     * 生成自定义前缀的订单号
     * 
     * @param prefix 订单号前缀
     * @return 订单号
     */
    public static String generate(String prefix) {
        return generateOrderNo(prefix);
    }
}

