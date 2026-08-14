package zov.crickclient.module.list.movement;

import zov.crickclient.module.Module;
import zov.crickclient.module.ModuleCategory;
import zov.crickclient.module.ModuleInformation;
import zov.crickclient.util.player.move.ReallyWorldTimerBypass;

@ModuleInformation(
        moduleName = "Timer",
        moduleDesc = "ReallyWorld: замедляется, накапливает инерцию и резко выбрасывает её в ускорение",
        moduleCategory = ModuleCategory.MOVEMENT
)
public final class Timer extends Module {

    @Override
    public void onEnable() {
        ReallyWorldTimerBypass.enable();
        super.onEnable();
    }

    @Override
    public void onDisable() {
        ReallyWorldTimerBypass.disable();
        super.onDisable();
    }
}
