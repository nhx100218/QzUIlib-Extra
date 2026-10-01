package com.gtnh.qzuilibenhance.vanilla;

/** 快捷栏选中框的平滑缓动（显示槽位向实际槽位插值）。 */
public final class HotbarSmoothScroll {

    private static float sDisplayed = 0.0F;
    private static boolean sInit;

    private HotbarSmoothScroll() {}

    /** 每帧推进并返回当前显示槽位（浮点）。跨度过大（如 8→0）直接吸附，避免长距离滑动。 */
    public static float update(int current) {
        if (!SmoothScrollConfig.enabled) {
            sDisplayed = current;
            sInit = true;
            return current;
        }
        if (!sInit) {
            sDisplayed = current;
            sInit = true;
            return sDisplayed;
        }
        float diff = current - sDisplayed;
        if (diff > 1.5F || diff < -1.5F) {
            sDisplayed = current;
            return sDisplayed;
        }
        float factor = Math.max(0.05F, Math.min(1.0F, SmoothScrollConfig.factor));
        sDisplayed += diff * factor;
        if (Math.abs(current - sDisplayed) < 0.01F) {
            sDisplayed = current;
        }
        return sDisplayed;
    }
}
