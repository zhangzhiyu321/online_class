package com.zzy.backend.service.student.teacher.impl;

import com.zzy.backend.common.page.PageResult;
import com.zzy.backend.dto.request.student.teacher.TeacherListRequest;
import com.zzy.backend.dto.response.student.teacher.*;
import com.zzy.backend.mapper.student.teacher.TeacherMapper;
import com.zzy.backend.service.student.teacher.TeacherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 教师服务实现类
 */
@Slf4j
@Service
public class TeacherServiceImpl implements TeacherService {

    @Autowired
    private TeacherMapper teacherMapper;

    @Override
    public PageResult<TeacherListItemResponse> getTeacherList(TeacherListRequest request) {
        // 验证并修正分页参数
        request.validate();

        // 查询教师列表
        List<TeacherListItemResponse> teacherList = teacherMapper.selectTeacherList(request);

        // 为每个教师填充科目信息（列表查询中已包含认证状态和最低价格）
        for (TeacherListItemResponse teacher : teacherList) {
            // 查询教师的科目列表
            List<SubjectResponse> subjects = teacherMapper.selectTeacherSubjects(teacher.getId());
            teacher.setSubjects(subjects);
        }

        // 统计总数
        Long total = teacherMapper.countTeacherList(request);

        // 构建分页结果
        return PageResult.of(teacherList, total, request.getPage(), request.getPageSize());
    }

    @Override
    public TeacherDetailResponse getTeacherDetail(Long userId) {
        log.info("查询教师详情, userId: {}", userId);

        // 查询教师基本信息
        TeacherDetailResponse detail = teacherMapper.selectTeacherDetail(userId);
        if (detail == null) {
            return null;
        }

        // 查询教学阶段列表
        List<StageResponse> stages = teacherMapper.selectTeacherStages(userId);
        detail.setStages(stages);

        // 查询教学信息列表
        List<TeachingResponse> teachings = teacherMapper.selectTeacherTeachings(userId);
        detail.setTeachings(teachings);

        // 查询时间表列表
        List<ScheduleResponse> schedules = teacherMapper.selectTeacherSchedules(userId);
        detail.setSchedules(schedules);

        return detail;
    }
}

