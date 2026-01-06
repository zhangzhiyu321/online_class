package com.zzy.backend.dto.response.student.chat;

import lombok.Data;

/**
 * 未读消息数响应DTO
 */
@Data
public class UnreadCountResponse {
    /**
     * 未读消息总数
     */
    private Integer total;

    /**
     * 未读消息数（与total相同，兼容前端）
     */
    private Integer count;
}

