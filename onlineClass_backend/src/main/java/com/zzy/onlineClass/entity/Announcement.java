package com.zzy.onlineClass.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 公告实体类
 */
@Entity
@Table(name = "announcements")
@Data
public class Announcement {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 200)
    private String title;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
    
    /**
     * 公告类型：1-系统公告，2-活动公告，3-维护公告
     */
    @Column(nullable = false)
    private Integer type = 1;
    
    /**
     * 优先级：1-普通，2-重要，3-紧急
     */
    @Column(nullable = false)
    private Integer priority = 1;
    
    /**
     * 状态：1-已发布，2-草稿，3-已下线
     */
    @Column(nullable = false)
    private Integer status = 1;
    
    @Column(name = "publish_time")
    private LocalDateTime publishTime;
    
    @Column(name = "expire_time")
    private LocalDateTime expireTime;
    
    @Column(name = "view_count")
    private Integer viewCount = 0;
    
    @Column(name = "created_by")
    private Long createdBy;
    
    @Column(name = "updated_by")
    private Long updatedBy;
    
    @Column(columnDefinition = "JSON")
    private String extra;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}

