package com.zzy.backend.entity.chat;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 聊天消息实体类
 */
@Data
public class ChatMessage {
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
     * 消息类型：1-文本，2-文件，3-语音，4-图片
     */
    private Integer messageType;

    /**
     * 消息内容（文本消息或JSON格式）
     */
    private String content;

    /**
     * 文件URL（文件/语音/图片）
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
     * 语音时长（秒）
     */
    private Integer duration;

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
     * 撤回时间
     */
    private LocalDateTime recalledAt;

    /**
     * 消息状态：1-正常，2-已删除
     */
    private Integer status;

    /**
     * 扩展信息（JSON格式）
     */
    private String extra;

    /**
     * 发送时间
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

