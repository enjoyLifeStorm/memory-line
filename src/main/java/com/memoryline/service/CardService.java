package com.memoryline.service;

import java.util.List;

import com.memoryline.constant.CardCategory;
import com.memoryline.dto.TodayTaskResponseDTO;
import com.memoryline.entity.PracticeCardEntity;

/**
 * 练习卡片业务服务接口
 *
 * @author congwu.lei
 */
public interface CardService {

    /**
     * 获取指定日期的今日练字任务（新学卡片 + 待复习卡片）
     *
     * @param dateStr 目标公历日期 (YYYY-MM-DD)，若为空则取系统当前日期
     * @return 今日任务响应对象
     */
    TodayTaskResponseDTO getTodayTasks(String dateStr);

    /**
     * 提交单张卡片的书写反馈评分，并自适应调整下一次复习公历日期
     *
     * @param cardId 卡片 ID
     * @param rating 反馈评分：1:需重练(太难), 2:良好, 3:熟练
     * @param reviewDate 打卡公历日期 (YYYY-MM-DD)
     * @return 调整后的卡片实体
     */
    PracticeCardEntity reviewCard(Long cardId, Integer rating, String reviewDate);

    /**
     * 按品类查询卡片列表
     *
     * @param category 品类枚举
     * @return 卡片列表
     */
    List<PracticeCardEntity> getCardsByCategory(CardCategory category);

    /**
     * 保存或更新卡片
     *
     * @param card 卡片实体
     * @return 保存后的卡片实体
     */
    PracticeCardEntity saveCard(PracticeCardEntity card);

    /**
     * 根据 ID 获取卡片详情
     *
     * @param id 卡片主键 ID
     * @return 卡片实体
     */
    PracticeCardEntity getCardById(Long id);
}
