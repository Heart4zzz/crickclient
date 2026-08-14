package zov.crickclient.util.player.combat.minced;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Items;
import zov.crickclient.module.list.combat.KillAura;
import zov.crickclient.util.IMinecraft;

public final class MincedWeaponUtil implements IMinecraft {
    private final MincedAuraDistanceUtil distanceUtil = new MincedAuraDistanceUtil();

    public boolean canAttackNow(KillAura aura) {
        if (mc.player == null || mc.world == null) {
            return false;
        }
        if (aura.isNoEatEnabled()
                && mc.player.isUsingItem()
                && !mc.player.getOffHandStack().isOf(Items.SHIELD)) {
            return false;
        }
        return distanceUtil.canAttack(aura) && System.currentTimeMillis() >= aura.getNextAttackMs();
    }

    public boolean canAttackEntity(KillAura aura, LivingEntity target) {
        return canAttackNow(aura) && target != null;
    }
}
