package com.zzy.backend.controller.student.payment;

import com.zzy.backend.common.Result;
import com.zzy.backend.service.student.payment.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 支付回调控制器
 * 用于接收支付宝、微信等支付平台的支付结果回调
 */
@Slf4j
@RestController
@RequestMapping("/payment/callback")
@Tag(name = "支付回调", description = "支付平台回调接口（无需认证）")
public class PaymentCallbackController {

    @Autowired
    private PaymentService paymentService;

    /**
     * 支付宝支付回调
     * 
     * 注意：实际使用时需要：
     * 1. 验证签名确保请求来自支付宝
     * 2. 验证订单金额
     * 3. 处理幂等性（防止重复回调）
     */
    @PostMapping("/alipay")
    @Operation(summary = "支付宝支付回调", description = "接收支付宝支付结果通知")
    public String alipayCallback(HttpServletRequest request) {
        log.info("收到支付宝支付回调");
        
        try {
            // 从请求中获取参数
            Map<String, String> params = getRequestParams(request);
            log.info("支付宝回调参数: {}", params);
            
            // TODO: 验证签名（实际使用时必须实现）
            // boolean signValid = AlipaySignature.rsaCheckV1(params, alipayPublicKey, charset, signType);
            // if (!signValid) {
            //     log.error("支付宝回调签名验证失败");
            //     return "failure";
            // }
            
            // 获取关键参数
            String outTradeNo = params.get("out_trade_no"); // 商户订单号（即我们的支付单号）
            String tradeNo = params.get("trade_no"); // 支付宝交易号
            String tradeStatus = params.get("trade_status"); // 交易状态
            String totalAmount = params.get("total_amount"); // 订单金额
            
            log.info("支付宝回调: outTradeNo={}, tradeNo={}, tradeStatus={}, totalAmount={}", 
                    outTradeNo, tradeNo, tradeStatus, totalAmount);
            
            // 只处理支付成功的回调
            if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
                // 自动确认支付
                boolean success = paymentService.autoConfirmPayment(outTradeNo, tradeNo, 2); // 2表示支付宝
                if (success) {
                    log.info("支付宝支付自动确认成功, paymentNo: {}", outTradeNo);
                    return "success"; // 返回success告知支付宝处理成功
                } else {
                    log.error("支付宝支付自动确认失败, paymentNo: {}", outTradeNo);
                    return "failure";
                }
            } else {
                log.warn("支付宝回调交易状态不是成功状态, tradeStatus: {}", tradeStatus);
                return "failure";
            }
            
        } catch (Exception e) {
            log.error("处理支付宝回调失败", e);
            return "failure";
        }
    }

    /**
     * 微信支付回调
     * 
     * 注意：实际使用时需要：
     * 1. 验证签名确保请求来自微信
     * 2. 验证订单金额
     * 3. 处理幂等性（防止重复回调）
     */
    @PostMapping("/wechat")
    @Operation(summary = "微信支付回调", description = "接收微信支付结果通知")
    public String wechatCallback(HttpServletRequest request) {
        log.info("收到微信支付回调");
        
        try {
            // 从请求中获取参数（微信使用XML格式）
            String xmlData = getRequestBody(request);
            log.info("微信回调XML数据: {}", xmlData);
            
            // TODO: 解析XML并验证签名（实际使用时必须实现）
            // Map<String, String> params = WechatPayUtil.parseXml(xmlData);
            // boolean signValid = WechatPayUtil.verifySign(params, wechatPayKey);
            // if (!signValid) {
            //     log.error("微信回调签名验证失败");
            //     return generateWechatFailureResponse();
            // }
            
            // 模拟解析（实际需要解析XML）
            // String outTradeNo = params.get("out_trade_no"); // 商户订单号
            // String transactionId = params.get("transaction_id"); // 微信支付订单号
            // String returnCode = params.get("return_code"); // 返回状态码
            // String resultCode = params.get("result_code"); // 业务结果
            
            // 由于是模拟，这里返回成功
            // 实际使用时需要根据解析的参数进行处理
            log.warn("微信支付回调处理未完整实现，需要集成微信支付SDK");
            return generateWechatSuccessResponse();
            
        } catch (Exception e) {
            log.error("处理微信回调失败", e);
            return generateWechatFailureResponse();
        }
    }

    /**
     * 查询支付状态（供前端轮询使用）
     */
    @GetMapping("/status/{paymentNo}")
    @Operation(summary = "查询支付状态", description = "根据支付单号查询支付状态")
    public Result<?> queryPaymentStatus(@PathVariable String paymentNo) {
        log.info("查询支付状态, paymentNo: {}", paymentNo);
        
        try {
            var result = paymentService.queryPaymentStatus(paymentNo);
            if (result == null) {
                return Result.error("支付记录不存在");
            }
            return Result.success(result);
        } catch (Exception e) {
            log.error("查询支付状态失败", e);
            return Result.error("查询支付状态失败");
        }
    }

    /**
     * 从HttpServletRequest中获取所有参数
     */
    private Map<String, String> getRequestParams(HttpServletRequest request) {
        Map<String, String> params = new java.util.HashMap<>();
        request.getParameterMap().forEach((key, values) -> {
            if (values != null && values.length > 0) {
                params.put(key, values[0]);
            }
        });
        return params;
    }

    /**
     * 从HttpServletRequest中获取请求体
     */
    private String getRequestBody(HttpServletRequest request) {
        try {
            java.io.BufferedReader reader = request.getReader();
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        } catch (Exception e) {
            log.error("读取请求体失败", e);
            return "";
        }
    }

    /**
     * 生成微信支付成功响应
     */
    private String generateWechatSuccessResponse() {
        return "<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml>";
    }

    /**
     * 生成微信支付失败响应
     */
    private String generateWechatFailureResponse() {
        return "<xml><return_code><![CDATA[FAIL]]></return_code><return_msg><![CDATA[处理失败]]></return_msg></xml>";
    }
}

