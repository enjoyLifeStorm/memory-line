package com.memoryline.controller;

import java.util.List;

import com.memoryline.constant.VoiceProviderType;
import com.memoryline.dto.VoiceConfigDTO;
import com.memoryline.entity.VoiceConfigEntity;
import com.memoryline.service.VoiceService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 多源发音库配置与合成播放控制器
 *
 * @author congwu.lei
 */
@RestController
@RequestMapping("/api/voice")
@RequiredArgsConstructor
public class VoiceController {

    private final VoiceService voiceService;

    /**
     * 获取所有可用的发音源配置
     *
     * @return 发音配置列表
     */
    @GetMapping("/configs")
    public ResponseEntity<List<VoiceConfigEntity>> getAllConfigs() {
        return ResponseEntity.ok(voiceService.getAllConfigs());
    }

    /**
     * 获取当前生效的发音源配置
     *
     * @return 当前激活发音配置
     */
    @GetMapping("/active")
    public ResponseEntity<VoiceConfigEntity> getActiveConfig() {
        return ResponseEntity.ok(voiceService.getActiveConfig());
    }

    /**
     * 切换当前激活的发音引擎
     *
     * @param providerType 发音提供商类型
     * @return 激活的发音配置
     */
    @PutMapping("/active/{providerType}")
    public ResponseEntity<VoiceConfigEntity> setActiveProvider(@PathVariable("providerType") VoiceProviderType providerType) {
        return ResponseEntity.ok(voiceService.setActiveProvider(providerType));
    }

    /**
     * 更新发音源参数配置
     *
     * @param dto 发音源传输对象
     * @return 更新后的发音配置
     */
    @PutMapping("/configs")
    public ResponseEntity<VoiceConfigEntity> updateConfig(@RequestBody VoiceConfigDTO dto) {
        return ResponseEntity.ok(voiceService.updateConfig(dto));
    }

    /**
     * 获取当前引擎生成的试听 URL
     *
     * @param text 文本
     * @param category 品类
     * @return URL 包装
     */
    @GetMapping("/preview-url")
    public ResponseEntity<String> getPreviewUrl(
            @RequestParam("text") String text,
            @RequestParam(value = "category", required = false, defaultValue = "HANZI") String category) {
        return ResponseEntity.ok(voiceService.getAudioUrl(text, category));
    }

    /**
     * 服务端合成音频流代理（解决跨域与直连限制）
     *
     * @param text 文本
     * @param category 品类
     * @return 音频流响应
     */
    @GetMapping("/synthesize")
    public ResponseEntity<byte[]> synthesize(
            @RequestParam("text") String text,
            @RequestParam(value = "category", required = false, defaultValue = "HANZI") String category) {
        byte[] audioBytes = voiceService.synthesizeAudio(text, category);
        if (audioBytes == null || audioBytes.length == 0) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("audio/mpeg"));
        headers.setContentLength(audioBytes.length);
        headers.setCacheControl("public, max-age=86400");
        return new ResponseEntity<>(audioBytes, headers, HttpStatus.OK);
    }
}
