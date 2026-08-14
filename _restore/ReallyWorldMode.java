package zov.crickclient.util.player.combat.minced.modes;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import zov.crickclient.module.list.combat.KillAura;
import zov.crickclient.util.player.combat.minced.MincedAimRotation;
import zov.crickclient.util.player.combat.minced.MincedAuraMode;
import zov.crickclient.util.player.combat.minced.MincedRotationTarget;
import zov.crickclient.util.rotation.Rotation;

import java.util.concurrent.ThreadLocalRandom;

public final class ReallyWorldMode implements MincedAuraMode {
    private static final String NAME = "ReallyWorld";

    private float smoothYawStep;
    private float smoothPitchStep;
    private int pauseTicks;

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void reset(KillAura aura) {
        smoothYawStep = 0.0F;
        smoothPitchStep = 0.0F;
        pauseTicks = 0;
    }

    @Override
    public void updateRotation(KillAura aura, LivingEntity target) {
        Vec3d camera = mc.player.getCameraPosVec(mc.getRenderTickCounter().getTickDelta(false));
        Vec3d velocity = new Vec3d(target.getX() - target.prevX, target.getY() - target.prevY, target.getZ() - target.prevZ);
        Vec3d aimPoint = target.getPos()
                .add(0.0, target.getHeight() * 0.55D, 0.0)
                .add(velocity.multiply(1.0D));
        Vec3d delta = aimPoint.subtract(camera);
        if (delta.lengthSquared() < 1.0E-4D) {
            return;
        }

        float targetYaw = (float) MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(delta.z, delta.x)) - 90.0D);
        float targetPitch = MathHelper.clamp(
                (float) (-Math.toDegrees(Math.atan2(delta.y, Math.hypot(delta.x, delta.z)))),
                -89.9F,
                89.9F
        );

        float yawDelta = MathHelper.wrapDegrees(targetYaw - mc.player.getYaw());
        float pitchDelta = MathHelper.wrapDegrees(targetPitch - mc.player.getPitch());
        if (Math.abs(yawDelta) < 2.0F) {
            yawDelta = 0.0F;
        }
        if (Math.abs(pitchDelta) < 1.0F) {
            pitchDelta = 0.0F;
        }

        float yawStep = yawDelta * 0.62F;
        float pitchStep = pitchDelta * 0.45F;
        yawStep = MathHelper.clamp(yawStep, -45.0F, 45.0F);
        pitchStep = MathHelper.clamp(pitchStep, -35.0F, 35.0F);

        if (pauseTicks > 0) {
            yawStep *= 0.35F;
            pitchStep *= 0.35F;
            pauseTicks--;
        } else if (Math.abs(yawDelta) < 2.5F && Math.abs(pitchDelta) < 2.5F
                && ThreadLocalRandom.current().nextFloat() < 0.35F) {
            pauseTicks = ThreadLocalRandom.current().nextInt(1, 4);
        }

        if (ThreadLocalRandom.current().nextFloat() < 0.12F) {
            yawStep += ThreadLocalRandom.current().nextFloat(-1.5F, 1.5F);
        }
        if (ThreadLocalRandom.current().nextFloat() < 0.10F) {
            pitchStep += ThreadLocalRandom.current().nextFloat(-1.0F, 1.0F);
        }

        smoothYawStep = MathHelper.lerp(0.35F, smoothYawStep, yawStep);
        smoothPitchStep = MathHelper.lerp(0.35F, smoothPitchStep, pitchStep);
        float gcd = MincedRotationTarget.gcdStep();
        smoothYawStep = snapGcd(smoothYawStep, gcd);
        smoothPitchStep = snapGcd(smoothPitchStep, gcd);

        float yaw = mc.player.getYaw() + smoothYawStep;
        float pitch = MathHelper.clamp(mc.player.getPitch() + smoothPitchStep, -89.9F, 89.9F);
        MincedAimRotation.rotate(new Rotation(yaw, pitch), 360.0F, 360.0F, 3.0F, 3.0F, 6, 1, aura.getClientLook().getValue());
        aura.lastYaw = yaw;
        aura.lastPitch = pitch;
    }

    private static float snapGcd(float value, float gcd) {
        if (gcd <= 0.0F) {
            return value;
        }
        return Math.round(value / gcd) * gcd;
    }
}
