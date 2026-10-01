package com.gtnh.qzuilibenhance.vanilla;

/** 通用界面增强开关。 */
public final class UiConfig {

    public static volatile boolean textUndoEnabled = true;
    public static volatile int textUndoLimit = 100;
    /** 文本框选中高亮（替代原版 XOR 蓝色）。 */
    public static volatile boolean selectionEnabled = true;
    public static volatile int selectionColor = 0x33B5E5;
    public static volatile int selectionAlpha = 0x38;

    private UiConfig() {}
}
