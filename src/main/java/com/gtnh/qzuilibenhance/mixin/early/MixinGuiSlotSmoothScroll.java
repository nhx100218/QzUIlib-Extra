package com.gtnh.qzuilibenhance.mixin.early;

import org.lwjgl.input.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.gtnh.qzuilibenhance.vanilla.SmoothScrollConfig;
import com.gtnh.qzuilibenhance.vanilla.SmoothScrollModel;

import net.minecraft.client.gui.GuiSlot;

/**
 * 原版可滚动列表 {@link GuiSlot} 的指数平滑滚动（对齐 Minecraft-Smooth-Scrolling 的 Entry List）。
 *
 * <p>滚轮/scrollBy 只累积目标值，每帧用 {@link SmoothScrollModel} 向目标逼近；拖动滚动条时直接吸附，
 * 避免缓动与拖拽打架。缓动强度与每次滚动量由配置页提供。</p>
 */
@Mixin(GuiSlot.class)
public abstract class MixinGuiSlotSmoothScroll {

    @Shadow
    private float amountScrolled;

    @Shadow
    private float initialClickY;

    @Shadow
    public int top;

    @Shadow
    public int bottom;

    @Shadow
    public int slotHeight;

    @Shadow
    protected abstract int getContentHeight();

    private final SmoothScrollModel qzuilib$model = new SmoothScrollModel();
    private boolean qzuilib$tracking;

    @Inject(method = "drawScreen", at = @At("HEAD"))
    private void qzuilib$animate(int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        if (!SmoothScrollConfig.enabled || SmoothScrollConfig.listSmoothness == 0 || !qzuilib$tracking) {
            return;
        }
        // 拖动滚动条（initialClickY >= 0）时直接同步，避免缓动与拖动打架。
        if (initialClickY >= 0.0F) {
            qzuilib$model.snap(amountScrolled);
            return;
        }
        amountScrolled = (float) qzuilib$model.update(
                SmoothScrollConfig.smoothness(SmoothScrollConfig.listSmoothness));
    }

    @Redirect(method = "drawScreen", at = @At(value = "INVOKE", remap = false,
            target = "Lorg/lwjgl/input/Mouse;getEventDWheel()I"))
    private int qzuilib$onWheel() {
        int delta = Mouse.getEventDWheel();
        if (!SmoothScrollConfig.enabled || SmoothScrollConfig.listSmoothness == 0 || delta == 0) {
            return delta;
        }
        int step = delta > 0 ? -1 : 1;
        int amount = SmoothScrollConfig.listAmount > 0 ? SmoothScrollConfig.listAmount : Math.max(1, slotHeight / 2);
        qzuilib$addTarget(step * amount);
        return 0;
    }

    @Inject(method = "scrollBy", at = @At("HEAD"), cancellable = true)
    private void qzuilib$scrollBy(int amount, CallbackInfo ci) {
        if (!SmoothScrollConfig.enabled || SmoothScrollConfig.listSmoothness == 0) {
            return;
        }
        qzuilib$addTarget(amount);
        ci.cancel();
    }

    private void qzuilib$addTarget(int amount) {
        if (!qzuilib$tracking) {
            qzuilib$model.snap(amountScrolled);
            qzuilib$tracking = true;
        }
        qzuilib$model.setTarget(qzuilib$model.target() + amount);
        int max = getContentHeight() - (bottom - top - 4);
        if (qzuilib$model.target() < 0.0D) {
            qzuilib$model.setTarget(0.0D);
        }
        if (qzuilib$model.target() > max) {
            qzuilib$model.setTarget(max);
        }
    }
}
