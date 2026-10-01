package com.gtnh.qzuilibenhance.mixin.early;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.gtnh.qzuilibenhance.vanilla.VanillaPauseConfig;

import net.minecraft.client.gui.inventory.GuiContainer;

/** 让容器界面在单机下暂停游戏（原版 isGamePaused 每 tick 依据 doesGuiPauseGame 重算）。 */
@Mixin(GuiContainer.class)
public class MixinGuiContainerPause {

    @Inject(method = "doesGuiPauseGame", at = @At("HEAD"), cancellable = true)
    private void qzuilib$pause(CallbackInfoReturnable<Boolean> cir) {
        if (VanillaPauseConfig.enabled) {
            cir.setReturnValue(Boolean.TRUE);
        }
    }
}
