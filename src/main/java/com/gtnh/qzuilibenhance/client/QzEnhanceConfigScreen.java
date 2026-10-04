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
import club.heiqi.uilib.ui.scene.UiSurface;
import club.heiqi.uilib.ui.scene.host.lwjgl.LwjglInputSource;
import club.heiqi.uilib.ui.scene.host.lwjgl.LwjglStateReader;
import club.heiqi.uilib.ui.scene.input.PlatformInputSource;
import club.heiqi.uilib.ui.screen.McScreenBridge;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
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
        return new Built(manager, surface);
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
