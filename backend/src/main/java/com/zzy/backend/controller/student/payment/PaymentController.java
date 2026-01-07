package com.zzy.backend.controller.student.payment;

import com.zzy.backend.common.Result;
import com.zzy.backend.common.constant.ResponseCode;
import com.zzy.backend.common.exception.BusinessException;
import com.zzy.backend.common.page.PageResult;
import com.zzy.backend.dto.request.student.payment.CreatePaymentRequest;
import com.zzy.backend.dto.request.student.payment.PaymentListRequest;
import com.zzy.backend.dto.request.student.payment.UploadPaymentProofRequest;
import com.zzy.backend.dto.response.student.payment.CreatePaymentResponse;
import com.zzy.backend.dto.response.student.payment.PaymentDetailResponse;
import com.zzy.backend.dto.response.student.payment.PaymentListItemResponse;
import com.zzy.backend.service.student.payment.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 支付控制器
 */
@Slf4j
@RestController
@RequestMapping("/payment")
@Tag(name = "支付管理", description = "学生端-支付相关功能")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    /**
     * 创建支付记录
     *
     * @param request 创建支付请求
     * @param httpRequest HTTP请求对象
     * @return 创建支付响应
     */
    @PostMapping
    @Operation(summary = "创建支付记录", description = "根据预约ID创建支付记录，支持选择支付方式")
    public Result<CreatePaymentResponse> createPayment(
            @Valid @RequestBody CreatePaymentRequest request,
            HttpServletRequest httpRequest) {
        log.info("创建支付记录请求, request: {}", request);

        // 从请求属性中获取当前登录用户ID
        Long studentId = (Long) httpRequest.getAttribute("userId");
        if (studentId == null) {
            return Result.unauthorized("未登录或Token无效");
        }

        try {
            CreatePaymentResponse response = paymentService.createPayment(request, studentId);
            return Result.success("支付订单创建成功", response);
        } catch (BusinessException e) {
            log.error("创建支付记录失败: {}", e.getMessage());
            return Result.error(ResponseCode.BUSINESS_ERROR, e.getMessage());
        } catch (Exception e) {
            log.error("创建支付记录失败", e);
            return Result.error("创建支付记录失败，请稍后重试");
        }
    }

    /**
     * 获取支付列表
     *
     * @param request 查询请求参数
     * @param httpRequest HTTP请求对象
     * @return 支付列表
     */
    @GetMapping("/list")
    @Operation(summary = "获取支付记录列表", description = "分页查询当前学生的支付记录列表，支持状态筛选和关键词搜索")
    public Result<PageResult<PaymentListItemResponse>> getPaymentList(
            PaymentListRequest request,
            HttpServletRequest httpRequest) {
        log.info("查询支付列表, request: {}", request);

        // 从请求属性中获取当前登录用户ID
        Long studentId = (Long) httpRequest.getAttribute("userId");
        if (studentId == null) {
            return Result.unauthorized("未登录或Token无效");
        }

        try {
            PageResult<PaymentListItemResponse> result = paymentService.getPaymentList(request, studentId);
            return Result.success(result);
        } catch (Exception e) {
            log.error("查询支付列表失败", e);
            return Result.error("查询支付列表失败，请稍后重试");
        }
    }

    /**
     * 获取支付详情
     *
     * @param id 支付ID
     * @param httpRequest HTTP请求对象
     * @return 支付详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取支付详情", description = "根据支付ID查询支付详细信息")
    public Result<PaymentDetailResponse> getPaymentDetail(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        log.info("查询支付详情, id: {}", id);

        // 从请求属性中获取当前登录用户ID
        Long studentId = (Long) httpRequest.getAttribute("userId");
        if (studentId == null) {
            return Result.unauthorized("未登录或Token无效");
        }

        try {
            PaymentDetailResponse response = paymentService.getPaymentDetail(id, studentId);
            return Result.success(response);
        } catch (BusinessException e) {
            log.error("查询支付详情失败: {}", e.getMessage());
            return Result.error(ResponseCode.BUSINESS_ERROR, e.getMessage());
        } catch (Exception e) {
            log.error("查询支付详情失败", e);
            return Result.error("查询支付详情失败，请稍后重试");
        }
    }

    /**
     * 上传支付凭证
     *
     * @param id 支付ID
     * @param request 上传凭证请求
     * @param httpRequest HTTP请求对象
     * @return 操作结果
     */
    @PostMapping("/{id}/proof")
    @Operation(summary = "上传支付凭证", description = "学生上传转账截图、转账金额和转账时间等信息")
    public Result<Void> uploadPaymentProof(
            @PathVariable Long id,
            @Valid @RequestBody UploadPaymentProofRequest request,
            HttpServletRequest httpRequest) {
        log.info("上传支付凭证请求, id: {}, request: {}", id, request);

        // 从请求属性中获取当前登录用户ID
        Long studentId = (Long) httpRequest.getAttribute("userId");
        if (studentId == null) {
            return Result.unauthorized("未登录或Token无效");
        }

        try {
            paymentService.uploadPaymentProof(id, request, studentId);
            return Result.<Void>success("支付凭证上传成功，等待教师确认", null);
        } catch (BusinessException e) {
            log.error("上传支付凭证失败: {}", e.getMessage());
            return Result.error(ResponseCode.BUSINESS_ERROR, e.getMessage());
        } catch (Exception e) {
            log.error("上传支付凭证失败", e);
            return Result.error("上传支付凭证失败，请稍后重试");
        }
    }

    /**
     * 更新支付方式
     *
     * @param id 支付ID
     * @param paymentMethod 支付方式
     * @param httpRequest HTTP请求对象
     * @return 操作结果
     */
    @PutMapping("/{id}/method")
    @Operation(summary = "更新支付方式", description = "更新支付记录的支付方式（仅限待支付状态）")
    public Result<Void> updatePaymentMethod(
            @PathVariable Long id,
            @RequestParam Integer paymentMethod,
            HttpServletRequest httpRequest) {
        log.info("更新支付方式请求, id: {}, paymentMethod: {}", id, paymentMethod);

        // 从请求属性中获取当前登录用户ID
        Long studentId = (Long) httpRequest.getAttribute("userId");
        if (studentId == null) {
            return Result.unauthorized("未登录或Token无效");
        }

        try {
            paymentService.updatePaymentMethod(id, paymentMethod, studentId);
            return Result.<Void>success("支付方式更新成功", null);
        } catch (BusinessException e) {
            log.error("更新支付方式失败: {}", e.getMessage());
            return Result.error(ResponseCode.BUSINESS_ERROR, e.getMessage());
        } catch (Exception e) {
            log.error("更新支付方式失败", e);
            return Result.error("更新支付方式失败，请稍后重试");
        }
    }
}

