package com.memoryline.dto;

import java.util.List;

import com.memoryline.entity.PracticeCardEntity;

import lombok.Data;

/**
 * 今日练习任务响应数据传输对象
 *
 * @author congwu.lei
 */
@Data
public class TodayTaskResponseDTO {

    /**
     * 今日公历日期 (YYYY-MM-DD)
     */
    private String todayDate;

    /**
     * 今日新学卡片列表
     */
    private List<PracticeCardEntity> newCards;

    /**
     * 今日待复习卡片列表
     */
    private List<PracticeCardEntity> reviewCards;

    /**
     * 今日已完成练习数
     */
    private Integer completedTodayCount;
}
