package com.memoryline.dto;

import lombok.Data;

/**
 * 练习打卡反馈请求数据传输对象
 *
 * @author congwu.lei
 */
@Data
public class ReviewFeedbackDTO {

    /**
     * 卡片主键 ID
     */
    private Long cardId;

    /**
     * 用户反馈评分：1: 需重练(太难), 2: 良好(一般), 3: 熟练(太棒了)
     */
    private Integer rating;
}
