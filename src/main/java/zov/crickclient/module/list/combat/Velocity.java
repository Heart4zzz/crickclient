package zov.crickclient.module.list.combat;

import com.google.common.eventbus.Subscribe;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import zov.crickclient.event.list.EventPacket;
import zov.crickclient.module.Module;
import zov.crickclient.module.ModuleCategory;
import zov.crickclient.module.ModuleInformation;

@ModuleInformation(moduleName = "Velocity", moduleDesc = "Убирает отбрасывание от ударов", moduleCategory = ModuleCategory.COMBAT)
public class Velocity extends Module {
    @Subscribe
    private void onPacket(EventPacket e) {
        if (e.getPacket() instanceof EntityVelocityUpdateS2CPacket packet) {
            if (packet.getEntityId() != mc.player.getId()) return;

            e.cancelEvent();
        }
    }
}