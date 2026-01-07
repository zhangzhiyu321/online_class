package com.zzy.backend.service.student.profile;

/**
 * 用户信息服务接口
 */
public interface UserService {
    /**
     * 更新用户头像
     *
     * @param userId 用户ID
     * @param avatarUrl 头像URL
     */
    void updateAvatar(Long userId, String avatarUrl);
}

