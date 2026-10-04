package com.gtnh.qzuilibenhance.mixin.early;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.gtnh.qzuilibenhance.vanilla.SmoothScrollConfig;
import com.gtnh.qzuilibenhance.vanilla.SmoothScrollModel;

import net.minecraft.client.gui.inventory.GuiContainerCreative;

/**
 * 创造模式物品栏滚动的指数平滑（对齐 Minecraft-Smooth-Scrolling 的 Creative Screen）。
 *
 * <p>1.7.10 的创造栏按行重排槽位，无法像现代版那样逐像素重绘，这里平滑 {@code currentScroll}
 * 目标本身，把整行跳变收成缓动，属 best-effort。</p>
 */
@Mixin(GuiContainerCreative.class)
public abstract class MixinGuiContainerCreativeSmoothScroll {

    @Shadow
    private float currentScroll;

    @Shadow
    private boolean isScrolling;

    private final SmoothScrollModel qzuilib$model = new SmoothScrollModel();
    private boolean qzuilib$tracking;

    @Inject(method = "handleMouseInput", at = @At("TAIL"))
    private void qzuilib$capture(CallbackInfo ci) {
        if (!SmoothScrollConfig.enabled || SmoothScrollConfig.creativeSmoothness == 0) {
            return;
        }
        qzuilib$model.setTarget(currentScroll);
        qzuilib$tracking = true;
    }

    @Inject(method = "drawScreen", at = @At("HEAD"))
    private void qzuilib$animate(int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        if (!SmoothScrollConfig.enabled || SmoothScrollConfig.creativeSmoothness == 0 || !qzuilib$tracking) {
            return;
        }
        if (isScrolling) {
            qzuilib$model.snap(currentScroll);
            return;
        }
        currentScroll = (float) qzuilib$model.update(
                SmoothScrollConfig.smoothness(SmoothScrollConfig.creativeSmoothness));
    }
}
