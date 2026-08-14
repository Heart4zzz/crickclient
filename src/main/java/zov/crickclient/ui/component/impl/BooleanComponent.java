package zov.crickclient.ui.component.impl;

import net.minecraft.client.util.math.MatrixStack;
import org.lwjgl.glfw.GLFW;
import zov.crickclient.module.settings.BooleanSetting;
import zov.crickclient.ui.component.Component;
import zov.crickclient.ui.ClickGuiStyles;
import zov.crickclient.util.cursor.CursorManager;
import zov.crickclient.util.render.helper.HoverUtil;
import zov.crickclient.util.render.msdf.Fonts;
import zov.crickclient.util.render.providers.ColorProvider;
import zov.crickclient.util.render.renderers.DrawUtil;

public class BooleanComponent extends Component {
    private final BooleanSetting setting;
    private boolean binding;

    public BooleanComponent(BooleanSetting setting) {
        this.setting = setting;
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        float alpha = Math.max(Math.min(getAlphaAnimSetting().getValue(), 1), 0) * Math.max(Math.min(getAlphaAnim().getValue(), 1), 0);
        int alphaInt = (int) (255 * alpha);

        float toggleW = 18f;
        float toggleH = 9f;
        float toggleX = x + width - toggleW - 2.5f;
        float toggleY = y + 3.5f;

        if (HoverUtil.isHovered(mouseX, mouseY, toggleX, toggleY, toggleW, toggleH)) {
            CursorManager.requestHand();
        }

        DrawUtil.drawText(Fonts.SFREGULAR.get(), binding ? "Binding..." : setting.getName(),
                x + 4.5f, y + 4.5f, ColorProvider.setAlpha(ColorProvider.getColorText(), alphaInt), 7f, 0.4f, 1f, width - toggleW - 8f);

        float anim = (float) setting.getAnimation().getValue();
        ClickGuiStyles.drawToggle(toggleX, toggleY, alpha, anim);

        setHeight(16);
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        float toggleW = 18f;
        float toggleH = 9f;
        float toggleX = x + width - toggleW - 2.5f;
        float toggleY = y + 3.5f;

        if (HoverUtil.isHovered(mouseX, mouseY, toggleX, toggleY, toggleW, toggleH)) {
            if (button == 0) setting.setValue(!setting.getValue());
            if (button == 2 && binding) {
                binding = false;
                return;
            }
            if (binding) {
                setting.setKey(button);
                binding = false;
            }
            if (button == 2) binding = true;
        }
    }

    @Override
    public void keyPressed(int keyCode, int scanCode, int modifiers) {
        if (binding) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                binding = false;
                return;
            }
            if (keyCode == GLFW.GLFW_KEY_DELETE) {
                setting.setKey(-1);
                return;
            }
            setting.setKey(keyCode);
            binding = false;
        }
    }

    @Override
    public boolean isVisible() {
        return setting.visible.get();
    }
}
