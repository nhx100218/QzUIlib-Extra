package com.gtnh.qzuilibenhance.mixin.early;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.gtnh.qzuilibenhance.vanilla.HotbarSmoothScroll;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraftforge.client.GuiIngameForge;

/** 快捷栏选中框平滑：Forge 的快捷栏绘制在 {@code GuiIngameForge.renderHotbar}。 */
@Mixin(GuiIngameForge.class)
public class MixinGuiIngameHotbarScroll {

    @Redirect(method = "renderHotbar", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/Gui;drawTexturedModalRect(IIIIII)V", ordinal = 1))
    private void qzuilib$selection(GuiIngameForge self, int x, int y, int u, int v, int w, int h) {
        int current = 0;
        try {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc.thePlayer != null) {
                current = mc.thePlayer.inventory.currentItem;
            }
        } catch (Throwable ignored) {
        }
        float displayed = HotbarSmoothScroll.update(current);
        int offset = Math.round((displayed - current) * 20.0F);
        self.drawTexturedModalRect(x + offset, y, u, v, w, h);
    }
}
