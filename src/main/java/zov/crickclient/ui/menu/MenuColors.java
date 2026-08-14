package zov.crickclient.ui.menu;

import zov.crickclient.util.render.providers.ColorProvider;

/** Shared palette for main menu & alt manager — soft periwinkle, not gray and not neon. */
public final class MenuColors {
    public static final int ACCENT = ColorProvider.rgba(158, 178, 228, 255);
    public static final int ACCENT_GLOW = ColorProvider.rgba(158, 178, 228, 90);
    public static final int ACCENT_SOFT = ColorProvider.rgba(158, 178, 228, 160);
    public static final int ACCENT_MUTED = ColorProvider.rgba(110, 128, 175, 255);

    public static final int BTN_BG = ColorProvider.rgba(22, 28, 50, 210);
    public static final int BTN_BG_HOVER = ColorProvider.rgba(38, 48, 78, 230);
    public static final int BTN_BORDER = ColorProvider.rgba(70, 88, 135, 120);
    public static final int BTN_BORDER_HOVER = ColorProvider.rgba(158, 178, 228, 180);

    public static final int PANEL_BG = ColorProvider.rgba(10, 14, 30, 215);
    public static final int PANEL_INNER = ColorProvider.rgba(8, 12, 26, 190);
    public static final int ROW_BG = ColorProvider.rgba(16, 22, 42, 170);
    public static final int ROW_HOVER = ColorProvider.rgba(26, 34, 58, 210);
    public static final int ROW_SELECTED = ColorProvider.rgba(45, 58, 95, 200);

    private MenuColors() {
    }

    public static int withAlpha(int color, int alpha) {
        return ColorProvider.setAlpha(color, alpha);
    }
}
