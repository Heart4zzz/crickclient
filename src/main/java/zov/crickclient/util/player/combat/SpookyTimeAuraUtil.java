package zov.crickclient.util.player.combat;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import zov.crickclient.util.IMinecraft;
import zov.crickclient.util.render.math.MathUtil;
import zov.crickclient.util.rotation.Rotation;
import zov.crickclient.util.rotation.RotationComponent;

/**
 * Port of Minced {@code AuraModeSmooth} ("Spooky Test") rotation and
 * {@code AuraDistanceUtil} / {@code AttackAura} attack gates.
 */
public class SpookyTimeAuraUtil implements IMinecraft {

    private static final double YAW_OFFSET = 90.0;
    private static final float YAW_NOISE_MAX = 3.0F;
    private static final float PITCH_OFFSET = 0.2958859F;
    private static final float ROTATION_SPEED = 360.0F;
    private static final long ATTACK_DELAY_MS = 460L;
    private static final float COOLDOWN_PROGRESS_BASE = 0.5F;
    private static final int STUCK_RECOVERY_TICKS = 16;

    private long nextAttackTimeMs = 0L;
    private float cooldownThreshold = 0.91F;
    private boolean zeroMovementNextTick = false;
    private boolean waitingAfterSprintReset = false;
    private float aimYaw;
    private float aimPitch;
    private int ticksSinceLastAttack;

    public void reset() {
        nextAttackTimeMs = 0L;
        cooldownThreshold = MathUtil.random(0.88F, 0.94F);
        zeroMovementNextTick = false;
        waitingAfterSprintReset = false;
        ticksSinceLastAttack = 0;
    }

    public void updateRotation(LivingEntity target) {
        if (mc.player == null || target == null) {
            return;
        }

        Vec3d delta = getAimDelta(target);
        aimYaw = (float) MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(delta.z, delta.x)) - YAW_OFFSET);
        aimPitch = (float) (-Math.toDegrees(Math.atan2(delta.y, Math.hypot(delta.x, delta.z)))) + PITCH_OFFSET;

        float noiseRange = MathUtil.random(0.0F, YAW_NOISE_MAX);
        float yawNoise = MathUtil.random(-noiseRange, noiseRange);

        RotationComponent.update(
                new Rotation(aimYaw + yawNoise, aimPitch),
                ROTATION_SPEED,
                ROTATION_SPEED,
                360.0F,
                360.0F,
                6,
                1,
                false
        );
    }

    public float getFocusYaw(LivingEntity target) {
        Vec3d delta = getAimDelta(target);
        return MathHelper.wrapDegrees((float) (Math.toDegrees(Math.atan2(delta.z, delta.x)) - YAW_OFFSET));
    }

    public boolean shouldAttack(LivingEntity target, double maxDistance, boolean raycastCheck, boolean onlySpace) {
        if (target == null || mc.player == null || mc.world == null) {
            return false;
        }

        if (mc.player.distanceTo(target) > maxDistance) {
            ticksSinceLastAttack = 0;
            return false;
        }

        if (System.currentTimeMillis() < nextAttackTimeMs) {
            return false;
        }

        if (!canAttackByCooldownAndCrits(onlySpace)) {
            return false;
        }

        if (raycastCheck && !rayTraceEntity(aimYaw, aimPitch, maxDistance, target)) {
            if (++ticksSinceLastAttack >= STUCK_RECOVERY_TICKS) {
                recoverFromStuckState();
            }
            return false;
        }

        ticksSinceLastAttack = 0;
        return true;
    }

    /**
     * Stops sprint before attack and waits one tick so the server accepts the hit.
     */
    public boolean prepareSprintReset() {
        if (mc.player == null) {
            return true;
        }

        if (waitingAfterSprintReset) {
            waitingAfterSprintReset = false;
            return true;
        }

        boolean inFluid = mc.player.isTouchingWater()
                || mc.player.isInLava()
                || mc.player.isSwimming()
                || mc.player.isGliding();

        if (!inFluid && mc.player.isSprinting()) {
            zeroMovementNextTick = true;
            waitingAfterSprintReset = true;
            mc.player.setSprinting(false);
            if (mc.getNetworkHandler() != null) {
                mc.getNetworkHandler().sendPacket(
                        new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.STOP_SPRINTING)
                );
            }
            return false;
        }

        return true;
    }

    public boolean consumeZeroMovement() {
        if (zeroMovementNextTick) {
            zeroMovementNextTick = false;
            return true;
        }
        return false;
    }

    public void onAttack() {
        nextAttackTimeMs = System.currentTimeMillis() + ATTACK_DELAY_MS;
        cooldownThreshold = MathUtil.random(0.88F, 0.94F);
        ticksSinceLastAttack = 0;
        waitingAfterSprintReset = false;
    }

    public void onExternalAttack() {
        waitingAfterSprintReset = false;
        zeroMovementNextTick = false;
        ticksSinceLastAttack = 0;
    }

    private void recoverFromStuckState() {
        nextAttackTimeMs = 0L;
        waitingAfterSprintReset = false;
        zeroMovementNextTick = false;
        ticksSinceLastAttack = 0;
    }

    public static Vec3d getAimDelta(Entity target) {
        float tickDelta = mc.getRenderTickCounter().getTickDelta(true);
        Vec3d camera = mc.player.getCameraPosVec(tickDelta);
        Vec3d closest = clampToBox(camera, target.getBoundingBox());
        return closest.subtract(camera);
    }

    private static Vec3d clampToBox(Vec3d point, Box box) {
        return new Vec3d(
                MathHelper.clamp(point.x, box.minX, box.maxX),
                MathHelper.clamp(point.y, box.minY, box.maxY),
                MathHelper.clamp(point.z, box.minZ, box.maxZ)
        );
    }

    private boolean canAttackByCooldownAndCrits(boolean onlySpace) {
        float progress = mc.player.getAttackCooldownProgress(COOLDOWN_PROGRESS_BASE);
        if (progress < cooldownThreshold) {
            return false;
        }

        boolean bypassOnlyCrits = !canCritPose();
        boolean jumpNotPressed = onlySpace && !mc.options.jumpKey.isPressed();

        if (!jumpNotPressed && mc.player.isOnGround()) {
            return false;
        }

        boolean falling = mc.player.fallDistance > 0.0F;
        return bypassOnlyCrits || jumpNotPressed || falling;
    }

    /** Minced {@code EntityPoseUtil.method3146}. */
    private static boolean canCritPose() {
        if (mc.player == null || mc.world == null) {
            return false;
        }
        if (mc.player.isInLava()
                || mc.player.hasVehicle() && mc.player.getVehicle() instanceof AbstractHorseEntity
                || mc.player.isClimbing()
                || mc.player.hasStatusEffect(StatusEffects.BLINDNESS)
                || mc.player.hasStatusEffect(StatusEffects.SLOW_FALLING)
                || mc.player.getAbilities().flying) {
            return false;
        }
        if (mc.player.isOnGround() && mc.player.getPose() == EntityPose.GLIDING) {
            return false;
        }
        if (mc.player.isOnGround()
                && !mc.world.isSpaceEmpty(mc.player, mc.player.getBoundingBox().expand(0.0, 0.1, 0.0))) {
            return false;
        }
        return !mc.player.isSubmergedInWater()
                && (!mc.player.isTouchingWater() || !mc.player.isOnGround());
    }

    public static boolean rayTraceEntity(float yaw, float pitch, double distance, Entity entity) {
        if (mc.player == null || entity == null) {
            return false;
        }
        float tickDelta = mc.getRenderTickCounter().getTickDelta(false);
        Vec3d eyeVec = mc.player.getCameraPosVec(tickDelta);
        Vec3d lookVec = mc.player.getRotationVector(pitch, yaw);
        Vec3d endVec = eyeVec.add(lookVec.multiply(distance));
        Box entityBox = entity.getBoundingBox();
        return entityBox.contains(eyeVec) || entityBox.raycast(eyeVec, endVec).isPresent();
    }
}
