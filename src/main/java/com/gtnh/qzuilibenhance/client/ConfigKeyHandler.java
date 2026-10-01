package com.gtnh.qzuilibenhance.client;

import org.lwjgl.input.Keyboard;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

/** Ctrl+K 打开配置页（对齐 ModernUI）。 */
public class ConfigKeyHandler {

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        try {
            if (Keyboard.getEventKeyState()
                    && Keyboard.getEventKey() == Keyboard.KEY_K
                    && GuiScreen.isCtrlKeyDown()) {
                Minecraft mc = Minecraft.getMinecraft();
                if (mc.currentScreen == null) {
                    mc.displayGuiScreen(new com.gtnh.qzuilibenhance.client.md.ModernConfigScreen(null));
                }
            }
        } catch (Throwable ignored) {
        }
    }
}
