# Yonsei Korean Learner

> 基于《延世韩国语》课程体系打造的现代化全功能 Android 韩语学习应用与自然语言算法实践工程。

[![Platform](https://img.shields.io/badge/Platform-Android-3DDC84?style=flat-square&logo=android&logoColor=white)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-7F52FF?style=flat-square&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20%7C%20Material%203-4285F4?style=flat-square)](https://developer.android.com/jetpack/compose)
[![Architecture](https://img.shields.io/badge/Architecture-Clean%20%7C%20MVI-009688?style=flat-square)](https://developer.android.com/topic/architecture)
[![License](https://img.shields.io/badge/License-MIT-black?style=flat-square)](./LICENSE)
[![Release](https://img.shields.io/badge/Release-v1.0.48-blue?style=flat-square)](https://github.com/uminrae/Yonsei-Korean-Learner/releases/latest)

---

## 概述

**Yonsei Korean Learner**（应用名：未来的韩语）是一套面向严肃韩语学习者的移动端完整解决方案。项目以经典《延世韩国语》（第 1 册至第 6 册）教学大纲为骨架，系统性整理了覆盖初级、中级到高级的全部核心词汇与语法规则，并将自然语言处理算法（音变推导、变位推导、隔写校验）与现代认知科学（艾宾浩斯记忆模型、语调高频采样）深度融合于 Android 客户端中。

项目采用 100% Kotlin 与 Jetpack Compose 响应式声明式 UI 开发，遵循 Clean Architecture 架构分层规范，具备高内聚低耦合的代码组织与全套单元测试覆盖。

---

## 核心算法与语言学引擎

本项目不仅是学习内容的承载容器，底层实现了一整套韩语语言学与自然语言处理（NLP）本地化算法引擎：

- **发音推导与音变规则引擎 (Hangul Pronunciation Engine)**  
  完整实现韩语严式同化、连音化、紧音化、气音化、头音规则等底层音变逻辑，能对任意输入词组自动推导其实际发音与 IPA 国际音标。

- **基频音高追踪与影子跟读系统 (YIN Pitch Detector & Shadowing Trainer)**  
  移植并优化了音频工程中的经典 YIN 基频提取算法，在移动端低延迟环境下实时捕获并可视化学习者的语音 Pitch 曲线，与标准首尔音调曲线进行动态时间规整（DTW）对比，提供可视化音高跟读反馈。

- **动词/形容词变位与敬语阶称转置器 (Conjugation & Speech Levels Engine)**  
  覆盖所有规则变位与 7 类不规则变位（ㄷ, ㅂ, ㅅ, ㅡ, 르, ㅎ, 여 不规则），支持在格代式、准敬阶、非格式体（해요体）、半语（해体）之间实时转置，并内置间接引语变位演算。

- **艾宾浩斯遗忘曲线复习调度算法 (Memory Scheduler)**  
  基于遗忘率数学模型，对用户的单词记忆状态、反应时、错误频度进行动态权重运算，自动生成个性化的每日复习计划与间隔调度。

- **汉字词同根拓扑网络 (Hanja Network)**  
  建立韩语汉字词与汉语语素的拓扑映射网络，帮助学习者通过字根逻辑快速推导并联想记忆海量中高级词汇。

- **语法积木拼装系统 (Syntax Lego Engine)**  
  将复合长句拆解为主谓宾、格助词、连接词尾和终结词尾，提供可视化的语法节点交互拼装校验。

- **自动隔写校对引擎 (Korean Spacing Checker)**  
  针对韩语学习中最易混淆的词间空格与助词连写规范，提供语法级的规则检查与智能纠错建议。

---

## 功能模块矩阵

| 模块分类 | 包含功能与特性 | 关键技术实现 |
| :--- | :--- | :--- |
| **词汇核心** | 延世 1~6 册全量词汇库、双向翻转词卡、无限流分页漫游、听写默写测试 | Room Paging 3, FTS 全文检索 |
| **语法全解** | 体系化分类语法库、接续规则详解、真实例句库、关联易混淆语法比对 | Json Serializer, Expandable Composables |
| **口语与发音** | 字母笔顺动态绘制、40 音音标实验室、实时音高轨迹绘制、影子跟读训练 | Canvas Drawing, AudioRecord API, YIN Algorithm |
| **实战语料** | K-Pop 歌词同步听写、经典篇章分级精读、韩国流行俚语语料库、成语溯源故事 | LRC Parser, Graded Reader Flow |
| **能力自测** | TOPIK 题型模拟自测、短文写作指导与范文精析、词汇接龙对抗游戏 | Finite State Machine, Random Seed Generator |
| **桌面小部件** | 每日一词桌面卡片、复习进度追踪小组件 | Jetpack Glance, Glance Material 3, WorkManager |

---

## 技术规范与架构设计

### 架构模式
工程严格践行 **Clean Architecture** 分层模型，数据流与状态控制统一遵循 **MVI / 单向数据流 (UDF)**：

```text
[ Data Layer ]                [ Domain Layer ]              [ UI Layer ]
Local Database (Room)   --->   Use Cases & Repositories ---> ViewModels (StateFlow)
Assets & Seed Importers --->   NLP Algorithm Engines    ---> Compose Screens & Components
```

### 依赖技术栈

| 维度 | 技术栈 / 依赖库 | 版本 | 用途 |
| :--- | :--- | :--- | :--- |
| **开发语言** | Kotlin | 2.0.21 | 强类型现代化语言、协程支持、Compose 编译器插件 |
| **构建系统** | Android Gradle Plugin (AGP) | 8.8.0 | Gradle Kotlin DSL、版本目录 (libs.versions.toml) |
| **UI 框架** | Jetpack Compose (BOM) | 2024.12.01 | 声明式响应式界面开发 |
| **设计规范** | Material Design 3 | 最新稳定版 | 现代化色彩系统、动态无障碍适配 |
| **依赖注入** | Dagger Hilt | 2.52 | 全局依赖注入容器与作用域管理 |
| **本地持久化** | Jetpack Room | 2.6.1 | 关系型 SQLite 抽象层、支持 FTS4 全文检索 |
| **数据分页** | Jetpack Paging 3 | 3.3.5 | 支撑海量词条的无缝懒加载流 |
| **异步流** | Kotlinx Coroutines & Flow | 1.9.0 | 结构化并发与响应式状态流 |
| **桌面部件** | AndroidX Glance | 1.1.1 | 基于 Compose 语法的交互式 AppWidget |
| **后台调度** | AndroidX WorkManager | 2.10.0 | 每日复习提醒与离线缓存定时更新 |
| **性能基准** | Baseline Profile / Benchmark | 1.4.1 | 预编译优化启动耗时与首屏帧率 |

---

## 工程目录结构

```text
.
├── android_arch/                             # Android 客户端工程根目录
│   ├── app/                                  # 主应用模块
│   │   ├── src/main/assets/                  # 内置预打包词汇与语法种子数据
│   │   │   ├── words_seed.json               # 核心单词结构化定义
│   │   │   └── grammars_seed.json            # 核心语法结构化定义
│   │   ├── src/main/kotlin/com/yonsei/koreanlearner/
│   │   │   ├── algorithm/                    # 语言学与 NLP 核心算法实现
│   │   │   ├── data/                         # 数据访问层 (DAO, Entity, Database, Repository)
│   │   │   ├── di/                           # Hilt 依赖注入依赖组装模块
│   │   │   ├── domain/                       # 领域实体与交互用例
│   │   │   ├── ui/                           # 展现层 (Screen, ViewModel, Component, Theme)
│   │   │   └── widget/                       # Glance 桌面微件实现
│   │   └── src/test/kotlin/                  # 算法引擎单元测试集
│   ├── benchmark/                            # Macrobenchmark 与基准生成模块
│   ├── gradle/                               # Gradle 包装器与版本目录配置
│   ├── build.gradle.kts                      # 根构建配置
│   └── settings.gradle.kts                   # 模块挂载配置
├── build_dataset.py                          # 原始资料解析与结构化 ETL 管道
├── generate_final_seeds.py                   # 终版种子数据合成与校验脚本
├── validate_dataset.py                       # 知识库完整性校验与 Schema 断言工具
├── words_seed.json                           # 词汇知识库种子文件 (JSON)
├── grammars_seed.json                        # 语法知识库种子文件 (JSON)
├── LICENSE                                   # MIT 开源许可证
├── .gitignore                                # 忽略规则定义
└── README.md                                 # 项目说明文档
```

---

## 知识库构建与 ETL 管道

知识库的数据生命周期由 Python ETL 脚本集保障，确保离线数据在压入应用资产包前满足强类型约束：

1. **数据清洗与构建**：运行 `build_dataset.py` 解析结构化学习语料。
2. **种子数据生成**：通过 `generate_final_seeds.py` 对单词例句、词性、变位类型以及语法接续进行交叉校验，输出标准 JSON 种子。
3. **数据完整性校验**：执行 `validate_dataset.py` 进行字段存在性与数据边界断言。
4. **客户端幂等注入**：Android 客户端启动时通过 `DatabaseSeeder` 在后台 IO 线程以事务方式将种子数据无感注入 Room 数据库。

---

## 构建与运行指南

### 环境准备

- **JDK**：OpenJDK 17 或以上版本
- **Android SDK**：API Level 35 (Compile SDK: 35, Min SDK: 26)
- **开发工具**：Android Studio Ladybug (2024.2) 或更高版本

### 命令行构建

```bash
# 1. 克隆代码仓库
git clone https://github.com/uminrae/Yonsei-Korean-Learner.git
cd Yonsei-Korean-Learner/android_arch

# 2. 运行全量单元测试（验证自然语言引擎核心算法）
./gradlew test

# 3. 构建 Release 正式安装包
./gradlew assembleRelease
```

产物输出目录位于 `android_arch/app/build/outputs/apk/release/`。

---

## 安装包下载

最新编译就绪的 APK 安装包通过 GitHub Releases 进行分发：

- **版本发布页面**：[GitHub Releases](https://github.com/uminrae/Yonsei-Korean-Learner/releases/latest)
- **最新安装包直接下载**：[YonseiKoreanLearner-v1.0.48.apk](https://github.com/uminrae/Yonsei-Korean-Learner/releases/download/v1.0.48/YonseiKoreanLearner-v1.0.48.apk)

---

## 开源许可证 (License)

本项目采用 **MIT License**（麻省理工学院许可证）开源，详情请参阅 [LICENSE](./LICENSE) 文件全文。

```text
MIT License

Copyright (c) 2026 uminrae

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.
```

---

## 免责声明与知识产权说明

1. **教育与学术研究用途**：本项目为开源技术实践与个人学习辅助工具，开发初衷为自然语言处理技术在语言教学中的探索与应用。
2. **课程体系参考**：项目中包含的基础词汇列表、词性分类及语法解释框架主要参考并对齐了广泛公认的《延世韩国语》（Yonsei Korean）课程教学大纲与公开知识点。项目代码及数据分发不包含任何未经授权的受版权保护的原始扫描件、影音教材录音或商业排版文件。
3. **内容准确性**：尽管已通过自动化测试与数据校验机制对知识库进行了严格核对，但因语言演化及个人整理局限，软件内呈现之例句与语法推导仅供语言自学参考，不构成任何专业语言等级考试官方定论。
4. **第三方商标**：文中提及之《延世韩国语》、Yonsei 等名称仅作学习脉络事实性指引，其相关商标与知识产权归原权利人所有。
