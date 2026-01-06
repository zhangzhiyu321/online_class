package com.zzy.backend.service.student.notification;

import com.zzy.backend.common.page.PageResult;
import com.zzy.backend.dto.request.student.notification.NotificationListRequest;
import com.zzy.backend.dto.response.student.notification.NotificationDetailResponse;
import com.zzy.backend.dto.response.student.notification.NotificationListItemResponse;
import com.zzy.backend.dto.response.student.notification.UnreadCountResponse;

/**
 * 通知服务接口
 */
public interface NotificationService {

    /**
     * 获取通知列表（分页）
     *
     * @param request 查询请求
     * @param userId 用户ID
     * @return 通知列表
     */
    PageResult<NotificationListItemResponse> getNotificationList(NotificationListRequest request, Long userId);

    /**
     * 获取通知详情
     *
     * @param id 通知ID
     * @param userId 用户ID
     * @return 通知详情
     */
    NotificationDetailResponse getNotificationDetail(Long id, Long userId);

    /**
     * 标记通知为已读
     *
     * @param id 通知ID
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean markNotificationRead(Long id, Long userId);

    /**
     * 标记所有通知为已读
     *
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean markAllNotificationsRead(Long userId);

    /**
     * 删除通知
     *
     * @param id 通知ID
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean deleteNotification(Long id, Long userId);

    /**
     * 获取未读通知数
     *
     * @param userId 用户ID
     * @return 未读通知数
     */
    UnreadCountResponse getUnreadCount(Long userId);
}

