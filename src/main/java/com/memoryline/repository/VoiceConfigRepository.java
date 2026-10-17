package com.memoryline.repository;

import java.util.Optional;

import com.memoryline.constant.VoiceProviderType;
import com.memoryline.entity.VoiceConfigEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 发音源配置数据访问接口
 *
 * @author congwu.lei
 */
@Repository
public interface VoiceConfigRepository extends JpaRepository<VoiceConfigEntity, Long> {

    /**
     * 根据发音源类型查找配置
     *
     * @param providerType 发音源类型
     * @return 配置可选对象
     */
    Optional<VoiceConfigEntity> findByProviderType(VoiceProviderType providerType);

    /**
     * 查找当前处于激活生效状态的发音源配置
     *
     * @return 激活的发音配置
     */
    Optional<VoiceConfigEntity> findByIsActiveTrue();
}
