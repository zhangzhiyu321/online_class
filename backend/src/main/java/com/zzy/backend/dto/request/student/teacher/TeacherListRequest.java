package com.zzy.backend.dto.request.student.teacher;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 教师列表查询请求DTO
 */
@Data
public class TeacherListRequest {
    /**
     * 页码，从1开始
     */
    private Integer page = 1;

    /**
     * 每页数量
     */
    private Integer pageSize = 12;

    /**
     * 搜索关键词（教师姓名或科目）
     */
    private String keyword;

    /**
     * 教学阶段ID
     */
    private Integer stageId;

    /**
     * 科目ID
     */
    private Integer subjectId;

    /**
     * 最低评分
     */
    private BigDecimal minRating;

    /**
     * 最低价格
     */
    private BigDecimal minPrice;

    /**
     * 最高价格
     */
    private BigDecimal maxPrice;

    /**
     * 验证并修正分页参数
     */
    public void validate() {
        if (page == null || page < 1) {
            page = 1;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = 12;
        }
        if (pageSize > 100) {
            pageSize = 100; // 最大100条
        }
    }

    /**
     * 获取偏移量
     */
    public Integer getOffset() {
        return (page - 1) * pageSize;
    }
}

