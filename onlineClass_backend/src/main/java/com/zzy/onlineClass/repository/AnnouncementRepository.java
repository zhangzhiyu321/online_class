package com.zzy.onlineClass.repository;

import com.zzy.onlineClass.entity.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 公告Repository
 */
@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {
    
    /**
     * 查询已发布的公告列表（未删除且未过期）
     */
    @Query("SELECT a FROM Announcement a WHERE a.deletedAt IS NULL " +
           "AND a.status = 1 " +
           "AND (a.expireTime IS NULL OR a.expireTime > :now) " +
           "ORDER BY a.priority DESC, a.publishTime DESC")
    List<Announcement> findPublishedAnnouncements(@Param("now") LocalDateTime now);
    
    /**
     * 根据类型查询已发布的公告
     */
    @Query("SELECT a FROM Announcement a WHERE a.deletedAt IS NULL " +
           "AND a.status = 1 " +
           "AND a.type = :type " +
           "AND (a.expireTime IS NULL OR a.expireTime > :now) " +
           "ORDER BY a.priority DESC, a.publishTime DESC")
    List<Announcement> findPublishedByType(@Param("type") Integer type, @Param("now") LocalDateTime now);
    
    /**
     * 查询所有未删除的公告（管理员用）
     */
    @Query("SELECT a FROM Announcement a WHERE a.deletedAt IS NULL " +
           "ORDER BY a.createdAt DESC")
    List<Announcement> findAllNotDeleted();
}

