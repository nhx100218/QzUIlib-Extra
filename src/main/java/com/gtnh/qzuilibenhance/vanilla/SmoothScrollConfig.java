package com.gtnh.qzuilibenhance.vanilla;

/**
 * 原版可滚动列表（{@code GuiSlot}）的平滑滚动配置。
 */
public final class SmoothScrollConfig {

    private SmoothScrollConfig() {}

    /** 是否启用平滑滚动。 */
    public static volatile boolean enabled = true;

    /** 每帧向目标插值的比例（0~1，越大越快）。 */
    public static volatile float factor = 0.35F;
}
