package com.zzy.backend.dto.response.student.chat;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 聊天列表项响应DTO
 */
@Data
public class ChatListItemResponse {
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
     * 未读消息数
     */
    private Integer unreadCount;

    /**
     * 是否置顶：0-否，1-是
     */
    private Integer isTop;

    /**
     * 最后一条消息内容
     */
    private String lastMessageContent;

    /**
     * 最后一条消息类型：1-文本，4-图片
     */
    private Integer lastMessageType;

    /**
     * 最后消息时间
     */
    private LocalDateTime lastMessageTime;
}

