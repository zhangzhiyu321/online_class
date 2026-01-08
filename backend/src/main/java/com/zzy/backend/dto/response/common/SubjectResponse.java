package com.zzy.backend.dto.response.common;

import lombok.Data;

/**
 * 科目响应DTO
 */
@Data
public class SubjectResponse {
    /**
     * ID
     */
    private Integer id;

    /**
     * 科目代码
     */
    private String code;

    /**
     * 科目名称
     */
    private String name;

    /**
     * 科目分类：理科、文科等
     */
    private String category;

    /**
     * 排序顺序
     */
    private Integer sortOrder;
}

