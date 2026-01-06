package com.zzy.backend.mapper.student.chat;

import com.zzy.backend.dto.response.student.chat.ChatListItemResponse;
import com.zzy.backend.dto.response.student.chat.ChatRelationshipResponse;
import com.zzy.backend.entity.chat.ChatRelationship;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 聊天关系Mapper接口
 */
@Mapper
public interface ChatRelationshipMapper {

    /**
     * 根据用户ID查询聊天关系（获取或创建）
     *
     * @param user1Id 用户1 ID（较小的ID）
     * @param user2Id 用户2 ID（较大的ID）
     * @return 聊天关系
     */
    ChatRelationship selectByUserPair(@Param("user1Id") Long user1Id, @Param("user2Id") Long user2Id);

    /**
     * 插入聊天关系
     *
     * @param relationship 聊天关系实体
     * @return 影响行数
     */
    int insert(ChatRelationship relationship);

    /**
     * 根据ID查询聊天关系
     *
     * @param id 关系ID
     * @return 聊天关系
     */
    ChatRelationship selectById(@Param("id") Long id);

    /**
     * 查询用户的聊天列表
     *
     * @param userId 用户ID
     * @return 聊天列表
     */
    List<ChatListItemResponse> selectChatListByUserId(@Param("userId") Long userId);

    /**
     * 查询聊天关系详情
     *
     * @param relationshipId 关系ID
     * @param userId 当前用户ID
     * @return 聊天关系详情
     */
    ChatRelationshipResponse selectChatRelationshipDetail(@Param("relationshipId") Long relationshipId, @Param("userId") Long userId);

    /**
     * 更新最后消息信息
     *
     * @param relationshipId 关系ID
     * @param lastMessageId 最后消息ID
     * @param lastMessageTime 最后消息时间
     * @return 影响行数
     */
    int updateLastMessage(@Param("relationshipId") Long relationshipId,
                          @Param("lastMessageId") Long lastMessageId,
                          @Param("lastMessageTime") java.time.LocalDateTime lastMessageTime);

    /**
     * 增加未读消息数
     *
     * @param relationshipId 关系ID
     * @param userId 用户ID（user1或user2）
     * @param count 增加的数量
     * @return 影响行数
     */
    int increaseUnreadCount(@Param("relationshipId") Long relationshipId,
                           @Param("userId") Long userId,
                           @Param("count") Integer count);

    /**
     * 清空未读消息数
     *
     * @param relationshipId 关系ID
     * @param userId 用户ID（user1或user2）
     * @return 影响行数
     */
    int clearUnreadCount(@Param("relationshipId") Long relationshipId, @Param("userId") Long userId);

    /**
     * 统计用户未读消息总数
     *
     * @param userId 用户ID
     * @return 未读消息总数
     */
    Integer countUnreadMessages(@Param("userId") Long userId);
}

