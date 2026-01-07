package com.zzy.backend.dto.request.student.payment;

import lombok.Data;

/**
 * 支付列表查询请求DTO
 */
@Data
public class PaymentListRequest {
    /**
     * 页码，从1开始
     */
    private Integer page = 1;

    /**
     * 每页数量
     */
    private Integer pageSize = 10;

    /**
     * 支付状态：1-待支付，2-待确认，3-已完成，4-已拒绝
     */
    private Integer status;

    /**
     * 搜索关键词（教师姓名、支付单号等）
     */
    private String keyword;

    /**
     * 学生用户ID（从Token中获取，不需要前端传递）
     */
    private Long studentId;

    /**
     * 验证并修正分页参数
     */
    public void validate() {
        if (page == null || page < 1) {
            page = 1;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = 10;
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

