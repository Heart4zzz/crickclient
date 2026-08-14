package zov.crickclient.module.list.combat;

import com.google.common.eventbus.Subscribe;
import lombok.Getter;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import zov.crickclient.CrickClient;
import zov.crickclient.event.list.EventFrame;
import zov.crickclient.event.list.EventPlayerUpdate;
import zov.crickclient.event.list.MoveInputEvent;
import zov.crickclient.module.Module;
import zov.crickclient.module.ModuleCategory;
import zov.crickclient.module.ModuleInformation;
import zov.crickclient.module.settings.BooleanSetting;
import zov.crickclient.module.settings.ModeListSetting;
import zov.crickclient.module.settings.ModeSetting;
import zov.crickclient.module.settings.SliderSetting;
import zov.crickclient.util.friend.FriendRepository;
import zov.crickclient.util.player.combat.minced.MincedAuraModes;
import zov.crickclient.util.player.combat.minced.MincedRotationTarget;
import zov.crickclient.util.player.combat.minced.MincedWeaponUtil;
import zov.crickclient.util.render.math.MathUtil;
import zov.crickclient.util.rotation.FreeLookComponent;
import zov.crickclient.util.rotation.Rotation;
import zov.crickclient.util.rotation.RotationComponent;
import zov.crickclient.util.text.ValueUnit;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.ThreadLocalRandom;

@ModuleInformation(moduleName = "KillAura", moduleDesc = "Minced AttackAura port", moduleCategory = ModuleCategory.COMBAT)
public class KillAura extends Module {

    public final ModeSetting bypassMode = new ModeSetting(
            "Обход",
            "Spooky Test",
            "ReallyWorld",
            "Spooky Test",
            "Legit",
            "Matrix"
    );

    @Deprecated
    public final ModeSetting rotation = bypassMode;

    private final ModeListSetting targets = new ModeListSetting(
            "Таргеты",
            new BooleanSetting("Игроки", true),
            new BooleanSetting("Голые", true),
            new BooleanSetting("Монстры", true),
            new BooleanSetting("Животные", true),
            new BooleanSetting("Друзья", false)
    );

    public final SliderSetting distance = new SliderSetting("Дистанция", ValueUnit.countable("блок", "блока", "блоков"), 3.0F, 2.0F, 6.0F, 0.1F);
    private final SliderSetting preDistance = new SliderSetting("Пре дистанция", ValueUnit.countable("блок", "блока", "блоков"), 0.0F, 0.0F, 3.0F, 0.1F);

    private final ModeListSetting critOptions = new ModeListSetting(
            "Опции",
            new BooleanSetting("Только криты", true),
            new BooleanSetting("Отжимать щит", false),
            new BooleanSetting("Ломать щит", false)
    );

    public final BooleanSetting onlySpace = new BooleanSetting("Только с пробелом", true);
    public final BooleanSetting tpsSync = new BooleanSetting("Синхронизация с ТПС", false);
    public final BooleanSetting raycastCheck = new BooleanSetting("Проверка на луч", false);
    public final BooleanSetting wallHit = new BooleanSetting("Бить через стены", true);
    public final BooleanSetting noEat = new BooleanSetting("Не бить если ешь", false);
    public final ModeSetting moveFix = new ModeSetting("Коррекция движения", "Сфокусированная", "Нет", "Сфокусированная", "Свободная");
    public final BooleanSetting clientLook = new BooleanSetting("Клиент лук", false);

    public final BooleanSetting visualElytraRotation = new BooleanSetting("Визуал. ротка Элитры", true);
    public final SliderSetting predictValue = new SliderSetting("Предикт значение", 3.0F, 1.0F, 5.0F, 0.1F);
    public static final BooleanSetting useResolver = new BooleanSetting("Резольвер (Elytra)", true);

    public static LivingEntity lastTarget;
    public static long lastPhysicalMoveTime;
    public static boolean isSlowdownActive;
    public static boolean isTurnaroundActive;

    public float lastYaw;
    public float lastPitch;
    public float speedAcceleration;

    @Getter
    private LivingEntity target;

    private final MincedAuraModes auraModes = new MincedAuraModes();
    private final MincedWeaponUtil weaponUtil = new MincedWeaponUtil();

    private long nextAttackMs;
    private float cooldownThreshold = 0.91F;
    private boolean zeroMoveNextTick;
    private boolean wasSprintingLastTick;

    public ModeSetting getBypassMode() {
        return bypassMode;
    }

    public BooleanSetting getOnlySpace() {
        return onlySpace;
    }

    public BooleanSetting getTpsSync() {
        return tpsSync;
    }

    public BooleanSetting getClientLook() {
        return clientLook;
    }

    public long getNextAttackMs() {
        return nextAttackMs;
    }

    public float getCooldownThreshold() {
        return cooldownThreshold;
    }

    public boolean isOnlyCritsEnabled() {
        return critOptions.isEnabled("Только криты");
    }

    public boolean isNoEatEnabled() {
        return noEat.getValue();
    }

    public KillAura() {
        addSettings(
                bypassMode,
                targets,
                distance,
                preDistance,
                critOptions,
                onlySpace,
                tpsSync,
                raycastCheck,
                wallHit,
                noEat,
                moveFix,
                clientLook,
                visualElytraRotation,
                predictValue,
                useResolver
        );
    }

    @Override
    public void onEnable() {
        nextAttackMs = 0L;
        cooldownThreshold = MathUtil.random(0.88F, 0.94F);
        zeroMoveNextTick = false;
        wasSprintingLastTick = false;
        auraModes.activateSelected(this);
    }

    @Override
    public void onDisable() {
        target = null;
        auraModes.deactivateCurrent(this);
        auraModes.resetSelected(this);
        zeroMoveNextTick = false;
        wasSprintingLastTick = false;
    }

    @Subscribe
    private void onPlayerUpdate(EventPlayerUpdate ignored) {
        if (mc.player == null || mc.world == null) {
            return;
        }

        if (target == null || !isValidTarget(target)) {
            updateTarget();
        }

        if (target == null) {
            auraModes.resetSelected(this);
            return;
        }

        lastTarget = target;
        attackLoop();
        wasSprintingLastTick = mc.player.isSprinting();
    }

    @Subscribe
    private void onFrame(EventFrame ignored) {
        if (!isEnabled() || mc.player == null || mc.world == null || target == null) {
            return;
        }

        auraModes.updateRotation(this, target);
        lastYaw = mc.player.getYaw();
        lastPitch = mc.player.getPitch();
    }

    @Subscribe
    private void onMoveInput(MoveInputEvent event) {
        if (mc.player == null || target == null || moveFix.is("Нет")) {
            return;
        }

        float yaw = moveFix.is("Свободная")
                ? (FreeLookComponent.isActive() ? FreeLookComponent.getFreeYaw() : mc.player.getYaw())
                : MincedRotationTarget.getFocusYaw(target);
        RotationComponent.fixMovement(event, MathHelper.wrapDegrees(yaw));

        if (zeroMoveNextTick) {
            event.forward = 0;
            event.strafe = 0;
            zeroMoveNextTick = false;
        }
    }

    private void attackLoop() {
        if (!weaponUtil.canAttackEntity(this, target)) {
            auraModes.beforeAttack(this, target);
            return;
        }

        if (!auraModes.isSelectedReady(this, target)) {
            if (raycastCheck.getValue() && !rayTrace(mc.player.getYaw(), mc.player.getPitch(), distance.getValue(), target)) {
                auraModes.beforeAttack(this, target);
                return;
            }

            if (!prepareSprintReset()) {
                return;
            }

            if (!wallHit.getValue() && !mc.player.canSee(target)) {
                return;
            }

            if (mc.player.distanceTo(target) > distance.getValue()) {
                return;
            }

            auraModes.beforeAttack(this, target);
            mc.interactionManager.attackEntity(mc.player, target);
            mc.player.swingHand(Hand.MAIN_HAND);
            auraModes.notifyAttack(this, target);
            scheduleNextAttack();
        } else {
            auraModes.beforeAttack(this, target);
        }
    }

    private void scheduleNextAttack() {
        nextAttackMs = System.currentTimeMillis() + 460L;
        cooldownThreshold = MathUtil.random(0.88F, 0.94F);
    }

    private boolean prepareSprintReset() {
        boolean fluid = mc.player.isTouchingWater()
                || mc.player.isInLava()
                || mc.player.isSwimming()
                || mc.player.isGliding();
        if (!fluid && mc.player.isSprinting()) {
            zeroMoveNextTick = true;
            if (wasSprintingLastTick && bypassMode.is("Spooky Test")) {
                return false;
            }
        }
        return true;
    }

    private void updateTarget() {
        ArrayList<LivingEntity> valid = new ArrayList<>();
        for (Entity entity : mc.world.getEntities()) {
            if (entity instanceof LivingEntity living && isValidTarget(living)) {
                valid.add(living);
            }
        }

        if (valid.isEmpty()) {
            target = null;
            return;
        }

        if (valid.size() == 1) {
            target = valid.getFirst();
            return;
        }

        target = valid.stream()
                .min(Comparator.comparingDouble(entity -> mc.player.distanceTo(entity)))
                .orElse(null);
    }

    private boolean isValidTarget(LivingEntity entity) {
        if (mc.player == null || mc.world == null || !entity.isAlive() || entity.isInvulnerable()) {
            return false;
        }
        if (entity instanceof ClientPlayerEntity || entity instanceof ArmorStandEntity) {
            return false;
        }

        float maxRange = distance.getValue() + preDistance.getValue()
                + (mc.player.isGliding() ? 20.0F : 0.0F);
        if (mc.player.distanceTo(entity) > maxRange) {
            return false;
        }

        if (entity instanceof PlayerEntity player) {
            if (player.getName().getString().equalsIgnoreCase(mc.player.getName().getString())) {
                return false;
            }
            if (AntiBot.isBot(player)) {
                return false;
            }
            if (!targets.isEnabled("Друзья") && FriendRepository.isFriend(player.getName().getString())) {
                return false;
            }
            if (!targets.isEnabled("Игроки") && player.getArmor() != 0) {
                return false;
            }
            if (!targets.isEnabled("Голые") && player.getArmor() == 0) {
                return false;
            }
            return true;
        }

        if (entity instanceof MobEntity || entity instanceof HostileEntity) {
            return targets.isEnabled("Монстры");
        }

        if (entity instanceof AnimalEntity) {
            return targets.isEnabled("Животные");
        }

        return false;
    }

    public static boolean rayTrace(float yaw, float pitch, double reach, Entity entity) {
        if (mc.player == null || entity == null) {
            return false;
        }
        float tickDelta = mc.getRenderTickCounter().getTickDelta(false);
        Vec3d eye = mc.player.getCameraPosVec(tickDelta);
        Vec3d look = mc.player.getRotationVector(pitch, yaw);
        Vec3d end = eye.add(look.multiply(reach));
        Box box = entity.getBoundingBox();
        return box.contains(eye) || box.raycast(eye, end).isPresent();
    }

    public boolean canAttack() {
        return target != null && weaponUtil.canAttackEntity(this, target);
    }
}
