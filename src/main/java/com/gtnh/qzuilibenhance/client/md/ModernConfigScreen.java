package com.gtnh.qzuilibenhance.client.md;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.input.Mouse;

import com.gtnh.qzuilibenhance.Config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

/**
 * Material Design 3 配置页（全自绘控件）。
 *
 * <p>结构对齐 QzUILib 配置页的 Material settings 骨架：</p>
 * <pre>
 * scrim（世界遮罩）
 *   page（大圆角表面）
 *     titleBar   页标题 + 副标题
 *     navPane    固定左栏：图标 + 文字导航
 *     tabs       内容区顶部标签页
 *     rows       圆角字段卡片：开关 / 滑条 / 下拉 / 分段按钮 / 字体排序列表
 *     actionBar  取消 / 应用 / 完成
 * </pre>
 *
 * <p>数据源分两类：</p>
 * <ul>
 *   <li>「附属增强」页直读 {@code qzuilibenhance.cfg} 的 Forge {@link Configuration}，
 *       改动即时 {@code save()} + {@link Config#apply}。</li>
 *   <li>「QzUILib」页经 {@link QzUiLibModernConfig} 文本读写
 *       {@code config/qzuilib-modern.yaml}（保存后需重启游戏生效）。</li>
 * </ul>
 *
 * <p>文字统一走 {@code mc.fontRenderer}（QzUILib 的 FontRenderer mixin → SDF）。</p>
 */
public class ModernConfigScreen extends GuiScreen {

    // ===== Material 3 暗色 token（与 QzUILib ConfigTheme 对齐） =====
    static final int SCRIM = 0xCC111318;
    static final int SURFACE = 0xFF1B1B1F;
    static final int CONTAINER = 0xFF211F26;
    static final int FIELD = 0xFF2B2930;
    static final int FIELD_HOVER = 0xFF36343B;
    static final int NAV_ACTIVE = 0xFF4A4458;
    static final int BORDER = 0xFF49454F;
    static final int PRIMARY = 0xFFD0BCFF;
    static final int ON_PRIMARY = 0xFF381E72;
    static final int ON_ACTIVE = 0xFFE8DEF8;
    static final int TEXT = 0xFFE6E1E5;
    static final int MUTED = 0xFFCAC4D0;
    static final int TRACK = 0xFF49454F;
    static final int ERROR = 0xFFFFB4AB;

    static final int MARGIN = 12;
    static final int TITLE_H = 44;
    static final int ACTION_H = 52;
    static final int NAV_W = 168;
    static final int ROW_H = 44;
    static final int ROW_GAP = 8;

    private final GuiScreen parent;
    private final List<String> cats = new ArrayList<String>();
    private final List<Widget> widgets = new ArrayList<Widget>();
    private final Map<Property, Object> snapshot = new HashMap<Property, Object>();
    private boolean snapshotTaken;

    /** 0 = 主页, 1 = 附属增强, 2 = QzUILib。 */
    private int navIndex;
    /** 附属增强页 = 分类下标；QzUILib 页 = 标签页下标。 */
    private int tabIndex;
    private int scroll;
    private int contentHeight;
    private int tabsBottom;
    private Widget dragging;
    private DropdownWidget openDropdown;

    private QzUiLibModernConfig qz;
    private boolean yamlDirty;

    public ModernConfigScreen(GuiScreen parent) {
        this.parent = parent;
    }

    // ---------- 几何 ----------

    private int pageX() {
        return MARGIN;
    }

    private int pageY() {
        return MARGIN;
    }

    private int pageW() {
        return width - MARGIN * 2;
    }

    private int pageH() {
        return height - MARGIN * 2;
    }

    private int bodyY() {
        return pageY() + TITLE_H;
    }

    private int actionY() {
        return pageY() + pageH() - ACTION_H;
    }

    private int navW() {
        return Math.min(NAV_W, Math.max(132, width / 4));
    }

    private int contentX() {
        return pageX() + navW() + 8;
    }

    private int contentW() {
        return pageW() - navW() - 8;
    }

    private int listTop() {
        return tabsBottom;
    }

    private int listBottom() {
        return actionY() - 8;
    }

    private int tabCount() {
        if (navIndex == 1) {
            return cats.size();
        }
        if (navIndex == 2) {
            return 3;
        }
        return 0;
    }

    private String currentCategory() {
        return (navIndex == 1 && tabIndex >= 0 && tabIndex < cats.size()) ? cats.get(tabIndex) : null;
    }

    // ---------- 生命周期 ----------

    @Override
    public void initGui() {
        Configuration cfg = Config.getConfiguration();
        cats.clear();
        if (cfg != null) {
            cats.addAll(cfg.getCategoryNames());
            Collections.sort(cats);
        }
        if (qz == null) {
            File dir = mc != null ? mc.mcDataDir : new File(".");
            qz = new QzUiLibModernConfig(new File(new File(dir, "config"), "qzuilib-modern.yaml"));
        }
        if (navIndex > 2) {
            navIndex = 0;
        }
        if (tabIndex >= tabCount() || tabIndex < 0) {
            tabIndex = 0;
        }
        takeSnapshot(cfg);
        layout();
    }

    private void takeSnapshot(Configuration cfg) {
        if (snapshotTaken || cfg == null) {
            return;
        }
        for (String c : cfg.getCategoryNames()) {
            for (Property p : cfg.getCategory(c).getValues().values()) {
                snapshot.put(p, snapshotValue(p));
            }
        }
        snapshotTaken = true;
    }

    private static Object snapshotValue(Property p) {
        switch (p.getType()) {
            case BOOLEAN:
                return Boolean.valueOf(p.getBoolean());
            case INTEGER:
                return Integer.valueOf(p.getInt());
            case DOUBLE:
                return Double.valueOf(p.getDouble());
            default:
                return p.isList() ? p.getStringList() : p.getString();
        }
    }

    // ---------- 布局 ----------

    private void layout() {
        widgets.clear();
        openDropdown = null;
        layoutNav();
        layoutTabs();
        contentHeight = 0;

        if (navIndex == 1) {
            layoutEnhance();
        } else if (navIndex == 2) {
            layoutQzUiLib();
        } else {
            scroll = 0;
        }

        layoutAction();
    }

    private void layoutNav() {
        int x = pageX() + 8;
        int y = bodyY() + 10;
        int w = navW() - 16;
        addNav(0, "home", x, y, w, I18n.format("qzuilibenhance.md.nav.home"));
        y += 42;
        addNav(1, "tune", x, y, w, I18n.format("qzuilibenhance.md.nav.addon"));
        y += 42;
        addNav(2, "font", x, y, w, I18n.format("qzuilibenhance.md.nav.qzuilib"));
    }

    private void addNav(int id, String icon, int x, int y, int w, String label) {
        widgets.add(new NavWidget(id, icon, x, y, w, 36, label));
    }

    private void layoutTabs() {
        int count = tabCount();
        if (count <= 0) {
            tabsBottom = bodyY() + 8;
            return;
        }
        int start = contentX() + 4;
        int maxX = contentX() + contentW() - 4;
        int rowH = 30;
        int x = start;
        int y = bodyY() + 6;
        int lastBottom = y;
        for (int i = 0; i < count; i++) {
            String label = navIndex == 1 ? I18n.format("qzuilibenhance.config.cat." + cats.get(i))
                    : I18n.format(qzTabKey(i));
            int w = mc.fontRenderer.getStringWidth(label) + 22;
            if (x > start && x + w > maxX) {
                x = start;
                y += rowH;
            }
            widgets.add(new TabWidget(i, x, y, w, 26, label));
            lastBottom = y + 26;
            x += w + 6;
        }
        tabsBottom = lastBottom + 8;
    }

    private static String qzTabKey(int index) {
        switch (index) {
            case 0:
                return "qzuilibenhance.md.tab.general";
            case 1:
                return "qzuilibenhance.md.tab.fontsystem";
            default:
                return "qzuilibenhance.md.tab.fontsize";
        }
    }

    private void layoutEnhance() {
        Configuration cfg = Config.getConfiguration();
        String category = currentCategory();
        if (category == null || cfg == null) {
            return;
        }
        List<Property> props = new ArrayList<Property>(cfg.getCategory(category).getValues().values());
        int row = ROW_H + ROW_GAP;
        contentHeight = props.isEmpty() ? 0 : props.size() * row - ROW_GAP;
        int view = listBottom() - listTop();
        int max = Math.max(0, contentHeight - view);
        if (scroll > max) {
            scroll = max;
        }
        if (scroll < 0) {
            scroll = 0;
        }
        for (int i = 0; i < props.size(); i++) {
            Property p = props.get(i);
            int y = listTop() + i * row - scroll;
            if (y + ROW_H < bodyY() || y > listBottom()) {
                continue;
            }
            String label = I18n.format(p.getLanguageKey());
            String desc = I18n.format(p.getLanguageKey() + ".tooltip");
            if (p.getType() == Property.Type.BOOLEAN) {
                widgets.add(new SwitchWidget(boolBinding(p), contentX(), y, contentW(), ROW_H, label, desc));
            } else if ((p.getType() == Property.Type.INTEGER || p.getType() == Property.Type.DOUBLE) && !p.isList()) {
                double min = parse(p.getMinValue(), 0.0D);
                double max2 = parse(p.getMaxValue(), 1.0D);
                widgets.add(new SliderWidget(doubleBinding(p), min, max2, p.getType() == Property.Type.INTEGER,
                        contentX(), y, contentW(), ROW_H, label, desc));
            } else {
                widgets.add(new TextWidget(stringBinding(p), contentX(), y, contentW(), ROW_H, label, desc));
            }
        }
    }

    private void layoutQzUiLib() {
        if (qz == null) {
            return;
        }
        if (tabIndex == 0) {
            layoutQzGeneral();
        } else if (tabIndex == 1) {
            layoutQzFontSort();
        } else {
            layoutQzFontSize();
        }
    }

    private void layoutQzGeneral() {
        int row = ROW_H + ROW_GAP;
        contentHeight = 2 * row - ROW_GAP;
        String[] chatLabels = new String[QzUiLibModernConfig.CHAT_FRAME.length];
        for (int i = 0; i < chatLabels.length; i++) {
            chatLabels[i] = I18n.format("qzuilibenhance.qzuilib.chatFrame."
                    + QzUiLibModernConfig.CHAT_FRAME[i]);
        }
        String[] densityLabels = new String[QzUiLibModernConfig.PICKER_DENSITY.length];
        for (int i = 0; i < densityLabels.length; i++) {
            densityLabels[i] = I18n.format("qzuilibenhance.qzuilib.pickerDensity."
                    + QzUiLibModernConfig.PICKER_DENSITY[i]);
        }
        int y0 = listTop() - scroll;
        int y1 = listTop() + row - scroll;
        widgets.add(new SegmentedWidget(QzUiLibModernConfig.CHAT_FRAME, chatLabels, new StringBinding() {
            @Override
            public String get() {
                return qz.chatFrame();
            }

            @Override
            public void set(String value) {
                qz.chatFrame(value);
                yamlDirty = true;
            }
        }, contentX(), y0, contentW(), ROW_H, I18n.format("qzuilibenhance.qzuilib.chatFrame"),
                I18n.format("qzuilibenhance.qzuilib.chatFrame.tooltip")));
        widgets.add(new DropdownWidget(QzUiLibModernConfig.PICKER_DENSITY, densityLabels, new StringBinding() {
            @Override
            public String get() {
                return qz.pickerDensity();
            }

            @Override
            public void set(String value) {
                qz.pickerDensity(value);
                yamlDirty = true;
            }
        }, contentX(), y1, contentW(), ROW_H, I18n.format("qzuilibenhance.qzuilib.pickerDensity"),
                I18n.format("qzuilibenhance.qzuilib.pickerDensity.tooltip")));
    }

    private void layoutQzFontSize() {
        int row = ROW_H + ROW_GAP;
        contentHeight = 2 * row - ROW_GAP;
        int y0 = listTop() - scroll;
        int y1 = listTop() + row - scroll;
        widgets.add(new SliderWidget(new DoubleBinding() {
            @Override
            public double get() {
                return qz.glyphGenerationSize();
            }

            @Override
            public void set(double value) {
                qz.glyphGenerationSize(value);
                yamlDirty = true;
            }
        }, 8.0D, 256.0D, true, contentX(), y0, contentW(), ROW_H,
                I18n.format("qzuilibenhance.qzuilib.glyphGenerationSize"),
                I18n.format("qzuilibenhance.qzuilib.glyphGenerationSize.tooltip")));
        widgets.add(new SliderWidget(new DoubleBinding() {
            @Override
            public double get() {
                return qz.gameCharSize();
            }

            @Override
            public void set(double value) {
                qz.gameCharSize(value);
                yamlDirty = true;
            }
        }, 1.0D, 72.0D, true, contentX(), y1, contentW(), ROW_H,
                I18n.format("qzuilibenhance.qzuilib.gameCharSize"),
                I18n.format("qzuilibenhance.qzuilib.gameCharSize.tooltip")));
    }

    private void layoutQzFontSort() {
        int size = qz.fontSort().size();
        int row = 32;
        contentHeight = size == 0 ? 0 : size * row;
        int view = listBottom() - listTop();
        int max = Math.max(0, contentHeight - view);
        if (scroll > max) {
            scroll = max;
        }
        if (scroll < 0) {
            scroll = 0;
        }
        for (int i = 0; i < size; i++) {
            int y = listTop() + i * row - scroll;
            if (y + 30 < bodyY() || y > listBottom()) {
                continue;
            }
            widgets.add(new FontSortRow(i, qz.fontSort().get(i), contentX(), y, contentW(), 30));
        }
    }

    private void layoutAction() {
        int bh = 30;
        int by = actionY() + (ACTION_H - bh) / 2;
        int gap = 8;
        int w = 94;
        int x = pageX() + pageW() - 20 - w;
        widgets.add(new ButtonWidget(999, x, by, w, bh, I18n.format("qzuilibenhance.md.done"), true));
        x -= gap + w;
        widgets.add(new ButtonWidget(997, x, by, w, bh, I18n.format("qzuilibenhance.md.apply"), false));
        x -= gap + w;
        widgets.add(new ButtonWidget(998, x, by, w, bh, I18n.format("qzuilibenhance.md.cancel"), false));
    }

    // ---------- 绘制 ----------

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        Gui.drawRect(0, 0, width, height, SCRIM);
        roundRect(pageX(), pageY(), pageW(), pageH(), 20, SURFACE);
        roundRect(pageX(), bodyY(), navW(), actionY() - bodyY(), 16, CONTAINER);
        roundRect(pageX(), actionY(), pageW(), ACTION_H, 16, CONTAINER);

        drawString(mc.fontRenderer, pageTitle(), pageX() + 20, pageY() + 10, TEXT);
        drawString(mc.fontRenderer, pageSubtitle(), pageX() + 20, pageY() + 26, MUTED);

        if (navIndex == 0) {
            drawHome();
        } else if (navIndex == 2 && tabIndex == 1) {
            drawFontSortHint();
        }

        for (Widget w : widgets) {
            w.draw(mc, mouseX, mouseY);
        }
        drawScrollbar();
        if (openDropdown != null) {
            openDropdown.drawMenu(mc, mouseX, mouseY);
        }
    }

    private String pageTitle() {
        if (navIndex == 1) {
            return I18n.format("qzuilibenhance.md.nav.addon");
        }
        if (navIndex == 2) {
            return I18n.format(qzTabKey(tabIndex));
        }
        return I18n.format("qzuilibenhance.config.title");
    }

    private String pageSubtitle() {
        if (navIndex == 1) {
            return "qzuilibenhance";
        }
        if (navIndex == 2) {
            return "qzuilib-modern.yaml";
        }
        return I18n.format("qzuilibenhance.md.home.hint");
    }

    private void drawHome() {
        int x = contentX();
        int y = bodyY() + 12;
        int w = contentW();
        roundRect(x, y, w, 96, 16, FIELD);
        drawString(mc.fontRenderer, I18n.format("qzuilibenhance.config.title"), x + 18, y + 16, TEXT);
        drawString(mc.fontRenderer, I18n.format("qzuilibenhance.md.home.desc"), x + 18, y + 38, MUTED);
        drawString(mc.fontRenderer, I18n.format("qzuilibenhance.md.home.hint"), x + 18, y + 56, MUTED);
        roundRect(x, y + 108, w, 60, 16, FIELD);
        drawString(mc.fontRenderer, I18n.format("qzuilibenhance.md.nav.qzuilib"), x + 18, y + 124, TEXT);
        drawString(mc.fontRenderer, I18n.format("qzuilibenhance.md.qzuilib.desc"), x + 18, y + 142, MUTED);
    }

    private void drawFontSortHint() {
        String hint = qz.available() ? I18n.format("qzuilibenhance.md.qzuilib.restartHint")
                : I18n.format("qzuilibenhance.md.qzuilib.missing");
        int color = qz.available() ? MUTED : ERROR;
        drawString(mc.fontRenderer, hint, contentX() + 4, listTop() - 14, color);
    }

    private void drawScrollbar() {
        if (contentHeight <= 0) {
            return;
        }
        int view = listBottom() - listTop();
        if (contentHeight <= view) {
            return;
        }
        int trackX = contentX() + contentW() - 4;
        roundRect(trackX, listTop(), 4, view, 2, TRACK);
        int thumbH = Math.max(20, view * view / contentHeight);
        int max = contentHeight - view;
        int thumbY = listTop() + (max <= 0 ? 0 : (view - thumbH) * scroll / max);
        roundRect(trackX, thumbY, 4, thumbH, 2, PRIMARY);
    }

    // ---------- 输入 ----------

    @Override
    public void handleMouseInput() {
        super.handleMouseInput();
        int d = Mouse.getEventDWheel();
        if (d == 0) {
            return;
        }
        if (openDropdown != null) {
            openDropdown.setOpen(false);
            openDropdown = null;
        }
        int view = listBottom() - listTop();
        int max = Math.max(0, contentHeight - view);
        scroll += d > 0 ? -24 : 24;
        if (scroll < 0) {
            scroll = 0;
        }
        if (scroll > max) {
            scroll = max;
        }
        layout();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) {
        if (button != 0) {
            return;
        }
        if (openDropdown != null) {
            if (openDropdown.menuClicked(mouseX, mouseY)) {
                return;
            }
            openDropdown.setOpen(false);
            openDropdown = null;
        }
        for (int i = widgets.size() - 1; i >= 0; i--) {
            if (widgets.get(i).mouseClicked(mouseX, mouseY)) {
                return;
            }
        }
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int button, long timeSinceLastClick) {
        if (dragging != null) {
            dragging.mouseDragged(mouseX, mouseY);
        }
    }

    @Override
    protected void mouseMovedOrUp(int mouseX, int mouseY, int which) {
        if (which >= 0 && dragging != null) {
            dragging.mouseReleased(mouseX, mouseY);
            dragging = null;
        }
    }

    // ---------- 应用 / 取消 / 完成 ----------

    private void applyKeep() {
        Configuration cfg = Config.getConfiguration();
        if (cfg != null) {
            cfg.save();
            Config.apply(cfg);
        }
        if (yamlDirty && qz != null) {
            qz.save();
            yamlDirty = false;
        }
        initGui();
    }

    private void cancelAndClose() {
        Configuration cfg = Config.getConfiguration();
        if (cfg != null) {
            for (Map.Entry<Property, Object> e : snapshot.entrySet()) {
                restore(e.getKey(), e.getValue());
            }
            cfg.save();
            Config.apply(cfg);
        }
        if (yamlDirty && qz != null) {
            qz.load();
            yamlDirty = false;
        }
        mc.displayGuiScreen(parent);
    }

    private static void restore(Property p, Object v) {
        if (v instanceof Boolean) {
            p.set(((Boolean) v).booleanValue());
        } else if (v instanceof Integer) {
            p.set(((Integer) v).intValue());
        } else if (v instanceof Double) {
            p.set(((Double) v).doubleValue());
        } else if (v instanceof String[]) {
            p.set((String[]) v);
        } else {
            p.set(String.valueOf(v));
        }
    }

    // ---------- 绑定 ----------

    interface BoolBinding {
        boolean get();

        void set(boolean value);
    }

    interface DoubleBinding {
        double get();

        void set(double value);
    }

    interface StringBinding {
        String get();

        void set(String value);
    }

    private static BoolBinding boolBinding(final Property p) {
        return new BoolBinding() {
            @Override
            public boolean get() {
                return p.getBoolean();
            }

            @Override
            public void set(boolean value) {
                p.set(value);
            }
        };
    }

    private static DoubleBinding doubleBinding(final Property p) {
        final boolean integer = p.getType() == Property.Type.INTEGER;
        return new DoubleBinding() {
            @Override
            public double get() {
                return integer ? p.getInt() : p.getDouble();
            }

            @Override
            public void set(double value) {
                if (integer) {
                    p.set((int) Math.round(value));
                } else {
                    p.set(value);
                }
            }
        };
    }

    private static StringBinding stringBinding(final Property p) {
        return new StringBinding() {
            @Override
            public String get() {
                return valueText(p);
            }

            @Override
            public void set(String value) {
                // 只读
            }
        };
    }

    private static String valueText(Property p) {
        if (p.isList()) {
            String[] v = p.getStringList();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < v.length; i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append(v[i]);
            }
            return sb.toString();
        }
        return p.getString();
    }

    // ---------- 控件基类 ----------

    abstract class Widget {
        int x;
        int y;
        int w;
        int h;
        String label;
        String desc;

        Widget(int x, int y, int w, int h) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }

        boolean in(int mx, int my) {
            return mx >= x && my >= y && mx < x + w && my < y + h;
        }

        void draw(Minecraft mc, int mx, int my) {}

        boolean mouseClicked(int mx, int my) {
            if (in(mx, my)) {
                dragging = this;
                return true;
            }
            return false;
        }

        void mouseDragged(int mx, int my) {}

        void mouseReleased(int mx, int my) {}

        void drawCard(Minecraft mc, int mx, int my) {
            boolean hover = in(mx, my);
            roundRect(x, y, w, h, 14, hover ? FIELD_HOVER : FIELD);
            int ty = desc != null ? y + 9 : y + (h - 8) / 2;
            if (label != null) {
                drawString(mc.fontRenderer, label, x + 14, ty, TEXT);
            }
            if (desc != null) {
                drawString(mc.fontRenderer, desc, x + 14, y + 24, MUTED);
            }
        }
    }

    class NavWidget extends Widget {
        private final int navId;
        private final String icon;

        NavWidget(int navId, String icon, int x, int y, int w, int h, String label) {
            super(x, y, w, h);
            this.navId = navId;
            this.icon = icon;
            this.label = label;
        }

        @Override
        void draw(Minecraft mc, int mx, int my) {
            boolean active = navIndex == navId;
            if (active) {
                roundRect(x, y, w, h, 18, NAV_ACTIVE);
            } else if (in(mx, my)) {
                roundRect(x, y, w, h, 18, FIELD_HOVER);
            }
            int color = active ? ON_ACTIVE : MUTED;
            drawIcon(icon, x + 12, y + h / 2 - 7, color);
            drawString(mc.fontRenderer, label, x + 34, y + h / 2 - 4, color);
        }

        @Override
        boolean mouseClicked(int mx, int my) {
            if (in(mx, my)) {
                navIndex = navId;
                tabIndex = 0;
                scroll = 0;
                initGui();
                return true;
            }
            return false;
        }
    }

    class TabWidget extends Widget {
        private final int tabId;
        private final String text;

        TabWidget(int tabId, int x, int y, int w, int h, String text) {
            super(x, y, w, h);
            this.tabId = tabId;
            this.text = text;
        }

        @Override
        void draw(Minecraft mc, int mx, int my) {
            boolean active = tabIndex == tabId;
            if (active) {
                roundRect(x, y, w, h, h / 2, NAV_ACTIVE);
            } else if (in(mx, my)) {
                roundRect(x, y, w, h, h / 2, FIELD_HOVER);
            }
            drawCenteredString(mc.fontRenderer, text, x + w / 2, y + (h - 8) / 2, active ? ON_ACTIVE : MUTED);
        }

        @Override
        boolean mouseClicked(int mx, int my) {
            if (in(mx, my)) {
                tabIndex = tabId;
                scroll = 0;
                initGui();
                return true;
            }
            return false;
        }
    }

    class SwitchWidget extends Widget {
        private final BoolBinding binding;

        SwitchWidget(BoolBinding binding, int x, int y, int w, int h, String label, String desc) {
            super(x, y, w, h);
            this.binding = binding;
            this.label = label;
            this.desc = desc;
        }

        @Override
        void draw(Minecraft mc, int mx, int my) {
            drawCard(mc, mx, my);
            int sw = 46;
            int sh = 24;
            int sx = x + w - 18 - sw;
            int sy = y + (h - sh) / 2;
            boolean on = binding.get();
            roundRect(sx, sy, sw, sh, sh / 2, on ? PRIMARY : TRACK);
            int d = sh - 6;
            int kx = on ? sx + sw - 3 - d : sx + 3;
            roundRect(kx, sy + 3, d, d, d / 2, on ? ON_PRIMARY : 0xFFCAC4D0);
        }

        @Override
        boolean mouseClicked(int mx, int my) {
            if (in(mx, my)) {
                binding.set(!binding.get());
                return true;
            }
            return false;
        }
    }

    class SliderWidget extends Widget {
        private static final int CTRL_W = 224;
        private static final int BOX_W = 56;
        private final DoubleBinding binding;
        private final double min;
        private final double max;
        private final boolean integer;

        SliderWidget(DoubleBinding binding, double min, double max, boolean integer, int x, int y, int w, int h,
                String label, String desc) {
            super(x, y, w, h);
            this.binding = binding;
            this.min = min;
            this.max = max;
            this.integer = integer;
            this.label = label;
            this.desc = desc;
        }

        private int trackX() {
            return x + w - 18 - CTRL_W;
        }

        private int trackW() {
            return CTRL_W - BOX_W - 10;
        }

        private int trackY() {
            return y + h / 2 - 2;
        }

        @Override
        void draw(Minecraft mc, int mx, int my) {
            drawCard(mc, mx, my);
            int tx = trackX();
            int tw = trackW();
            int ty = trackY();
            double t = max > min ? (binding.get() - min) / (max - min) : 0.0D;
            t = t < 0.0D ? 0.0D : (t > 1.0D ? 1.0D : t);
            roundRect(tx, ty, tw, 4, 2, TRACK);
            if (t > 0.0D) {
                roundRect(tx, ty, (int) (t * tw), 4, 2, PRIMARY);
            }
            int knob = 12;
            int kx = tx + (int) (t * tw) - knob / 2;
            if (kx < tx) {
                kx = tx;
            }
            if (kx > tx + tw - knob) {
                kx = tx + tw - knob;
            }
            roundRect(kx, ty + 2 - knob / 2, knob, knob, knob / 2, PRIMARY);
            int bx = x + w - 18 - BOX_W;
            roundRect(bx, y + (h - 26) / 2, BOX_W, 26, 8, CONTAINER);
            drawCenteredString(mc.fontRenderer, label(), bx + BOX_W / 2, y + h / 2 - 4, TEXT);
        }

        private String label() {
            return integer ? Integer.toString((int) Math.round(binding.get()))
                    : String.format(java.util.Locale.ROOT, "%.2f", binding.get());
        }

        @Override
        boolean mouseClicked(int mx, int my) {
            if (in(mx, my) && mx < trackX() + trackW() + 10) {
                dragging = this;
                mouseDragged(mx, my);
                return true;
            }
            if (in(mx, my)) {
                return true;
            }
            return false;
        }

        @Override
        void mouseDragged(int mx, int my) {
            double t = (double) (mx - trackX()) / (double) trackW();
            t = t < 0.0D ? 0.0D : (t > 1.0D ? 1.0D : t);
            binding.set(min + t * (max - min));
        }
    }

    class TextWidget extends Widget {
        private final StringBinding binding;

        TextWidget(StringBinding binding, int x, int y, int w, int h, String label, String desc) {
            super(x, y, w, h);
            this.binding = binding;
            this.label = label;
            this.desc = desc;
        }

        @Override
        void draw(Minecraft mc, int mx, int my) {
            drawCard(mc, mx, my);
            int bw = 200;
            int bx = x + w - 18 - bw;
            roundRect(bx, y + (h - 26) / 2, bw, 26, 8, CONTAINER);
            String value = binding.get();
            if (mc.fontRenderer.getStringWidth(value) > bw - 12) {
                value = mc.fontRenderer.trimStringToWidth(value, bw - 18) + "...";
            }
            drawCenteredString(mc.fontRenderer, value, bx + bw / 2, y + h / 2 - 4, MUTED);
        }
    }

    class SegmentedWidget extends Widget {
        private final String[] values;
        private final String[] labels;
        private final StringBinding binding;

        SegmentedWidget(String[] values, String[] labels, StringBinding binding, int x, int y, int w, int h,
                String label, String desc) {
            super(x, y, w, h);
            this.values = values;
            this.labels = labels;
            this.binding = binding;
            this.label = label;
            this.desc = desc;
        }

        private int totalW() {
            int total = 0;
            for (String s : labels) {
                total += mc.fontRenderer.getStringWidth(s) + 22;
            }
            return total;
        }

        @Override
        void draw(Minecraft mc, int mx, int my) {
            drawCard(mc, mx, my);
            int total = totalW();
            int sx = x + w - 18 - total;
            int sy = y + (h - 28) / 2;
            roundRect(sx, sy, total, 28, 14, CONTAINER);
            String current = binding.get();
            int cx = sx;
            for (int i = 0; i < values.length; i++) {
                int sw = mc.fontRenderer.getStringWidth(labels[i]) + 22;
                boolean selected = values[i].equals(current);
                if (selected) {
                    roundRect(cx + 2, sy + 2, sw - 4, 24, 12, PRIMARY);
                }
                drawCenteredString(mc.fontRenderer, labels[i], cx + sw / 2, sy + 10, selected ? ON_PRIMARY : MUTED);
                cx += sw;
            }
        }

        @Override
        boolean mouseClicked(int mx, int my) {
            if (!in(mx, my)) {
                return false;
            }
            int total = totalW();
            int sx = x + w - 18 - total;
            String current = binding.get();
            int cx = sx;
            for (int i = 0; i < values.length; i++) {
                int sw = mc.fontRenderer.getStringWidth(labels[i]) + 22;
                if (mx >= cx && mx < cx + sw) {
                    binding.set(values[i]);
                    return true;
                }
                cx += sw;
            }
            return true;
        }
    }

    class DropdownWidget extends Widget {
        private static final int CTRL_W = 168;
        private final String[] values;
        private final String[] labels;
        private final StringBinding binding;
        private boolean open;

        DropdownWidget(String[] values, String[] labels, StringBinding binding, int x, int y, int w, int h,
                String label, String desc) {
            super(x, y, w, h);
            this.values = values;
            this.labels = labels;
            this.binding = binding;
            this.label = label;
            this.desc = desc;
        }

        private int boxX() {
            return x + w - 18 - CTRL_W;
        }

        private int boxY() {
            return y + (h - 28) / 2;
        }

        void setOpen(boolean value) {
            open = value;
        }

        @Override
        void draw(Minecraft mc, int mx, int my) {
            drawCard(mc, mx, my);
            int bx = boxX();
            int by = boxY();
            boolean hover = open || (mx >= bx && my >= by && mx < bx + CTRL_W && my < by + 28);
            roundRect(bx, by, CTRL_W, 28, 10, hover ? FIELD_HOVER : CONTAINER);
            String current = binding.get();
            String text = current;
            for (int i = 0; i < values.length; i++) {
                if (values[i].equals(current)) {
                    text = labels[i];
                    break;
                }
            }
            drawString(mc.fontRenderer, text, bx + 12, by + 10, TEXT);
            drawChevron(bx + CTRL_W - 18, by + 12, MUTED);
        }

        @Override
        boolean mouseClicked(int mx, int my) {
            int bx = boxX();
            int by = boxY();
            if (mx >= bx && my >= by && mx < bx + CTRL_W && my < by + 28) {
                open = !open;
                openDropdown = open ? this : null;
                return true;
            }
            return false;
        }

        @Override
        void mouseReleased(int mx, int my) {
            // 下拉不用拖拽
        }

        boolean menuClicked(int mx, int my) {
            int bx = boxX();
            int by = boxY() + 30;
            int itemH = 26;
            for (int i = 0; i < values.length; i++) {
                int iy = by + i * itemH;
                if (mx >= bx && mx < bx + CTRL_W && my >= iy && my < iy + itemH) {
                    binding.set(values[i]);
                    setOpen(false);
                    openDropdown = null;
                    return true;
                }
            }
            return false;
        }

        void drawMenu(Minecraft mc, int mx, int my) {
            if (!open) {
                return;
            }
            int bx = boxX();
            int by = boxY() + 30;
            int itemH = 26;
            int totalH = values.length * itemH;
            roundRect(bx, by, CTRL_W, totalH, 10, FIELD_HOVER);
            String current = binding.get();
            for (int i = 0; i < values.length; i++) {
                int iy = by + i * itemH;
                boolean hover = mx >= bx && mx < bx + CTRL_W && my >= iy && my < iy + itemH;
                if (hover) {
                    roundRect(bx + 2, iy + 1, CTRL_W - 4, itemH - 2, 8, CONTAINER);
                }
                boolean selected = values[i].equals(current);
                drawString(mc.fontRenderer, labels[i], bx + 12, iy + 9, selected ? PRIMARY : TEXT);
            }
        }
    }

    class FontSortRow extends Widget {
        private final int index;
        private final String name;

        FontSortRow(int index, String name, int x, int y, int w, int h) {
            super(x, y, w, h);
            this.index = index;
            this.name = name;
        }

        @Override
        void draw(Minecraft mc, int mx, int my) {
            roundRect(x, y, w, h, 10, in(mx, my) ? FIELD_HOVER : FIELD);
            drawString(mc.fontRenderer, Integer.toString(index + 1), x + 12, y + (h - 8) / 2, MUTED);
            String text = name;
            int maxW = w - 100;
            if (mc.fontRenderer.getStringWidth(text) > maxW) {
                text = mc.fontRenderer.trimStringToWidth(text, maxW - 6) + "...";
            }
            drawString(mc.fontRenderer, text, x + 42, y + (h - 8) / 2, TEXT);
            int bw = 22;
            int up = x + w - 14 - bw * 2 - 4;
            drawArrow(up, y + (h - 20) / 2, bw, 20, "up", index > 0, mx, my);
            drawArrow(up + bw + 4, y + (h - 20) / 2, bw, 20, "down", index < qz.fontSort().size() - 1, mx, my);
        }

        private void drawArrow(int ax, int ay, int aw, int ah, String dir, boolean enabled, int mx, int my) {
            boolean hover = enabled && mx >= ax && my >= ay && mx < ax + aw && my < ay + ah;
            roundRect(ax, ay, aw, ah, 8, hover ? NAV_ACTIVE : CONTAINER);
            int color = enabled ? TEXT : TRACK;
            int cxp = ax + aw / 2;
            int cyp = ay + ah / 2;
            for (int i = 0; i < 4; i++) {
                int yy = "up".equals(dir) ? cyp + 2 - i : cyp - 3 + i;
                Gui.drawRect(cxp - 3 + i, yy, cxp + 4 - i, yy + 1, color);
            }
        }

        @Override
        boolean mouseClicked(int mx, int my) {
            int bw = 22;
            int up = x + w - 14 - bw * 2 - 4;
            int ay = y + (h - 20) / 2;
            if (mx >= up && mx < up + bw && my >= ay && my < ay + 20) {
                if (qz.moveFontSort(index, -1)) {
                    yamlDirty = true;
                    layout();
                }
                return true;
            }
            if (mx >= up + bw + 4 && mx < up + bw * 2 + 4 && my >= ay && my < ay + 20) {
                if (qz.moveFontSort(index, 1)) {
                    yamlDirty = true;
                    layout();
                }
                return true;
            }
            return in(mx, my);
        }
    }

    class ButtonWidget extends Widget {
        private final int id;
        private final String text;
        private final boolean primary;

        ButtonWidget(int id, int x, int y, int w, int h, String text, boolean primary) {
            super(x, y, w, h);
            this.id = id;
            this.text = text;
            this.primary = primary;
        }

        @Override
        void draw(Minecraft mc, int mx, int my) {
            boolean hover = in(mx, my);
            if (primary) {
                roundRect(x, y, w, h, h / 2, hover ? 0xFFE0C8FF : PRIMARY);
                drawCenteredString(mc.fontRenderer, text, x + w / 2, y + (h - 8) / 2 + 1, ON_PRIMARY);
            } else {
                roundRect(x, y, w, h, h / 2, hover ? NAV_ACTIVE : CONTAINER);
                drawCenteredString(mc.fontRenderer, text, x + w / 2, y + (h - 8) / 2 + 1, hover ? ON_ACTIVE : MUTED);
            }
        }

        @Override
        boolean mouseClicked(int mx, int my) {
            if (!in(mx, my)) {
                return false;
            }
            if (id == 997) {
                applyKeep();
            } else if (id == 998) {
                cancelAndClose();
            } else if (id == 999) {
                applyKeep();
                mc.displayGuiScreen(parent);
            }
            return true;
        }
    }

    // ---------- 图标 ----------

    private void drawIcon(String kind, int x, int y, int color) {
        if ("home".equals(kind)) {
            Gui.drawRect(x + 6, y, x + 8, y + 3, color);
            Gui.drawRect(x + 3, y + 3, x + 11, y + 5, color);
            Gui.drawRect(x + 1, y + 5, x + 13, y + 14, color);
            Gui.drawRect(x + 6, y + 9, x + 8, y + 14, 0);
        } else if ("font".equals(kind)) {
            Gui.drawRect(x + 2, y + 12, x + 4, y + 14, color);
            Gui.drawRect(x + 5, y + 4, x + 7, y + 14, color);
            Gui.drawRect(x + 8, y + 8, x + 10, y + 14, color);
            Gui.drawRect(x + 11, y, x + 13, y + 14, color);
            Gui.drawRect(x + 2, y + 3, x + 6, y + 4, color);
            Gui.drawRect(x + 9, y + 11, x + 13, y + 12, color);
        } else {
            Gui.drawRect(x, y + 2, x + 12, y + 3, color);
            Gui.drawRect(x, y + 7, x + 12, y + 8, color);
            Gui.drawRect(x, y + 11, x + 12, y + 12, color);
            Gui.drawRect(x + 3, y, x + 5, y + 5, color);
            Gui.drawRect(x + 7, y + 5, x + 9, y + 10, color);
            Gui.drawRect(x + 2, y + 10, x + 4, y + 14, color);
        }
    }

    private void drawChevron(int x, int y, int color) {
        Gui.drawRect(x, y, x + 6, y + 1, color);
        Gui.drawRect(x + 1, y + 1, x + 5, y + 2, color);
        Gui.drawRect(x + 2, y + 2, x + 4, y + 3, color);
    }

    // ---------- 工具 ----------

    private static double parse(String s, double fallback) {
        try {
            return Double.parseDouble(s);
        } catch (Throwable t) {
            return fallback;
        }
    }

    static void roundRect(int x, int y, int w, int h, int r, int color) {
        if (w <= 0 || h <= 0) {
            return;
        }
        int rr = Math.min(r, Math.min(w / 2, h / 2));
        if (rr <= 0) {
            Gui.drawRect(x, y, x + w, y + h, color);
            return;
        }
        Gui.drawRect(x + rr, y, x + w - rr, y + h, color);
        Gui.drawRect(x, y + rr, x + rr, y + h - rr, color);
        Gui.drawRect(x + w - rr, y + rr, x + w, y + h - rr, color);
        for (int i = 0; i < rr; i++) {
            int dx = (int) Math.round(rr - Math.sqrt((double) (rr * rr - (rr - i) * (rr - i))));
            Gui.drawRect(x + dx, y + i, x + rr, y + i + 1, color);
            Gui.drawRect(x + w - rr, y + i, x + w - dx, y + i + 1, color);
            Gui.drawRect(x + dx, y + h - 1 - i, x + rr, y + h - i, color);
            Gui.drawRect(x + w - rr, y + h - 1 - i, x + w - dx, y + h - i, color);
        }
    }
}
