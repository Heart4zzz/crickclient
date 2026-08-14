package zov.crickclient.ui;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.gui.DrawContext;
import zov.crickclient.module.settings.impl.Theme;
import zov.crickclient.module.settings.impl.ThemeManager;
import zov.crickclient.util.IMinecraft;
import zov.crickclient.util.cursor.CursorManager;
import zov.crickclient.util.render.helper.HoverUtil;
import zov.crickclient.util.render.math.Animation;
import zov.crickclient.util.render.math.Easing;
import zov.crickclient.util.render.math.Scissor;
import zov.crickclient.util.render.msdf.Fonts;
import zov.crickclient.util.render.providers.ColorProvider;
import zov.crickclient.util.render.renderers.DrawUtil;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class ThemeEditor implements IMinecraft {

    public static final float COLLAPSED_W = 24f;
    public static final float POPUP_W = 168f;
    private static final float POPUP_H = 262f;
    private static final float SHELL_SHIFT_X = -(POPUP_W + COLLAPSED_W + 28f) * 0.42f;
    private static final float SHELL_SHIFT_Y = -14f;
    private static final float PRESET_SIZE = 11f;
    private static final float PRESET_GAP = 3f;
    private static final float PRESET_BLOCK_H = 34f;
    private static final float COLORS_TOP = 22f + PRESET_BLOCK_H + 8f;

    private static final String[] SLOTS = {
            "Accent", "Secondary", "Icons", "Text", "Muted", "Header",
            "Cards", "Slider", "Toggle", "Line", "HUD", "GUI"
    };

    private static final float ROW_H = 14f;
    private static final float BTN_H = 12f;

    private static final int[] DEFAULTS = ThemePresets.BLUE;

    private int selectedPreset = 0;

    private static final float SV_SIZE = 52f;
    private static final float HUE_W = 6f;
    private static final float HUE_GAP = 4f;
    private static final float PICKER_PAD = 4f;
    private static final float PICKER_W = SV_SIZE + HUE_GAP + HUE_W;

    private static final File DIR = new File("crickclient");
    private static final File FILE = new File(DIR, "theme.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Animation expandAnim = new Animation(Easing.QUINTIC_OUT, 280);
    private final Animation contentAnim = new Animation(Easing.QUINTIC_OUT, 220);
    private final Animation pickerAnim = new Animation(Easing.QUINTIC_OUT, 220);

    private boolean expanded;
    private int editingSlot = -1;
    private final float[] hsv = new float[3];
    private boolean draggingSV, draggingH;
    private float pickerX, pickerY;

    private final int[] colors = new int[SLOTS.length];
    private float anchorRight, anchorY, anchorH;
    private float tabX, tabY, popupX, popupY;
    private float shellVisibility = 1f;

    public ThemeEditor() {
        Theme t = ThemeManager.getInstance().getCurrentTheme();
        colors[0] = t.colorClient;
        colors[1] = t.color2;
        colors[2] = t.colorIcons;
        colors[3] = t.colorText;
        colors[4] = t.colorInactiveText;
        colors[5] = t.colorHeaderBg;
        colors[6] = t.colorField;
        colors[7] = t.colorSlider;
        colors[8] = t.colorInactiveButton;
        colors[9] = t.colorSeparator;
        colors[10] = t.colorInterfaceBg;
        colors[11] = t.colorClickGui;
        load();
        syncPresetFromColors();
        applyColors();
    }

    private void syncPresetFromColors() {
        selectedPreset = 0;
        for (int i = 0; i < ThemePresets.ALL.length; i++) {
            if (colorsMatch(colors, ThemePresets.ALL[i].colors())) {
                selectedPreset = i;
                return;
            }
        }
        selectedPreset = -1;
    }

    private static boolean colorsMatch(int[] a, int[] b) {
        for (int i = 0; i < Math.min(a.length, b.length); i++) {
            if (a[i] != b[i]) return false;
        }
        return true;
    }

    private void applyPreset(int index) {
        if (index < 0 || index >= ThemePresets.ALL.length) return;
        System.arraycopy(ThemePresets.ALL[index].colors(), 0, colors, 0, colors.length);
        selectedPreset = index;
        editingSlot = -1;
        applyColors();
        save();
    }

    public void resetAppear() {
    }

    public boolean isExpanded() {
        return expanded;
    }

    public float getShellShiftX() {
        expandAnim.run(expanded);
        return (float) expandAnim.getValue() * SHELL_SHIFT_X;
    }

    public float getShellShiftY() {
        expandAnim.run(expanded);
        return (float) expandAnim.getValue() * SHELL_SHIFT_Y;
    }

    public void setAnchor(float shellRight, float shellY, float shellHeight) {
        this.anchorRight = shellRight;
        this.anchorY = shellY;
        this.anchorH = shellHeight;
    }

    private void applyColors() {
        Theme t = ThemeManager.getInstance().getCurrentTheme();
        t.setAccent(colors[0]);
        t.color2 = colors[1];
        t.colorIcons = colors[2];
        t.colorText = colors[3];
        t.colorInactiveText = colors[4];
        t.colorHeaderBg = colors[5];
        t.colorField = colors[6];
        t.colorSlider = colors[7];
        t.colorInactiveButton = colors[8];
        t.colorInactiveIndicator = colors[8];
        t.colorSeparator = colors[9];
        t.colorInterfaceBg = colors[10];
        t.colorClickGui = colors[11];
        t.colorHeaderText = colors[0];
        t.colorButton = colors[0];
        t.colorIndicator = colors[0];
        t.colorVisualModules = colors[0];
        t.colorWindowBg = colors[6];
        t.colorInactiveField = colors[8];
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta, float visibility) {
        shellVisibility = Math.max(0f, Math.min(visibility, 1f));
        if (shellVisibility < 0.01f) return;

        expandAnim.run(expanded);
        contentAnim.run(expanded);
        float ep = (float) expandAnim.getValue();
        float cp = (float) contentAnim.getValue();

        tabX = anchorRight + 6f;
        tabY = anchorY + anchorH / 2f - 28f;
        popupX = tabX + COLLAPSED_W + 6f;
        popupY = anchorY + anchorH - POPUP_H;

        renderTab(mouseX, mouseY);

        if (cp > 0.01f) {
            renderPopup(mouseX, mouseY, cp);
        }

        if (draggingSV) {
            hsv[1] = clamp01((mouseX - pickerX) / SV_SIZE);
            hsv[2] = 1f - clamp01((mouseY - pickerY) / SV_SIZE);
            applyHSV();
        } else if (draggingH) {
            hsv[0] = clamp01((float) (mouseY - pickerY) / SV_SIZE);
            applyHSV();
        }

        pickerAnim.run(editingSlot >= 0 && expanded);
        if (pickerAnim.getValue() > 0.01f) {
            renderPicker((float) pickerAnim.getValue());
        }
    }

    private void renderTab(int mouseX, int mouseY) {
        boolean hover = HoverUtil.isHovered(mouseX, mouseY, tabX, tabY, COLLAPSED_W, 56f);
        if (hover) CursorManager.requestHand();

        DrawUtil.drawRound(tabX, tabY, COLLAPSED_W, 56f, 6f,
                ColorProvider.rgba(16, 18, 26, (int) (245 * shellVisibility)));
        DrawUtil.drawRound(tabX + 7f, tabY + 10f, 10f, 10f, 3f, ColorProvider.setAlpha(colors[0], (int) (255 * shellVisibility)));
        DrawUtil.drawRound(tabX + 7f, tabY + 24f, 10f, 10f, 3f, ColorProvider.setAlpha(colors[1], (int) (255 * shellVisibility)));
        DrawUtil.drawText(Fonts.GUI_BODY.get(), expanded ? "<" : ">",
                tabX + 8f, tabY + 40f,
                ColorProvider.setAlpha(ColorProvider.getColorInactiveText(), (int) (200 * shellVisibility)), 7f);
    }

    private void renderPopup(int mouseX, int mouseY, float alpha) {
        float combined = alpha * shellVisibility;
        int a = (int) (255 * combined);
        float slide = (1f - alpha) * 12f;

        DrawUtil.drawRound(popupX + slide, popupY, POPUP_W, POPUP_H, 7f,
                ColorProvider.rgba(16, 18, 26, (int) (245 * combined)));

        DrawUtil.drawText(Fonts.GUI_TITLE.get(), "Themes", popupX + slide + 8f, popupY + 7f,
                ColorProvider.setAlpha(ColorProvider.getColorText(), a), 7.5f);

        renderPresetGrid(popupX + slide + 6f, popupY + 22f, POPUP_W - 12f, mouseX, mouseY, combined);

        DrawUtil.drawRound(popupX + slide + 8f, popupY + 22f + PRESET_BLOCK_H + 2f, POPUP_W - 16f, 0.5f, 0.25f,
                ColorProvider.rgba(255, 255, 255, (int) (10 * combined)));
        DrawUtil.drawText(Fonts.GUI_BODY.get(), "Custom", popupX + slide + 8f, popupY + COLORS_TOP - 10f,
                ColorProvider.setAlpha(ColorProvider.getColorInactiveText(), (int) (200 * combined)), 5.5f);

        Scissor.push();
        Scissor.setFromComponentCoordinates(popupX + slide, popupY + COLORS_TOP, POPUP_W, POPUP_H - COLORS_TOP - BTN_H - 10f);

        float rowY = popupY + COLORS_TOP + 2f;
        for (int i = 0; i < SLOTS.length; i++) {
            renderRow(i, popupX + slide + 6f, rowY, POPUP_W - 12f, mouseX, mouseY, combined);
            rowY += ROW_H;
        }

        Scissor.unset();
        Scissor.pop();

        float btnY = popupY + POPUP_H - BTN_H - 6f;
        boolean btnHover = HoverUtil.isHovered(mouseX, mouseY, popupX + slide + 6f, btnY, POPUP_W - 12f, BTN_H);
        if (btnHover) CursorManager.requestHand();
        DrawUtil.drawRound(popupX + slide + 6f, btnY, POPUP_W - 12f, BTN_H, 3f,
                ColorProvider.setAlpha(ColorProvider.getColorClient(), (int) ((btnHover ? 40 : 25) * combined)));
        ClickGuiStyles.drawCenteredButtonText("Reset", popupX + slide + 6f, btnY, POPUP_W - 12f, BTN_H,
                combined, 6.5f, ColorProvider.getColorText());
    }

    private void renderPresetGrid(float rx, float ry, float rw, int mouseX, int mouseY, float alpha) {
        DrawUtil.drawText(Fonts.GUI_BODY.get(), "Presets", rx + 2f, ry,
                ColorProvider.setAlpha(ColorProvider.getColorInactiveText(), (int) (200 * alpha)), 5.5f);
        float startY = ry + 9f;
        float x = rx;
        float y = startY;

        for (int i = 0; i < ThemePresets.ALL.length; i++) {
            ThemePresets.Preset preset = ThemePresets.ALL[i];
            boolean selected = i == selectedPreset;
            boolean hover = HoverUtil.isHovered(mouseX, mouseY, x, y, PRESET_SIZE, PRESET_SIZE + 8f);
            if (hover) CursorManager.requestHand();

            if (selected) {
                DrawUtil.drawRound(x - 1f, y - 1f, PRESET_SIZE + 2f, PRESET_SIZE + 2f, 3.5f,
                        ColorProvider.setAlpha(preset.colors()[0], (int) (120 * alpha)));
            } else if (hover) {
                DrawUtil.drawRound(x - 0.5f, y - 0.5f, PRESET_SIZE + 1f, PRESET_SIZE + 1f, 3f,
                        ColorProvider.rgba(255, 255, 255, (int) (12 * alpha)));
            }

            DrawUtil.drawRound(x, y, PRESET_SIZE, PRESET_SIZE, 2.5f,
                    ColorProvider.setAlpha(preset.colors()[0], (int) (255 * alpha)));

            float nameW = Fonts.GUI_BODY.get().getWidth(preset.name(), 4.8f);
            DrawUtil.drawText(Fonts.GUI_BODY.get(), preset.name(), x + (PRESET_SIZE - nameW) / 2f, y + PRESET_SIZE + 1f,
                    ColorProvider.setAlpha(selected ? ColorProvider.getColorText() : ColorProvider.getColorInactiveText(),
                            (int) (255 * alpha)), 4.8f);

            x += PRESET_SIZE + PRESET_GAP + 7f;
        }
    }

    private void renderRow(int slot, float rx, float ry, float rw, int mouseX, int mouseY, float alpha) {
        boolean hov = HoverUtil.isHovered(mouseX, mouseY, rx, ry, rw, ROW_H);
        boolean active = editingSlot == slot;
        if ((hov || active) && alpha > 0.9f) CursorManager.requestHand();

        if (hov || active) {
            DrawUtil.drawRound(rx, ry, rw, ROW_H - 1f, 2.5f,
                    ColorProvider.rgba(255, 255, 255, (int) (8 * alpha)));
        }

        DrawUtil.drawText(Fonts.GUI_BODY.get(), SLOTS[slot], rx + 2f, ry + 2.5f,
                ColorProvider.setAlpha(active ? ColorProvider.getColorText() : ColorProvider.getColorInactiveText(), (int) (255 * alpha)), 6f);

        float sw = 9f;
        float sx = rx + rw - sw - 1f;
        DrawUtil.drawRound(sx, ry + 2f, sw, sw, 2f, ColorProvider.setAlpha(colors[slot], (int) (255 * alpha)));
    }

    private void renderPicker(float anim) {
        float combined = anim * shellVisibility;
        int a = (int) (255 * combined);
        float px = pickerX, py = pickerY;
        DrawUtil.drawRound(px - PICKER_PAD, py - PICKER_PAD, PICKER_W + PICKER_PAD * 2f, SV_SIZE + PICKER_PAD * 2f, 5f,
                ColorProvider.rgba(16, 18, 26, (int) (245 * combined)));

        int cHue = ColorProvider.setAlpha(Color.HSBtoRGB(hsv[0], 1f, 1f), a);
        DrawUtil.drawRound(px, py, SV_SIZE, SV_SIZE, 2f, cHue);
        DrawUtil.drawRound(px, py, SV_SIZE, SV_SIZE, 2f,
                ColorProvider.rgba(255, 255, 255, a), ColorProvider.rgba(255, 255, 255, a),
                ColorProvider.rgba(255, 255, 255, 0), ColorProvider.rgba(255, 255, 255, 0));
        DrawUtil.drawRound(px, py, SV_SIZE, SV_SIZE, 2f,
                ColorProvider.rgba(0, 0, 0, 0), ColorProvider.rgba(0, 0, 0, a),
                ColorProvider.rgba(0, 0, 0, a), ColorProvider.rgba(0, 0, 0, 0));

        float scx = px + hsv[1] * SV_SIZE;
        float scy = py + (1f - hsv[2]) * SV_SIZE;
        DrawUtil.drawRound(scx - 2f, scy - 2f, 4f, 4f, 2f, ColorProvider.rgba(255, 255, 255, a));

        float hueX = px + SV_SIZE + HUE_GAP;
        for (float i = 0; i <= SV_SIZE; i += 0.5f) {
            DrawUtil.drawRound(hueX, py + i, HUE_W, 1f, 0f,
                    ColorProvider.setAlpha(Color.HSBtoRGB(i / SV_SIZE, 1f, 1f), a));
        }
        float hcy = py + hsv[0] * SV_SIZE;
        DrawUtil.drawRound(hueX - 1f, hcy - 1f, HUE_W + 2f, 2f, 1f, ColorProvider.rgba(255, 255, 255, a));
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (HoverUtil.isHovered(mouseX, mouseY, tabX, tabY, COLLAPSED_W, 56f) && button == 0) {
            expanded = !expanded;
            if (!expanded) editingSlot = -1;
            return true;
        }

        if (!expanded) return false;

        if (editingSlot >= 0 && pickerAnim.getValue() > 0.5f) {
            if (HoverUtil.isHovered(mouseX, mouseY, pickerX, pickerY, SV_SIZE, SV_SIZE) && button == 0) {
                draggingSV = true;
                applyHSVFromMouse(mouseX, mouseY);
                return true;
            }
            if (HoverUtil.isHovered(mouseX, mouseY, pickerX + SV_SIZE + HUE_GAP - 1f, pickerY, HUE_W + 2f, SV_SIZE) && button == 0) {
                draggingH = true;
                hsv[0] = clamp01((float) (mouseY - pickerY) / SV_SIZE);
                applyHSV();
                return true;
            }
            if (HoverUtil.isHovered(mouseX, mouseY, pickerX - PICKER_PAD, pickerY - PICKER_PAD,
                    PICKER_W + PICKER_PAD * 2f, SV_SIZE + PICKER_PAD * 2f)) {
                return true;
            }
        }

        float slide = 0f;
        float btnY = popupY + POPUP_H - BTN_H - 6f;
        if (HoverUtil.isHovered(mouseX, mouseY, popupX + slide + 6f, btnY, POPUP_W - 12f, BTN_H) && button == 0) {
            resetToDefault();
            return true;
        }

        if (handlePresetClick(mouseX, mouseY, popupX + slide + 6f, popupY + 31f, button)) {
            return true;
        }

        float rowY = popupY + COLORS_TOP + 2f;
        for (int i = 0; i < SLOTS.length; i++) {
            if (HoverUtil.isHovered(mouseX, mouseY, popupX + slide + 6f, rowY, POPUP_W - 12f, ROW_H) && button == 0) {
                editingSlot = (editingSlot == i) ? -1 : i;
                if (editingSlot >= 0) {
                    loadHSV(i);
                    pickerX = popupX - PICKER_W - 8f;
                    pickerY = rowY + ROW_H / 2f - SV_SIZE / 2f;
                }
                return true;
            }
            rowY += ROW_H;
        }

        if (HoverUtil.isHovered(mouseX, mouseY, popupX, popupY, POPUP_W, POPUP_H)) return true;
        if (!HoverUtil.isHovered(mouseX, mouseY, tabX, tabY, COLLAPSED_W, 56f)) {
            expanded = false;
            editingSlot = -1;
        }
        return expanded;
    }

    private void applyHSVFromMouse(double mouseX, double mouseY) {
        hsv[1] = clamp01((float) (mouseX - pickerX) / SV_SIZE);
        hsv[2] = 1f - clamp01((float) (mouseY - pickerY) / SV_SIZE);
        applyHSV();
    }

    public void mouseReleased(double mouseX, double mouseY, int button) {
        if (draggingSV || draggingH) save();
        draggingSV = false;
        draggingH = false;
    }

    private boolean handlePresetClick(double mouseX, double mouseY, float rx, float ry, int button) {
        if (button != 0) return false;
        float x = rx;
        float y = ry;
        for (int i = 0; i < ThemePresets.ALL.length; i++) {
            if (HoverUtil.isHovered(mouseX, mouseY, x, y, PRESET_SIZE, PRESET_SIZE + 8f)) {
                applyPreset(i);
                return true;
            }
            x += PRESET_SIZE + PRESET_GAP + 7f;
        }
        return false;
    }

    private void resetToDefault() {
        applyPreset(0);
    }

    public static void applyStartupTheme() {
        int[] c = DEFAULTS.clone();
        if (FILE.exists()) {
            try {
                JsonObject json = JsonParser.parseString(Files.readString(FILE.toPath())).getAsJsonObject();
                if (json.has("colors")) {
                    JsonArray arr = json.getAsJsonArray("colors");
                    for (int i = 0; i < Math.min(arr.size(), c.length); i++) {
                        c[i] = arr.get(i).getAsInt();
                    }
                }
            } catch (Exception ignored) {
            }
        }
        Theme t = ThemeManager.getInstance().getCurrentTheme();
        t.setAccent(c[0]);
        t.color2 = c[1];
        t.colorIcons = c[2];
        t.colorText = c[3];
        t.colorInactiveText = c[4];
        t.colorHeaderBg = c[5];
        t.colorField = c[6];
        t.colorSlider = c[7];
        t.colorInactiveButton = c[8];
        t.colorInactiveIndicator = c[8];
        t.colorSeparator = c[9];
        t.colorInterfaceBg = c[10];
        t.colorClickGui = c[11];
        t.colorHeaderText = c[0];
        t.colorButton = c[0];
        t.colorIndicator = c[0];
        t.colorVisualModules = c[0];
        t.colorWindowBg = c[6];
        t.colorInactiveField = c[8];
    }

    private void applyHSV() {
        if (editingSlot < 0) return;
        colors[editingSlot] = Color.HSBtoRGB(hsv[0], hsv[1], hsv[2]) | 0xFF000000;
        selectedPreset = -1;
        applyColors();
    }

    private void loadHSV(int slot) {
        Color c = new Color(colors[slot], true);
        Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), hsv);
    }

    private static float clamp01(double v) {
        return (float) Math.max(0.0, Math.min(1.0, v));
    }

    public void save() {
        try {
            if (!DIR.exists()) DIR.mkdirs();
            JsonObject json = new JsonObject();
            JsonArray arr = new JsonArray();
            for (int c : colors) arr.add(c);
            json.add("colors", arr);
            if (selectedPreset >= 0 && selectedPreset < ThemePresets.ALL.length) {
                json.addProperty("preset", ThemePresets.ALL[selectedPreset].name());
            }
            Files.writeString(FILE.toPath(), GSON.toJson(json));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void load() {
        if (!FILE.exists()) return;
        try {
            JsonObject json = JsonParser.parseString(Files.readString(FILE.toPath())).getAsJsonObject();
            if (json.has("colors")) {
                JsonArray arr = json.getAsJsonArray("colors");
                for (int i = 0; i < Math.min(arr.size(), colors.length); i++) {
                    colors[i] = arr.get(i).getAsInt();
                }
            }
            if (json.has("preset")) {
                int idx = ThemePresets.indexOf(json.get("preset").getAsString());
                if (idx >= 0) selectedPreset = idx;
            }
            syncPresetFromColors();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
