package com.zzy.backend.service.common.impl;

import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.sms.v20210111.SmsClient;
import com.tencentcloudapi.sms.v20210111.models.SendSmsRequest;
import com.tencentcloudapi.sms.v20210111.models.SendSmsResponse;
import com.tencentcloudapi.sms.v20210111.models.SendStatus;
import com.zzy.backend.common.exception.BusinessException;
import com.zzy.backend.service.common.SmsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 短信服务实现类（腾讯云）
 */
@Slf4j
@Service
public class SmsServiceImpl implements SmsService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    // 腾讯云短信配置
    @Value("${sms.tencent.secret-id:}")
    private String secretId;

    @Value("${sms.tencent.secret-key:}")
    private String secretKey;

    @Value("${sms.tencent.app-id:}")
    private String appId;

    @Value("${sms.tencent.sign-name:}")
    private String signName;

    @Value("${sms.tencent.template-id:}")
    private String templateId;

    // 短信配置
    @Value("${sms.code.expiration:300}")
    private int codeExpiration; // 验证码有效期（秒），默认5分钟

    @Value("${sms.code.send-interval:60}")
    private int sendInterval; // 发送间隔（秒），默认1分钟

    @Value("${sms.code.max-error-count:5}")
    private int maxErrorCount; // 最大错误次数，默认5次

    @Value("${sms.ip.daily-limit:20}")
    private int ipDailyLimit; // 每个IP每天最多发送次数，默认20次

    @Value("${sms.phone.daily-limit:10}")
    private int phoneDailyLimit; // 每个手机号每天最多发送次数，默认10次

    // Redis Key 前缀
    private static final String CODE_PREFIX = "sms:code:";
    private static final String CODE_SEND_PREFIX = "sms:send:";
    private static final String CODE_ERROR_PREFIX = "sms:error:";
    private static final String IP_SEND_PREFIX = "sms:ip:";
    private static final String PHONE_SEND_PREFIX = "sms:phone:";

    // 开发环境标识
    @Value("${spring.profiles.active:prod}")
    private String activeProfile;

    /**
     * 发送短信验证码
     */
    @Override
    public String sendVerificationCode(String phone, String clientIp) {
        log.info("发送短信验证码, phone: {}, ip: {}", phone, clientIp);

        // 1. 检查手机号是否可以发送（频率限制）
        if (!canSendCode(phone)) {
            throw new BusinessException(String.format("发送过于频繁，请%d秒后再试", sendInterval));
        }

        // 2. 检查IP是否可以发送（IP限制）
        if (!canSendCodeByIp(clientIp)) {
            throw new BusinessException("今日发送次数已达上限，请明天再试");
        }

        // 3. 检查手机号每日发送次数
        String phoneDailyKey = PHONE_SEND_PREFIX + phone;
        String phoneDailyCount = redisTemplate.opsForValue().get(phoneDailyKey);
        if (phoneDailyCount != null && Integer.parseInt(phoneDailyCount) >= phoneDailyLimit) {
            throw new BusinessException("今日发送次数已达上限，请明天再试");
        }

        // 4. 生成6位随机验证码
        String code = generateCode();

        // 5. 发送短信
        try {
            sendSms(phone, code);
            log.info("短信验证码发送成功, phone: {}", phone);
        } catch (Exception e) {
            log.error("短信验证码发送失败, phone: {}, error: {}", phone, e.getMessage(), e);
            throw new BusinessException("验证码发送失败，请稍后重试");
        }

        // 6. 存储验证码到Redis（有效期5分钟）
        String codeKey = CODE_PREFIX + phone;
        redisTemplate.opsForValue().set(codeKey, code, codeExpiration, TimeUnit.SECONDS);

        // 7. 记录发送时间（防止频繁发送）
        String sendKey = CODE_SEND_PREFIX + phone;
        redisTemplate.opsForValue().set(sendKey, "1", sendInterval, TimeUnit.SECONDS);

        // 8. 记录IP发送次数（每天重置）
        String ipKey = IP_SEND_PREFIX + clientIp;
        redisTemplate.opsForValue().increment(ipKey);
        redisTemplate.expire(ipKey, 24, TimeUnit.HOURS);

        // 9. 记录手机号发送次数（每天重置）
        redisTemplate.opsForValue().increment(phoneDailyKey);
        redisTemplate.expire(phoneDailyKey, 24, TimeUnit.HOURS);

        // 10. 清除之前的错误次数记录
        String errorKey = CODE_ERROR_PREFIX + phone;
        redisTemplate.delete(errorKey);

        // 开发环境返回验证码（方便测试），生产环境不返回
        if ("dev".equals(activeProfile) || "local".equals(activeProfile)) {
            log.warn("开发环境模式：验证码为 {}", code);
            return code;
        }
        return null;
    }

    /**
     * 验证短信验证码
     */
    @Override
    public boolean verifyCode(String phone, String code) {
        log.info("验证短信验证码, phone: {}", phone);

        // 1. 检查错误次数
        String errorKey = CODE_ERROR_PREFIX + phone;
        String errorCountStr = redisTemplate.opsForValue().get(errorKey);
        int errorCount = errorCountStr != null ? Integer.parseInt(errorCountStr) : 0;

        if (errorCount >= maxErrorCount) {
            // 清除验证码和错误记录，需要重新发送
            String codeKey = CODE_PREFIX + phone;
            redisTemplate.delete(codeKey);
            redisTemplate.delete(errorKey);
            throw new BusinessException("验证码错误次数过多，请重新获取");
        }

        // 2. 获取存储的验证码
        String codeKey = CODE_PREFIX + phone;
        String storedCode = redisTemplate.opsForValue().get(codeKey);

        if (storedCode == null) {
            // 验证码不存在或已过期
            errorCount++;
            redisTemplate.opsForValue().set(errorKey, String.valueOf(errorCount), 30, TimeUnit.MINUTES);
            throw new BusinessException("验证码已过期，请重新获取");
        }

        // 3. 验证验证码
        if (!storedCode.equals(code)) {
            // 验证码错误
            errorCount++;
            redisTemplate.opsForValue().set(errorKey, String.valueOf(errorCount), 30, TimeUnit.MINUTES);
            
            if (errorCount >= maxErrorCount) {
                // 达到最大错误次数，清除验证码
                redisTemplate.delete(codeKey);
                throw new BusinessException("验证码错误次数过多，请重新获取");
            }
            
            throw new BusinessException(String.format("验证码错误，还可尝试%d次", maxErrorCount - errorCount));
        }

        // 4. 验证成功，清除验证码和错误记录
        redisTemplate.delete(codeKey);
        redisTemplate.delete(errorKey);
        log.info("短信验证码验证成功, phone: {}", phone);
        return true;
    }

    /**
     * 检查手机号是否可以发送验证码
     */
    @Override
    public boolean canSendCode(String phone) {
        String sendKey = CODE_SEND_PREFIX + phone;
        String value = redisTemplate.opsForValue().get(sendKey);
        return value == null;
    }

    /**
     * 检查IP是否可以发送验证码
     */
    @Override
    public boolean canSendCodeByIp(String clientIp) {
        String ipKey = IP_SEND_PREFIX + clientIp;
        String countStr = redisTemplate.opsForValue().get(ipKey);
        if (countStr == null) {
            return true;
        }
        int count = Integer.parseInt(countStr);
        return count < ipDailyLimit;
    }

    /**
     * 获取验证码错误次数
     */
    @Override
    public int getErrorCount(String phone) {
        String errorKey = CODE_ERROR_PREFIX + phone;
        String countStr = redisTemplate.opsForValue().get(errorKey);
        return countStr != null ? Integer.parseInt(countStr) : 0;
    }

    /**
     * 生成6位随机验证码
     */
    private String generateCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }

    /**
     * 发送短信（腾讯云）
     */
    private void sendSms(String phone, String code) throws TencentCloudSDKException {
        // 如果未配置腾讯云密钥，在开发环境直接返回（不实际发送）
        if (secretId == null || secretId.isEmpty() || secretKey == null || secretKey.isEmpty()) {
            if ("dev".equals(activeProfile) || "local".equals(activeProfile)) {
                log.warn("未配置腾讯云短信，开发环境跳过实际发送, phone: {}, code: {}", phone, code);
                return;
            } else {
                throw new BusinessException("短信服务未配置，请联系管理员");
            }
        }

        try {
            // 实例化认证对象
            Credential cred = new Credential(secretId, secretKey);

            // 实例化HTTP选项
            HttpProfile httpProfile = new HttpProfile();
            httpProfile.setEndpoint("sms.tencentcloudapi.com");

            // 实例化客户端选项
            ClientProfile clientProfile = new ClientProfile();
            clientProfile.setHttpProfile(httpProfile);

            // 实例化SMS客户端对象
            SmsClient smsClient = new SmsClient(cred, "ap-guangzhou", clientProfile);

            // 实例化请求对象
            SendSmsRequest req = new SendSmsRequest();
            
            // 设置应用ID
            req.setSmsSdkAppId(appId);
            
            // 设置签名名称（可选，如果模板已包含签名）
            // req.setSignName(signName);
            
            // 设置模板ID
            req.setTemplateId(templateId);

            // 设置手机号（需要加国家码，中国为+86）
            String[] phoneNumbers = {"+86" + phone};
            req.setPhoneNumberSet(phoneNumbers);

            // 设置模板参数（验证码和有效期（分钟））
            String[] templateParams = {code, String.valueOf(codeExpiration / 60)};
            req.setTemplateParamSet(templateParams);

            // 发送短信
            SendSmsResponse resp = smsClient.SendSms(req);
            
            // 检查发送结果
            if (resp.getSendStatusSet() != null && resp.getSendStatusSet().length > 0) {
                SendStatus statusInfo = resp.getSendStatusSet()[0];
                String statusCode = statusInfo.getCode();
                if (!"Ok".equals(statusCode)) {
                    String message = statusInfo.getMessage() != null ? statusInfo.getMessage() : "未知错误";
                    log.error("腾讯云短信发送失败, phone: {}, code: {}, message: {}", phone, statusCode, message);
                    throw new BusinessException("验证码发送失败：" + message);
                }
                log.info("腾讯云短信发送成功, phone: {}, requestId: {}", phone, resp.getRequestId());
            }
        } catch (TencentCloudSDKException e) {
            log.error("腾讯云短信SDK异常, phone: {}, error: {}", phone, e.getMessage(), e);
            throw new BusinessException("验证码发送失败，请稍后重试");
        }
    }
}

