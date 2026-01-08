package com.zzy.backend.service.student.payment;

import com.zzy.backend.common.page.PageResult;
import com.zzy.backend.dto.request.student.payment.CreatePaymentRequest;
import com.zzy.backend.dto.request.student.payment.PaymentListRequest;
import com.zzy.backend.dto.request.student.payment.UploadPaymentProofRequest;
import com.zzy.backend.dto.response.student.payment.CreatePaymentResponse;
import com.zzy.backend.dto.response.student.payment.PaymentDetailResponse;
import com.zzy.backend.dto.response.student.payment.PaymentListItemResponse;
import com.zzy.backend.entity.student.Payment;

import java.util.List;

/**
 * 支付服务接口
 */
public interface PaymentService {

    /**
     * 创建支付记录
     *
     * @param request 创建支付请求
     * @param studentId 学生用户ID
     * @return 创建支付响应
     */
    CreatePaymentResponse createPayment(CreatePaymentRequest request, Long studentId);

    /**
     * 获取支付列表
     *
     * @param request 查询请求
     * @param studentId 学生用户ID
     * @return 分页结果
     */
    PageResult<PaymentListItemResponse> getPaymentList(PaymentListRequest request, Long studentId);

    /**
     * 获取支付详情
     *
     * @param id 支付ID
     * @param studentId 学生用户ID
     * @return 支付详情
     */
    PaymentDetailResponse getPaymentDetail(Long id, Long studentId);

    /**
     * 上传支付凭证
     *
     * @param id 支付ID
     * @param request 上传凭证请求
     * @param studentId 学生用户ID
     * @return 是否成功
     */
    boolean uploadPaymentProof(Long id, UploadPaymentProofRequest request, Long studentId);

    /**
     * 更新支付方式
     *
     * @param id 支付ID
     * @param paymentMethod 支付方式
     * @param studentId 学生用户ID
     * @return 是否成功
     */
    boolean updatePaymentMethod(Long id, Integer paymentMethod, Long studentId);

    /**
     * 自动确认支付（系统自动确认，用于支付宝/微信支付回调）
     *
     * @param paymentNo 支付单号
     * @param thirdPartyOrderNo 第三方订单号（支付宝/微信订单号）
     * @param paymentMethod 支付方式：2-支付宝，3-微信
     * @return 是否成功
     */
    boolean autoConfirmPayment(String paymentNo, String thirdPartyOrderNo, Integer paymentMethod);

    /**
     * 查询支付状态（用于定时任务轮询和前端查询）
     * 
     * @param paymentNo 支付单号
     * @return 支付记录详情
     */
    PaymentDetailResponse queryPaymentStatus(String paymentNo);

    /**
     * 查询待支付的支付宝/微信订单（用于定时任务）
     * 
     * @param paymentMethod 支付方式：2-支付宝，3-微信
     * @param limit 查询数量限制
     * @return 支付记录列表
     */
    List<Payment> listPendingOnlinePayments(Integer paymentMethod, Integer limit);
}

