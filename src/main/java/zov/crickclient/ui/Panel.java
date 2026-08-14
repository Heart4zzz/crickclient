package zov.crickclient.ui;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;
import zov.crickclient.CrickClient;
import zov.crickclient.module.ModuleCategory;
import zov.crickclient.module.settings.ItemModelSetting;
import zov.crickclient.ui.component.Component;
import zov.crickclient.util.IMinecraft;
import zov.crickclient.util.render.helper.HoverUtil;
import zov.crickclient.util.render.math.Animation;
import zov.crickclient.util.render.math.Easing;
import zov.crickclient.util.render.math.Scissor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Getter
@Setter
public class Panel implements IMinecraft {
    public final ModuleCategory category;
    public List<ModuleComponent> moduleComponents = new ArrayList<>();

    private float contentX, contentY, contentW, contentH;
    private float alphaMultiplier = 1f;

    private Animation scrollAnim = new Animation(Easing.QUINTIC_OUT, 350);
    private Animation animationAlpha = new Animation(Easing.BOUNCE_OUT, 350);
    private final Animation scrollbarAnim = new Animation(Easing.CUBIC_IN_OUT, 220);

    float scroll;
    float maxScroll;

    private final ClickGuiFrame parent;

    public Animation getScrollAnim() {
        return scrollAnim;
    }

    public float getScroll() {
        return scroll;
    }

    public void setMaxScroll(float maxScroll) {
        this.maxScroll = maxScroll;
        clampScroll();
    }

    public Panel(ModuleCategory category, ClickGuiFrame parent) {
        this.category = category;
        this.parent = parent;
        CrickClient.getInstance().getModuleStorage().getModules().stream()
                .filter(m -> m.getCategory() == this.category)
                .sorted(Comparator.comparing(m -> m.getName().toLowerCase()))
                .forEach(m -> moduleComponents.add(new ModuleComponent(m, this)));
    }

    public void setContentBounds(float x, float y, float w, float h) {
        this.contentX = x;
        this.contentY = y;
        this.contentW = w;
        this.contentH = h;
    }

    public float getContentTop() {
        return contentY;
    }

    public float getContentBottom() {
        return contentY + contentH;
    }

    public Animation getAnimationAlpha() {
        return animationAlpha;
    }

    public void clampScroll() {
        scroll = maxScroll > 0 ? MathHelper.clamp(scroll, -maxScroll, 0) : 0;
    }

    public void setScroll(float scroll) {
        this.scroll = scroll;
        clampScroll();
    }

    public float computeComponentHeight(ModuleComponent component) {
        float headerH = ClickGuiStyles.moduleHeaderHeight(component.getModule().getDesc());
        float extraHeight = 0;
        if (component.getAnimation().getValue() > 0.01f) {
            extraHeight = 5f;
            for (Component comp : component.getComponents()) {
                float visibleProgress = MathHelper.clamp(comp.getAlphaAnimSetting().getValue(), 0f, 1f);
                if (comp.isVisible() || visibleProgress > 0f) {
                    extraHeight += comp.getHeight() * visibleProgress;
                }
            }
            extraHeight += 6f;
        }
        return headerH + extraHeight * (float) component.getAnimation().getValue();
    }

    public void layoutGrid(List<ModuleComponent> components) {
        float cardW = ClickGuiStyles.moduleCardWidth(contentW);
        float colGap = ClickGuiStyles.MODULE_COLUMN_GAP;
        float scrollOff = scrollAnim.getValue();
        float[] colY = {contentY + 2f + scrollOff, contentY + 2f + scrollOff};
        float[] naturalColY = {contentY + 2f, contentY + 2f};
        int col = 0;

        for (ModuleComponent component : components) {
            if (parent.searchCheck(component.getModule().getName())) continue;

            float cardX = contentX + col * (cardW + colGap);
            float cardH = computeComponentHeight(component);
            component.setX(cardX);
            component.setY(colY[col]);
            component.setWidth(cardW);
            component.setHeight(cardH);

            colY[col] += cardH + ClickGuiStyles.MODULE_GAP;
            naturalColY[col] += cardH + ClickGuiStyles.MODULE_GAP;
            col = (col + 1) % ClickGuiStyles.MODULE_COLUMNS;
        }

        float naturalBottom = Math.max(naturalColY[0], naturalColY[1]);
        maxScroll = Math.max(0, naturalBottom - (contentY + contentH) + 56f);
    }

    public void render(DrawContext context, int mouseX, int mouseY, float partialTicks) {
        animationAlpha.setValue(alphaMultiplier);
        float alphaRatio = alphaMultiplier;

        scrollAnim.run(scroll);
        layoutGrid(moduleComponents);
        clampScroll();

        Scissor.push();
        Scissor.setFromComponentCoordinates(contentX, contentY, contentW, contentH);

        for (ModuleComponent component : moduleComponents) {
            if (parent.searchCheck(component.getModule().getName())) continue;
            Scissor.setFromComponentCoordinates(contentX, contentY, contentW, contentH);
            component.render(context, mouseX, mouseY, partialTicks);
        }

        scrollbarAnim.run(maxScroll > 0f);
        Scissor.unset();
        Scissor.pop();

        if (maxScroll > 0.5f) {
            float trackY = contentY + 4f;
            float trackH = contentH - 8f;
            float scrollRatio = -scroll / maxScroll;
            float thumbH = Math.max(24f, trackH * (trackH / (trackH + maxScroll)));
            float thumbY = trackY + (trackH - thumbH) * scrollRatio;
            ClickGuiStyles.drawScrollbar(contentX + contentW - 4f, trackY, trackH, thumbY, thumbH, alphaRatio);
        }

        ClickGuiStyles.drawScrollFade(contentX, contentY, contentW, contentH, 0f, alphaRatio);
    }

    public void renderComponents(DrawContext context, int mouseX, int mouseY, float partialTicks, List<ModuleComponent> components) {
        animationAlpha.setValue(alphaMultiplier);
        scrollAnim.run(scroll);
        layoutGrid(components);
        clampScroll();

        Scissor.push();
        Scissor.setFromComponentCoordinates(contentX, contentY, contentW, contentH);
        for (ModuleComponent component : components) {
            if (parent.searchCheck(component.getModule().getName())) continue;
            Scissor.setFromComponentCoordinates(contentX, contentY, contentW, contentH);
            component.render(context, mouseX, mouseY, partialTicks);
        }
        Scissor.unset();
        Scissor.pop();
    }

    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (HoverUtil.isHovered(mouseX, mouseY, contentX, contentY, contentW, contentH)) {
            for (ModuleComponent moduleComponent : moduleComponents) {
                if (!parent.searchCheck(moduleComponent.getModule().getName())) {
                    moduleComponent.mouseClicked(mouseX, mouseY, button);
                }
            }
        }
    }

    public void mouseReleased(double mouseX, double mouseY, int button) {
        for (ModuleComponent moduleComponent : moduleComponents) {
            if (!parent.searchCheck(moduleComponent.getModule().getName())) {
                moduleComponent.mouseReleased(mouseX, mouseY, button);
            }
        }
    }

    public void mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (HoverUtil.isHovered(mouseX, mouseY, contentX, contentY, contentW, contentH)) {
            scroll += (float) (verticalAmount * 28f);
            clampScroll();
        }
    }

    public void keyPressed(int keyCode, int scanCode, int modifiers) {
        for (ModuleComponent moduleComponent : moduleComponents) {
            moduleComponent.keyPressed(keyCode, scanCode, modifiers);
        }
    }

    public void openItemModelGallery(ItemModelSetting setting) {
        parent.openItemModelGallery(setting);
    }
}
