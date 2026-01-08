package com.zzy.backend.controller.student.chat;

import com.zzy.backend.common.Result;
import com.zzy.backend.dto.request.student.chat.SendMessageRequest;
import com.zzy.backend.dto.response.student.chat.ChatListItemResponse;
import com.zzy.backend.dto.response.student.chat.ChatRelationshipResponse;
import com.zzy.backend.dto.response.student.chat.MessageResponse;
import com.zzy.backend.dto.response.student.chat.UnreadCountResponse;
import com.zzy.backend.service.student.chat.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "聊天管理", description = "学生端-聊天相关功能")
public class ChatController {

    @Autowired
    private ChatService chatService;

    /**
     * 获取聊天列表
     */
    @GetMapping("/relationships")
    @Operation(summary = "获取聊天列表", description = "获取当前学生的所有聊天会话列表")
    public Result<List<ChatListItemResponse>> getChatList(HttpServletRequest request) {
        log.info("查询聊天列表");
        Long userId = (Long) request.getAttribute("userId");
        List<ChatListItemResponse> list = chatService.getChatList(userId);
        return Result.success("获取成功", list);
    }

    /**
     * 获取聊天消息列表（分页）
     */
    @GetMapping("/messages/{relationshipId}")
    @Operation(summary = "获取聊天消息列表", description = "分页获取指定聊天关系的消息列表")
    public Result<List<MessageResponse>> getChatMessages(
            @PathVariable Long relationshipId,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer pageSize,
            HttpServletRequest request) {
        log.info("查询聊天消息列表, relationshipId: {}, page: {}, pageSize: {}", relationshipId, page, pageSize);
        Long userId = (Long) request.getAttribute("userId");
        List<MessageResponse> messages = chatService.getChatMessages(relationshipId, userId, page, pageSize);
        return Result.success("获取成功", messages);
    }

    /**
     * 发送消息
     */
    @PostMapping("/message")
    @Operation(summary = "发送消息", description = "向指定聊天关系发送消息，支持文本、图片、文件等类型")
    public Result<MessageResponse> sendMessage(
            @Valid @RequestBody SendMessageRequest request,
            HttpServletRequest httpRequest) {
        log.info("发送消息请求, request: {}", request);
        Long senderId = (Long) httpRequest.getAttribute("userId");
        MessageResponse response = chatService.sendMessage(request, senderId);
        return Result.success("发送成功", response);
    }

    /**
     * 撤回消息
     */
    @PutMapping("/message/{messageId}/recall")
    @Operation(summary = "撤回消息", description = "撤回自己发送的消息（仅限2分钟内）")
    public Result<Void> recallMessage(
            @PathVariable Long messageId,
            HttpServletRequest request) {
        log.info("撤回消息, messageId: {}", messageId);
        Long userId = (Long) request.getAttribute("userId");
        boolean success = chatService.recallMessage(messageId, userId);
        if (success) {
            return Result.success("撤回成功", null);
        } else {
            log.error("撤回消息失败, messageId: {}", messageId);
            return Result.error("撤回失败");
        }
    }

    /**
     * 标记消息已读
     */
    @PutMapping("/relationship/{relationshipId}/read")
    @Operation(summary = "标记消息已读", description = "将指定聊天关系的所有未读消息标记为已读")
    public Result<Void> markMessageRead(
            @PathVariable Long relationshipId,
            HttpServletRequest request) {
        log.info("标记消息已读, relationshipId: {}", relationshipId);
        Long userId = (Long) request.getAttribute("userId");
        boolean success = chatService.markMessageRead(relationshipId, userId);
        if (success) {
            return Result.success("标记成功", null);
        } else {
            log.error("标记消息已读失败, relationshipId: {}", relationshipId);
            return Result.error("标记失败");
        }
    }

    /**
     * 获取聊天关系详情
     */
    @GetMapping("/relationship/{relationshipId}")
    @Operation(summary = "获取聊天关系详情", description = "查询指定聊天关系的详细信息，包括对方信息和最新消息")
    public Result<ChatRelationshipResponse> getChatRelationship(
            @PathVariable Long relationshipId,
            HttpServletRequest request) {
        log.info("查询聊天关系详情, relationshipId: {}", relationshipId);
        Long userId = (Long) request.getAttribute("userId");
        ChatRelationshipResponse response = chatService.getChatRelationship(relationshipId, userId);
        return Result.success("获取成功", response);
    }

    /**
     * 获取未读消息数
     */
    @GetMapping("/unread-count")
    @Operation(summary = "获取未读消息数", description = "获取当前学生的所有未读消息总数")
    public Result<UnreadCountResponse> getUnreadCount(HttpServletRequest request) {
        log.info("查询未读消息数");
        Long userId = (Long) request.getAttribute("userId");
        UnreadCountResponse response = chatService.getUnreadCount(userId);
        return Result.success("获取成功", response);
    }
}

