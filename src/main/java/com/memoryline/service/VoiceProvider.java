package com.memoryline.service;

import com.memoryline.constant.VoiceProviderType;
import com.memoryline.entity.VoiceConfigEntity;

/**
 * 发音提供商适配策略接口
 *
 * @author congwu.lei
 */
public interface VoiceProvider {

    /**
     * 获取支持的发音源类型
     *
     * @return 发音源枚举类型
     */
    VoiceProviderType getProviderType();

    /**
     * 构建音频流访问链接
     *
     * @param text 需发音文本或字符
     * @param category 字符品类
     * @param config 发音配置
     * @return 音频访问链接
     */
    String generateAudioUrl(String text, String category, VoiceConfigEntity config);

    /**
     * 服务端合成音频流字节数据
     *
     * @param text 需发音文本或字符
     * @param category 字符品类
     * @param config 发音配置
     * @return 音频字节数组
     */
    byte[] synthesizeAudioBytes(String text, String category, VoiceConfigEntity config);
}
