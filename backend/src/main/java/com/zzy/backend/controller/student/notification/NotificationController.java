package com.zzy.backend.controller.student.notification;

import com.zzy.backend.common.Result;
import com.zzy.backend.common.page.PageResult;
import com.zzy.backend.dto.request.student.notification.NotificationListRequest;
import com.zzy.backend.dto.response.student.notification.NotificationDetailResponse;
import com.zzy.backend.dto.response.student.notification.NotificationListItemResponse;
import com.zzy.backend.dto.response.student.notification.UnreadCountResponse;
import com.zzy.backend.service.student.notification.NotificationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 通知控制器
 */
@RestController
@RequestMapping("/notification")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    /**
     * 获取通知列表（分页）
     */
    @GetMapping("/list")
    public Result<PageResult<NotificationListItemResponse>> getNotificationList(
            NotificationListRequest request,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        PageResult<NotificationListItemResponse> result = notificationService.getNotificationList(request, userId);
        return Result.success("获取成功", result);
    }

    /**
     * 获取通知详情
     */
    @GetMapping("/{id}")
    public Result<NotificationDetailResponse> getNotificationDetail(
            @PathVariable Long id,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        NotificationDetailResponse response = notificationService.getNotificationDetail(id, userId);
        return Result.success("获取成功", response);
    }

    /**
     * 标记通知为已读
     */
    @PutMapping("/{id}/read")
    public Result<Void> markNotificationRead(
            @PathVariable Long id,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        boolean success = notificationService.markNotificationRead(id, userId);
        if (success) {
            return Result.success("标记成功", null);
        } else {
            return Result.error("标记失败");
        }
    }

    /**
     * 标记所有通知为已读
     */
    @PutMapping("/read-all")
    public Result<Void> markAllNotificationsRead(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        boolean success = notificationService.markAllNotificationsRead(userId);
        if (success) {
            return Result.success("标记成功", null);
        } else {
            return Result.error("标记失败");
        }
    }

    /**
     * 获取未读通知数量
     */
    @GetMapping("/unread-count")
    public Result<UnreadCountResponse> getUnreadCount(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        UnreadCountResponse response = notificationService.getUnreadCount(userId);
        return Result.success("获取成功", response);
    }

    /**
     * 删除通知
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteNotification(
            @PathVariable Long id,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        boolean success = notificationService.deleteNotification(id, userId);
        if (success) {
            return Result.success("删除成功", null);
        } else {
            return Result.error("删除失败");
        }
    }
}

