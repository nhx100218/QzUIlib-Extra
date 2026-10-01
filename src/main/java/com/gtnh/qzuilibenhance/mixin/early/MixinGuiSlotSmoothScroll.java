package com.gtnh.qzuilibenhance.mixin.early;

import org.lwjgl.input.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.gtnh.qzuilibenhance.vanilla.SmoothScrollConfig;
import net.minecraft.client.gui.GuiSlot;

/**
 * 给原版可滚动列表 {@link GuiSlot} 加平滑滚动（ModernUI 式）。
 *
 * <p>拦截滚轮与 {@code scrollBy}，把滚动量累积到目标值，再在每帧平滑逼近，替代原版的一次性跳变。
 * 拖动滚动条时直接同步目标，不做缓动。</p>
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

    private float qzuilib$target;
    private boolean qzuilib$hasTarget;

    @Inject(method = "drawScreen", at = @At("HEAD"))
    private void qzuilib$animate(int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        if (!SmoothScrollConfig.enabled || !qzuilib$hasTarget) {
            return;
        }
        // 拖动滚动条（initialClickY >= 0）时直接同步，避免缓动与拖动打架。
        if (initialClickY >= 0.0F) {
            qzuilib$target = amountScrolled;
            qzuilib$hasTarget = false;
            return;
        }
        float diff = qzuilib$target - amountScrolled;
        if (Math.abs(diff) < 0.25F) {
            amountScrolled = qzuilib$target;
            qzuilib$hasTarget = false;
            return;
        }
        amountScrolled += diff * SmoothScrollConfig.factor;
    }

    @Redirect(method = "drawScreen", at = @At(value = "INVOKE", remap = false,
            target = "Lorg/lwjgl/input/Mouse;getEventDWheel()I"))
    private int qzuilib$onWheel() {
        int delta = Mouse.getEventDWheel();
        if (!SmoothScrollConfig.enabled || delta == 0) {
            return delta;
        }
        int step = delta > 0 ? -1 : 1;
        qzuilib$addTarget(step * slotHeight / 2);
        return 0;
    }

    @Inject(method = "scrollBy", at = @At("HEAD"), cancellable = true)
    private void qzuilib$scrollBy(int amount, CallbackInfo ci) {
        if (!SmoothScrollConfig.enabled) {
            return;
        }
        qzuilib$addTarget(amount);
        ci.cancel();
    }

    private void qzuilib$addTarget(int amount) {
        if (!qzuilib$hasTarget) {
            qzuilib$target = amountScrolled;
            qzuilib$hasTarget = true;
        }
        qzuilib$target += amount;
        int max = getContentHeight() - (bottom - top - 4);
        if (qzuilib$target < 0.0F) {
            qzuilib$target = 0.0F;
        }
        if (qzuilib$target > max) {
            qzuilib$target = max;
        }
    }
}
