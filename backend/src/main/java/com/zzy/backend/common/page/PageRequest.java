package com.zzy.backend.common.page;

import lombok.Data;

/**
 * 分页请求参数
 */
@Data
public class PageRequest {
    
    /**
     * 页码，从1开始
     */
    private Integer page = 1;
    
    /**
     * 每页数量
     */
    private Integer pageSize = 10;
    
    /**
     * 排序字段
     */
    private String orderBy;
    
    /**
     * 排序方式：ASC/DESC
     */
    private String orderDirection = "DESC";
    
    /**
     * 获取偏移量
     */
    public Integer getOffset() {
        return (page - 1) * pageSize;
    }
    
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
}

