package zov.crickclient.util.player.combat.minced;

import lombok.experimental.UtilityClass;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import zov.crickclient.util.IMinecraft;

@UtilityClass
public class MincedRotationTarget implements IMinecraft {
    private static final double GCD_SCALE = 0.15;
    private static final double RAD_TO_DEG = 57.29577951308232;
    private static final double YAW_OFFSET = 90.0;

    public static float gcdStep() {
        float step = (float) (getMouseGcd() * GCD_SCALE);
        return Float.isFinite(step) && step > 0.0F ? step : 0.15F;
    }

    public static float fixGcd(float rotation) {
        return Math.round(rotation / gcdStep()) * gcdStep();
    }

    public static Vec3d getAimDelta(Entity entity) {
        float tickDelta = mc.getRenderTickCounter().getTickDelta(true);
        Vec3d camera = mc.player.getCameraPosVec(tickDelta);
        return clampToBox(camera, entity.getBoundingBox()).subtract(camera);
    }

    public static Vec2f rotationTo(Vec3d point) {
        double dx = point.x - mc.player.getX();
        double dy = point.y - mc.player.getEyeY();
        double dz = point.z - mc.player.getZ();
        double horizontal = MathHelper.sqrt((float) (dx * dx + dz * dz));
        float yaw = (float) (MathHelper.atan2(dz, dx) * RAD_TO_DEG - YAW_OFFSET);
        float pitch = (float) (-MathHelper.atan2(dy, horizontal) * RAD_TO_DEG);
        return new Vec2f(yaw, pitch);
    }

    public static float getFocusYaw(Entity entity) {
        Vec3d delta = getAimDelta(entity);
        return MathHelper.wrapDegrees((float) (Math.toDegrees(Math.atan2(delta.z, delta.x)) - YAW_OFFSET));
    }

    private static Vec3d clampToBox(Vec3d point, Box box) {
        return new Vec3d(
                MathHelper.clamp(point.x, box.minX, box.maxX),
                MathHelper.clamp(point.y, box.minY, box.maxY),
                MathHelper.clamp(point.z, box.minZ, box.maxZ)
        );
    }

    private static float getMouseGcd() {
        double value = mc.options.getMouseSensitivity().getValue() / 0.15D / 8.0D;
        double cubeRoot = Math.cbrt(value);
        float sensitivity = (float) ((cubeRoot - 0.2D) / 0.6D * 0.6D + 0.2D);
        return sensitivity * sensitivity * sensitivity * 8.0F;
    }
}
