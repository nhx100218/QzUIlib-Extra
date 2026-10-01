package com.gtnh.qzuilibenhance.mixin.late.codechicken;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.gtnh.qzuilibenhance.tooltip.CodeChickenTooltipStyle;

/**
 * 把 CodeChickenLib 的方形 tooltip 背景替换为圆角（覆盖 NEI 面板/配置等）。
 */
@Mixin(targets = "codechicken.lib.gui.GuiDraw")
public class MixinGuiDrawTooltip {

    @Inject(method = "drawTooltipBox(IIIIIIII)V", at = @At("HEAD"), cancellable = true, remap = false)
    private static void qzuilib$drawTooltipBox(int x, int y, int width, int height, int borderStart,
            int borderEnd, int bgStart, int bgEnd, CallbackInfo ci) {
        if (CodeChickenTooltipStyle.draw(x, y, width, height, borderStart, borderEnd, bgStart, bgEnd)) {
            ci.cancel();
        }
    }
}
