package com.zzy.backend.dto.request.student.appointment;

import lombok.Data;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/**
 * 创建预约请求DTO
 */
@Data
public class CreateAppointmentRequest {
    /**
     * 教师ID（必填）
     */
    @NotNull(message = "教师ID不能为空")
    private Long teacherId;

    /**
     * 教学阶段ID（必填）
     */
    @NotNull(message = "教学阶段ID不能为空")
    private Integer stageId;

    /**
     * 科目ID（必填）
     */
    @NotNull(message = "科目ID不能为空")
    private Integer subjectId;

    /**
     * 预约日期（必填，格式：YYYY-MM-DD）
     */
    @NotBlank(message = "预约日期不能为空")
    private String appointmentDate;

    /**
     * 开始时间（必填，格式：HH:mm）
     */
    @NotBlank(message = "开始时间不能为空")
    private String startTime;

    /**
     * 结束时间（必填，格式：HH:mm）
     */
    @NotBlank(message = "结束时间不能为空")
    private String endTime;

    /**
     * 时长（分钟）（必填）
     */
    @NotNull(message = "时长不能为空")
    @Min(value = 1, message = "时长必须大于0")
    private Integer duration;

    /**
     * 课时单价（必填）
     */
    @NotNull(message = "课时单价不能为空")
    @DecimalMin(value = "0.01", message = "课时单价必须大于0")
    private BigDecimal pricePerHour;

    /**
     * 总金额（必填）
     */
    @NotNull(message = "总金额不能为空")
    @DecimalMin(value = "0.01", message = "总金额必须大于0")
    private BigDecimal totalAmount;

    /**
     * 学生姓名（必填）
     */
    @NotBlank(message = "学生姓名不能为空")
    @Size(max = 50, message = "学生姓名长度不能超过50个字符")
    private String studentName;

    /**
     * 学生年级（必填）
     */
    @NotBlank(message = "学生年级不能为空")
    @Size(max = 50, message = "学生年级长度不能超过50个字符")
    private String studentGrade;

    /**
     * 联系方式（必填）
     */
    @NotBlank(message = "联系方式不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "请输入正确的手机号")
    private String studentPhone;

    /**
     * 备注信息（选填）
     */
    private String remark;
}

