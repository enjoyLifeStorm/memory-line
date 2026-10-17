package com.memoryline.entity;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;

import com.memoryline.constant.VoiceProviderType;

import lombok.Data;

/**
 * 发音源配置实体
 *
 * @author congwu.lei
 */
@Data
@Entity
@Table(name = "tb_voice_config")
public class VoiceConfigEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 发音源提供商类型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "provider_type", nullable = false, unique = true, length = 32)
    private VoiceProviderType providerType;

    /**
     * 发音源显示名称，如 "百度标准中文语音"
     */
    @Column(name = "provider_name", nullable = false, length = 64)
    private String providerName;

    /**
     * API 或 URL 模板，支持 {text}, {lang}, {speed} 占位符
     */
    @Column(name = "api_url_template", length = 512)
    private String apiUrlTemplate;

    /**
     * 第三方平台 AppKey / APIKey (可选)
     */
    @Column(name = "app_key", length = 128)
    private String appKey;

    /**
     * 第三方平台 SecretKey (可选)
     */
    @Column(name = "secret_key", length = 128)
    private String secretKey;

    /**
     * 默认语速 (0.5 ~ 1.5)
     */
    @Column(name = "speed")
    private Double speed = 0.75;

    /**
     * 音调 (0.5 ~ 1.5)
     */
    @Column(name = "pitch")
    private Double pitch = 1.0;

    /**
     * 音色标识
     */
    @Column(name = "voice_persona", length = 64)
    private String voicePersona;

    /**
     * 是否为全局激活的发音引擎
     */
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = false;

    /**
     * 更新时间
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void onPrePersist() {
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onPreUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
