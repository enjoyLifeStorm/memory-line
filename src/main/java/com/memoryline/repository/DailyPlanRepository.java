package com.memoryline.repository;
import com.memoryline.entity.DailyPlanEntity;
import org.springframework.data.jpa.repository.JpaRepository;
public interface DailyPlanRepository extends JpaRepository<DailyPlanEntity, String> {}
