package com.zzy.backend.service.student.appointment.impl;

import com.zzy.backend.common.exception.BusinessException;
import com.zzy.backend.dto.request.student.appointment.CreateAppointmentRequest;
import com.zzy.backend.dto.response.student.appointment.CreateAppointmentResponse;
import com.zzy.backend.entity.student.Appointment;
import com.zzy.backend.mapper.student.appointment.AppointmentMapper;
import com.zzy.backend.service.student.appointment.AppointmentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

/**
 * 预约服务实现类
 */
@Slf4j
@Service
public class AppointmentServiceImpl implements AppointmentService {

    @Autowired
    private AppointmentMapper appointmentMapper;

    /**
     * 日期格式化器
     */
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    /**
     * 时间格式化器
     */
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CreateAppointmentResponse createAppointment(CreateAppointmentRequest request, Long studentId) {
        log.info("创建预约, studentId: {}, request: {}", studentId, request);

        // 1. 参数校验
        validateRequest(request);

        // 2. 生成订单号
        String orderNo = generateOrderNo();

        // 3. 检查订单号是否已存在（理论上不会重复，但为了安全起见）
        Appointment existingAppointment = appointmentMapper.selectByOrderNo(orderNo);
        if (existingAppointment != null) {
            // 如果订单号重复，重新生成
            orderNo = generateOrderNo();
        }

        // 4. 构建预约实体
        Appointment appointment = new Appointment();
        appointment.setOrderNo(orderNo);
        appointment.setStudentId(studentId);
        appointment.setTeacherId(request.getTeacherId());
        appointment.setStageId(request.getStageId());
        appointment.setSubjectId(request.getSubjectId());
        
        // 解析日期和时间
        appointment.setAppointmentDate(LocalDate.parse(request.getAppointmentDate(), DATE_FORMATTER));
        appointment.setStartTime(LocalTime.parse(request.getStartTime(), TIME_FORMATTER));
        appointment.setEndTime(LocalTime.parse(request.getEndTime(), TIME_FORMATTER));
        
        appointment.setDuration(request.getDuration());
        appointment.setPricePerHour(request.getPricePerHour());
        appointment.setTotalAmount(request.getTotalAmount());
        appointment.setStudentName(request.getStudentName());
        appointment.setStudentGrade(request.getStudentGrade());
        appointment.setStudentPhone(request.getStudentPhone());
        appointment.setRemark(request.getRemark());
        
        // 设置默认状态为待确认
        appointment.setStatus(1);
        appointment.setVersion(1);
        
        LocalDateTime now = LocalDateTime.now();
        appointment.setCreatedAt(now);
        appointment.setUpdatedAt(now);

        // 5. 保存预约记录
        int result = appointmentMapper.insert(appointment);
        if (result <= 0) {
            throw new BusinessException("创建预约失败");
        }

        log.info("预约创建成功, orderNo: {}, appointmentId: {}", orderNo, appointment.getId());

        // 6. 构建响应
        CreateAppointmentResponse response = new CreateAppointmentResponse();
        response.setId(appointment.getId());
        response.setOrderNo(appointment.getOrderNo());
        response.setStatus(appointment.getStatus());
        response.setAppointmentDate(appointment.getAppointmentDate());
        response.setStartTime(appointment.getStartTime());
        response.setEndTime(appointment.getEndTime());
        response.setTotalAmount(appointment.getTotalAmount());

        return response;
    }

    /**
     * 验证请求参数
     */
    private void validateRequest(CreateAppointmentRequest request) {
        // 验证日期不能是过去（允许今天）
        LocalDate appointmentDate = LocalDate.parse(request.getAppointmentDate(), DATE_FORMATTER);
        LocalDate today = LocalDate.now();
        if (appointmentDate.isBefore(today)) {
            throw new BusinessException("预约日期不能是过去的时间");
        }
        
        // 如果选择的是今天，需要验证时间是否已过
        if (appointmentDate.equals(today)) {
            LocalTime startTime = LocalTime.parse(request.getStartTime(), TIME_FORMATTER);
            LocalTime now = LocalTime.now();
            if (startTime.isBefore(now)) {
                throw new BusinessException("预约开始时间不能是过去的时间");
            }
        }

        // 验证时间段的合理性
        LocalTime startTime = LocalTime.parse(request.getStartTime(), TIME_FORMATTER);
        LocalTime endTime = LocalTime.parse(request.getEndTime(), TIME_FORMATTER);
        if (!endTime.isAfter(startTime)) {
            throw new BusinessException("结束时间必须晚于开始时间");
        }

        // 验证时长是否匹配（允许1分钟的误差，因为可能存在舍入问题）
        int calculatedDuration = (int) java.time.Duration.between(startTime, endTime).toMinutes();
        if (Math.abs(calculatedDuration - request.getDuration()) > 1) {
            throw new BusinessException("时长与时间段不匹配，计算时长：" + calculatedDuration + "分钟，提交时长：" + request.getDuration() + "分钟");
        }

        // 验证金额是否匹配
        double calculatedAmount = (request.getPricePerHour().doubleValue() * request.getDuration()) / 60.0;
        double expectedAmount = request.getTotalAmount().doubleValue();
        // 允许0.01的误差（处理浮点数精度问题）
        if (Math.abs(calculatedAmount - expectedAmount) > 0.01) {
            log.warn("金额验证: 计算金额={}, 提交金额={}, 差值={}", calculatedAmount, expectedAmount, Math.abs(calculatedAmount - expectedAmount));
            throw new BusinessException("总金额计算不正确，计算金额：" + String.format("%.2f", calculatedAmount) + "元，提交金额：" + String.format("%.2f", expectedAmount) + "元");
        }
    }

    /**
     * 生成订单号
     * 格式：APT + yyyyMMdd + 5位随机数
     * 例如：APT2024012012345
     */
    private String generateOrderNo() {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        Random random = new Random();
        int randomNum = random.nextInt(90000) + 10000; // 生成5位随机数
        return "APT" + dateStr + randomNum;
    }
}

