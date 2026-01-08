package com.zzy.backend.mapper.common;

import com.zzy.backend.dto.response.common.SubjectResponse;
import com.zzy.backend.dto.response.common.TeachingStageResponse;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 通用Mapper接口
 */
@Mapper
public interface CommonMapper {

    /**
     * 查询所有启用的教学阶段列表（按排序顺序）
     *
     * @return 教学阶段列表
     */
    List<TeachingStageResponse> selectTeachingStages();

    /**
     * 查询所有启用的科目列表（按排序顺序）
     *
     * @return 科目列表
     */
    List<SubjectResponse> selectSubjects();
}

