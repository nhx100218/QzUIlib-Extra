package com.gtnh.qzuilibenhance.tooltip;

/**
 * Waila HUD 框的圆角化渲染。
 *
 * <p>Waila 的 {@code OverlayRenderer.drawTooltipBox(x,y,w,h,border,gradTop,gradBottom)} 原本用
 * 多个矩形拼出方角框；这里改画一个圆角矩形，颜色沿用 Waila 的边框色与纵向渐变填充。</p>
 */
public final class WailaTooltipStyle {

    private WailaTooltipStyle() {}

    public static boolean draw(int x, int y, int width, int height, int border, int gradTop,
            int gradBottom) {
        if (!TooltipConfig.enabled || !TooltipConfig.waila || width <= 0 || height <= 0) {
            return false;
        }
        // 颜色沿用 Waila 传入值；启动时 StartupConfigFixer 已把背景改成黑色、外边框改成深灰。
        TooltipRoundedRect.draw(x, y, x + width, y + height, TooltipConfig.wailaCornerRadius,
                Math.max(0.5F, TooltipConfig.wailaBorderWidth),
                gradTop, gradTop, gradBottom, gradBottom,
                border, border, border, border, 300.0);
        return true;
    }
}
