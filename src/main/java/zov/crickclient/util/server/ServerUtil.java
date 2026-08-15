package zov.crickclient.util.server;

import lombok.experimental.UtilityClass;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.text.Text;
import zov.crickclient.mixin.PlayerListHudAccessor;
import zov.crickclient.util.IMinecraft;

/**
 * Доступ к серверным данным, которые сервер отдаёт в табе (header/footer)
 * и к адресу подключения. Используется модулями-помощниками (ServerJoiner и т.п.),
 * чтобы определять, где именно находится игрок: хаб, лобби, анархия.
 */
@UtilityClass
public class ServerUtil implements IMinecraft {

    /** Заголовок таба (header). Пустая строка, если данных нет. */
    public String getTabHeader() {
        if (mc.player == null || mc.player.networkHandler == null || mc.inGameHud == null) return "";

        PlayerListHud hud = mc.inGameHud.getPlayerListHud();
        if (hud == null) return "";

        Text header = ((PlayerListHudAccessor) hud).getHeader();
        return header == null ? "" : header.getString();
    }

    /** Подвал таба (footer). Пустая строка, если данных нет. */
    public String getTabFooter() {
        if (mc.player == null || mc.player.networkHandler == null || mc.inGameHud == null) return "";

        PlayerListHud hud = mc.inGameHud.getPlayerListHud();
        if (hud == null) return "";

        Text footer = ((PlayerListHudAccessor) hud).getFooter();
        return footer == null ? "" : footer.getString();
    }

    /** Адрес сервера, к которому подключён клиент. Пустая строка в одиночной игре. */
    public String getServerAddress() {
        if (mc.player == null || mc.player.networkHandler == null) return "";

        ServerInfo info = mc.player.networkHandler.getServerInfo();
        return info == null ? "" : info.address;
    }
}
