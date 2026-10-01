package com.gtnh.qzuilibenhance.vanilla;

/** 聊天栏滚轮平滑：显示行偏移向实际滚动位置缓动，返回对整体内容的 y 残留偏移。 */
public final class ChatScrollSmooth {

    private static final float LINE = 9.0F;
    private static float sDisplayed;
    private static boolean sInit;

    private ChatScrollSmooth() {}

    public static float update(int target, boolean active) {
        if (!active) {
            sDisplayed = target;
            sInit = true;
            return 0.0F;
        }
        if (!sInit) {
            sDisplayed = target;
            sInit = true;
            return 0.0F;
        }
        float diff = target - sDisplayed;
        float factor = Math.max(0.1F, Math.min(1.0F, SmoothScrollConfig.factor));
        sDisplayed += diff * factor;
        if (Math.abs(target - sDisplayed) < 0.01F) {
            sDisplayed = target;
        }
        return (sDisplayed - target) * LINE;
    }
}
