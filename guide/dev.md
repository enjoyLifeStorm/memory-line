# 小孩练字记忆曲线工具系统架构与技术设计文档 (dev.md)

## 1. 系统总体架构设计

本系统采用现代轻量级单体全栈架构（Spring Boot + H2 Database + 嵌入式响应式前端），专为家庭幼小衔接练字场景量身定制。支持零外部依赖一键启动，具备离线可用性与云端扩展性。

```
+-----------------------------------------------------------------------------------+
|                              前端交互层 (Presentation Layer)                        |
|   +-----------------------+  +-----------------------+  +----------------------+  |
|   | 幼小衔接4合1书写工作台 |  | SVG 动态运笔笔顺演示  |  | 真实公历排期日历看板 |  |
|   +-----------------------+  +-----------------------+  +----------------------+  |
|   | 慢速朗读与口诀播放器  |  | 3星自适应反馈与树成长 |  | 多源发音配置管理中心 |  |
|   +-----------------------+  +-----------------------+  +----------------------+  |
+-----------------------------------------------------------------------------------+
                                          | RESTful API (JSON / Audio Stream)
+-----------------------------------------------------------------------------------+
|                              服务端业务层 (Spring Boot Core)                       |
|   +-----------------------+  +-----------------------+  +----------------------+  |
|   |   CardController      |  |   CalendarController  |  |   VoiceConfigControl |  |
|   +-----------------------+  +-----------------------+  +----------------------+  |
|   |   CardService         |  |   CalendarService     |  |   VoiceProviderHub   |  |
|   |   (SM-2 自适应调度)   |  |   (真实公历聚合)      |  |   (百度/有道/自定义) |  |
|   +-----------------------+  +-----------------------+  +----------------------+  |
|                              Spring Data JPA 仓库层                               |
+-----------------------------------------------------------------------------------+
                                          | JDBC
+-----------------------------------------------------------------------------------+
|                              持久化存储层 (Persistence)                           |
|   - 嵌入式 H2 关系型数据库 (File-based: `./data/memory_line`)                     |
|   - H2 Web Console 控制台 (路径: `/h2-console`)                                   |
|   - 内置幼小衔接四大品类基础字库数据种子 (DataInitializer)                        |
+-----------------------------------------------------------------------------------+
```

---

## 2. 数据库与领域模型设计 (Domain Models)

### 2.1 实体设计与表结构

#### 1. 练习卡片表 (`tb_practice_card`)
存储汉字、数字、拼音、英文字母及运笔、记忆状态。
```sql
CREATE TABLE tb_practice_card (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    character_val VARCHAR(64) NOT NULL,            -- 字符或单词（如 "2", "b", "春", "Aa"）
    pinyin VARCHAR(64),                             -- 拼音标注（如 "chūn", "èr", "b"）
    category VARCHAR(32) NOT NULL,                  -- 品类：HANZI(汉字), NUMBER(数字), PINYIN(拼音), LETTER(字母)
    chant VARCHAR(255),                             -- 幼小衔接记忆口诀童谣
    stroke_count INT DEFAULT 1,                     -- 笔画总数
    stroke_paths TEXT,                              -- SVG 笔画路径序列 (JSON 数组)
    stroke_hints TEXT,                              -- 运笔要领说明 (JSON 数组)
    grid_type VARCHAR(32) DEFAULT 'MI_ZI_GE',       -- 适用书写格：MI_ZI_GE, RI_ZI_GE, FOUR_LINE
    stage INT DEFAULT 0,                            -- 艾宾浩斯阶段 (0:未学, 1~6:巩固中, 7:永久掌握)
    easiness_factor DOUBLE DEFAULT 2.5,             -- SM-2 熟练度因子
    next_review_date VARCHAR(10),                   -- 下次复习公历日期 (YYYY-MM-DD)
    last_practiced_date VARCHAR(10),                -- 上次练习公历日期 (YYYY-MM-DD)
    total_reviews INT DEFAULT 0,                    -- 累计复习次数
    is_mastered BOOLEAN DEFAULT FALSE,              -- 是否已完全掌握
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_card_next_review ON tb_practice_card(next_review_date);
CREATE INDEX idx_card_category ON tb_practice_card(category);
```

#### 2. 打卡练习日志表 (`tb_practice_log`)
记录每次练习评分与阶段跃迁。
```sql
CREATE TABLE tb_practice_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    card_id BIGINT NOT NULL,                        -- 关联卡片 ID
    character_val VARCHAR(64) NOT NULL,            -- 字符快照
    category VARCHAR(32) NOT NULL,                  -- 品类
    rating INT NOT NULL,                            -- 打分反馈：1(需重练), 2(良好), 3(熟练)
    pre_stage INT NOT NULL,                         -- 练习前阶段
    post_stage INT NOT NULL,                        -- 练习后阶段
    review_date VARCHAR(10) NOT NULL,               -- 打卡公历日期 (YYYY-MM-DD)
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_log_review_date ON tb_practice_log(review_date);
CREATE INDEX idx_log_card_id ON tb_practice_log(card_id);
```

#### 3. 发音源配置表 (`tb_voice_config`)
存储百度 TTS、有道发音、自定义 API 及原生配置。
```sql
CREATE TABLE tb_voice_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    provider_type VARCHAR(32) NOT NULL UNIQUE,     -- 提供商代码：WEB_SPEECH, BAIDU_TTS, YOUDAO_VOICE, CUSTOM_API
    provider_name VARCHAR(64) NOT NULL,            -- 友好显示名称（如 "百度标准中文语音"）
    api_url_template VARCHAR(512),                 -- URL模板，支持 {text}, {lang}, {speed} 等占位符
    app_key VARCHAR(128),                          -- 厂商 AppKey / APIKey（可选）
    secret_key VARCHAR(128),                       -- 厂商 SecretKey（可选）
    speed DOUBLE DEFAULT 0.75,                     -- 默认语速 (0.5 ~ 1.5)
    pitch DOUBLE DEFAULT 1.0,                      -- 音调
    voice_persona VARCHAR(64),                     -- 音色标识（如度小美、甜美女声）
    is_active BOOLEAN DEFAULT FALSE,               -- 是否作为全局当前生效发音引擎
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

---

## 3. 多源发音库体系设计 (Voice & Audio Engine)

### 3.1 架构设计与适配器模式
为了支持百度、有道、自定义发音及原生 Web Speech 的无缝插拔，系统采用**策略模式（Strategy Pattern）**与**发音适配器（Voice Provider Adapter）**：

```
                    +-----------------------------+
                    |    VoiceProvider (接口)     |
                    +-----------------------------+
                    | + getProviderType(): String |
                    | + buildAudioUrl(): String   |
                    | + synthesize(text): byte[]  |
                    +-----------------------------+
                                   ▲
      +----------------------------+----------------------------+
      |                            |                            |
+-------------------+      +-------------------+      +-------------------+
|  BaiduVoiceProvider|     | YoudaoVoiceProvider|    | CustomUrlProvider |
+-------------------+      +-------------------+      +-------------------+
| 百度翻译/TTS合成   |     | 有道真人原声音频  |     | 支持自定义URL占位符|
| spd/lan/pdt 参数  |     | audio/le 接口参数 |     | 如私有TTS服务     |
+-------------------+      +-------------------+      +-------------------+
```

### 3.2 发音源配置项详解
1. **浏览器原生 (WEB_SPEECH)**：
   - 依赖浏览器端 `window.speechSynthesis`，服务端无需请求外部网络，返回标识，前端自主调用。
2. **百度发音源 (BAIDU_TTS)**：
   - 默认 URL 模板：`https://fanyi.baidu.com/gettts?lan=zh&text={text}&spd=4&source=web`
   - 支持后端代理流或前端直连播放，发音标准柔和，适合中文及词句口诀。
3. **有道发音源 (YOUDAO_VOICE)**：
   - 中文及拼音 URL 模板：`https://dict.youdao.com/dictvoice?audio={text}&le=zh`
   - 英文单字母/单词 URL 模板：`https://dict.youdao.com/dictvoice?audio={text}&type=2`
4. **自定义发音源 (CUSTOM_API)**：
   - 允许用户在设置界面配置自定义 TTS 接口模板，例如 `https://tts.example.com/speak?q={text}&v={speed}`。

---

## 4. 记忆曲线自适应算法 (SM-2 Modified)

公历调度推算逻辑：
- 若用户打分评级为 **1星（需重练）**：
  - `stage = 1`
  - `nextReviewDate = 当前日期 + 1天`
  - `easinessFactor = Math.max(1.3, easinessFactor - 0.2)`
- 若用户打分评级为 **2星（良好）**：
  - `stage = stage + 1`
  - 间隔按标准阶梯：`Stage 1 -> 1天`, `Stage 2 -> 2天`, `Stage 3 -> 4天`, `Stage 4 -> 7天`, `Stage 5 -> 15天`, `Stage 6 -> 30天`
  - `nextReviewDate = 当前日期 + 阶段对应天数`
- 若用户打分评级为 **3星（熟练）**：
  - `stage = stage + 1`
  - `easinessFactor = easinessFactor + 0.15`
  - `intervalDays = Math.round(阶段基准天数 * (easinessFactor / 2.5))`
  - `nextReviewDate = 当前日期 + intervalDays`

---

## 5. RESTful API 规范

### 5.1 练习卡片接口 (`/api/cards`)
- `GET /api/cards/today`：获取今日待练任务（新学卡片 + 达到复习日期的卡片）。
- `GET /api/cards?category=HANZI`：按品类筛选卡片列表。
- `POST /api/cards`：新增自定义练习字符。
- `POST /api/cards/{id}/review`：提交练习评分反馈（`rating`: 1|2|3），计算下一次公历复习日期。
- `GET /api/cards/export`：全量导出练习卡片、发音配置与学习记录数据为 JSON 备份。
- `POST /api/cards/import`：解析并恢复导入 JSON 备份数据，实现跨设备无缝迁移。

### 5.2 记忆日历接口 (`/api/calendar`)
- `GET /api/calendar/summary?yearMonth=2026-10`：获取某月的日历统计数据（每日历史练习数、未来待复习数、打卡卡片列表）。
- `GET /api/calendar/day-details?date=2026-10-17`：获取特定公历日期的所有任务与完成明细。

### 5.3 发音配置与试听接口 (`/api/voice`)
- `GET /api/voice/configs`：获取所有支持的发音源及当前激活项。
- `PUT /api/voice/active`：切换当前激活的发音源。
- `PUT /api/voice/configs/{providerType}`：更新指定发音源的 API 模板、AppKey、语速等参数。
- `GET /api/voice/synthesize?text={text}&type={type}`：后端代理生成/重定向音频流，解决跨域与直连问题。

---

## 6. 全套进阶优化体系设计 (Advanced Features)

### 6.1 A4 标准字帖排版与打印引擎 (@media print)
- **纸张规格**：适配标准 A4 纵向排版（210mm × 297mm），边距设为 10mm。
- **行格布局**：每行一个汉字/字母/拼音/数字，标准 8 格布局：
  - 第 1 格：标准示范字（包含拼音标注、笔顺拆解或示范运笔）。
  - 第 2~4 格：描红虚线字（50% 浅灰 + 虚线轮廓或浅色虚线字，供孩子沿线描红）。
  - 第 5~8 格：空白对照格（带米字格/日字格/四线三格辅助虚线，供孩子独立书写）。
- **打印优化**：采用 CSS `@media print` 媒体查询，自动隐藏顶部导航、控制面板、打分操作栏，保持纯净纸质字帖输出；页面顶部包含宝贝姓名、练习日期、自我打星与家长签字栏。

### 6.2 屏幕手写/描红板 (HTML5 Canvas 交互层)
- **双层架构**：底层为标准田字格/米字格与半透明示范字（`z-index: 1`），顶层为透明的高清 `<canvas>` 画板（`z-index: 2`）。
- **触控与平滑笔触**：使用 Pointer Events（`pointerdown`, `pointermove`, `pointerup`）统一支持鼠标、触控笔（Apple Pencil 等）与手指划线；通过二次贝塞尔曲线实现流畅笔触，开启 `touch-action: none` 防止移动端拖拽回弹。
- **辅助开关**：提供“底字可见/半透明/隐藏”三段式开关，让孩子完成“描红 -> 半临摹 -> 盲默写”三阶跃迁；提供一键撤销与清空画板功能。

### 6.3 Web Audio 原生激励音效与小树苗成长体系
- **零依赖原生音频合成**：使用浏览器 `AudioContext` 实时合成正弦波音阶（C5: 523.25Hz, E5: 659.25Hz, G5: 783.99Hz, C6: 1046.50Hz），打分 3 星播放轻快明亮的清脆三和弦，连击时频率倍乘，无需任何外部 mp3 网络请求，零延迟、永不 404。
- **能量与树苗演进**：
  - 3 星评价奖励 15 滴墨水能量，2 星奖励 8 滴能量，1 星鼓励复习。
  - 累计能量驱动小树苗从“🌱 萌芽” -> “🌿 小树苗” -> “🌳 葱郁大树” -> “🍎 硕果累累”，配合 CSS 粒子与彩带爆破动效。
- **成就勋章**：包含“初出茅庐”、“连续打卡 3 天”、“全能小能手”、“书写大师”等成就勋章系统。

### 6.4 镜像与易混淆专项辨析模块 (Mirror Letter Distinguisher)
- 幼小衔接儿童因视知觉空间定向发育特征，极易产生镜像反写混淆（如 b/d、p/q、2/3、土/士 等）。
- 系统内置专项对比视图与顺口溜口诀：
  - `b` vs `d`：右下半圆 bbb，左下半圆 ddd / 左手竖拇指是大 b，右手竖拇指是大 d。
  - `p` vs `q`：右上半圆 ppp，左上半圆 qqq。
  - `2` vs `3`：小鸭子 2 脖子向右弯，耳朵 3 两个半圆朝右边。
  - `土` vs `士`：土下横长，士上横长。
- 支持一键进入易混淆卡片对比练习模式，专项攻克。

### 6.5 家长生字录入与自定义管理
- 提供前端简易录入弹窗与后端 `/api/cards` 接口对接：
  - 字段包括：字符内容、品类（汉字/数字/拼音/字母）、拼音、儿歌口诀、适用格型（米字格/日字格/四线三格）、所属单元课文。
  - 录入后即刻纳入艾宾浩斯初始阶段（Stage 0），可在今日新学任务中立即调取练习。

### 6.6 日历日程下钻与负荷预警
- 点击公历日历中任意日期单元格，展开该日任务明细抽屉：
  - 呈现当日新学卡片、复习卡片清单及完成状态。
  - 针对任务量超过 15 个字的日期，触发黄色/红色“⚠️ 负荷预警：今日任务偏多，建议分早晚两场练习，保护宝贝视力与书写兴趣”。

### 6.7 数据全量备份与跨设备恢复
- **导出 (Export)**：后端将 `tb_practice_card`、`tb_voice_config` 及打卡统计打包为 JSON 文件下发，文件名为 `memory-line-backup-YYYYMMDD.json`。
- **导入 (Import)**：支持上传备份 JSON 文件，采用主键/字符特征匹配（`UPSERT` 逻辑），恢复复习进度与自定义字库，避免多端练习数据丢失。

### 6.8 PWA 轻应用全屏化支持
- 配置 `manifest.json`，声明应用名称、图标、主题色与 `display: standalone` 模式。
- 在平板（iPad / Android Pad）与手机上支持“添加到主屏幕”，全屏无浏览器地址栏干扰，模拟原生儿童早教平板 App 沉浸体验。

---

## 7. 技术规约与编码标准

为确保代码健壮性与高质量，严格遵守以下项目规约：
1. **类注释与作者**：所有 Java 类注释必须包含 `@author congwu.lei`，严禁包含 `@since`。
2. **方法注释**：所有公共业务方法提供合规 Javadoc 注释；重写方法（`@Override`）无需冗余注释。
3. **Lombok 规范**：使用 `@Data` 与 `@Slf4j`，禁止使用 `@Builder`。
4. **日志规范**：日志必须基于 Lombok `@Slf4j` 引入；使用 Logback 异步滚动日志配置，单机限制总大小 300MB，最多保留 60 天数据。
5. **包名规范**：配置类包名必须为 `com.memoryline.autoconfig`，绝对不能命名为 `config`。
6. **配置文件格式**：Spring Boot 配置文件必须为 `application.yml`，严禁使用 `properties` 格式。
7. **编码格式**：全工程文件（Java、YAML、HTML、XML等）一律必须使用 `UTF-8` 编码。
8. **Import 导入规范**：
   - 第一组：`java.*` 与 `javax.*`
   - 第二组：第三方类库（`com.*`, `io.*`, `org.*`, `cn.hutool.*`, `lombok` 等）
   - 第三组：静态导入 `import static ...`
   - 每组内字母升序排列，组间空一行，严禁星号导入。
9. **构建与打包规范**：先执行 `mvn clean`，再执行 `mvn package`，必须全面验证并生成可执行 jar 包。
