package com.memoryline.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.memoryline.constant.CardCategory;
import com.memoryline.dto.DataBackupDTO;
import com.memoryline.dto.TodayTaskResponseDTO;
import com.memoryline.entity.PracticeCardEntity;
import com.memoryline.entity.PracticeLogEntity;
import com.memoryline.entity.VoiceConfigEntity;
import com.memoryline.repository.PracticeCardRepository;
import com.memoryline.repository.PracticeLogRepository;
import com.memoryline.repository.VoiceConfigRepository;
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
    private final VoiceConfigRepository voiceConfigRepository;

    @Override
    public TodayTaskResponseDTO getTodayTasks(String dateStr, int newLimit, CardCategory category, String lesson) {
        String queryDate = (dateStr == null || dateStr.isBlank())
                ? LocalDate.now().format(DATE_FORMATTER)
                : dateStr;

        TodayTaskResponseDTO dto = new TodayTaskResponseDTO();
        dto.setTodayDate(queryDate);

        // 新学与待复习卡片查询
        List<PracticeCardEntity> dueReviews = practiceCardRepository.findDueReviewCards(queryDate).stream()
                .filter(c -> c.getStage() > 0)
                .filter(c -> !queryDate.equals(c.getLastPracticedDate()))
                .filter(c -> category == null || c.getCategory() == category)
                .filter(c -> lesson == null || lesson.isBlank() || lesson.equals(c.getLessonTitle()))
                .collect(Collectors.toList());
        dto.setReviewCards(dueReviews);

        List<PracticeCardEntity> todayNew = practiceCardRepository.findByStageEquals(0).stream()
                .filter(c -> !queryDate.equals(c.getLastPracticedDate()))
                .filter(c -> category == null || c.getCategory() == category)
                .filter(c -> lesson == null || lesson.isBlank() || lesson.equals(c.getLessonTitle()))
                .sorted(curriculumOrder())
                .limit(newLimit)
                .collect(Collectors.toList());
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
        boolean practicedSameDay = actualDate.equals(card.getLastPracticedDate());
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
            factor = practicedSameDay ? factor : Math.max(1.3, factor - 0.2);
            card.setNextReviewDate(baseDate.plusDays(1).format(DATE_FORMATTER));
        } else if (rating == 2) {
            // 良好：阶段顺延，保持标准艾宾浩斯间隔
            postStage = practicedSameDay ? Math.max(1, preStage) : preStage + 1;
            int interval = calculateInterval(postStage, factor, false);
            card.setNextReviewDate(baseDate.plusDays(interval).format(DATE_FORMATTER));
        } else {
            // 熟练：阶段顺延，增加熟练度因子并适度延长复习间隔
            postStage = practicedSameDay ? Math.max(1, preStage) : preStage + 1;
            factor = practicedSameDay ? factor : Math.min(3.0, factor + 0.15);
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
        List<PracticeCardEntity> cards = category == null
                ? practiceCardRepository.findAll() : practiceCardRepository.findByCategory(category);
        cards.sort(curriculumOrder());
        return cards;
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

    @Override
    public DataBackupDTO exportData() {
        DataBackupDTO backupDTO = new DataBackupDTO();
        backupDTO.setVersion("1.0.0");
        backupDTO.setExportTime(LocalDateTime.now().toString());
        backupDTO.setCards(practiceCardRepository.findAll());
        backupDTO.setVoiceConfigs(voiceConfigRepository.findAll());
        backupDTO.setPracticeLogs(practiceLogRepository.findAll());
        return backupDTO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int importData(DataBackupDTO backupDTO) {
        if (backupDTO == null) {
            return 0;
        }

        int count = 0;
        if (backupDTO.getCards() != null) {
            for (PracticeCardEntity c : backupDTO.getCards()) {
                if (c.getCharacterVal() == null || c.getCategory() == null) {
                    continue;
                }
                Optional<PracticeCardEntity> existOpt = practiceCardRepository
                        .findByCharacterValAndCategory(c.getCharacterVal(), c.getCategory());
                if (existOpt.isPresent()) {
                    PracticeCardEntity exist = existOpt.get();
                    exist.setPinyin(c.getPinyin());
                    exist.setChant(c.getChant());
                    exist.setStrokeCount(c.getStrokeCount());
                    exist.setStrokePaths(c.getStrokePaths());
                    exist.setStrokeHints(c.getStrokeHints());
                    exist.setGridType(c.getGridType());
                    exist.setStage(c.getStage() != null ? c.getStage() : 0);
                    exist.setEasinessFactor(c.getEasinessFactor() != null ? c.getEasinessFactor() : 2.5);
                    exist.setNextReviewDate(c.getNextReviewDate());
                    exist.setLastPracticedDate(c.getLastPracticedDate());
                    exist.setTotalReviews(c.getTotalReviews() != null ? c.getTotalReviews() : 0);
                    exist.setIsMastered(Boolean.TRUE.equals(c.getIsMastered()));
                    exist.setLearningLevel(c.getLearningLevel());
                    exist.setLearningOrder(c.getLearningOrder());
                    exist.setLessonTitle(c.getLessonTitle());
                    practiceCardRepository.save(exist);
                } else {
                    c.setId(null);
                    practiceCardRepository.save(c);
                }
                count++;
            }
        }

        if (backupDTO.getVoiceConfigs() != null) {
            for (VoiceConfigEntity vc : backupDTO.getVoiceConfigs()) {
                if (vc.getProviderType() == null) {
                    continue;
                }
                Optional<VoiceConfigEntity> existOpt = voiceConfigRepository.findByProviderType(vc.getProviderType());
                if (existOpt.isPresent()) {
                    VoiceConfigEntity exist = existOpt.get();
                    exist.setProviderName(vc.getProviderName());
                    exist.setApiUrlTemplate(vc.getApiUrlTemplate());
                    exist.setAppKey(vc.getAppKey());
                    exist.setSecretKey(vc.getSecretKey());
                    exist.setSpeed(vc.getSpeed());
                    exist.setPitch(vc.getPitch());
                    exist.setVoicePersona(vc.getVoicePersona());
                    exist.setIsActive(vc.getIsActive());
                    voiceConfigRepository.save(exist);
                } else {
                    vc.setId(null);
                    voiceConfigRepository.save(vc);
                }
            }
        }

        log.info("Successfully imported backup data: {} cards processed", count);
        return count;
    }

    /**
     * 排序规则：按难度级别、课程顺序、品类与字符排序
     *
     * @return 比较器
     */
    private Comparator<PracticeCardEntity> curriculumOrder() {
        return Comparator.comparing((PracticeCardEntity c) -> c.getLearningLevel() == null ? 99 : c.getLearningLevel())
                .thenComparing(c -> c.getLearningOrder() == null ? Integer.MAX_VALUE : c.getLearningOrder())
                .thenComparing(c -> c.getCategory().name())
                .thenComparing(PracticeCardEntity::getCharacterVal);
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
