package zov.crickclient.util.math;

import com.google.common.eventbus.Subscribe;
import lombok.Getter;
import net.minecraft.network.packet.s2c.common.CommonPingS2CPacket;
import zov.crickclient.CrickClient;
import zov.crickclient.event.list.EventPacket;
import zov.crickclient.event.list.EventTick;
import zov.crickclient.util.IMinecraft;

@Getter
public class PingGetter implements IMinecraft {
    public PingGetter() {
        CrickClient.getInstance().getEventBus().register(this);
    }

    private final StopWatch stopWatch = new StopWatch();
    private boolean lagged;
    private int ping;

    @Subscribe
    private void onUpdate(EventTick e) {
        ping = (int) stopWatch.getTime();
        if (stopWatch.getTime() > 1000) lagged = true;
    }

    @Subscribe
    private void onPacket(EventPacket e) {
        if (e.getPacket() instanceof CommonPingS2CPacket) {
            stopWatch.reset();
        }
    }
}