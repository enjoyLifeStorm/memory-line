package com.memoryline;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import com.memoryline.constant.CardCategory;
import com.memoryline.dto.TodayTaskResponseDTO;
import com.memoryline.entity.PracticeCardEntity;
import com.memoryline.initializer.DataInitializer;
import com.memoryline.repository.PracticeCardRepository;
import com.memoryline.repository.PracticeLogRepository;
import com.memoryline.service.CardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:learning-tests;DB_CLOSE_DELAY=-1",
        "spring.h2.console.enabled=false"
})
@AutoConfigureMockMvc
@Transactional
class MemoryLineApplicationTests {
    @Autowired PracticeCardRepository cards;
    @Autowired PracticeLogRepository logs;
    @Autowired CardService service;
    @Autowired com.memoryline.service.DailyPlanService daily;
    @Autowired com.memoryline.repository.DailyPlanRepository plans;
    @Autowired com.memoryline.repository.RecallResultRepository recalls;
    @Autowired DataInitializer initializer;
    @Autowired MockMvc mvc;

    @BeforeEach
    void clearLearningData() {
        recalls.deleteAll();
        plans.deleteAll();
        logs.deleteAll();
        cards.deleteAll();
    }

    @Test
    void upgradingAddsAllDigitsWithoutResettingExistingProgress() throws Exception {
        PracticeCardEntity existing = card("2", CardCategory.NUMBER, 4);
        existing.setTotalReviews(9);
        existing.setNextReviewDate(LocalDate.now().plusDays(7).toString());
        cards.saveAndFlush(existing);
        initializer.run();
        initializer.run();
        List<PracticeCardEntity> digits = cards.findByCategory(CardCategory.NUMBER);
        assertThat(digits).hasSize(10);
        assertThat(digits.stream().map(PracticeCardEntity::getCharacterVal).collect(Collectors.toSet()))
                .containsExactlyInAnyOrder("0", "1", "2", "3", "4", "5", "6", "7", "8", "9");
        PracticeCardEntity retained = cards.findById(existing.getId()).orElseThrow();
        assertThat(retained.getStage()).isEqualTo(4);
        assertThat(retained.getTotalReviews()).isEqualTo(9);
        assertThat(retained.getNextReviewDate()).isEqualTo(LocalDate.now().plusDays(7).toString());
        assertThat(logs.count()).isZero();
    }

    @Test
    void taskBatchFiltersCategoryBeforeLimitingAndSeparatesNewFromReviews() {
        card("a", CardCategory.LETTER, 0);
        for (int i = 0; i < 10; i++) card(String.valueOf(i), CardCategory.NUMBER, 0);
        PracticeCardEntity due = card("due", CardCategory.NUMBER, 2);
        due.setNextReviewDate(LocalDate.now().minusDays(1).toString());
        cards.save(due);
        TodayTaskResponseDTO tasks = service.getTodayTasks(null, 5, CardCategory.NUMBER);
        assertThat(tasks.getNewCards()).hasSize(5).allMatch(c -> c.getCategory() == CardCategory.NUMBER);
        assertThat(tasks.getReviewCards()).extracting(PracticeCardEntity::getId).containsExactly(due.getId());
        assertThat(service.getTodayTasks(null, 20, CardCategory.NUMBER).getNewCards()).hasSize(10);
    }

    @Test
    void sameDayExtraPracticeRecordsHistoryWithoutAdvancingMultipleStages() {
        PracticeCardEntity digit = card("5", CardCategory.NUMBER, 0);
        PracticeCardEntity first = service.reviewCard(digit.getId(), 3, null);
        int stage = first.getStage();
        String next = first.getNextReviewDate();
        service.reviewCard(digit.getId(), 3, null);
        PracticeCardEntity updated = cards.findById(digit.getId()).orElseThrow();
        assertThat(updated.getStage()).isEqualTo(stage);
        assertThat(updated.getNextReviewDate()).isEqualTo(next);
        assertThat(updated.getTotalReviews()).isEqualTo(2);
        assertThat(logs.count()).isEqualTo(2);
        TodayTaskResponseDTO tasks = service.getTodayTasks(null, 10, null);
        assertThat(tasks.getNewCards()).isEmpty();
        assertThat(tasks.getReviewCards()).isEmpty();
    }

    @Test
    void curriculumIsCompleteOrderedAndIdempotent() throws Exception {
        initializer.run();
        long count = cards.count();
        initializer.run();
        assertThat(cards.count()).isEqualTo(count).isEqualTo(223);
        assertThat(service.getCardsByCategory(CardCategory.HANZI)).hasSize(100);
        assertThat(service.getCardsByCategory(CardCategory.PINYIN)).hasSize(87);
        assertThat(service.getCardsByCategory(CardCategory.LETTER)).hasSize(26);
        assertThat(service.getCardsByCategory(CardCategory.PINYIN).subList(0, 6))
                .extracting(PracticeCardEntity::getCharacterVal).containsExactly("a", "o", "e", "i", "u", "ü");
        assertThat(service.getTodayTasks(null, 5, CardCategory.HANZI).getNewCards())
                .extracting(PracticeCardEntity::getCharacterVal).containsExactly("一", "二", "十", "人", "八");
        assertThat(service.getCardsByCategory(CardCategory.LETTER).subList(0, 3))
                .extracting(PracticeCardEntity::getCharacterVal).containsExactly("Ll", "Tt", "Ii");
        for (PracticeCardEntity card : cards.findAll()) {
            assertThat(card.getLearningOrder()).isPositive();
            assertThat(card.getLearningLevel()).isBetween(1, 4);
            assertThat(card.getLessonTitle()).isNotBlank();
            assertThat(card.getStrokePaths()).startsWith("[").contains("M ");
        }
        TodayTaskResponseDTO lesson = service.getTodayTasks(null, 5, CardCategory.PINYIN, "12 前鼻韵母");
        assertThat(lesson.getNewCards()).extracting(PracticeCardEntity::getCharacterVal)
                .containsExactly("an", "en", "in", "un", "ün");
        mvc.perform(get("/api/cards/today").param("category", "PINYIN").param("lesson", "12 前鼻韵母")
                .param("newLimit", "5")).andExpect(status().isOk())
                .andExpect(jsonPath("$.newCards[0].characterVal").value("an"));
    }

    @Test
    void curriculumUpgradeRefreshesTeachingMaterialWithoutResettingOldReviewState() throws Exception {
        PracticeCardEntity existing = card("春", CardCategory.HANZI, 4);
        existing.setTotalReviews(12);
        existing.setEasinessFactor(2.8);
        existing.setLastPracticedDate(LocalDate.now().minusDays(2).toString());
        existing.setNextReviewDate(LocalDate.now().plusDays(6).toString());
        cards.saveAndFlush(existing);
        Long id = existing.getId();
        initializer.run();
        PracticeCardEntity upgraded = cards.findById(id).orElseThrow();
        assertThat(upgraded.getStage()).isEqualTo(4);
        assertThat(upgraded.getTotalReviews()).isEqualTo(12);
        assertThat(upgraded.getEasinessFactor()).isEqualTo(2.8);
        assertThat(upgraded.getLastPracticedDate()).isEqualTo(LocalDate.now().minusDays(2).toString());
        assertThat(upgraded.getNextReviewDate()).isEqualTo(LocalDate.now().plusDays(6).toString());
        assertThat(upgraded.getLearningLevel()).isEqualTo(4);
        assertThat(upgraded.getStrokeCount()).isEqualTo(9);
        assertThat(upgraded.getStrokePaths()).isNotBlank();
        assertThat(logs.count()).isZero();
    }

    @Test
    void apiRejectsInvalidBatchSizes() throws Exception {
        mvc.perform(get("/api/cards/today").param("newLimit", "0")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/cards/today").param("newLimit", "51")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/cards/today").param("newLimit", "10").param("category", "NUMBER"))
                .andExpect(status().isOk());
    }

    @Test
    void dailyPlanIsFixedResumableAndIncludesReviewsAcrossSubjects() {
        initializer.run();
        PracticeCardEntity due = cards.findByCategory(CardCategory.HANZI).get(0);
        due.setStage(2); due.setNextReviewDate(LocalDate.now().minusDays(1).toString()); cards.save(due);
        java.util.Map<String,Object> preview = daily.get(5, CardCategory.NUMBER, null);
        assertThat(preview.get("started")).isEqualTo(false);
        assertThat(plans.count()).isZero();
        java.util.Map<String,Object> plan = daily.start(5, CardCategory.NUMBER, null);
        assertThat((List<Long>)plan.get("reviewIds")).containsExactly(due.getId());
        List<Long> original = (List<Long>)plan.get("newIds");
        assertThat(original).hasSize(5);
        service.reviewCard(due.getId(), 2, null);
        service.reviewCard(original.get(0), 2, null);
        service.reviewCard(original.get(0), 3, null);
        java.util.Map<String,Object> resumed = daily.start(20, CardCategory.LETTER, null);
        assertThat(resumed.get("newIds")).isEqualTo(original);
        assertThat(resumed.get("subject")).isEqualTo("NUMBER");
        assertThat(resumed.get("completedCount")).isEqualTo(2L);
        assertThat((List<?>)resumed.get("remainingCards")).hasSize(4);
        org.junit.jupiter.api.Assertions.assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> daily.extra());
        for(Long id : original) service.reviewCard(id, 2, null);
        assertThat(daily.get(5, null, null).get("complete")).isEqualTo(true);
        finishRecalls();
        java.util.Map<String,Object> extra = daily.extra();
        assertThat(extra.get("newIds")).isEqualTo(original);
        assertThat((List<?>)extra.get("extraIds")).hasSize(5);
        assertThat(extra.get("completedCount")).isEqualTo(6L);
        for(Long id : (List<Long>)extra.get("extraIds")) service.reviewCard(id, 2, null);
        assertThat(daily.get(5, null, null).get("extraDone")).isEqualTo(5L);
        finishRecalls();
        assertThat((List<?>)daily.extra().get("extraIds")).hasSize(5);
    }

    @Test
    void previousDayPlanDoesNotBlockANewDay() {
        com.memoryline.entity.DailyPlanEntity yesterday = new com.memoryline.entity.DailyPlanEntity();
        yesterday.setPlanDate(LocalDate.now(java.time.ZoneId.of("Asia/Shanghai")).minusDays(1).toString());
        yesterday.setSubject("NUMBER"); yesterday.setNewLimit(5); plans.save(yesterday);
        card("1", CardCategory.NUMBER, 0);
        assertThat(daily.get(5, CardCategory.NUMBER, null).get("started")).isEqualTo(false);
        daily.start(5, CardCategory.NUMBER, null);
        assertThat(plans.count()).isEqualTo(2);
    }

    @Test
    void emptyPlanFinishesAndInvalidPreviewIsRejected() {
        assertThat(daily.start(5, null, null).get("complete")).isEqualTo(true);
        assertThat((List<?>)daily.extra().get("remainingCards")).isEmpty();
        plans.deleteAll();
        org.junit.jupiter.api.Assertions.assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> daily.get(0,null,null));
    }

    private void finishRecalls() {
        java.util.Map<String,Object> state=daily.get(5,null,null);
        for(PracticeCardEntity c:(List<PracticeCardEntity>)state.get("recallCards")) daily.recall(c.getId(),3,false);
    }

    @Test
    void recallWaitsForInterveningCardsAndNeverInflatesPracticeCounts() {
        for(int i=0;i<5;i++) card(String.valueOf(i),CardCategory.NUMBER,0);
        java.util.Map<String,Object> plan=daily.start(5,CardCategory.NUMBER,null);
        List<Long> ids=(List<Long>)plan.get("newIds");
        service.reviewCard(ids.get(0),3,null);
        assertThat((List<?>)daily.get(5,null,null).get("recallCards")).isEmpty();
        service.reviewCard(ids.get(1),3,null);
        assertThat((List<?>)daily.get(5,null,null).get("recallCards")).isEmpty();
        service.reviewCard(ids.get(2),3,null);
        List<PracticeCardEntity> ready=(List<PracticeCardEntity>)daily.get(5,null,null).get("recallCards");
        assertThat(ready).extracting(PracticeCardEntity::getId).containsExactly(ids.get(0));
        int stage=cards.findById(ids.get(0)).orElseThrow().getStage();
        long count=logs.count();
        daily.recall(ids.get(0),3,true);
        assertThat(recalls.findAll().get(0).getRating()).isEqualTo(2);
        assertThat(logs.count()).isEqualTo(count);
        assertThat(cards.findById(ids.get(0)).orElseThrow().getStage()).isEqualTo(stage);
        assertThat(daily.get(5,null,null).get("completedCount")).isEqualTo(3L);
        daily.recall(ids.get(0),1,false);
        assertThat(recalls.count()).isEqualTo(1);
        assertThat(recalls.findAll().get(0).getRating()).isEqualTo(2);
        org.junit.jupiter.api.Assertions.assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> daily.recall(ids.get(4),3,false));
        service.reviewCard(ids.get(3),3,null);service.reviewCard(ids.get(4),3,null);
        assertThat(daily.get(5,null,null).get("recallRemaining")).isEqualTo(4L);
        org.junit.jupiter.api.Assertions.assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> daily.extra());
        finishRecalls();
        assertThat(daily.get(5,null,null).get("recallRemaining")).isEqualTo(0L);
        assertThat(logs.count()).isEqualTo(5);
    }

    @Test
    void preUpgradePlansRetainTheirOriginalCompletionRules() {
        PracticeCardEntity c=card("1",CardCategory.NUMBER,0);
        com.memoryline.entity.DailyPlanEntity plan=new com.memoryline.entity.DailyPlanEntity();
        plan.setPlanDate(LocalDate.now(java.time.ZoneId.of("Asia/Shanghai")).toString());
        plan.setSubject("NUMBER");plan.setNewLimit(5);plan.setNewIds(c.getId().toString());plans.save(plan);
        service.reviewCard(c.getId(),2,null);
        assertThat(daily.get(5,null,null).get("recallRemaining")).isEqualTo(0);
        assertThat(daily.extra().get("complete")).isEqualTo(true);
    }

    private PracticeCardEntity card(String value, CardCategory category, int stage) {
        PracticeCardEntity card = new PracticeCardEntity();
        card.setCharacterVal(value);
        card.setCategory(category);
        card.setStage(stage);
        card.setNextReviewDate(LocalDate.now().toString());
        return cards.save(card);
    }
}
