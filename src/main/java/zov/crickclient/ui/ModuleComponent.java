package zov.crickclient.ui;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;
import zov.crickclient.module.Module;
import zov.crickclient.module.settings.*;
import zov.crickclient.ui.component.Component;
import zov.crickclient.ui.component.impl.*;
import zov.crickclient.util.cursor.CursorManager;
import zov.crickclient.util.keyboard.KeyStorage;
import zov.crickclient.util.render.helper.HoverUtil;
import zov.crickclient.util.render.math.Animation;
import zov.crickclient.util.render.math.Easing;
import zov.crickclient.util.render.msdf.Fonts;
import zov.crickclient.util.render.providers.ColorProvider;
import zov.crickclient.util.render.renderers.DrawUtil;

@Getter
public class ModuleComponent extends Component {
    private final Module module;
    private final Panel panel;

    private final Animation animation = new Animation(Easing.QUINTIC_OUT, 320);
    private final Animation hoverAnim = new Animation(Easing.QUINTIC_OUT, 220);
    private final Animation enabledAnim = new Animation(Easing.QUINTIC_OUT, 350);
    private final Animation chevronAnim = new Animation(Easing.QUINTIC_OUT, 240);

    public boolean open;
    private boolean isHovered;
    private boolean binding;

    private final ObjectArrayList<Component> components = new ObjectArrayList<>();

    public ModuleComponent(Module module, Panel panel) {
        this.module = module;
        this.panel = panel;
        for (Setting setting : module.getSettings()) {
            switch (setting) {
                case BooleanSetting option -> components.add(new BooleanComponent(option));
                case ItemModelSetting option ->
                        components.add(new ItemModelComponent(option, panel::openItemModelGallery));
                case ModeSetting option -> components.add(new ModeComponent(option));
                case ModeListSetting option -> components.add(new ModeListComponent(option));
                case SliderSetting option -> components.add(new SliderComponent(option));
                case BindSetting option -> components.add(new BindComponent(option));
                case ThemeSetting option -> components.add(new ThemeComponent(option));
                case ColorSetting option -> components.add(new ColorPickerComponent(option));
                case ActionSetting option -> components.add(new ActionComponent(option));
                default -> {}
            }
        }
    }

    public float getHeaderHeight() {
        return ClickGuiStyles.moduleHeaderHeight(module.getDesc());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float partialTicks) {
        float headerH = getHeaderHeight();
        isHovered = HoverUtil.isHovered(mouseX, mouseY, x, y, width, headerH);

        hoverAnim.run(isHovered);
        animation.run(open);
        enabledAnim.run(module.isEnabled());
        chevronAnim.run(open);

        if (isHovered) CursorManager.requestHand();

        float alpha = Math.max(Math.min(panel.getAnimationAlpha().getValue(), 1), 0);
        float enabled = (float) enabledAnim.getValue();
        float hover = (float) hoverAnim.getValue();

        float currentHeight = headerH + (height - headerH) * (float) animation.getValue();

        float visTop = panel.getContentTop();
        float visBottom = panel.getContentBottom();
        if (y + currentHeight < visTop || y > visBottom) {
            isHovered = false;
            return;
        }

        ClickGuiStyles.drawModuleCard(x, y, width, currentHeight - 0.5f, headerH, alpha, enabled, hover, open);

        if (binding) {
            DrawUtil.drawText(Fonts.GUI_BODY.get(), "Press key...",
                    x + width / 2f - Fonts.GUI_BODY.get().getWidth("Press key...", 6.5f) / 2f,
                    y + headerH / 2f - 3f, ColorProvider.rgba(255, 255, 255, (int) (255 * alpha)), 6.5f);
        } else {
            renderRowContent(alpha, enabled, headerH);
        }

        if (animation.getValue() > 0.01f) {
            renderSettings(context, mouseX, mouseY, partialTicks, alpha, currentHeight, headerH);
        }
    }

    private void renderRowContent(float alpha, float enabled, float headerH) {
        float padX = 8f;
        float titleY = y + 6f;
        float right = x + width - 6f;

        float toggleW = ClickGuiStyles.TOGGLE_W;
        float toggleH = ClickGuiStyles.TOGGLE_H;
        float toggleX = right - toggleW;
        float toggleY = y + 7f;
        right = toggleX - 3f;

        if (!components.isEmpty()) right -= 8f;
        if (module.getKey() != -1) {
            right -= Fonts.GUI_BODY.get().getWidth(KeyStorage.getKey(module.getKey()), 5.5f) + 10f;
        }

        float maxTextW = Math.max(36f, right - (x + padX));
        ClickGuiStyles.drawModuleTitle(module.getName(), x + padX, titleY, maxTextW, alpha, enabled);

        String desc = module.getDesc();
        if (desc != null && !desc.isEmpty()) {
            float descMaxW = width - padX * 2f;
            ClickGuiStyles.drawModuleDesc(desc, x + padX, titleY + 9f, descMaxW, alpha);
        }

        ClickGuiStyles.drawToggle(toggleX, toggleY, alpha, enabled);
        right = toggleX - 3f;

        if (!components.isEmpty()) {
            String chevron = open ? "^" : "v";
            float chevronW = Fonts.GUI_BODY.get().getWidth(chevron, 6f);
            DrawUtil.drawText(Fonts.GUI_BODY.get(), chevron, right - chevronW, titleY + 1f,
                    ColorProvider.setAlpha(ColorProvider.getColorInactiveText(), (int) (180 * alpha)), 6f);
            right -= chevronW + 3f;
        }

        if (module.getKey() != -1) {
            String key = KeyStorage.getKey(module.getKey());
            float keyW = Fonts.GUI_BODY.get().getWidth(key, 5.5f);
            float keyBoxW = keyW + 8f;
            float keyX = right - keyBoxW;
            DrawUtil.drawRound(keyX, y + 8f, keyBoxW, 10f, 3f, ColorProvider.rgba(255, 255, 255, (int) (10 * alpha)));
            DrawUtil.drawText(Fonts.GUI_BODY.get(), key, keyX + 4f, y + 9.5f,
                    ColorProvider.setAlpha(ColorProvider.getColorInactiveText(), (int) (190 * alpha)), 5.5f);
        }
    }

    private void renderSettings(DrawContext context, int mouseX, int mouseY, float partialTicks,
                                float alpha, float currentHeight, float headerH) {
        float compY = y + headerH + 3f;
        float panelTop = panel.getContentTop();
        float panelBottom = panel.getContentBottom();
        float intersectY = Math.max(y + headerH, panelTop);
        float intersectBottom = Math.min(y + currentHeight, panelBottom);
        float intersectHeight = Math.max(0, intersectBottom - intersectY);

        DrawUtil.drawRound(x + 8f, y + headerH, width - 16f, 0.5f, 0.25f,
                ColorProvider.setAlpha(ColorProvider.getColorClient(), (int) (30 * alpha * animation.getValue())));

        for (Component component : components) {
            component.getAlphaAnim().setValue(Math.min(panel.getAnimationAlpha().getValue(), 1) * (float) animation.getValue());
            component.getAlphaAnimSetting().run(component.isVisible());

            float visibleProgress = MathHelper.clamp(component.getAlphaAnimSetting().getValue(), 0f, 1f);
            if (component.isVisible() || visibleProgress > 0) {
                component.setX(x + 2f);
                component.setY(compY);
                component.setWidth(width - 4f);

                zov.crickclient.util.render.math.Scissor.push();
                zov.crickclient.util.render.math.Scissor.setFromComponentCoordinates(x, intersectY, width, intersectHeight);
                component.render(context, mouseX, mouseY, partialTicks);
                zov.crickclient.util.render.math.Scissor.unset();
                zov.crickclient.util.render.math.Scissor.pop();

                compY += component.getHeight() * visibleProgress;
            }
        }
    }

    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (isHovered(mouseX, mouseY, getHeaderHeight())) {
            if (button == 0) module.setEnabled(!module.isEnabled());
            if (button == 1 && !components.isEmpty()) open = !open;
            if (button == 2) binding = !binding;
        }

        if (open) {
            for (Component component : components) {
                if (component.isVisible() && component.getAlphaAnimSetting().getValue() > 0.5f) {
                    component.mouseClicked(mouseX, mouseY, button);
                }
            }
        }
    }

    public void mouseReleased(double mouseX, double mouseY, int button) {
        if (open) {
            for (Component component : components) {
                component.mouseReleased(mouseX, mouseY, button);
            }
        }
    }

    public void keyPressed(int keyCode, int scanCode, int modifiers) {
        if (binding) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_DELETE) {
                module.setKey(-1);
            } else {
                module.setKey(keyCode);
            }
            binding = false;
        }

        if (open) {
            for (Component component : components) {
                component.keyPressed(keyCode, scanCode, modifiers);
            }
        }
    }

    public boolean isBinding() {
        return binding;
    }

    private boolean isHovered(double mouseX, double mouseY, float heightCheck) {
        return HoverUtil.isHovered(mouseX, mouseY, x, y, width, heightCheck);
    }
}
