package com.zzy.backend.entity.teacher;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 教师扩展信息实体类
 */
@Data
public class TeacherProfile {
    /**
     * ID
     */
    private Long id;

    /**
     * 用户ID（逻辑外键）
     */
    private Long userId;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 个人简介
     */
    private String introduction;

    /**
     * 教学经验年限
     */
    private Integer teachingYears;

    /**
     * 教学风格描述
     */
    private String teachingStyle;

    /**
     * 综合评分（0-5分）
     */
    private BigDecimal rating;

    /**
     * 评价总数
     */
    private Integer ratingCount;

    /**
     * 银行账号
     */
    private String bankAccount;

    /**
     * 开户银行
     */
    private String bankName;

    /**
     * 账户持有人
     */
    private String accountHolder;

    /**
     * 扩展信息
     */
    private String extra;

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

