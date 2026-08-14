package zov.crickclient.util.player.combat.minced;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.AbstractHorseEntity;
import zov.crickclient.util.IMinecraft;

public final class MincedEntityPoseUtil implements IMinecraft {
    public boolean canCritPose() {
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
}
