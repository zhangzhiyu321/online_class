package com.zzy.backend.controller.common;

import com.zzy.backend.common.Result;
import com.zzy.backend.common.constant.ResponseCode;
import com.zzy.backend.common.exception.BusinessException;
import com.zzy.backend.dto.response.common.SubjectResponse;
import com.zzy.backend.dto.response.common.TeachingStageResponse;
import com.zzy.backend.service.common.CommonService;
import com.zzy.backend.service.common.FileUploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 通用控制器
 */
@Slf4j
@RestController
@RequestMapping("/common")
@Tag(name = "通用接口", description = "通用功能接口")
public class CommonController {

    @Autowired
    private FileUploadService fileUploadService;

    @Autowired
    private CommonService commonService;

    /**
     * 文件上传
     *
     * @param file 文件
     * @param type 文件类型：image、document、video、audio
     * @param httpRequest HTTP请求对象
     * @return 上传结果
     */
    @PostMapping("/upload")
    @Operation(summary = "文件上传", description = "通用文件上传接口，支持图片、文档、视频、音频等")
    public Result<Map<String, String>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "type", defaultValue = "image") String type,
            HttpServletRequest httpRequest) {
        log.info("文件上传请求, type: {}, fileName: {}", type, file.getOriginalFilename());

        // 从请求属性中获取当前登录用户ID
        Long userId = (Long) httpRequest.getAttribute("userId");
        if (userId == null) {
            return Result.unauthorized("未登录或Token无效");
        }

        try {
            String fileUrl = fileUploadService.uploadFile(file, type, userId);
            Map<String, String> result = new HashMap<>();
            result.put("url", fileUrl);
            return Result.success("上传成功", result);
        } catch (BusinessException e) {
            log.error("文件上传失败: {}", e.getMessage());
            return Result.error(ResponseCode.BUSINESS_ERROR, e.getMessage());
        } catch (Exception e) {
            log.error("文件上传失败", e);
            return Result.error("文件上传失败，请稍后重试");
        }
    }

    /**
     * 获取教学阶段列表
     *
     * @return 教学阶段列表
     */
    @GetMapping("/teaching-stages")
    @Operation(summary = "获取教学阶段列表", description = "获取所有启用的教学阶段列表，用于下拉框选择")
    public Result<List<TeachingStageResponse>> getTeachingStages() {
        log.info("获取教学阶段列表");
        try {
            List<TeachingStageResponse> stages = commonService.getTeachingStages();
            return Result.success("获取成功", stages);
        } catch (Exception e) {
            log.error("获取教学阶段列表失败", e);
            return Result.error("获取教学阶段列表失败，请稍后重试");
        }
    }

    /**
     * 获取科目列表
     *
     * @return 科目列表
     */
    @GetMapping("/subjects")
    @Operation(summary = "获取科目列表", description = "获取所有启用的科目列表，用于下拉框选择")
    public Result<List<SubjectResponse>> getSubjects() {
        log.info("获取科目列表");
        try {
            List<SubjectResponse> subjects = commonService.getSubjects();
            return Result.success("获取成功", subjects);
        } catch (Exception e) {
            log.error("获取科目列表失败", e);
            return Result.error("获取科目列表失败，请稍后重试");
        }
    }
}

