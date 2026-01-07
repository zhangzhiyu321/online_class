package com.zzy.backend.dto.response.student.payment;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 创建支付记录响应DTO
 */
@Data
public class CreatePaymentResponse {
    /**
     * 支付ID
     */
    private Long id;

    /**
     * 支付单号
     */
    private String paymentNo;

    /**
     * 预约ID
     */
    private Long appointmentId;

    /**
     * 教师ID
     */
    private Long teacherId;

    /**
     * 教师姓名
     */
    private String teacherName;

    /**
     * 支付金额
     */
    private BigDecimal amount;

    /**
     * 支付方式：1-银行转账，2-支付宝，3-微信，4-其他
     */
    private Integer paymentMethod;

    /**
     * 开户银行（银行转账时显示）
     */
    private String bankName;

    /**
     * 银行账号（银行转账时显示）
     */
    private String bankAccount;

    /**
     * 账户持有人（银行转账时显示）
     */
    private String accountHolder;

    /**
     * 支付宝二维码URL（支付宝支付时显示）
     */
    private String alipayQrCode;

    /**
     * 微信二维码URL（微信支付时显示）
     */
    private String wechatQrCode;

    /**
     * 预约日期
     */
    private LocalDate appointmentDate;

    /**
     * 支付状态：1-待支付，2-待确认，3-已完成，4-已拒绝
     */
    private Integer status;
}

