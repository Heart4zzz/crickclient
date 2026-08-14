package zov.crickclient.module.list.movement;

import com.google.common.eventbus.Subscribe;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import zov.crickclient.event.list.EventPlayerUpdate;
import zov.crickclient.module.Module;
import zov.crickclient.module.ModuleCategory;
import zov.crickclient.module.ModuleInformation;
import zov.crickclient.module.settings.ModeSetting;
import zov.crickclient.util.player.move.MoveUtil;
import zov.crickclient.util.player.move.ReallyWorldTimerBypass;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ModuleInformation(moduleName = "Speed", moduleDesc = "Увеличение скорости передвижения", moduleCategory = ModuleCategory.MOVEMENT)
public class Speed extends Module {

    private final ModeSetting mode = new ModeSetting("Mode", "ReallyWorld", "ReallyWorld", "Collision");
    private final Map<Entity, Vec3d> previousPositions = new HashMap<>();

    @Subscribe
    public void onPlayerUpdate(EventPlayerUpdate event) {
        if (!isEnabled()) {
            return;
        }
        if (this.mode.is("Collision")) {
            this.tickCollision();
        }
    }

    private void tickCollision() {
        if (!MoveUtil.isMoving()) {
            return;
        }
        double scanRadius = 1.25;
        Box searchBox = this.mc.player.getBoundingBox().expand(scanRadius);
        List<Entity> nearbyEntities = this.mc.world.getOtherEntities(this.mc.player, searchBox, entity -> entity instanceof PlayerEntity);
        for (Entity entity2 : nearbyEntities) {
            if (!(entity2 instanceof PlayerEntity)) continue;
            double distanceX = Math.abs(this.mc.player.getX() - entity2.getX());
            double distanceZ = Math.abs(this.mc.player.getZ() - entity2.getZ());
            if (distanceX > 2.1 || distanceZ > 1.3) continue;
            double entitySpeed = this.getEntitySpeed(entity2);
            if (entitySpeed < 5.0) {
                double boostAmount = 0.02;
                Box collisionBox = this.mc.player.getBoundingBox().expand(0.1);
                List<Entity> collisionEntities = this.mc.world.getOtherEntities(this.mc.player, collisionBox, e2 -> e2 instanceof PlayerEntity);
                if (collisionEntities.isEmpty()) break;
                double[] motion = this.forward(boostAmount);
                this.mc.player.addVelocity(motion[0], 0.0, motion[1]);
                break;
            }
            double boostAmount = 0.032;
            Box checkBox = this.mc.player.getBoundingBox().expand(1.25);
            List<Entity> potentialCollisions = this.mc.world.getOtherEntities(this.mc.player, checkBox, e2 -> e2 instanceof PlayerEntity);
            int collisions = 0;
            for (Entity collisionEntity : potentialCollisions) {
                double distToCollision = this.mc.player.distanceTo(collisionEntity);
                if (!(distToCollision <= 1.25)) continue;
                ++collisions;
            }
            if (collisions <= 0) break;
            double[] motion = this.forward(boostAmount);
            this.mc.player.addVelocity(motion[0], 0.0, motion[1]);
            break;
        }
    }

    public double getEntitySpeed(Entity entity) {
        Vec3d currentPos = entity.getPos();
        Vec3d previousPos = this.previousPositions.getOrDefault(entity, currentPos);
        double dx = currentPos.x - previousPos.x;
        double dz = currentPos.z - previousPos.z;
        double speed = Math.sqrt(dx * dx + dz * dz) * 20.0;
        this.previousPositions.put(entity, currentPos);
        return speed;
    }

    private double[] forward(double speed) {
        float forward = this.mc.player.input.movementForward;
        float strafe = this.mc.player.input.movementSideways;
        float yaw = this.mc.player.getYaw();
        if (forward != 0.0f) {
            if (strafe > 0.0f) {
                yaw += forward > 0.0f ? -45.0f : 45.0f;
            } else if (strafe < 0.0f) {
                yaw += forward > 0.0f ? 45.0f : -45.0f;
            }
            strafe = 0.0f;
            if (forward > 0.0f) {
                forward = 1.0f;
            } else if (forward < 0.0f) {
                forward = -1.0f;
            }
        }
        double sin = Math.sin(Math.toRadians(yaw + 90.0f));
        double cos = Math.cos(Math.toRadians(yaw + 90.0f));
        double posX = (double) forward * speed * cos + (double) strafe * speed * sin;
        double posZ = (double) forward * speed * sin - (double) strafe * speed * cos;
        return new double[]{posX, posZ};
    }

    private void updateReallyWorldBypass() {
        if (isEnabled() && this.mode.is("ReallyWorld")) {
            ReallyWorldTimerBypass.enable();
        } else {
            ReallyWorldTimerBypass.disable();
        }
    }

    @Override
    public void onEnable() {
        updateReallyWorldBypass();
        super.onEnable();
    }

    @Override
    public void onDisable() {
        ReallyWorldTimerBypass.disable();
        super.onDisable();
    }
}
