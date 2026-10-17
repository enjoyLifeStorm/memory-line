package com.memoryline.service.impl;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.PostConstruct;

import com.memoryline.constant.VoiceProviderType;
import com.memoryline.dto.VoiceConfigDTO;
import com.memoryline.entity.VoiceConfigEntity;
import com.memoryline.repository.VoiceConfigRepository;
import com.memoryline.service.VoiceProvider;
import com.memoryline.service.VoiceService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 发音源综合管理服务实现类
 *
 * @author congwu.lei
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VoiceServiceImpl implements VoiceService {

    private final VoiceConfigRepository voiceConfigRepository;
    private final List<VoiceProvider> voiceProviders;

    private final Map<VoiceProviderType, VoiceProvider> providerMap = new EnumMap<>(VoiceProviderType.class);

    @PostConstruct
    public void init() {
        for (VoiceProvider provider : voiceProviders) {
            providerMap.put(provider.getProviderType(), provider);
        }
    }

    @Override
    public List<VoiceConfigEntity> getAllConfigs() {
        return voiceConfigRepository.findAll();
    }

    @Override
    public VoiceConfigEntity getActiveConfig() {
        return voiceConfigRepository.findByIsActiveTrue()
                .orElseGet(() -> voiceConfigRepository.findByProviderType(VoiceProviderType.WEB_SPEECH)
                        .orElse(null));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public VoiceConfigEntity setActiveProvider(VoiceProviderType providerType) {
        List<VoiceConfigEntity> all = voiceConfigRepository.findAll();
        VoiceConfigEntity target = null;
        for (VoiceConfigEntity config : all) {
            if (config.getProviderType() == providerType) {
                config.setIsActive(true);
                target = config;
            } else {
                config.setIsActive(false);
            }
        }
        voiceConfigRepository.saveAll(all);
        return target;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public VoiceConfigEntity updateConfig(VoiceConfigDTO dto) {
        if (dto.getProviderType() == null) {
            throw new IllegalArgumentException("Provider type cannot be null");
        }
        VoiceConfigEntity entity = voiceConfigRepository.findByProviderType(dto.getProviderType())
                .orElseGet(() -> {
                    VoiceConfigEntity newConfig = new VoiceConfigEntity();
                    newConfig.setProviderType(dto.getProviderType());
                    return newConfig;
                });

        if (dto.getProviderName() != null) {
            entity.setProviderName(dto.getProviderName());
        }
        if (dto.getApiUrlTemplate() != null) {
            entity.setApiUrlTemplate(dto.getApiUrlTemplate());
        }
        if (dto.getAppKey() != null) {
            entity.setAppKey(dto.getAppKey());
        }
        if (dto.getSecretKey() != null) {
            entity.setSecretKey(dto.getSecretKey());
        }
        if (dto.getSpeed() != null) {
            entity.setSpeed(dto.getSpeed());
        }
        if (dto.getPitch() != null) {
            entity.setPitch(dto.getPitch());
        }
        if (dto.getVoicePersona() != null) {
            entity.setVoicePersona(dto.getVoicePersona());
        }
        if (dto.getIsActive() != null && dto.getIsActive()) {
            setActiveProvider(dto.getProviderType());
        } else {
            voiceConfigRepository.save(entity);
        }
        return entity;
    }

    @Override
    public String getAudioUrl(String text, String category) {
        VoiceConfigEntity activeConfig = getActiveConfig();
        if (activeConfig == null) {
            return "";
        }
        VoiceProvider provider = providerMap.get(activeConfig.getProviderType());
        if (provider == null) {
            return "";
        }
        return provider.generateAudioUrl(text, category, activeConfig);
    }

    @Override
    public byte[] synthesizeAudio(String text, String category) {
        VoiceConfigEntity activeConfig = getActiveConfig();
        if (activeConfig == null) {
            return new byte[0];
        }
        VoiceProvider provider = providerMap.get(activeConfig.getProviderType());
        if (provider == null) {
            return new byte[0];
        }
        return provider.synthesizeAudioBytes(text, category, activeConfig);
    }
}
