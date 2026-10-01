package com.gtnh.qzuilibenhance.vanilla;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** 聊天行首次可见时间（按未格式化文本），用于逐行淡入。 */
public final class ChatState {

    private static final Map<String, Long> FIRST_SEEN = new ConcurrentHashMap<String, Long>();

    private ChatState() {}

    /** 计算某行的淡入进度 0..1（首次见到为 0，随后升到 1）。 */
    public static float fade(String line) {
        long now = System.currentTimeMillis();
        Long first = FIRST_SEEN.get(line);
        if (first == null) {
            FIRST_SEEN.put(line, now);
            if (FIRST_SEEN.size() > 4000) {
                FIRST_SEEN.clear();
                FIRST_SEEN.put(line, now);
            }
            return 0.0F;
        }
        float t = (now - first) / (float) Math.max(1, ChatConfig.durationMs);
        if (t <= 0.0F) {
            return 0.0F;
        }
        if (t >= 1.0F) {
            return 1.0F;
        }
        // ease-out cubic
        float u = 1.0F - t;
        return 1.0F - u * u * u;
    }
}
