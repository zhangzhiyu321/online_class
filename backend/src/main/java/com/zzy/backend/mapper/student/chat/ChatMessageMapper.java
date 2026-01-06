package com.zzy.backend.mapper.student.chat;

import com.zzy.backend.dto.response.student.chat.MessageResponse;
import com.zzy.backend.entity.chat.ChatMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 聊天消息Mapper接口
 */
@Mapper
public interface ChatMessageMapper {

    /**
     * 插入消息
     *
     * @param message 消息实体
     * @return 影响行数
     */
    int insert(ChatMessage message);

    /**
     * 根据ID查询消息
     *
     * @param id 消息ID
     * @return 消息实体
     */
    ChatMessage selectById(@Param("id") Long id);

    /**
     * 查询聊天消息列表（分页）
     *
     * @param relationshipId 关系ID
     * @param userId 当前用户ID（用于判断是否已读）
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 消息列表
     */
    List<MessageResponse> selectMessagesByRelationshipId(@Param("relationshipId") Long relationshipId,
                                                         @Param("userId") Long userId,
                                                         @Param("offset") Integer offset,
                                                         @Param("limit") Integer limit);

    /**
     * 统计聊天消息总数
     *
     * @param relationshipId 关系ID
     * @return 消息总数
     */
    Long countMessagesByRelationshipId(@Param("relationshipId") Long relationshipId);

    /**
     * 标记消息为已读
     *
     * @param relationshipId 关系ID
     * @param receiverId 接收者ID
     * @return 影响行数
     */
    int markMessagesAsRead(@Param("relationshipId") Long relationshipId, @Param("receiverId") Long receiverId);

    /**
     * 撤回消息
     *
     * @param messageId 消息ID
     * @param senderId 发送者ID（用于权限验证）
     * @return 影响行数
     */
    int recallMessage(@Param("messageId") Long messageId, @Param("senderId") Long senderId);
}

