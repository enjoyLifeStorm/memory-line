package com.memoryline.dto;

import com.memoryline.constant.VoiceProviderType;

import lombok.Data;

/**
 * 发音源配置传输对象
 *
 * @author congwu.lei
 */
@Data
public class VoiceConfigDTO {

    /**
     * 发音源提供商类型
     */
    private VoiceProviderType providerType;

    /**
     * 发音源显示名称
     */
    private String providerName;

    /**
     * API 或 URL 模板
     */
    private String apiUrlTemplate;

    /**
     * 第三方平台 AppKey / APIKey
     */
    private String appKey;

    /**
     * 第三方平台 SecretKey
     */
    private String secretKey;

    /**
     * 默认语速 (0.5 ~ 1.5)
     */
    private Double speed;

    /**
     * 音调 (0.5 ~ 1.5)
     */
    private Double pitch;

    /**
     * 音色标识
     */
    private String voicePersona;

    /**
     * 是否激活
     */
    private Boolean isActive;
}
