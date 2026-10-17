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
 * 百度语音合成发音提供商实现
 *
 * @author congwu.lei
 */
@Slf4j
@Component
public class BaiduVoiceProvider implements VoiceProvider {

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public VoiceProviderType getProviderType() {
        return VoiceProviderType.BAIDU_TTS;
    }

    @Override
    public String generateAudioUrl(String text, String category, VoiceConfigEntity config) {
        String encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8);
        String lang = "LETTER".equalsIgnoreCase(category) ? "en" : "zh";
        String template = (config != null && config.getApiUrlTemplate() != null && !config.getApiUrlTemplate().isBlank())
                ? config.getApiUrlTemplate()
                : "https://fanyi.baidu.com/gettts?lan={lang}&text={text}&spd=4&source=web";

        return template.replace("{text}", encodedText)
                .replace("{lang}", lang)
                .replace("{speed}", "4");
    }

    @Override
    public byte[] synthesizeAudioBytes(String text, String category, VoiceConfigEntity config) {
        String url = generateAudioUrl(text, category, config);
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/120.0.0.0 Safari/537.36");
            headers.set(HttpHeaders.REFERER, "https://fanyi.baidu.com/");
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<byte[]> response = restTemplate.exchange(url, HttpMethod.GET, entity, byte[].class);
            return response.getBody();
        } catch (Exception e) {
            log.error("Failed to fetch audio from Baidu TTS: {}", url, e);
            return new byte[0];
        }
    }
}
