package com.memoryline.repository;
import java.util.List;
import com.memoryline.entity.RecallResultEntity;
import org.springframework.data.jpa.repository.JpaRepository;
public interface RecallResultRepository extends JpaRepository<RecallResultEntity,String> {
    List<RecallResultEntity> findByRecallDate(String recallDate);
}
