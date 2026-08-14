package zov.crickclient.util.player.combat.minced;

import net.minecraft.entity.LivingEntity;
import zov.crickclient.module.list.combat.KillAura;

public interface MincedAuraMode {
    String getName();

    default void onActivate(KillAura aura) {
    }

    default void onDeactivate(KillAura aura) {
    }

    default void reset(KillAura aura) {
    }

    default void beforeAttack(KillAura aura, LivingEntity target) {
    }

    default void onAttack(KillAura aura, LivingEntity target) {
    }

    default boolean isReadyToAttack(KillAura aura, LivingEntity target) {
        return false;
    }

    void updateRotation(KillAura aura, LivingEntity target);
}
