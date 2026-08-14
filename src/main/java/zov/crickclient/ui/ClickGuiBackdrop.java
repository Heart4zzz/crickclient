package zov.crickclient.ui;

import zov.crickclient.util.render.providers.ColorProvider;
import zov.crickclient.util.render.renderers.DrawUtil;

public final class ClickGuiBackdrop {
    private ClickGuiBackdrop() {
    }

    public static void render(int width, int height, float alpha) {
        DrawUtil.drawRound(0, 0, width, height, 0,
                ColorProvider.rgba(4, 6, 12, (int) (200 * alpha)),
                ColorProvider.rgba(8, 10, 18, (int) (200 * alpha)),
                ColorProvider.rgba(6, 8, 14, (int) (210 * alpha)),
                ColorProvider.rgba(10, 12, 20, (int) (210 * alpha)));

        int accent = ColorProvider.getColorClient();
        float cx = width / 2f;
        float cy = height / 2f;
        DrawUtil.drawRoundBlur(cx - 200f, cy - 140f, 400f, 280f, 100f,
                ColorProvider.setAlpha(accent, (int) (10 * alpha)), 50f);

        DrawUtil.drawRound(0, 0, width, height, 0,
                ColorProvider.rgba(0, 0, 0, (int) (60 * alpha)),
                ColorProvider.rgba(0, 0, 0, (int) (60 * alpha)),
                ColorProvider.rgba(0, 0, 0, (int) (100 * alpha)),
                ColorProvider.rgba(0, 0, 0, (int) (100 * alpha)));
    }
}
