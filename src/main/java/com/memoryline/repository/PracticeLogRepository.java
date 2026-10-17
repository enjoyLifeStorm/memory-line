package com.memoryline.repository;

import java.util.List;

import com.memoryline.entity.PracticeLogEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * 练字打卡历史日志数据访问接口
 *
 * @author congwu.lei
 */
@Repository
public interface PracticeLogRepository extends JpaRepository<PracticeLogEntity, Long> {

    /**
     * 根据卡片 ID 查询所有历史打卡日志
     *
     * @param cardId 卡片ID
     * @return 日志列表
     */
    List<PracticeLogEntity> findByCardIdOrderByCreatedAtDesc(Long cardId);

    /**
     * 根据打卡日期查询日志列表
     *
     * @param reviewDate 打卡公历日期 (YYYY-MM-DD)
     * @return 日志列表
     */
    List<PracticeLogEntity> findByReviewDate(String reviewDate);

    /**
     * 查询在指定月份内完成打卡的日志列表
     *
     * @param monthPrefix 月份前缀 (如 "2026-10")
     * @return 日志列表
     */
    @Query("SELECT l FROM PracticeLogEntity l WHERE l.reviewDate LIKE CONCAT(:monthPrefix, '%')")
    List<PracticeLogEntity> findByReviewDateStartingWith(@Param("monthPrefix") String monthPrefix);
}
