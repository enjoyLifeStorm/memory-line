package com.memoryline.service.impl;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import com.memoryline.constant.VoiceProviderType;
import com.memoryline.entity.VoiceConfigEntity;
import com.memoryline.service.VoiceProvider;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * 网易有道发音源提供商实现
 *
 * @author congwu.lei
 */
@Slf4j
@Component
public class YoudaoVoiceProvider implements VoiceProvider {

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public VoiceProviderType getProviderType() {
        return VoiceProviderType.YOUDAO_VOICE;
    }

    @Override
    public String generateAudioUrl(String text, String category, VoiceConfigEntity config) {
        String encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8);
        if ("LETTER".equalsIgnoreCase(category)) {
            return "https://dict.youdao.com/dictvoice?audio=" + encodedText + "&type=2";
        }
        return "https://dict.youdao.com/dictvoice?audio=" + encodedText + "&le=zh";
    }

    @Override
    public byte[] synthesizeAudioBytes(String text, String category, VoiceConfigEntity config) {
        String url = generateAudioUrl(text, category, config);
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/120.0.0.0 Safari/537.36");
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<byte[]> response = restTemplate.exchange(url, HttpMethod.GET, entity, byte[].class);
            return response.getBody();
        } catch (Exception e) {
            log.error("Failed to fetch audio from Youdao Voice: {}", url, e);
            return new byte[0];
        }
    }
}
