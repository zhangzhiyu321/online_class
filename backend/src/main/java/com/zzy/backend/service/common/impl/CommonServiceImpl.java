package com.zzy.backend.service.common.impl;

import com.zzy.backend.dto.response.common.SubjectResponse;
import com.zzy.backend.dto.response.common.TeachingStageResponse;
import com.zzy.backend.mapper.common.CommonMapper;
import com.zzy.backend.service.common.CommonService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 通用服务实现类
 */
@Slf4j
@Service
public class CommonServiceImpl implements CommonService {

    @Autowired
    private CommonMapper commonMapper;

    @Override
    public List<TeachingStageResponse> getTeachingStages() {
        log.info("查询教学阶段列表");
        return commonMapper.selectTeachingStages();
    }

    @Override
    public List<SubjectResponse> getSubjects() {
        log.info("查询科目列表");
        return commonMapper.selectSubjects();
    }
}

