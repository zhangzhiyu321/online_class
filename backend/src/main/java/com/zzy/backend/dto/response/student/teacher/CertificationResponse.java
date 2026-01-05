package com.zzy.backend.dto.response.student.teacher;

import lombok.Data;

/**
 * 教师认证响应DTO
 */
@Data
public class CertificationResponse {
    /**
     * 认证ID
     */
    private Long id;

    /**
     * 证书类型：1-学历证书，2-教师资格证，3-其他
     */
    private Integer certificateType;

    /**
     * 证书图片URL
     */
    private String certificateImage;

    /**
     * 学校名称
     */
    private String schoolName;

    /**
     * 专业
     */
    private String major;

    /**
     * 学历：本科、硕士、博士等
     */
    private String degree;

    /**
     * 审核状态：1-待审核，2-已通过，3-已拒绝
     */
    private Integer status;
}

