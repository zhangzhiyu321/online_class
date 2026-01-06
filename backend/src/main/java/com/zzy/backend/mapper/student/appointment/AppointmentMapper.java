package com.zzy.backend.mapper.student.appointment;

import com.zzy.backend.entity.student.Appointment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 预约Mapper接口
 */
@Mapper
public interface AppointmentMapper {

    /**
     * 插入预约记录
     *
     * @param appointment 预约实体
     * @return 影响行数
     */
    int insert(Appointment appointment);

    /**
     * 根据订单号查询预约
     *
     * @param orderNo 订单号
     * @return 预约实体
     */
    Appointment selectByOrderNo(@Param("orderNo") String orderNo);

    /**
     * 根据ID查询预约
     *
     * @param id 预约ID
     * @return 预约实体
     */
    Appointment selectById(@Param("id") Long id);
}

