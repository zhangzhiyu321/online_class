package com.zzy.backend.service.student.teacher;

import com.zzy.backend.common.page.PageResult;
import com.zzy.backend.dto.request.student.teacher.TeacherListRequest;
import com.zzy.backend.dto.response.student.teacher.TeacherDetailResponse;
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

    /**
     * 根据教师ID查询详细信息
     *
     * @param userId 教师用户ID
     * @return 教师详情
     */
    TeacherDetailResponse getTeacherDetail(Long userId);
}

