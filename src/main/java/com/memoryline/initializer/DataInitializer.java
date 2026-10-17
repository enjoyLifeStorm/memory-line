package com.memoryline.initializer;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

import com.memoryline.constant.CardCategory;
import com.memoryline.constant.GridType;
import com.memoryline.constant.VoiceProviderType;
import com.memoryline.entity.PracticeCardEntity;
import com.memoryline.entity.PracticeLogEntity;
import com.memoryline.entity.VoiceConfigEntity;
import com.memoryline.repository.PracticeCardRepository;
import com.memoryline.repository.PracticeLogRepository;
import com.memoryline.repository.VoiceConfigRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 幼小衔接预置练习卡片与发音源数据初始化器
 *
 * @author congwu.lei
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final PracticeCardRepository practiceCardRepository;
    private final PracticeLogRepository practiceLogRepository;
    private final VoiceConfigRepository voiceConfigRepository;

    @Override
    public void run(String... args) {
        initVoiceConfigs();
        initPracticeCards();
    }

    /**
     * 初始化多渠道发音源配置
     */
    private void initVoiceConfigs() {
        if (voiceConfigRepository.count() > 0) {
            return;
        }
        log.info("Initializing default voice provider configurations...");

        // 1. 百度标准语音 TTS
        VoiceConfigEntity baiduConfig = new VoiceConfigEntity();
        baiduConfig.setProviderType(VoiceProviderType.BAIDU_TTS);
        baiduConfig.setProviderName("百度标准语音 (Baidu TTS)");
        baiduConfig.setApiUrlTemplate("https://fanyi.baidu.com/gettts?lan={lang}&text={text}&spd=4&source=web");
        baiduConfig.setSpeed(0.75);
        baiduConfig.setPitch(1.0);
        baiduConfig.setVoicePersona("度小美/标准童声");
        baiduConfig.setIsActive(true);

        // 2. 网易有道发音源
        VoiceConfigEntity youdaoConfig = new VoiceConfigEntity();
        youdaoConfig.setProviderType(VoiceProviderType.YOUDAO_VOICE);
        youdaoConfig.setProviderName("网易有道词典真人发音 (Youdao Voice)");
        youdaoConfig.setApiUrlTemplate("https://dict.youdao.com/dictvoice?audio={text}&le={lang}");
        youdaoConfig.setSpeed(0.8);
        youdaoConfig.setPitch(1.0);
        youdaoConfig.setVoicePersona("标准真人原声");
        youdaoConfig.setIsActive(false);

        // 3. 浏览器原生离线合成 Web Speech API
        VoiceConfigEntity webSpeechConfig = new VoiceConfigEntity();
        webSpeechConfig.setProviderType(VoiceProviderType.WEB_SPEECH);
        webSpeechConfig.setProviderName("系统/浏览器原生离线语音 (Web Speech)");
        webSpeechConfig.setApiUrlTemplate("");
        webSpeechConfig.setSpeed(0.75);
        webSpeechConfig.setPitch(1.15);
        webSpeechConfig.setVoicePersona("浏览器默认女声");
        webSpeechConfig.setIsActive(false);

        // 4. 自定义 API 模板
        VoiceConfigEntity customConfig = new VoiceConfigEntity();
        customConfig.setProviderType(VoiceProviderType.CUSTOM_API);
        customConfig.setProviderName("自定义第三方 TTS 接口 (Custom API)");
        customConfig.setApiUrlTemplate("https://fanyi.baidu.com/gettts?lan=zh&text={text}&spd=4&source=web");
        customConfig.setSpeed(0.75);
        customConfig.setPitch(1.0);
        customConfig.setVoicePersona("自定义");
        customConfig.setIsActive(false);

        voiceConfigRepository.saveAll(Arrays.asList(baiduConfig, youdaoConfig, webSpeechConfig, customConfig));
    }

    /**
     * 初始化幼小衔接基础卡片
     */
    private void initPracticeCards() {
        if (practiceCardRepository.count() > 0) {
            return;
        }
        log.info("Initializing kindergarten transition practice cards...");

        String today = LocalDate.now().format(DATE_FORMATTER);
        String yesterday = LocalDate.now().minusDays(1).format(DATE_FORMATTER);

        // 卡片1：数字 2
        PracticeCardEntity num2 = new PracticeCardEntity();
        num2.setCharacterVal("2");
        num2.setPinyin("èr");
        num2.setCategory(CardCategory.NUMBER);
        num2.setGridType(GridType.RI_ZI_GE);
        num2.setChant("2像小鸭水上漂，半圆弯弯平底横");
        num2.setStrokeCount(2);
        num2.setStrokePaths("[\"M 65 60 C 85 40, 140 40, 140 85 C 140 120, 65 190, 60 215\", \"M 60 215 L 145 215\"]");
        num2.setStrokeHints("[\"右上格起笔画大圆弧，顺时针滑落至左下角\", \"从左下角水平向右拉出平底横\"]");
        num2.setStage(1);
        num2.setEasinessFactor(2.5);
        num2.setNextReviewDate(today);
        num2.setLastPracticedDate(yesterday);
        num2.setTotalReviews(1);

        // 卡片2：拼音 b
        PracticeCardEntity pinyinB = new PracticeCardEntity();
        pinyinB.setCharacterVal("b");
        pinyinB.setPinyin("b");
        pinyinB.setCategory(CardCategory.PINYIN);
        pinyinB.setGridType(GridType.FOUR_LINE);
        pinyinB.setChant("右下半圆 b b b，拉开天线听广播");
        pinyinB.setStrokeCount(2);
        pinyinB.setStrokePaths("[\"M 80 50 L 80 200\", \"M 80 140 C 95 110, 145 110, 145 155 C 145 200, 95 200, 80 200\"]");
        pinyinB.setStrokeHints("[\"从上格中间起笔垂直直下拉到第三线收笔\", \"在中格中间顺时针画出饱满圆润的右下半圆\"]");
        pinyinB.setStage(2);
        pinyinB.setEasinessFactor(2.5);
        num2.setLastPracticedDate(yesterday);
        pinyinB.setNextReviewDate(today);
        pinyinB.setTotalReviews(2);

        // 卡片3：汉字 春
        PracticeCardEntity hanziChun = new PracticeCardEntity();
        hanziChun.setCharacterVal("春");
        hanziChun.setPinyin("chūn");
        hanziChun.setCategory(CardCategory.HANZI);
        hanziChun.setGridType(GridType.MI_ZI_GE);
        hanziChun.setChant("三人同日去踏青，春天到来百花开");
        hanziChun.setStrokeCount(6);
        hanziChun.setStrokePaths("[\"M 65 65 L 135 65\", \"M 75 90 L 125 90\", \"M 50 115 L 150 115\", \"M 100 45 C 95 90, 75 140, 45 165\", \"M 105 118 C 115 130, 135 150, 160 165\", \"M 75 145 L 75 210 M 75 145 L 125 145 L 125 210 M 75 178 L 125 178 M 75 210 L 125 210\"]");
        hanziChun.setStrokeHints("[\"一横较短居中\", \"二横稍短紧随\", \"三横舒展最长\", \"撇画自中轴向左下舒展\", \"捺画右下方放开生姿\", \"日字底居中稳立\"]");
        hanziChun.setStage(0);
        hanziChun.setEasinessFactor(2.5);
        hanziChun.setNextReviewDate(today);
        hanziChun.setTotalReviews(0);

        // 卡片4：字母 Aa
        PracticeCardEntity letterA = new PracticeCardEntity();
        letterA.setCharacterVal("Aa");
        letterA.setPinyin("eɪ");
        letterA.setCategory(CardCategory.LETTER);
        letterA.setGridType(GridType.FOUR_LINE);
        letterA.setChant("大A像山峰，小a小肚子带小尾巴");
        letterA.setStrokeCount(3);
        letterA.setStrokePaths("[\"M 65 50 L 35 190\", \"M 65 50 L 95 190\", \"M 45 135 L 85 135\"]");
        letterA.setStrokeHints("[\"大写A从顶点斜向下左撇\", \"从顶点斜向下右拉\", \"腰间搭上一根横木梁\"]");
        letterA.setStage(0);
        letterA.setEasinessFactor(2.5);
        letterA.setNextReviewDate(today);
        letterA.setTotalReviews(0);

        List<PracticeCardEntity> savedCards = practiceCardRepository.saveAll(Arrays.asList(num2, pinyinB, hanziChun, letterA));

        // 模拟昨天的打卡日志
        PracticeLogEntity yesterdayLog = new PracticeLogEntity();
        yesterdayLog.setCardId(savedCards.get(0).getId());
        yesterdayLog.setCharacterVal("2");
        yesterdayLog.setCategory(CardCategory.NUMBER);
        yesterdayLog.setRating(3);
        yesterdayLog.setPreStage(0);
        yesterdayLog.setPostStage(1);
        yesterdayLog.setReviewDate(yesterday);
        practiceLogRepository.save(yesterdayLog);
    }
}
