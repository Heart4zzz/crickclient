package zov.crickclient.ui.collector;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import zov.crickclient.module.list.misc.InvBuilder;
import zov.crickclient.ui.menu.MenuBackgroundRenderer;
import zov.crickclient.ui.menu.MenuColors;
import zov.crickclient.ui.menu.MenuTextField;
import zov.crickclient.util.IMinecraft;
import zov.crickclient.util.chat.ChatUtil;
import zov.crickclient.util.collector.CollectorEntry;
import zov.crickclient.util.collector.CollectorStorage;
import zov.crickclient.util.collector.DescriptionFilter.DescriptionRule;
import zov.crickclient.util.collector.EnchantFilter.EnchantRule;
import zov.crickclient.util.render.helper.HoverUtil;
import zov.crickclient.util.render.math.Animation;
import zov.crickclient.util.render.math.Easing;
import zov.crickclient.util.render.math.Scissor;
import zov.crickclient.util.render.msdf.Fonts;
import zov.crickclient.util.render.providers.ColorProvider;
import zov.crickclient.util.render.renderers.DrawUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class InvBuilderScreen extends Screen implements IMinecraft {
    private static final float PANEL_W = 430f;
    private static final float PANEL_H = 290f;
    private static final float SLOT = 20f;
    private static final float SLOT_GAP = 4f;
    private static final int COLUMNS = 10;
    private static final float FILTER_H = 16f;

    private final Screen parent;
    private final List<CollectorEntry> entries = CollectorStorage.getInstance().getEntries();
    private final Animation openAnim = new Animation(Easing.QUINTIC_OUT, 320);
    private final MenuTextField filterField = new MenuTextField("Поиск предмета...");

    private CollectorEntry selected;
    private CollectorEntry hoveredEntry;
    private float gridScroll;
    private float settingsScroll;

    private float toggleX, toggleY, toggleW, toggleH;
    private float countMinusX, countMinusY, countPlusX, countPlusY, countStepW, countStepH;
    private float searchBtnX, searchBtnY, searchBtnW, searchBtnH;
    private float saveBtnX, saveBtnY, saveBtnW, saveBtnH;
    private final List<RuleHitbox> ruleHitboxes = new ArrayList<>();

    private record RuleHitbox(float x, float y, float w, float h, Runnable action) {
    }

    public InvBuilderScreen(Screen parent) {
        super(Text.literal("Inv Builder"));
        this.parent = parent;
        if (!entries.isEmpty()) {
            selected = entries.getFirst();
        }
    }

    @Override
    protected void init() {
        openAnim.reset(0f);
        filterField.resetAppear();
    }

    private List<CollectorEntry> visibleEntries() {
        String query = filterField.text.trim().toLowerCase(Locale.ROOT);
        if (query.isEmpty()) {
            return entries;
        }
        return entries.stream()
                .filter(entry -> entry.getDisplayName().toLowerCase(Locale.ROOT).contains(query))
                .toList();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        MenuBackgroundRenderer.render(context, width, height);

        openAnim.run(true);
        float open = openAnim.getValue();
        float panelX = width / 2f - PANEL_W / 2f;
        float panelY = height / 2f - PANEL_H / 2f + (1f - open) * 24f;

        DrawUtil.drawRoundBlur(panelX, panelY, PANEL_W, PANEL_H, 10f,
                ColorProvider.rgba(180, 190, 220, (int) (90 * open)), 16f);
        DrawUtil.drawRound(panelX - 0.5f, panelY - 0.5f, PANEL_W + 1f, PANEL_H + 1f, 10.5f,
                MenuColors.withAlpha(MenuColors.BTN_BORDER_HOVER, (int) (130 * open)));
        DrawUtil.drawRound(panelX, panelY, PANEL_W, PANEL_H, 10f,
                MenuColors.withAlpha(MenuColors.PANEL_BG, (int) (235 * open)));

        DrawUtil.drawRound(panelX + 14f, panelY + 14f, 3f, 18f, 1.5f,
                MenuColors.withAlpha(MenuColors.ACCENT, (int) (200 * open)));
        DrawUtil.drawText(Fonts.SFBOLD.get(), "Редактор закупки", panelX + 22f, panelY + 16f,
                MenuColors.withAlpha(MenuColors.ACCENT, (int) (255 * open)), 10f);
        DrawUtil.drawText(Fonts.SFREGULAR.get(), "Вкл/выкл, кол-во, фильтры — как в Delta Collector", panelX + 22f, panelY + 28f,
                MenuColors.withAlpha(MenuColors.ACCENT_MUTED, (int) (170 * open)), 6f);

        float bodyY = panelY + 44f;
        float bodyH = PANEL_H - 52f;
        float gridX = panelX + 12f;
        float gridW = 244f;
        float settingsX = gridX + gridW + 10f;
        float settingsW = PANEL_W - gridW - 34f;

        DrawUtil.drawRound(gridX, bodyY, gridW, bodyH, 7f,
                MenuColors.withAlpha(MenuColors.PANEL_INNER, (int) (210 * open)));
        DrawUtil.drawRound(settingsX, bodyY, settingsW, bodyH, 7f,
                MenuColors.withAlpha(MenuColors.PANEL_INNER, (int) (210 * open)));

        filterField.setBounds(gridX + 8f, bodyY + 8f, gridW - 16f, FILTER_H);
        filterField.render(context, mouseX, mouseY, delta);

        float gridTop = bodyY + 8f + FILTER_H + 6f;
        float gridH = bodyH - 16f - FILTER_H - 6f;
        renderGrid(context, gridX + 8f, gridTop, gridW - 16f, gridH, mouseX, mouseY, open);
        renderSettings(context, settingsX + 8f, bodyY + 8f, settingsW - 16f, bodyH - 16f, mouseX, mouseY, open);
    }

    private void renderGrid(DrawContext context, float x, float y, float w, float h, int mouseX, int mouseY, float open) {
        hoveredEntry = null;
        List<CollectorEntry> visible = visibleEntries();
        int rows = Math.max(1, (visible.size() + COLUMNS - 1) / COLUMNS);
        float contentHeight = rows * (SLOT + SLOT_GAP) - SLOT_GAP;
        float maxScroll = Math.max(0f, contentHeight - h);
        gridScroll = MathHelper.clamp(gridScroll, -maxScroll, 0f);

        Scissor.push();
        Scissor.setFromComponentCoordinates(x, y, w, h);

        for (int i = 0; i < visible.size(); i++) {
            CollectorEntry entry = visible.get(i);
            float slotX = x + (i % COLUMNS) * (SLOT + SLOT_GAP);
            float slotY = y + (i / COLUMNS) * (SLOT + SLOT_GAP) + gridScroll;

            if (slotY + SLOT < y || slotY > y + h) {
                continue;
            }

            boolean hovered = HoverUtil.isHovered(mouseX, mouseY, slotX, slotY, SLOT, SLOT);
            if (hovered) {
                hoveredEntry = entry;
            }

            boolean active = entry.isEnabled();
            boolean picked = entry == selected;
            int bg = picked
                    ? MenuColors.withAlpha(MenuColors.ROW_SELECTED, (int) (220 * open))
                    : active
                    ? MenuColors.withAlpha(MenuColors.ROW_HOVER, (int) ((hovered ? 180 : 120) * open))
                    : MenuColors.withAlpha(MenuColors.ROW_BG, (int) ((hovered ? 150 : 90) * open));

            DrawUtil.drawRound(slotX, slotY, SLOT, SLOT, 4f, bg);
            if (picked) {
                DrawUtil.drawRound(slotX - 0.5f, slotY - 0.5f, SLOT + 1f, SLOT + 1f, 4.5f,
                        MenuColors.withAlpha(MenuColors.ACCENT, (int) (140 * open)));
            }

            context.drawItem(withIconStack(entry), (int) slotX + 2, (int) slotY + 2);
            if (entry.getTargetCount() > 1) {
                String count = String.valueOf(entry.getTargetCount());
                float countW = Fonts.SFREGULAR.get().getWidth(count, 5.5f);
                DrawUtil.drawRound(slotX + SLOT - countW - 3f, slotY + SLOT - 8f, countW + 2f, 7f, 2f,
                        ColorProvider.rgba(0, 0, 0, 140));
                DrawUtil.drawText(Fonts.SFREGULAR.get(), count,
                        slotX + SLOT - countW - 2f,
                        slotY + SLOT - 7.5f,
                        MenuColors.withAlpha(active ? MenuColors.ACCENT : MenuColors.ACCENT_MUTED, (int) (255 * open)), 5.5f);
            }
        }

        Scissor.unset();
        Scissor.pop();
    }

    private void renderSettings(DrawContext context, float x, float y, float w, float h, int mouseX, int mouseY, float open) {
        ruleHitboxes.clear();
        toggleX = toggleY = toggleW = toggleH = 0f;
        countMinusX = countMinusY = countPlusX = countPlusY = countStepW = countStepH = 0f;
        searchBtnX = searchBtnY = searchBtnW = searchBtnH = 0f;
        saveBtnX = saveBtnY = saveBtnW = saveBtnH = 0f;

        saveBtnW = 52f;
        saveBtnH = 14f;
        saveBtnX = x + w - saveBtnW;
        saveBtnY = y;
        drawActionButton(saveBtnX, saveBtnY, saveBtnW, saveBtnH, "Сохранить", mouseX, mouseY, open);

        if (selected == null) {
            DrawUtil.drawText(Fonts.SFREGULAR.get(), "Нет предметов", x, y + 24f,
                    MenuColors.withAlpha(MenuColors.ACCENT_MUTED, (int) (180 * open)), 7f);
            return;
        }

        float rowY = y + settingsScroll + 18f;
        float headerY = rowY;

        toggleW = 14f;
        toggleH = 8.5f;
        toggleX = x + w - toggleW;
        toggleY = headerY + 1f;

        float countBoxW = selected.supportsCountEditor() ? 34f : 0f;
        float countBoxX = toggleX - 8f - countBoxW;
        float controlsLeft = selected.supportsCountEditor() ? countBoxX - 6f : toggleX - 6f;

        context.drawItem(withIconStack(selected), (int) x, (int) headerY);

        searchBtnW = 18f;
        searchBtnH = 18f;
        searchBtnX = x + 22f;
        searchBtnY = headerY + 1f;
        drawActionButton(searchBtnX, searchBtnY, searchBtnW, searchBtnH, "AH", mouseX, mouseY, open);

        float nameX = x + 44f;
        float nameMaxW = Math.max(20f, controlsLeft - nameX);
        drawTruncatedText(selected.getDisplayName(), nameX, headerY + 3f, nameMaxW, 7.5f,
                MenuColors.withAlpha(MenuColors.ACCENT, (int) (255 * open)));

        drawToggle(toggleX, toggleY, toggleW, toggleH, selected.isEnabled(), open);

        float centerY = headerY + 6f;
        if (selected.supportsCountEditor()) {
            countStepW = 16f;
            countStepH = 10f;
            countMinusX = countBoxX;
            countMinusY = centerY - 5f;
            countPlusX = countBoxX + countBoxW - countStepW;
            countPlusY = centerY - 5f;

            DrawUtil.drawRound(countBoxX, centerY - 5f, countBoxW, countStepH, 3f,
                    MenuColors.withAlpha(MenuColors.ROW_BG, (int) (200 * open)));
            drawStepButton(countMinusX, countMinusY, countStepW, countStepH, "-", mouseX, mouseY, open);
            drawStepButton(countPlusX, countPlusY, countStepW, countStepH, "+", mouseX, mouseY, open);
            String countText = String.valueOf(selected.getTargetCount());
            DrawUtil.drawText(Fonts.SFREGULAR.get(), countText,
                    countBoxX + (countBoxW - Fonts.SFREGULAR.get().getWidth(countText, 6.75f)) / 2f,
                    centerY - 3.5f,
                    MenuColors.withAlpha(MenuColors.ACCENT, (int) (255 * open)), 6.75f);
        }

        float rulesY = headerY + 22f;
        float rulesBottom = y + h;
        Scissor.push();
        Scissor.setFromComponentCoordinates(x, rulesY - 4f, w, rulesBottom - rulesY + 4f);

        float ruleY = rulesY;
        float ruleLabelMaxW = w - 52f;
        if (selected.getEnchantFilter() != null) {
            for (EnchantRule rule : selected.getEnchantFilter().getRules()) {
                if (rule.isDeny()) {
                    continue;
                }
                ruleY = renderRuleRow(x, w, ruleY, rule.displayName(), ruleLabelMaxW, rule.isEnabled(), rule.hasLevel(), rule.getLevel(), open, mouseX, mouseY,
                        () -> {
                            rule.setEnabled(!rule.isEnabled());
                            CollectorStorage.getInstance().save();
                        },
                        () -> {
                            rule.changeLevel(-1);
                            CollectorStorage.getInstance().save();
                        },
                        () -> {
                            rule.changeLevel(1);
                            CollectorStorage.getInstance().save();
                        });
            }
        }

        if (selected.getDescriptionFilter() != null) {
            for (DescriptionRule rule : selected.getDescriptionFilter().getRules()) {
                if (rule.isDeny()) {
                    continue;
                }
                ruleY = renderRuleRow(x, w, ruleY, rule.displayName(), ruleLabelMaxW, rule.isEnabled(), rule.hasLevel(), rule.getLevel(), open, mouseX, mouseY,
                        () -> {
                            rule.setEnabled(!rule.isEnabled());
                            CollectorStorage.getInstance().save();
                        },
                        () -> {
                            rule.changeLevel(-1);
                            CollectorStorage.getInstance().save();
                        },
                        () -> {
                            rule.changeLevel(1);
                            CollectorStorage.getInstance().save();
                        });
            }
        }

        Scissor.unset();
        Scissor.pop();
    }

    private void drawActionButton(float x, float y, float w, float h, String label, int mouseX, int mouseY, float open) {
        boolean hovered = HoverUtil.isHovered(mouseX, mouseY, x, y, w, h);
        DrawUtil.drawRound(x, y, w, h, 4f, MenuColors.withAlpha(
                hovered ? MenuColors.BTN_BG_HOVER : MenuColors.BTN_BG, (int) ((hovered ? 220 : 180) * open)));
        DrawUtil.drawText(Fonts.SFREGULAR.get(), label,
                x + (w - Fonts.SFREGULAR.get().getWidth(label, 6.5f)) / 2f,
                y + (h - 6.5f) / 2f,
                MenuColors.withAlpha(MenuColors.ACCENT, (int) (255 * open)), 6.5f);
    }

    private static net.minecraft.item.ItemStack withIconStack(CollectorEntry entry) {
        net.minecraft.item.ItemStack stack = entry.previewStack().copy();
        stack.setCount(1);
        return stack;
    }

    private void drawTruncatedText(String text, float x, float y, float maxWidth, float size, int color) {
        String clipped = truncate(text, maxWidth, size);
        DrawUtil.drawText(Fonts.SFREGULAR.get(), clipped, x, y, color, size);
    }

    private String truncate(String text, float maxWidth, float size) {
        if (Fonts.SFREGULAR.get().getWidth(text, size) <= maxWidth) {
            return text;
        }
        String ellipsis = "...";
        float ellipsisW = Fonts.SFREGULAR.get().getWidth(ellipsis, size);
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            builder.append(text.charAt(i));
            if (Fonts.SFREGULAR.get().getWidth(builder.toString(), size) + ellipsisW > maxWidth) {
                builder.setLength(Math.max(0, builder.length() - 1));
                return builder + ellipsis;
            }
        }
        return text;
    }

    private float renderRuleRow(float x, float w, float y, String label, float labelMaxW, boolean enabled, boolean hasLevel, int level,
                                float open, int mouseX, int mouseY, Runnable toggleAction, Runnable minusAction, Runnable plusAction) {
        float switchW = 13f;
        float switchH = 7.7f;
        float switchX = x + w - switchW;
        float switchY = y + 1.5f;

        drawTruncatedText(label, x, y + 1f, labelMaxW, 6.75f,
                MenuColors.withAlpha(enabled ? MenuColors.ACCENT : MenuColors.ACCENT_MUTED, (int) (255 * open)));

        if (hasLevel) {
            float levelBoxW = 28f;
            float levelBoxX = switchX - 8f - levelBoxW;
            float levelY = y + 0.5f;
            DrawUtil.drawRound(levelBoxX, levelY, levelBoxW, 10f, 3f,
                    MenuColors.withAlpha(MenuColors.ROW_BG, (int) (180 * open)));
            ruleHitboxes.add(new RuleHitbox(levelBoxX, levelY, 14f, 10f, minusAction));
            ruleHitboxes.add(new RuleHitbox(levelBoxX + 14f, levelY, 14f, 10f, plusAction));
            drawStepButton(levelBoxX, levelY, 14f, 10f, "-", mouseX, mouseY, open);
            drawStepButton(levelBoxX + 14f, levelY, 14f, 10f, "+", mouseX, mouseY, open);
            String levelText = String.valueOf(level);
            DrawUtil.drawText(Fonts.SFREGULAR.get(), levelText,
                    levelBoxX + (levelBoxW - Fonts.SFREGULAR.get().getWidth(levelText, 6.5f)) / 2f,
                    levelY + 2f,
                    MenuColors.withAlpha(MenuColors.ACCENT_MUTED, (int) (220 * open)), 6.5f);
        }

        drawToggle(switchX, switchY, switchW, switchH, enabled, open);
        ruleHitboxes.add(new RuleHitbox(switchX, switchY, switchW, switchH, toggleAction));
        return y + 14f;
    }

    private void drawToggle(float x, float y, float w, float h, boolean enabled, float open) {
        DrawUtil.drawRound(x, y, w, h, h / 2f, MenuColors.withAlpha(MenuColors.ACCENT, enabled ? (int) (220 * open) : (int) (70 * open)));
        float knob = h - 3f;
        float knobX = enabled ? x + w - knob - 1.5f : x + 1.5f;
        DrawUtil.drawRound(knobX, y + 1.5f, knob, knob, knob / 2f,
                ColorProvider.rgba(255, 255, 255, (int) (255 * open)));
    }

    private void drawStepButton(float x, float y, float w, float h, String label, int mouseX, int mouseY, float open) {
        boolean hovered = HoverUtil.isHovered(mouseX, mouseY, x, y, w, h);
        if (hovered) {
            DrawUtil.drawRound(x, y, w, h, 3f, MenuColors.withAlpha(MenuColors.BTN_BG_HOVER, (int) (180 * open)));
        }
        DrawUtil.drawText(Fonts.SFREGULAR.get(), label, x + (w - Fonts.SFREGULAR.get().getWidth(label, 7f)) / 2f, y + 1.5f,
                MenuColors.withAlpha(MenuColors.ACCENT_MUTED, (int) (230 * open)), 7f);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) {
            return super.mouseClicked(mouseX, mouseY, button);
        }

        filterField.mouseClicked(mouseX, mouseY, button);
        if (filterField.isFocused()) {
            return true;
        }

        if (HoverUtil.isHovered(mouseX, mouseY, saveBtnX, saveBtnY, saveBtnW, saveBtnH)) {
            CollectorStorage.getInstance().save();
            ChatUtil.send("Список закупки сохранён");
            return true;
        }

        if (hoveredEntry != null) {
            selected = hoveredEntry;
            return true;
        }

        if (selected != null && searchBtnW > 0f && HoverUtil.isHovered(mouseX, mouseY, searchBtnX, searchBtnY, searchBtnW, searchBtnH)) {
            InvBuilder.openAuctionSearch(selected);
            return true;
        }

        if (selected != null && toggleW > 0f && HoverUtil.isHovered(mouseX, mouseY, toggleX, toggleY, toggleW, toggleH)) {
            selected.setEnabled(!selected.isEnabled());
            CollectorStorage.getInstance().save();
            return true;
        }

        if (selected != null && selected.supportsCountEditor() && countStepW > 0f) {
            if (HoverUtil.isHovered(mouseX, mouseY, countMinusX, countMinusY, countStepW, countStepH)) {
                selected.setTargetCount(Math.max(1, selected.getTargetCount() - 1));
                CollectorStorage.getInstance().save();
                return true;
            }
            if (HoverUtil.isHovered(mouseX, mouseY, countPlusX, countPlusY, countStepW, countStepH)) {
                selected.setTargetCount(Math.min(selected.maxPurchaseBatch(), selected.getTargetCount() + 1));
                CollectorStorage.getInstance().save();
                return true;
            }
        }

        for (RuleHitbox hitbox : ruleHitboxes) {
            if (HoverUtil.isHovered(mouseX, mouseY, hitbox.x, hitbox.y, hitbox.w, hitbox.h)) {
                hitbox.action.run();
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        float panelX = width / 2f - PANEL_W / 2f;
        float panelY = height / 2f - PANEL_H / 2f;
        float bodyY = panelY + 44f;
        float bodyH = PANEL_H - 52f;
        float gridX = panelX + 12f;
        float gridW = 244f;
        float settingsX = gridX + gridW + 10f;
        float settingsW = PANEL_W - gridW - 34f;
        float gridTop = bodyY + 8f + FILTER_H + 6f;
        float gridH = bodyH - 16f - FILTER_H - 6f;

        if (HoverUtil.isHovered(mouseX, mouseY, gridX + 8f, gridTop, gridW - 16f, gridH)) {
            gridScroll += (float) (verticalAmount * 12f);
            return true;
        }
        if (HoverUtil.isHovered(mouseX, mouseY, settingsX + 8f, bodyY + 8f, settingsW - 16f, bodyH - 16f)) {
            settingsScroll += (float) (verticalAmount * 10f);
            settingsScroll = Math.min(0f, settingsScroll);
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (filterField.isFocused()) {
            filterField.charTyped(chr, modifiers);
            return true;
        }
        return super.charTyped(chr, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (filterField.isFocused()) {
            filterField.keyPressed(keyCode, scanCode, modifiers);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void close() {
        CollectorStorage.getInstance().save();
        if (parent != null) {
            mc.setScreen(parent);
        } else {
            super.close();
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
