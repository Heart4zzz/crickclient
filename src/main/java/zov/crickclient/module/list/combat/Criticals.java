package zov.crickclient.module.list.combat;

import com.google.common.eventbus.Subscribe;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import zov.crickclient.event.list.EventAttack;
import zov.crickclient.module.Module;
import zov.crickclient.module.ModuleCategory;
import zov.crickclient.module.ModuleInformation;

@ModuleInformation(moduleName = "Criticals", moduleDesc = "Все удары будут критическими", moduleCategory = ModuleCategory.COMBAT)
public class Criticals extends Module {
    @Subscribe
    private void onAttack(EventAttack e) {
        if (mc.player.fallDistance == 0 && !mc.player.isOnGround()) {
            mc.player.fallDistance = 0.001f;
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.Full(mc.player.getX(), mc.player.getY() - 0.0000999999999, mc.player.getZ(), mc.player.getYaw(), mc.player.getPitch(), false, mc.player.horizontalCollision));
        }
    }
}