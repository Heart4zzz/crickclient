package zov.crickclient.ui;

import lombok.Getter;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;
import org.joml.Vector4f;
import zov.crickclient.module.ModuleCategory;
import zov.crickclient.ui.component.SearchField;
import zov.crickclient.util.IMinecraft;
import zov.crickclient.util.cursor.CursorManager;
import zov.crickclient.util.render.helper.HoverUtil;
import zov.crickclient.util.render.math.Animation;
import zov.crickclient.util.render.math.Easing;
import zov.crickclient.util.render.msdf.Fonts;
import zov.crickclient.util.render.providers.ColorProvider;
import zov.crickclient.util.render.renderers.DrawUtil;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Getter
public class ClickGuiShell implements IMinecraft {
    private static final float ROW_H = 19f;
    private static final float SECTION_GAP = 3f;
    private static final float HEADER_H = 10f;

    private record SidebarEntry(String icon, String label, ModuleCategory category) {
    }

    private record SidebarSection(String title, SidebarEntry[] entries) {
    }

    private static final SidebarSection[] MODULE_SECTIONS = {
            new SidebarSection("Combat", new SidebarEntry[]{
                    new SidebarEntry("a", "Combat", ModuleCategory.COMBAT),
                    new SidebarEntry("b", "Movement", ModuleCategory.MOVEMENT),
            }),
            new SidebarSection("Visuals", new SidebarEntry[]{
                    new SidebarEntry("c", "Render", ModuleCategory.RENDER),
            }),
            new SidebarSection("Player", new SidebarEntry[]{
                    new SidebarEntry("d", "Player", ModuleCategory.PLAYER),
            }),
            new SidebarSection("Other", new SidebarEntry[]{
                    new SidebarEntry("e", "Misc", ModuleCategory.MISC),
            }),
    };

    private final ClickGuiFrame parent;
    private final Map<ModuleCategory, Panel> panels = new EnumMap<>(ModuleCategory.class);
    private final SearchField searchField;
    private final ConfigPanel configPanel = new ConfigPanel();
    private final Animation openAnim = new Animation(Easing.QUINTIC_OUT, 360);
    private final Animation categoryAnim = new Animation(Easing.QUINTIC_OUT, 240);

    private ModuleCategory selectedCategory = ModuleCategory.COMBAT;
    private ModuleCategory displayedCategory = ModuleCategory.COMBAT;
    private boolean configView;

    public float x, y, width, height;
    private float contentX, contentY, contentW, contentH;
    private float configsItemY;

    public ClickGuiShell(ClickGuiFrame parent, SearchField searchField) {
        this.parent = parent;
        this.searchField = searchField;
        for (ModuleCategory category : ModuleCategory.values()) {
            panels.put(category, new Panel(category, parent));
        }
    }

    public void resetOpenAnimation() {
        openAnim.reset(0f);
        categoryAnim.reset(1f);
    }

    public Panel getActivePanel() {
        return panels.get(displayedCategory);
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta, float alpha) {
        float open = MathHelper.clamp(openAnim.getValue(), 0f, 1f);
        if (open < 0.01f) return;

        if (!configView) {
            categoryAnim.run(selectedCategory == displayedCategory ? 1f : 0f);
            if (categoryAnim.getValue() < 0.02f && selectedCategory != displayedCategory) {
                displayedCategory = selectedCategory;
                getActivePanel().setScroll(0f);
                categoryAnim.reset(0f);
            }
        }

        float drawY = y + (1f - open) * 18f;
        float contentAlpha = open * (configView ? 1f : Math.max((float) categoryAnim.getValue(), 0.2f));

        contentX = x + ClickGuiStyles.SIDEBAR_WIDTH + 10f;
        contentY = drawY + ClickGuiStyles.CONTENT_HEADER + 2f;
        contentW = width - ClickGuiStyles.SIDEBAR_WIDTH - 20f;
        contentH = height - ClickGuiStyles.CONTENT_HEADER - ClickGuiStyles.FOOTER_HEIGHT - 4f;

        ClickGuiStyles.drawShellWindow(x, drawY, width, height, open);
        renderSidebar(drawY, mouseX, mouseY, open);
        renderContentHeader(drawY, open);
        if (configView) {
            configPanel.setBounds(contentX, contentY, contentW, contentH);
            configPanel.render(context, mouseX, mouseY, delta, contentAlpha);
        } else {
            renderModuleArea(context, mouseX, mouseY, delta, contentAlpha);
            renderFooterSearch(context, drawY, mouseX, mouseY, delta, open);
        }
    }

    private void renderSidebar(float drawY, int mouseX, int mouseY, float alpha) {
        float sx = x;
        float sy = drawY;
        float sw = ClickGuiStyles.SIDEBAR_WIDTH;

        DrawUtil.drawRound(sx, sy, sw, height, ClickGuiStyles.SIDEBAR_CORNERS,
                ColorProvider.rgba(12, 14, 20, (int) (255 * alpha)));

        float profileY = sy + 10f;
        float avatarSize = 18f;
        float avatarX = sx + 10f;
        float textBlockH = 15f;
        float avatarY = profileY + (textBlockH - avatarSize) / 2f + 0.5f;

        String username = mc.getSession().getUsername();
        String initial = username == null || username.isEmpty()
                ? "?"
                : String.valueOf(Character.toUpperCase(username.charAt(0)));

        DrawUtil.drawRound(avatarX, avatarY, avatarSize, avatarSize, avatarSize / 2f,
                ColorProvider.setAlpha(ColorProvider.getColorClient(), (int) (70 * alpha)));
        ClickGuiStyles.drawCenteredGuiTitle(initial, avatarX, avatarY, avatarSize, avatarSize, alpha, 7.5f);

        String user = username == null ? "" : username;
        if (user.length() > 9) user = user.substring(0, 8) + "…";
        ClickGuiStyles.drawGuiBody(user, sx + 32f, profileY + 1f, alpha, 6.5f);
        ClickGuiStyles.drawGuiBody("CrickClient", sx + 32f, profileY + 9.5f, alpha * 0.7f, 5.5f);

        DrawUtil.drawRound(sx + 8f, sy + 34f, sw - 16f, 0.5f, 0.25f,
                ColorProvider.rgba(255, 255, 255, (int) (10 * alpha)));

        float itemY = sy + 42f;
        for (SidebarSection section : MODULE_SECTIONS) {
            renderSectionHeader(sx, sw, itemY, section.title(), alpha);
            itemY += HEADER_H;
            for (SidebarEntry entry : section.entries()) {
                boolean selected = !configView && entry.category() == selectedCategory;
                renderSidebarItem(sx, sw, itemY, entry.icon(), entry.label(), selected,
                        hover(mouseX, mouseY, sx, itemY, sw), alpha);
                itemY += ROW_H;
            }
            itemY += SECTION_GAP;
        }

        itemY += 2f;
        DrawUtil.drawRound(sx + 8f, itemY, sw - 16f, 0.5f, 0.25f,
                ColorProvider.rgba(255, 255, 255, (int) (8 * alpha)));
        itemY += 6f;

        renderSectionHeader(sx, sw, itemY, "Settings", alpha);
        itemY += HEADER_H;
        configsItemY = itemY;
        renderSidebarItem(sx, sw, itemY, "f", "Configs", configView,
                hover(mouseX, mouseY, sx, itemY, sw), alpha);
    }

    private void renderSectionHeader(float sx, float sw, float y, String title, float alpha) {
        ClickGuiStyles.drawGuiBody(title, sx + 12f, y + 1f, alpha * 0.45f, 5f);
    }

    private void renderSidebarItem(float sx, float sw, float itemY, String icon, String label, boolean selected, boolean hover, float alpha) {
        if (hover) CursorManager.requestHand();
        if (selected) {
            DrawUtil.drawRound(sx + 6f, itemY, sw - 12f, ROW_H, 5f, ColorProvider.rgba(255, 255, 255, (int) (10 * alpha)));
            DrawUtil.drawRound(sx + 6f, itemY, 2f, ROW_H, 1f, ColorProvider.setAlpha(ColorProvider.getColorClient(), (int) (230 * alpha)));
        } else if (hover) {
            DrawUtil.drawRound(sx + 6f, itemY, sw - 12f, ROW_H, 5f, ColorProvider.rgba(255, 255, 255, (int) (5 * alpha)));
        }

        float iconSize = 8f;
        float textSize = 6.5f;
        float iconY = itemY + (ROW_H - iconSize) / 2f + 1f;
        float textY = itemY + (ROW_H - textSize) / 2f + 0.5f;

        int iconColor = selected
                ? ColorProvider.setAlpha(ColorProvider.getColorClient(), (int) (255 * alpha))
                : ColorProvider.setAlpha(ColorProvider.getColorIcons(), (int) (160 * alpha));
        DrawUtil.drawText(Fonts.ICONS_MINCED.get(), icon, sx + 12f, iconY, iconColor, iconSize);

        int textColor = selected
                ? ColorProvider.setAlpha(ColorProvider.getColorText(), (int) (255 * alpha))
                : ColorProvider.setAlpha(ColorProvider.getColorInactiveText(), (int) (200 * alpha));
        DrawUtil.drawText(Fonts.GUI_BODY.get(), label, sx + 24f, textY, textColor, textSize);
    }

    private boolean hover(int mouseX, int mouseY, float sx, float itemY, float sw) {
        return HoverUtil.isHovered(mouseX, mouseY, sx + 6f, itemY, sw - 12f, ROW_H);
    }

    private void renderContentHeader(float drawY, float alpha) {
        String title = configView ? "Configs"
                : (searchField.isEmpty() ? formatCategory(selectedCategory) : "Search");
        ClickGuiStyles.drawGuiTitle(title, contentX, drawY + 8f, alpha, 9f);

        if (configView) {
            String subtitle = configPanel.getStatusText();
            ClickGuiStyles.drawGuiBody(subtitle, contentX, drawY + 18f, alpha * 0.65f, 5.5f);
        } else if (!searchField.isEmpty()) {
            ClickGuiStyles.drawGuiBody("Filtered modules", contentX, drawY + 18f, alpha * 0.65f, 5.5f);
        } else {
            String section = sectionTitleFor(selectedCategory);
            ClickGuiStyles.drawGuiBody(section + " / " + title, contentX, drawY + 18f, alpha * 0.65f, 5.5f);
        }
    }

    private static String sectionTitleFor(ModuleCategory category) {
        return switch (category) {
            case COMBAT, MOVEMENT -> "Combat";
            case RENDER -> "Visuals";
            case PLAYER -> "Player";
            case MISC -> "Other";
        };
    }

    private void renderModuleArea(DrawContext context, int mouseX, int mouseY, float delta, float alpha) {
        Panel panel = getActivePanel();
        panel.setContentBounds(contentX, contentY, contentW, contentH);
        panel.setAlphaMultiplier(alpha);

        if (searchField.isEmpty()) {
            panel.render(context, mouseX, mouseY, delta);
            return;
        }

        List<ModuleComponent> all = new ArrayList<>();
        for (Panel p : panels.values()) {
            for (ModuleComponent c : p.getModuleComponents()) {
                if (!parent.searchCheck(c.getModule().getName())) all.add(c);
            }
        }
        panel.renderComponents(context, mouseX, mouseY, delta, all);
    }

    private void renderFooterSearch(DrawContext context, float drawY, int mouseX, int mouseY, float delta, float alpha) {
        float searchW = Math.min(180f, contentW - 8f);
        float searchH = 18f;
        float searchX = contentX + contentW / 2f - searchW / 2f;
        float searchY = drawY + height - ClickGuiStyles.FOOTER_HEIGHT + 1f;
        searchField.setBounds(searchX, searchY, searchW, searchH);
        searchField.render(context, mouseX, mouseY, delta, alpha);
    }

    public void layout(int windowWidth, int windowHeight) {
        layout(windowWidth, windowHeight, 0f, 0f);
    }

    public void layout(int windowWidth, int windowHeight, float offsetX, float offsetY) {
        width = ClickGuiStyles.SHELL_WIDTH;
        height = ClickGuiStyles.SHELL_HEIGHT;
        x = (windowWidth - width) / 2f + offsetX;
        y = (windowHeight - height) / 2f + offsetY;
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!HoverUtil.isHovered(mouseX, mouseY, x, y, width, height)) return false;
        float drawY = y + (1f - MathHelper.clamp(openAnim.getValue(), 0f, 1f)) * 18f;
        if (handleSidebarClick(mouseX, mouseY, drawY, button)) return true;

        if (configView) {
            configPanel.mouseClicked(mouseX, mouseY, button);
            return true;
        }

        searchField.mouseClicked(mouseX, mouseY, button);
        if (searchField.isEmpty()) {
            if (HoverUtil.isHovered(mouseX, mouseY, contentX, contentY, contentW, contentH)) {
                getActivePanel().mouseClicked(mouseX, mouseY, button);
            }
        } else if (HoverUtil.isHovered(mouseX, mouseY, contentX, contentY, contentW, contentH)) {
            for (Panel p : panels.values()) {
                for (ModuleComponent component : p.getModuleComponents()) {
                    if (!parent.searchCheck(component.getModule().getName())) {
                        component.mouseClicked(mouseX, mouseY, button);
                    }
                }
            }
        }
        return true;
    }

    private boolean handleSidebarClick(double mouseX, double mouseY, float drawY, int button) {
        if (button != 0) return false;
        float sx = x;
        float sw = ClickGuiStyles.SIDEBAR_WIDTH;
        float itemY = drawY + 42f;

        for (SidebarSection section : MODULE_SECTIONS) {
            itemY += HEADER_H;
            for (SidebarEntry entry : section.entries()) {
                if (HoverUtil.isHovered(mouseX, mouseY, sx + 6f, itemY, sw - 12f, ROW_H)) {
                    if (configView) configPanel.onClose();
                    configView = false;
                    if (selectedCategory != entry.category()) {
                        selectedCategory = entry.category();
                        categoryAnim.run(0f);
                    }
                    return true;
                }
                itemY += ROW_H;
            }
            itemY += SECTION_GAP;
        }

        if (HoverUtil.isHovered(mouseX, mouseY, sx + 6f, configsItemY, sw - 12f, ROW_H)) {
            if (!configView) configPanel.onOpen();
            configView = true;
            return true;
        }
        return HoverUtil.isHovered(mouseX, mouseY, x, drawY, sw, height);
    }

    public void mouseReleased(double mouseX, double mouseY, int button) {
        if (configView) return;
        if (searchField.isEmpty()) {
            getActivePanel().mouseReleased(mouseX, mouseY, button);
            return;
        }
        for (Panel panel : panels.values()) {
            panel.mouseReleased(mouseX, mouseY, button);
        }
    }

    public void mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (configView) {
            configPanel.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
            return;
        }
        float scrollH = contentH + ClickGuiStyles.FOOTER_HEIGHT + 6f;
        if (HoverUtil.isHovered(mouseX, mouseY, contentX, contentY, contentW, scrollH)) {
            getActivePanel().mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
        }
    }

    public void keyPressed(int keyCode, int scanCode, int modifiers) {
        if (configView) {
            configPanel.keyPressed(keyCode, scanCode, modifiers);
            return;
        }
        if (searchField.isEmpty()) {
            getActivePanel().keyPressed(keyCode, scanCode, modifiers);
            return;
        }
        for (Panel panel : panels.values()) {
            panel.keyPressed(keyCode, scanCode, modifiers);
        }
    }

    public void charTyped(char chr, int modifiers) {
        if (configView) {
            configPanel.charTyped(chr, modifiers);
        }
    }

    public boolean isConfigFieldFocused() {
        return configView && configPanel.isAnyFieldFocused();
    }

    public boolean isBindingAnyModule() {
        for (Panel panel : panels.values()) {
            for (ModuleComponent component : panel.getModuleComponents()) {
                if (component.isBinding()) return true;
            }
        }
        return false;
    }

    public ModuleComponent findHoveredModule(int mouseX, int mouseY) {
        return null;
    }

    public float getAnchorRight() {
        return x + width;
    }

    public float getAnchorY() {
        return y;
    }

    public float getAnchorHeight() {
        return height;
    }

    private static String formatCategory(ModuleCategory category) {
        String name = category.name();
        return name.charAt(0) + name.substring(1).toLowerCase();
    }
}
