package com.zzy.backend.service.common;

/**
 * 短信服务接口
 */
public interface SmsService {
    
    /**
     * 发送短信验证码
     * 
     * @param phone 手机号
     * @param clientIp 客户端IP（用于安全控制）
     * @return 验证码（仅在开发环境返回，生产环境返回null）
     */
    String sendVerificationCode(String phone, String clientIp);
    
    /**
     * 验证短信验证码
     * 
     * @param phone 手机号
     * @param code 验证码
     * @return 是否验证通过
     */
    boolean verifyCode(String phone, String code);
    
    /**
     * 检查手机号是否可以发送验证码（频率限制检查）
     * 
     * @param phone 手机号
     * @return 是否可以发送
     */
    boolean canSendCode(String phone);
    
    /**
     * 检查IP是否可以发送验证码（IP限制检查）
     * 
     * @param clientIp 客户端IP
     * @return 是否可以发送
     */
    boolean canSendCodeByIp(String clientIp);
    
    /**
     * 获取验证码错误次数
     * 
     * @param phone 手机号
     * @return 错误次数
     */
    int getErrorCount(String phone);
}

