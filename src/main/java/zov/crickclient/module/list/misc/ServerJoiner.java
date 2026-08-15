package zov.crickclient.module.list.misc;

import com.google.common.eventbus.Subscribe;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.s2c.play.OpenScreenS2CPacket;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import zov.crickclient.event.list.EventPacket;
import zov.crickclient.event.list.EventTick;
import zov.crickclient.module.Module;
import zov.crickclient.module.ModuleCategory;
import zov.crickclient.module.ModuleInformation;
import zov.crickclient.module.settings.ModeSetting;
import zov.crickclient.module.settings.SliderSetting;
import zov.crickclient.ui.ClickGuiFrame;
import zov.crickclient.util.chat.ChatUtil;
import zov.crickclient.util.math.StopWatch;
import zov.crickclient.util.packet.NetworkUtils;
import zov.crickclient.util.player.other.InventoryUtil;
import zov.crickclient.util.server.ServerUtil;

@ModuleInformation(moduleName = "ServerJoiner", moduleDesc = "Автоматически подключается к указанному серверу", moduleCategory = ModuleCategory.MISC)
public class ServerJoiner extends Module {

    private final ModeSetting server = new ModeSetting("Выберите сервер", "SpookyTime", "SpookyTime", "ReallyWorld")
            // Старые конфиги хранили русские названия режимов.
            .addAlias("СпукиТайм дуэли", "SpookyTime")
            .addAlias("РиллиВорлд", "ReallyWorld");

    private final SliderSetting griefNumber = new SliderSetting("Укажите номер грифа (1-54)", 1, 1, 54, 1)
            .setVisible(() -> server.is("ReallyWorld"));

    /** Задержка между кликами по слоту грифа (аналог CounterUtil из исходника). */
    private static final long GRIEF_CLICK_DELAY = 5500L;

    private final StopWatch griefTimer = new StopWatch();

    /** syncId меню «☫ Выберите режим:», перехваченного из OpenScreenS2CPacket. */
    private int modeMenuSyncId = -1;

    @Subscribe
    private void onTick(EventTick ignored) {
        if (mc.player == null || mc.world == null || mc.getNetworkHandler() == null || mc.interactionManager == null) {
            return;
        }

        // Пока открыто меню клиента — ничего не делаем.
        if (mc.currentScreen instanceof ClickGuiFrame) return;

        if (server.is("SpookyTime")) {
            handleSpookyTime();
            return;
        }

        if (server.is("ReallyWorld")) {
            handleReallyWorld();
        }
    }

    private void handleSpookyTime() {
        String tab = ServerUtil.getTabHeader();

        if (tab.contains("Хаб")) {
            useCompass();

            if (modeMenuSyncId != -1) {
                NetworkUtils.sendPacket(new ClickSlotC2SPacket(
                        modeMenuSyncId, 0, 13, 0, SlotActionType.PICKUP, ItemStack.EMPTY, Int2ObjectMaps.emptyMap()));
                modeMenuSyncId = -1;
            }
            return;
        }

        if (!tab.isEmpty() && !tab.contains("Режим: Хаб # ")) {
            ChatUtil.send("Вы находитесь не в хабе SpookyTime, а значит модуль выключается!");
            setEnabled(false);
        }
    }

    private void handleReallyWorld() {
        if (!ServerUtil.getTabHeader().isEmpty()) {
            ChatUtil.send("Вы находитесь не в лобби ReallyWorld, а значит модуль выключается!");
            setEnabled(false);
            return;
        }

        useCompass();

        if (!(mc.currentScreen instanceof GenericContainerScreen screen)) return;

        GenericContainerScreenHandler handler = screen.getScreenHandler();
        String title = screen.getTitle().getString();

        if (title.contains("» Выбор сервера")) {
            NetworkUtils.sendPacket(new ClickSlotC2SPacket(
                    handler.syncId, handler.getRevision(), 21, 0, SlotActionType.PICKUP,
                    handler.getCursorStack().copy(), Int2ObjectMaps.emptyMap()));
        }

        if (!title.contains("Выбор мира грифа ")) return;

        String griefName = "ГРИФ #" + griefNumber.getIntValue() + " (1.16.5+)";
        int containerSlots = Math.min(handler.getRows() * 9, handler.slots.size());

        for (int i = 0; i < containerSlots; i++) {
            Slot slot = handler.slots.get(i);
            if (!slot.getStack().getName().getString().contains(griefName)) continue;

            if (griefTimer.every(GRIEF_CLICK_DELAY)) {
                NetworkUtils.sendPacket(new ClickSlotC2SPacket(
                        handler.syncId, handler.getRevision(), slot.id, 0, SlotActionType.PICKUP,
                        handler.getCursorStack().copy(), Int2ObjectMaps.emptyMap()));
            }
            return;
        }
    }

    /** Берёт компас из хотбара и использует его (только когда никакой экран не открыт). */
    private void useCompass() {
        int compassSlot = InventoryUtil.searchItemHotbar(Items.COMPASS);
        if (compassSlot < 0 || compassSlot > 8 || mc.currentScreen != null) return;

        NetworkUtils.sendPacket(new UpdateSelectedSlotC2SPacket(compassSlot));
        mc.interactionManager.sendSequencedPacket(mc.world, sequence -> new PlayerInteractItemC2SPacket(
                mc.player.getActiveHand(), sequence, mc.player.getYaw(), mc.player.getPitch()));
    }

    @Subscribe
    private void onPacket(EventPacket event) {
        if (!server.is("SpookyTime") || event.getType() != EventPacket.Type.RECEIVE) return;
        if (!(event.getPacket() instanceof OpenScreenS2CPacket packet)) return;

        if (packet.getName().getString().contains("☫ Выберите режим:")) {
            modeMenuSyncId = packet.getSyncId();
        }
        // Меню не показываем игроку — кликаем по нему пакетом.
        event.setCancelled(true);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        modeMenuSyncId = -1;
        // Первый клик по грифу должен уходить сразу, задержка действует только между кликами.
        griefTimer.setTime(0L);
    }

    @Override
    public void onDisable() {
        super.onDisable();
        modeMenuSyncId = -1;
        griefTimer.reset();
    }
}
