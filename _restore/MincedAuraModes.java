package zov.crickclient.util.player.combat.minced;

import net.minecraft.entity.LivingEntity;
import zov.crickclient.module.list.combat.KillAura;
import zov.crickclient.util.player.combat.minced.modes.LegitMode;
import zov.crickclient.util.player.combat.minced.modes.MatrixMode;
import zov.crickclient.util.player.combat.minced.modes.ReallyWorldMode;
import zov.crickclient.util.player.combat.minced.modes.SpookyTestMode;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MincedAuraModes {
    private final Map<String, MincedAuraMode> modes = new HashMap<>();
    private String activeModeName;

    public MincedAuraModes() {
        register(new SpookyTestMode());
        register(new MatrixMode());
        register(new LegitMode());
        register(new ReallyWorldMode());
    }

    private void register(MincedAuraMode mode) {
        modes.put(mode.getName(), mode);
    }

    public void activateSelected(KillAura aura) {
        activeModeName = aura.getBypassMode().getValue();
        MincedAuraMode mode = modes.get(activeModeName);
        if (mode != null) {
            mode.onActivate(aura);
        }
    }

    public void deactivateCurrent(KillAura aura) {
        MincedAuraMode mode = modes.get(activeModeName);
        if (mode != null) {
            mode.onDeactivate(aura);
        }
        activeModeName = null;
    }

    public void resetSelected(KillAura aura) {
        MincedAuraMode mode = getSelected(aura);
        if (mode != null) {
            mode.reset(aura);
        }
    }

    public void updateRotation(KillAura aura, LivingEntity target) {
        MincedAuraMode mode = getSelected(aura);
        if (mode != null && target != null) {
            mode.updateRotation(aura, target);
        }
    }

    public void beforeAttack(KillAura aura, LivingEntity target) {
        MincedAuraMode mode = getSelected(aura);
        if (mode != null && target != null) {
            mode.beforeAttack(aura, target);
        }
    }

    public void notifyAttack(KillAura aura, LivingEntity target) {
        MincedAuraMode mode = getSelected(aura);
        if (mode != null && target != null) {
            mode.onAttack(aura, target);
        }
    }

    public boolean isSelectedReady(KillAura aura, LivingEntity target) {
        MincedAuraMode mode = getSelected(aura);
        return mode != null && target != null && mode.isReadyToAttack(aura, target);
    }

    private MincedAuraMode getSelected(KillAura aura) {
        String selected = aura.getBypassMode().getValue();
        if (!Objects.equals(activeModeName, selected)) {
            MincedAuraMode previous = modes.get(activeModeName);
            if (previous != null) {
                previous.reset(aura);
            }
            activeModeName = selected;
            MincedAuraMode next = modes.get(selected);
            if (next != null) {
                next.onActivate(aura);
            }
        }
        return modes.get(selected);
    }
}
