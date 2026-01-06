package com.zzy.backend.dto.response.student.notification;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 通知详情响应DTO
 */
@Data
public class NotificationDetailResponse {
    /**
     * 通知ID
     */
    private Long id;

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
     * 关联ID
     */
    private Long relatedId;

    /**
     * 关联类型
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
     * 扩展信息
     */
    private String extra;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}

