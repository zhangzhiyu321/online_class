package com.zzy.backend.service.student.chat;

import com.zzy.backend.dto.request.student.chat.SendMessageRequest;
import com.zzy.backend.dto.response.student.chat.ChatListItemResponse;
import com.zzy.backend.dto.response.student.chat.ChatRelationshipResponse;
import com.zzy.backend.dto.response.student.chat.MessageResponse;
import com.zzy.backend.dto.response.student.chat.UnreadCountResponse;

import java.util.List;

/**
 * 聊天服务接口
 */
public interface ChatService {

    /**
     * 获取聊天列表
     *
     * @param userId 用户ID
     * @return 聊天列表
     */
    List<ChatListItemResponse> getChatList(Long userId);

    /**
     * 获取聊天消息列表（分页）
     *
     * @param relationshipId 关系ID
     * @param userId 当前用户ID
     * @param page 页码
     * @param pageSize 每页数量
     * @return 消息列表
     */
    List<MessageResponse> getChatMessages(Long relationshipId, Long userId, Integer page, Integer pageSize);

    /**
     * 发送消息
     *
     * @param request 发送消息请求
     * @param senderId 发送者ID
     * @return 消息响应
     */
    MessageResponse sendMessage(SendMessageRequest request, Long senderId);

    /**
     * 撤回消息
     *
     * @param messageId 消息ID
     * @param userId 用户ID（用于权限验证）
     * @return 是否成功
     */
    boolean recallMessage(Long messageId, Long userId);

    /**
     * 标记消息已读
     *
     * @param relationshipId 关系ID
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean markMessageRead(Long relationshipId, Long userId);

    /**
     * 获取聊天关系详情
     *
     * @param relationshipId 关系ID
     * @param userId 当前用户ID
     * @return 聊天关系详情
     */
    ChatRelationshipResponse getChatRelationship(Long relationshipId, Long userId);

    /**
     * 获取未读消息数
     *
     * @param userId 用户ID
     * @return 未读消息数
     */
    UnreadCountResponse getUnreadCount(Long userId);
}

