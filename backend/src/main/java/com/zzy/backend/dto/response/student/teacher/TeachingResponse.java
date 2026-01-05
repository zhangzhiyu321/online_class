package com.zzy.backend.dto.response.student.teacher;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 教学信息响应DTO
 */
@Data
public class TeachingResponse {
    /**
     * 教学信息ID
     */
    private Long id;

    /**
     * 阶段ID
     */
    private Integer stageId;

    /**
     * 阶段名称
     */
    private String stageName;

    /**
     * 科目ID
     */
    private Integer subjectId;

    /**
     * 科目名称
     */
    private String subjectName;

    /**
     * 每小时价格
     */
    private BigDecimal pricePerHour;
}

