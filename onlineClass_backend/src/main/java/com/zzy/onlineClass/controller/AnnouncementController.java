package com.zzy.onlineClass.controller;

import com.zzy.onlineClass.common.Result;
import com.zzy.onlineClass.entity.Announcement;
import com.zzy.onlineClass.service.AnnouncementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * 公告控制器
 */
@RestController
@RequestMapping("/api/announcements")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://localhost:5175"})
public class AnnouncementController {
    
    @Autowired
    private AnnouncementService announcementService;
    
    /**
     * 获取已发布的公告列表
     */
    @GetMapping("/published")
    public Result<List<Announcement>> getPublishedAnnouncements(
            @RequestParam(required = false) Integer type) {
        try {
            List<Announcement> announcements;
            if (type != null) {
                announcements = announcementService.getPublishedByType(type);
            } else {
                announcements = announcementService.getPublishedAnnouncements();
            }
            return Result.success(announcements);
        } catch (Exception e) {
            return Result.error("获取公告列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取公告详情
     */
    @GetMapping("/{id}")
    public Result<Announcement> getAnnouncement(@PathVariable Long id) {
        try {
            Optional<Announcement> announcement = announcementService.getById(id);
            if (announcement.isPresent()) {
                return Result.success(announcement.get());
            } else {
                return Result.error("公告不存在或已删除");
            }
        } catch (Exception e) {
            return Result.error("获取公告详情失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取所有公告（管理员用）
     */
    @GetMapping("/all")
    public Result<List<Announcement>> getAllAnnouncements() {
        try {
            List<Announcement> announcements = announcementService.getAllAnnouncements();
            return Result.success(announcements);
        } catch (Exception e) {
            return Result.error("获取公告列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 创建公告（管理员用）
     */
    @PostMapping
    public Result<Announcement> createAnnouncement(@RequestBody Announcement announcement) {
        try {
            Announcement created = announcementService.createAnnouncement(announcement);
            return Result.success("创建公告成功", created);
        } catch (Exception e) {
            return Result.error("创建公告失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新公告（管理员用）
     */
    @PutMapping("/{id}")
    public Result<Announcement> updateAnnouncement(
            @PathVariable Long id,
            @RequestBody Announcement announcement) {
        try {
            Announcement updated = announcementService.updateAnnouncement(id, announcement);
            return Result.success("更新公告成功", updated);
        } catch (Exception e) {
            return Result.error("更新公告失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除公告（管理员用）
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteAnnouncement(@PathVariable Long id) {
        try {
            announcementService.deleteAnnouncement(id);
            return Result.success("删除公告成功");
        } catch (Exception e) {
            return Result.error("删除公告失败: " + e.getMessage());
        }
    }
}

