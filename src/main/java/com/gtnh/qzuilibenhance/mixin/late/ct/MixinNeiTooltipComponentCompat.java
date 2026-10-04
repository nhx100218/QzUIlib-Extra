package com.gtnh.qzuilibenhance.mixin.late.ct;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.gtnh.qzuilibenhance.tooltip.TooltipConfig;
import com.slprime.chromatictooltips.api.TooltipContext;

import codechicken.lib.gui.GuiDraw;

/**
 * ChromaticTooltips 的 NEI 兼容包装 {@code NEIHandler$TooltipComponentCompat} 默认把 NEI 的 tooltip
 * 行处理器（含配方预览「工作台页面」）画在内容区左侧。这里把配方预览在内容区内居中，和本模组的
 * 圆角 tooltip 对齐；其它行处理器保持原样。
 */
@Mixin(targets = "com.slprime.chromatictooltipscompat.event.NEIHandler$TooltipComponentCompat")
public class MixinNeiTooltipComponentCompat {

    @Shadow(remap = false)
    protected GuiDraw.ITooltipLineHandler lineHandler;

    @Inject(method = "draw", at = @At("HEAD"), cancellable = true, remap = false)
    private void qzuilib$center(int x, int y, int width, TooltipContext ctx, CallbackInfo ci) {
        if (!TooltipConfig.enabled || lineHandler == null) {
            return;
        }
        if (lineHandler.getClass().getName().indexOf("Recipe") < 0) {
            return;
        }
        java.awt.Dimension size = lineHandler.getSize();
        int w = size == null ? 0 : size.width;
        // 配方预览通常独占一个 section，此时传入的 width 等于面板宽度（偏移为 0），
        // 因此改用整个 tooltip 的内容宽度来居中。
        int targetWidth = width;
        try {
            if (ctx != null && ctx.getTooltipSize() != null) {
                targetWidth = Math.max(width, ctx.getTooltipSize().width - 8);
            }
        } catch (Throwable ignored) {
        }
        int offset = (targetWidth - w) / 2;
        if (offset < 0) {
            offset = 0;
        }
        lineHandler.draw(x + offset, y);
        ci.cancel();
    }
}
