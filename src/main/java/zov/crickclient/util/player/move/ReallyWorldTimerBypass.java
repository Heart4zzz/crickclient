package zov.crickclient.util.player.move;

import com.google.common.eventbus.Subscribe;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.c2s.common.CommonPongC2SPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import zov.crickclient.CrickClient;
import zov.crickclient.event.list.EventPacket;
import zov.crickclient.event.list.EventTick;
import zov.crickclient.event.list.EventWorldRender;
import zov.crickclient.event.list.MoveInputEvent;
import zov.crickclient.util.packet.NetworkUtils;
import zov.crickclient.util.timer.TimerManager;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * ReallyWorld timer bypass — логика 1:1 из референса (charge/boost + transaction delay).
 */
public final class ReallyWorldTimerBypass {
    private static final ReallyWorldTimerBypass INSTANCE = new ReallyWorldTimerBypass();

    private static final float CHARGE_TIMER = 0.05F;
    private static final float BOOST_TIMER = 1.7F;
    private static final long CHARGE_DURATION_NANOS = 1_250_000_000L;
    private static final long MAX_BOOST_DURATION_NANOS = 2_400_000_000L;
    private static final int FULL_BOOST_JUMPS = 4;

    private final Queue<CommonPongC2SPacket> delayedTransactions = new ConcurrentLinkedQueue<>();

    private Phase phase = Phase.CHARGING;
    private boolean cycleActive;
    private boolean airborne;
    private int completedJumps;
    private int groundTicks;
    private long phaseStartedAt;
    private int enableCount;

    private ReallyWorldTimerBypass() {
    }

    public static void init() {
        CrickClient.getInstance().getEventBus().register(INSTANCE);
    }

    public static void enable() {
        if (INSTANCE.enableCount++ == 0) {
            INSTANCE.stopCycle();
        }
    }

    public static void disable() {
        if (INSTANCE.enableCount <= 0) {
            return;
        }
        if (--INSTANCE.enableCount <= 0) {
            INSTANCE.enableCount = 0;
            INSTANCE.stopCycle();
        }
    }

    @Subscribe
    private void onTick(EventTick event) {
        if (enableCount <= 0) {
            return;
        }

        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null || !cycleActive || phase != Phase.BOOSTING) {
            return;
        }

        if (player.verticalCollision) {
            if (airborne) {
                airborne = false;
                completedJumps++;
            }
        } else {
            airborne = true;
            if (completedJumps >= FULL_BOOST_JUMPS && player.getVelocity().y <= 0.0D) {
                beginCharging();
            }
        }
    }

    @Subscribe
    private void onWorldRender(EventWorldRender event) {
        if (enableCount <= 0) {
            return;
        }

        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null || !isMoving(player)) {
            stopCycle();
            return;
        }

        long now = System.nanoTime();
        if (!cycleActive) {
            cycleActive = true;
            beginCharging();
            return;
        }

        if (phase == Phase.CHARGING && now - phaseStartedAt >= CHARGE_DURATION_NANOS) {
            beginBoosting();
        } else if (phase == Phase.BOOSTING && now - phaseStartedAt >= MAX_BOOST_DURATION_NANOS) {
            beginCharging();
        }
    }

    @Subscribe
    private void onClientTick(EventTick event) {
        if (enableCount <= 0) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) {
            stopCycle();
        }
    }

    @Subscribe
    private void onInput(MoveInputEvent event) {
        if (enableCount <= 0) {
            return;
        }

        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null || phase != Phase.BOOSTING || !isMoving(player)) {
            groundTicks = 0;
            return;
        }

        groundTicks = player.verticalCollision ? groundTicks + 1 : 0;
        event.sprint = true;
        if (groundTicks > 0) {
            event.jump = true;
        }
    }

    @Subscribe
    private void onPacket(EventPacket event) {
        if (enableCount <= 0) {
            return;
        }

        if (event.getType() == EventPacket.Type.SEND && cycleActive && event.getPacket() instanceof CommonPongC2SPacket packet) {
            delayedTransactions.add(packet);
            event.cancelEvent();
            return;
        }

        if (event.getType() == EventPacket.Type.RECEIVE && cycleActive && event.getPacket() instanceof PlayerPositionLookS2CPacket) {
            beginCharging();
        }
    }

    private boolean isMoving(ClientPlayerEntity player) {
        return player.input.movementForward != 0.0F
                || player.input.movementSideways != 0.0F;
    }

    private void beginCharging() {
        flushTransactions();
        phase = Phase.CHARGING;
        airborne = false;
        completedJumps = 0;
        groundTicks = 0;
        phaseStartedAt = System.nanoTime();
        TimerManager.setTimer(CHARGE_TIMER);
    }

    private void beginBoosting() {
        phase = Phase.BOOSTING;
        airborne = false;
        completedJumps = 0;
        groundTicks = 0;
        phaseStartedAt = System.nanoTime();
        TimerManager.setTimer(BOOST_TIMER);
    }

    private void stopCycle() {
        flushTransactions();
        cycleActive = false;
        phase = Phase.CHARGING;
        airborne = false;
        completedJumps = 0;
        groundTicks = 0;
        phaseStartedAt = 0L;
        TimerManager.reset();
    }

    private void flushTransactions() {
        CommonPongC2SPacket packet;
        while ((packet = delayedTransactions.poll()) != null) {
            NetworkUtils.sendSilentPacket(packet);
        }
    }

    private enum Phase {
        CHARGING,
        BOOSTING
    }
}
