package com.zzy.backend.entity.student;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付记录实体类
 */
@Data
public class Payment {
    /**
     * 支付ID
     */
    private Long id;

    /**
     * 支付单号（唯一）
     */
    private String paymentNo;

    /**
     * 预约ID
     */
    private Long appointmentId;

    /**
     * 学生用户ID
     */
    private Long studentId;

    /**
     * 教师用户ID
     */
    private Long teacherId;

    /**
     * 支付金额
     */
    private BigDecimal amount;

    /**
     * 支付方式：1-银行转账，2-支付宝，3-微信，4-其他
     */
    private Integer paymentMethod;

    /**
     * 转账截图URL
     */
    private String transferImage;

    /**
     * 转账时间
     */
    private LocalDateTime transferTime;

    /**
     * 转账账号
     */
    private String transferAccount;

    /**
     * 转账金额
     */
    private BigDecimal transferAmount;

    /**
     * 支付状态：1-待支付，2-待确认，3-已完成，4-已拒绝
     */
    private Integer status;

    /**
     * 拒绝原因
     */
    private String rejectReason;

    /**
     * 确认收款时间
     */
    private LocalDateTime confirmedAt;

    /**
     * 确认人ID（教师）
     */
    private Long confirmedBy;

    /**
     * 扩展信息（JSON格式）
     */
    private String extra;

    /**
     * 版本号（乐观锁）
     */
    private Integer version;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 删除时间（软删除）
     */
    private LocalDateTime deletedAt;
}

