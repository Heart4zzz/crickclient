package zov.crickclient.ui.menu;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import zov.crickclient.util.IMinecraft;
import zov.crickclient.util.alt.AltAccount;
import zov.crickclient.util.alt.AltNameGenerator;
import zov.crickclient.util.alt.AltRepository;
import zov.crickclient.util.alt.SessionHelper;
import zov.crickclient.util.render.helper.HoverUtil;
import zov.crickclient.util.render.math.Animation;
import zov.crickclient.util.render.math.Easing;
import zov.crickclient.util.render.math.Scissor;
import zov.crickclient.util.render.msdf.Fonts;
import zov.crickclient.util.render.providers.ColorProvider;
import zov.crickclient.util.render.renderers.DrawUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class AltManagerScreen extends Screen implements IMinecraft {
    private static final float PANEL_W = 330f;
    private static final float PANEL_H = 278f;
    private static final float ROW_H = 24f;
    private static final float BTN_H = 16f;
    private static final float BTN_GAP = 5f;

    private record ActionButton(String label, Runnable action) {}

    private final Screen parent;
    private final MenuTextField nameField = new MenuTextField("Enter nickname...");
    private final Animation openAnim = new Animation(Easing.QUINTIC_OUT, 320);
    private final Map<String, Animation> rowHoverAnims = new HashMap<>();

    private float scrollOffset = 0f;
    private String hoveredName;

    public AltManagerScreen(Screen parent) {
        super(Text.empty());
        this.parent = parent;
    }

    @Override
    protected void init() {
        openAnim.reset(0f);
        nameField.resetAppear();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        MenuBackgroundRenderer.render(context, width, height);

        openAnim.run(true);
        float open = openAnim.getValue();
        float panelX = width / 2f - PANEL_W / 2f;
        float panelY = height / 2f - PANEL_H / 2f + (1f - open) * 28f;

        DrawUtil.drawRoundBlur(panelX, panelY, PANEL_W, PANEL_H, 10f,
                ColorProvider.rgba(180, 190, 220, (int) (90 * open)), 16f);
        DrawUtil.drawRound(panelX - 0.5f, panelY - 0.5f, PANEL_W + 1f, PANEL_H + 1f, 10.5f,
                MenuColors.withAlpha(MenuColors.BTN_BORDER_HOVER, (int) (130 * open)));
        DrawUtil.drawRound(panelX, panelY, PANEL_W, PANEL_H, 10f,
                MenuColors.withAlpha(MenuColors.PANEL_BG, (int) (230 * open)));

        DrawUtil.drawRound(panelX + 14f, panelY + 14f, 3f, 18f, 1.5f,
                MenuColors.withAlpha(MenuColors.ACCENT, (int) (200 * open)));

        DrawUtil.drawText(Fonts.SFBOLD.get(), "Alt Manager", panelX + 22f, panelY + 16f,
                MenuColors.withAlpha(MenuColors.ACCENT, (int) (255 * open)), 10f);

        DrawUtil.drawText(Fonts.SFREGULAR.get(), "Account Switcher", panelX + 22f, panelY + 28f,
                MenuColors.withAlpha(MenuColors.ACCENT_MUTED, (int) (160 * open)), 6f);

        String selected = AltRepository.getSelectedName().orElse(mc.getSession().getUsername());
        float selW = Fonts.SFREGULAR.get().getWidth(selected, 7f);
        float badgeW = selW + 36f;
        float badgeX = panelX + PANEL_W - badgeW - 14f;
        float badgeY = panelY + 15f;

        DrawUtil.drawRound(badgeX, badgeY, badgeW, 18f, 5f,
                MenuColors.withAlpha(MenuColors.ROW_SELECTED, (int) (200 * open)));
        DrawUtil.drawRound(badgeX - 0.5f, badgeY - 0.5f, badgeW + 1f, 19f, 5.5f,
                MenuColors.withAlpha(MenuColors.BTN_BORDER, (int) (120 * open)));
        DrawUtil.drawText(Fonts.SFREGULAR.get(), "Active", badgeX + 8f, badgeY + 3f,
                MenuColors.withAlpha(MenuColors.ACCENT_MUTED, (int) (170 * open)), 5.5f);
        DrawUtil.drawText(Fonts.SFREGULAR.get(), selected, badgeX + 8f, badgeY + 9f,
                MenuColors.withAlpha(MenuColors.ACCENT, (int) (255 * open)), 7f);

        float listX = panelX + 14f;
        float listY = panelY + 48f;
        float listW = PANEL_W - 28f;
        float listH = PANEL_H - 108f;

        DrawUtil.drawRound(listX, listY, listW, listH, 6f,
                MenuColors.withAlpha(MenuColors.PANEL_INNER, (int) (200 * open)));

        renderAccountList(listX, listY, listW, listH, mouseX, mouseY, open);

        float fieldY = panelY + PANEL_H - 52f;
        nameField.setBounds(listX, fieldY, listW, 22f);
        nameField.render(context, mouseX, mouseY, delta);

        float btnY = panelY + PANEL_H - 24f;
        float btnW = (listW - BTN_GAP * 4f) / 5f;

        ActionButton[] buttons = {
                new ActionButton("Add", this::addAccount),
                new ActionButton("Delete", this::deleteSelected),
                new ActionButton("Favorite", this::toggleFavorite),
                new ActionButton("Random", this::addRandomAccount),
                new ActionButton("Back", () -> mc.setScreen(parent))
        };

        for (int i = 0; i < buttons.length; i++) {
            renderActionButton(buttons[i].label(), listX + i * (btnW + BTN_GAP), btnY, btnW, BTN_H, mouseX, mouseY, open);
        }

        super.render(context, mouseX, mouseY, delta);
    }

    private void renderAccountList(float x, float y, float w, float h,
                                   int mouseX, int mouseY, float open) {
        List<AltAccount> accounts = AltRepository.getAccounts();
        hoveredName = null;

        Scissor.push();
        Scissor.setFromComponentCoordinates(x, y, w, h);

        if (accounts.isEmpty()) {
            String empty = "No accounts yet — add one below";
            float emptyW = Fonts.SFREGULAR.get().getWidth(empty, 7f);
            DrawUtil.drawText(Fonts.SFREGULAR.get(), empty, x + (w - emptyW) / 2f, y + h / 2f - 8f,
                    MenuColors.withAlpha(MenuColors.ACCENT_MUTED, (int) (160 * open)), 7f);
            String hint = "or press Random";
            float hintW = Fonts.SFREGULAR.get().getWidth(hint, 6f);
            DrawUtil.drawText(Fonts.SFREGULAR.get(), hint, x + (w - hintW) / 2f, y + h / 2f + 4f,
                    MenuColors.withAlpha(MenuColors.ACCENT_SOFT, (int) (140 * open)), 6f);
        } else {
            float contentH = accounts.size() * (ROW_H + 3f);
            float maxScroll = Math.max(0f, contentH - h + 8f);
            scrollOffset = Math.max(0f, Math.min(scrollOffset, maxScroll));

            for (int i = 0; i < accounts.size(); i++) {
                AltAccount account = accounts.get(i);
                float rowY = y + 5f + i * (ROW_H + 3f) - scrollOffset;
                if (rowY + ROW_H < y || rowY > y + h) continue;

                boolean hovered = HoverUtil.isHovered(mouseX, mouseY, x + 5f, rowY, w - 10f, ROW_H);
                if (hovered) hoveredName = account.getName();

                Animation hoverAnim = rowHoverAnims.computeIfAbsent(account.getName(),
                        k -> new Animation(Easing.QUINTIC_OUT, 160));
                hoverAnim.run(hovered);
                float hover = (float) hoverAnim.getValue();

                boolean selected = account.getName().equalsIgnoreCase(
                        AltRepository.getSelectedName().orElse(""));
                boolean active = account.getName().equalsIgnoreCase(mc.getSession().getUsername());

                int bg = selected
                        ? ColorProvider.interpolateColor(
                                MenuColors.withAlpha(MenuColors.ROW_SELECTED, (int) (210 * open)),
                                MenuColors.withAlpha(MenuColors.ROW_HOVER, (int) (230 * open)), hover)
                        : ColorProvider.interpolateColor(
                                MenuColors.withAlpha(MenuColors.ROW_BG, (int) (180 * open)),
                                MenuColors.withAlpha(MenuColors.ROW_HOVER, (int) (210 * open)), hover);

                DrawUtil.drawRound(x + 5f, rowY, w - 10f, ROW_H, 5f, bg);

                if (selected) {
                    DrawUtil.drawRound(x + 5f, rowY, 2.5f, ROW_H, 1.2f,
                            MenuColors.withAlpha(MenuColors.ACCENT, (int) (220 * open)));
                }

                String star = account.isFavorite() ? "★ " : "";
                DrawUtil.drawText(Fonts.SFREGULAR.get(), star + account.getName(), x + 14f, rowY + ROW_H / 2f - 3.5f,
                        MenuColors.withAlpha(MenuColors.ACCENT, (int) (255 * open)), 7f);

                if (active) {
                    String tag = "active";
                    float tagW = Fonts.SFREGULAR.get().getWidth(tag, 5.5f);
                    DrawUtil.drawRound(x + w - tagW - 18f, rowY + 5f, tagW + 8f, 12f, 3f,
                            MenuColors.withAlpha(MenuColors.BTN_BG_HOVER, (int) (180 * open)));
                    DrawUtil.drawText(Fonts.SFREGULAR.get(), tag, x + w - tagW - 14f, rowY + 8f,
                            MenuColors.withAlpha(MenuColors.ACCENT_MUTED, (int) (200 * open)), 5.5f);
                }
            }
        }

        Scissor.unset();
        Scissor.pop();
    }

    private void renderActionButton(String label, float x, float y, float w, float h,
                                    int mouseX, int mouseY, float open) {
        boolean hovered = HoverUtil.isHovered(mouseX, mouseY, x, y, w, h);

        int border = hovered
                ? MenuColors.withAlpha(MenuColors.BTN_BORDER_HOVER, (int) (180 * open))
                : MenuColors.withAlpha(MenuColors.BTN_BORDER, (int) (100 * open));
        DrawUtil.drawRound(x - 0.5f, y - 0.5f, w + 1f, h + 1f, 4.5f, border);

        int bg = hovered
                ? MenuColors.withAlpha(MenuColors.BTN_BG_HOVER, (int) (240 * open))
                : MenuColors.withAlpha(MenuColors.BTN_BG, (int) (210 * open));
        DrawUtil.drawRound(x, y, w, h, 4f, bg);

        float textW = Fonts.SFREGULAR.get().getWidth(label, 6.5f);
        DrawUtil.drawText(Fonts.SFREGULAR.get(), label, x + (w - textW) / 2f, y + h / 2f - 3.2f,
                MenuColors.withAlpha(MenuColors.ACCENT, (int) (255 * open)), 6.5f);
    }

    private void addAccount() {
        if (nameField.text.isBlank()) return;
        String name = nameField.text.trim();
        if (AltRepository.addAccount(name)) {
            nameField.clear();
            selectAccount(name);
        }
    }

    private void addRandomAccount() {
        String name = AltNameGenerator.generate();
        int attempts = 0;
        while (AltRepository.getAccount(name).isPresent() && attempts++ < 20) {
            name = AltNameGenerator.generate();
        }
        AltRepository.addAccount(name);
        nameField.clear();
        selectAccount(name);
    }

    private void deleteSelected() {
        String target = resolveTargetName();
        if (target != null) {
            AltRepository.removeAccount(target);
            rowHoverAnims.remove(target);
        }
    }

    private void toggleFavorite() {
        String target = resolveTargetName();
        if (target != null) {
            AltRepository.toggleFavorite(target);
        }
    }

    private String resolveTargetName() {
        return AltRepository.getSelectedName()
                .or(() -> hoveredName != null ? Optional.of(hoveredName) : Optional.empty())
                .orElse(null);
    }

    private void selectAccount(String name) {
        if (name == null || name.isBlank()) return;
        SessionHelper.applySession(name, this);
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        nameField.mouseClicked(mouseX, mouseY, button);

        float panelX = width / 2f - PANEL_W / 2f;
        float panelY = height / 2f - PANEL_H / 2f;
        float listX = panelX + 14f;
        float listY = panelY + 48f;
        float listW = PANEL_W - 28f;
        float listH = PANEL_H - 108f;

        if (button == 0) {
            List<AltAccount> accounts = AltRepository.getAccounts();
            for (int i = 0; i < accounts.size(); i++) {
                AltAccount account = accounts.get(i);
                float rowY = listY + 5f + i * (ROW_H + 3f) - scrollOffset;
                if (HoverUtil.isHovered(mouseX, mouseY, listX + 5f, rowY, listW - 10f, ROW_H)) {
                    selectAccount(account.getName());
                    return true;
                }
            }
        }

        float btnY = panelY + PANEL_H - 24f;
        float btnW = (listW - BTN_GAP * 4f) / 5f;

        if (button == 0 && clickAt(listX, btnY, btnW, BTN_H, mouseX, mouseY)) { addAccount(); return true; }
        if (button == 0 && clickAt(listX + (btnW + BTN_GAP), btnY, btnW, BTN_H, mouseX, mouseY)) { deleteSelected(); return true; }
        if (button == 0 && clickAt(listX + (btnW + BTN_GAP) * 2, btnY, btnW, BTN_H, mouseX, mouseY)) { toggleFavorite(); return true; }
        if (button == 0 && clickAt(listX + (btnW + BTN_GAP) * 3, btnY, btnW, BTN_H, mouseX, mouseY)) { addRandomAccount(); return true; }
        if (button == 0 && clickAt(listX + (btnW + BTN_GAP) * 4, btnY, btnW, BTN_H, mouseX, mouseY)) { mc.setScreen(parent); return true; }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean clickAt(float x, float y, float w, float h, double mouseX, double mouseY) {
        return HoverUtil.isHovered(mouseX, mouseY, x, y, w, h);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        scrollOffset -= (float) verticalAmount * 14f;
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        nameField.keyPressed(keyCode, scanCode, modifiers);
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            mc.setScreen(parent);
            return true;
        }
        if ((keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) && nameField.isFocused()) {
            addAccount();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        nameField.charTyped(chr, modifiers);
        return super.charTyped(chr, modifiers);
    }
}
