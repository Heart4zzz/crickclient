package zov.crickclient.util.player.combat;

import lombok.experimental.UtilityClass;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Items;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import zov.crickclient.util.IMinecraft;
import zov.crickclient.util.render.math.GCDFixer;
import zov.crickclient.util.render.math.MathUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@UtilityClass
public class FunTimeAuraUtil implements IMinecraft {

    public double squaredDistanceToEntity(Vec3d eye, Entity entity) {
        Box box = entity.getBoundingBox();
        double cx = MathHelper.clamp(eye.x, box.minX, box.maxX);
        double cy = MathHelper.clamp(eye.y, box.minY, box.maxY);
        double cz = MathHelper.clamp(eye.z, box.minZ, box.maxZ);
        double dx = cx - eye.x;
        double dy = cy - eye.y;
        double dz = cz - eye.z;
        return (dx * dx) + (dy * dy) + (dz * dz);
    }

    public boolean isLookingAt(float yaw, float pitch, double distance, Entity entity, boolean throughWalls) {
        if (mc.player == null || mc.world == null) {
            return false;
        }
        return isLookingAt(mc.player.getEyePos(), yaw, pitch, distance, entity, throughWalls);
    }

    public boolean isLookingAt(Vec3d rayOrigin, float yaw, float pitch, double distance, Entity entity, boolean throughWalls) {
        if (mc.player == null || mc.world == null) {
            return false;
        }
        Vec3d dir = Vec3d.fromPolar(pitch, yaw).multiply(distance);
        var hit = entity.getBoundingBox().contains(rayOrigin)
                ? java.util.Optional.of(rayOrigin)
                : entity.getBoundingBox().raycast(rayOrigin, rayOrigin.add(dir));
        if (hit.isEmpty()) {
            return false;
        }
        if (!throughWalls && mc.world.raycast(new RaycastContext(
                rayOrigin, hit.get(), RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, mc.player
        )).getType() != HitResult.Type.MISS) {
            return false;
        }
        return true;
    }

    public boolean isWithinReach(Entity entity, double maxReach) {
        if (mc.player == null) {
            return false;
        }
        return squaredDistanceToEntity(mc.player.getEyePos(), entity) <= maxReach * maxReach;
    }

    public Vec3d getAimOffset(Vec3d eye, LivingEntity target, double reach, boolean throughWalls) {
        Box bb = target.getBoundingBox();
        boolean mace = mc.player != null && mc.player.getMainHandStack().isOf(Items.MACE);
        Vec3d aimEye = (!mace || mc.player == null) ? eye : eye.add(mc.player.getVelocity());
        double mx = (bb.minX + bb.maxX) * 0.5D;
        double mz = (bb.minZ + bb.maxZ) * 0.5D;
        Vec3d targetEye = target.getPos().add(0.0D, target.getStandingEyeHeight(), 0.0D);
        double distToTargetEye = aimEye.distanceTo(targetEye);
        Vec3d aimOrigin = aimEye;
        if (mace && distToTargetEye > 3.0D) {
            aimOrigin = new Vec3d(aimEye.x, targetEye.y, aimEye.z);
        }
        double blendDist = mace ? Math.min(distToTargetEye, 3.0D) : distToTargetEye;
        double aimHeight = aimEye.y;
        if (mace && distToTargetEye > 3.0D) {
            aimHeight = targetEye.y;
        }
        double ay = MathHelper.lerp(MathHelper.clamp(blendDist / 3.0D, 0.0D, 1.0D), bb.minY, MathHelper.clamp(aimHeight, bb.minY, bb.maxY));
        Vec3d ideal = new Vec3d(mx, ay, mz);

        List<Vec3d> points = new ArrayList<>();
        points.add(ideal);
        double[] t = {0.0D, 0.125D, 0.25D, 0.375D, 0.5D, 0.625D, 0.75D, 0.875D, 1.0D};
        int last = t.length - 1;
        for (int a = 0; a < t.length; a++) {
            for (int b = 0; b < t.length; b++) {
                for (int c = 0; c < t.length; c++) {
                    if (a == 0 || a == last || b == 0 || b == last || c == 0 || c == last) {
                        points.add(new Vec3d(
                                MathHelper.lerp(t[a], bb.minX, bb.maxX),
                                MathHelper.lerp(t[b], bb.minY, bb.maxY),
                                MathHelper.lerp(t[c], bb.minZ, bb.maxZ)
                        ));
                    }
                }
            }
        }

        for (double pad : new double[]{0.0D, 0.20000001551382535D}) {
            List<Vec3d> visible = collectAimPoints(points, aimOrigin, reach, pad, mace, target, false);
            if (!visible.isEmpty()) {
                return pickClosestToCentroid(visible, aimOrigin);
            }
            if (throughWalls) {
                List<Vec3d> through = collectAimPoints(points, aimOrigin, reach, pad, mace, target, true);
                if (!through.isEmpty()) {
                    return pickClosestToCentroid(through, aimOrigin);
                }
            }
        }
        return Vec3d.ZERO;
    }

    private List<Vec3d> collectAimPoints(List<Vec3d> points, Vec3d aimOrigin, double reach, double pad,
                                         boolean mace, LivingEntity target, boolean throughWalls) {
        List<Vec3d> result = new ArrayList<>();
        for (Vec3d point : points) {
            Vec3d delta = point.subtract(aimOrigin);
            double len = delta.length();
            double limit = reach + pad;
            if (mace || len <= limit) {
                float traceDist = (float) (mace ? len + pad + 0.010000001417203743D : limit);
                float yaw = (float) MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(delta.z, delta.x)) - 90.0D);
                float pitch = (float) (-Math.toDegrees(Math.atan2(delta.y, Math.hypot(delta.x, delta.z))));
                if (isLookingAt(aimOrigin, yaw, pitch, traceDist, target, throughWalls)) {
                    result.add(point);
                }
            }
        }
        return result;
    }

    private Vec3d pickClosestToCentroid(List<Vec3d> points, Vec3d aimOrigin) {
        Vec3d centroid = points.stream().reduce(Vec3d.ZERO, Vec3d::add).multiply(1.0D / points.size());
        return points.stream()
                .min(Comparator.comparingDouble(point -> point.squaredDistanceTo(centroid)))
                .orElse(centroid)
                .subtract(aimOrigin);
    }

    public boolean shouldPrepareCrit(int ticksSinceAttack, LivingEntity target, boolean skipChecks, double distance) {
        if (!skipChecks && ticksSinceAttack >= 7 && squaredDistanceToEntity(mc.player.getEyePos(), target) <= distance * distance
                && mc.player.getAttackCooldownProgress(0.5F) > 0.7F) {
            return willLandCrit();
        }
        return false;
    }

    public boolean willLandCrit() {
        if (mc.player == null || mc.world == null) {
            return false;
        }
        double dy = (mc.player.getVelocity().y - 0.08000000049877275D) * 0.9799995837206814D;
        if (dy >= 0.0D) {
            return false;
        }
        Box moved = mc.player.getBoundingBox().offset(0.0D, dy, 0.0D);
        Box feet = new Box(moved.minX, moved.minY - 0.010000001417203743D, moved.minZ, moved.maxX, moved.minY, moved.maxZ);
        return mc.world.isSpaceEmpty(feet);
    }

    public float rotateStep(float start, float end, float amount) {
        float alpha = MathHelper.clamp(amount, 0.0F, 1.0F);
        float delta = MathHelper.wrapDegrees(end - start);
        if (Math.abs(delta) < 0.5F) {
            return end;
        }
        float stepped = MathHelper.wrapDegrees(start + (delta * alpha));
        float gcd = GCDFixer.getGCDValue();
        if (gcd > 0.0F) {
            stepped = start + (float) Math.round((stepped - start) / gcd) * gcd;
        }
        float remaining = MathHelper.wrapDegrees(end - stepped);
        return Math.abs(remaining) < 0.5F ? end : stepped;
    }

    public float random(float min, float max) {
        return MathUtil.random(min, max);
    }
}
