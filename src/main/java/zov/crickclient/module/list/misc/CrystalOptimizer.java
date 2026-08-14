package zov.crickclient.module.list.misc;

import com.google.common.eventbus.Subscribe;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import zov.crickclient.event.list.EventAttack;
import zov.crickclient.module.Module;
import zov.crickclient.module.ModuleCategory;
import zov.crickclient.module.ModuleInformation;

@ModuleInformation(moduleName = "Crystal Optimizer", moduleDesc = "Оптимизация рендера кристаллов", moduleCategory = ModuleCategory.MISC)
public class CrystalOptimizer extends Module {
    @Subscribe
    private void onAttack(EventAttack e) {
        if (e.getEntity() instanceof EndCrystalEntity entity) {
            entity.remove(Entity.RemovalReason.DISCARDED);
        }
    }
}