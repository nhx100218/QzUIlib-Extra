package com.gtnh.qzuilibenhance.client;

import java.io.File;

import com.gtnh.qzuilibenhance.Config;

import club.heiqi.config.ConfigChangeEvent;
import club.heiqi.config.ConfigChangeListener;
import club.heiqi.config.ConfigException;
import club.heiqi.config.runtime.ConfigManager;
import club.heiqi.config.schema.ConfigSchema;
import club.heiqi.config.ui.ConfigScreen;
import club.heiqi.config.ui.ConfigUI;
import com.gtnh.qzuilibenhance.MyMod;

import club.heiqi.uilib.ui.scene.UiSurface;
import club.heiqi.uilib.ui.scene.host.lwjgl.LwjglInputSource;
import club.heiqi.uilib.ui.scene.host.lwjgl.LwjglStateReader;
import club.heiqi.uilib.ui.scene.input.PlatformInputSource;
import club.heiqi.uilib.ui.scene.layout.CrossAxisAlign;
import club.heiqi.uilib.ui.scene.node.SceneNode;
import club.heiqi.uilib.ui.screen.McScreenBridge;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.resources.I18n;

/**
 * 附属增强配置页的 MC 宿主：用 QzUILib 的 {@link ConfigUI#buildScreen} 构建
 * {@link ConfigScreen}（官方 schema 配置页），再经 {@link McScreenBridge} 包成
 * Minecraft {@link GuiScreen}。取代旧的自绘 MD3 页面。
 *
 * <p>配置页保存/重载经 manager 的事件总线回灌运行态静态字段
 * （{@link Config#apply(club.heiqi.config.runtime.Authority)}）。</p>
 */
public class QzEnhanceConfigScreen extends McScreenBridge {

    /** 把页内各节点 maxWidth 顶到「无上限」，让内容铺满整屏。 */
    private static final int FULL_WIDTH = 100000;
    /** 页壳内边距：整屏布局下仍留出四周留白，避免顶到屏幕边缘。 */
    private static final int PAGE_PADDING = 16;
    /** 底部操作条放大后的容器高与按钮尺寸（默认 108 宽会截断「恢复默认」等文案）。 */
    private static final int ACTION_BAR_HEIGHT = 56;
    private static final int ACTION_BUTTON_WIDTH = 150;
    private static final int ACTION_BUTTON_HEIGHT = 36;
    /** 整页缩放上下限（runtime 正交倍率层，100 = 原尺寸）。 */
    private static final int FONT_SCALE_MIN = 100;
    private static final int FONT_SCALE_MAX = 200;

    private final ConfigManager manager;

    public QzEnhanceConfigScreen(GuiScreen parent) {
        this(parent, build());
    }

    private QzEnhanceConfigScreen(GuiScreen parent, Built built) {
        super(parent, built.surface);
        this.manager = built.manager;
        manager.eventBus().subscribe(new ConfigChangeListener() {

            @Override
            public void onConfigChanged(ConfigChangeEvent event) {
                Config.apply(manager.authority());
            }
        });
    }

    private static Built build() {
        Minecraft mc = Minecraft.getMinecraft();
        File yaml = Config.configFile(new File(mc.mcDataDir, "config"));
        ConfigSchema schema = Config.buildSchema(key -> I18n.format(key));
        final ConfigManager manager;
        try {
            manager = ConfigManager.bootstrap(yaml, schema);
        } catch (ConfigException e) {
            throw new RuntimeException("QzUILib Enhance 配置加载失败: " + yaml, e);
        }
        PlatformInputSource input = new LwjglInputSource(new LwjglStateReader());
        ConfigScreen surface = ConfigUI.buildScreen(manager, input, registry -> {});
        surface.runtime().setFontScale(fontScalePercentFromGuiScale(mc));
        applyFullWidthLayout(surface);
        removeStatusSummary(surface);
        return new Built(manager, surface);
    }

    /**
     * 按玩家在「视频设置 → GUI 缩放」里选的档位决定整页缩放：档位 0（自动）先经
     * {@link ScaledResolution} 解析成实际档位，再换算成缩放百分比并钳到
     * {@link #FONT_SCALE_MIN}..{@link #FONT_SCALE_MAX}（runtime 正交倍率层上限为 200%）。
     */
    private static int fontScalePercentFromGuiScale(Minecraft mc) {
        int guiScale = 2;
        if (mc != null && mc.gameSettings != null) {
            guiScale = mc.gameSettings.guiScale;
            if (guiScale <= 0) {
                guiScale = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight).getScaleFactor();
            }
        }
        return Math.max(FONT_SCALE_MIN, Math.min(FONT_SCALE_MAX, guiScale * 100));
    }

    /**
     * 把 QzUILib {@link ConfigScreen} 的页壳从「居中 + 1120px 上限」改为整屏布局：
     * 侧边导航贴最左，右侧内容铺满剩余宽度。
     *
     * <p>库未开放布局配置入口，这里经包内视觉节点访问器（{@code __getXxx}）拿到节点后
     * 放开 maxWidth / 改交叉轴对齐；只作用于本模组配置页，不改动库本身。</p>
     */
    private static void applyFullWidthLayout(ConfigScreen screen) {
        try {
            Minecraft mc = Minecraft.getMinecraft();
            int scale = Math.max(1, screen.runtime().getFontScalePercent());
            int logicalHeight = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight).getScaledHeight();
            // preferredHeight 在解析出口乘 fontScale，故先除回去，使解析后正好等于逻辑屏高。
            int fillHeight = Math.max(1, (int) Math.round(logicalHeight * 100.0D / scale));

            SceneNode root = node(screen, "__getRoot");
            if (root != null) {
                root.setPreferredHeight(fillHeight);
                // 世界遮罩置透明：游戏内打开配置页时能看到背后的游戏画面。
                root.setBackgroundColor(0x00000000);
            }
            SceneNode pageRoot = node(screen, "__getPageRoot");
            if (pageRoot != null) {
                pageRoot.setCrossAxisAlign(CrossAxisAlign.STRETCH);
                pageRoot.setPadding(PAGE_PADDING);
                pageRoot.setPreferredHeight(fillHeight);
            }
            widen(node(screen, "__getTitleBar"));
            widen(node(screen, "__getStatusSummary"));
            widen(node(screen, "__getActionBar"));
            widen(node(screen, "__getBodyRow"));
            widen(node(screen, "__getScrollContainer"));
            widen(node(screen, "__getContent"));
            SceneNode viewport = node(screen, "__getViewport");
            if (viewport != null) {
                viewport.setCrossAxisAlign(CrossAxisAlign.STRETCH);
            }
            enlargeActionBar(screen);
        } catch (Throwable t) {
            MyMod.LOG.warn("配置页全宽布局调整失败", t);
        }
    }

    /**
     * 放大底部操作条的按钮（默认宽 {@code ConfigTheme.BUTTON_WIDTH=108} 在放大后的字号下
     * 会把「恢复默认」等文案截断）。这里加宽/加高按钮并抬高操作条容器。
     */
    private static void enlargeActionBar(ConfigScreen screen) throws ReflectiveOperationException {
        SceneNode bar = node(screen, "__getActionBar");
        if (bar == null) {
            return;
        }
        bar.setPreferredHeight(ACTION_BAR_HEIGHT);
        java.util.List<SceneNode> children = bar.__getChildren();
        for (int i = 0; i < children.size(); i++) {
            if (i == 1) {
                continue; // 子 1 是 flexGrow 占位，不设宽
            }
            SceneNode button = children.get(i);
            if (button != null) {
                button.setPreferredWidth(ACTION_BUTTON_WIDTH);
                button.setPreferredHeight(ACTION_BUTTON_HEIGHT);
            }
        }
    }

    private static void widen(SceneNode node) {
        if (node == null) {
            return;
        }
        node.setMaxWidth(FULL_WIDTH);
        node.setFillParentWidth(true);
    }

    private static SceneNode node(ConfigScreen screen, String getter) throws ReflectiveOperationException {
        java.lang.reflect.Method method = ConfigScreen.class.getDeclaredMethod(getter);
        method.setAccessible(true);
        Object value = method.invoke(screen);
        return value instanceof SceneNode ? (SceneNode) value : null;
    }

    /**
     * 移除页壳顶部的状态摘要行（「N 项未保存」+「校验通过/N 项校验错误」徽标）。
     *
     * <p>QzUILib {@link ConfigScreen} 未开放隐藏该行的公共入口，这里经包内访问器
     * {@code __getStatusSummary()} 拿到节点后从父节点摘除；只影响本模组配置页，
     * 不改动库本身。</p>
     */
    private static void removeStatusSummary(ConfigScreen screen) {
        try {
            SceneNode status = node(screen, "__getStatusSummary");
            if (status == null) {
                return;
            }
            SceneNode parent = status.__getParent();
            if (parent != null) {
                parent.removeChild(status);
            }
        } catch (Throwable t) {
            MyMod.LOG.warn("移除配置页状态摘要失败", t);
        }
    }

    @Override
    public void drawDefaultBackground() {
        if (!hasWorldContext()) {
            super.drawDefaultBackground();
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    private static boolean hasWorldContext() {
        Minecraft mc = Minecraft.getMinecraft();
        return mc != null && mc.theWorld != null;
    }

    private static final class Built {

        final ConfigManager manager;
        final UiSurface surface;

        Built(ConfigManager manager, UiSurface surface) {
            this.manager = manager;
            this.surface = surface;
        }
    }
}
