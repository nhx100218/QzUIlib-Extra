package com.gtnh.qzuilibenhance;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.gtnh.qzuilibenhance.tooltip.TooltipConfig;
import com.gtnh.qzuilibenhance.vanilla.ScreenTransition;
import com.gtnh.qzuilibenhance.vanilla.SmoothScrollConfig;

import club.heiqi.config.ConfigException;
import club.heiqi.config.runtime.Authority;
import club.heiqi.config.runtime.ConfigManager;
import club.heiqi.config.runtime.DraftBuffer;
import club.heiqi.config.schema.ConfigSchema;
import club.heiqi.config.schema.FieldSpec;
import club.heiqi.config.schema.FieldType;
import club.heiqi.config.schema.SectionSpec;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.common.config.Configuration;

/**
 * 附属增强配置：改用 QzUILib 的 Config 模块（schema + {@link ConfigManager}）承载，
 * 存储于 {@code config/qzuilibenhance.yaml}。配置页由 {@code client/QzEnhanceConfigScreen}
 * 经 {@code ConfigUI.buildScreen} 构建，不再自绘。
 *
 * <p>启动加载 / 配置页保存 / 磁盘重载三条入口统一经 {@link #apply(Authority)} 把权威值回灌到
 * 各运行态静态字段。旧 {@code qzuilibenhance.cfg} 在首次加载（YAML 不存在）时一次性迁入，
 * 之后不互通。</p>
 */
public final class Config {

    /** 新栈配置相对 {@code mcDataDir} 的路径。 */
    public static final String CONFIG_RELATIVE_PATH = "config/qzuilibenhance.yaml";

    private Config() {}

    /** 新栈配置文件的绝对路径。 */
    public static File configFile(File configDir) {
        return new File(configDir, "qzuilibenhance.yaml");
    }

    /**
     * 客户端启动加载：迁移旧 cfg（如需要）→ bootstrap 新栈 YAML → 回灌静态字段。
     */
    public static void load(FMLPreInitializationEvent event) {
        File configDir = event.getModConfigurationDirectory();
        File yaml = configFile(configDir);
        File parent = yaml.getParentFile();
        if (parent != null && !parent.isDirectory()) {
            parent.mkdirs();
        }
        ConfigSchema schema = buildSchema(key -> key);
        migrateLegacy(new File(configDir, "qzuilibenhance.cfg"), yaml, schema);
        try {
            ConfigManager manager = ConfigManager.bootstrap(yaml, schema);
            apply(manager.authority());
            MyMod.LOG.info("附属增强配置已加载: {}", yaml.getAbsolutePath());
        } catch (ConfigException e) {
            MyMod.LOG.error("附属增强配置加载失败，保留当前默认值", e);
        }
    }

    // ---------- Schema ----------

    /** 声明全部设置字段；{@code tr} 负责把语言键翻译为当前语言文本（启动期为原键）。 */
    public static ConfigSchema buildSchema(Function<String, String> tr) {
        return ConfigSchema.builder("qzuilibenhance")
                .title(tr.apply("qzuilibenhance.config.title"))
                .section("tooltip")
                    .title(tr.apply("qzuilibenhance.config.cat.tooltip"))
                    .bool("enabled").defaultValue(TooltipConfig.enabled).label(l(tr, "tooltip", "enabled")).helper(h(tr, "tooltip", "enabled")).build()
                    .bool("rounded").defaultValue(TooltipConfig.rounded).label(l(tr, "tooltip", "rounded")).helper(h(tr, "tooltip", "rounded")).build()
                    .bool("centerTitle").defaultValue(TooltipConfig.centerTitle).label(l(tr, "tooltip", "centerTitle")).helper(h(tr, "tooltip", "centerTitle")).build()
                    .bool("titleBreak").defaultValue(TooltipConfig.titleBreak).label(l(tr, "tooltip", "titleBreak")).helper(h(tr, "tooltip", "titleBreak")).build()
                    .bool("adaptiveColors").defaultValue(TooltipConfig.adaptiveColors).label(l(tr, "tooltip", "adaptiveColors")).helper(h(tr, "tooltip", "adaptiveColors")).build()
                    .integer("borderColorCycle").defaultValue(Long.valueOf(TooltipConfig.borderColorCycle)).range(0, 10000).slider().label(l(tr, "tooltip", "borderColorCycle")).helper(h(tr, "tooltip", "borderColorCycle")).build()
                    .number("borderWidth").defaultValue(Double.valueOf(TooltipConfig.borderWidth)).range(0, 8).slider().label(l(tr, "tooltip", "borderWidth")).helper(h(tr, "tooltip", "borderWidth")).build()
                    .number("cornerRadius").defaultValue(Double.valueOf(TooltipConfig.cornerRadius)).range(0, 16).slider().label(l(tr, "tooltip", "cornerRadius")).helper(h(tr, "tooltip", "cornerRadius")).build()
                    .integer("modularUiMaxWidth").defaultValue(Long.valueOf(TooltipConfig.modularUiMaxWidth)).range(0, 2000).slider().label(l(tr, "tooltip", "modularUiMaxWidth")).helper(h(tr, "tooltip", "modularUiMaxWidth")).build()
                    .bool("modularUiNoWrap").defaultValue(TooltipConfig.modularUiNoWrap).label(l(tr, "tooltip", "modularUiNoWrap")).helper(h(tr, "tooltip", "modularUiNoWrap")).build()
                    .simpleList("fillColor").defaultValue(hexList(TooltipConfig.fillColor)).label(l(tr, "tooltip", "fillColor")).helper(h(tr, "tooltip", "fillColor")).build()
                    .simpleList("strokeColor").defaultValue(hexList(TooltipConfig.strokeColor)).label(l(tr, "tooltip", "strokeColor")).helper(h(tr, "tooltip", "strokeColor")).build()
                .endSection()
                .section("waila")
                    .title(tr.apply("qzuilibenhance.config.cat.waila"))
                    .bool("enabled").defaultValue(TooltipConfig.waila).label(l(tr, "waila", "enabled")).helper(h(tr, "waila", "enabled")).build()
                    .number("cornerRadius").defaultValue(Double.valueOf(TooltipConfig.wailaCornerRadius)).range(0, 16).slider().label(l(tr, "waila", "cornerRadius")).helper(h(tr, "waila", "cornerRadius")).build()
                    .number("borderWidth").defaultValue(Double.valueOf(TooltipConfig.wailaBorderWidth)).range(0, 8).slider().label(l(tr, "waila", "borderWidth")).helper(h(tr, "waila", "borderWidth")).build()
                .endSection()
                .section("smoothscroll")
                    .title(tr.apply("qzuilibenhance.config.cat.smoothscroll"))
                    .bool("enabled").defaultValue(SmoothScrollConfig.enabled).label(l(tr, "smoothscroll", "enabled")).helper(h(tr, "smoothscroll", "enabled")).build()
                    .number("factor").defaultValue(Double.valueOf(SmoothScrollConfig.factor)).range(0.05, 1).slider().label(l(tr, "smoothscroll", "factor")).helper(h(tr, "smoothscroll", "factor")).build()
                .endSection()
                .section("screentransition")
                    .title(tr.apply("qzuilibenhance.config.cat.screentransition"))
                    .bool("enabled").defaultValue(ScreenTransition.enabled).label(l(tr, "screentransition", "enabled")).helper(h(tr, "screentransition", "enabled")).build()
                    .integer("durationMs").defaultValue(Long.valueOf(ScreenTransition.durationMs)).range(0, 2000).slider().label(l(tr, "screentransition", "durationMs")).helper(h(tr, "screentransition", "durationMs")).build()
                    .number("offset").defaultValue(Double.valueOf(ScreenTransition.offset)).range(0, 64).slider().label(l(tr, "screentransition", "offset")).helper(h(tr, "screentransition", "offset")).build()
                    .number("scale").defaultValue(Double.valueOf(ScreenTransition.scale)).range(0, 4).slider().label(l(tr, "screentransition", "scale")).helper(h(tr, "screentransition", "scale")).build()
                .endSection()
                .section("zoom")
                    .title(tr.apply("qzuilibenhance.config.cat.zoom"))
                    .bool("enabled").defaultValue(com.gtnh.qzuilibenhance.vanilla.ZoomConfig.enabled).label(l(tr, "zoom", "enabled")).helper(h(tr, "zoom", "enabled")).build()
                    .number("factor").defaultValue(Double.valueOf(com.gtnh.qzuilibenhance.vanilla.ZoomConfig.factor)).range(1, 20).slider().label(l(tr, "zoom", "factor")).helper(h(tr, "zoom", "factor")).build()
                    .bool("smooth").defaultValue(com.gtnh.qzuilibenhance.vanilla.ZoomConfig.smooth).label(l(tr, "zoom", "smooth")).helper(h(tr, "zoom", "smooth")).build()
                    .number("speed").defaultValue(Double.valueOf(com.gtnh.qzuilibenhance.vanilla.ZoomConfig.speed)).range(0.02, 1).slider().label(l(tr, "zoom", "speed")).helper(h(tr, "zoom", "speed")).build()
                    .number("sensitivity").defaultValue(Double.valueOf(com.gtnh.qzuilibenhance.vanilla.ZoomConfig.sensitivity)).range(1, 5).slider().label(l(tr, "zoom", "sensitivity")).helper(h(tr, "zoom", "sensitivity")).build()
                    .integer("keyCode").defaultValue(Long.valueOf(com.gtnh.qzuilibenhance.vanilla.ZoomConfig.keyCode)).range(0, 255).slider().label(l(tr, "zoom", "keyCode")).helper(h(tr, "zoom", "keyCode")).build()
                .endSection()
                .section("chat")
                    .title(tr.apply("qzuilibenhance.config.cat.chat"))
                    .bool("smoothEnabled").defaultValue(com.gtnh.qzuilibenhance.vanilla.ChatConfig.smoothEnabled).label(l(tr, "chat", "smoothEnabled")).helper(h(tr, "chat", "smoothEnabled")).build()
                    .integer("durationMs").defaultValue(Long.valueOf(com.gtnh.qzuilibenhance.vanilla.ChatConfig.durationMs)).range(0, 2000).slider().label(l(tr, "chat", "durationMs")).helper(h(tr, "chat", "durationMs")).build()
                    .number("offset").defaultValue(Double.valueOf(com.gtnh.qzuilibenhance.vanilla.ChatConfig.offset)).range(0, 32).slider().label(l(tr, "chat", "offset")).helper(h(tr, "chat", "offset")).build()
                    .bool("headsEnabled").defaultValue(com.gtnh.qzuilibenhance.vanilla.ChatConfig.headsEnabled).label(l(tr, "chat", "headsEnabled")).helper(h(tr, "chat", "headsEnabled")).build()
                    .number("headSize").defaultValue(Double.valueOf(com.gtnh.qzuilibenhance.vanilla.ChatConfig.headSize)).range(4, 16).slider().label(l(tr, "chat", "headSize")).helper(h(tr, "chat", "headSize")).build()
                    .number("headGap").defaultValue(Double.valueOf(com.gtnh.qzuilibenhance.vanilla.ChatConfig.headGap)).range(0, 8).slider().label(l(tr, "chat", "headGap")).helper(h(tr, "chat", "headGap")).build()
                .endSection()
                .section("performance")
                    .title(tr.apply("qzuilibenhance.config.cat.performance"))
                    .bool("unfocusedFramerate").defaultValue(com.gtnh.qzuilibenhance.vanilla.UnfocusedConfig.framerateEnabled).label(l(tr, "performance", "unfocusedFramerate")).helper(h(tr, "performance", "unfocusedFramerate")).build()
                    .integer("unfocusedFramerateLimit").defaultValue(Long.valueOf(com.gtnh.qzuilibenhance.vanilla.UnfocusedConfig.framerateLimit)).range(1, 120).slider().label(l(tr, "performance", "unfocusedFramerateLimit")).helper(h(tr, "performance", "unfocusedFramerateLimit")).build()
                    .bool("unfocusedVolume").defaultValue(com.gtnh.qzuilibenhance.vanilla.UnfocusedConfig.volumeEnabled).label(l(tr, "performance", "unfocusedVolume")).helper(h(tr, "performance", "unfocusedVolume")).build()
                    .number("unfocusedVolumeFactor").defaultValue(Double.valueOf(com.gtnh.qzuilibenhance.vanilla.UnfocusedConfig.volumeFactor)).range(0, 1).slider().label(l(tr, "performance", "unfocusedVolumeFactor")).helper(h(tr, "performance", "unfocusedVolumeFactor")).build()
                .endSection()
                .section("ui")
                    .title(tr.apply("qzuilibenhance.config.cat.ui"))
                    .bool("textUndoEnabled").defaultValue(com.gtnh.qzuilibenhance.vanilla.UiConfig.textUndoEnabled).label(l(tr, "ui", "textUndoEnabled")).helper(h(tr, "ui", "textUndoEnabled")).build()
                    .integer("textUndoLimit").defaultValue(Long.valueOf(com.gtnh.qzuilibenhance.vanilla.UiConfig.textUndoLimit)).range(1, 1000).slider().label(l(tr, "ui", "textUndoLimit")).helper(h(tr, "ui", "textUndoLimit")).build()
                    .bool("pauseContainers").defaultValue(com.gtnh.qzuilibenhance.vanilla.VanillaPauseConfig.enabled).label(l(tr, "ui", "pauseContainers")).helper(h(tr, "ui", "pauseContainers")).build()
                .endSection()
                .section("textselection")
                    .title(tr.apply("qzuilibenhance.config.cat.textselection"))
                    .bool("selectionEnabled").defaultValue(com.gtnh.qzuilibenhance.vanilla.UiConfig.selectionEnabled).label(l(tr, "textselection", "selectionEnabled")).helper(h(tr, "textselection", "selectionEnabled")).build()
                    .integer("selectionColor").defaultValue(Long.valueOf(com.gtnh.qzuilibenhance.vanilla.UiConfig.selectionColor)).range(0, 0xFFFFFF).slider().label(l(tr, "textselection", "selectionColor")).helper(h(tr, "textselection", "selectionColor")).build()
                    .integer("selectionAlpha").defaultValue(Long.valueOf(com.gtnh.qzuilibenhance.vanilla.UiConfig.selectionAlpha)).range(0, 255).slider().label(l(tr, "textselection", "selectionAlpha")).helper(h(tr, "textselection", "selectionAlpha")).build()
                .endSection()
                .section("emoji")
                    .title(tr.apply("qzuilibenhance.config.cat.emoji"))
                    .bool("enabled").defaultValue(com.gtnh.qzuilibenhance.vanilla.EmojiConfig.enabled).label(l(tr, "emoji", "enabled")).helper(h(tr, "emoji", "enabled")).build()
                .endSection()
                .build();
    }

    private static String l(Function<String, String> tr, String section, String key) {
        return tr.apply("qzuilibenhance.config." + section + "." + key);
    }

    private static String h(Function<String, String> tr, String section, String key) {
        return tr.apply("qzuilibenhance.config." + section + "." + key + ".tooltip");
    }

    private static List<String> hexList(int[] values) {
        List<String> list = new ArrayList<String>();
        for (int value : values) {
            list.add(String.format("%08X", value));
        }
        return list;
    }

    // ---------- 回灌 ----------

    /** 从权威源全量回灌运行态静态字段（启动加载 / 保存 / 磁盘重载统一入口）。 */
    public static void apply(Authority authority) {
        if (authority == null) {
            return;
        }

        TooltipConfig.enabled = authority.getBool("tooltip.enabled");
        TooltipConfig.rounded = authority.getBool("tooltip.rounded");
        TooltipConfig.centerTitle = authority.getBool("tooltip.centerTitle");
        TooltipConfig.titleBreak = authority.getBool("tooltip.titleBreak");
        TooltipConfig.adaptiveColors = authority.getBool("tooltip.adaptiveColors");
        TooltipConfig.borderColorCycle = (int) Math.round(authority.getNumber("tooltip.borderColorCycle"));
        TooltipConfig.borderWidth = (float) authority.getNumber("tooltip.borderWidth");
        TooltipConfig.cornerRadius = (float) authority.getNumber("tooltip.cornerRadius");
        TooltipConfig.modularUiMaxWidth = (int) Math.round(authority.getNumber("tooltip.modularUiMaxWidth"));
        TooltipConfig.modularUiNoWrap = authority.getBool("tooltip.modularUiNoWrap");
        readColors(authority, "tooltip.fillColor", TooltipConfig.fillColor);
        readColors(authority, "tooltip.strokeColor", TooltipConfig.strokeColor);

        TooltipConfig.waila = authority.getBool("waila.enabled");
        TooltipConfig.wailaCornerRadius = (float) authority.getNumber("waila.cornerRadius");
        TooltipConfig.wailaBorderWidth = (float) authority.getNumber("waila.borderWidth");

        SmoothScrollConfig.enabled = authority.getBool("smoothscroll.enabled");
        SmoothScrollConfig.factor = (float) authority.getNumber("smoothscroll.factor");

        ScreenTransition.enabled = authority.getBool("screentransition.enabled");
        ScreenTransition.durationMs = (int) Math.round(authority.getNumber("screentransition.durationMs"));
        ScreenTransition.offset = (float) authority.getNumber("screentransition.offset");
        ScreenTransition.scale = (float) authority.getNumber("screentransition.scale");

        com.gtnh.qzuilibenhance.vanilla.ZoomConfig.enabled = authority.getBool("zoom.enabled");
        com.gtnh.qzuilibenhance.vanilla.ZoomConfig.factor = (float) authority.getNumber("zoom.factor");
        com.gtnh.qzuilibenhance.vanilla.ZoomConfig.smooth = authority.getBool("zoom.smooth");
        com.gtnh.qzuilibenhance.vanilla.ZoomConfig.speed = (float) authority.getNumber("zoom.speed");
        com.gtnh.qzuilibenhance.vanilla.ZoomConfig.sensitivity = (float) authority.getNumber("zoom.sensitivity");
        com.gtnh.qzuilibenhance.vanilla.ZoomConfig.keyCode = (int) Math.round(authority.getNumber("zoom.keyCode"));

        com.gtnh.qzuilibenhance.vanilla.ChatConfig.smoothEnabled = authority.getBool("chat.smoothEnabled");
        com.gtnh.qzuilibenhance.vanilla.ChatConfig.durationMs = (int) Math.round(authority.getNumber("chat.durationMs"));
        com.gtnh.qzuilibenhance.vanilla.ChatConfig.offset = (float) authority.getNumber("chat.offset");
        com.gtnh.qzuilibenhance.vanilla.ChatConfig.headsEnabled = authority.getBool("chat.headsEnabled");
        com.gtnh.qzuilibenhance.vanilla.ChatConfig.headSize = (float) authority.getNumber("chat.headSize");
        com.gtnh.qzuilibenhance.vanilla.ChatConfig.headGap = (float) authority.getNumber("chat.headGap");

        com.gtnh.qzuilibenhance.vanilla.UnfocusedConfig.framerateEnabled = authority.getBool("performance.unfocusedFramerate");
        com.gtnh.qzuilibenhance.vanilla.UnfocusedConfig.framerateLimit = (int) Math.round(authority.getNumber("performance.unfocusedFramerateLimit"));
        com.gtnh.qzuilibenhance.vanilla.UnfocusedConfig.volumeEnabled = authority.getBool("performance.unfocusedVolume");
        com.gtnh.qzuilibenhance.vanilla.UnfocusedConfig.volumeFactor = (float) authority.getNumber("performance.unfocusedVolumeFactor");

        com.gtnh.qzuilibenhance.vanilla.UiConfig.textUndoEnabled = authority.getBool("ui.textUndoEnabled");
        com.gtnh.qzuilibenhance.vanilla.UiConfig.textUndoLimit = (int) Math.round(authority.getNumber("ui.textUndoLimit"));
        com.gtnh.qzuilibenhance.vanilla.VanillaPauseConfig.enabled = authority.getBool("ui.pauseContainers");

        com.gtnh.qzuilibenhance.vanilla.UiConfig.selectionEnabled = authority.getBool("textselection.selectionEnabled");
        com.gtnh.qzuilibenhance.vanilla.UiConfig.selectionColor = (int) Math.round(authority.getNumber("textselection.selectionColor"));
        com.gtnh.qzuilibenhance.vanilla.UiConfig.selectionAlpha = (int) Math.round(authority.getNumber("textselection.selectionAlpha"));

        com.gtnh.qzuilibenhance.vanilla.EmojiConfig.enabled = authority.getBool("emoji.enabled");
    }

    private static void readColors(Authority authority, String path, int[] target) {
        Object value = authority.get(path);
        if (!(value instanceof List)) {
            return;
        }
        List<?> list = (List<?>) value;
        for (int i = 0; i < target.length && i < list.size(); i++) {
            try {
                target[i] = (int) Long.parseLong(String.valueOf(list.get(i)).replace("#", "").trim(), 16);
            } catch (NumberFormatException ignored) {
                // 保留默认值
            }
        }
    }

    // ---------- 旧 cfg 迁移 ----------

    private static void migrateLegacy(File legacyFile, File yaml, ConfigSchema schema) {
        if (yaml.isFile() || !legacyFile.isFile()) {
            return;
        }
        Configuration legacy = new Configuration(legacyFile);
        try {
            legacy.load();
            ConfigManager manager = ConfigManager.bootstrap(yaml, schema);
            DraftBuffer draft = manager.openDraft();
            boolean any = false;
            for (SectionSpec section : schema.sections()) {
                for (FieldSpec field : section.fields()) {
                    String path = field.path();
                    int dot = path.indexOf('.');
                    if (dot <= 0) {
                        continue;
                    }
                    String category = path.substring(0, dot);
                    String key = path.substring(dot + 1);
                    Object value = readLegacy(legacy, category, key, field.type(), field.defaultValue());
                    if (value == null) {
                        continue;
                    }
                    try {
                        draft.setDraft(path, value);
                        any = true;
                    } catch (RuntimeException ignored) {
                        // 单字段类型不符则跳过
                    }
                }
            }
            if (any) {
                manager.save(draft);
                MyMod.LOG.info("已把旧 qzuilibenhance.cfg 迁入 {}", yaml.getAbsolutePath());
            }
        } catch (ConfigException | RuntimeException e) {
            MyMod.LOG.warn("旧配置迁移失败，使用默认值", e);
        }
    }

    private static Object readLegacy(Configuration legacy, String category, String key, FieldType type, Object def) {
        try {
            switch (type) {
                case BOOLEAN:
                    return Boolean.valueOf(legacy.getBoolean(key, category, ((Boolean) def).booleanValue(), ""));
                case NUMBER:
                    return Double.valueOf(legacy.get(category, key, ((Number) def).doubleValue(), "", 0.0D, 1.0E9D).getDouble());
                case INTEGER:
                    return Long.valueOf(legacy.getInt(key, category, ((Number) def).intValue(), 0, Integer.MAX_VALUE, ""));
                case SIMPLE_LIST:
                    return new ArrayList<String>(java.util.Arrays.asList(legacy.getStringList(key, category, toStringArray(def), "")));
                case STRING:
                    return legacy.getString(key, category, String.valueOf(def), "");
                default:
                    return null;
            }
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static String[] toStringArray(Object def) {
        if (def instanceof List) {
            List<?> list = (List<?>) def;
            String[] out = new String[list.size()];
            for (int i = 0; i < out.length; i++) {
                out[i] = String.valueOf(list.get(i));
            }
            return out;
        }
        return new String[0];
    }
}
