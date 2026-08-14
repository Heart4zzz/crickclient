package zov.crickclient.ui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;
import zov.crickclient.ui.component.SearchField;
import zov.crickclient.util.IMinecraft;
import zov.crickclient.util.config.ConfigManager;
import zov.crickclient.util.cursor.CursorManager;
import zov.crickclient.util.render.helper.HoverUtil;
import zov.crickclient.util.render.math.Animation;
import zov.crickclient.util.render.math.Easing;
import zov.crickclient.util.render.math.Scissor;
import zov.crickclient.util.render.msdf.Fonts;
import zov.crickclient.util.render.providers.ColorProvider;
import zov.crickclient.util.render.renderers.DrawUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class ConfigPanel implements IMinecraft {
    private final SearchField nameField = new SearchField("New config name...");
    private final SearchField searchField = new SearchField("Search configs...");
    private final Animation scrollAnim = new Animation(Easing.QUINTIC_OUT, 300);

    private float x, y, w, h;
    private float scroll;
    private float maxScroll;
    private List<String> configs = new ArrayList<>();
    private List<String> filtered = new ArrayList<>();

    public void onOpen() {
        refresh();
        scroll = 0f;
        nameField.clear();
        searchField.clear();
    }

    public void onClose() {
        nameField.clear();
        searchField.clear();
    }

    public void refresh() {
        configs = new ArrayList<>(ConfigManager.getUserConfigs());
        configs.sort(Comparator.comparing(String::toLowerCase));
        rebuildFilter();
    }

    private void rebuildFilter() {
        String query = searchField.text.trim().toLowerCase(Locale.ROOT);
        filtered = new ArrayList<>();
        for (String name : configs) {
            if (query.isEmpty() || name.toLowerCase(Locale.ROOT).contains(query)) {
                filtered.add(name);
            }
        }
    }

    public String getStatusText() {
        int total = configs.size();
        int shown = filtered.size();
        if (total == 0) return "No saved configs — enter a name and press Save";
        if (!searchField.text.trim().isEmpty() && shown != total) {
            return shown + " of " + total + " configs";
        }
        return total + (total == 1 ? " config" : " configs");
    }

    public void setBounds(float x, float y, float w, float h) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta, float alpha) {
        if (configs.isEmpty()) refresh();
        rebuildFilter();

        float toolbarH = 18f;
        float gap = 6f;
        float saveY = y;
        float fieldW = w - 54f;
        nameField.setBounds(x, saveY, fieldW, toolbarH);
        nameField.render(context, mouseX, mouseY, delta, alpha);

        float saveBtnX = x + fieldW + gap;
        renderActionButton(saveBtnX, saveY, 48f, toolbarH, "Save", false, mouseX, mouseY, alpha);

        float searchY = saveY + toolbarH + gap;
        searchField.setBounds(x, searchY, w, toolbarH);
        searchField.render(context, mouseX, mouseY, delta, alpha);

        float listY = searchY + toolbarH + 8f;
        float listH = h - (listY - y);
        scroll = maxScroll > 0 ? MathHelper.clamp(scroll, -maxScroll, 0) : 0;
        scrollAnim.run(scroll);

        if (filtered.isEmpty()) {
            String message = configs.isEmpty()
                    ? "Create your first config above"
                    : "No configs match your search";
            float msgW = Fonts.GUI_BODY.get().getWidth(message, 6.5f);
            DrawUtil.drawText(Fonts.GUI_BODY.get(), message, x + (w - msgW) / 2f, listY + listH / 2f - 8f,
                    ColorProvider.setAlpha(ColorProvider.getColorInactiveText(), (int) (170 * alpha)), 6.5f);
            maxScroll = 0f;
            return;
        }

        float cardW = (w - ClickGuiStyles.MODULE_COLUMN_GAP) / ClickGuiStyles.MODULE_COLUMNS;
        float colGap = ClickGuiStyles.MODULE_COLUMN_GAP;
        float[] colY = {listY + scrollAnim.getValue(), listY + scrollAnim.getValue()};
        int col = 0;

        Scissor.push();
        Scissor.setFromComponentCoordinates(x, listY, w, listH);

        for (String name : filtered) {
            float cx = x + col * (cardW + colGap);
            float cy = colY[col];
            float cardH = 36f;
            renderConfigCard(name, cx, cy, cardW, cardH, mouseX, mouseY, alpha);
            colY[col] += cardH + ClickGuiStyles.MODULE_GAP;
            col = (col + 1) % ClickGuiStyles.MODULE_COLUMNS;
        }

        Scissor.unset();
        Scissor.pop();

        float bottom = Math.max(colY[0], colY[1]);
        maxScroll = Math.max(0, bottom - listY - listH + 4f);
    }

    private void renderConfigCard(String name, float cx, float cy, float cardW, float cardH, int mouseX, int mouseY, float alpha) {
        boolean hover = HoverUtil.isHovered(mouseX, mouseY, cx, cy, cardW, cardH);
        if (hover) CursorManager.requestHand();

        int bg = ColorProvider.rgba(22, 24, 32, (int) (240 * alpha));
        if (hover) {
            bg = ColorProvider.interpolateColor(bg, ColorProvider.rgba(30, 32, 42, (int) (240 * alpha)), 0.5f);
        }

        DrawUtil.drawRound(cx, cy, cardW, cardH, 6f, bg);

        DrawUtil.drawText(Fonts.GUI_TITLE.get(), name, cx + 8f, cy + 6f,
                ColorProvider.setAlpha(ColorProvider.getColorText(), (int) (255 * alpha)), 6.8f);
        DrawUtil.drawText(Fonts.GUI_BODY.get(), "Click Load to apply", cx + 8f, cy + 15f,
                ColorProvider.setAlpha(ColorProvider.getColorInactiveText(), (int) (150 * alpha)), 5.2f);

        float loadW = 36f;
        float delW = 36f;
        float btnH = 10f;
        float btnY = cy + cardH - 13f;
        float loadX = cx + 8f;
        float delX = cx + cardW - delW - 8f;

        renderMiniButton(loadX, btnY, loadW, btnH, "Load", false, mouseX, mouseY, alpha);
        renderMiniButton(delX, btnY, delW, btnH, "Del", true, mouseX, mouseY, alpha);
    }

    private void renderActionButton(float bx, float by, float bw, float bh, String label, boolean accent,
                                    int mouseX, int mouseY, float alpha) {
        boolean hover = HoverUtil.isHovered(mouseX, mouseY, bx, by, bw, bh);
        if (hover) CursorManager.requestHand();
        int bg = accent
                ? ColorProvider.setAlpha(ColorProvider.getColorClient(), (int) ((hover ? 55 : 35) * alpha))
                : ColorProvider.rgba(255, 255, 255, (int) ((hover ? 14 : 8) * alpha));
        DrawUtil.drawRound(bx, by, bw, bh, 4f, bg);
        ClickGuiStyles.drawCenteredButtonText(label, bx, by, bw, bh, alpha, 6.5f, ColorProvider.getColorText());
    }

    private void renderMiniButton(float bx, float by, float bw, float bh, String label, boolean danger,
                                  int mouseX, int mouseY, float alpha) {
        boolean hover = HoverUtil.isHovered(mouseX, mouseY, bx, by, bw, bh);
        if (hover) CursorManager.requestHand();
        int bg = danger
                ? ColorProvider.rgba(255, 80, 80, (int) ((hover ? 35 : 20) * alpha))
                : ColorProvider.setAlpha(ColorProvider.getColorClient(), (int) ((hover ? 45 : 28) * alpha));
        DrawUtil.drawRound(bx, by, bw, bh, 3f, bg);
        int textColor = danger
                ? ColorProvider.rgba(255, 180, 180, (int) (230 * alpha))
                : ColorProvider.setAlpha(ColorProvider.getColorText(), (int) (230 * alpha));
        ClickGuiStyles.drawCenteredButtonText(label, bx, by, bw, bh, alpha, 5.5f, textColor);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return false;
        if (!HoverUtil.isHovered(mouseX, mouseY, x, y, w, h)) return false;

        float toolbarH = 18f;
        float gap = 6f;
        float fieldW = w - 54f;
        float saveBtnX = x + fieldW + gap;

        float searchY = y + toolbarH + gap;
        float listY = searchY + toolbarH + 8f;
        float cardW = (w - ClickGuiStyles.MODULE_COLUMN_GAP) / ClickGuiStyles.MODULE_COLUMNS;
        float colGap = ClickGuiStyles.MODULE_COLUMN_GAP;
        float scrollOff = scrollAnim.getValue();
        float[] colY = {listY + scrollOff, listY + scrollOff};
        int col = 0;

        for (String name : filtered) {
            float cx = x + col * (cardW + colGap);
            float cy = colY[col];
            float cardH = 36f;
            float loadW = 36f;
            float delW = 36f;
            float btnY = cy + cardH - 13f;

            if (HoverUtil.isHovered(mouseX, mouseY, cx + 8f, btnY, loadW, 10f)) {
                ConfigManager.load(name);
                return true;
            }
            if (HoverUtil.isHovered(mouseX, mouseY, cx + cardW - delW - 8f, btnY, delW, 10f)) {
                if (ConfigManager.delete(name)) refresh();
                return true;
            }

            colY[col] += cardH + ClickGuiStyles.MODULE_GAP;
            col = (col + 1) % ClickGuiStyles.MODULE_COLUMNS;
        }

        if (HoverUtil.isHovered(mouseX, mouseY, saveBtnX, y, 48f, toolbarH)) {
            saveNamedConfig();
            return true;
        }

        nameField.mouseClicked(mouseX, mouseY, button);
        searchField.mouseClicked(mouseX, mouseY, button);
        return true;
    }

    private void saveNamedConfig() {
        String name = nameField.text.trim();
        if (name.isEmpty() || name.equalsIgnoreCase("autocfg")) return;
        ConfigManager.save(name);
        ConfigManager.load(name);
        nameField.clear();
        refresh();
    }

    public void mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
        if (HoverUtil.isHovered(mouseX, mouseY, x, y, w, h)) {
            scroll += (float) (vertical * 24f);
            scroll = maxScroll > 0 ? MathHelper.clamp(scroll, -maxScroll, 0) : 0;
        }
    }

    public boolean isAnyFieldFocused() {
        return nameField.isFocused() || searchField.isFocused();
    }

    public void keyPressed(int keyCode, int scanCode, int modifiers) {
        if (nameField.isFocused()) {
            nameField.keyPressed(keyCode, scanCode, modifiers);
        } else if (searchField.isFocused()) {
            searchField.keyPressed(keyCode, scanCode, modifiers);
        }
    }

    public void charTyped(char chr, int modifiers) {
        if (nameField.isFocused()) {
            nameField.charTyped(chr, modifiers);
        } else if (searchField.isFocused()) {
            searchField.charTyped(chr, modifiers);
        }
    }
}
