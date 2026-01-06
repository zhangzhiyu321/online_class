package com.zzy.backend.controller.student.appointment;

import com.zzy.backend.common.Result;
import com.zzy.backend.common.constant.ResponseCode;
import com.zzy.backend.common.exception.BusinessException;
import com.zzy.backend.dto.request.student.appointment.CreateAppointmentRequest;
import com.zzy.backend.dto.response.student.appointment.CreateAppointmentResponse;
import com.zzy.backend.service.student.appointment.AppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 预约控制器
 */
@Slf4j
@RestController
@RequestMapping("/appointment")
@Tag(name = "预约管理", description = "学生端-预约相关功能")
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    /**
     * 创建预约
     *
     * @param request 创建预约请求
     * @param httpRequest HTTP请求对象
     * @return 创建预约响应
     */
    @PostMapping
    @Operation(summary = "创建预约", description = "学生提交预约申请，等待教师确认")
    public Result<CreateAppointmentResponse> createAppointment(
            @Valid @RequestBody CreateAppointmentRequest request,
            HttpServletRequest httpRequest) {
        log.info("创建预约请求, request: {}", request);

        // 从请求属性中获取当前登录用户ID（由AuthInterceptor设置）
        Long studentId = (Long) httpRequest.getAttribute("userId");
        if (studentId == null) {
            return Result.unauthorized("未登录或Token无效");
        }

        try {
            CreateAppointmentResponse response = appointmentService.createAppointment(request, studentId);
            return Result.success("预约成功，等待教师确认", response);
        } catch (BusinessException e) {
            log.error("创建预约失败: {}", e.getMessage());
            return Result.error(ResponseCode.BUSINESS_ERROR, e.getMessage());
        } catch (Exception e) {
            log.error("创建预约失败", e);
            return Result.error("预约失败，请稍后重试");
        }
    }
}

