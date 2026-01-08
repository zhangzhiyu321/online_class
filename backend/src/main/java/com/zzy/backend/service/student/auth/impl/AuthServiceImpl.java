package com.zzy.backend.service.student.auth.impl;

import com.zzy.backend.common.exception.BusinessException;
import com.zzy.backend.dto.request.student.auth.LoginRequest;
import com.zzy.backend.dto.request.student.auth.PhoneLoginRequest;
import com.zzy.backend.dto.request.student.auth.RegisterRequest;
import com.zzy.backend.dto.response.student.auth.LoginResponse;
import com.zzy.backend.entity.user.User;
import com.zzy.backend.mapper.student.auth.UserMapper;
import com.zzy.backend.service.common.SmsService;
import com.zzy.backend.service.student.auth.AuthService;
import com.zzy.backend.common.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 认证服务实现类
 */
@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private SmsService smsService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginResponse login(LoginRequest request, String clientIp) {
        log.info("用户登录, username: {}", request.getUsername());

        // 1. 根据用户名查询用户
        User user = userMapper.selectByUsername(request.getUsername());
        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }

        // 2. 检查账号状态
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BusinessException("账号已被禁用，请联系管理员");
        }

        // 3. 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        // 4. 更新最后登录信息
        LocalDateTime now = LocalDateTime.now();
        userMapper.updateLastLogin(user.getId(), now, clientIp);

        // 5. 生成Token
        String role = String.valueOf(user.getRole());
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), role);

        // 6. 构建响应
        LoginResponse response = new LoginResponse();
        response.setToken(token);

        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setNickname(user.getNickname());
        userInfo.setAvatar(user.getAvatar());
        userInfo.setRole(user.getRole());
        userInfo.setPhone(user.getPhone());
        userInfo.setEmail(user.getEmail());

        response.setUserInfo(userInfo);

        log.info("用户登录成功, userId: {}, username: {}", user.getId(), user.getUsername());
        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean register(RegisterRequest request) {
        log.info("用户注册, username: {}", request.getUsername());

        // 1. 检查用户名是否已存在
        User existingUser = userMapper.selectByUsername(request.getUsername());
        if (existingUser != null) {
            throw new BusinessException("用户名已存在");
        }

        // 2. 如果提供了手机号，检查手机号是否已存在
        if (request.getPhone() != null && !request.getPhone().trim().isEmpty()) {
            User userByPhone = userMapper.selectByPhone(request.getPhone());
            if (userByPhone != null) {
                throw new BusinessException("手机号已被注册");
            }
        }

        // 3. 如果提供了邮箱，检查邮箱是否已存在
        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            User userByEmail = userMapper.selectByEmail(request.getEmail());
            if (userByEmail != null) {
                throw new BusinessException("邮箱已被注册");
            }
        }

        // 4. 创建新用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword())); // 加密密码
        user.setNickname(request.getNickname());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setRole(request.getRole() != null ? request.getRole() : 1); // 默认学生
        user.setStatus(1); // 默认正常状态
        user.setOnlineStatus(0); // 默认离线
        user.setVersion(1);

        LocalDateTime now = LocalDateTime.now();
        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        // 5. 保存用户
        int result = userMapper.insert(user);
        if (result <= 0) {
            throw new BusinessException("注册失败，请稍后重试");
        }

        log.info("用户注册成功, userId: {}, username: {}", user.getId(), user.getUsername());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginResponse phoneLogin(PhoneLoginRequest request, String clientIp) {
        log.info("手机号登录, phone: {}", request.getPhone());

        // 1. 验证短信验证码
        boolean verified = smsService.verifyCode(request.getPhone(), request.getCode());
        if (!verified) {
            throw new BusinessException("验证码错误或已过期");
        }

        // 2. 根据手机号查询用户
        User user = userMapper.selectByPhone(request.getPhone());

        // 3. 如果用户不存在，自动注册
        if (user == null) {
            log.info("手机号未注册，自动创建账号, phone: {}", request.getPhone());
            user = autoRegisterByPhone(request.getPhone());
        }

        // 4. 检查账号状态
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BusinessException("账号已被禁用，请联系管理员");
        }

        // 5. 更新最后登录信息
        LocalDateTime now = LocalDateTime.now();
        userMapper.updateLastLogin(user.getId(), now, clientIp);

        // 6. 生成Token
        String role = String.valueOf(user.getRole());
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), role);

        // 7. 构建响应
        LoginResponse response = new LoginResponse();
        response.setToken(token);

        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setNickname(user.getNickname());
        userInfo.setAvatar(user.getAvatar());
        userInfo.setRole(user.getRole());
        userInfo.setPhone(user.getPhone());
        userInfo.setEmail(user.getEmail());

        response.setUserInfo(userInfo);

        log.info("手机号登录成功, userId: {}, phone: {}", user.getId(), request.getPhone());
        return response;
    }

    /**
     * 根据手机号自动注册用户
     * 
     * @param phone 手机号
     * @return 新创建的用户
     */
    private User autoRegisterByPhone(String phone) {
        // 1. 检查手机号是否已被注册（双重检查）
        User existingUser = userMapper.selectByPhone(phone);
        if (existingUser != null) {
            return existingUser;
        }

        // 2. 生成用户名（手机号_随机字符串）
        String username = "user_" + phone.substring(phone.length() - 4) + "_" + System.currentTimeMillis();

        // 3. 生成随机密码（手机号登录不需要密码，但数据库字段必填）
        String randomPassword = UUID.randomUUID().toString().replace("-", "");

        // 4. 创建新用户
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(randomPassword)); // 加密随机密码
        user.setNickname("用户" + phone.substring(phone.length() - 4)); // 默认昵称：用户+后4位
        user.setPhone(phone);
        user.setRole(1); // 默认学生
        user.setStatus(1); // 默认正常状态
        user.setOnlineStatus(0); // 默认离线
        user.setVersion(1);

        LocalDateTime now = LocalDateTime.now();
        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        // 5. 保存用户
        int result = userMapper.insert(user);
        if (result <= 0) {
            throw new BusinessException("自动注册失败，请稍后重试");
        }

        log.info("自动注册成功, userId: {}, phone: {}, username: {}", user.getId(), phone, username);
        return user;
    }
}

