package com.memoryline.service.impl;

import com.memoryline.constant.VoiceProviderType;
import com.memoryline.entity.VoiceConfigEntity;
import com.memoryline.service.VoiceProvider;

import org.springframework.stereotype.Component;

/**
 * 浏览器端原生 Web Speech API 发音提供商（纯客户端离线）
 *
 * @author congwu.lei
 */
@Component
public class WebSpeechVoiceProvider implements VoiceProvider {

    @Override
    public VoiceProviderType getProviderType() {
        return VoiceProviderType.WEB_SPEECH;
    }

    @Override
    public String generateAudioUrl(String text, String category, VoiceConfigEntity config) {
        return "";
    }

    @Override
    public byte[] synthesizeAudioBytes(String text, String category, VoiceConfigEntity config) {
        return new byte[0];
    }
}
