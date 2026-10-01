package com.gtnh.qzuilibenhance.vanilla;

/** 聊天平滑与聊天头像配置。 */
public final class ChatConfig {

    private ChatConfig() {}

    /** 平滑（新消息淡入）总开关。 */
    public static volatile boolean smoothEnabled = true;
    /** 淡入时长(ms)。 */
    public static volatile int durationMs = 200;
    /** 位移幅度(px)，新消息从下往上滑入。 */
    public static volatile float offset = 4.0F;
    /** 聊天头像总开关。 */
    public static volatile boolean headsEnabled = true;
    /** 头像边长(px)。 */
    public static volatile float headSize = 8.0F;
    /** 头像与文字间距(px)。 */
    public static volatile float headGap = 2.0F;
}
