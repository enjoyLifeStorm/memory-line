package com.memoryline.service.impl;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.memoryline.dto.CalendarSummaryDTO;
import com.memoryline.dto.CalendarSummaryDTO.CalendarDayItem;
import com.memoryline.entity.PracticeCardEntity;
import com.memoryline.entity.PracticeLogEntity;
import com.memoryline.repository.PracticeCardRepository;
import com.memoryline.repository.PracticeLogRepository;
import com.memoryline.service.CalendarService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

/**
 * 练字记忆日历业务服务实现类
 *
 * @author congwu.lei
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CalendarServiceImpl implements CalendarService {

    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    private final PracticeCardRepository practiceCardRepository;
    private final PracticeLogRepository practiceLogRepository;

    @Override
    public CalendarSummaryDTO getMonthlySummary(String yearMonth) {
        String targetMonth = (yearMonth == null || yearMonth.isBlank())
                ? LocalDate.now().format(MONTH_FORMATTER)
                : yearMonth;

        CalendarSummaryDTO summary = new CalendarSummaryDTO();
        summary.setYearMonth(targetMonth);

        Map<String, CalendarDayItem> dayMap = new HashMap<>();

        // 1. 查询该月内历史打卡记录
        List<PracticeLogEntity> monthLogs = practiceLogRepository.findByReviewDateStartingWith(targetMonth);
        summary.setTotalPracticedCount(monthLogs.size());

        for (PracticeLogEntity logItem : monthLogs) {
            String date = logItem.getReviewDate();
            CalendarDayItem item = dayMap.computeIfAbsent(date, k -> {
                CalendarDayItem newItem = new CalendarDayItem();
                newItem.setDate(k);
                newItem.setPracticedCount(0);
                newItem.setScheduledReviewCount(0);
                newItem.setCharacters(new ArrayList<>());
                return newItem;
            });
            item.setPracticedCount(item.getPracticedCount() + 1);
            if (!item.getCharacters().contains(logItem.getCharacterVal())) {
                item.getCharacters().add(logItem.getCharacterVal());
            }
        }

        // 2. 查询该月内未来计划复习任务
        List<PracticeCardEntity> scheduledCards = practiceCardRepository.findByNextReviewDateStartingWith(targetMonth);
        for (PracticeCardEntity card : scheduledCards) {
            String date = card.getNextReviewDate();
            CalendarDayItem item = dayMap.computeIfAbsent(date, k -> {
                CalendarDayItem newItem = new CalendarDayItem();
                newItem.setDate(k);
                newItem.setPracticedCount(0);
                newItem.setScheduledReviewCount(0);
                newItem.setCharacters(new ArrayList<>());
                return newItem;
            });
            item.setScheduledReviewCount(item.getScheduledReviewCount() + 1);
            if (!item.getCharacters().contains(card.getCharacterVal())) {
                item.getCharacters().add(card.getCharacterVal());
            }
        }

        summary.setDaySummaries(dayMap);
        return summary;
    }

    @Override
    public List<PracticeLogEntity> getDayLogs(String dateStr) {
        return practiceLogRepository.findByReviewDate(dateStr);
    }
}
