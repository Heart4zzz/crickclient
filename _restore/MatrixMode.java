package zov.crickclient.util.player.combat.minced.modes;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import zov.crickclient.module.list.combat.KillAura;
import zov.crickclient.util.player.combat.minced.MincedAimRotation;
import zov.crickclient.util.player.combat.minced.MincedAuraMode;
import zov.crickclient.util.player.combat.minced.MincedRotationTarget;
import zov.crickclient.util.rotation.Rotation;

public final class MatrixMode implements MincedAuraMode {
    private static final String NAME = "Matrix";

    private boolean reversing;
    private float progress;
    private float currentYaw;
    private float currentPitch;

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void onActivate(KillAura aura) {
        reset(aura);
    }

    @Override
    public void reset(KillAura aura) {
        reversing = false;
        progress = 0.0F;
        if (mc.player != null) {
            currentYaw = mc.player.getYaw();
            currentPitch = MathHelper.clamp(mc.player.getPitch(), -40.0F, 40.0F);
        }
    }

    @Override
    public void updateRotation(KillAura aura, LivingEntity target) {
        Box box = target.getBoundingBox();
        Vec3d camera = mc.player.getCameraPosVec(1.0F);
        Vec3d delta = box.getCenter().subtract(camera);
        float targetYaw = (float) MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(delta.z, delta.x)) - 90.0D);
        float targetPitch = MathHelper.clamp(
                (float) (-Math.toDegrees(Math.atan2(delta.y, Math.hypot(delta.x, delta.z)))),
                -40.0F,
                40.0F
        );

        Vec3d lookEnd = camera.add(mc.player.getRotationVector(mc.player.getPitch(), mc.player.getYaw()).multiply(999.0D));
        boolean looking = box.expand(-0.5D).raycast(camera, lookEnd).isPresent();

        if (reversing) {
            if (progress >= -0.01F) {
                progress -= Math.abs(MathHelper.wrapDegrees(targetYaw - currentYaw)) > 80.0F ? 0.055F : 0.0055F;
            }
            if (progress <= -0.01F) {
                reversing = false;
            }
        } else {
            progress += 0.0034F;
            if (progress >= 0.36F || looking) {
                reversing = true;
            }
        }

        float yawDelta = MathHelper.wrapDegrees(targetYaw - currentYaw);
        float pitchDelta = targetPitch - currentPitch;
        float factor = Math.max(progress, 0.0F);
        float nextYaw = currentYaw + yawDelta * MathHelper.clamp(factor * 1.3F, 0.0F, 1.0F);
        float nextPitch = currentPitch + pitchDelta * MathHelper.clamp(factor / 1.7F, 0.0F, 1.0F);
        float gcd = MincedRotationTarget.gcdStep();
        nextYaw -= (nextYaw - currentYaw) % gcd;
        nextPitch -= (nextPitch - currentPitch) % gcd;
        nextPitch = MathHelper.clamp(nextPitch, -40.0F, 40.0F);
        currentYaw = nextYaw;
        currentPitch = nextPitch;
        MincedAimRotation.rotate(new Rotation(nextYaw, nextPitch), 45.0F, 15.0F, 1, 6);
    }
}
