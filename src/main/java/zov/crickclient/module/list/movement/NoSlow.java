package zov.crickclient.module.list.movement;

import com.google.common.eventbus.Subscribe;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import zov.crickclient.event.list.EventNoSlow;
import zov.crickclient.event.list.EventPlayerUpdate;
import zov.crickclient.module.Module;
import zov.crickclient.module.ModuleCategory;
import zov.crickclient.module.ModuleInformation;
import zov.crickclient.module.settings.BooleanSetting;
import zov.crickclient.module.settings.ModeSetting;
import zov.crickclient.util.base.Instance;

@ModuleInformation(moduleName = "No Slow", moduleDesc = "Убирает замедление при использовании", moduleCategory = ModuleCategory.MOVEMENT)
public class NoSlow extends Module {

    public final ModeSetting mainHand = new ModeSetting("Основная рука", "Vanilla",
            "None", "Grim Ticks", "LonyGrief", "Vanilla")
            .addAlias("Grim", "Grim Ticks");

    public final ModeSetting offHand = new ModeSetting("Вторая рука", "Grim Ticks",
            "None", "Grim Ticks", "Vanilla")
            .addAlias("Grim", "Grim Ticks");

    public final BooleanSetting mainHandSprint = new BooleanSetting("Спринт основной руки", true);
    public final BooleanSetting offHandSprint = new BooleanSetting("Спринт второй руки", true);

    private int mainHandTicks;
    private int offHandTicks;

    // EventNoSlow приходит из @Redirect на isUsingItem() в tickMovement и может
    // сработать несколько раз за один игровой тик. Оригинальный EventSlowDown
    // был ровно один за тик, поэтому решение считаем один раз за тик и
    // переиспользуем его для остальных вызовов — так Grim Ticks остаётся 1 в 1.
    private int tickCounter;
    private int lastDecisionTick = Integer.MIN_VALUE;
    private boolean cancelThisTick;

    @Subscribe
    private void onUpdate(EventPlayerUpdate e) {
        // EventPlayerUpdate постится в HEAD ClientPlayerEntity#tick, т.е. строго до
        // tickMovement, откуда прилетает EventNoSlow — счётчик всегда актуален.
        tickCounter++;

        setSuffix(mainHand.getValue() + " | " + offHand.getValue());

        if (!nullCheck()) return;
        if (!mainHand.is("LonyGrief")) return;

        AirStuck airStuck = Instance.get(AirStuck.class);
        if (airStuck != null && airStuck.isEnabled() && airStuck.isStuck()) return;

        if (!mc.player.isUsingItem()) return;
        if (mc.player.getActiveHand() != Hand.MAIN_HAND) return;
        if (mc.player.getItemUseTime() != 0) return;

        mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(
                PlayerActionC2SPacket.Action.DROP_ALL_ITEMS,
                BlockPos.ORIGIN,
                mc.player.getHorizontalFacing()));
    }

    @Subscribe
    private void onSlowDown(EventNoSlow event) {
        if (!nullCheck()) return;
        if (!mc.player.isUsingItem()) return;

        if (tickCounter != lastDecisionTick) {
            lastDecisionTick = tickCounter;
            cancelThisTick = decide();
        }

        if (cancelThisTick) event.cancelEvent();
    }

    /** Тело оригинального onSlowDown: считает тики и решает, гасить ли замедление. */
    private boolean decide() {
        Hand hand = mc.player.getActiveHand();

        if (mc.player.isUsingItem()) {
            if (hand == Hand.MAIN_HAND) {
                mainHandTicks++;
                if (mainHandSprint.getValue()) mc.player.setSprinting(true);
            } else {
                offHandTicks++;
                if (offHandSprint.getValue()) mc.player.setSprinting(true);
            }
        } else {
            mainHandTicks = 0;
            offHandTicks = 0;
        }

        return hand == Hand.MAIN_HAND ? mainHand() : offHand();
    }

    private boolean mainHand() {
        if (mainHand.is("None")) return false;

        if (mainHand.is("Grim Ticks")) {
            if (mainHandTicks >= 2) {
                mainHandTicks = 0;
                return true;
            }
        } else if (mainHand.is("LonyGrief")) {
            return mc.player.getItemUseTime() > 0;
        } else if (mainHand.is("Vanilla")) {
            return true;
        }
        return false;
    }

    private boolean offHand() {
        if (offHand.is("None")) return false;

        if (offHand.is("Grim Ticks")) {
            if (offHandTicks >= 2) {
                offHandTicks = 0;
                return true;
            }
        } else if (offHand.is("Vanilla")) {
            return true;
        }
        return false;
    }

    @Override
    public void onEnable() {
        super.onEnable();
        reset();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        reset();
        setSuffix(null);
    }

    private void reset() {
        mainHandTicks = 0;
        offHandTicks = 0;
        tickCounter = 0;
        lastDecisionTick = Integer.MIN_VALUE;
        cancelThisTick = false;
    }

    /** true — игрок и мир существуют (аналог nullCheck() из исходника). */
    private boolean nullCheck() {
        return mc.player != null && mc.world != null;
    }
}
