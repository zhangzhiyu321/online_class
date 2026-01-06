package com.zzy.backend.controller.student.appointment;

import com.zzy.backend.common.Result;
import com.zzy.backend.common.constant.ResponseCode;
import com.zzy.backend.common.exception.BusinessException;
import com.zzy.backend.common.page.PageResult;
import com.zzy.backend.dto.request.student.appointment.AppointmentListRequest;
import com.zzy.backend.dto.request.student.appointment.CancelAppointmentRequest;
import com.zzy.backend.dto.request.student.appointment.CreateAppointmentRequest;
import com.zzy.backend.dto.response.student.appointment.AppointmentDetailResponse;
import com.zzy.backend.dto.response.student.appointment.AppointmentListItemResponse;
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

    /**
     * 获取预约列表
     *
     * @param request 查询请求参数
     * @param httpRequest HTTP请求对象
     * @return 预约列表
     */
    @GetMapping("/list")
    @Operation(summary = "获取预约列表", description = "分页查询当前学生的预约列表，支持状态筛选和关键词搜索")
    public Result<PageResult<AppointmentListItemResponse>> getAppointmentList(
            AppointmentListRequest request,
            HttpServletRequest httpRequest) {
        log.info("查询预约列表, request: {}", request);

        // 从请求属性中获取当前登录用户ID
        Long studentId = (Long) httpRequest.getAttribute("userId");
        if (studentId == null) {
            return Result.unauthorized("未登录或Token无效");
        }

        try {
            PageResult<AppointmentListItemResponse> result = appointmentService.getAppointmentList(request, studentId);
            return Result.success(result);
        } catch (Exception e) {
            log.error("查询预约列表失败", e);
            return Result.error("查询预约列表失败，请稍后重试");
        }
    }

    /**
     * 获取预约详情
     *
     * @param id 预约ID
     * @param httpRequest HTTP请求对象
     * @return 预约详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取预约详情", description = "根据预约ID查询预约详细信息")
    public Result<AppointmentDetailResponse> getAppointmentDetail(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        log.info("查询预约详情, id: {}", id);

        // 从请求属性中获取当前登录用户ID
        Long studentId = (Long) httpRequest.getAttribute("userId");
        if (studentId == null) {
            return Result.unauthorized("未登录或Token无效");
        }

        try {
            AppointmentDetailResponse response = appointmentService.getAppointmentDetail(id, studentId);
            return Result.success(response);
        } catch (BusinessException e) {
            log.error("查询预约详情失败: {}", e.getMessage());
            return Result.error(ResponseCode.BUSINESS_ERROR, e.getMessage());
        } catch (Exception e) {
            log.error("查询预约详情失败", e);
            return Result.error("查询预约详情失败，请稍后重试");
        }
    }

    /**
     * 取消预约
     *
     * @param id 预约ID
     * @param request 取消预约请求
     * @param httpRequest HTTP请求对象
     * @return 操作结果
     */
    @PutMapping("/{id}/cancel")
    @Operation(summary = "取消预约", description = "学生取消自己的预约，需要提供取消原因")
    public Result<Void> cancelAppointment(
            @PathVariable Long id,
            @Valid @RequestBody CancelAppointmentRequest request,
            HttpServletRequest httpRequest) {
        log.info("取消预约请求, id: {}, reason: {}", id, request.getReason());

        // 从请求属性中获取当前登录用户ID
        Long studentId = (Long) httpRequest.getAttribute("userId");
        if (studentId == null) {
            return Result.unauthorized("未登录或Token无效");
        }

        try {
            appointmentService.cancelAppointment(id, request, studentId);
            return Result.<Void>success("取消成功", null);
        } catch (BusinessException e) {
            log.error("取消预约失败: {}", e.getMessage());
            return Result.error(ResponseCode.BUSINESS_ERROR, e.getMessage());
        } catch (Exception e) {
            log.error("取消预约失败", e);
            return Result.error("取消预约失败，请稍后重试");
        }
    }
}

