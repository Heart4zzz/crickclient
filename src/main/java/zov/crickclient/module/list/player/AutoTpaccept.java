package zov.crickclient.module.list.player;

import com.google.common.eventbus.Subscribe;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import zov.crickclient.event.list.EventPacket;
import zov.crickclient.module.Module;
import zov.crickclient.module.ModuleCategory;
import zov.crickclient.module.ModuleInformation;
import zov.crickclient.module.ModuleSettingDefinitions;
import zov.crickclient.module.settings.BooleanSetting;
import zov.crickclient.util.friend.FriendRepository;

@ModuleInformation(moduleName = "Auto Tpaccept", moduleDesc = "Автоматически принимает тп запросы", moduleCategory = ModuleCategory.PLAYER)
public class AutoTpaccept extends Module {
    private final BooleanSetting friendsOnly = ModuleSettingDefinitions.autoTpOnlyFriends();

    @Subscribe
    public void onPacket(EventPacket e) {
        if (mc.player == null || mc.world == null) return;

        if (e.getPacket() instanceof GameMessageS2CPacket p
                && TpRequestRecognizer.shouldAccept(
                        p.content().getString(),
                        friendsOnly.getValue(),
                        FriendRepository::isFriend
                )) {
            mc.getNetworkHandler().sendChatCommand("tpaccept");
        }
    }
}