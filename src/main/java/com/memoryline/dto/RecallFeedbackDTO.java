package com.memoryline.dto;
import lombok.Data;
@Data public class RecallFeedbackDTO {
    private Long cardId;
    private Integer rating;
    private Boolean usedHint;
}
