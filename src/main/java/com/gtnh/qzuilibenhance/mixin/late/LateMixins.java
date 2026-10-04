package com.gtnh.qzuilibenhance.mixin.late;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.gtnewhorizon.gtnhmixins.ILateMixinLoader;
import com.gtnewhorizon.gtnhmixins.LateMixin;

import cpw.mods.fml.relauncher.FMLLaunchHandler;
import cpw.mods.fml.relauncher.Side;

/** 可选模组的客户端兼容 mixin 入口。 */
@LateMixin
public class LateMixins implements ILateMixinLoader {

    @Override
    public String getMixinConfig() {
        return "mixins.qzuilibenhance.late.json";
    }

    @Override
    public List<String> getMixins(Set<String> loadedMods) {
        List<String> mixins = new ArrayList<String>();
        if (FMLLaunchHandler.side() == Side.CLIENT && loadedMods != null) {
            final Set<String> mods = loadedMods;
            java.util.function.Predicate<String> has = id -> {
                for (String s : mods) {
                    if (s != null && s.equalsIgnoreCase(id)) {
                        return true;
                    }
                }
                return false;
            };
            if (has.test("Waila")) {
                mixins.add("waila.MixinWailaOverlayRenderer");
            }
            if (has.test("CodeChickenCore")) {
                mixins.add("codechicken.MixinGuiDrawTooltip");
            }
            if (has.test("modularui2")) {
                mixins.add("modularui.MixinModularUiTooltip");
                mixins.add("modularui.MixinModularScreenTransition");
                mixins.add("modularui.MixinTextRendererNoWrap");
                mixins.add("modularui.MixinRichTextCompilerNoWrap");
            }
            if (has.test("chromatictooltips")) {
                mixins.add("ct.MixinCtTooltipDecorator");
                mixins.add("ct.MixinCtTextComponent");
            }
            if (has.test("chromatictooltipscompat")) {
                mixins.add("ct.MixinNeiTooltipComponentCompat");
            }
            if (has.test("betterquesting")) {
                mixins.add("bq.MixinBqTooltip");
                mixins.add("bq.MixinBqSceneTransition");
            }
            if (has.test("NotEnoughItems")) {
                mixins.add("nei.MixinFormattedTextFieldSelection");
                mixins.add("nei.MixinRecipeTooltipLineHandler");
            }
            if (has.test("TConstruct")) {
                mixins.add("tconstruct.MixinSmelteryGui");
            }
        }
        return mixins;
    }
}
