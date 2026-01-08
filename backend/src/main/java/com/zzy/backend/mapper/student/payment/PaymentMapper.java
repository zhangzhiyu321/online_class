package com.zzy.backend.mapper.student.payment;

import com.zzy.backend.dto.request.student.payment.PaymentListRequest;
import com.zzy.backend.dto.response.student.payment.PaymentDetailResponse;
import com.zzy.backend.dto.response.student.payment.PaymentListItemResponse;
import com.zzy.backend.entity.student.Payment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 支付记录Mapper接口
 */
@Mapper
public interface PaymentMapper {

    /**
     * 插入支付记录
     *
     * @param payment 支付实体
     * @return 影响行数
     */
    int insert(Payment payment);

    /**
     * 根据支付单号查询支付记录
     *
     * @param paymentNo 支付单号
     * @return 支付实体
     */
    Payment selectByPaymentNo(@Param("paymentNo") String paymentNo);

    /**
     * 根据ID查询支付记录
     *
     * @param id 支付ID
     * @return 支付实体
     */
    Payment selectById(@Param("id") Long id);

    /**
     * 根据预约ID查询支付记录
     *
     * @param appointmentId 预约ID
     * @return 支付实体
     */
    Payment selectByAppointmentId(@Param("appointmentId") Long appointmentId);

    /**
     * 查询支付列表（分页）
     *
     * @param request 查询请求参数
     * @return 支付列表
     */
    List<PaymentListItemResponse> selectPaymentList(PaymentListRequest request);

    /**
     * 统计支付总数
     *
     * @param request 查询请求参数
     * @return 总数
     */
    Long countPaymentList(PaymentListRequest request);

    /**
     * 查询支付详情
     *
     * @param id 支付ID
     * @param studentId 学生用户ID（用于权限验证）
     * @return 支付详情
     */
    PaymentDetailResponse selectPaymentDetail(@Param("id") Long id, @Param("studentId") Long studentId);

    /**
     * 更新支付凭证信息
     *
     * @param id 支付ID
     * @param transferImage 转账截图URL
     * @param transferTime 转账时间
     * @param transferAccount 转账账号
     * @param status 支付状态
     * @return 影响行数
     */
    int updatePaymentProof(@Param("id") Long id,
                          @Param("transferImage") String transferImage,
                          @Param("transferTime") java.time.LocalDateTime transferTime,
                          @Param("transferAccount") String transferAccount,
                          @Param("status") Integer status);

    /**
     * 更新支付方式
     *
     * @param id 支付ID
     * @param paymentMethod 支付方式
     * @return 影响行数
     */
    int updatePaymentMethod(@Param("id") Long id, @Param("paymentMethod") Integer paymentMethod);

    /**
     * 自动确认支付
     *
     * @param id 支付ID
     * @param status 支付状态（3-已完成）
     * @param confirmedAt 确认时间
     * @param confirmedBy 确认人ID（null表示系统自动确认）
     * @param extra 扩展信息（JSON格式，包含第三方订单号）
     * @return 影响行数
     */
    int autoConfirmPayment(@Param("id") Long id,
                          @Param("status") Integer status,
                          @Param("confirmedAt") java.time.LocalDateTime confirmedAt,
                          @Param("confirmedBy") Long confirmedBy,
                          @Param("extra") String extra);

    /**
     * 查询待支付的在线支付订单（支付宝/微信）
     *
     * @param paymentMethod 支付方式：2-支付宝，3-微信
     * @param limit 查询数量限制
     * @return 支付记录列表
     */
    List<Payment> selectPendingOnlinePayments(@Param("paymentMethod") Integer paymentMethod,
                                             @Param("limit") Integer limit);
}

