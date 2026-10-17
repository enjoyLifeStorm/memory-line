package com.memoryline.service.impl;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.memoryline.constant.CardCategory;
import com.memoryline.dto.TodayTaskResponseDTO;
import com.memoryline.entity.PracticeCardEntity;
import com.memoryline.entity.PracticeLogEntity;
import com.memoryline.repository.PracticeCardRepository;
import com.memoryline.repository.PracticeLogRepository;
import com.memoryline.service.CardService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 练习卡片业务服务实现类
 *
 * @author congwu.lei
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CardServiceImpl implements CardService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final int[] STAGE_INTERVALS = {0, 1, 2, 4, 7, 15, 30};

    private final PracticeCardRepository practiceCardRepository;
    private final PracticeLogRepository practiceLogRepository;

    @Override
    public TodayTaskResponseDTO getTodayTasks(String dateStr) {
        String queryDate = (dateStr == null || dateStr.isBlank())
                ? LocalDate.now().format(DATE_FORMATTER)
                : dateStr;

        TodayTaskResponseDTO dto = new TodayTaskResponseDTO();
        dto.setTodayDate(queryDate);

        // 获取待复习卡片 (包含今天或之前到期的未掌握卡片)
        List<PracticeCardEntity> dueReviews = practiceCardRepository.findDueReviewCards(queryDate);
        dto.setReviewCards(dueReviews);

        // 获取新学卡片 (stage = 0 的卡片，每天取前5张作为今日新学，避免压力过大)
        List<PracticeCardEntity> allNewCards = practiceCardRepository.findByStageEquals(0);
        List<PracticeCardEntity> todayNew = new ArrayList<>();
        for (int i = 0; i < Math.min(5, allNewCards.size()); i++) {
            todayNew.add(allNewCards.get(i));
        }
        dto.setNewCards(todayNew);

        // 统计今日已完成练习次数
        List<PracticeLogEntity> todayLogs = practiceLogRepository.findByReviewDate(queryDate);
        dto.setCompletedTodayCount(todayLogs.size());

        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PracticeCardEntity reviewCard(Long cardId, Integer rating, String reviewDate) {
        PracticeCardEntity card = practiceCardRepository.findById(cardId)
                .orElseThrow(() -> new IllegalArgumentException("Card not found with id: " + cardId));

        String actualDate = (reviewDate == null || reviewDate.isBlank())
                ? LocalDate.now().format(DATE_FORMATTER)
                : reviewDate;

        LocalDate baseDate = LocalDate.parse(actualDate, DATE_FORMATTER);
        int preStage = card.getStage() != null ? card.getStage() : 0;
        int postStage;
        double factor = card.getEasinessFactor() != null ? card.getEasinessFactor() : 2.5;

        if (rating == null || rating < 1) {
            rating = 1;
        }

        // SM-2 改良版遗忘自适应计算
        if (rating == 1) {
            // 需重练：重置为阶段1，次日必须再次复习
            postStage = 1;
            factor = Math.max(1.3, factor - 0.2);
            card.setNextReviewDate(baseDate.plusDays(1).format(DATE_FORMATTER));
        } else if (rating == 2) {
            // 良好：阶段顺延，保持标准艾宾浩斯间隔
            postStage = preStage + 1;
            int interval = calculateInterval(postStage, factor, false);
            card.setNextReviewDate(baseDate.plusDays(interval).format(DATE_FORMATTER));
        } else {
            // 熟练：阶段顺延，增加熟练度因子并适度延长复习间隔
            postStage = preStage + 1;
            factor = Math.min(3.0, factor + 0.15);
            int interval = calculateInterval(postStage, factor, true);
            card.setNextReviewDate(baseDate.plusDays(interval).format(DATE_FORMATTER));
        }

        // 超过第6阶段则标记为完全掌握
        if (postStage >= 7) {
            card.setIsMastered(true);
            card.setStage(7);
        } else {
            card.setStage(postStage);
            card.setIsMastered(false);
        }

        card.setEasinessFactor(factor);
        card.setLastPracticedDate(actualDate);
        card.setTotalReviews((card.getTotalReviews() != null ? card.getTotalReviews() : 0) + 1);

        PracticeCardEntity savedCard = practiceCardRepository.save(card);

        // 记录打卡历史日志
        PracticeLogEntity logEntity = new PracticeLogEntity();
        logEntity.setCardId(savedCard.getId());
        logEntity.setCharacterVal(savedCard.getCharacterVal());
        logEntity.setCategory(savedCard.getCategory());
        logEntity.setRating(rating);
        logEntity.setPreStage(preStage);
        logEntity.setPostStage(postStage);
        logEntity.setReviewDate(actualDate);
        practiceLogRepository.save(logEntity);

        log.info("Card {} ({}) reviewed: rating={}, preStage={}, postStage={}, nextReviewDate={}",
                card.getId(), card.getCharacterVal(), rating, preStage, postStage, card.getNextReviewDate());

        return savedCard;
    }

    @Override
    public List<PracticeCardEntity> getCardsByCategory(CardCategory category) {
        if (category == null) {
            return practiceCardRepository.findAll();
        }
        return practiceCardRepository.findByCategory(category);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PracticeCardEntity saveCard(PracticeCardEntity card) {
        return practiceCardRepository.save(card);
    }

    @Override
    public PracticeCardEntity getCardById(Long id) {
        return practiceCardRepository.findById(id).orElse(null);
    }

    /**
     * 计算下一个复习周期的间隔天数
     *
     * @param stage 艾宾浩斯阶段
     * @param factor 熟练度因子
     * @param isBonus 是否给予熟练加成
     * @return 间隔天数
     */
    private int calculateInterval(int stage, double factor, boolean isBonus) {
        if (stage <= 0) {
            return 1;
        }
        if (stage >= STAGE_INTERVALS.length) {
            return 30;
        }
        int baseDays = STAGE_INTERVALS[stage];
        if (isBonus && stage >= 2) {
            return (int) Math.max(baseDays + 1, Math.round(baseDays * (factor / 2.5)));
        }
        return baseDays;
    }
}
