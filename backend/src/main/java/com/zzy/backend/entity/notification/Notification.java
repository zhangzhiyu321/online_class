package com.zzy.backend.entity.notification;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 通知实体类
 */
@Data
public class Notification {
    /**
     * ID
     */
    private Long id;

    /**
     * 接收用户ID
     */
    private Long userId;

    /**
     * 通知类型：1-系统通知，2-预约通知，3-支付通知，4-评价通知，5-聊天通知
     */
    private Integer type;

    /**
     * 通知标题
     */
    private String title;

    /**
     * 通知内容
     */
    private String content;

    /**
     * 关联ID（如预约ID、支付ID等）
     */
    private Long relatedId;

    /**
     * 关联类型：appointment、payment等
     */
    private String relatedType;

    /**
     * 是否已读：0-未读，1-已读
     */
    private Integer isRead;

    /**
     * 已读时间
     */
    private LocalDateTime readAt;

    /**
     * 状态：1-正常，2-已删除
     */
    private Integer status;

    /**
     * 扩展信息（JSON格式）
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

