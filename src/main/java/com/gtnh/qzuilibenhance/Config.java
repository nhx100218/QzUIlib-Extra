package com.gtnh.qzuilibenhance;

import com.gtnh.qzuilibenhance.tooltip.TooltipConfig;
import com.gtnh.qzuilibenhance.vanilla.ScreenTransition;
import com.gtnh.qzuilibenhance.vanilla.SmoothScrollConfig;

import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

/**
 * 附属增强的 Forge 配置（{@code config/qzuilibenhance.cfg}）。启动时回灌到各运行态静态字段。
 */
public final class Config {

    private static Configuration sConfiguration;

    private Config() {}

    public static Configuration getConfiguration() {
        return sConfiguration;
    }

    public static void load(FMLPreInitializationEvent event) {
        sConfiguration = new Configuration(event.getSuggestedConfigurationFile());
        apply(sConfiguration);
        if (sConfiguration.hasChanged()) {
            sConfiguration.save();
        }
        MyMod.LOG.info("附属增强配置已加载");
    }

    /** 从给定 Configuration 回灌全部运行态字段（配置页保存后即时生效）。 */
    public static void apply(Configuration cfg) {
        if (cfg == null) {
            return;
        }

        TooltipConfig.enabled = cfg.getBoolean("enabled", "tooltip", TooltipConfig.enabled,
                "启用圆角 tooltip 样式");
        TooltipConfig.rounded = cfg.getBoolean("rounded", "tooltip", TooltipConfig.rounded, "使用圆角");
        TooltipConfig.centerTitle = cfg.getBoolean("centerTitle", "tooltip", TooltipConfig.centerTitle,
                "标题行居中");
        TooltipConfig.titleBreak = cfg.getBoolean("titleBreak", "tooltip", TooltipConfig.titleBreak,
                "标题下画分隔线");
        TooltipConfig.adaptiveColors = cfg.getBoolean("adaptiveColors", "tooltip", TooltipConfig.adaptiveColors,
                "按物品稀有度/名称颜色自适应边框");
        TooltipConfig.borderColorCycle = cfg.getInt("borderColorCycle", "tooltip", TooltipConfig.borderColorCycle,
                0, 10000, "边框颜色循环周期(ms)，0=关闭");
        TooltipConfig.borderWidth = cfg.getFloat("borderWidth", "tooltip", TooltipConfig.borderWidth,
                0.0F, 8.0F, "边框宽度");
        TooltipConfig.cornerRadius = cfg.getFloat("cornerRadius", "tooltip", TooltipConfig.cornerRadius,
                0.0F, 16.0F, "圆角半径");
        TooltipConfig.modularUiMaxWidth = cfg.getInt("modularUiMaxWidth", "tooltip", TooltipConfig.modularUiMaxWidth,
                0, 2000, "ModularUI2(格雷)tooltip 最大折行宽度，0=自动");
        TooltipConfig.modularUiNoWrap = cfg.getBoolean("modularUiNoWrap", "tooltip", TooltipConfig.modularUiNoWrap,
                "禁用 ModularUI2 文本自动折行（影响所有 ModularUI 文本）");
        readColors(cfg, "fillColor", TooltipConfig.fillColor, "背景四角颜色(ARGB)");
        readColors(cfg, "strokeColor", TooltipConfig.strokeColor, "边框四角颜色(ARGB)");

        TooltipConfig.waila = cfg.getBoolean("enabled", "waila", TooltipConfig.waila, "Waila HUD 框圆角");
        TooltipConfig.wailaCornerRadius = cfg.getFloat("cornerRadius", "waila", TooltipConfig.wailaCornerRadius,
                0.0F, 16.0F, "Waila 圆角半径");
        TooltipConfig.wailaBorderWidth = cfg.getFloat("borderWidth", "waila", TooltipConfig.wailaBorderWidth,
                0.0F, 8.0F, "Waila 边框宽度");

        SmoothScrollConfig.enabled = cfg.getBoolean("enabled", "smoothScroll", SmoothScrollConfig.enabled,
                "原版列表平滑滚动");
        SmoothScrollConfig.factor = cfg.getFloat("factor", "smoothScroll", SmoothScrollConfig.factor,
                0.05F, 1.0F, "平滑系数(越大越快)");

        ScreenTransition.enabled = cfg.getBoolean("enabled", "screenTransition", ScreenTransition.enabled,
                "界面切换动画");
        ScreenTransition.durationMs = cfg.getInt("durationMs", "screenTransition", ScreenTransition.durationMs,
                0, 2000, "动画时长(ms)");
        ScreenTransition.offset = cfg.getFloat("offset", "screenTransition", ScreenTransition.offset,
                0.0F, 64.0F, "位移幅度(px)");
        ScreenTransition.scale = cfg.getFloat("scale", "screenTransition", ScreenTransition.scale,
                0.0F, 4.0F, "位移缩放");

        com.gtnh.qzuilibenhance.vanilla.ZoomConfig.enabled = cfg.getBoolean("enabled", "zoom",
                com.gtnh.qzuilibenhance.vanilla.ZoomConfig.enabled, "按 C 缩放");
        com.gtnh.qzuilibenhance.vanilla.ZoomConfig.factor = cfg.getFloat("factor", "zoom",
                com.gtnh.qzuilibenhance.vanilla.ZoomConfig.factor, 1.0F, 20.0F, "缩放倍率");
        com.gtnh.qzuilibenhance.vanilla.ZoomConfig.smooth = cfg.getBoolean("smooth", "zoom",
                com.gtnh.qzuilibenhance.vanilla.ZoomConfig.smooth, "平滑过渡");
        com.gtnh.qzuilibenhance.vanilla.ZoomConfig.speed = cfg.getFloat("speed", "zoom",
                com.gtnh.qzuilibenhance.vanilla.ZoomConfig.speed, 0.02F, 1.0F, "过渡速度");
        com.gtnh.qzuilibenhance.vanilla.ZoomConfig.sensitivity = cfg.getFloat("sensitivity", "zoom",
                com.gtnh.qzuilibenhance.vanilla.ZoomConfig.sensitivity, 1.0F, 5.0F, "灵敏度额外减速系数(越大越慢)");

        com.gtnh.qzuilibenhance.vanilla.ChatConfig.smoothEnabled = cfg.getBoolean("smoothEnabled", "chat", com.gtnh.qzuilibenhance.vanilla.ChatConfig.smoothEnabled, "chat message fade-in");
        com.gtnh.qzuilibenhance.vanilla.ChatConfig.durationMs = cfg.getInt("durationMs", "chat", com.gtnh.qzuilibenhance.vanilla.ChatConfig.durationMs, 0, 2000, "fade-in duration(ms)");
        com.gtnh.qzuilibenhance.vanilla.ChatConfig.offset = cfg.getFloat("offset", "chat", com.gtnh.qzuilibenhance.vanilla.ChatConfig.offset, 0.0F, 32.0F, "slide offset(px)");
        com.gtnh.qzuilibenhance.vanilla.ChatConfig.headsEnabled = cfg.getBoolean("headsEnabled", "chat", com.gtnh.qzuilibenhance.vanilla.ChatConfig.headsEnabled, "chat heads");
        com.gtnh.qzuilibenhance.vanilla.ChatConfig.headSize = cfg.getFloat("headSize", "chat", com.gtnh.qzuilibenhance.vanilla.ChatConfig.headSize, 4.0F, 16.0F, "head size(px)");
        com.gtnh.qzuilibenhance.vanilla.ChatConfig.headGap = cfg.getFloat("headGap", "chat", com.gtnh.qzuilibenhance.vanilla.ChatConfig.headGap, 0.0F, 8.0F, "head gap(px)");
        com.gtnh.qzuilibenhance.vanilla.ZoomConfig.keyCode = cfg.getInt("keyCode", "zoom",
                com.gtnh.qzuilibenhance.vanilla.ZoomConfig.keyCode, 0, 255, "触发按键 LWJGL 键码(默认 46=C)");

        com.gtnh.qzuilibenhance.vanilla.UnfocusedConfig.framerateEnabled = cfg.getBoolean("unfocusedFramerate",
                "performance", com.gtnh.qzuilibenhance.vanilla.UnfocusedConfig.framerateEnabled, "窗口失焦时降低帧率");
        com.gtnh.qzuilibenhance.vanilla.UnfocusedConfig.framerateLimit = cfg.getInt("unfocusedFramerateLimit",
                "performance", com.gtnh.qzuilibenhance.vanilla.UnfocusedConfig.framerateLimit, 1, 120, "失焦帧率上限");
        com.gtnh.qzuilibenhance.vanilla.UnfocusedConfig.volumeEnabled = cfg.getBoolean("unfocusedVolume", "performance",
                com.gtnh.qzuilibenhance.vanilla.UnfocusedConfig.volumeEnabled, "窗口失焦时降低音量");
        com.gtnh.qzuilibenhance.vanilla.UnfocusedConfig.volumeFactor = cfg.getFloat("unfocusedVolumeFactor",
                "performance", com.gtnh.qzuilibenhance.vanilla.UnfocusedConfig.volumeFactor, 0.0F, 1.0F, "失焦音量系数");

        com.gtnh.qzuilibenhance.vanilla.UiConfig.textUndoEnabled = cfg.getBoolean("textUndoEnabled", "ui",
                com.gtnh.qzuilibenhance.vanilla.UiConfig.textUndoEnabled, "文本框 Ctrl+Z 撤销 / Ctrl+Y 重做");
        com.gtnh.qzuilibenhance.vanilla.UiConfig.textUndoLimit = cfg.getInt("textUndoLimit", "ui",
                com.gtnh.qzuilibenhance.vanilla.UiConfig.textUndoLimit, 1, 1000, "撤销历史步数上限");

        com.gtnh.qzuilibenhance.vanilla.VanillaPauseConfig.enabled = cfg.getBoolean("pauseContainers", "ui",
                com.gtnh.qzuilibenhance.vanilla.VanillaPauseConfig.enabled, "单机打开容器界面时暂停世界");

        com.gtnh.qzuilibenhance.vanilla.UiConfig.selectionEnabled = cfg.getBoolean("selectionEnabled", "textSelection",
                com.gtnh.qzuilibenhance.vanilla.UiConfig.selectionEnabled, "文本框选中高亮（替代原版 XOR 蓝色）");
        com.gtnh.qzuilibenhance.vanilla.UiConfig.selectionColor = cfg.getInt("selectionColor", "textSelection",
                com.gtnh.qzuilibenhance.vanilla.UiConfig.selectionColor, 0, 0xFFFFFF, "选中高亮颜色(RGB)");
        com.gtnh.qzuilibenhance.vanilla.UiConfig.selectionAlpha = cfg.getInt("selectionAlpha", "textSelection",
                com.gtnh.qzuilibenhance.vanilla.UiConfig.selectionAlpha, 0, 255, "选中高亮不透明度(0-255)");

        com.gtnh.qzuilibenhance.vanilla.EmojiConfig.enabled = cfg.getBoolean("enabled", "emoji",
                com.gtnh.qzuilibenhance.vanilla.EmojiConfig.enabled, "聊天 :name: 短代码转 emoji（需系统 emoji 字体）");

        localize(cfg);
    }

    /** 为全部分类与属性设置语言键（保持 cfg 短键不变），由 en_us/zh_cn.lang 提供显示名与提示。 */
    private static void localize(Configuration cfg) {
        for (String categoryName : cfg.getCategoryNames()) {
            ConfigCategory category = cfg.getCategory(categoryName);
            category.setLanguageKey("qzuilibenhance.config.cat." + categoryName);
            for (Property property : category.getValues().values()) {
                property.setLanguageKey("qzuilibenhance.config." + categoryName + "." + property.getName());
            }
        }
    }

    private static void readColors(Configuration cfg, String key, int[] target, String comment) {
        String[] defaults = new String[target.length];
        for (int i = 0; i < target.length; i++) {
            defaults[i] = String.format("%08X", target[i]);
        }
        String[] values = cfg.getStringList(key, "tooltip", defaults, comment + "（最多 4 个，顺序 左上/右上/右下/左下）");
        for (int i = 0; i < target.length && i < values.length; i++) {
            try {
                target[i] = (int) Long.parseLong(values[i].replace("#", "").trim(), 16);
            } catch (NumberFormatException ignored) {
                // 保留默认值
            }
        }
    }
}
