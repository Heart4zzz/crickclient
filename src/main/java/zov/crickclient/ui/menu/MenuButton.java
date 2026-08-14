package zov.crickclient.ui.menu;

import net.minecraft.client.gui.DrawContext;
import zov.crickclient.util.render.helper.HoverUtil;
import zov.crickclient.util.render.math.Animation;
import zov.crickclient.util.render.math.Easing;
import zov.crickclient.util.render.msdf.Fonts;
import zov.crickclient.util.render.providers.ColorProvider;
import zov.crickclient.util.render.renderers.DrawUtil;

public class MenuButton {
    private final String label;
    private final Runnable action;
    private float x, y, width, height;
    private final Animation hoverAnim = new Animation(Easing.QUINTIC_OUT, 180);
    private final Animation appearAnim = new Animation(Easing.QUINTIC_OUT, 420);

    public MenuButton(String label, Runnable action) {
        this.label = label;
        this.action = action;
    }

    public void setBounds(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void resetAppear() {
        appearAnim.reset(0f);
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta, float appearDelay) {
        appearAnim.run(true);
        float appear = Math.max(0f, Math.min(1f, appearAnim.getValue() - appearDelay));

        boolean hovered = HoverUtil.isHovered(mouseX, mouseY, x, y, width, height);
        hoverAnim.run(hovered);
        float hover = (float) hoverAnim.getValue();

        float slideY = y + (1f - appear) * 22f;
        float scale = 1f + hover * 0.03f;
        float drawW = width * scale;
        float drawH = height * scale;
        float drawX = x - (drawW - width) / 2f;
        float drawY = slideY - (drawH - height) / 2f;

        DrawUtil.drawRoundBlur(drawX, drawY, drawW, drawH, 7f,
                ColorProvider.rgba(180, 190, 220, (int) (80 * appear)), 12f);

        int border = ColorProvider.interpolateColor(
                MenuColors.withAlpha(MenuColors.BTN_BORDER, (int) (120 * appear)),
                MenuColors.withAlpha(MenuColors.BTN_BORDER_HOVER, (int) (200 * appear)),
                hover);
        DrawUtil.drawRound(drawX - 0.5f, drawY - 0.5f, drawW + 1f, drawH + 1f, 7.5f, border);

        int bg = ColorProvider.interpolateColor(
                MenuColors.withAlpha(MenuColors.BTN_BG, (int) (220 * appear)),
                MenuColors.withAlpha(MenuColors.BTN_BG_HOVER, (int) (240 * appear)),
                hover);
        DrawUtil.drawRound(drawX, drawY, drawW, drawH, 7f, bg);

        if (hover > 0.05f) {
            DrawUtil.drawRound(drawX + 2f, drawY + 2f, drawW - 4f, 1f, 0.5f,
                    MenuColors.withAlpha(MenuColors.ACCENT_SOFT, (int) (140 * appear * hover)));
        }

        float textW = Fonts.SFREGULAR.get().getWidth(label, 8f);
        float textX = drawX + (drawW - textW) / 2f;
        float textY = drawY + drawH / 2f - 4f;
        DrawUtil.drawText(Fonts.SFREGULAR.get(), label, textX, textY,
                MenuColors.withAlpha(MenuColors.ACCENT, (int) (255 * appear)), 8f);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return false;
        if (HoverUtil.isHovered(mouseX, mouseY, x, y, width, height)) {
            action.run();
            return true;
        }
        return false;
    }
}
