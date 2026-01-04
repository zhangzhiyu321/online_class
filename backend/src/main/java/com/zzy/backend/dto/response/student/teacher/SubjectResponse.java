package com.zzy.backend.dto.response.student.teacher;

import lombok.Data;

/**
 * 科目响应DTO
 */
@Data
public class SubjectResponse {
    /**
     * 科目ID
     */
    private Integer id;

    /**
     * 科目名称
     */
    private String name;
}

