package com.zzy.backend.service.student.profile.impl;

import com.zzy.backend.mapper.student.auth.UserMapper;
import com.zzy.backend.service.student.profile.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 用户信息服务实现类
 */
@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public void updateAvatar(Long userId, String avatarUrl) {
        userMapper.updateAvatar(userId, avatarUrl);
        log.info("更新用户头像成功, userId: {}, avatarUrl: {}", userId, avatarUrl);
    }
}

