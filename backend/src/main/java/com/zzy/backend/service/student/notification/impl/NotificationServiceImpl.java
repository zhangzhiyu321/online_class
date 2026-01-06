package com.zzy.backend.service.student.notification.impl;

import com.zzy.backend.common.exception.BusinessException;
import com.zzy.backend.common.page.PageResult;
import com.zzy.backend.dto.request.student.notification.NotificationListRequest;
import com.zzy.backend.dto.response.student.notification.NotificationDetailResponse;
import com.zzy.backend.dto.response.student.notification.NotificationListItemResponse;
import com.zzy.backend.dto.response.student.notification.UnreadCountResponse;
import com.zzy.backend.entity.notification.Notification;
import com.zzy.backend.mapper.student.notification.NotificationMapper;
import com.zzy.backend.service.student.notification.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 通知服务实现类
 */
@Slf4j
@Service
public class NotificationServiceImpl implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);

    @Autowired
    private NotificationMapper notificationMapper;

    @Override
    public PageResult<NotificationListItemResponse> getNotificationList(NotificationListRequest request, Long userId) {
        log.info("查询通知列表, userId: {}, request: {}", userId, request);

        // 设置用户ID
        request.setUserId(userId);

        // 验证并修正分页参数
        request.validate();

        // 查询通知列表
        List<NotificationListItemResponse> list = notificationMapper.selectNotificationList(request);

        // 统计总数
        Long total = notificationMapper.countNotificationList(request);

        // 构建分页结果
        return PageResult.of(list, total, request.getPage(), request.getPageSize());
    }

    @Override
    public NotificationDetailResponse getNotificationDetail(Long id, Long userId) {
        log.info("查询通知详情, id: {}, userId: {}", id, userId);

        NotificationDetailResponse detail = notificationMapper.selectNotificationDetail(id, userId);
        if (detail == null) {
            throw new BusinessException("通知不存在或无权限访问");
        }

        return detail;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean markNotificationRead(Long id, Long userId) {
        log.info("标记通知为已读, id: {}, userId: {}", id, userId);

        // 验证通知是否存在且属于该用户
        Notification notification = notificationMapper.selectById(id);
        if (notification == null) {
            throw new BusinessException("通知不存在");
        }
        if (!notification.getUserId().equals(userId)) {
            throw new BusinessException("无权访问该通知");
        }

        int result = notificationMapper.markAsRead(id, userId);
        return result > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean markAllNotificationsRead(Long userId) {
        log.info("标记所有通知为已读, userId: {}", userId);

        int result = notificationMapper.markAllAsRead(userId);
        return result >= 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteNotification(Long id, Long userId) {
        log.info("删除通知, id: {}, userId: {}", id, userId);

        // 验证通知是否存在且属于该用户
        Notification notification = notificationMapper.selectById(id);
        if (notification == null) {
            throw new BusinessException("通知不存在");
        }
        if (!notification.getUserId().equals(userId)) {
            throw new BusinessException("无权删除该通知");
        }

        int result = notificationMapper.deleteNotification(id, userId);
        return result > 0;
    }

    @Override
    public UnreadCountResponse getUnreadCount(Long userId) {
        log.info("获取未读通知数, userId: {}", userId);

        Integer count = notificationMapper.countUnreadNotifications(userId);
        if (count == null) {
            count = 0;
        }

        UnreadCountResponse response = new UnreadCountResponse();
        response.setCount(count);
        return response;
    }
}

