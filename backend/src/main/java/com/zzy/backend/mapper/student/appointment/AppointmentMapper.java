package com.zzy.backend.mapper.student.appointment;

import com.zzy.backend.dto.request.student.appointment.AppointmentListRequest;
import com.zzy.backend.dto.response.student.appointment.AppointmentDetailResponse;
import com.zzy.backend.dto.response.student.appointment.AppointmentListItemResponse;
import com.zzy.backend.entity.student.Appointment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

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

    /**
     * 查询预约列表（分页）
     *
     * @param request 查询请求参数
     * @return 预约列表
     */
    List<AppointmentListItemResponse> selectAppointmentList(AppointmentListRequest request);

    /**
     * 统计预约总数
     *
     * @param request 查询请求参数
     * @return 总数
     */
    Long countAppointmentList(AppointmentListRequest request);

    /**
     * 查询预约详情
     *
     * @param id 预约ID
     * @param studentId 学生用户ID（用于权限验证）
     * @return 预约详情
     */
    AppointmentDetailResponse selectAppointmentDetail(@Param("id") Long id, @Param("studentId") Long studentId);

    /**
     * 更新预约状态（取消预约）
     *
     * @param id 预约ID
     * @param status 新状态
     * @param cancelReason 取消原因
     * @param cancelBy 取消人ID
     * @return 影响行数
     */
    int updateAppointmentStatus(@Param("id") Long id,
                                @Param("status") Integer status,
                                @Param("cancelReason") String cancelReason,
                                @Param("cancelBy") Long cancelBy);
}

