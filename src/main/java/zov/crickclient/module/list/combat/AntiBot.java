package zov.crickclient.module.list.combat;

import com.google.common.eventbus.Subscribe;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.MathHelper;
import zov.crickclient.event.list.EventPacket;
import zov.crickclient.event.list.EventTick;
import zov.crickclient.module.Module;
import zov.crickclient.module.ModuleCategory;
import zov.crickclient.module.ModuleInformation;
import zov.crickclient.util.base.Instance;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@ModuleInformation(moduleName = "Anti Bot", moduleDesc = "Фильтрует ботов (Matrix, Relic, RW и др.) для KillAura и HUD", moduleCategory = ModuleCategory.COMBAT)
public class AntiBot extends Module {

    private final Set<UUID> suspectSet = new HashSet<>();
    private static final Set<UUID> botSet = new HashSet<>();
    private final Map<UUID, RelicBotTracker> relicTrackers = new HashMap<>();

    private ClientWorld lastWorld;

    private static final class RelicBotTracker {
        int ticks;
        int jumpTicks;
        int orbitTicks;
        double lastOrbitAngle = Double.NaN;
    }

    @Subscribe
    public void onPacket(EventPacket event) {
        if (event.getType() != EventPacket.Type.RECEIVE) return;
        if (!(event.getPacket() instanceof PlayerListS2CPacket packet)) return;

        if (packet.getActions().contains(PlayerListS2CPacket.Action.ADD_PLAYER)) {
            for (PlayerListS2CPacket.Entry entry : packet.getPlayerAdditionEntries()) {
                checkPlayerAfterSpawn(entry);
            }
        }

        if (packet.getActions().contains(PlayerListS2CPacket.Action.UPDATE_LISTED)) {
            for (PlayerListS2CPacket.Entry entry : packet.getEntries()) {
                if (!entry.listed()) {
                    removePlayer(entry.profileId());
                }
            }
        }
    }

    @Subscribe
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        if (mc.world != lastWorld) {
            reset();
            lastWorld = mc.world;
        }

        if (!suspectSet.isEmpty()) {
            for (AbstractClientPlayerEntity player : mc.world.getPlayers()) {
                if (suspectSet.contains(player.getUuid())) {
                    evaluateSuspectPlayer(player);
                }
            }
        }

        scanPlayers();
        updateRelicTrackers();
    }

    private void updateRelicTrackers() {
        if (mc.player == null) return;

        Set<UUID> seen = new HashSet<>();
        for (AbstractClientPlayerEntity entity : mc.world.getPlayers()) {
            if (entity == mc.player) continue;
            seen.add(entity.getUuid());

            RelicBotTracker tracker = relicTrackers.computeIfAbsent(entity.getUuid(), id -> new RelicBotTracker());
            tracker.ticks++;

            if (!entity.isOnGround() && entity.getVelocity().y > 0.08) {
                tracker.jumpTicks++;
            }

            double dx = entity.getX() - mc.player.getX();
            double dz = entity.getZ() - mc.player.getZ();
            double dist = Math.hypot(dx, dz);
            if (dist >= 1.8 && dist <= 6.5) {
                double angle = Math.atan2(dz, dx);
                if (!Double.isNaN(tracker.lastOrbitAngle)) {
                    double delta = Math.abs(MathHelper.wrapDegrees(
                            (float) Math.toDegrees(angle - tracker.lastOrbitAngle)));
                    if (delta > 1.5 && delta < 40.0) {
                        tracker.orbitTicks++;
                    }
                }
                tracker.lastOrbitAngle = angle;
            }
        }

        relicTrackers.entrySet().removeIf(entry -> !seen.contains(entry.getKey()));
    }

    private void checkPlayerAfterSpawn(PlayerListS2CPacket.Entry entry) {
        if (mc.getNetworkHandler() == null) return;

        GameProfile profile = entry.profile();
        if (isRealPlayer(entry, profile)) return;

        if (isDuplicateProfile(profile)) {
            botSet.add(profile.getId());
        } else {
            suspectSet.add(profile.getId());
        }
    }

    private void removePlayer(UUID uuid) {
        suspectSet.remove(uuid);
        botSet.remove(uuid);
    }

    private boolean isRealPlayer(PlayerListS2CPacket.Entry entry, GameProfile profile) {
        return entry.latency() < 5
                || (profile.getProperties() != null && !profile.getProperties().isEmpty());
    }

    private boolean isDuplicateProfile(GameProfile profile) {
        if (mc.getNetworkHandler() == null) return false;

        long duplicates = mc.getNetworkHandler().getPlayerList().stream()
                .filter(info -> info.getProfile().getName().equals(profile.getName())
                        && !info.getProfile().getId().equals(profile.getId()))
                .count();
        return duplicates == 1;
    }

    private void evaluateSuspectPlayer(PlayerEntity player) {
        Iterable<ItemStack> previousArmor = null;

        if (!isFullyEquipped(player)) {
            previousArmor = player.getArmorItems();
        }

        if (isFullyEquipped(player) || hasArmorChanged(player, previousArmor)) {
            botSet.add(player.getUuid());
        }

        suspectSet.remove(player.getUuid());
    }

    private boolean isFullyEquipped(PlayerEntity entity) {
        for (int slot = 0; slot < 4; slot++) {
            ItemStack stack = getArmorStack(entity, slot);
            if (!(stack.getItem() instanceof ArmorItem) || stack.hasEnchantments()) {
                return false;
            }
        }
        return true;
    }

    private boolean hasArmorChanged(PlayerEntity entity, Iterable<ItemStack> previousArmor) {
        if (previousArmor == null) return true;

        int index = 0;
        for (ItemStack previous : previousArmor) {
            if (index >= 4 || !ItemStack.areEqual(getArmorStack(entity, index), previous)) {
                return true;
            }
            index++;
        }
        return index != 4;
    }

    private void scanPlayers() {
        for (AbstractClientPlayerEntity entity : mc.world.getPlayers()) {
            if (entity == mc.player) continue;

            UUID uuid = entity.getUuid();
            boolean matrix = matchesMatrix(entity);
            boolean reallyWorld = matchesReallyWorld(entity);
            boolean uniAc = matchesUniAc(entity);
            boolean relic = matchesRelic(entity);

            if (matrix || reallyWorld || uniAc || relic) {
                botSet.add(uuid);
            } else if (!suspectSet.contains(uuid)) {
                botSet.remove(uuid);
            }
        }
    }

    /**
     * Relic AC: при ударе по игроку спавнит «клона» с разной бронёй (кожа/железо/алмаз),
     * прыгает и летает по кругу в паре блоков от тебя.
     */
    private boolean matchesRelic(AbstractClientPlayerEntity entity) {
        if (!hasMixedArmorMaterials(entity)) return false;

        int armorPieces = countArmorPieces(entity);
        if (armorPieces < 3) return false;

        boolean noEnchants = !hasEnchantedArmor(entity);

        net.minecraft.client.network.PlayerListEntry tabEntry = mc.getNetworkHandler() != null
                ? mc.getNetworkHandler().getPlayerListEntry(entity.getUuid()) : null;
        boolean missingFromTab = tabEntry == null;
        boolean zeroPing = tabEntry != null && tabEntry.getLatency() <= 0;

        RelicBotTracker tracker = relicTrackers.get(entity.getUuid());
        boolean jumpsOften = tracker != null && tracker.ticks >= 12
                && tracker.jumpTicks >= Math.max(3, tracker.ticks / 4);
        boolean orbits = tracker != null && tracker.ticks >= 10 && tracker.orbitTicks >= 6;

        double dist = mc.player != null ? mc.player.distanceTo(entity) : 999;
        boolean closeRange = dist >= 1.5 && dist <= 7.0;

        if (missingFromTab && closeRange) return true;
        if (zeroPing && noEnchants && closeRange && (jumpsOften || orbits)) return true;
        if (noEnchants && jumpsOften && orbits && closeRange) return true;

        return armorPieces == 4 && noEnchants && jumpsOften && closeRange;
    }

    private int countArmorPieces(PlayerEntity entity) {
        int count = 0;
        for (int slot = 0; slot < 4; slot++) {
            if (!getArmorStack(entity, slot).isEmpty()) count++;
        }
        return count;
    }

    private boolean hasEnchantedArmor(PlayerEntity entity) {
        for (int slot = 0; slot < 4; slot++) {
            if (getArmorStack(entity, slot).hasEnchantments()) return true;
        }
        return false;
    }

    private boolean hasMixedArmorMaterials(PlayerEntity entity) {
        Set<String> materials = new HashSet<>();
        for (int slot = 0; slot < 4; slot++) {
            String material = armorMaterial(getArmorStack(entity, slot));
            if (material != null) materials.add(material);
        }
        return materials.size() >= 2;
    }

    private String armorMaterial(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return null;
        String path = Registries.ITEM.getId(stack.getItem()).getPath();
        if (path.contains("leather")) return "leather";
        if (path.contains("chainmail") || path.contains("chain")) return "chain";
        if (path.contains("iron")) return "iron";
        if (path.contains("golden") || path.contains("gold")) return "gold";
        if (path.contains("diamond")) return "diamond";
        if (path.contains("netherite")) return "netherite";
        return "other";
    }

    private boolean matchesMatrix(AbstractClientPlayerEntity entity) {
        boolean hasBoots = !getArmorStack(entity, 0).isEmpty();
        boolean hasLeggings = !getArmorStack(entity, 1).isEmpty();
        boolean hasChestplate = !getArmorStack(entity, 2).isEmpty();
        boolean hasHelmet = !getArmorStack(entity, 3).isEmpty();

        boolean bootsEnchantable = getArmorStack(entity, 0).isEnchantable();
        boolean leggingsEnchantable = getArmorStack(entity, 1).isEnchantable();
        boolean chestplateEnchantable = getArmorStack(entity, 2).isEnchantable();
        boolean helmetEnchantable = getArmorStack(entity, 3).isEnchantable();

        boolean emptyOffhand = entity.getOffHandStack().isEmpty();
        boolean hasLeatherOrIron = hasLeatherOrIronArmor(entity);
        boolean hasMainHandItem = !entity.getMainHandStack().isEmpty();

        boolean notDamagedBoots = !getArmorStack(entity, 0).isDamaged();
        boolean notDamagedLeggings = !getArmorStack(entity, 1).isDamaged();
        boolean notDamagedChestplate = !getArmorStack(entity, 2).isDamaged();
        boolean notDamagedHelmet = !getArmorStack(entity, 3).isDamaged();

        boolean fullHunger = entity.getHungerManager().getFoodLevel() == 20;

        return hasBoots && hasLeggings && hasChestplate && hasHelmet
                && bootsEnchantable && leggingsEnchantable && chestplateEnchantable && helmetEnchantable
                && emptyOffhand && hasLeatherOrIron && hasMainHandItem
                && notDamagedBoots && notDamagedLeggings && notDamagedChestplate && notDamagedHelmet
                && fullHunger;
    }

    private boolean hasLeatherOrIronArmor(AbstractClientPlayerEntity entity) {
        return isArmorItem(entity, 0, Items.LEATHER_BOOTS, Items.IRON_BOOTS)
                || isArmorItem(entity, 1, Items.LEATHER_LEGGINGS, Items.IRON_LEGGINGS)
                || isArmorItem(entity, 2, Items.LEATHER_CHESTPLATE, Items.IRON_CHESTPLATE)
                || isArmorItem(entity, 3, Items.LEATHER_HELMET, Items.IRON_HELMET);
    }

    private boolean isArmorItem(AbstractClientPlayerEntity entity, int slot, net.minecraft.item.Item first, net.minecraft.item.Item second) {
        var item = getArmorStack(entity, slot).getItem();
        return item == first || item == second;
    }

    private boolean matchesReallyWorld(AbstractClientPlayerEntity entity) {
        String playerName = entity.getName().getString();
        UUID expectedUuid = Uuids.getOfflinePlayerUuid(playerName);
        boolean fakeUuid = !entity.getUuid().equals(expectedUuid);
        boolean notNpc = !playerName.contains("NPC") && !playerName.startsWith("[ZNPC]");
        return fakeUuid && notNpc;
    }

    private boolean matchesUniAc(AbstractClientPlayerEntity entity) {
        boolean boots = isUniAcArmorSlot(entity, 0);
        boolean leggings = isUniAcArmorSlot(entity, 1);
        boolean chestplate = isUniAcArmorSlot(entity, 2);
        boolean helmet = isUniAcArmorSlot(entity, 3);

        boolean playerIsNotNaked = entity.getArmor() != 0;
        boolean allDamaged = getArmorStack(entity, 0).isDamaged()
                && getArmorStack(entity, 1).isDamaged()
                && getArmorStack(entity, 2).isDamaged()
                && getArmorStack(entity, 3).isDamaged();
        boolean nameWidth = entity.getName().getString().length() == 6;
        boolean fullArmor = boots && leggings && chestplate && helmet;

        return nameWidth && playerIsNotNaked && !allDamaged && fullArmor;
    }

    private boolean isUniAcArmorSlot(AbstractClientPlayerEntity entity, int slot) {
        ItemStack stack = getArmorStack(entity, slot);
        if (stack.isEmpty()) return true;
        return !stack.hasEnchantments();
    }

    private ItemStack getArmorStack(PlayerEntity entity, int slot) {
        return entity.getInventory().armor.get(slot);
    }

    private static boolean isInvalidName(String name) {
        return name == null || name.isEmpty() || name.length() > 16 || !name.matches("^[a-zA-Z0-9_]{3,16}$");
    }

    public void reset() {
        suspectSet.clear();
        botSet.clear();
        relicTrackers.clear();
    }

    @Override
    public void onDisable() {
        reset();
        lastWorld = null;
        super.onDisable();
    }

    public static boolean isBot(Entity entity) {
        AntiBot instance = Instance.get(AntiBot.class);
        if (instance == null || !instance.isEnabled()) return false;
        if (!(entity instanceof PlayerEntity player)) return false;

        String name = player.getGameProfile().getName();
        return isInvalidName(name) || botSet.contains(player.getUuid());
    }
}
