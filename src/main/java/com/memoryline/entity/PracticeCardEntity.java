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
import javax.persistence.Lob;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;

import com.memoryline.constant.CardCategory;
import com.memoryline.constant.GridType;

import lombok.Data;

/**
 * 练字字符卡片实体
 *
 * @author congwu.lei
 */
@Data
@Entity
@Table(name = "tb_practice_card", indexes = {
    @Index(name = "idx_card_next_review", columnList = "next_review_date"),
    @Index(name = "idx_card_category", columnList = "category")
})
public class PracticeCardEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 练习字符或文字，如 "2", "b", "春", "Aa"
     */
    @Column(name = "character_val", nullable = false, length = 64)
    private String characterVal;

    /**
     * 拼音注音
     */
    @Column(name = "pinyin", length = 64)
    private String pinyin;

    /**
     * 所属品类 (HANZI, NUMBER, PINYIN, LETTER)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 32)
    private CardCategory category;

    /**
     * 幼小衔接童谣口诀
     */
    @Column(name = "chant", length = 255)
    private String chant;

    /**
     * 笔画总数
     */
    @Column(name = "stroke_count")
    private Integer strokeCount = 1;

    /**
     * SVG 笔画路径数据 (JSON 数组字符串)
     */
    @Lob
    @Column(name = "stroke_paths")
    private String strokePaths;

    /**
     * 笔画要领说明 (JSON 数组字符串)
     */
    @Lob
    @Column(name = "stroke_hints")
    private String strokeHints;

    /**
     * 适用书写格类型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "grid_type", length = 32)
    private GridType gridType = GridType.MI_ZI_GE;

    /**
     * 艾宾浩斯复习阶段：0:未学, 1~6:阶段巩固, 7:已完全掌握
     */
    @Column(name = "stage")
    private Integer stage = 0;

    /**
     * SM-2 熟练度因子，默认 2.5
     */
    @Column(name = "easiness_factor")
    private Double easinessFactor = 2.5;

    /**
     * 下次计划复习的公历日期 (格式: YYYY-MM-DD)
     */
    @Column(name = "next_review_date", length = 10)
    private String nextReviewDate;

    /**
     * 上一次练习完成的公历日期 (格式: YYYY-MM-DD)
     */
    @Column(name = "last_practiced_date", length = 10)
    private String lastPracticedDate;

    /**
     * 累计复习次数
     */
    @Column(name = "total_reviews")
    private Integer totalReviews = 0;

    /**
     * 是否已完全掌握
     */
    @Column(name = "is_mastered")
    private Boolean isMastered = false;

    /**
     * 创建时间
     */
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void onPrePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onPreUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
