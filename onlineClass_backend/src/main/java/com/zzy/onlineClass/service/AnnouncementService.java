package com.zzy.onlineClass.service;

import com.zzy.onlineClass.entity.Announcement;
import com.zzy.onlineClass.repository.AnnouncementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 公告服务类
 */
@Service
public class AnnouncementService {
    
    @Autowired
    private AnnouncementRepository announcementRepository;
    
    /**
     * 获取已发布的公告列表
     */
    public List<Announcement> getPublishedAnnouncements() {
        return announcementRepository.findPublishedAnnouncements(LocalDateTime.now());
    }
    
    /**
     * 根据类型获取已发布的公告
     */
    public List<Announcement> getPublishedByType(Integer type) {
        return announcementRepository.findPublishedByType(type, LocalDateTime.now());
    }
    
    /**
     * 根据ID获取公告详情
     */
    public Optional<Announcement> getById(Long id) {
        Announcement announcement = announcementRepository.findById(id).orElse(null);
        if (announcement != null && announcement.getDeletedAt() == null) {
            // 增加浏览次数
            if (announcement.getStatus() == 1) {
                announcement.setViewCount(announcement.getViewCount() + 1);
                announcementRepository.save(announcement);
            }
            return Optional.of(announcement);
        }
        return Optional.empty();
    }
    
    /**
     * 获取所有公告（管理员用）
     */
    public List<Announcement> getAllAnnouncements() {
        return announcementRepository.findAllNotDeleted();
    }
    
    /**
     * 创建公告
     */
    @Transactional
    public Announcement createAnnouncement(Announcement announcement) {
        if (announcement.getStatus() == 1 && announcement.getPublishTime() == null) {
            announcement.setPublishTime(LocalDateTime.now());
        }
        return announcementRepository.save(announcement);
    }
    
    /**
     * 更新公告
     */
    @Transactional
    public Announcement updateAnnouncement(Long id, Announcement announcement) {
        Announcement existing = announcementRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("公告不存在"));
        
        if (existing.getDeletedAt() != null) {
            throw new RuntimeException("公告已删除");
        }
        
        existing.setTitle(announcement.getTitle());
        existing.setContent(announcement.getContent());
        existing.setType(announcement.getType());
        existing.setPriority(announcement.getPriority());
        existing.setStatus(announcement.getStatus());
        existing.setExpireTime(announcement.getExpireTime());
        existing.setUpdatedBy(announcement.getUpdatedBy());
        
        // 如果状态改为已发布且发布时间为空，设置发布时间
        if (existing.getStatus() == 1 && existing.getPublishTime() == null) {
            existing.setPublishTime(LocalDateTime.now());
        }
        
        return announcementRepository.save(existing);
    }
    
    /**
     * 删除公告（软删除）
     */
    @Transactional
    public void deleteAnnouncement(Long id) {
        Announcement announcement = announcementRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("公告不存在"));
        
        announcement.setDeletedAt(LocalDateTime.now());
        announcementRepository.save(announcement);
    }
}

