package zov.crickclient.util.packet;

import lombok.experimental.UtilityClass;
import net.minecraft.network.packet.Packet;
import zov.crickclient.util.IMinecraft;

@UtilityClass
public class NetworkUtils implements IMinecraft {

    private static boolean sendingSilent;

    public void sendSilentPacket(Packet<?> packet) {
        if (mc.player == null || mc.getNetworkHandler() == null) {
            return;
        }

        try {
            sendingSilent = true;
            mc.getNetworkHandler().sendPacket(packet);
        } finally {
            sendingSilent = false;
        }
    }

    public void sendPacket(Packet<?> packet) {
        if (mc.player == null || mc.getNetworkHandler() == null) {
            return;
        }
        mc.getNetworkHandler().sendPacket(packet);
    }

    public static boolean isSendingSilent() {
        return sendingSilent;
    }
}
