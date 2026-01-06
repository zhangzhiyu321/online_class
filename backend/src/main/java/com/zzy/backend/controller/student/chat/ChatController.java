package com.zzy.backend.controller.student.chat;

import com.zzy.backend.common.Result;
import com.zzy.backend.dto.request.student.chat.SendMessageRequest;
import com.zzy.backend.dto.response.student.chat.ChatListItemResponse;
import com.zzy.backend.dto.response.student.chat.ChatRelationshipResponse;
import com.zzy.backend.dto.response.student.chat.MessageResponse;
import com.zzy.backend.dto.response.student.chat.UnreadCountResponse;
import com.zzy.backend.service.student.chat.ChatService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 聊天控制器
 */
@Slf4j
@RestController
@RequestMapping("/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    /**
     * 获取聊天列表
     */
    @GetMapping("/relationships")
    public Result<List<ChatListItemResponse>> getChatList(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<ChatListItemResponse> list = chatService.getChatList(userId);
        return Result.success("获取成功", list);
    }

    /**
     * 获取聊天消息列表（分页）
     */
    @GetMapping("/messages/{relationshipId}")
    public Result<List<MessageResponse>> getChatMessages(
            @PathVariable Long relationshipId,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer pageSize,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<MessageResponse> messages = chatService.getChatMessages(relationshipId, userId, page, pageSize);
        return Result.success("获取成功", messages);
    }

    /**
     * 发送消息
     */
    @PostMapping("/message")
    public Result<MessageResponse> sendMessage(
            @Valid @RequestBody SendMessageRequest request,
            HttpServletRequest httpRequest) {
        Long senderId = (Long) httpRequest.getAttribute("userId");
        MessageResponse response = chatService.sendMessage(request, senderId);
        return Result.success("发送成功", response);
    }

    /**
     * 撤回消息
     */
    @PutMapping("/message/{messageId}/recall")
    public Result<Void> recallMessage(
            @PathVariable Long messageId,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        boolean success = chatService.recallMessage(messageId, userId);
        if (success) {
            return Result.success("撤回成功", null);
        } else {
            return Result.error("撤回失败");
        }
    }

    /**
     * 标记消息已读
     */
    @PutMapping("/relationship/{relationshipId}/read")
    public Result<Void> markMessageRead(
            @PathVariable Long relationshipId,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        boolean success = chatService.markMessageRead(relationshipId, userId);
        if (success) {
            return Result.success("标记成功", null);
        } else {
            return Result.error("标记失败");
        }
    }

    /**
     * 获取聊天关系详情
     */
    @GetMapping("/relationship/{relationshipId}")
    public Result<ChatRelationshipResponse> getChatRelationship(
            @PathVariable Long relationshipId,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        ChatRelationshipResponse response = chatService.getChatRelationship(relationshipId, userId);
        return Result.success("获取成功", response);
    }

    /**
     * 获取未读消息数
     */
    @GetMapping("/unread-count")
    public Result<UnreadCountResponse> getUnreadCount(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        UnreadCountResponse response = chatService.getUnreadCount(userId);
        return Result.success("获取成功", response);
    }
}

