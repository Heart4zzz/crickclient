package zov.crickclient.ui;

import org.joml.Vector4f;
import zov.crickclient.util.render.msdf.Fonts;
import zov.crickclient.util.render.msdf.MsdfFont;
import zov.crickclient.util.render.providers.ColorProvider;
import zov.crickclient.util.render.renderers.DrawUtil;

public final class ClickGuiStyles {
    public static final float SHELL_WIDTH = 520f;
    public static final float SHELL_HEIGHT = 268f;
    public static final float SHELL_RADIUS = 8f;
    public static final float SIDEBAR_WIDTH = 112f;
    public static final float CONTENT_HEADER = 28f;
    public static final float FOOTER_HEIGHT = 28f;

    public static final int MODULE_COLUMNS = 2;
    public static final float MODULE_COLUMN_GAP = 6f;
    public static final float MODULE_RADIUS = 7f;
    public static final float MODULE_GAP = 6f;

    public static final float MODULE_HEADER_NO_DESC = 30f;
    public static final float MODULE_HEADER_WITH_DESC = 42f;

    public static final float TOGGLE_W = 22f;
    public static final float TOGGLE_H = 11f;

    public static float moduleHeaderHeight(String desc) {
        return desc != null && !desc.isEmpty() ? MODULE_HEADER_WITH_DESC : MODULE_HEADER_NO_DESC;
    }

    public static float moduleCardWidth(float contentW) {
        return (contentW - MODULE_COLUMN_GAP) / MODULE_COLUMNS;
    }

    private ClickGuiStyles() {
    }

    public static void drawBackdrop(int width, int height, float alpha) {
        ClickGuiBackdrop.render(width, height, alpha);
    }

    /** Shader radius order: top-left, top-right, bottom-right, bottom-left (see common.glsl). */
    public static final Vector4f SHELL_CORNERS = new Vector4f(SHELL_RADIUS, SHELL_RADIUS, SHELL_RADIUS, SHELL_RADIUS);
    public static final Vector4f SIDEBAR_CORNERS = new Vector4f(0f, 0f, SHELL_RADIUS, SHELL_RADIUS);

    public static void drawShellWindow(float x, float y, float w, float h, float alpha) {
        DrawUtil.drawRoundBlur(x - 2f, y - 2f, w + 4f, h + 4f, SHELL_CORNERS,
                ColorProvider.rgba(0, 0, 0, (int) (70 * alpha)), 16f);
        DrawUtil.drawRound(x, y, w, h, SHELL_CORNERS, ColorProvider.rgba(16, 18, 26, (int) (248 * alpha)));
        DrawUtil.drawRound(x + SIDEBAR_WIDTH, y + 6f, 0.5f, h - 12f, 0.25f,
                ColorProvider.rgba(255, 255, 255, (int) (8 * alpha)));
    }

    public static void drawCenteredGuiTitle(String text, float boxX, float boxY, float boxW, float boxH, float alpha, float size) {
        MsdfFont font = Fonts.GUI_TITLE.get();
        float tw = font.getWidth(text, size);
        float ty = boxY + (boxH - size) / 2f - 1f;
        DrawUtil.drawText(font, text, boxX + (boxW - tw) / 2f, ty,
                ColorProvider.setAlpha(ColorProvider.getColorText(), (int) (255 * alpha)), size);
    }

    public static void drawCenteredButtonText(String text, float boxX, float boxY, float boxW, float boxH,
                                              float alpha, float size, int color) {
        MsdfFont font = Fonts.GUI_BODY.get();
        float tw = font.getWidth(text, size);
        float ty = boxY + (boxH - size) / 2f - 2f;
        DrawUtil.drawText(font, text, boxX + (boxW - tw) / 2f, ty,
                ColorProvider.setAlpha(color, (int) (255 * alpha)), size);
    }

    public static void drawScrollFade(float x, float y, float w, float h, float headerH, float alpha) {
        DrawUtil.drawRound(x, y, w, 10f, 0,
                ColorProvider.rgba(16, 18, 26, (int) (100 * alpha)),
                ColorProvider.rgba(16, 18, 26, (int) (100 * alpha)),
                ColorProvider.rgba(16, 18, 26, 0),
                ColorProvider.rgba(16, 18, 26, 0));
        DrawUtil.drawRound(x, y + h - 12f, w, 12f, 0,
                ColorProvider.rgba(16, 18, 26, 0),
                ColorProvider.rgba(16, 18, 26, 0),
                ColorProvider.rgba(16, 18, 26, (int) (120 * alpha)),
                ColorProvider.rgba(16, 18, 26, (int) (120 * alpha)));
    }

    public static void drawScrollbar(float trackX, float trackY, float trackH, float thumbY, float thumbH, float alpha) {
        DrawUtil.drawRound(trackX, trackY, 1.5f, trackH, 0.75f, ColorProvider.rgba(255, 255, 255, (int) (8 * alpha)));
        DrawUtil.drawRound(trackX - 0.5f, thumbY, 2.5f, thumbH, 1.25f,
                ColorProvider.setAlpha(ColorProvider.getColorClient(), (int) (130 * alpha)));
    }

    public static void drawModuleCard(float x, float y, float w, float h, float headerH, float alpha, float enabled, float hover, boolean open) {
        int accent = ColorProvider.getColorClient();
        int bg = ColorProvider.rgba(22, 24, 32, (int) (240 * alpha));
        if (hover > 0.01f) {
            bg = ColorProvider.interpolateColor(bg, ColorProvider.rgba(28, 30, 40, (int) (240 * alpha)), hover * 0.55f);
        }
        DrawUtil.drawRound(x, y, w, h, MODULE_RADIUS, bg);

        if (enabled > 0.05f) {
            DrawUtil.drawRound(x, y, 2f, Math.min(h, headerH), 1f,
                    ColorProvider.setAlpha(accent, (int) ((90 + enabled * 165) * alpha)));
        }

        if (open && h > headerH + 2f) {
            DrawUtil.drawRound(x + 1f, y + headerH, w - 2f, h - headerH, 0,
                    ColorProvider.rgba(0, 0, 0, (int) (22 * alpha)));
        }
    }

    public static void drawActiveBar(float x, float y, float rowH, float alpha, float enabled) {
    }

    public static void drawToggle(float x, float y, float alpha, float enabled) {
        int accent = ColorProvider.getColorClient();
        int off = ColorProvider.rgba(42, 44, 54, (int) (255 * alpha));
        int on = ColorProvider.interpolateColor(off, ColorProvider.setAlpha(accent, (int) (255 * alpha)), enabled);
        DrawUtil.drawRound(x, y, TOGGLE_W, TOGGLE_H, TOGGLE_H / 2f, on);
        float knob = TOGGLE_H - 2.6f;
        float knobX = x + 1.3f + (TOGGLE_W - knob - 2.6f) * enabled;
        DrawUtil.drawRound(knobX, y + 1.3f, knob, knob, knob / 2f,
                ColorProvider.rgba(255, 255, 255, (int) (255 * alpha)));
    }

    public static void drawModuleTitle(String name, float x, float y, float maxW, float alpha, float enabled) {
        int text = ColorProvider.interpolateColor(
                ColorProvider.setAlpha(ColorProvider.getColorInactiveText(), (int) (255 * alpha)),
                ColorProvider.setAlpha(ColorProvider.getColorText(), (int) (255 * alpha)),
                enabled);
        DrawUtil.drawText(Fonts.GUI_TITLE.get(), name, x, y, text, 7.2f, 0.4f, 1f, maxW);
    }

    public static void drawModuleDesc(String desc, float x, float y, float maxW, float alpha) {
        if (desc == null || desc.isEmpty()) return;
        DrawUtil.drawText(Fonts.GUI_BODY.get(), desc, x, y,
                ColorProvider.setAlpha(ColorProvider.getColorInactiveText(), (int) (175 * alpha)),
                5.8f);
    }

    public static void drawGuiTitle(String text, float x, float y, float alpha, float size) {
        DrawUtil.drawText(Fonts.GUI_TITLE.get(), text, x, y,
                ColorProvider.setAlpha(ColorProvider.getColorText(), (int) (255 * alpha)), size);
    }

    public static void drawGuiBody(String text, float x, float y, float alpha, float size) {
        DrawUtil.drawText(Fonts.GUI_BODY.get(), text, x, y,
                ColorProvider.setAlpha(ColorProvider.getColorInactiveText(), (int) (220 * alpha)), size);
    }

    public static void drawTooltip(float x, float y, float w, float h, float alpha) {
        DrawUtil.drawRound(x, y, w, h, 5f, ColorProvider.rgba(24, 26, 34, (int) (245 * alpha)));
    }
}
