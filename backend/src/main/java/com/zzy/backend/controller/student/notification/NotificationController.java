package com.zzy.backend.controller.student.notification;

import com.zzy.backend.common.Result;
import com.zzy.backend.common.page.PageResult;
import com.zzy.backend.dto.request.student.notification.NotificationListRequest;
import com.zzy.backend.dto.response.student.notification.NotificationDetailResponse;
import com.zzy.backend.dto.response.student.notification.NotificationListItemResponse;
import com.zzy.backend.dto.response.student.notification.UnreadCountResponse;
import com.zzy.backend.service.student.notification.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 通知控制器
 */
@Slf4j
@RestController
@RequestMapping("/notification")
@Tag(name = "通知管理", description = "学生端-通知相关功能")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    /**
     * 获取通知列表（分页）
     */
    @GetMapping("/list")
    @Operation(summary = "获取通知列表", description = "分页查询当前学生的通知列表，支持状态筛选和关键词搜索")
    public Result<PageResult<NotificationListItemResponse>> getNotificationList(
            NotificationListRequest request,
            HttpServletRequest httpRequest) {
        log.info("查询通知列表, request: {}", request);
        Long userId = (Long) httpRequest.getAttribute("userId");
        PageResult<NotificationListItemResponse> result = notificationService.getNotificationList(request, userId);
        return Result.success("获取成功", result);
    }

    /**
     * 获取通知详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取通知详情", description = "根据通知ID查询通知详细信息")
    public Result<NotificationDetailResponse> getNotificationDetail(
            @PathVariable Long id,
            HttpServletRequest request) {
        log.info("查询通知详情, id: {}", id);
        Long userId = (Long) request.getAttribute("userId");
        NotificationDetailResponse response = notificationService.getNotificationDetail(id, userId);
        return Result.success("获取成功", response);
    }

    /**
     * 标记通知为已读
     */
    @PutMapping("/{id}/read")
    @Operation(summary = "标记通知为已读", description = "将指定的通知标记为已读状态")
    public Result<Void> markNotificationRead(
            @PathVariable Long id,
            HttpServletRequest request) {
        log.info("标记通知已读, id: {}", id);
        Long userId = (Long) request.getAttribute("userId");
        boolean success = notificationService.markNotificationRead(id, userId);
        if (success) {
            return Result.success("标记成功", null);
        } else {
            log.error("标记通知已读失败, id: {}", id);
            return Result.error("标记失败");
        }
    }

    /**
     * 标记所有通知为已读
     */
    @PutMapping("/read-all")
    @Operation(summary = "标记所有通知为已读", description = "将当前学生的所有未读通知标记为已读状态")
    public Result<Void> markAllNotificationsRead(HttpServletRequest request) {
        log.info("标记所有通知已读");
        Long userId = (Long) request.getAttribute("userId");
        boolean success = notificationService.markAllNotificationsRead(userId);
        if (success) {
            return Result.success("标记成功", null);
        } else {
            log.error("标记所有通知已读失败");
            return Result.error("标记失败");
        }
    }

    /**
     * 获取未读通知数量
     */
    @GetMapping("/unread-count")
    @Operation(summary = "获取未读通知数量", description = "获取当前学生的未读通知总数")
    public Result<UnreadCountResponse> getUnreadCount(HttpServletRequest request) {
        log.info("查询未读通知数量");
        Long userId = (Long) request.getAttribute("userId");
        UnreadCountResponse response = notificationService.getUnreadCount(userId);
        return Result.success("获取成功", response);
    }

    /**
     * 删除通知
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除通知", description = "删除指定的通知记录")
    public Result<Void> deleteNotification(
            @PathVariable Long id,
            HttpServletRequest request) {
        log.info("删除通知, id: {}", id);
        Long userId = (Long) request.getAttribute("userId");
        boolean success = notificationService.deleteNotification(id, userId);
        if (success) {
            return Result.success("删除成功", null);
        } else {
            log.error("删除通知失败, id: {}", id);
            return Result.error("删除失败");
        }
    }
}

