package com.gtnh.qzuilibenhance.mixin.early;

import java.lang.reflect.Field;

import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.gtnh.qzuilibenhance.MyMod;
import com.gtnh.qzuilibenhance.vanilla.ChatScrollSmooth;
import com.gtnh.qzuilibenhance.vanilla.HudCache;
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
    private static Field qzuilib$scrollField2;

    @Unique
    private boolean qzuilib$pushed;
    @Unique
    private int qzuilib$logTick;

    @Inject(method = "scroll", at = @At("HEAD"))
    private void qzuilib$onScroll(int amount, CallbackInfo ci) {
        MyMod.LOG.info("[ChatScroll] scroll amount={}", Integer.valueOf(amount));
    }

    @Inject(method = "drawChat", at = @At("HEAD"))
    private void qzuilib$head(int counter, CallbackInfo ci) {
        qzuilib$pushed = false;
        float d = 0.0F;
        try {
            Minecraft mc = Minecraft.getMinecraft();
            boolean open = mc.currentScreen instanceof GuiChat && SmoothScrollConfig.enabled;
            int s1 = qzuilib$read(1);
            int s2 = qzuilib$read(2);
            if (open && (qzuilib$logTick++ % 40) == 0) {
                MyMod.LOG.info("[ChatField] f146250_j={} f146251_k={}", Integer.valueOf(s1), Integer.valueOf(s2));
            }
            d = ChatScrollSmooth.update(s1, open);
        } catch (Throwable ignored) {
        }
        if (d != 0.0F) {
            GL11.glPushMatrix();
            GL11.glTranslatef(0.0F, d, 0.0F);
            qzuilib$pushed = true;
            HudCache.markDirty();
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
    private int qzuilib$read(int which) {
        try {
            Field field;
            if (which == 1) {
                if (qzuilib$scrollField == null) {
                    qzuilib$scrollField = GuiNewChat.class.getDeclaredField("field_146250_j");
                    qzuilib$scrollField.setAccessible(true);
                }
                field = qzuilib$scrollField;
            } else {
                if (qzuilib$scrollField2 == null) {
                    qzuilib$scrollField2 = GuiNewChat.class.getDeclaredField("field_146251_k");
                    qzuilib$scrollField2.setAccessible(true);
                }
                field = qzuilib$scrollField2;
            }
            Object value = field.get(this);
            if (value instanceof Number) {
                return ((Number) value).intValue();
            }
            if (value instanceof Boolean) {
                return ((Boolean) value).booleanValue() ? 1 : 0;
            }
        } catch (Throwable ignored) {
        }
        return 0;
    }
}
