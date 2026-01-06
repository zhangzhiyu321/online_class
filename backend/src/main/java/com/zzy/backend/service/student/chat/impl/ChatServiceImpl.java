package com.zzy.backend.service.student.chat.impl;

import com.zzy.backend.dto.request.student.chat.SendMessageRequest;
import com.zzy.backend.dto.response.student.chat.ChatListItemResponse;
import com.zzy.backend.dto.response.student.chat.ChatRelationshipResponse;
import com.zzy.backend.dto.response.student.chat.MessageResponse;
import com.zzy.backend.dto.response.student.chat.UnreadCountResponse;
import com.zzy.backend.entity.chat.ChatMessage;
import com.zzy.backend.entity.chat.ChatRelationship;
import com.zzy.backend.common.exception.BusinessException;
import com.zzy.backend.mapper.student.auth.UserMapper;
import com.zzy.backend.mapper.student.chat.ChatMessageMapper;
import com.zzy.backend.mapper.student.chat.ChatRelationshipMapper;
import com.zzy.backend.service.student.chat.ChatService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 聊天服务实现类
 */
@Slf4j
@Service
public class ChatServiceImpl implements ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatServiceImpl.class);

    @Autowired
    private ChatRelationshipMapper chatRelationshipMapper;

    @Autowired
    private ChatMessageMapper chatMessageMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public List<ChatListItemResponse> getChatList(Long userId) {
        log.info("获取聊天列表, userId: {}", userId);
        return chatRelationshipMapper.selectChatListByUserId(userId);
    }

    @Override
    public List<MessageResponse> getChatMessages(Long relationshipId, Long userId, Integer page, Integer pageSize) {
        log.info("获取聊天消息, relationshipId: {}, userId: {}, page: {}, pageSize: {}", relationshipId, userId, page, pageSize);

        // 验证关系是否存在且用户有权限
        ChatRelationship relationship = chatRelationshipMapper.selectById(relationshipId);
        if (relationship == null) {
            throw new BusinessException("聊天关系不存在");
        }
        Long user1Id = relationship.getUser1Id();
        Long user2Id = relationship.getUser2Id();
        if ((user1Id == null || !user1Id.equals(userId)) && (user2Id == null || !user2Id.equals(userId))) {
            throw new BusinessException("无权访问该聊天");
        }

        // 设置默认分页参数
        if (page == null || page < 1) {
            page = 1;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = 20;
        }
        if (pageSize > 100) {
            pageSize = 100;
        }

        int offset = (page - 1) * pageSize;
        List<MessageResponse> messages = chatMessageMapper.selectMessagesByRelationshipId(relationshipId, userId, offset, pageSize);

        // 反转列表，使消息按时间正序排列
        java.util.Collections.reverse(messages);

        return messages;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MessageResponse sendMessage(SendMessageRequest request, Long senderId) {
        log.info("发送消息, senderId: {}, receiverId: {}, messageType: {}", senderId, request.getReceiverId(), request.getMessageType());

        // 验证接收者是否存在
        if (userMapper.selectById(request.getReceiverId()) == null) {
            throw new BusinessException("接收者不存在");
        }

        // 不能给自己发消息（可选，根据业务需求）
        if (senderId.equals(request.getReceiverId())) {
            throw new BusinessException("不能给自己发送消息");
        }

        // 验证消息类型和内容
        if (request.getMessageType() == null) {
            throw new BusinessException("消息类型不能为空");
        }

        if (request.getMessageType() == 1) {
            // 文本消息
            if (request.getContent() == null || request.getContent().trim().isEmpty()) {
                throw new BusinessException("文本消息内容不能为空");
            }
        } else if (request.getMessageType() == 4) {
            // 图片消息
            if (request.getFileUrl() == null || request.getFileUrl().trim().isEmpty()) {
                throw new BusinessException("图片URL不能为空");
            }
        } else {
            throw new BusinessException("暂不支持该消息类型");
        }

        // 获取或创建聊天关系
        Long user1Id = Math.min(senderId, request.getReceiverId());
        Long user2Id = Math.max(senderId, request.getReceiverId());

        ChatRelationship relationship = chatRelationshipMapper.selectByUserPair(user1Id, user2Id);
        if (relationship == null) {
            // 创建新的聊天关系
            relationship = new ChatRelationship();
            relationship.setUser1Id(user1Id);
            relationship.setUser2Id(user2Id);
            relationship.setUser1UnreadCount(0);
            relationship.setUser2UnreadCount(0);
            relationship.setUser1Top(0);
            relationship.setUser2Top(0);
            relationship.setStatus(1);
            relationship.setCreatedAt(LocalDateTime.now());
            relationship.setUpdatedAt(LocalDateTime.now());
            chatRelationshipMapper.insert(relationship);
            log.info("创建新的聊天关系, relationshipId: {}", relationship.getId());
        }

        // 创建消息
        ChatMessage message = new ChatMessage();
        message.setRelationshipId(relationship.getId());
        message.setSenderId(senderId);
        message.setReceiverId(request.getReceiverId());
        message.setMessageType(request.getMessageType());
        message.setContent(request.getContent());
        message.setFileUrl(request.getFileUrl());
        message.setFileName(request.getFileName());
        message.setFileSize(request.getFileSize());
        message.setImageWidth(request.getImageWidth());
        message.setImageHeight(request.getImageHeight());
        message.setThumbnailUrl(request.getThumbnailUrl());
        message.setIsRead(0); // 默认未读
        message.setIsRecalled(0);
        message.setStatus(1);
        message.setCreatedAt(LocalDateTime.now());
        message.setUpdatedAt(LocalDateTime.now());

        chatMessageMapper.insert(message);
        log.info("消息发送成功, messageId: {}", message.getId());

        // 更新聊天关系的最后消息信息
        chatRelationshipMapper.updateLastMessage(relationship.getId(), message.getId(), message.getCreatedAt());

        // 增加接收者的未读消息数（如果接收者不是发送者）
        if (!senderId.equals(request.getReceiverId())) {
            chatRelationshipMapper.increaseUnreadCount(relationship.getId(), request.getReceiverId(), 1);
        }

        // 构建响应
        MessageResponse response = new MessageResponse();
        BeanUtils.copyProperties(message, response);
        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean recallMessage(Long messageId, Long userId) {
        log.info("撤回消息, messageId: {}, userId: {}", messageId, userId);

        ChatMessage message = chatMessageMapper.selectById(messageId);
        if (message == null) {
            throw new BusinessException("消息不存在");
        }

        if (!message.getSenderId().equals(userId)) {
            throw new BusinessException("只能撤回自己发送的消息");
        }

        // 检查消息是否已超过2分钟
        LocalDateTime now = LocalDateTime.now();
        long minutes = java.time.Duration.between(message.getCreatedAt(), now).toMinutes();
        if (minutes > 2) {
            throw new BusinessException("消息发送超过2分钟，无法撤回");
        }

        int result = chatMessageMapper.recallMessage(messageId, userId);
        return result > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean markMessageRead(Long relationshipId, Long userId) {
        log.info("标记消息已读, relationshipId: {}, userId: {}", relationshipId, userId);

        // 验证关系是否存在且用户有权限
        ChatRelationship relationship = chatRelationshipMapper.selectById(relationshipId);
        if (relationship == null) {
            throw new BusinessException("聊天关系不存在");
        }
        Long user1Id = relationship.getUser1Id();
        Long user2Id = relationship.getUser2Id();
        if ((user1Id == null || !user1Id.equals(userId)) && (user2Id == null || !user2Id.equals(userId))) {
            throw new BusinessException("无权访问该聊天");
        }

        // 标记消息为已读
        int result = chatMessageMapper.markMessagesAsRead(relationshipId, userId);

        // 清空未读消息数
        chatRelationshipMapper.clearUnreadCount(relationshipId, userId);

        return result >= 0;
    }

    @Override
    public ChatRelationshipResponse getChatRelationship(Long relationshipId, Long userId) {
        log.info("获取聊天关系详情, relationshipId: {}, userId: {}", relationshipId, userId);
        return chatRelationshipMapper.selectChatRelationshipDetail(relationshipId, userId);
    }

    @Override
    public UnreadCountResponse getUnreadCount(Long userId) {
        log.info("获取未读消息数, userId: {}", userId);
        Integer total = chatRelationshipMapper.countUnreadMessages(userId);
        if (total == null) {
            total = 0;
        }

        UnreadCountResponse response = new UnreadCountResponse();
        response.setTotal(total);
        response.setCount(total); // 兼容前端
        return response;
    }
}

