package com.zzy.backend.service.student.appointment;

import com.zzy.backend.dto.request.student.appointment.CreateAppointmentRequest;
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
}

