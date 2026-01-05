package com.zzy.backend.dto.response.student.teacher;

import lombok.Data;

/**
 * 教师时间表响应DTO
 */
@Data
public class ScheduleResponse {
    /**
     * 时间表ID
     */
    private Long id;

    /**
     * 星期几（1-7，1表示周一）
     */
    private Integer weekday;

    /**
     * 开始时间（格式：HH:mm:ss）
     */
    private String startTime;

    /**
     * 结束时间（格式：HH:mm:ss）
     */
    private String endTime;

    /**
     * 时间表类型：1-固定周期（每周重复），2-临时时间段（特定日期范围）
     */
    private Integer scheduleType;

    /**
     * 生效开始日期（临时时间段使用，格式：yyyy-MM-dd）
     */
    private String startDate;

    /**
     * 生效结束日期（临时时间段使用，格式：yyyy-MM-dd）
     */
    private String endDate;
}

