package zov.crickclient.util.player.combat.minced;

import lombok.experimental.UtilityClass;
import net.minecraft.util.math.MathHelper;
import zov.crickclient.util.rotation.Rotation;
import zov.crickclient.util.rotation.RotationComponent;

@UtilityClass
public class MincedAimRotation {
    public static void rotate(Rotation target, float speed, int priority, int timeout) {
        rotate(target, speed, speed, speed, speed, timeout, priority, false);
    }

    public static void rotate(
            Rotation target,
            float yawSpeed,
            float pitchSpeed,
            float yawReturn,
            float pitchReturn,
            int timeout,
            int priority,
            boolean clientLook
    ) {
        if (target == null) {
            return;
        }
        RotationComponent.update(target, yawSpeed, pitchSpeed, yawReturn, pitchReturn, timeout, priority, clientLook);
    }

    public static void applyGcd(float targetYaw, float targetPitch) {
        if (mc.player == null) {
            return;
        }
        float yawDelta = MathHelper.wrapDegrees(targetYaw - mc.player.getYaw());
        float pitchDelta = targetPitch - mc.player.getPitch();
        float gcd = MincedRotationTarget.gcdStep();
        float yawStep = MincedRotationTarget.fixGcd(MathHelper.clamp(yawDelta, -180.0F, 180.0F));
        float pitchStep = MincedRotationTarget.fixGcd(MathHelper.clamp(pitchDelta, -180.0F, 180.0F));
        mc.player.setYaw(mc.player.getYaw() + yawStep - yawStep % gcd);
        mc.player.setPitch(MathHelper.clamp(
                mc.player.getPitch() + pitchStep - pitchStep % gcd,
                -90.0F,
                90.0F
        ));
    }
}
