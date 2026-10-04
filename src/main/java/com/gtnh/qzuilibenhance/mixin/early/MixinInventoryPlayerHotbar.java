package com.gtnh.qzuilibenhance.mixin.early;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.gtnh.qzuilibenhance.vanilla.HotbarSmoothScroll;

import net.minecraft.entity.player.InventoryPlayer;

/**
 * 快捷栏滚轮：捕获 {@code changeCurrentItem} 的前后槽位与方向，交给
 * {@link HotbarSmoothScroll#onWheel} 决定是否环绕（键盘 1-9 直接改槽位、不走此方法）。
 */
@Mixin(InventoryPlayer.class)
public class MixinInventoryPlayerHotbar {

    @Shadow
    public int currentItem;

    private int qzuilib$oldSlot;

    @Inject(method = "changeCurrentItem", at = @At("HEAD"))
    private void qzuilib$head(int direction, CallbackInfo ci) {
        qzuilib$oldSlot = currentItem;
    }

    @Inject(method = "changeCurrentItem", at = @At("RETURN"))
    private void qzuilib$ret(int direction, CallbackInfo ci) {
        HotbarSmoothScroll.onWheel(qzuilib$oldSlot, currentItem, direction);
    }
}
