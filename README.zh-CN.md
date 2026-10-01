# Qz UILib Enhance 附属增强

<p align="center">

<a href="README.md">
<img src="https://img.shields.io/badge/Language-English-blue?style=for-the-badge">
</a>

<a href="README.zh-CN.md">
<img src="https://img.shields.io/badge/%E8%AF%AD%E8%A8%80-%E4%B8%AD%E6%96%87-red?style=for-the-badge">
</a>

</p>

<p align="center">

<a href="https://github.com/nhx100218/QzUIlib-Extra/actions/workflows/build.yml">
<img src="https://github.com/nhx100218/QzUIlib-Extra/actions/workflows/build.yml/badge.svg?branch=main">
</a>

<a href="https://github.com/nhx100218/QzUIlib-Extra/releases">
<img src="https://img.shields.io/github/downloads/nhx100218/QzUIlib-Extra/total?label=%E4%B8%8B%E8%BD%BD&style=flat">
</a>

<a href="https://github.com/nhx100218/QzUIlib-Extra/releases/">
<img src="https://img.shields.io/github/v/release/nhx100218/QzUIlib-Extra?style=flat">
</a>

<a href="https://raw.githubusercontent.com/nhx100218/QzUIlib-Extra/main/LICENSE">
<img src="https://img.shields.io/github/license/nhx100218/QzUIlib-Extra?style=flat">
</a>

<img src="https://img.shields.io/github/last-commit/nhx100218/QzUIlib-Extra?color=c78aff&label=%E6%9C%80%E8%BF%91%E6%8F%90%E4%BA%A4&style=flat">

</p>

**QzUILib** 的客户端视觉 / 交互增强附属模组，面向 GTNH（Minecraft 1.7.10）。在不 fork QzUILib 的前提下，叠加圆角提示框、更顺滑的界面动效、聊天体验优化，以及一套完全自绘的 Material Design 3 配置页。

## 🌟 核心亮点
- **界面+动效优化**：以Smooth GUI为蓝本，将动效带回1.7.10，以zoomfiy为蓝本，增加平滑缩放；
- **提示框优化**：增加圆角，将modernUI功能带回1.7.10；
- **内置字体**：随模组分发 Inter Frozen 与思源黑体 CN，并自动释放；附带真正的斜体字面供渲染期斜切使用。

## ✨ 功能

### 提示框（Tooltip）
- 圆角、渐变提示框，可配置圆角半径、边框宽度与四角填充 / 边框颜色。
- 自适应边框颜色（随物品稀有度 / 名称颜色），可选边框颜色循环动画。
- 联动 **NEI**、**格雷（ModularUI2）**、**匠魂冶炼炉**、**Waila**、**任务书（Better Questing）** 与 **Chromatic Tooltips**。
- 可选标题居中与标题分隔线，另提供 ModularUI2 最大折行宽度 / 禁用折行控制。

### 界面与动效
- 界面打开 / 关闭切换动画（时长、位移、缩放可调）。
- 按住缩放，倍率、平滑过渡与灵敏度减速可调。
- 原版列表平滑滚动。
- 自定义文本选中高亮（替换原版 XOR 蓝色选中框）。
- 文本框撤销 / 重做（`Ctrl+Z` / `Ctrl+Y`）。
- 单机打开容器界面时暂停世界。

### 聊天
- 新消息淡入 / 滑入动画。
- 聊天行显示发送者皮肤头像。
- Emoji 短代码转换（`:name:` → emoji，需系统 emoji 字体）。

### 性能
- 窗口失焦时自动降低帧率与音量。

## 📦 环境要求

| 类型 | 版本 |
|------|------|
| Minecraft | 1.7.10（GTNH） |
| **QzUILib**（`qz_uilib`） | 必需 |
| 侧别 | 客户端 |

## 🚀 安装

1. 将 **QzUILib** 安装到 GTNH 的 `mods/` 目录。
2. 从 [Releases](https://github.com/nhx100218/QzUIlib-Extra/releases) 下载最新的 `qzuilibenhance-*.jar`。
3. 放入 `mods/` 目录。
4. 启动游戏。

## 🔧 配置

- Forge 配置：`config/qzuilibenhance.cfg`，可在模组列表点击 **Config** 按钮或按 **Ctrl+K** 打开。
- 配置页中的 **QzUILib** 分区用于编辑 `config/qzuilib-modern.yaml`。多数改动在下次启动后生效。

## 🛠️ 从源码构建

需要 JDK 25。

```bash
./gradlew build
```

产物位于 `build/libs/`。

## 🙏 致谢

感谢 heiqi 的 QzUILib，以及 GTNH 模组工具链。

## 📄 许可证

本项目基于 [MIT 协议](LICENSE) 开源。

## CI 构件

GitHub Action 会构建模组，并将 jar 作为工作流构件上传。
