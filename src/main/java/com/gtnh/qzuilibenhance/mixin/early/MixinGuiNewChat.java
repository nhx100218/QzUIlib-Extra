package com.gtnh.qzuilibenhance.mixin.early;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.gtnh.qzuilibenhance.vanilla.ChatConfig;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.gui.ChatLine;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;

/** 聊天平滑（按 ChatLine 年龄淡入+滑入）与聊天头像（复用 ServerUtilities 皮肤缓存）。 */
@Mixin(GuiNewChat.class)
public class MixinGuiNewChat {

    private static final Pattern SENDER = Pattern.compile("^(?:<([A-Za-z0-9_]{1,16})>|([A-Za-z0-9_]{1,16}):)\\s?");
    private static final Pattern COLOR = Pattern.compile("\u00a7.");

    // 当前正在绘制的行（用于头像）。
    private static ChatLine qzuilib$line;

    // 按行缓存，避免每帧做正则/反射。
    private static final Map<ChatLine, String> qzuilib$names = new WeakHashMap<ChatLine, String>();
    private static final Map<ChatLine, ResourceLocation> qzuilib$skins = new WeakHashMap<ChatLine, ResourceLocation>();

    // ServerUtilities TabSkinCache 反射缓存（可选依赖）。
    private static boolean qzuilib$suResolved;
    private static Object qzuilib$suInstance;
    private static Method qzuilib$suGetSkin;

    @Redirect(method = "drawChat", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/ChatLine;getUpdatedCounter()I"))
    private int qzuilib$captureLine(ChatLine line) {
        qzuilib$line = line;
        return line.getUpdatedCounter();
    }

    @Redirect(method = "drawChat", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/FontRenderer;drawStringWithShadow(Ljava/lang/String;III)I"))
    private int qzuilib$drawLine(FontRenderer font, String text, int x, int y, int color) {
        ChatLine line = qzuilib$line;
        int drawX = x;
        if (ChatConfig.headsEnabled && line != null) {
            int headWidth = drawHead(line, text, x, y);
            if (headWidth > 0) {
                drawX += headWidth;
            }
        }
        String shown = com.gtnh.qzuilibenhance.vanilla.EmojiConfig.enabled
                ? com.gtnh.qzuilibenhance.emoji.EmojiShortcodes.replace(text)
                : text;
        return font.drawStringWithShadow(shown, drawX, y, color);
    }

    private static String qzuilib$sender(ChatLine line, String text) {
        String name = qzuilib$names.get(line);
        if (name != null) {
            return name.isEmpty() ? null : name;
        }
        String plain = COLOR.matcher(text).replaceAll("");
        Matcher matcher = SENDER.matcher(plain);
        name = matcher.find() ? (matcher.group(1) != null ? matcher.group(1) : matcher.group(2)) : "";
        qzuilib$names.put(line, name);
        return name.isEmpty() ? null : name;
    }

    /** 优先用 ServerUtilities 的皮肤缓存（含联网），失败再退回世界内的玩家实体。 */
    private static ResourceLocation qzuilib$skin(String name) {
        Minecraft mc = Minecraft.getMinecraft();
        try {
            if (!qzuilib$suResolved) {
                qzuilib$suResolved = true;
                Class<?> cache = Class.forName("serverutils.client.tab.TabSkinCache");
                Field instance = cache.getField("INSTANCE");
                qzuilib$suInstance = instance.get(null);
                qzuilib$suGetSkin = cache.getMethod("getOrLoadSkin", String.class);
            }
            if (qzuilib$suGetSkin != null && qzuilib$suInstance != null) {
                Object skin = qzuilib$suGetSkin.invoke(qzuilib$suInstance, name);
                if (skin instanceof ResourceLocation) {
                    return (ResourceLocation) skin;
                }
            }
        } catch (Throwable ignored) {
        }
        if (mc != null && mc.theWorld != null) {
            for (Object o : mc.theWorld.playerEntities) {
                if (o instanceof AbstractClientPlayer && ((AbstractClientPlayer) o).getCommandSenderName().equals(name)) {
                    return ((AbstractClientPlayer) o).getLocationSkin();
                }
            }
        }
        return null;
    }

    /** 在行首绘制皮肤头部，返回占用宽度（0 表示未绘制）。 */
    private static int drawHead(ChatLine line, String text, int x, int y) {
        String name = qzuilib$sender(line, text);
        if (name == null) {
            return 0;
        }
        ResourceLocation skin = qzuilib$skins.get(line);
        if (skin == null) {
            skin = qzuilib$skin(name);
            if (skin == null) {
                return 0;
            }
            qzuilib$skins.put(line, skin);
        }
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null) {
            return 0;
        }
        float size = ChatConfig.headSize;
        float gap = ChatConfig.headGap;
        try {
            mc.getTextureManager().bindTexture(skin);
            // 脸部位于像素 (8,8)-(16,16)；宽度恒为 64，高度 32(旧版) 或 64(含帽子层)需运行时判定。
            int texH = 64;
            try {
                int h = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_HEIGHT);
                if (h >= 16) {
                    texH = h;
                }
            } catch (Throwable ignored) {
            }
            float u0 = 8.0F / 64.0F;
            float u1 = 16.0F / 64.0F;
            float v0 = 8.0F / texH;
            float v1 = 16.0F / texH;
            float top = y - 1.0F;
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            Tessellator tessellator = Tessellator.instance;
            tessellator.startDrawingQuads();
            tessellator.addVertexWithUV(x, top + size, 0.0D, u0, v1);
            tessellator.addVertexWithUV(x + size, top + size, 0.0D, u1, v1);
            tessellator.addVertexWithUV(x + size, top, 0.0D, u1, v0);
            tessellator.addVertexWithUV(x, top, 0.0D, u0, v0);
            tessellator.draw();
        } catch (Throwable ignored) {
            return 0;
        }
        return (int) (size + gap);
    }
}
