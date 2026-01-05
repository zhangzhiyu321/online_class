package com.zzy.backend.controller.student.teacher;

import com.zzy.backend.common.Result;
import com.zzy.backend.common.page.PageResult;
import com.zzy.backend.dto.request.student.teacher.TeacherListRequest;
import com.zzy.backend.dto.response.student.teacher.TeacherDetailResponse;
import com.zzy.backend.dto.response.student.teacher.TeacherListItemResponse;
import com.zzy.backend.service.student.teacher.TeacherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 教师控制器
 */
@Slf4j
@RestController
@RequestMapping("/teacher")
@Tag(name = "教师管理", description = "学生端-教师相关功能")
public class TeacherController {

    @Autowired
    private TeacherService teacherService;

    /**
     * 获取教师列表
     *
     * @param request 查询请求参数
     * @return 教师列表
     */
    @GetMapping("/list")
    @Operation(summary = "查看教师列表", description = "分页查询教师列表，支持关键词搜索和多条件筛选")
    public Result<PageResult<TeacherListItemResponse>> getTeacherList(TeacherListRequest request) {
        log.info("查询教师列表, 参数: {}", request);
        PageResult<TeacherListItemResponse> result = teacherService.getTeacherList(request);
        return Result.success(result);
    }

    /**
     * 根据教师 id 查询详细信息
     *
     * @param id 教师用户ID
     * @return 教师详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取教师详情", description = "根据教师ID查询教师的详细信息，包括基本信息、教学信息、时间表和认证信息")
    public Result<TeacherDetailResponse> getTeacherDetail(@PathVariable Long id) {
        log.info("查询教师详情, id: {}", id);
        TeacherDetailResponse result = teacherService.getTeacherDetail(id);
        if (result == null) {
            return Result.error("教师不存在或已被删除");
        }
        return Result.success(result);
    }
}

