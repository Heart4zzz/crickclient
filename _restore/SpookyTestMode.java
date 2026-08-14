package zov.crickclient.util.player.combat.minced.modes;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import zov.crickclient.module.list.combat.KillAura;
import zov.crickclient.util.player.combat.minced.MincedAimRotation;
import zov.crickclient.util.player.combat.minced.MincedAuraMode;
import zov.crickclient.util.player.combat.minced.MincedRotationTarget;
import zov.crickclient.util.render.math.MathUtil;
import zov.crickclient.util.rotation.Rotation;

public final class SpookyTestMode implements MincedAuraMode {
    private static final String NAME = "Spooky Test";
    private static final float YAW_NOISE_MAX = 3.0F;
    private static final float PITCH_OFFSET = 0.2958859F;
    private static final float ROTATION_SPEED = 360.0F;

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void updateRotation(KillAura aura, Entity target) {
        Vec3d delta = MincedRotationTarget.getAimDelta(target);
        float yaw = (float) MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(delta.z, delta.x)) - 90.0D);
        float pitch = (float) (-Math.toDegrees(Math.atan2(delta.y, Math.hypot(delta.x, delta.z))));
        float noiseRange = MathUtil.random(0.0F, YAW_NOISE_MAX);
        float yawNoise = MathUtil.random(-noiseRange, noiseRange);
        MincedAimRotation.rotate(
                new Rotation(yaw + yawNoise, pitch + PITCH_OFFSET),
                ROTATION_SPEED,
                1,
                6
        );
    }
}
