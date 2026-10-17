package com.memoryline.controller;

import java.util.List;

import com.memoryline.dto.CalendarSummaryDTO;
import com.memoryline.entity.PracticeLogEntity;
import com.memoryline.service.CalendarService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 练字记忆日历控制器
 *
 * @author congwu.lei
 */
@RestController
@RequestMapping("/api/calendar")
@RequiredArgsConstructor
public class CalendarController {

    private final CalendarService calendarService;

    /**
     * 获取月度记忆日历汇总
     *
     * @param yearMonth 查询月份 (YYYY-MM，可选)
     * @return 月度汇总数据
     */
    @GetMapping("/summary")
    public ResponseEntity<CalendarSummaryDTO> getMonthlySummary(@RequestParam(value = "yearMonth", required = false) String yearMonth) {
        return ResponseEntity.ok(calendarService.getMonthlySummary(yearMonth));
    }

    /**
     * 获取特定日期的打卡记录列表
     *
     * @param date 目标日期 (YYYY-MM-DD)
     * @return 打卡日志明细列表
     */
    @GetMapping("/day-logs")
    public ResponseEntity<List<PracticeLogEntity>> getDayLogs(@RequestParam("date") String date) {
        return ResponseEntity.ok(calendarService.getDayLogs(date));
    }
}
