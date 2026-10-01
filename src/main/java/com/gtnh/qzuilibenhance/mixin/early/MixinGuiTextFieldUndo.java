package com.gtnh.qzuilibenhance.mixin.early;

import java.util.ArrayDeque;

import org.lwjgl.input.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.gtnh.qzuilibenhance.vanilla.UiConfig;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.util.ChatAllowedCharacters;

/** 给原版 GuiTextField 增加 Ctrl+Z 撤销 / Ctrl+Y 重做。 */
@Mixin(GuiTextField.class)
public class MixinGuiTextFieldUndo {

    @Shadow
    private String text;
    @Shadow
    private int cursorPosition;
    @Shadow
    private int selectionEnd;
    @Shadow
    private int lineScrollOffset;

    @Unique
    private final ArrayDeque<Object[]> qzuilib$undo = new ArrayDeque<Object[]>();
    @Unique
    private final ArrayDeque<Object[]> qzuilib$redo = new ArrayDeque<Object[]>();
    @Unique
    private boolean qzuilib$applying;

    /**
     * 在按键处理前，对“会改动文本”的按键记录快照。直接挂在 textboxKeyTyped 上，
     * 不依赖 writeText/deleteFromCursor（子类可能覆写而不调用父类）。
     */
    @Inject(method = "textboxKeyTyped", at = @At("HEAD"))
    private void qzuilib$recordKey(char typedChar, int keyCode, CallbackInfoReturnable<Boolean> cir) {
        if (qzuilib$applying || !UiConfig.textUndoEnabled) {
            return;
        }
        if (GuiScreen.isCtrlKeyDown() && (keyCode == Keyboard.KEY_Z || keyCode == Keyboard.KEY_Y)) {
            return;
        }
        boolean mutates = typedChar == 22 // 粘贴
                || typedChar == 24 // 剪切
                || keyCode == Keyboard.KEY_BACK
                || keyCode == Keyboard.KEY_DELETE
                || (!GuiScreen.isCtrlKeyDown() && ChatAllowedCharacters.isAllowedCharacter(typedChar));
        if (mutates) {
            qzuilib$record();
        }
    }

    @Inject(method = "textboxKeyTyped", at = @At("HEAD"), cancellable = true)
    private void qzuilib$undoRedo(char typedChar, int keyCode, CallbackInfoReturnable<Boolean> cir) {
        if (!UiConfig.textUndoEnabled || !GuiScreen.isCtrlKeyDown()) {
            return;
        }
        if (keyCode == Keyboard.KEY_Z) {
            if (!qzuilib$undo.isEmpty()) {
                qzuilib$redo.push(new Object[] { this.text, this.cursorPosition, this.selectionEnd });
                qzuilib$apply(qzuilib$undo.pop());
            }
            cir.setReturnValue(Boolean.TRUE);
        } else if (keyCode == Keyboard.KEY_Y) {
            if (!qzuilib$redo.isEmpty()) {
                qzuilib$undo.push(new Object[] { this.text, this.cursorPosition, this.selectionEnd });
                qzuilib$apply(qzuilib$redo.pop());
            }
            cir.setReturnValue(Boolean.TRUE);
        }
    }

    @Unique
    private void qzuilib$record() {
        if (qzuilib$applying || !UiConfig.textUndoEnabled) {
            return;
        }
        String current = this.text == null ? "" : this.text;
        if (!qzuilib$undo.isEmpty() && ((String) qzuilib$undo.peek()[0]).equals(current)) {
            return;
        }
        qzuilib$undo.push(new Object[] { current, this.cursorPosition, this.selectionEnd });
        while (qzuilib$undo.size() > Math.max(1, UiConfig.textUndoLimit)) {
            qzuilib$undo.removeLast();
        }
        qzuilib$redo.clear();
    }

    @Unique
    private void qzuilib$apply(Object[] snapshot) {
        qzuilib$applying = true;
        try {
            this.text = snapshot[0] == null ? "" : (String) snapshot[0];
            this.cursorPosition = (Integer) snapshot[1];
            this.selectionEnd = (Integer) snapshot[2];
            this.lineScrollOffset = 0;
        } finally {
            qzuilib$applying = false;
        }
    }
}
