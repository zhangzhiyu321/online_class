package com.zzy.backend.dto.response.student.chat;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 聊天关系详情响应DTO
 */
@Data
public class ChatRelationshipResponse {
    /**
     * 聊天关系ID
     */
    private Long relationshipId;

    /**
     * 对方用户ID
     */
    private Long otherUserId;

    /**
     * 对方用户姓名
     */
    private String otherUserName;

    /**
     * 对方用户头像
     */
    private String otherUserAvatar;

    /**
     * 对方用户联系方式
     */
    private String otherUserPhone;

    /**
     * 关联预约ID
     */
    private Long appointmentId;

    /**
     * 未读消息数
     */
    private Integer unreadCount;

    /**
     * 是否置顶：0-否，1-是
     */
    private Integer isTop;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}

