package zov.crickclient.util.alt;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.session.Session;
import net.minecraft.util.Uuids;
import zov.crickclient.mixin.IMinecraftClientAccessor;

import java.util.Optional;

public final class SessionHelper {
    private SessionHelper() {
    }

    public static Session createOfflineSession(String username) {
        return new Session(
                username,
                Uuids.getOfflinePlayerUuid(username),
                "",
                Optional.empty(),
                Optional.empty(),
                Session.AccountType.LEGACY
        );
    }

    public static void applySession(String username, Screen returnScreen) {
        MinecraftClient mc = MinecraftClient.getInstance();
        Session session = createOfflineSession(username);
        ((IMinecraftClientAccessor) mc).setSession(session);
        AltRepository.setSelected(username);
        AltRepository.save();
        mc.reset(returnScreen);
    }

    public static void applySelectedOnStartup() {
        AltRepository.getSelectedName().ifPresent(name -> {
            if (AltRepository.getAccount(name).isPresent()) {
                MinecraftClient mc = MinecraftClient.getInstance();
                if (mc != null) {
                    ((IMinecraftClientAccessor) mc).setSession(createOfflineSession(name));
                }
            }
        });
    }
}
