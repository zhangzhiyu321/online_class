package com.zzy.backend.controller.student.profile;

import com.zzy.backend.common.Result;
import com.zzy.backend.common.constant.ResponseCode;
import com.zzy.backend.common.exception.BusinessException;
import com.zzy.backend.service.common.FileUploadService;
import com.zzy.backend.service.student.profile.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户信息控制器
 */
@Slf4j
@RestController
@RequestMapping("/user")
@Tag(name = "用户管理", description = "用户信息相关功能")
public class UserController {

    @Autowired
    private FileUploadService fileUploadService;

    @Autowired
    private UserService userService;

    /**
     * 上传头像
     *
     * @param file 头像文件
     * @param httpRequest HTTP请求对象
     * @return 上传结果
     */
    @PostMapping("/avatar")
    @Operation(summary = "上传头像", description = "上传用户头像，支持jpg、png、gif、webp格式，最大5MB")
    public Result<Map<String, String>> uploadAvatar(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest httpRequest) {
        log.info("上传头像请求, fileName: {}", file.getOriginalFilename());

        // 从请求属性中获取当前登录用户ID
        Long userId = (Long) httpRequest.getAttribute("userId");
        if (userId == null) {
            return Result.unauthorized("未登录或Token无效");
        }

        try {
            String avatarUrl = fileUploadService.uploadAvatar(file, userId);
            // 更新用户头像
            userService.updateAvatar(userId, avatarUrl);
            
            Map<String, String> result = new HashMap<>();
            result.put("url", avatarUrl);
            return Result.success("上传成功", result);
        } catch (BusinessException e) {
            log.error("头像上传失败: {}", e.getMessage());
            return Result.error(ResponseCode.BUSINESS_ERROR, e.getMessage());
        } catch (Exception e) {
            log.error("头像上传失败", e);
            return Result.error("头像上传失败，请稍后重试");
        }
    }
}

