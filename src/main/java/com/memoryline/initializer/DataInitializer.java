package com.memoryline.initializer;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.io.InputStream;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.annotation.Transactional;

import com.memoryline.constant.CardCategory;
import com.memoryline.constant.GridType;
import com.memoryline.constant.VoiceProviderType;
import com.memoryline.entity.PracticeCardEntity;
import com.memoryline.entity.VoiceConfigEntity;
import com.memoryline.repository.PracticeCardRepository;
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
    private final VoiceConfigRepository voiceConfigRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public void run(String... args) {
        initVoiceConfigs();
        initPracticeCards();
        initNumberCards();
        initCurriculum();
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
        num2.setStage(0);
        num2.setEasinessFactor(2.5);
        num2.setNextReviewDate(today);

        num2.setTotalReviews(0);

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
        pinyinB.setStage(0);
        pinyinB.setEasinessFactor(2.5);

        pinyinB.setNextReviewDate(today);
        pinyinB.setTotalReviews(0);

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

        practiceCardRepository.saveAll(Arrays.asList(num2, pinyinB, hanziChun, letterA));
    }

    /** Add missing digits on upgrades without resetting any existing learning record. */
    private void initNumberCards() {
        addNumber("0", "líng", "0像鸡蛋圆溜溜，一笔绕回起点头", "[\"M 105 50 C 45 45, 40 200, 90 210 C 150 220, 155 55, 105 50\"]", "[\"上方起笔，向左下绕圆，回到起点封口\"]", 1);
        addNumber("1", "yī", "1像铅笔细又长，从上到下一笔成", "[\"M 110 50 L 90 210\"]", "[\"从上向下写直线，略向左倾斜\"]", 1);
        addNumber("2", "èr", "2像小鸭水上漂，弯弯脖子平底横", "[\"M 65 65 C 90 30, 150 45, 140 85 C 130 120, 75 170, 60 210 L 145 210\"]", "[\"从左上起笔，向右上弯，再斜向左下，最后向右写平底\"]", 1);
        addNumber("3", "sān", "3像耳朵听声音，上下两弯一笔成", "[\"M 65 60 C 130 20, 160 105, 100 120 C 165 125, 145 230, 60 200\"]", "[\"从左上向右画上半圆，接着写下半圆，下弯稍大\"]", 1);
        addNumber("4", "sì", "4像小旗迎风飘，先折后竖记得牢", "[\"M 115 50 L 55 150 L 150 150\", \"M 130 50 L 110 210\"]", "[\"先从上方向左下斜写，再向右横折\", \"从右上向下写竖，穿过横线\"]", 2);
        addNumber("5", "wǔ", "5像秤钩弯又弯，先竖再弯补短横", "[\"M 75 60 L 65 120 C 145 90, 165 225, 60 200\", \"M 75 60 L 140 60\"]", "[\"先向下写短竖，再向右下画圆弯收笔\", \"从起笔处向右写短横\"]", 2);
        addNumber("6", "liù", "6像哨子吹得响，从上往下绕个圈", "[\"M 130 50 C 70 75, 40 170, 70 200 C 110 240, 165 180, 125 140 C 105 120, 75 135, 65 155\"]", "[\"从右上向左下画弧，再绕出下方圆圈\"]", 1);
        addNumber("7", "qī", "7像镰刀弯又弯，先横再斜一笔完", "[\"M 60 55 L 145 55 L 85 210\"]", "[\"先从左向右写横，再斜向左下写长斜线\"]", 1);
        addNumber("8", "bā", "8像葫芦腰儿细，上下交叉一笔成", "[\"M 105 120 C 30 80, 80 20, 125 55 C 180 100, 35 150, 65 195 C 100 245, 180 190, 105 120\"]", "[\"从腰部向左上绕小圈，交叉向左下绕大圈，回到腰部\"]", 1);
        addNumber("9", "jiǔ", "9像气球带根线，先画圆圈再直下", "[\"M 135 100 C 145 30, 55 35, 55 95 C 55 145, 125 150, 135 100 L 115 210\"]", "[\"先画上方圆圈，回到右侧后向下写长竖\"]", 1);
    }

    private void addNumber(String digit, String pinyin, String chant, String paths, String hints, int strokes) {
        if (practiceCardRepository.findByCharacterValAndCategory(digit, CardCategory.NUMBER).isPresent()) {
            return;
        }
        PracticeCardEntity card = new PracticeCardEntity();
        card.setCharacterVal(digit);
        card.setPinyin(pinyin);
        card.setChant(chant);
        card.setCategory(CardCategory.NUMBER);
        card.setGridType(GridType.RI_ZI_GE);
        card.setStrokePaths(paths);
        card.setStrokeHints(hints);
        card.setStrokeCount(strokes);
        card.setNextReviewDate(LocalDate.now().format(DATE_FORMATTER));
        practiceCardRepository.save(card);
    }

    private void initCurriculum() {
        try (InputStream input = new ClassPathResource("curriculum.json").getInputStream()) {
            List<PracticeCardEntity> curriculum = objectMapper.readValue(input,
                    new TypeReference<List<PracticeCardEntity>>() { });
            for (PracticeCardEntity template : curriculum) {
                PracticeCardEntity card = practiceCardRepository
                        .findByCharacterValAndCategory(template.getCharacterVal(), template.getCategory())
                        .orElseGet(PracticeCardEntity::new);
                // Update teaching content only. Never replace IDs, scheduling or review history.
                card.setCharacterVal(template.getCharacterVal());
                card.setCategory(template.getCategory());
                card.setPinyin(template.getPinyin());
                card.setChant(template.getChant());
                card.setStrokePaths(template.getStrokePaths());
                card.setStrokeHints(template.getStrokeHints());
                card.setStrokeCount(template.getStrokeCount());
                card.setGridType(template.getGridType());
                card.setLearningLevel(template.getLearningLevel());
                card.setLearningOrder(template.getLearningOrder());
                card.setLessonTitle(template.getLessonTitle());
                card.setReadingText(template.getReadingText());
                if (card.getNextReviewDate() == null) {
                    card.setNextReviewDate(LocalDate.now().format(DATE_FORMATTER));
                }
                practiceCardRepository.save(card);
            }
            for (PracticeCardEntity digit : practiceCardRepository.findByCategory(CardCategory.NUMBER)) {
                if (digit.getCharacterVal().matches("[0-9]")) {
                    digit.setLearningLevel(1);
                    digit.setLearningOrder(Integer.parseInt(digit.getCharacterVal()) + 1);
                    digit.setLessonTitle("01 数字 0–9");
                    digit.setReadingText(digit.getCharacterVal());
                }
            }
        } catch (java.io.IOException ex) {
            throw new IllegalStateException("Unable to load local curriculum", ex);
        }
    }
}
