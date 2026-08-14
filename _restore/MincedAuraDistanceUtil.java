package zov.crickclient.util.player.combat.minced;

import zov.crickclient.module.list.combat.KillAura;
import zov.crickclient.util.IMinecraft;

public final class MincedAuraDistanceUtil implements IMinecraft {
    private final MincedEntityPoseUtil poseUtil = new MincedEntityPoseUtil();

    public boolean canAttack(KillAura aura) {
        if (mc.player == null || mc.world == null) {
            return false;
        }
        boolean onlyCrits = aura.isOnlyCritsEnabled();
        boolean onlyWithSpace = aura.getOnlySpace().getValue();
        boolean tpsSync = aura.getTpsSync().getValue();
        return canAttack(onlyCrits, onlyWithSpace, tpsSync, aura.getCooldownThreshold());
    }

    public boolean canAttack(boolean onlyCrits, boolean onlyWithSpace, boolean tpsSync, float threshold) {
        if (mc.player == null || mc.world == null) {
            return false;
        }

        boolean bypassOnlyCrits = !onlyCrits || !poseUtil.canCritPose();
        boolean jumpNotPressed = onlyWithSpace && !mc.options.jumpKey.isPressed();
        float progress = mc.player.getAttackCooldownProgress(tpsSync ? 1.0F : 0.5F);
        if (progress < threshold) {
            return false;
        }

        if (!jumpNotPressed && mc.player.isOnGround() && onlyCrits) {
            return false;
        }

        boolean falling = mc.player.fallDistance > 0.0F;
        if (onlyCrits && !jumpNotPressed) {
            falling = falling
                    || mc.player.isOnGround() && mc.world.isSpaceEmpty(
                            mc.player, mc.player.getBoundingBox().offset(0.0, -0.08, 0.0))
                    || mc.player.getVelocity().y < -0.05;
        }

        return bypassOnlyCrits || jumpNotPressed || falling;
    }
}
