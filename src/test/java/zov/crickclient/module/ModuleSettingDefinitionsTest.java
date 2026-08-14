package zov.crickclient.module;

import org.junit.jupiter.api.Test;
import zov.crickclient.module.settings.ModeSetting;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ModuleSettingDefinitionsTest {

    @Test
    void killAuraRotationContainsLegacyModes() {
        ModeSetting setting = ModuleSettingDefinitions.killAuraRotation();
        assertEquals(List.of("ReallyWorld", "FunTime", "SpookyTime"), setting.getModes());
    }
}
