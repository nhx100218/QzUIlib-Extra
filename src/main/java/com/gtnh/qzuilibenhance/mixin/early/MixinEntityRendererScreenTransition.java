package com.gtnh.qzuilibenhance.mixin.early;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.gtnh.qzuilibenhance.vanilla.ScreenTransition;
import net.minecraft.client.renderer.EntityRenderer;

/**
 * 在 GUI 绘制调用（{@code currentScreen.drawScreen}）外包裹界面切换位移。
 */
@Mixin(EntityRenderer.class)
public abstract class MixinEntityRendererScreenTransition {

    @Inject(method = "updateCameraAndRender", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiScreen;drawScreen(IIF)V", shift = At.Shift.BEFORE))
    private void qzuilib$preScreenTransition(float partialTicks, CallbackInfo ci) {
        ScreenTransition.push();
    }

    @Inject(method = "updateCameraAndRender", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiScreen;drawScreen(IIF)V", shift = At.Shift.AFTER))
    private void qzuilib$postScreenTransition(float partialTicks, CallbackInfo ci) {
        ScreenTransition.pop();
    }
}
