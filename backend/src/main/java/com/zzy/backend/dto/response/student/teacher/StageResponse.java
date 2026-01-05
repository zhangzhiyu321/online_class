package com.zzy.backend.dto.response.student.teacher;

import lombok.Data;

/**
 * 教学阶段响应DTO
 */
@Data
public class StageResponse {
    /**
     * 阶段ID
     */
    private Integer id;

    /**
     * 阶段名称
     */
    private String name;
}

