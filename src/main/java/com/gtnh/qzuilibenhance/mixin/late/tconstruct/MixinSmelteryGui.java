package com.gtnh.qzuilibenhance.mixin.late.tconstruct;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.gtnh.qzuilibenhance.tooltip.TooltipRenderer;
import net.minecraft.client.Minecraft;

/**
 * 匠魂炉（SmelteryGui）的液体/燃料 tooltip 由 TConstruct 自绘
 * {@code drawToolTip(List,int,int)}，这里替换为 UILib 的圆角 tooltip。
 */
@Mixin(targets = "tconstruct.smeltery.gui.SmelteryGui")
public class MixinSmelteryGui {

    @Inject(method = "drawToolTip", at = @At("HEAD"), cancellable = true, remap = false)
    private void qzuilib$drawToolTip(List<String> lines, int x, int y, CallbackInfo ci) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.currentScreen == null) {
            return;
        }
        if (TooltipRenderer.draw(lines, x, y, mc.fontRenderer, mc.currentScreen.width,
                mc.currentScreen.height)) {
            ci.cancel();
        }
    }
}
