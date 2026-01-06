package com.zzy.backend.mapper.student.auth;

import com.zzy.backend.entity.user.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户Mapper接口
 */
@Mapper
public interface UserMapper {

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户实体
     */
    User selectByUsername(@Param("username") String username);

    /**
     * 根据手机号查询用户
     *
     * @param phone 手机号
     * @return 用户实体
     */
    User selectByPhone(@Param("phone") String phone);

    /**
     * 根据邮箱查询用户
     *
     * @param email 邮箱
     * @return 用户实体
     */
    User selectByEmail(@Param("email") String email);

    /**
     * 插入用户
     *
     * @param user 用户实体
     * @return 影响行数
     */
    int insert(User user);

    /**
     * 根据ID查询用户
     *
     * @param id 用户ID
     * @return 用户实体
     */
    User selectById(@Param("id") Long id);

    /**
     * 更新用户最后登录信息
     *
     * @param userId 用户ID
     * @param lastLoginAt 最后登录时间
     * @param lastLoginIp 最后登录IP
     * @return 影响行数
     */
    int updateLastLogin(@Param("userId") Long userId, 
                       @Param("lastLoginAt") java.time.LocalDateTime lastLoginAt,
                       @Param("lastLoginIp") String lastLoginIp);
}

