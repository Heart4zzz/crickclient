package zov.crickclient.module;

import org.junit.jupiter.api.Test;
import zov.crickclient.module.list.movement.DogFly;
import zov.crickclient.module.list.movement.NoJumpDelay;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ModuleMetadataTest {
    @Test
    void dogFlyKeepsClassButUsesBibFlyDisplayName() {
        var information = DogFly.class.getAnnotation(ModuleInformation.class);

        assertEquals("Bib Fly", information.moduleName());
    }

    @Test
    void noJumpDelayIsMiscModule() {
        var information = NoJumpDelay.class.getAnnotation(ModuleInformation.class);

        assertEquals(ModuleCategory.MISC, information.moduleCategory());
    }
}
