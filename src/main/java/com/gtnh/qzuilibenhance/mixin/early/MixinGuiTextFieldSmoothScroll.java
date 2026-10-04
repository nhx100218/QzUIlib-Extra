package com.gtnh.qzuilibenhance.mixin.early;

import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.gtnh.qzuilibenhance.vanilla.SmoothScrollConfig;
import com.gtnh.qzuilibenhance.vanilla.SmoothScrollModel;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;

/**
 * 文本框水平滚动的像素级平滑（对齐 Minecraft-Smooth-Scrolling 的 Text Input Field）。
 *
 * <p>原版 {@code drawTextBox} 按 {@code lineScrollOffset} 整字符跳过。这里在绘制期间把显示起点
 * 换成由缓动像素位置推出的字符下标，并平移分数像素；绘制结束恢复真实 {@code lineScrollOffset}，
 * 不污染原版光标/选择逻辑。文本框始终使用标准字体，故直接取 {@code Minecraft.fontRenderer}。</p>
 */
@Mixin(GuiTextField.class)
public abstract class MixinGuiTextFieldSmoothScroll {

    @Shadow
    private int lineScrollOffset;

    @Shadow
    private String text;

    @Unique
    private final SmoothScrollModel qzuilib$model = new SmoothScrollModel();
    @Unique
    private int qzuilib$savedOffset;
    @Unique
    private boolean qzuilib$pushed;

    @Inject(method = "drawTextBox", at = @At("HEAD"))
    private void qzuilib$head(CallbackInfo ci) {
        qzuilib$savedOffset = lineScrollOffset;
        qzuilib$pushed = false;
        FontRenderer font = Minecraft.getMinecraft().fontRenderer;
        if (!SmoothScrollConfig.enabled || SmoothScrollConfig.textSmoothness == 0 || text == null || font == null) {
            return;
        }
        double target = font.getStringWidth(qzuilib$prefix(text, lineScrollOffset));
        // 光标向右移动（输入）时立即到位，避免缓动落后把正在输入的字符挤出可视区；
        // 向左回退时才缓动。
        if (target > qzuilib$model.current()) {
            qzuilib$model.snap(target);
        } else {
            qzuilib$model.setTarget(target);
        }
        double smoothPixels = qzuilib$model.update(
                SmoothScrollConfig.smoothness(SmoothScrollConfig.textSmoothness));
        int index = qzuilib$indexFor(font, text, smoothPixels);
        double prefixWidth = font.getStringWidth(qzuilib$prefix(text, index));
        lineScrollOffset = index;
        float shift = (float) (prefixWidth - smoothPixels);
        if (shift != 0.0F) {
            GL11.glPushMatrix();
            GL11.glTranslatef(shift, 0.0F, 0.0F);
            qzuilib$pushed = true;
        }
    }

    @Inject(method = "drawTextBox", at = @At("RETURN"))
    private void qzuilib$return(CallbackInfo ci) {
        lineScrollOffset = qzuilib$savedOffset;
        if (qzuilib$pushed) {
            GL11.glPopMatrix();
            qzuilib$pushed = false;
        }
    }

    @Unique
    private static String qzuilib$prefix(String value, int length) {
        int end = length < 0 ? 0 : (length > value.length() ? value.length() : length);
        return value.substring(0, end);
    }

    /** 返回宽度不超过 {@code pixels} 的最大字符前缀长度。 */
    @Unique
    private static int qzuilib$indexFor(FontRenderer font, String value, double pixels) {
        double width = 0.0D;
        int index = 0;
        while (index < value.length()) {
            width += font.getCharWidth(value.charAt(index));
            if (width > pixels) {
                break;
            }
            index++;
        }
        return index;
    }
}
