package zov.crickclient.module.list.movement;

import com.google.common.eventbus.Subscribe;
import zov.crickclient.CrickClient;
import zov.crickclient.event.list.EventNoSlow;
import zov.crickclient.module.Module;
import zov.crickclient.module.ModuleCategory;
import zov.crickclient.module.ModuleInformation;
import zov.crickclient.module.list.combat.KillAura;
import zov.crickclient.module.settings.ModeSetting;
import zov.crickclient.util.player.simulate.SimulatedPlayer;

@ModuleInformation(moduleName = "No Slow", moduleDesc = "Убирает замедление при использовании", moduleCategory = ModuleCategory.MOVEMENT)
public class NoSlow extends Module {

    private final ModeSetting mode = new ModeSetting("Мод", "Vanilla", "Vanilla", "Grim");

    @Subscribe
    private void onNoSlow(EventNoSlow e) {
        switch (mode.getValue()) {
            case "Vanilla" -> {
                if (!(CrickClient.getInstance().getModuleStorage().get(KillAura.class).getTarget() != null && SimulatedPlayer.simulateLocalPlayer(1).fallDistance > 0)) mc.player.setSprinting(true);
                e.cancelEvent();
            }
            case "Grim" -> {
                mc.player.setSprinting(mc.player.getItemUseTime() > 4 && !(CrickClient.getInstance().getModuleStorage().get(KillAura.class).getTarget() != null && SimulatedPlayer.simulateLocalPlayer(1).fallDistance > 0) && CrickClient.getInstance().getServerManager().getSprintingChangeTicks() > 0);
                if (mc.player.getItemUseTime() % 2 == 0) e.cancelEvent();
            }
        }
    }
}