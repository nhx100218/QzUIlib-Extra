package com.gtnh.qzuilibenhance.mixin.early;

import java.lang.reflect.Field;

import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.gtnh.qzuilibenhance.vanilla.ChatScrollSmooth;
import com.gtnh.qzuilibenhance.vanilla.SmoothScrollConfig;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiNewChat;

/** 聊天栏滚轮平滑：整体按缓动残差做纵向位移。 */
@Mixin(GuiNewChat.class)
public class MixinGuiNewChatScroll {

    @Unique
    private static Field qzuilib$scrollField;

    @Unique
    private boolean qzuilib$pushed;

    @Inject(method = "drawChat", at = @At("HEAD"))
    private void qzuilib$head(int counter, CallbackInfo ci) {
        qzuilib$pushed = false;
        float d = 0.0F;
        try {
            Minecraft mc = Minecraft.getMinecraft();
            boolean open = mc.currentScreen instanceof GuiChat && SmoothScrollConfig.enabled;
            d = ChatScrollSmooth.update(qzuilib$scrollPos(), open);
        } catch (Throwable ignored) {
        }
        if (d != 0.0F) {
            GL11.glPushMatrix();
            GL11.glTranslatef(0.0F, d, 0.0F);
            qzuilib$pushed = true;
        }
    }

    @Inject(method = "drawChat", at = @At("RETURN"))
    private void qzuilib$ret(int counter, CallbackInfo ci) {
        if (qzuilib$pushed) {
            GL11.glPopMatrix();
            qzuilib$pushed = false;
        }
    }

    @Unique
    private int qzuilib$scrollPos() {
        try {
            if (qzuilib$scrollField == null) {
                qzuilib$scrollField = GuiNewChat.class.getDeclaredField("field_146250_j");
                qzuilib$scrollField.setAccessible(true);
            }
            return qzuilib$scrollField.getInt(this);
        } catch (Throwable t) {
            return 0;
        }
    }
}
