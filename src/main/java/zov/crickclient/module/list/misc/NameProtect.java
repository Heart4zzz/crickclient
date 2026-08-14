package zov.crickclient.module.list.misc;

import zov.crickclient.module.Module;
import zov.crickclient.module.ModuleCategory;
import zov.crickclient.module.ModuleInformation;
import zov.crickclient.module.settings.BooleanSetting;
import zov.crickclient.util.friend.Friend;
import zov.crickclient.util.friend.FriendRepository;

@ModuleInformation(moduleName = "Streamer Mode", moduleDesc = "Скрывает никнеймы игроков", moduleCategory = ModuleCategory.MISC)
public class NameProtect extends Module {

    public final BooleanSetting hideFriends = new BooleanSetting("Скрыть друзей", false);

    public String getCustomName() {
        return isEnabled() ? "crickclient" : mc.player.getNameForScoreboard();
    }

    public String getCustomName(String originalName) {
        if (!isEnabled() || mc.player == null) {
            return originalName;
        }

        String me = mc.player.getNameForScoreboard();
        if (originalName.contains(me)) {
            return originalName.replace(me, "crickclient");
        }

        if (hideFriends.getValue()) {
            var friends = FriendRepository.getFriends();
            for (Friend friend : friends) {
                if (originalName.contains(friend.name())) {
                    return originalName.replace(friend.name(), "crickclient");
                }
            }
        }

        return originalName;
    }
}