package com.memoryline.entity;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.PrePersist;
import javax.persistence.Table;

import com.memoryline.constant.CardCategory;

import lombok.Data;

/**
 * 练字打卡历史日志实体
 *
 * @author congwu.lei
 */
@Data
@Entity
@Table(name = "tb_practice_log", indexes = {
    @Index(name = "idx_log_review_date", columnList = "review_date"),
    @Index(name = "idx_log_card_id", columnList = "card_id")
})
public class PracticeLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 关联练习卡片主键 ID
     */
    @Column(name = "card_id", nullable = false)
    private Long cardId;

    /**
     * 练习字符快照
     */
    @Column(name = "character_val", nullable = false, length = 64)
    private String characterVal;

    /**
     * 所属品类
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 32)
    private CardCategory category;

    /**
     * 本次打分反馈：1: 需重练, 2: 良好, 3: 熟练
     */
    @Column(name = "rating", nullable = false)
    private Integer rating;

    /**
     * 练习前艾宾浩斯阶段
     */
    @Column(name = "pre_stage", nullable = false)
    private Integer preStage;

    /**
     * 练习后艾宾浩斯阶段
     */
    @Column(name = "post_stage", nullable = false)
    private Integer postStage;

    /**
     * 打卡练习公历日期 (格式: YYYY-MM-DD)
     */
    @Column(name = "review_date", nullable = false, length = 10)
    private String reviewDate;

    /**
     * 创建时间
     */
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void onPrePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
