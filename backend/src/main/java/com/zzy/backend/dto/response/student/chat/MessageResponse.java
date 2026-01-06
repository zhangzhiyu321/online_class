package com.zzy.backend.dto.response.student.chat;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 消息响应DTO
 */
@Data
public class MessageResponse {
    /**
     * 消息ID
     */
    private Long id;

    /**
     * 聊天关系ID
     */
    private Long relationshipId;

    /**
     * 发送者ID
     */
    private Long senderId;

    /**
     * 接收者ID
     */
    private Long receiverId;

    /**
     * 消息类型：1-文本，4-图片
     */
    private Integer messageType;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 文件URL
     */
    private String fileUrl;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 图片宽度
     */
    private Integer imageWidth;

    /**
     * 图片高度
     */
    private Integer imageHeight;

    /**
     * 缩略图URL
     */
    private String thumbnailUrl;

    /**
     * 是否已读：0-未读，1-已读
     */
    private Integer isRead;

    /**
     * 已读时间
     */
    private LocalDateTime readAt;

    /**
     * 是否已撤回：0-否，1-是
     */
    private Integer isRecalled;

    /**
     * 发送时间
     */
    private LocalDateTime createdAt;
}

