package com.zzy.backend.dto.response.student.notification;

import lombok.Data;

/**
 * 未读通知数响应DTO
 */
@Data
public class UnreadCountResponse {
    /**
     * 未读通知数
     */
    private Integer count;
}

