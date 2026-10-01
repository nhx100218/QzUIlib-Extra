package com.gtnh.qzuilibenhance.mixin.late.modularui;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.gtnh.qzuilibenhance.vanilla.ScreenTransition;

/**
 * 让 ModularUI2（格雷）界面也参与切换动画。ModularScreen 在自身 drawScreen 内重置矩阵，
 * 外层包裹无效，故直接在其 drawScreen 入口施加位移。
 */
@Mixin(targets = "com.cleanroommc.modularui.screen.ModularScreen")
public class MixinModularScreenTransition {

    @Inject(method = "drawScreen", at = @At("HEAD"), remap = false)
    private void qzuilib$pre(CallbackInfo ci) {
        ScreenTransition.push();
    }

    @Inject(method = "drawScreen", at = @At("RETURN"), remap = false)
    private void qzuilib$post(CallbackInfo ci) {
        ScreenTransition.pop();
    }
}
