package com.memoryline.dto;

import java.util.List;
import java.util.Map;

import lombok.Data;

/**
 * 记忆日历月度汇总数据传输对象
 *
 * @author congwu.lei
 */
@Data
public class CalendarSummaryDTO {

    /**
     * 当前查询月份 (YYYY-MM)
     */
    private String yearMonth;

    /**
     * 累计打卡总次数
     */
    private Integer totalPracticedCount;

    /**
     * 按公历日期 (YYYY-MM-DD) 映射的每日日程详情
     */
    private Map<String, CalendarDayItem> daySummaries;

    /**
     * 每日日程单元
     *
     * @author congwu.lei
     */
    @Data
    public static class CalendarDayItem {

        /**
         * 日期 (YYYY-MM-DD)
         */
        private String date;

        /**
         * 历史已练习完成的字符数量
         */
        private Integer practicedCount;

        /**
         * 当天计划复习的字符数量
         */
        private Integer scheduledReviewCount;

        /**
         * 当天关联的字符简要标签列表
         */
        private List<String> characters;
    }
}
