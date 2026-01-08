package com.zzy.backend.service.common;

import com.zzy.backend.dto.response.common.SubjectResponse;
import com.zzy.backend.dto.response.common.TeachingStageResponse;

import java.util.List;

/**
 * 通用服务接口
 */
public interface CommonService {

    /**
     * 获取所有启用的教学阶段列表
     *
     * @return 教学阶段列表
     */
    List<TeachingStageResponse> getTeachingStages();

    /**
     * 获取所有启用的科目列表
     *
     * @return 科目列表
     */
    List<SubjectResponse> getSubjects();
}

