package zov.crickclient.module.list.player;

import com.google.common.eventbus.Subscribe;
import net.minecraft.item.Items;
import zov.crickclient.event.list.EventPlayerUpdate;
import zov.crickclient.module.Module;
import zov.crickclient.module.ModuleCategory;
import zov.crickclient.module.ModuleInformation;

@ModuleInformation(moduleName = "Fast Exp", moduleDesc = "Ускоренное использование опыта", moduleCategory = ModuleCategory.PLAYER)
public class FastExp extends Module {

    @Subscribe
    private void onPlayerUpdate(EventPlayerUpdate e) {
        if (!(mc.player.getMainHandStack().getItem() == Items.EXPERIENCE_BOTTLE || mc.player.getOffHandStack().getItem() == Items.EXPERIENCE_BOTTLE)) return;

        mc.itemUseCooldown = 0;
    }
}