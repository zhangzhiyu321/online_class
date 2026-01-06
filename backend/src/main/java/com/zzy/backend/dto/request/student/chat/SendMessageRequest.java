package com.zzy.backend.dto.request.student.chat;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 发送消息请求DTO
 */
@Data
public class SendMessageRequest {
    /**
     * 接收者ID（必填）
     */
    @NotNull(message = "接收者ID不能为空")
    private Long receiverId;

    /**
     * 消息类型：1-文本，4-图片（必填）
     */
    @NotNull(message = "消息类型不能为空")
    private Integer messageType;

    /**
     * 消息内容（文本消息时必填，图片消息时可为空）
     */
    @Size(max = 5000, message = "消息内容长度不能超过5000个字符")
    private String content;

    /**
     * 文件URL（图片消息时必填）
     */
    private String fileUrl;

    /**
     * 文件名（图片消息时可选）
     */
    private String fileName;

    /**
     * 文件大小（字节，图片消息时可选）
     */
    private Long fileSize;

    /**
     * 图片宽度（图片消息时可选）
     */
    private Integer imageWidth;

    /**
     * 图片高度（图片消息时可选）
     */
    private Integer imageHeight;

    /**
     * 缩略图URL（图片消息时可选）
     */
    private String thumbnailUrl;
}

