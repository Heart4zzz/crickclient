package zov.crickclient.ui.menu;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.text.Text;
import zov.crickclient.util.IMinecraft;
import zov.crickclient.util.render.msdf.Fonts;
import zov.crickclient.util.render.providers.ColorProvider;
import zov.crickclient.util.render.renderers.DrawUtil;

import java.util.ArrayList;
import java.util.List;

public class AlphaTitleScreen extends Screen implements IMinecraft {
    private final List<MenuButton> buttons = new ArrayList<>();

    public AlphaTitleScreen() {
        super(Text.empty());
    }

    @Override
    protected void init() {
        buttons.clear();

        float btnW = 190f;
        float btnH = 26f;
        float gap = 7f;
        float totalH = btnH * 5 + gap * 4;
        float startY = height / 2f - totalH / 2f + 24f;
        float centerX = width / 2f - btnW / 2f;

        buttons.add(new MenuButton("Singleplayer", () -> mc.setScreen(new SelectWorldScreen(this))));
        buttons.add(new MenuButton("Multiplayer", () -> mc.setScreen(new MultiplayerScreen(this))));
        buttons.add(new MenuButton("Alt Manager", () -> mc.setScreen(new AltManagerScreen(this))));
        buttons.add(new MenuButton("Settings", () -> mc.setScreen(new OptionsScreen(this, mc.options))));
        buttons.add(new MenuButton("Quit", mc::stop));

        for (int i = 0; i < buttons.size(); i++) {
            MenuButton btn = buttons.get(i);
            btn.setBounds(centerX, startY + i * (btnH + gap), btnW, btnH);
            btn.resetAppear();
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        MenuBackgroundRenderer.render(context, width, height);

        float pulse = (float) (Math.sin(System.currentTimeMillis() * 0.002) * 0.12 + 0.88);
        String title = "Crick Client";
        float titleSize = 22f;
        float titleW = Fonts.SFBOLD.get().getWidth(title, titleSize);
        float titleX = width / 2f - titleW / 2f;
        float titleY = height / 2f - 132f;

        DrawUtil.drawText(Fonts.SFBOLD.get(), title, titleX + 1f, titleY + 1f,
                MenuColors.withAlpha(MenuColors.ACCENT_GLOW, (int) (255 * pulse)), titleSize);
        DrawUtil.drawText(Fonts.SFBOLD.get(), title, titleX, titleY,
                MenuColors.withAlpha(MenuColors.ACCENT, (int) (255 * pulse)), titleSize);

        float lineW = 50f;
        float lineX = width / 2f - lineW / 2f;
        DrawUtil.drawRound(lineX, titleY + 28f, lineW, 1f, 0.5f,
                MenuColors.withAlpha(MenuColors.ACCENT_SOFT, (int) (200 * pulse)));

        String subtitle = "1.21.4";
        float subSize = 7f;
        float subW = Fonts.SFREGULAR.get().getWidth(subtitle, subSize);
        DrawUtil.drawText(Fonts.SFREGULAR.get(), subtitle, width / 2f - subW / 2f, titleY + 34f,
                MenuColors.withAlpha(MenuColors.ACCENT_MUTED, 180), subSize);

        String currentNick = mc.getSession().getUsername();
        String nickLabel = "Logged in as  " + currentNick;
        float nickSize = 6.5f;
        float nickW = Fonts.SFREGULAR.get().getWidth(nickLabel, nickSize);
        float badgeW = nickW + 20f;
        float badgeX = width / 2f - badgeW / 2f;
        float badgeY = height - 34f;

        DrawUtil.drawRoundBlur(badgeX, badgeY, badgeW, 16f, 5f,
                ColorProvider.rgba(180, 190, 220, 80), 8f);
        DrawUtil.drawRound(badgeX, badgeY, badgeW, 16f, 5f, MenuColors.PANEL_BG);
        DrawUtil.drawRound(badgeX - 0.5f, badgeY - 0.5f, badgeW + 1f, 17f, 5.5f,
                MenuColors.withAlpha(MenuColors.BTN_BORDER, 100));
        DrawUtil.drawText(Fonts.SFREGULAR.get(), nickLabel, badgeX + 10f, badgeY + 4.5f,
                MenuColors.withAlpha(MenuColors.ACCENT_MUTED, 200), nickSize);

        for (int i = 0; i < buttons.size(); i++) {
            buttons.get(i).render(context, mouseX, mouseY, delta, i * 0.07f);
        }

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (MenuButton menuButton : buttons) {
            if (menuButton.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}
