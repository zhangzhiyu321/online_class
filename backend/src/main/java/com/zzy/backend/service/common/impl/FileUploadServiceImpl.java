package com.zzy.backend.service.common.impl;

import com.zzy.backend.common.exception.BusinessException;
import com.zzy.backend.service.common.FileUploadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import jakarta.annotation.PostConstruct;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 文件上传服务实现类
 */
@Slf4j
@Service
public class FileUploadServiceImpl implements FileUploadService {

    @Value("${file.upload.path:./uploads}")
    private String uploadPath;

    @Value("${file.upload.url-prefix:http://localhost:8080/api/uploads}")
    private String urlPrefix;

    /**
     * 获取绝对路径
     */
    private Path getAbsoluteUploadPath() {
        Path path = Paths.get(uploadPath);
        // 如果是相对路径，转换为绝对路径
        if (!path.isAbsolute()) {
            // 获取项目根目录
            String projectRoot = System.getProperty("user.dir");
            path = Paths.get(projectRoot, uploadPath).normalize();
        }
        return path;
    }

    /**
     * 初始化上传目录
     */
    private void initUploadDirectory() {
        try {
            Path uploadDir = getAbsoluteUploadPath();
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
                log.info("创建上传目录: {}", uploadDir.toAbsolutePath());
            } else {
                log.info("上传目录已存在: {}", uploadDir.toAbsolutePath());
            }
        } catch (IOException e) {
            log.error("创建上传目录失败", e);
            throw new RuntimeException("无法创建上传目录", e);
        }
    }

    private static final long MAX_IMAGE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final long MAX_AVATAR_SIZE = 5 * 1024 * 1024; // 5MB
    private static final String[] ALLOWED_IMAGE_TYPES = {"image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"};

    /**
     * 初始化方法，在Bean创建后执行
     */
    @PostConstruct
    public void init() {
        initUploadDirectory();
    }

    @Override
    public String uploadFile(MultipartFile file, String type, Long userId) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }

        // 验证文件类型
        String contentType = file.getContentType();
        if (contentType == null || !isAllowedImageType(contentType)) {
            throw new BusinessException("不支持的文件类型，仅支持：jpg、png、gif、webp");
        }

        // 验证文件大小
        long maxSize = "image".equals(type) ? MAX_IMAGE_SIZE : MAX_IMAGE_SIZE;
        if (file.getSize() > maxSize) {
            throw new BusinessException("文件大小不能超过 " + (maxSize / 1024 / 1024) + "MB");
        }

        try {
            // 获取绝对上传路径
            Path basePath = getAbsoluteUploadPath();
            
            // 生成文件存储路径
            String dateDir = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String fileName = generateFileName(file.getOriginalFilename());
            String relativePath = type + "/" + dateDir + "/" + fileName;
            Path fullPath = basePath.resolve(relativePath).normalize();

            // 创建目录
            Files.createDirectories(fullPath.getParent());

            // 保存文件（使用Files.copy更可靠）
            Files.copy(file.getInputStream(), fullPath, StandardCopyOption.REPLACE_EXISTING);

            // 生成访问URL（确保URL格式正确）
            String cleanPrefix = urlPrefix.endsWith("/") ? urlPrefix.substring(0, urlPrefix.length() - 1) : urlPrefix;
            String cleanPath = relativePath.replace("\\", "/");
            if (!cleanPath.startsWith("/")) {
                cleanPath = "/" + cleanPath;
            }
            String fileUrl = cleanPrefix + cleanPath;

            log.info("文件上传成功, userId: {}, type: {}, url: {}", userId, type, fileUrl);
            return fileUrl;
        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new BusinessException("文件上传失败：" + e.getMessage());
        }
    }

    @Override
    public String uploadAvatar(MultipartFile file, Long userId) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("头像文件不能为空");
        }

        // 验证文件类型
        String contentType = file.getContentType();
        if (contentType == null || !isAllowedImageType(contentType)) {
            throw new BusinessException("不支持的文件类型，仅支持：jpg、png、gif、webp");
        }

        // 验证文件大小
        if (file.getSize() > MAX_AVATAR_SIZE) {
            throw new BusinessException("头像大小不能超过 5MB");
        }

        try {
            // 获取绝对上传路径
            Path basePath = getAbsoluteUploadPath();
            
            // 生成文件存储路径
            String fileName = generateFileName(file.getOriginalFilename());
            String relativePath = "avatar/" + fileName;
            Path fullPath = basePath.resolve(relativePath).normalize();

            // 创建目录
            Files.createDirectories(fullPath.getParent());

            // 保存文件（使用Files.copy更可靠）
            Files.copy(file.getInputStream(), fullPath, StandardCopyOption.REPLACE_EXISTING);

            // 生成访问URL（确保URL格式正确）
            String cleanPrefix = urlPrefix.endsWith("/") ? urlPrefix.substring(0, urlPrefix.length() - 1) : urlPrefix;
            String cleanPath = relativePath.replace("\\", "/");
            if (!cleanPath.startsWith("/")) {
                cleanPath = "/" + cleanPath;
            }
            String fileUrl = cleanPrefix + cleanPath;

            log.info("头像上传成功, userId: {}, url: {}", userId, fileUrl);
            return fileUrl;
        } catch (IOException e) {
            log.error("头像上传失败", e);
            throw new BusinessException("头像上传失败：" + e.getMessage());
        }
    }

    /**
     * 检查是否为允许的图片类型
     */
    private boolean isAllowedImageType(String contentType) {
        for (String allowedType : ALLOWED_IMAGE_TYPES) {
            if (allowedType.equalsIgnoreCase(contentType)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 生成唯一文件名
     */
    private String generateFileName(String originalFilename) {
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        return UUID.randomUUID().toString().replace("-", "") + extension;
    }
}

