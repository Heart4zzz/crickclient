package zov.crickclient.module.list.render;

import zov.crickclient.module.Module;
import zov.crickclient.module.ModuleCategory;
import zov.crickclient.module.ModuleInformation;
import zov.crickclient.module.settings.SliderSetting;

@ModuleInformation(moduleName = "SeeInvisible", moduleDesc = "Делает невидимых игроков полупрозрачными", moduleCategory = ModuleCategory.RENDER)
public class SeeInvisible extends Module {

    private final SliderSetting alpha = new SliderSetting("Прозрачность", 0.5, 0.1, 1.0, 0.05);

    public float getAlpha() {
        return alpha.getFloatValue();
    }

    // Слайдер близко к максимуму — рисуем игрока полностью (непрозрачно),
    // иначе используем ванильный полупрозрачный путь для невидимых сущностей.
    public boolean isOpaque() {
        return alpha.getFloatValue() >= 0.9f;
    }
}
