package com.zzy.backend.mapper.student.notification;

import com.zzy.backend.dto.request.student.notification.NotificationListRequest;
import com.zzy.backend.dto.response.student.notification.NotificationDetailResponse;
import com.zzy.backend.dto.response.student.notification.NotificationListItemResponse;
import com.zzy.backend.entity.notification.Notification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 通知Mapper接口
 */
@Mapper
public interface NotificationMapper {

    /**
     * 查询通知列表
     *
     * @param request 查询请求
     * @return 通知列表
     */
    List<NotificationListItemResponse> selectNotificationList(NotificationListRequest request);

    /**
     * 统计通知总数
     *
     * @param request 查询请求
     * @return 总数
     */
    Long countNotificationList(NotificationListRequest request);

    /**
     * 根据ID查询通知详情
     *
     * @param id 通知ID
     * @param userId 用户ID
     * @return 通知详情
     */
    NotificationDetailResponse selectNotificationDetail(@Param("id") Long id, @Param("userId") Long userId);

    /**
     * 根据ID查询通知
     *
     * @param id 通知ID
     * @return 通知实体
     */
    Notification selectById(@Param("id") Long id);

    /**
     * 标记通知为已读
     *
     * @param id 通知ID
     * @param userId 用户ID
     * @return 影响行数
     */
    int markAsRead(@Param("id") Long id, @Param("userId") Long userId);

    /**
     * 标记所有通知为已读
     *
     * @param userId 用户ID
     * @return 影响行数
     */
    int markAllAsRead(@Param("userId") Long userId);

    /**
     * 删除通知（软删除）
     *
     * @param id 通知ID
     * @param userId 用户ID
     * @return 影响行数
     */
    int deleteNotification(@Param("id") Long id, @Param("userId") Long userId);

    /**
     * 统计未读通知数
     *
     * @param userId 用户ID
     * @return 未读通知数
     */
    Integer countUnreadNotifications(@Param("userId") Long userId);
}

