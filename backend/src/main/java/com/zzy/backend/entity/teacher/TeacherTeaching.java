package com.zzy.backend.entity.teacher;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 教师教学信息实体类
 */
@Data
public class TeacherTeaching {
    /**
     * ID
     */
    private Long id;

    /**
     * 教师用户ID
     */
    private Long userId;

    /**
     * 教学阶段ID
     */
    private Integer stageId;

    /**
     * 科目ID
     */
    private Integer subjectId;

    /**
     * 课时价格（元/小时）
     */
    private BigDecimal pricePerHour;

    /**
     * 状态：1-启用，2-禁用
     */
    private Integer status;

    /**
     * 扩展信息
     */
    private String extra;

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

