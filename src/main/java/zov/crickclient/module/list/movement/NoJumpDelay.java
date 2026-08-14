package zov.crickclient.module.list.movement;

import com.google.common.eventbus.Subscribe;
import zov.crickclient.event.list.EventTick;
import zov.crickclient.module.Module;
import zov.crickclient.module.ModuleCategory;
import zov.crickclient.module.ModuleInformation;

@ModuleInformation(moduleName = "No Jump Delay", moduleDesc = "Убирает задержку между прыжками", moduleCategory = ModuleCategory.MISC)
public class NoJumpDelay extends Module {

    @Subscribe
    private void onUpdate(EventTick e) {
        if (mc.player == null || mc.world == null) return;

        mc.player.jumpingCooldown = 0;
    }
}