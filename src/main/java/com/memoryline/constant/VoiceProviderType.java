package com.memoryline.constant;

/**
 * 发音提供商类型枚举
 *
 * @author congwu.lei
 */
public enum VoiceProviderType {

    /**
     * 浏览器原生 Web Speech API 离线合成
     */
    WEB_SPEECH,

    /**
     * 百度标准语音 (Baidu TTS)
     */
    BAIDU_TTS,

    /**
     * 有道词典真人发音 (Youdao Voice)
     */
    YOUDAO_VOICE,

    /**
     * 自定义 HTTP TTS API 模板
     */
    CUSTOM_API
}
