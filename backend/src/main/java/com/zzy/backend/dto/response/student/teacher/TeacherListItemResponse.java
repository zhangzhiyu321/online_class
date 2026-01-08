package com.zzy.backend.dto.response.student.teacher;

import com.zzy.backend.dto.response.common.SubjectResponse;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

/**
 * 教师列表项响应DTO
 */
@Data
public class TeacherListItemResponse {
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
     * 评分
     */
    private BigDecimal rating;

    /**
     * 评价数量
     */
    private Integer ratingCount;

    /**
     * 教学年限
     */
    private Integer teachingYears;

    /**
     * 是否已认证
     */
    private Boolean certified;

    /**
     * 在线状态：0-离线，1-在线
     */
    private Integer onlineStatus;

    /**
     * 科目列表
     */
    private List<SubjectResponse> subjects;

    /**
     * 最低价格
     */
    private BigDecimal minPrice;
}

