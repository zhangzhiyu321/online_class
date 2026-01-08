package com.zzy.backend.dto.response.student.teacher;

import com.zzy.backend.dto.response.common.SubjectResponse;
import com.zzy.backend.dto.response.common.TeachingStageResponse;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

/**
 * 教师详情响应DTO
 */
@Data
public class TeacherDetailResponse {
    /**
     * 教师ID（用户ID）
     */
    private Long id;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 个人简介
     */
    private String introduction;

    /**
     * 教学年限
     */
    private Integer teachingYears;

    /**
     * 教学风格描述
     */
    private String teachingStyle;

    /**
     * 评分
     */
    private BigDecimal rating;

    /**
     * 评价数量
     */
    private Integer ratingCount;

    /**
     * 是否已认证
     */
    private Boolean certified;

    /**
     * 在线状态：0-离线，1-在线
     */
    private Integer onlineStatus;

    /**
     * 教学阶段列表
     */
    private List<TeachingStageResponse> stages;

    /**
     * 教学信息列表
     */
    private List<TeachingResponse> teachings;

    /**
     * 时间表列表
     */
    private List<ScheduleResponse> schedules;
}

