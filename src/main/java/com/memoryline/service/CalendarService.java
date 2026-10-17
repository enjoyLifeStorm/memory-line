package com.memoryline.service;

import java.util.List;

import com.memoryline.dto.CalendarSummaryDTO;
import com.memoryline.entity.PracticeLogEntity;

/**
 * 练字记忆日历业务服务接口
 *
 * @author congwu.lei
 */
public interface CalendarService {

    /**
     * 获取指定月份的记忆日历汇总
     *
     * @param yearMonth 查询月份 (格式: YYYY-MM)，为空时默认当月
     * @return 月度汇总传输对象
     */
    CalendarSummaryDTO getMonthlySummary(String yearMonth);

    /**
     * 获取指定公历日期的全部打卡日志明细
     *
     * @param dateStr 目标日期 (YYYY-MM-DD)
     * @return 打卡历史日志列表
     */
    List<PracticeLogEntity> getDayLogs(String dateStr);
}
