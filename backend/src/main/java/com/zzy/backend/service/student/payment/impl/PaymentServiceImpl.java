package com.zzy.backend.service.student.payment.impl;

import com.zzy.backend.common.exception.BusinessException;
import com.zzy.backend.common.page.PageResult;
import com.zzy.backend.common.util.OrderNoGenerator;
import com.zzy.backend.dto.request.student.payment.CreatePaymentRequest;
import com.zzy.backend.dto.request.student.payment.PaymentListRequest;
import com.zzy.backend.dto.request.student.payment.UploadPaymentProofRequest;
import com.zzy.backend.dto.response.student.payment.CreatePaymentResponse;
import com.zzy.backend.dto.response.student.payment.PaymentDetailResponse;
import com.zzy.backend.dto.response.student.payment.PaymentListItemResponse;
import com.zzy.backend.entity.student.Appointment;
import com.zzy.backend.entity.student.Payment;
import com.zzy.backend.mapper.student.appointment.AppointmentMapper;
import com.zzy.backend.mapper.student.payment.PaymentMapper;
import com.zzy.backend.mapper.student.teacher.TeacherMapper;
import com.zzy.backend.service.student.payment.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 支付服务实现类
 */
@Slf4j
@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private PaymentMapper paymentMapper;

    @Autowired
    private AppointmentMapper appointmentMapper;

    @Autowired
    private TeacherMapper teacherMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CreatePaymentResponse createPayment(CreatePaymentRequest request, Long studentId) {
        log.info("创建支付记录, studentId: {}, request: {}", studentId, request);

        // 1. 查询预约记录
        Appointment appointment = appointmentMapper.selectById(request.getAppointmentId());
        if (appointment == null) {
            throw new BusinessException("预约记录不存在");
        }

        // 2. 验证权限（只能为自己的预约创建支付记录）
        if (!appointment.getStudentId().equals(studentId)) {
            throw new BusinessException("无权限为该预约创建支付记录");
        }

        // 3. 验证预约状态（只有已完成的预约才能创建支付记录）
        if (appointment.getStatus() != 3) {
            throw new BusinessException("只有已完成的预约才能创建支付记录，当前状态：" + getAppointmentStatusText(appointment.getStatus()));
        }

        // 4. 检查是否已经存在支付记录
        Payment existingPayment = paymentMapper.selectByAppointmentId(request.getAppointmentId());
        if (existingPayment != null) {
            // 如果已存在，返回现有支付记录信息
            log.info("支付记录已存在, paymentId: {}, paymentNo: {}", existingPayment.getId(), existingPayment.getPaymentNo());
            return buildCreatePaymentResponse(existingPayment, appointment);
        }

        // 5. 生成支付单号（循环生成直到不重复，理论上不会重复，但为了安全起见）
        String paymentNo;
        Payment existingByNo;
        int maxRetries = 10; // 最多重试10次，避免无限循环
        int retryCount = 0;
        do {
            paymentNo = OrderNoGenerator.generatePaymentOrderNo();
            existingByNo = paymentMapper.selectByPaymentNo(paymentNo);
            retryCount++;
            if (retryCount >= maxRetries) {
                throw new BusinessException("生成支付单号失败，请稍后重试");
            }
        } while (existingByNo != null);

        // 7. 构建支付实体
        Payment payment = new Payment();
        payment.setPaymentNo(paymentNo);
        payment.setAppointmentId(request.getAppointmentId());
        payment.setStudentId(studentId);
        payment.setTeacherId(appointment.getTeacherId());
        payment.setAmount(appointment.getTotalAmount());
        payment.setPaymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : 1);
        payment.setStatus(1); // 待支付
        payment.setVersion(1);

        LocalDateTime now = LocalDateTime.now();
        payment.setCreatedAt(now);
        payment.setUpdatedAt(now);

        // 8. 保存支付记录
        int result = paymentMapper.insert(payment);
        if (result <= 0) {
            throw new BusinessException("创建支付记录失败");
        }

        log.info("支付记录创建成功, paymentNo: {}, paymentId: {}", paymentNo, payment.getId());

        // 9. 构建响应
        return buildCreatePaymentResponse(payment, appointment);
    }

    /**
     * 构建创建支付响应
     */
    private CreatePaymentResponse buildCreatePaymentResponse(Payment payment, Appointment appointment) {
        CreatePaymentResponse response = new CreatePaymentResponse();
        response.setId(payment.getId());
        response.setPaymentNo(payment.getPaymentNo());
        response.setAppointmentId(payment.getAppointmentId());
        response.setTeacherId(payment.getTeacherId());
        response.setAmount(payment.getAmount());
        response.setPaymentMethod(payment.getPaymentMethod());
        response.setStatus(payment.getStatus());
        response.setAppointmentDate(appointment.getAppointmentDate());

        // 查询教师信息（获取银行账号信息或二维码）
        // 根据支付方式返回不同的信息
        // 这里从PaymentMapper中查询教师信息（包含银行信息）
        PaymentDetailResponse detail = paymentMapper.selectPaymentDetail(payment.getId(), payment.getStudentId());
        if (detail != null) {
            response.setTeacherName(detail.getTeacherName());
            response.setBankName(detail.getBankName());
            response.setBankAccount(detail.getBankAccount());
            response.setAccountHolder(detail.getAccountHolder());
            
            // 如果是支付宝或微信，设置二维码URL（目前是占位符，后续可以集成真实支付）
            if (payment.getPaymentMethod() == 2) {
                // 支付宝二维码（占位符）
                response.setAlipayQrCode("https://example.com/qrcode/alipay/" + payment.getPaymentNo());
            } else if (payment.getPaymentMethod() == 3) {
                // 微信二维码（占位符）
                response.setWechatQrCode("https://example.com/qrcode/wechat/" + payment.getPaymentNo());
            }
        }

        return response;
    }


    /**
     * 获取预约状态文本
     */
    private String getAppointmentStatusText(Integer status) {
        switch (status) {
            case 1:
                return "待确认";
            case 2:
                return "已确认";
            case 3:
                return "已完成";
            case 4:
                return "已取消";
            default:
                return "未知";
        }
    }

    @Override
    public PageResult<PaymentListItemResponse> getPaymentList(PaymentListRequest request, Long studentId) {
        log.info("查询支付列表, studentId: {}, request: {}", studentId, request);

        // 设置学生ID
        request.setStudentId(studentId);

        // 验证并修正分页参数
        request.validate();

        // 查询支付列表
        List<PaymentListItemResponse> list = paymentMapper.selectPaymentList(request);

        // 统计总数
        Long total = paymentMapper.countPaymentList(request);

        // 构建分页结果
        return PageResult.of(list, total, request.getPage(), request.getPageSize());
    }

    @Override
    public PaymentDetailResponse getPaymentDetail(Long id, Long studentId) {
        log.info("查询支付详情, id: {}, studentId: {}", id, studentId);

        PaymentDetailResponse detail = paymentMapper.selectPaymentDetail(id, studentId);
        if (detail == null) {
            throw new BusinessException("支付记录不存在或无权限访问");
        }

        return detail;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean uploadPaymentProof(Long id, UploadPaymentProofRequest request, Long studentId) {
        log.info("上传支付凭证, id: {}, studentId: {}, request: {}", id, studentId, request);

        // 1. 查询支付记录
        Payment payment = paymentMapper.selectById(id);
        if (payment == null) {
            throw new BusinessException("支付记录不存在");
        }

        // 2. 验证权限（只能上传自己的支付凭证）
        if (!payment.getStudentId().equals(studentId)) {
            throw new BusinessException("无权限操作该支付记录");
        }

        // 3. 验证状态（只能上传待支付状态的支付凭证）
        if (payment.getStatus() != 1) {
            throw new BusinessException("当前状态不允许上传凭证，当前状态：" + getStatusText(payment.getStatus()));
        }

        // 4. 验证转账金额是否匹配（允许0.01的误差）
        double paymentAmount = payment.getAmount().doubleValue();
        double transferAmount = request.getTransferAmount().doubleValue();
        if (Math.abs(paymentAmount - transferAmount) > 0.01) {
            throw new BusinessException("转账金额与支付金额不匹配，支付金额：" + String.format("%.2f", paymentAmount) + "元，转账金额：" + String.format("%.2f", transferAmount) + "元");
        }

        // 5. 更新支付凭证信息，状态改为待确认
        // 注意：transferAmount暂时不存储到数据库，因为数据库schema中没有该字段
        // 如果需要存储，可以添加到extra字段中，或者修改数据库schema添加transfer_amount字段
        int result = paymentMapper.updatePaymentProof(
                id,
                request.getTransferImage(),
                request.getTransferTime(),
                request.getTransferAccount(),
                2 // 待确认状态
        );

        if (result <= 0) {
            throw new BusinessException("上传支付凭证失败");
        }

        log.info("支付凭证上传成功, id: {}, paymentNo: {}", id, payment.getPaymentNo());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updatePaymentMethod(Long id, Integer paymentMethod, Long studentId) {
        log.info("更新支付方式, id: {}, paymentMethod: {}, studentId: {}", id, paymentMethod, studentId);

        // 1. 查询支付记录
        Payment payment = paymentMapper.selectById(id);
        if (payment == null) {
            throw new BusinessException("支付记录不存在");
        }

        // 2. 验证权限（只能更新自己的支付记录）
        if (!payment.getStudentId().equals(studentId)) {
            throw new BusinessException("无权限操作该支付记录");
        }

        // 3. 验证状态（只能更新待支付状态的支付方式）
        if (payment.getStatus() != 1) {
            throw new BusinessException("当前状态不允许更新支付方式，当前状态：" + getStatusText(payment.getStatus()));
        }

        // 4. 更新支付方式
        int result = paymentMapper.updatePaymentMethod(id, paymentMethod);
        if (result <= 0) {
            throw new BusinessException("更新支付方式失败");
        }

        log.info("支付方式更新成功, id: {}, paymentNo: {}, paymentMethod: {}", id, payment.getPaymentNo(), paymentMethod);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean autoConfirmPayment(String paymentNo, String thirdPartyOrderNo, Integer paymentMethod) {
        log.info("自动确认支付, paymentNo: {}, thirdPartyOrderNo: {}, paymentMethod: {}", paymentNo, thirdPartyOrderNo, paymentMethod);

        // 1. 查询支付记录
        Payment payment = paymentMapper.selectByPaymentNo(paymentNo);
        if (payment == null) {
            log.warn("支付记录不存在, paymentNo: {}", paymentNo);
            return false;
        }

        // 2. 验证支付方式（只允许支付宝和微信自动确认）
        if (paymentMethod != 2 && paymentMethod != 3) {
            log.warn("支付方式不支持自动确认, paymentMethod: {}", paymentMethod);
            return false;
        }

        // 3. 验证支付方式是否匹配
        if (!payment.getPaymentMethod().equals(paymentMethod)) {
            log.warn("支付方式不匹配, paymentMethod: {}, expected: {}", payment.getPaymentMethod(), paymentMethod);
            return false;
        }

        // 4. 验证状态（只有待支付状态才能自动确认）
        if (payment.getStatus() != 1) {
            log.warn("支付状态不允许自动确认, paymentNo: {}, status: {}", paymentNo, payment.getStatus());
            return false;
        }

        // 5. 更新支付状态为已完成，记录第三方订单号和确认信息
        LocalDateTime now = LocalDateTime.now();
        
        // 将第三方订单号存储到extra字段（JSON格式）
        String extra = String.format("{\"thirdPartyOrderNo\":\"%s\",\"autoConfirmed\":true}", thirdPartyOrderNo);
        
        int result = paymentMapper.autoConfirmPayment(
                payment.getId(),
                3, // 已完成状态
                now, // 确认时间
                null, // 自动确认，确认人为null（表示系统自动确认）
                extra
        );

        if (result <= 0) {
            log.error("自动确认支付失败, paymentNo: {}", paymentNo);
            return false;
        }

        log.info("自动确认支付成功, paymentNo: {}, paymentId: {}", paymentNo, payment.getId());
        return true;
    }

    @Override
    public PaymentDetailResponse queryPaymentStatus(String paymentNo) {
        log.info("查询支付状态, paymentNo: {}", paymentNo);
        
        Payment payment = paymentMapper.selectByPaymentNo(paymentNo);
        if (payment == null) {
            return null;
        }
        
        return paymentMapper.selectPaymentDetail(payment.getId(), payment.getStudentId());
    }

    @Override
    public List<Payment> listPendingOnlinePayments(Integer paymentMethod, Integer limit) {
        log.info("查询待支付的在线支付订单, paymentMethod: {}, limit: {}", paymentMethod, limit);
        return paymentMapper.selectPendingOnlinePayments(paymentMethod, limit);
    }

    /**
     * 获取状态文本
     */
    private String getStatusText(Integer status) {
        switch (status) {
            case 1:
                return "待支付";
            case 2:
                return "待确认";
            case 3:
                return "已完成";
            case 4:
                return "已拒绝";
            default:
                return "未知";
        }
    }
}

