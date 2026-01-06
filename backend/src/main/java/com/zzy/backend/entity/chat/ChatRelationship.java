package com.zzy.backend.entity.chat;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 聊天关系实体类
 */
@Data
public class ChatRelationship {
    /**
     * ID
     */
    private Long id;

    /**
     * 用户1 ID（较小的ID）
     */
    private Long user1Id;

    /**
     * 用户2 ID（较大的ID）
     */
    private Long user2Id;

    /**
     * 关联预约ID
     */
    private Long appointmentId;

    /**
     * 用户1未读消息数
     */
    private Integer user1UnreadCount;

    /**
     * 用户2未读消息数
     */
    private Integer user2UnreadCount;

    /**
     * 用户1是否置顶：0-否，1-是
     */
    private Integer user1Top;

    /**
     * 用户2是否置顶：0-否，1-是
     */
    private Integer user2Top;

    /**
     * 最后一条消息ID
     */
    private Long lastMessageId;

    /**
     * 最后消息时间
     */
    private LocalDateTime lastMessageTime;

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

