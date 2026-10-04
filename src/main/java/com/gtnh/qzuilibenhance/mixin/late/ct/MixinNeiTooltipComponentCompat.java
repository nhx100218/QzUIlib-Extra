package com.gtnh.qzuilibenhance.mixin.late.ct;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.gtnh.qzuilibenhance.MyMod;
import com.gtnh.qzuilibenhance.tooltip.TooltipConfig;
import com.slprime.chromatictooltips.api.TooltipContext;

import codechicken.lib.gui.GuiDraw;

/**
 * ChromaticTooltips 的 NEI 兼容包装 {@code NEIHandler$TooltipComponentCompat} 默认把 NEI 的 tooltip
 * 行处理器（含配方预览「工作台页面」）画在内容区左侧。这里按整个 tooltip 宽度居中，和本模组的
 * 圆角 tooltip 对齐。
 */
@Mixin(targets = "com.slprime.chromatictooltipscompat.event.NEIHandler$TooltipComponentCompat")
public class MixinNeiTooltipComponentCompat {

    /** NEI 行处理器整体横向微调（像素）：负值左移、正值右移。 */
    private static final int PANEL_X_NUDGE = -2;

    @Unique
    private static final java.util.Set<String> qzuilib$logged =
            java.util.Collections.newSetFromMap(new java.util.concurrent.ConcurrentHashMap<String, Boolean>());

    @Shadow(remap = false)
    protected GuiDraw.ITooltipLineHandler lineHandler;

    @Inject(method = "draw", at = @At("HEAD"), cancellable = true, remap = false)
    private void qzuilib$center(int x, int y, int width, TooltipContext ctx, CallbackInfo ci) {
        if (!TooltipConfig.enabled || lineHandler == null) {
            return;
        }
        java.awt.Dimension size = lineHandler.getSize();
        int w = size == null ? 0 : size.width;
        int tooltipW = -1;
        try {
            if (ctx != null && ctx.getTooltipSize() != null) {
                tooltipW = ctx.getTooltipSize().width;
            }
        } catch (Throwable ignored) {
        }
        // NEI 行处理器通常独占一个 section，此时传入的 width 等于自身宽度（偏移为 0），
        // 因此改用整个 tooltip 宽度来居中。
        int targetWidth = tooltipW > 0 ? tooltipW : width;
        // 略微左移，与左侧文本对齐（观感修正）：改 PANEL_X_NUDGE 即可（负值左移，正值右移）。
        int offset = (targetWidth - w) / 2 + PANEL_X_NUDGE;
        if (offset < 0) {
            offset = 0;
        }
        qzuilib$log(lineHandler.getClass().getName(), w, width, tooltipW, offset);
        lineHandler.draw(x + offset, y);
        ci.cancel();
    }

    @Unique
    private static void qzuilib$log(String name, int selfW, int argWidth, int tooltipW, int offset) {
        String key = name + "|" + selfW + "|" + argWidth + "|" + tooltipW + "|" + offset;
        if (qzuilib$logged.size() < 300 && qzuilib$logged.add(key)) {
            MyMod.LOG.info("[CtNei] handler={} selfW={} argWidth={} tooltipW={} offset={}", name,
                    Integer.valueOf(selfW), Integer.valueOf(argWidth), Integer.valueOf(tooltipW),
                    Integer.valueOf(offset));
        }
    }
}
