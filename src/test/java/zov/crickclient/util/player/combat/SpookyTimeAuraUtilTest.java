package zov.crickclient.util.player.combat;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class SpookyTimeAuraUtilTest {

    @Test
    void utilCanBeInstantiated() {
        SpookyTimeAuraUtil util = new SpookyTimeAuraUtil();
        assertNotNull(util);
    }

    @Test
    void resetDoesNotThrow() {
        SpookyTimeAuraUtil util = new SpookyTimeAuraUtil();
        util.reset();
        util.reset();
    }
}
