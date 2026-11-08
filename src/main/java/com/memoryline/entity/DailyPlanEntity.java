package com.memoryline.entity;
import javax.persistence.*;
import lombok.Data;
@Data
@Entity
@Table(name = "tb_daily_plan")
public class DailyPlanEntity {
    @Id private String planDate;
    @Column(length=4000) private String reviewIds = "";
    @Column(length=4000) private String newIds = "";
    @Column(length=4000) private String extraIds = "";
    private String subject;
    private String lesson;
    private Integer newLimit;
    private Boolean recallEnabled;
}
