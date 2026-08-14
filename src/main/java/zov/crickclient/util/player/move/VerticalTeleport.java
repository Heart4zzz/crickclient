package zov.crickclient.util.player.move;

import lombok.experimental.UtilityClass;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import zov.crickclient.util.IMinecraft;
import zov.crickclient.util.packet.NetworkUtils;

/**
 * Общий примитив вертикального телепорта по механике VClip:
 * несколько OnGroundOnly пакетов + PositionAndOnGround со смещением по Y.
 * Пакеты отправляются тихо (sendSilentPacket), чтобы не триггерить собственные SEND-обработчики.
 */
@UtilityClass
public class VerticalTeleport implements IMinecraft {

    public void teleport(double offset) {
        if (mc.player == null || mc.getNetworkHandler() == null || offset == 0.0D) {
            return;
        }

        double x = mc.player.getX();
        double y = mc.player.getY();
        double z = mc.player.getZ();

        for (int i = 0; i < 3; i++) {
            NetworkUtils.sendSilentPacket(new PlayerMoveC2SPacket.OnGroundOnly(false, mc.player.horizontalCollision));
        }

        NetworkUtils.sendSilentPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
                x, y + offset, z, false, mc.player.horizontalCollision
        ));
        mc.player.setPosition(x, y + offset, z);
    }
}
