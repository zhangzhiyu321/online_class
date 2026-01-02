package com.zzy.backend.common.enums;

import lombok.Getter;

/**
 * 用户角色枚举
 */
@Getter
public enum UserRole {
    
    /**
     * 学生
     */
    STUDENT("student", "学生"),
    
    /**
     * 教师
     */
    TEACHER("teacher", "教师"),
    
    /**
     * 管理员
     */
    ADMIN("admin", "管理员");
    
    private final String code;
    private final String description;
    
    UserRole(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    /**
     * 根据代码获取枚举
     */
    public static UserRole getByCode(String code) {
        for (UserRole role : values()) {
            if (role.getCode().equals(code)) {
                return role;
            }
        }
        return null;
    }
}

