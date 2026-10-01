# Qz UILib Enhance

<p align="center">

<a href="README.zh-CN.md">
<img src="https://img.shields.io/badge/%E8%AF%AD%E8%A8%80-%E4%B8%AD%E6%96%87-red?style=for-the-badge">
</a>

<a href="README.md">
<img src="https://img.shields.io/badge/Language-English-blue?style=for-the-badge">
</a>

</p>

<p align="center">

<a href="https://github.com/nhx100218/QzUIlib-Extra/actions/workflows/build.yml">
<img src="https://github.com/nhx100218/QzUIlib-Extra/actions/workflows/build.yml/badge.svg?branch=main">
</a>

<a href="https://github.com/nhx100218/QzUIlib-Extra/releases">
<img src="https://img.shields.io/github/downloads/nhx100218/QzUIlib-Extra/total?label=Downloads&style=flat">
</a>

<a href="https://github.com/nhx100218/QzUIlib-Extra/releases/">
<img src="https://img.shields.io/github/v/release/nhx100218/QzUIlib-Extra?style=flat">
</a>

<a href="https://raw.githubusercontent.com/nhx100218/QzUIlib-Extra/main/LICENSE">
<img src="https://img.shields.io/github/license/nhx100218/QzUIlib-Extra?style=flat">
</a>

<img src="https://img.shields.io/github/last-commit/nhx100218/QzUIlib-Extra?color=c78aff&label=Last%20Commit&style=flat">

</p>

A client-side visual/UX addon for **QzUILib** on GTNH (Minecraft 1.7.10). It layers rounded tooltips, smoother UI motion, chat quality-of-life and a fully self-drawn Material Design 3 config screen on top of QzUILib without forking it.

## 🌟 Highlights

- **Modern Material 3 config screen** — icon + text left navigation, wrapping top tabs, rounded field cards, switches, sliders, segmented buttons and dropdowns, plus a bottom Cancel / Apply / Done bar. All controls are hand-drawn and localized (`en_US` / `zh_CN`).
- **QzUILib section in the config screen** — one navigation entry that reads/writes QzUILib's own `config/qzuilib-modern.yaml` (chat frame, picker density, font sort order and font sizes) without touching QzUILib's source.
- **Bundled fonts** — Inter Frozen and Source Han Sans CN are shipped and auto-extracted; a real italic face is included for render-time oblique.

## ✨ Features

### Tooltips
- Rounded, gradient tooltip rendering with configurable corner radius, border width and four-corner fill / stroke colors.
- Adaptive border colors (follows item rarity / name color) and an optional animated border color cycle.
- Integrated with **NEI**, **GregTech (ModularUI2)**, **Tinkers' Construct Smeltery**, **Waila**, **Better Questing** and **Chromatic Tooltips**.
- Optional centered title and title separator, plus ModularUI2 max-width / no-wrap controls.

### Interface & motion
- Screen open / close transition animation (duration, offset, scale).
- Hold-to-zoom with configurable factor, smoothing and sensitivity slowdown.
- Smooth scrolling for vanilla lists.
- Custom text selection highlight (replaces the vanilla XOR box).
- Text undo / redo (`Ctrl+Z` / `Ctrl+Y`) in text fields.
- Pause the world when a container is open in singleplayer.

### Chat
- Fade / slide-in animation for new chat messages.
- Sender skin heads in chat.
- Emoji shortcode conversion (`:name:` → emoji, needs a system emoji font).

### Performance
- Lower framerate and volume while the window is unfocused.

## 📦 Requirements

| Type | Version |
|------|---------|
| Minecraft | 1.7.10 (GTNH) |
| **QzUILib** (`qz_uilib`) | required |
| Side | Client |

## 🚀 Installation

1. Install **QzUILib** into your GTNH `mods/` folder.
2. Download the latest `qzuilibenhance-*.jar` from [Releases](https://github.com/nhx100218/QzUIlib-Extra/releases).
3. Drop it into `mods/`.
4. Launch the game.

## 🔧 Configuration

- Forge config: `config/qzuilibenhance.cfg`, edited in-game from the mod list **Config** button or with **Ctrl+K**.
- The **QzUILib** section of the config screen edits `config/qzuilib-modern.yaml`. Most changes apply on the next launch.

## 🛠️ Build from Source

Requirements: JDK 25.

```bash
./gradlew build
```

The output jar is written to `build/libs/`.

## 🙏 Acknowledgement

Bundled with thanks to QzUILib by heiqi, and the GTNH modding toolchain.

## 📄 License

This project is open-sourced under the [MIT License](LICENSE).

## CI Artifact

The GitHub Action builds the mod and uploads the jar as a workflow artifact.
