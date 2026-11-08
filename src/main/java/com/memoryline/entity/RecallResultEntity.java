package com.memoryline.entity;
import javax.persistence.*;
import lombok.Data;
@Data @Entity @Table(name="tb_recall_result")
public class RecallResultEntity {
    @Id private String resultKey;
    private String recallDate;
    private Long cardId;
    private Integer rating;
    private Boolean usedHint;
}
