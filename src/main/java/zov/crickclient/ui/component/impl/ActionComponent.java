package zov.crickclient.ui.component.impl;

import net.minecraft.client.util.math.MatrixStack;
import zov.crickclient.module.settings.ActionSetting;
import zov.crickclient.ui.component.Component;
import zov.crickclient.util.cursor.CursorManager;
import zov.crickclient.util.render.helper.HoverUtil;
import zov.crickclient.util.render.msdf.Fonts;
import zov.crickclient.util.render.providers.ColorProvider;
import zov.crickclient.util.render.renderers.DrawUtil;

public class ActionComponent extends Component {
    private final ActionSetting setting;

    public ActionComponent(ActionSetting setting) {
        this.setting = setting;
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        float alpha = Math.max(Math.min(getAlphaAnimSetting().getValue(), 1), 0) * Math.max(Math.min(getAlphaAnim().getValue(), 1), 0);
        int alphaInt = (int) (255 * alpha);
        boolean hovered = HoverUtil.isHovered(mouseX, mouseY, x + 4f, y + 2f, width - 8f, 12f);
        if (hovered) {
            CursorManager.requestHand();
        }

        DrawUtil.drawRound(x + 4f, y + 2f, width - 8f, 12f, 3f,
                ColorProvider.rgba(255, 255, 255, hovered ? (int) (24 * alpha) : (int) (12 * alpha)));
        DrawUtil.drawText(Fonts.SFREGULAR.get(), setting.getName(), x + width / 2f - Fonts.SFREGULAR.get().getWidth(setting.getName(), 6.5f) / 2f,
                y + 4.5f, ColorProvider.setAlpha(ColorProvider.getColorText(), alphaInt), 6.5f);
        setHeight(16);
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && HoverUtil.isHovered(mouseX, mouseY, x + 4f, y + 2f, width - 8f, 12f)) {
            setting.run();
        }
    }

    @Override
    public boolean isVisible() {
        return setting.visible.get();
    }
}
