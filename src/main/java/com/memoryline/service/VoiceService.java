package com.memoryline.service;

import java.util.List;

import com.memoryline.constant.VoiceProviderType;
import com.memoryline.dto.VoiceConfigDTO;
import com.memoryline.entity.VoiceConfigEntity;

/**
 * 发音源综合管理服务接口
 *
 * @author congwu.lei
 */
public interface VoiceService {

    /**
     * 获取所有可用的发音源配置列表
     *
     * @return 发音源配置列表
     */
    List<VoiceConfigEntity> getAllConfigs();

    /**
     * 获取当前处于激活生效状态的发音源配置
     *
     * @return 激活的发音源配置
     */
    VoiceConfigEntity getActiveConfig();

    /**
     * 设置并切换全局生效的发音引擎
     *
     * @param providerType 发音源类型
     * @return 切换后生效的配置
     */
    VoiceConfigEntity setActiveProvider(VoiceProviderType providerType);

    /**
     * 更新指定发音源的参数配置
     *
     * @param dto 配置更新传输对象
     * @return 更新后的发音配置
     */
    VoiceConfigEntity updateConfig(VoiceConfigDTO dto);

    /**
     * 获取指定文本的音频播放 URL
     *
     * @param text 文本
     * @param category 字符品类
     * @return 音频访问链接
     */
    String getAudioUrl(String text, String category);

    /**
     * 合成并获取指定文本的音频流字节
     *
     * @param text 文本
     * @param category 字符品类
     * @return 音频流字节数组
     */
    byte[] synthesizeAudio(String text, String category);
}
