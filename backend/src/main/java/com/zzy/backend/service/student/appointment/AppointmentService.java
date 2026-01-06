package com.zzy.backend.service.student.appointment;

import com.zzy.backend.common.page.PageResult;
import com.zzy.backend.dto.request.student.appointment.AppointmentListRequest;
import com.zzy.backend.dto.request.student.appointment.CancelAppointmentRequest;
import com.zzy.backend.dto.request.student.appointment.CreateAppointmentRequest;
import com.zzy.backend.dto.response.student.appointment.AppointmentDetailResponse;
import com.zzy.backend.dto.response.student.appointment.AppointmentListItemResponse;
import com.zzy.backend.dto.response.student.appointment.CreateAppointmentResponse;

/**
 * 预约服务接口
 */
public interface AppointmentService {

    /**
     * 创建预约
     *
     * @param request 创建预约请求
     * @param studentId 学生用户ID
     * @return 创建预约响应
     */
    CreateAppointmentResponse createAppointment(CreateAppointmentRequest request, Long studentId);

    /**
     * 获取预约列表
     *
     * @param request 查询请求
     * @param studentId 学生用户ID
     * @return 分页结果
     */
    PageResult<AppointmentListItemResponse> getAppointmentList(AppointmentListRequest request, Long studentId);

    /**
     * 获取预约详情
     *
     * @param id 预约ID
     * @param studentId 学生用户ID
     * @return 预约详情
     */
    AppointmentDetailResponse getAppointmentDetail(Long id, Long studentId);

    /**
     * 取消预约
     *
     * @param id 预约ID
     * @param request 取消预约请求
     * @param studentId 学生用户ID
     * @return 是否成功
     */
    boolean cancelAppointment(Long id, CancelAppointmentRequest request, Long studentId);
}

