package com.zzy.backend.dto.request.student.notification;

import lombok.Data;

/**
 * 通知列表请求DTO
 */
@Data
public class NotificationListRequest {
    /**
     * 通知类型：1-系统通知，2-预约通知，3-支付通知，4-评价通知，5-聊天通知
     */
    private Integer type;

    /**
     * 是否已读：0-未读，1-已读，null-全部
     */
    private Integer isRead;

    /**
     * 页码（从1开始）
     */
    private Integer page = 1;

    /**
     * 每页数量
     */
    private Integer pageSize = 20;

    /**
     * 用户ID（从Token中获取，不需要前端传递）
     */
    private Long userId;

    /**
     * 验证并修正分页参数
     */
    public void validate() {
        if (page == null || page < 1) {
            page = 1;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = 20;
        }
        if (pageSize > 100) {
            pageSize = 100;
        }
    }

    /**
     * 获取偏移量
     */
    public Integer getOffset() {
        return (page - 1) * pageSize;
    }
}

