package com.zzy.backend.entity.common;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 科目实体类
 */
@Data
public class Subject {
    /**
     * ID
     */
    private Integer id;

    /**
     * 科目代码（唯一）
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

