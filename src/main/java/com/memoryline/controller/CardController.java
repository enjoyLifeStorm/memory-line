package com.memoryline.controller;

import java.util.List;

import com.memoryline.constant.CardCategory;
import com.memoryline.dto.ReviewFeedbackDTO;
import com.memoryline.dto.TodayTaskResponseDTO;
import com.memoryline.entity.PracticeCardEntity;
import com.memoryline.service.CardService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

    /**
     * 获取今日练习任务
     *
     * @param date 指定日期 (可选，YYYY-MM-DD)
     * @return 今日任务响应
     */
    @GetMapping("/today")
    public ResponseEntity<TodayTaskResponseDTO> getTodayTasks(@RequestParam(value = "date", required = false) String date) {
        return ResponseEntity.ok(cardService.getTodayTasks(date));
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
}
