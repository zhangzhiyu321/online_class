package com.zzy.backend.entity.teacher;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 教师学历认证实体类
 */
@Data
public class TeacherCertification {
    /**
     * ID
     */
    private Long id;

    /**
     * 教师用户ID
     */
    private Long userId;

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
     * 毕业年份
     */
    private Integer graduationYear;

    /**
     * 审核状态：1-待审核，2-已通过，3-已拒绝
     */
    private Integer status;

    /**
     * 审核人ID（管理员）
     */
    private Long auditUserId;

    /**
     * 审核意见/拒绝原因
     */
    private String auditReason;

    /**
     * 审核时间
     */
    private LocalDateTime auditAt;

    /**
     * 扩展信息
     */
    private String extra;

    /**
     * 版本号
     */
    private Integer version;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 删除时间（软删除）
     */
    private LocalDateTime deletedAt;
}

