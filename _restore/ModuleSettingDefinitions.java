package zov.crickclient.module;

import zov.crickclient.module.settings.BooleanSetting;
import zov.crickclient.module.settings.ModeSetting;
import zov.crickclient.module.settings.SliderSetting;
import zov.crickclient.util.text.ValueUnit;

public final class ModuleSettingDefinitions {
    private ModuleSettingDefinitions() {
    }

    public static ModeSetting killAuraRotation() {
        return new ModeSetting("Обход", "Spooky Test", "ReallyWorld", "Spooky Test", "Legit", "Matrix");
    }

    public static BooleanSetting killAuraOnlySpace() {
        return new BooleanSetting("Только с пробелом", true);
    }

    public static BooleanSetting killAuraClientLook() {
        return new BooleanSetting("Клиент лук", false);
    }

    public static ModeSetting killAuraSprintReset() {
        return new ModeSetting("Сброс спринта", "Spooky Test", "Spooky Test", "ReallyWorld", "Legit", "Matrix");
    }

    public static ModeSetting autoSwapFrom() {
        return new ModeSetting("Первый предмет", "Талисман", "Талисман", "Гепл", "Щит", "Шар");
    }

    public static ModeSetting autoSwapTo() {
        return new ModeSetting("Второй предмет", "Гепл", "Талисман", "Гепл", "Щит", "Шар");
    }

    public static BooleanSetting autoTpOnlyFriends() {
        return new BooleanSetting("Только друзья", true);
    }

    public static SliderSetting clientSoundsVolume() {
        return new SliderSetting("Громкость", ValueUnit.percent(), 70.0F, 0.0F, 100.0F, 1.0F);
    }
}
