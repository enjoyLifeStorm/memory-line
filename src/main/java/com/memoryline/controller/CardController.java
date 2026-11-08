package com.memoryline.controller;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.memoryline.constant.CardCategory;
import com.memoryline.dto.DataBackupDTO;
import com.memoryline.dto.RecallFeedbackDTO;
import com.memoryline.dto.ReviewFeedbackDTO;
import com.memoryline.dto.TodayTaskResponseDTO;
import com.memoryline.entity.PracticeCardEntity;
import com.memoryline.service.CardService;
import com.memoryline.service.DailyPlanService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * 练字卡片与练习打卡控制器
 *
 * @author congwu.lei
 */
@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;
    private final DailyPlanService dailyPlanService;

    /**
     * 每日计划提取反馈
     *
     * @param feedback 回忆反馈数据
     * @return 计划结果
     */
    @PostMapping("/daily-plan/recall")
    public Map<String, Object> recall(@RequestBody RecallFeedbackDTO feedback) {
        return dailyPlanService.recall(feedback.getCardId(), feedback.getRating(), Boolean.TRUE.equals(feedback.getUsedHint()));
    }

    /**
     * 获取每日计划列表
     *
     * @param newLimit 新卡片数量限制
     * @param category 品类过滤
     * @param lesson 单元课文过滤
     * @return 每日计划详情
     */
    @GetMapping("/daily-plan")
    public Map<String, Object> dailyPlan(
            @RequestParam(defaultValue = "5") int newLimit,
            @RequestParam(required = false) CardCategory category,
            @RequestParam(required = false) String lesson) {
        return dailyPlanService.get(newLimit, category, lesson);
    }

    /**
     * 开启新一轮每日计划
     *
     * @param newLimit 新卡片数量限制
     * @param category 品类过滤
     * @param lesson 单元课文过滤
     * @return 计划详情
     */
    @PostMapping("/daily-plan/start")
    public Map<String, Object> startPlan(
            @RequestParam(defaultValue = "5") int newLimit,
            @RequestParam(required = false) CardCategory category,
            @RequestParam(required = false) String lesson) {
        return dailyPlanService.start(newLimit, category, lesson);
    }

    /**
     * 开启额外练习加练计划
     *
     * @return 计划详情
     */
    @PostMapping("/daily-plan/extra")
    public Map<String, Object> extraPlan() {
        return dailyPlanService.extra();
    }

    /**
     * 获取今日练习任务
     *
     * @param date 指定日期 (可选，YYYY-MM-DD)
     * @param newLimit 新学卡片上限
     * @param category 品类
     * @param lesson 单元课文
     * @return 今日任务响应
     */
    @GetMapping("/today")
    public ResponseEntity<TodayTaskResponseDTO> getTodayTasks(
            @RequestParam(value = "date", required = false) String date,
            @RequestParam(value = "newLimit", defaultValue = "10") int newLimit,
            @RequestParam(value = "category", required = false) CardCategory category,
            @RequestParam(value = "lesson", required = false) String lesson) {
        if (newLimit < 1 || newLimit > 50) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "newLimit must be between 1 and 50");
        }
        return ResponseEntity.ok(cardService.getTodayTasks(date, newLimit, category, lesson));
    }

    /**
     * 提交练习评分反馈
     *
     * @param feedbackDTO 反馈传输对象
     * @param date 打卡日期 (可选)
     * @return 调整后的卡片实体
     */
    @PostMapping("/review")
    public ResponseEntity<PracticeCardEntity> reviewCard(
            @RequestBody ReviewFeedbackDTO feedbackDTO,
            @RequestParam(value = "date", required = false) String date) {
        PracticeCardEntity updated = cardService.reviewCard(feedbackDTO.getCardId(), feedbackDTO.getRating(), date);
        return ResponseEntity.ok(updated);
    }

    /**
     * 按品类查询卡片列表
     *
     * @param category 品类 (可选)
     * @return 卡片列表
     */
    @GetMapping
    public ResponseEntity<List<PracticeCardEntity>> getCardsByCategory(
            @RequestParam(value = "category", required = false) CardCategory category) {
        return ResponseEntity.ok(cardService.getCardsByCategory(category));
    }

    /**
     * 根据 ID 获取卡片详情
     *
     * @param id 卡片 ID
     * @return 卡片实体
     */
    @GetMapping("/{id}")
    public ResponseEntity<PracticeCardEntity> getCardById(@PathVariable("id") Long id) {
        PracticeCardEntity card = cardService.getCardById(id);
        if (card == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(card);
    }

    /**
     * 新增或更新卡片
     *
     * @param card 卡片实体
     * @return 保存后的卡片
     */
    @PostMapping
    public ResponseEntity<PracticeCardEntity> saveCard(@RequestBody PracticeCardEntity card) {
        return ResponseEntity.ok(cardService.saveCard(card));
    }

    /**
     * 全量导出卡片与发音配置数据备份
     *
     * @return 备份数据
     */
    @GetMapping("/export")
    public ResponseEntity<DataBackupDTO> exportData() {
        return ResponseEntity.ok(cardService.exportData());
    }

    /**
     * 导入恢复卡片与发音配置数据备份
     *
     * @param backupDTO 备份数据传输对象
     * @return 导入结果
     */
    @PostMapping("/import")
    public ResponseEntity<Map<String, Object>> importData(@RequestBody DataBackupDTO backupDTO) {
        int count = cardService.importData(backupDTO);
        return ResponseEntity.ok(Collections.singletonMap("importedCardCount", count));
    }
}
