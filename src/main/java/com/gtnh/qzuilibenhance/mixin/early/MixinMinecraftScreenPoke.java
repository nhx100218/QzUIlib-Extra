package com.gtnh.qzuilibenhance.mixin.early;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.gtnh.qzuilibenhance.vanilla.ScreenTransition;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

/** 每次显示界面都复位切换动画计时，修复“复用同一界面实例重复打开不再播放动画”。 */
@Mixin(Minecraft.class)
public class MixinMinecraftScreenPoke {

    @Inject(method = "displayGuiScreen", at = @At("HEAD"))
    private void qzuilib$poke(GuiScreen screen, CallbackInfo ci) {
        ScreenTransition.poke();
    }
}
