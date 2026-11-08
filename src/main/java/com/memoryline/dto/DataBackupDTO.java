package com.memoryline.dto;

import java.util.List;

import com.memoryline.entity.PracticeCardEntity;
import com.memoryline.entity.PracticeLogEntity;
import com.memoryline.entity.VoiceConfigEntity;

import lombok.Data;

/**
 * 全量数据备份与导入传输对象
 *
 * @author congwu.lei
 */
@Data
public class DataBackupDTO {

    /**
     * 数据格式版本
     */
    private String version;

    /**
     * 导出时间 (ISO 8601 字符串)
     */
    private String exportTime;

    /**
     * 练习卡片列表
     */
    private List<PracticeCardEntity> cards;

    /**
     * 发音源配置列表
     */
    private List<VoiceConfigEntity> voiceConfigs;

    /**
     * 练习打卡日志
     */
    private List<PracticeLogEntity> practiceLogs;
}
