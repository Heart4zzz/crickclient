package zov.crickclient.util.player.combat.minced.modes;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import zov.crickclient.module.list.combat.KillAura;
import zov.crickclient.util.player.combat.minced.MincedAimRotation;
import zov.crickclient.util.player.combat.minced.MincedAuraMode;
import zov.crickclient.util.player.combat.minced.MincedRotationTarget;
import zov.crickclient.util.rotation.Rotation;

import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

public final class LegitMode implements MincedAuraMode {
    private static final String NAME = "Legit";
    private static final float ROTATION_SPEED = 180.0F;

    private float yawProgress;
    private float pitchProgress;

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void updateRotation(KillAura aura, LivingEntity target) {
        Vec3d camera = mc.player.getCameraPosVec(1.0F);
        Vec3d lookEnd = camera.add(mc.player.getRotationVec(1.0F).multiply(999.0D));
        Box aimBox = getAimBox(target);
        Optional<Vec3d> hit = aimBox.raycast(camera, lookEnd);
        boolean onTarget = aimBox.contains(camera) || hit.isPresent();

        if (onTarget) {
            yawProgress = MathHelper.clamp(yawProgress - ThreadLocalRandom.current().nextFloat(0.005F, 0.02F), 0.0F, 1.0F);
            pitchProgress = MathHelper.clamp(pitchProgress - ThreadLocalRandom.current().nextFloat(0.005F, 0.02F), 0.0F, 1.0F);
        } else if (mc.player.isGliding()) {
            yawProgress = MathHelper.clamp(yawProgress + ThreadLocalRandom.current().nextFloat(0.00009F, 0.009F), 0.0F, 1.0F);
            pitchProgress = MathHelper.clamp(pitchProgress + ThreadLocalRandom.current().nextFloat(0.0009F, 0.009F), 0.0F, 1.0F);
        } else if (target.isInSwimmingPose()) {
            yawProgress = MathHelper.clamp(yawProgress + ThreadLocalRandom.current().nextFloat(0.00009F, 0.009F), 0.0F, 1.0F);
            pitchProgress = MathHelper.clamp(pitchProgress + ThreadLocalRandom.current().nextFloat(0.00009F, 0.0009F), 0.0F, 1.0F);
        } else {
            yawProgress = MathHelper.clamp(yawProgress + ThreadLocalRandom.current().nextFloat(0.00009F, 0.009F), 0.0F, 1.0F);
            pitchProgress = MathHelper.clamp(pitchProgress + ThreadLocalRandom.current().nextFloat(0.0009F, 0.009F), 0.0F, 1.0F);
        }

        Vec2f targetRot = MincedRotationTarget.rotationTo(target.getBoundingBox().getCenter());
        float yaw = mc.player.getYaw() + MathHelper.wrapDegrees(targetRot.x - mc.player.getYaw()) * yawProgress;
        float pitch = mc.player.getPitch() + MathHelper.wrapDegrees(targetRot.y - mc.player.getPitch()) * pitchProgress;
        MincedAimRotation.rotate(new Rotation(yaw, pitch), ROTATION_SPEED, 1, 6);
    }

    @Override
    public void reset(KillAura aura) {
        yawProgress = 0.0F;
        pitchProgress = 0.0F;
    }

    @Override
    public void onDeactivate(KillAura aura) {
        reset(aura);
    }

    private static Box getAimBox(LivingEntity target) {
        Box box = target.getBoundingBox();
        double insetH = target.isGliding() ? -0.5D : 0.1D;
        double insetV = target.isGliding() ? -0.5D : 0.1D;
        return new Box(
                box.minX + box.getLengthX() * insetH / 2.0D,
                box.minY,
                box.minZ + box.getLengthZ() * insetH / 2.0D,
                box.maxX - box.getLengthX() * insetH / 2.0D,
                box.maxY - box.getLengthY() * insetV,
                box.maxZ - box.getLengthZ() * insetH / 2.0D
        );
    }
}
