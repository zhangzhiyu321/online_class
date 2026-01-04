package com.zzy.backend.service.student.teacher;

import com.zzy.backend.common.page.PageResult;
import com.zzy.backend.dto.request.student.teacher.TeacherListRequest;
import com.zzy.backend.dto.response.student.teacher.TeacherListItemResponse;

/**
 * 教师服务接口
 */
public interface TeacherService {

    /**
     * 查询教师列表（分页、搜索、筛选）
     *
     * @param request 查询请求参数
     * @return 分页结果
     */
    PageResult<TeacherListItemResponse> getTeacherList(TeacherListRequest request);
}

