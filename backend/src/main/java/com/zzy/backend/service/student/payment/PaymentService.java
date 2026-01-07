package com.zzy.backend.service.student.payment;

import com.zzy.backend.common.page.PageResult;
import com.zzy.backend.dto.request.student.payment.CreatePaymentRequest;
import com.zzy.backend.dto.request.student.payment.PaymentListRequest;
import com.zzy.backend.dto.request.student.payment.UploadPaymentProofRequest;
import com.zzy.backend.dto.response.student.payment.CreatePaymentResponse;
import com.zzy.backend.dto.response.student.payment.PaymentDetailResponse;
import com.zzy.backend.dto.response.student.payment.PaymentListItemResponse;

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
}

