package zov.crickclient.util.player.move;

import com.google.common.eventbus.Subscribe;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import zov.crickclient.CrickClient;
import zov.crickclient.event.list.EventTickEnd;
import zov.crickclient.util.chat.ChatUtil;
import zov.crickclient.util.packet.NetworkUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public final class TpGrim {
    private static final TpGrim INSTANCE = new TpGrim();

    private static final double STEP_MIN = 11.0D;
    private static final double STEP_MAX = 16.0D;
    private static final int WORLD_MIN_Y = -64;
    private static final int WORLD_MAX_Y = 320;
    private static final double CLIMB_THRESHOLD = 2.0D;
    private static final double FLY_CLEARANCE = 2.5D;

    private final Map<String, Vec3d> positions = new HashMap<>();
    private Vec3d destination;
    private boolean active;
    private int waitTicks;
    private int stepCount;
    private double lastProgress = -1.0D;
    private int stuckSteps;

    private TpGrim() {
    }

    public static TpGrim getInstance() {
        return INSTANCE;
    }

    public static void init() {
        CrickClient.getInstance().getEventBus().register(INSTANCE);
    }

    public void start(double x, double y, double z) {
        if (!isInFlyMode(MinecraftClient.getInstance().player)) {
            return;
        }
        destination = new Vec3d(x, y, z);
        active = true;
        waitTicks = 0;
        stepCount = 0;
        lastProgress = -1.0D;
        stuckSteps = 0;
        ChatUtil.send("ТП → %.0f %.0f %.0f".formatted(x, y, z));
    }

    public boolean startToPlayer(String name) {
        if (!isInFlyMode(MinecraftClient.getInstance().player)) {
            return false;
        }
        Vec3d position = positions.get(name.toLowerCase());
        if (position == null) {
            ChatUtil.send("Игрок не найден: " + name);
            if (!positions.isEmpty()) {
                ChatUtil.send("Доступные: " + String.join(", ", positions.keySet()));
            }
            return false;
        }

        ChatUtil.send("ТП к игроку %s (%.0f %.0f %.0f)".formatted(name, position.x, position.y, position.z));
        start(position.x, position.y, position.z);
        return true;
    }

    public boolean stop() {
        if (!active) {
            return false;
        }
        active = false;
        destination = null;
        waitTicks = 0;
        ChatUtil.send("ТП остановлен.");
        return true;
    }

    public boolean isActive() {
        return active;
    }

    @Subscribe
    public void onTick(EventTickEnd ignored) {
        tick(MinecraftClient.getInstance());
    }

    public void tick(MinecraftClient minecraft) {
        updatePositions(minecraft);
        if (!active || destination == null) {
            return;
        }

        ClientPlayerEntity player = minecraft.player;
        ClientWorld level = minecraft.world;
        if (player == null || level == null || minecraft.getNetworkHandler() == null) {
            active = false;
            destination = null;
            waitTicks = 0;
            return;
        }

        if (!isInFlyMode(player)) {
            active = false;
            destination = null;
            waitTicks = 0;
            ChatUtil.send("TP » Полёт отключён — телепорт остановлен.");
            return;
        }

        if (waitTicks > 0) {
            waitTicks--;
            if (!active) {
                return;
            }
            sendCurrentPosition(player);
            player.setVelocity(0, 0, 0);
            return;
        }

        if (!active || destination == null) {
            return;
        }

        Vec3d current = player.getPos();
        double distXZ = Math.hypot(destination.x - current.x, destination.z - current.z);
        double progress = current.distanceTo(destination);

        if (lastProgress >= 0.0D && progress >= lastProgress - 1.5D) {
            stuckSteps++;
            if (stuckSteps >= 20) {
                active = false;
                destination = null;
                waitTicks = 0;
                ChatUtil.send("TP » Телепорт заблокирован. Выключите Blink/AirStuck и держите /fly активным.");
                return;
            }
        } else {
            stuckSteps = 0;
        }
        lastProgress = progress;

        if (distXZ < 1.5D) {
            active = false;
            destination = null;
            player.setVelocity(0, 0, 0);
            player.fallDistance = 0.0F;
            ChatUtil.send("Прибыли! (%d шагов)".formatted(stepCount));
            return;
        }

        Vec3d direction = destination.subtract(current).normalize();
        double moveDist = Math.min(distXZ, ThreadLocalRandom.current().nextDouble(STEP_MIN, STEP_MAX));
        double sideOffset = ThreadLocalRandom.current().nextDouble(-0.4D, 0.4D);
        Vec3d side = new Vec3d(-direction.z, 0, direction.x).multiply(sideOffset);
        Vec3d nextXZ = current.add(direction.multiply(moveDist)).add(side);

        if (isHole(level, nextXZ.x, nextXZ.z, current.y)) {
            nextXZ = bypassHole(level, current, nextXZ);
            ChatUtil.send("Дыра обнаружена — обход");
        }

        boolean flying = player.getAbilities().flying;
        double surfaceAtNext = getRealSurfaceY(level, nextXZ.x, nextXZ.z);
        double terrainY = surfaceAtNext + FLY_CLEARANCE;
        double finalY;
        boolean climbing;
        if (isInFlyMode(player)) {
            if (distXZ > 12.0D) {
                finalY = Math.max(current.y, terrainY);
                if (destination.y > finalY) {
                    finalY += Math.min(destination.y - finalY, moveDist * 0.35D);
                }
            } else {
                double yStep = Math.min(Math.abs(destination.y - current.y), 4.0D);
                finalY = current.y + Math.copySign(yStep, destination.y - current.y);
                finalY = Math.max(finalY, terrainY);
            }
            climbing = finalY > current.y + CLIMB_THRESHOLD;
        } else {
            climbing = surfaceAtNext - current.y > CLIMB_THRESHOLD;
            finalY = climbing
                    ? findFullBuriedY(level, nextXZ.x, nextXZ.z)
                    : findHalfBuriedY(level, nextXZ.x, nextXZ.z);
        }

        performMove(player, new Vec3d(nextXZ.x, finalY, nextXZ.z), !flying);
        stepCount++;

        if (stepCount % 4 == 0) {
            waitTicks = ThreadLocalRandom.current().nextInt(7, 12);
            double remaining = player.getPos().distanceTo(destination);
            ChatUtil.send("%s | Y=%.1f | Пауза %d | Осталось %.0f".formatted(
                    climbing ? "▲" : "→", finalY, waitTicks, remaining
            ));
        } else {
            waitTicks = ThreadLocalRandom.current().nextInt(1, 4);
        }
    }

    private boolean isSolid(ClientWorld level, BlockState state, BlockPos pos) {
        return !state.isAir()
                && state.getFluidState().isEmpty()
                && !state.getCollisionShape(level, pos).isEmpty();
    }

    private double getRealSurfaceY(ClientWorld level, double x, double z) {
        int blockX = (int) Math.floor(x);
        int blockZ = (int) Math.floor(z);
        for (int y = WORLD_MAX_Y; y >= WORLD_MIN_Y; y--) {
            BlockPos pos = new BlockPos(blockX, y, blockZ);
            if (isSolid(level, level.getBlockState(pos), pos)) {
                return y + 1.0D;
            }
        }
        return WORLD_MIN_Y + 1.0D;
    }

    private double findHalfBuriedY(ClientWorld level, double x, double z) {
        return getRealSurfaceY(level, x, z) - 0.9D;
    }

    private double findFullBuriedY(ClientWorld level, double x, double z) {
        int blockX = (int) Math.floor(x);
        int blockZ = (int) Math.floor(z);
        double surfaceY = getRealSurfaceY(level, x, z);
        int surfaceBlock = (int) Math.floor(surfaceY) - 1;
        for (int y = surfaceBlock; y >= Math.max(WORLD_MIN_Y, surfaceBlock - 5); y--) {
            BlockPos lowerPos = new BlockPos(blockX, y, blockZ);
            BlockPos upperPos = new BlockPos(blockX, y + 1, blockZ);
            BlockState lower = level.getBlockState(lowerPos);
            BlockState upper = level.getBlockState(upperPos);
            if (isSolid(level, lower, lowerPos) && isSolid(level, upper, upperPos)) {
                return y + 0.1D;
            }
        }
        return surfaceY - 0.9D;
    }

    private boolean isHole(ClientWorld level, double x, double z, double currentY) {
        return currentY - getRealSurfaceY(level, x, z) > 4.0D;
    }

    private Vec3d bypassHole(ClientWorld level, Vec3d current, Vec3d nextXZ) {
        Vec3d direction = nextXZ.subtract(current).normalize();
        Vec3d perpendicular = new Vec3d(-direction.z, 0, direction.x);
        double[] offsets = {2.0D, -2.0D, 4.0D, -4.0D, 6.0D, -6.0D};
        for (double offset : offsets) {
            Vec3d candidate = nextXZ.add(perpendicular.multiply(offset));
            if (!isHole(level, candidate.x, candidate.z, current.y)) {
                return candidate;
            }
        }
        return current.add(destination.subtract(current).normalize().multiply(3.0D));
    }

    private void performMove(ClientPlayerEntity player, Vec3d target, boolean onGround) {
        for (int i = 0; i < 3; i++) {
            NetworkUtils.sendSilentPacket(new PlayerMoveC2SPacket.OnGroundOnly(onGround, player.horizontalCollision));
        }
        NetworkUtils.sendSilentPacket(new PlayerMoveC2SPacket.Full(
                target.x, target.y, target.z,
                player.getYaw(), player.getPitch(),
                onGround, player.horizontalCollision
        ));
        player.setPosition(target.x, target.y, target.z);
        player.setVelocity(0, 0, 0);
        player.fallDistance = 0.0F;
    }

    private void sendCurrentPosition(ClientPlayerEntity player) {
        boolean onGround = !player.getAbilities().flying;
        NetworkUtils.sendSilentPacket(new PlayerMoveC2SPacket.Full(
                player.getX(), player.getY(), player.getZ(),
                player.getYaw(), player.getPitch(),
                onGround, player.horizontalCollision
        ));
    }

    private void updatePositions(MinecraftClient minecraft) {
        if (minecraft.world == null) {
            return;
        }
        for (var player : minecraft.world.getPlayers()) {
            if (player != minecraft.player) {
                positions.put(player.getNameForScoreboard().toLowerCase(), player.getPos());
            }
        }
    }

    public static boolean isInFlyMode(ClientPlayerEntity player) {
        if (player == null) {
            return false;
        }
        return player.getAbilities().flying || player.getAbilities().allowFlying;
    }
}
