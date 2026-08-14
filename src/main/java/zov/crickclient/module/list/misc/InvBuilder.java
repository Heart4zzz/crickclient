package zov.crickclient.module.list.misc;

import com.google.common.eventbus.Subscribe;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PotionItem;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.OpenScreenS2CPacket;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import org.lwjgl.glfw.GLFW;
import zov.crickclient.event.list.ContainerEvent;
import zov.crickclient.event.list.EventKeyInput;
import zov.crickclient.event.list.EventPacket;
import zov.crickclient.event.list.EventTick;
import zov.crickclient.event.list.MoveInputEvent;
import zov.crickclient.module.Module;
import zov.crickclient.module.ModuleCategory;
import zov.crickclient.module.ModuleInformation;
import zov.crickclient.module.settings.ActionSetting;
import zov.crickclient.ui.collector.InvBuilderScreen;
import zov.crickclient.util.chat.ChatUtil;
import zov.crickclient.util.collector.AuctionPriceUtil;
import zov.crickclient.util.collector.CollectorEntry;
import zov.crickclient.util.collector.CollectorOffer;
import zov.crickclient.util.collector.CollectorStorage;
import zov.crickclient.util.collector.JitterTimer;
import zov.crickclient.util.render.math.MathUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

@ModuleInformation(moduleName = "InvBuilder", moduleDesc = "Автоматически покупает нужный инвентарь на аукционе", moduleCategory = ModuleCategory.MISC)
public class InvBuilder extends Module {

    private final List<CollectorEntry> catalog = CollectorStorage.getInstance().getEntries();
    private final List<CollectorOffer> scannedOffers = new ArrayList<>();
    private final JitterTimer actionTimer = new JitterTimer();
    private final JitterTimer openTimer = new JitterTimer();

    private CollectorEntry currentEntry;
    private CollectorOffer selectedOffer;
    private int searchTicks;
    private boolean waitingForSpawn;

    private final ActionSetting openList = new ActionSetting("Список предметов", () ->
            mc.setScreen(new InvBuilderScreen(mc.currentScreen)));

    private final ActionSetting startPurchase = new ActionSetting("Начать закупку", () -> {
        if (!isEnabled()) {
            setEnabled(true);
        }
        advanceTarget();
        if (currentEntry == null) {
            setEnabled(false);
        }
    });

    public boolean isWorking() {
        return currentEntry != null;
    }

    @Override
    public void onEnable() {
        ChatUtil.send("Модуль ищет самые дешёвые лоты среди доступных — будьте осторожны с балансом.");
        advanceTarget();
    }

    @Override
    public void onDisable() {
        stopWork();
    }

    private void advanceTarget() {
        int start = currentEntry == null ? 0 : catalog.indexOf(currentEntry) + 1;
        selectedOffer = null;
        scannedOffers.clear();
        waitingForSpawn = false;

        if (catalog.stream().noneMatch(CollectorEntry::isEnabled)) {
            ChatUtil.send("Нет включённых предметов — откройте «Список предметов» и включите нужные");
            stopWork();
            return;
        }

        currentEntry = IntStream.range(start, catalog.size())
                .mapToObj(catalog::get)
                .filter(CollectorEntry::isEnabled)
                .filter(entry -> countOwned(entry) < requiredCount(entry, false))
                .findFirst()
                .orElse(null);

        if (currentEntry != null) {
            actionTimer.reset();
            openTimer.reset();
            searchTicks = 0;
            ChatUtil.send("Собираю: " + currentEntry.getDisplayName());
        } else {
            ChatUtil.send("Все включённые предметы уже собраны");
            stopWork();
        }
    }

    public static void openAuctionSearch(CollectorEntry entry) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (entry == null || client.player == null || client.getNetworkHandler() == null) {
            return;
        }
        client.player.networkHandler.sendChatCommand("ah search " + entry.getDisplayName());
        ChatUtil.send("Поиск на аукционе: " + entry.getDisplayName());
    }

    @Subscribe
    private void onTick(EventTick ignored) {
        if (currentEntry == null || mc.player == null) {
            return;
        }

        if (mc.player.getInventory().getEmptySlot() == -1) {
            ChatUtil.send("Нет свободных слотов — освободите инвентарь");
            stopWork();
            setEnabled(false);
            return;
        }

        if (mc.player.age < 200) {
            if (!waitingForSpawn) {
                waitingForSpawn = true;
                ChatUtil.send("Ожидаю загрузку игрока, затем открою аукцион...");
            }
            return;
        }

        searchTicks++;
        if (!(mc.currentScreen instanceof GenericContainerScreen)
                && actionTimer.elapsed(1000L, 300L)) {
            openAuctionSearch(currentEntry);
            actionTimer.reset();
            searchTicks = 0;
        }
    }

    @Subscribe
    private void onContainer(ContainerEvent event) {
        if (currentEntry == null || event.getPhase() == ContainerEvent.Phase.PRE) {
            return;
        }

        String title = event.getTitle().getString().replaceAll("§.", "").toLowerCase().trim();
        if (title.contains(currentEntry.getDisplayName().toLowerCase())) {
            if (actionTimer.elapsed(300L, 80L) && countOwned(currentEntry) >= requiredCount(currentEntry, false)) {
                ChatUtil.send("Готово: " + currentEntry.getDisplayName());
                advanceTarget();
                closeAuction();
                return;
            }

            if (openTimer.elapsed(1000L, 150L)) {
                if (actionTimer.elapsed(currentEntry.isPageScan() ? 400L : 550L, 120L)) {
                    handleAuctionPage(event, title);
                    actionTimer.reset();
                }
            }
            return;
        }

        if (title.contains("подтверждение покупки")
                || title.contains("подозрительная цена!")
                || title.contains("подозрительная цена: ")) {
            if (actionTimer.elapsed(200L, 90L)) {
                clickNamedSlot(event, "[Кyпить]");
                actionTimer.reset();
            }
            return;
        }

        closeAuction();
    }

    private void handleAuctionPage(ContainerEvent event, String title) {
        if (currentEntry.isPageScan()) {
            handlePagedScan(event, title);
            return;
        }
        handleQuickBuy(event, title);
    }

    private void handlePagedScan(ContainerEvent event, String title) {
        Matcher matcher = Pattern.compile("(\\d+)/(\\d+)").matcher(title);
        if (!matcher.find()) {
            return;
        }

        int currentPage = Integer.parseInt(matcher.group(1));
        int lastScanPage = Math.min(4, Integer.parseInt(matcher.group(2)));

        if (selectedOffer == null) {
            if (scannedOffers.stream().noneMatch(offer -> offer.getPage() == currentPage)) {
                event.getSlots().stream()
                        .filter(slot -> isBuyable(slot.getStack()))
                        .forEach(slot -> scannedOffers.add(new CollectorOffer(currentPage, slot.id, AuctionPriceUtil.perItemPrice(slot.getStack()))));
            }
            if (currentPage < lastScanPage) {
                clickNamedSlot(event, "следующая страница");
            } else {
                selectedOffer = scannedOffers.stream().min(Comparator.comparingInt(CollectorOffer::getPrice)).orElse(null);
                if (selectedOffer == null) {
                    closeAuction();
                }
            }
            return;
        }

        if (currentPage == selectedOffer.getPage()) {
            Slot offer = event.getSlots().stream()
                    .filter(slot -> isBuyable(slot.getStack()) && AuctionPriceUtil.perItemPrice(slot.getStack()) == selectedOffer.getPrice())
                    .findFirst()
                    .orElse(null);
            if (offer == null) {
                offer = event.getSlots().stream()
                        .filter(slot -> isBuyable(slot.getStack()))
                        .min(Comparator.comparingInt(slot -> AuctionPriceUtil.perItemPrice(slot.getStack())))
                        .orElse(null);
            }
            if (offer != null) {
                mc.interactionManager.clickSlot(event.getHandler().syncId, offer.id, 0, SlotActionType.QUICK_MOVE, mc.player);
                actionTimer.reset();
            } else {
                ChatUtil.send("Лот пропал, пересканирую");
                selectedOffer = null;
                scannedOffers.clear();
                closeAuction();
            }
        } else {
            clickNamedSlot(event, currentPage < selectedOffer.getPage() ? "следующая страница" : "предыдущая страница");
        }
    }

    private void handleQuickBuy(ContainerEvent event, String title) {
        List<Slot> buyable = event.getSlots().stream().filter(slot -> isBuyable(slot.getStack())).toList();
        int minPrice = buyable.stream().mapToInt(slot -> AuctionPriceUtil.perItemPrice(slot.getStack())).min().orElse(0);
        List<Slot> affordable = buyable.stream()
                .filter(slot -> AuctionPriceUtil.perItemPrice(slot.getStack()) <= Math.round(minPrice * 2.0f))
                .filter(slot -> slot.getStack().getCount() <= requiredCount(currentEntry, true) - countOwned(currentEntry))
                .toList();

        Slot cheapest = affordable.stream()
                .filter(slot -> slot.getStack().getCount() >= requiredCount(currentEntry, false) - countOwned(currentEntry))
                .min(Comparator.comparingInt(slot -> AuctionPriceUtil.perItemPrice(slot.getStack())))
                .orElse(null);

        if (cheapest == null) {
            cheapest = affordable.stream()
                    .min(Comparator.comparingInt(slot -> AuctionPriceUtil.perItemPrice(slot.getStack())))
                    .orElse(null);
        }

        if (cheapest != null) {
            mc.interactionManager.clickSlot(event.getHandler().syncId, cheapest.id, 0, SlotActionType.QUICK_MOVE, mc.player);
            actionTimer.reset();
            return;
        }

        Matcher matcher = Pattern.compile("(\\d+)/(\\d+)").matcher(title);
        if (matcher.find()) {
            int currentPage = Integer.parseInt(matcher.group(1));
            int totalPages = Integer.parseInt(matcher.group(2));
            if (totalPages == 1) {
                ChatUtil.send("Пропускаю " + currentEntry.getDisplayName() + " — нет подходящих лотов");
                advanceTarget();
                closeAuction();
                return;
            }
            clickNamedSlot(event, currentPage == 1 ? "следующая страница" : "предыдущая страница");
        }
    }

    @Subscribe
    private void onPacket(EventPacket event) {
        if (currentEntry == null || event.getType() != EventPacket.Type.RECEIVE) {
            return;
        }

        if (event.getPacket() instanceof GameMessageS2CPacket packet) {
            String message = packet.content().getString();
            if (message.toLowerCase().contains("[✘] ошибка! этот товар уже купили!")) {
                selectedOffer = null;
                scannedOffers.clear();
            } else if (message.toLowerCase().contains("[✘] ошибка! у вас не хватает монет!")) {
                ChatUtil.send("Недостаточно монет — закупка остановлена");
                stopWork();
                setEnabled(false);
            }
        }

        if (event.getPacket() instanceof OpenScreenS2CPacket && !(mc.currentScreen instanceof GenericContainerScreen)) {
            if (searchTicks >= 8) {
                int anarchy = MathUtil.random(0.0f, 100.0f) <= 50.0f
                        ? (int) MathUtil.random(205.0f, 231.0f)
                        : (int) MathUtil.random(305.0f, 325.0f);
                mc.player.networkHandler.sendChatMessage("/an" + anarchy);
                ChatUtil.send("Медленный аукцион — перехожу на анархию " + anarchy);
            }
            openTimer.reset();
        }
    }

    @Subscribe
    private void onMoveInput(MoveInputEvent event) {
        if (currentEntry != null && mc.currentScreen instanceof HandledScreen) {
            event.forward = 0;
            event.strafe = 0;
        }
    }

    @Subscribe
    private void onKey(EventKeyInput event) {
        if (currentEntry != null && mc.currentScreen instanceof HandledScreen) {
            event.cancelEvent();
        }
        if (event.getKey() == GLFW.GLFW_KEY_ESCAPE && event.getAction() == 1 && currentEntry != null) {
            stopWork();
            ChatUtil.send("Закупка остановлена вручную");
        }
    }

    private void clickNamedSlot(ContainerEvent event, String name) {
        event.getSlots().stream()
                .filter(slot -> slot.getStack().getName().getString().toLowerCase().contains(name.toLowerCase()))
                .findFirst()
                .ifPresent(slot -> mc.interactionManager.clickSlot(event.getHandler().syncId, slot.id, 0, SlotActionType.QUICK_MOVE, mc.player));
    }

    private void closeAuction() {
        if (mc.currentScreen instanceof GenericContainerScreen) {
            mc.player.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(mc.player.currentScreenHandler.syncId));
            mc.player.closeScreen();
        }
    }

    private void stopWork() {
        currentEntry = null;
        selectedOffer = null;
        scannedOffers.clear();
        waitingForSpawn = false;
    }

    private boolean isBuyable(ItemStack stack) {
        if (stack.isEmpty() || currentEntry == null || !currentEntry.matches(stack)) {
            return false;
        }

        ItemStack inventorySample = IntStream.range(0, 40)
                .mapToObj(slot -> mc.player.getInventory().getStack(slot))
                .filter(item -> !item.isEmpty() && currentEntry.matches(item))
                .findFirst()
                .orElse(ItemStack.EMPTY);

        if ((!inventorySample.isEmpty()
                && (((currentEntry.getItem() instanceof PotionItem)
                && !Objects.equals(stack.get(DataComponentTypes.POTION_CONTENTS), inventorySample.get(DataComponentTypes.POTION_CONTENTS)))
                || !stack.getName().getString().trim().equalsIgnoreCase(inventorySample.getName().getString().trim())))
                || stack.getTooltip(Item.TooltipContext.DEFAULT, mc.player, TooltipType.BASIC).stream()
                .anyMatch(line -> line.getString().contains("➥ Нажмите, чтобы забрать"))
                || AuctionPriceUtil.perItemPrice(stack) <= 0) {
            return false;
        }

        if (currentEntry.getItem() == Items.TOTEM_OF_UNDYING && stack.hasGlint()) {
            return false;
        }

        if (currentEntry.getItem() == Items.ELYTRA && stack.get(DataComponentTypes.DAMAGE) != null) {
            return (432.0f - stack.get(DataComponentTypes.DAMAGE)) / 432.0f >= 0.5f;
        }

        if (currentEntry.getItem() instanceof ArmorItem && stack.get(DataComponentTypes.DAMAGE) != null && stack.getMaxDamage() > 0) {
            boolean needsMending = currentEntry.getEnchantFilter() != null
                    && currentEntry.getEnchantFilter().getRules().stream().anyMatch(rule ->
                    rule.isEnabled() && !rule.isDeny() && rule.getEnchantment().equals(net.minecraft.enchantment.Enchantments.MENDING));
            if (!needsMending) {
                return ((float) (stack.getMaxDamage() - stack.get(DataComponentTypes.DAMAGE)) / stack.getMaxDamage()) >= 0.7f;
            }
        }

        return true;
    }

    private int requiredCount(CollectorEntry entry, boolean upper) {
        return Math.max(1, upper ? entry.getTargetCount() + Math.round(entry.getTargetCount() * 0.2f) : entry.getTargetCount());
    }

    private int countOwned(CollectorEntry entry) {
        PlayerInventory inventory = mc.player.getInventory();
        return IntStream.range(0, 40)
                .mapToObj(inventory::getStack)
                .filter(stack -> !stack.isEmpty() && entry.matches(stack))
                .mapToInt(ItemStack::getCount)
                .sum();
    }
}
