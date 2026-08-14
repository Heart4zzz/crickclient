package zov.crickclient.module.list.player;

import zov.crickclient.module.Module;
import zov.crickclient.module.ModuleCategory;
import zov.crickclient.module.ModuleInformation;
import zov.crickclient.module.settings.BooleanSetting;
import zov.crickclient.module.settings.ModeListSetting;

@ModuleInformation(moduleName = "No Push", moduleDesc = "Убирает толкание от игроков и блоков", moduleCategory = ModuleCategory.PLAYER)
public class NoPush extends Module {
    public final ModeListSetting objects = new ModeListSetting("Обьекты",
            new BooleanSetting("Игроки", true),
            new BooleanSetting("Блоки", true)
    );
}