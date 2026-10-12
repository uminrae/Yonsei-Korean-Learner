# 未来的韩语 (Yonsei Korean Learner)

一款现代化的韩语学习 Android 应用程序，基于《延世韩国语》课程体系深度打造，集词汇学习、语法精讲、语调跟读、TOPIK 备考及智能化记忆复习于一体。

---

## 🌟 核心特性

- **体系化知识库**：深度集成《延世韩国语》全套核心单词与语法知识库（内置在应用 assets 及 Room 数据库中）。
- **多维度练习模块**：
  - 词汇卡片与艾宾浩斯智能复习调度系统
  - 语法全解与句型造句（Syntax Lego）
  - 语调发音可视化与影子跟读（Shadowing Trainer）
  - 敬语转换与间接引语变位系统
  - 韩国流行文化（K-Pop 歌词、流行语/俚语精选）
  - TOPIK 模拟试题生成与专项突破
- **现代 Android 技术栈**：
  - **UI**：100% Jetpack Compose + Material 3 现代化界面
  - **架构**：Clean Architecture + MVI/MVVM 架构
  - **依赖注入**：Dagger Hilt
  - **持久化**：Jetpack Room + Paging 3
  - **性能基准**：Baseline Profile 优化启动速度与帧率稳定性

---

## 📁 目录结构

```text
├── android_arch/           # Android 客户端主工程源码
│   ├── app/                # 应用程序模块 (Jetpack Compose, Room, Hilt)
│   │   ├── src/main/assets/ # 应用内置的核心词汇与语法种子数据
│   │   └── src/main/kotlin/ # 业务逻辑与 UI 实现
│   ├── benchmark/          # Baseline Profile 基准性能测试模块
│   └── gradle/             # Gradle 构建依赖与 Version Catalog
├── build_dataset.py        # 词汇与语法数据集解析构建脚本
├── generate_final_seeds.py # 知识库种子生成与校验脚本
├── validate_dataset.py     # 知识库完整性与规范性校验工具
├── words_seed.json         # 核心单词种子数据 (JSON)
├── grammars_seed.json      # 核心语法种子数据 (JSON)
└── .gitignore              # 忽略本地构建缓存与外部参考文档
```

---

## 🚀 安装包下载 (Releases)

编译就绪的最新 APK 安装包可在本仓库的 **[Releases](../../releases)** 页面直接下载体验。

---

## 🛠️ 构建与运行

1. 克隆本仓库：
   ```bash
   git clone <repository_url>
   ```
2. 使用 Android Studio 打开 `android_arch` 目录。
3. 等待 Gradle 同步完成，连接 Android 设备或模拟器后点击 **Run** 即可启动。
