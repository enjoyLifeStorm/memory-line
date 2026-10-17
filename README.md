# 小树苗练字记忆曲线工具 (Memory-Line)

> 专为幼儿园大班至小学一年级幼小衔接儿童设计的全场景练字与记忆曲线复习系统。

---

## 🌟 核心特性

1. **幼小衔接四大品类全覆盖**：
   - **基础汉字**：标准米字格/田字格，十字虚线与对角线辅助观察间架重心；
   - **阿拉伯数字 (0~9)**：标准 2:3 宽高比日字格，带有横向腰线虚线与 7° 倾斜导引，专治儿童数字倒写与比例失调；
   - **汉语拼音**：标准四线三格，中间格柔和高亮，帮助分清 `b/d/p/q` 等上下出头规则；
   - **英文字母**：大小写标准手写体占位。

2. **多模态发音配置中心 (Voice Provider Hub)**：
   - **百度语音 (Baidu TTS)**：支持百度标准中文语音合成，声音自然柔和；
   - **网易有道词典发音 (Youdao Voice)**：支持汉字、拼音和英语标准原声音频；
   - **系统原生离线语音 (Web Speech API)**：无需网络、免配置，支持 0.75x 慢速跟读；
   - **自定义 API 模板 (Custom TTS)**：支持在后台配置自定义第三方语音合成 URL 模板；
   - 系统支持一键切换生效引擎、在线试听测试，配置持久化于 H2 数据库。

3. **高保真动态运笔笔顺演示**：
   - 纯矢量 SVG 路径插值动画，带有笔尖红点光晕光标动态循迹；
   - 伴随生动的儿歌口诀（如“2像小鸭水上漂，半圆弯弯平底横”）与运笔要领同步引导。

4. **具体公历复习日历 (Calendar Schedule)**：
   - 废弃抽象的“Day+N”，直接映射为真实公历日期（YYYY-MM-DD）；
   - 直观查看历史哪天练了多少个、未来哪天有到期复习任务，避免复习扎堆。

5. **SM-2 改良版自适应遗忘调度**：
   - 提供 3 星反馈机制（需重练 / 良好 / 熟练）；
   - 太难则安排次日重新强化，熟练则顺延阶梯复习周期；
   - 配合小树成长与墨汁能量激励体系。

---

## 🛠️ 技术架构

- **后端**：Java 17, Spring Boot 2.7.18, Spring Data JPA, Lombok
- **数据库**：嵌入式 H2 Database (File-based: `./temp/data/memory_line`)
- **日志**：Logback 异步滚动日志 (限制 300MB，保留 60 天)
- **前端**：响应式现代 HTML5 / CSS3 / ES6+（前后端一体打包）

---

## 🚀 快速启动

### 方式一：Maven 编译并运行
```bash
# 1. 编译打包
mvn clean package

# 2. 启动服务
java -jar target/memory-line-1.0.0.jar
```

### 方式二：访问服务
- **系统主页**：[http://localhost:8088/](http://localhost:8088/)
- **H2 控制台**：[http://localhost:8088/h2-console](http://localhost:8088/h2-console)
  - JDBC URL: `jdbc:h2:file:./temp/data/memory_line`
  - 用户名: `sa`，密码为空

---

## 📁 目录规范

```text
├── guide/
│   ├── req.md          # 需求规格说明书
│   └── dev.md          # 系统架构与技术设计文档
├── prototype/
│   └── index.html      # 纯前端离线原型
├── src/
│   ├── main/
│   │   ├── java/com/memoryline/
│   │   │   ├── autoconfig/     # 配置类 (遵守 autoconfig 命名规范)
│   │   │   ├── constant/       # 枚举定义
│   │   │   ├── controller/     # RESTful 控制器
│   │   │   ├── dto/            # 数据传输对象
│   │   │   ├── entity/         # JPA 实体
│   │   │   ├── initializer/    # 数据初始化种子
│   │   │   ├── repository/     # 数据仓库
│   │   │   └── service/        # 业务逻辑与策略模式适配器
│   │   └── resources/
│   │       ├── application.yml # 配置文件 (YAML 格式)
│   │       ├── logback-spring.xml # 异步滚动日志配置
│   │       └── static/         # 前端静态单页面
│   └── test/                   # 单元测试
└── pom.xml
```

---

## 📄 开源许可证
本项目遵循 MIT 协议开源。
