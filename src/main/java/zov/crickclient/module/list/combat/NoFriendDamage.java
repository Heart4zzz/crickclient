package zov.crickclient.module.list.combat;

import com.google.common.eventbus.Subscribe;
import zov.crickclient.event.list.EventAttack;
import zov.crickclient.module.Module;
import zov.crickclient.module.ModuleCategory;
import zov.crickclient.module.ModuleInformation;
import zov.crickclient.util.friend.Friend;
import zov.crickclient.util.friend.FriendRepository;

@ModuleInformation(moduleName = "No Friend Damage", moduleDesc = "Не атакует друзей из списка", moduleCategory = ModuleCategory.COMBAT)
public class NoFriendDamage extends Module {

    @Subscribe
    private void onAttack(EventAttack e) {
        for (Friend friend : FriendRepository.getFriends()) {
            if (e.getEntity() == mc.player) continue;
            if (!e.getEntity().getNameForScoreboard().equals(friend.name())) continue;
            e.cancelEvent();
        }
    }
}