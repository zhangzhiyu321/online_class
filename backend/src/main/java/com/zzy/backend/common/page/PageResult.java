package com.zzy.backend.common.page;

import lombok.Data;
import java.util.List;

/**
 * 分页结果封装类
 */
@Data
public class PageResult<T> {
    
    /**
     * 数据列表
     */
    private List<T> list;
    
    /**
     * 总记录数
     */
    private Long total;
    
    /**
     * 当前页码
     */
    private Integer page;
    
    /**
     * 每页数量
     */
    private Integer pageSize;
    
    /**
     * 总页数
     */
    private Integer totalPages;
    
    public PageResult() {
    }
    
    /**
     * 创建分页结果
     */
    public static <T> PageResult<T> of(List<T> list, Long total, Integer page, Integer pageSize) {
        PageResult<T> result = new PageResult<>();
        result.list = list;
        result.total = total;
        result.page = page;
        result.pageSize = pageSize;
        result.totalPages = (int)Math.ceil((double)total/pageSize);
        return result;
    }
}

