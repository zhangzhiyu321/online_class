package com.zzy.backend.mapper.student.teacher;

import com.zzy.backend.dto.request.student.teacher.TeacherListRequest;
import com.zzy.backend.dto.response.common.SubjectResponse;
import com.zzy.backend.dto.response.common.TeachingStageResponse;
import com.zzy.backend.dto.response.student.teacher.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

/**
 * 教师Mapper接口
 */
@Mapper
public interface TeacherMapper {

    /**
     * 查询教师列表（分页）
     *
     * @param request 查询请求参数
     * @return 教师列表
     */
    List<TeacherListItemResponse> selectTeacherList(TeacherListRequest request);

    /**
     * 统计教师总数
     *
     * @param request 查询请求参数
     * @return 总数
     */
    Long countTeacherList(TeacherListRequest request);

    /**
     * 查询教师的科目列表
     *
     * @param userId 教师用户ID
     * @return 科目列表
     */
    List<SubjectResponse> selectTeacherSubjects(@Param("userId") Long userId);

    /**
     * 查询教师的最低价格
     *
     * @param userId 教师用户ID
     * @return 最低价格
     */
    BigDecimal selectTeacherMinPrice(@Param("userId") Long userId);

    /**
     * 查询教师是否已认证（至少有一个已通过的认证）
     *
     * @param userId 教师用户ID
     * @return 认证数量
     */
    Long countCertifiedTeacher(@Param("userId") Long userId);

    /**
     * 查询教师详情基本信息
     *
     * @param userId 教师用户ID
     * @return 教师详情
     */
    TeacherDetailResponse selectTeacherDetail(@Param("userId") Long userId);

    /**
     * 查询教师的教学阶段列表
     *
     * @param userId 教师用户ID
     * @return 阶段列表
     */
    List<TeachingStageResponse> selectTeacherStages(@Param("userId") Long userId);

    /**
     * 查询教师的教学信息列表
     *
     * @param userId 教师用户ID
     * @return 教学信息列表
     */
    List<TeachingResponse> selectTeacherTeachings(@Param("userId") Long userId);

    /**
     * 查询教师的时间表列表
     *
     * @param userId 教师用户ID
     * @return 时间表列表
     */
    List<ScheduleResponse> selectTeacherSchedules(@Param("userId") Long userId);
}

