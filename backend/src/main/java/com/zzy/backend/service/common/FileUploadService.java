package com.zzy.backend.service.common;

import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传服务接口
 */
public interface FileUploadService {
    /**
     * 上传文件
     *
     * @param file 文件
     * @param type 文件类型：image、document、video、audio等
     * @param userId 用户ID
     * @return 文件访问URL
     */
    String uploadFile(MultipartFile file, String type, Long userId);

    /**
     * 上传头像
     *
     * @param file 图片文件
     * @param userId 用户ID
     * @return 头像访问URL
     */
    String uploadAvatar(MultipartFile file, Long userId);
}

