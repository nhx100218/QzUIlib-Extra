package com.gtnh.qzuilibenhance.client.md;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * {@code config/qzuilib-modern.yaml} 的轻量读写模型。
 *
 * <p>纯文本 + 正则实现，刻意不引入 snakeyaml 等额外依赖：QzUILib 的新栈配置格式稳定，
 * 本附属页只需暴露少量字段。已覆盖：</p>
 * <ul>
 *   <li>{@code general.chatFrame}</li>
 *   <li>{@code general.pickerDensity}</li>
 *   <li>{@code fontSystem.fontSort}</li>
 *   <li>{@code fontSizeSetting.glyphGenerationSize} / {@code gameCharSize}</li>
 * </ul>
 *
 * <p>写盘只改动目标行/目标块，保留其余内容与注释原样（与
 * {@code vanilla/StartupConfigFixer.fixFontSort} 同一取向）。YAML 的 section 层级用于
 * 缩小匹配范围，避免跨 section 误伤同名键。</p>
 */
public final class QzUiLibModernConfig {

    /** 与 QzUILib schema 对齐的可选值。 */
    public static final String[] CHAT_FRAME = { "custom", "vanilla" };
    public static final String[] PICKER_DENSITY = { "auto", "compact", "standard", "roomy" };

    private final File file;
    private String raw = "";
    private boolean loaded;

    private String chatFrame = "custom";
    private String pickerDensity = "auto";
    private double glyphGenerationSize = 64.0D;
    private double gameCharSize = 9.0D;
    private final List<String> fontSort = new ArrayList<String>();

    public QzUiLibModernConfig(File file) {
        this.file = file;
        load();
    }

    public File file() {
        return file;
    }

    /** 文件存在且解析成功时为 true；否则页面显示占位提示。 */
    public boolean available() {
        return loaded;
    }

    /** 从磁盘重新读取（取消改动 / 打开页面时调用）。 */
    public void load() {
        loaded = false;
        if (file == null || !file.isFile()) {
            return;
        }
        try {
            raw = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return;
        }
        String cf = readScalar("general", "chatFrame");
        if (cf != null && cf.length() > 0) {
            chatFrame = cf;
        }
        String pd = readScalar("general", "pickerDensity");
        if (pd != null && pd.length() > 0) {
            pickerDensity = pd;
        }
        Double g = readDouble("fontSizeSetting", "glyphGenerationSize");
        if (g != null) {
            glyphGenerationSize = g.doubleValue();
        }
        Double c = readDouble("fontSizeSetting", "gameCharSize");
        if (c != null) {
            gameCharSize = c.doubleValue();
        }
        fontSort.clear();
        fontSort.addAll(readList("fontSystem", "fontSort"));
        loaded = true;
    }

    /** 把当前内存模型写回磁盘；失败静默忽略（配置页不应因 IO 崩溃）。 */
    public void save() {
        if (file == null) {
            return;
        }
        String text = raw == null ? "" : raw;
        text = writeScalar(text, "general", "chatFrame", chatFrame);
        text = writeScalar(text, "general", "pickerDensity", pickerDensity);
        text = writeScalar(text, "fontSizeSetting", "glyphGenerationSize", number(glyphGenerationSize));
        text = writeScalar(text, "fontSizeSetting", "gameCharSize", number(gameCharSize));
        text = writeList(text, "fontSystem", "fontSort", fontSort);
        try {
            File parent = file.getParentFile();
            if (parent != null && !parent.isDirectory()) {
                parent.mkdirs();
            }
            Files.write(file.toPath(), text.getBytes(StandardCharsets.UTF_8));
            raw = text;
        } catch (IOException ignored) {
            // 页面静默处理，避免 IO 异常影响 GUI 生命周期
        }
    }

    public String chatFrame() {
        return chatFrame;
    }

    public void chatFrame(String value) {
        if (value != null) {
            chatFrame = value;
        }
    }

    public String pickerDensity() {
        return pickerDensity;
    }

    public void pickerDensity(String value) {
        if (value != null) {
            pickerDensity = value;
        }
    }

    public double glyphGenerationSize() {
        return glyphGenerationSize;
    }

    public void glyphGenerationSize(double value) {
        glyphGenerationSize = value;
    }

    public double gameCharSize() {
        return gameCharSize;
    }

    public void gameCharSize(double value) {
        gameCharSize = value;
    }

    public List<String> fontSort() {
        return fontSort;
    }

    /** 交换 {@code index} 与 {@code index + delta} 的字体；越界返回 false。 */
    public boolean moveFontSort(int index, int delta) {
        int target = index + delta;
        if (index < 0 || index >= fontSort.size() || target < 0 || target >= fontSort.size()) {
            return false;
        }
        String value = fontSort.remove(index);
        fontSort.add(target, value);
        return true;
    }

    // ---------- YAML 文本处理 ----------

    /** 返回 section 块在原始文本中的 [start, end) 区间；找不到返回 null。 */
    private static int[] sectionRange(String text, String section) {
        Matcher head = Pattern.compile("(?m)^" + Pattern.quote(section) + ":[ \\t]*\\r?$").matcher(text);
        if (!head.find()) {
            return null;
        }
        int start = head.end();
        Matcher next = Pattern.compile("(?m)^\\S.*:[ \\t]*\\r?$").matcher(text);
        next.region(start, text.length());
        int end = next.find() ? next.start() : text.length();
        return new int[] { start, end };
    }

    private String readScalar(String section, String key) {
        int[] range = sectionRange(raw, section);
        String scope = range == null ? raw : raw.substring(range[0], range[1]);
        Matcher m = Pattern.compile("(?m)^[ \\t]*" + Pattern.quote(key) + ":[ \\t]*(\\S[^\\r\\n]*)").matcher(scope);
        if (!m.find()) {
            return null;
        }
        String value = m.group(1);
        int hash = value.indexOf(" #");
        if (hash >= 0) {
            value = value.substring(0, hash);
        }
        return unquote(value.trim());
    }

    private Double readDouble(String section, String key) {
        String value = readScalar(section, key);
        if (value == null) {
            return null;
        }
        try {
            return Double.valueOf(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private List<String> readList(String section, String key) {
        List<String> out = new ArrayList<String>();
        int[] range = sectionRange(raw, section);
        if (range == null) {
            return out;
        }
        String scope = raw.substring(range[0], range[1]);
        Matcher m = Pattern.compile("(?m)^[ \\t]*" + Pattern.quote(key) + ":[ \\t]*(.*)$").matcher(scope);
        if (!m.find()) {
            return out;
        }
        String inline = m.group(1).trim();
        if (inline.startsWith("[")) {
            String body = inline.substring(1);
            int close = body.lastIndexOf(']');
            if (close >= 0) {
                body = body.substring(0, close);
            }
            for (String part : body.split(",")) {
                String item = unquote(part.trim());
                if (item.length() > 0) {
                    out.add(item);
                }
            }
            return out;
        }
        int cursor = m.end();
        String[] lines = scope.substring(cursor).split("\n", -1);
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.length() == 0) {
                continue;
            }
            if (trimmed.equals("-")) {
                out.add("");
            } else if (trimmed.startsWith("- ")) {
                out.add(unquote(trimmed.substring(2).trim()));
            } else {
                break;
            }
        }
        return out;
    }

    private static String writeScalar(String text, String section, String key, String value) {
        int[] range = sectionRange(text, section);
        if (range == null) {
            return text;
        }
        String scope = text.substring(range[0], range[1]);
        Matcher m = Pattern.compile("(?m)^([ \\t]*" + Pattern.quote(key) + ":)[ \\t]*[^\\r\\n]*").matcher(scope);
        if (!m.find()) {
            return text;
        }
        StringBuffer buffer = new StringBuffer();
        m.appendReplacement(buffer, Matcher.quoteReplacement(m.group(1) + " " + value));
        m.appendTail(buffer);
        return text.substring(0, range[0]) + buffer.toString() + text.substring(range[1]);
    }

    private static String writeList(String text, String section, String key, List<String> values) {
        int[] range = sectionRange(text, section);
        if (range == null) {
            return text;
        }
        String scope = text.substring(range[0], range[1]);
        Pattern pattern = Pattern.compile("(?m)^([ \\t]*)" + Pattern.quote(key) + ":[ \\t]*(?:\\[[^\\r\\n]*\\])?\\r?\\n?"
                + "((?:[ \\t]*-[^\\r\\n]*(?:\\r?\\n|$))*)");
        Matcher m = pattern.matcher(scope);
        if (!m.find()) {
            return text;
        }
        String indent = m.group(1);
        StringBuilder replacement = new StringBuilder();
        replacement.append(indent).append(key).append(':');
        if (values.isEmpty()) {
            replacement.append(" []\n");
        } else {
            for (String value : values) {
                replacement.append('\n').append(indent).append("- ").append(quoteIfNeeded(value));
            }
            replacement.append('\n');
        }
        String scopeUpdated = scope.substring(0, m.start()) + replacement + scope.substring(m.end());
        return text.substring(0, range[0]) + scopeUpdated + text.substring(range[1]);
    }

    private static String number(double value) {
        return String.format(java.util.Locale.ROOT, "%.1f", value);
    }

    private static String quoteIfNeeded(String value) {
        if (value == null) {
            return "";
        }
        boolean needsQuote = value.length() == 0
                || value.charAt(0) == ' '
                || value.charAt(value.length() - 1) == ' '
                || value.indexOf(':') >= 0
                || value.indexOf('#') >= 0
                || value.startsWith("-")
                || value.startsWith("[")
                || value.startsWith("{")
                || value.startsWith("\"")
                || value.startsWith("'");
        if (!needsQuote) {
            return value;
        }
        return '"' + value.replace("\\", "\\\\").replace("\"", "\\\"") + '"';
    }

    private static String unquote(String value) {
        if (value == null || value.length() < 2) {
            return value;
        }
        char first = value.charAt(0);
        char last = value.charAt(value.length() - 1);
        if ((first == '"' && last == '"') || (first == '\'' && last == '\'')) {
            String inner = value.substring(1, value.length() - 1);
            if (first == '"') {
                inner = inner.replace("\\\"", "\"").replace("\\\\", "\\");
            } else {
                inner = inner.replace("''", "'");
            }
            return inner;
        }
        return value;
    }
}
