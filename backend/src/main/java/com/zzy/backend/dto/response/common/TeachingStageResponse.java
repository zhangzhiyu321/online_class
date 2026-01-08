package com.zzy.backend.dto.response.common;

import lombok.Data;

/**
 * 教学阶段响应DTO
 */
@Data
public class TeachingStageResponse {
    /**
     * ID
     */
    private Integer id;

    /**
     * 阶段代码
     */
    private String code;

    /**
     * 阶段名称
     */
    private String name;

    /**
     * 排序顺序
     */
    private Integer sortOrder;
}

