package com.memoryline.repository;

import java.util.List;
import java.util.Optional;

import com.memoryline.constant.CardCategory;
import com.memoryline.entity.PracticeCardEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * 练习卡片数据访问接口
 *
 * @author congwu.lei
 */
@Repository
public interface PracticeCardRepository extends JpaRepository<PracticeCardEntity, Long> {

    /**
     * 根据字符值与品类查找卡片
     *
     * @param characterVal 字符
     * @param category 品类
     * @return 卡片可选对象
     */
    Optional<PracticeCardEntity> findByCharacterValAndCategory(String characterVal, CardCategory category);

    /**
     * 根据品类查询所有卡片
     *
     * @param category 品类
     * @return 卡片列表
     */
    List<PracticeCardEntity> findByCategory(CardCategory category);

    /**
     * 查询今日待复习的卡片（下次复习日期小于等于今天，且未掌握）
     *
     * @param today 当前日期 (YYYY-MM-DD)
     * @return 卡片列表
     */
    @Query("SELECT c FROM PracticeCardEntity c WHERE c.nextReviewDate <= :today AND c.isMastered = false ORDER BY c.nextReviewDate ASC")
    List<PracticeCardEntity> findDueReviewCards(@Param("today") String today);

    /**
     * 查询尚未开始初学的新卡片 (stage = 0)
     *
     * @return 新卡片列表
     */
    List<PracticeCardEntity> findByStageEquals(Integer stage);

    /**
     * 查询在特定日期计划复习的卡片列表
     *
     * @param targetDate 目标公历日期 (YYYY-MM-DD)
     * @return 卡片列表
     */
    List<PracticeCardEntity> findByNextReviewDate(String targetDate);

    /**
     * 查询在指定月份内计划复习的卡片列表
     *
     * @param monthPrefix 月份前缀 (如 "2026-10")
     * @return 卡片列表
     */
    @Query("SELECT c FROM PracticeCardEntity c WHERE c.nextReviewDate LIKE CONCAT(:monthPrefix, '%')")
    List<PracticeCardEntity> findByNextReviewDateStartingWith(@Param("monthPrefix") String monthPrefix);
}
