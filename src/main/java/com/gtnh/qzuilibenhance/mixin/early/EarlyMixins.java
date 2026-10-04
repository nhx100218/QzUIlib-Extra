package com.gtnh.qzuilibenhance.mixin.early;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.gtnewhorizon.gtnhmixins.IEarlyMixinLoader;
import com.gtnh.qzuilibenhance.vanilla.StartupConfigFixer;

import cpw.mods.fml.relauncher.FMLLaunchHandler;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import cpw.mods.fml.relauncher.Side;

/** 原版类的早期 Mixin 加载器。 */
@IFMLLoadingPlugin.MCVersion("1.7.10")
public class EarlyMixins implements IEarlyMixinLoader, IFMLLoadingPlugin {

    @Override
    public String getMixinConfig() {
        return "mixins.qzuilibenhance.early.json";
    }

    @Override
    public List<String> getMixins(Set<String> loadedCoreMods) {
        List<String> mixins = new ArrayList<String>();
        if (FMLLaunchHandler.side() == Side.CLIENT) {
            mixins.add("MixinGuiScreenTooltip");
            mixins.add("MixinGuiSlotSmoothScroll");
            mixins.add("MixinEntityRendererScreenTransition");
            mixins.add("MixinEntityRendererZoom");
            mixins.add("MixinGuiNewChat");
            mixins.add("MixinMinecraftUnfocused");
            mixins.add("MixinGameSettingsUnfocused");
            mixins.add("MixinGuiTextFieldUndo");
            mixins.add("MixinGuiContainerPause");
            mixins.add("MixinGuiContainerCreativeSmoothScroll");
            mixins.add("MixinGuiTextFieldSmoothScroll");
            mixins.add("MixinGuiTextFieldSelection");
            mixins.add("MixinMinecraftScreenPoke");
            mixins.add("MixinGuiIngameHotbarScroll");
            mixins.add("MixinGuiHotbarTexture");
            mixins.add("MixinInventoryPlayerHotbar");
            mixins.add("MixinGuiNewChatScroll");
        }
        return mixins;
    }

    @Override
    public String[] getASMTransformerClass() {
        return null;
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
        // 模组加载前修正第三方配置（Waila/CT 兼容/字体排序）并释放内置字体。
        Object location = data.get("mcLocation");
        if (location instanceof File) {
            StartupConfigFixer.fix(new File((File) location, "config"));
        }
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
