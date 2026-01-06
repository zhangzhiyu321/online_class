package com.zzy.backend.dto.response.student.auth;

import lombok.Data;

/**
 * 登录响应DTO
 */
@Data
public class LoginResponse {
    /**
     * Token
     */
    private String token;

    /**
     * 用户信息
     */
    private UserInfo userInfo;

    /**
     * 用户信息内部类
     */
    @Data
    public static class UserInfo {
        /**
         * 用户ID
         */
        private Long id;

        /**
         * 用户名
         */
        private String username;

        /**
         * 昵称
         */
        private String nickname;

        /**
         * 头像URL
         */
        private String avatar;

        /**
         * 角色：1-学生，2-教师，3-管理员
         */
        private Integer role;

        /**
         * 手机号
         */
        private String phone;

        /**
         * 邮箱
         */
        private String email;
    }
}

