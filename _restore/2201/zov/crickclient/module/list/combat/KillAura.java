package zov.crickclient.module.list.combat;

import com.google.common.eventbus.Subscribe;
import com.mojang.blaze3d.systems.RenderSystem;
import lombok.Getter;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.mob.AmbientEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.FishEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import org.joml.Matrix4f;
import zov.crickclient.CrickClient;
import zov.crickclient.event.EventGameUpdate;
import zov.crickclient.event.list.EventTick;
import zov.crickclient.event.list.MoveInputEvent;
import zov.crickclient.module.Module;
import zov.crickclient.module.ModuleCategory;
import zov.crickclient.module.ModuleInformation;
import zov.crickclient.module.ModuleSettingDefinitions;
import zov.crickclient.module.list.player.FreeCamera;
import zov.crickclient.module.list.movement.ElytraTarget;
import zov.crickclient.module.settings.BooleanSetting;
import zov.crickclient.module.settings.ModeListSetting;
import zov.crickclient.module.settings.ModeSetting;
import zov.crickclient.module.settings.SliderSetting;
import zov.crickclient.util.friend.FriendRepository;
import zov.crickclient.util.math.BestPoint;
import zov.crickclient.util.math.RotationUtil;
import zov.crickclient.util.math.StopWatch;
import zov.crickclient.util.player.combat.FunTimeAuraUtil;
import zov.crickclient.util.player.combat.SpookyTimeAuraUtil;
import zov.crickclient.util.player.combat.PredictUtils;
import zov.crickclient.util.player.combat.RaytraceUtil;
import zov.crickclient.util.player.simulate.SimulatedPlayer;
import zov.crickclient.util.server.Server;
import zov.crickclient.util.render.math.GCDFixer;
import zov.crickclient.util.render.math.MathUtil;
import zov.crickclient.util.render.providers.ColorProvider;
import zov.crickclient.util.rotation.FreeLookComponent;
import zov.crickclient.util.rotation.Rotation;
import zov.crickclient.util.rotation.RotationComponent;
import zov.crickclient.util.text.ValueUnit;
import zov.crickclient.util.neuro.rotation.AIRotationRecorder;

import java.util.Arrays;

@ModuleInformation(moduleName = "KillAura", moduleDesc = "Автоматически атакует ближайших врагов", moduleCategory = ModuleCategory.COMBAT)
public class KillAura extends Module {

    public final ModeSetting rotation = ModuleSettingDefinitions.killAuraRotation();
    private final ModeListSetting targets = new ModeListSetting("Таргеты",
            new BooleanSetting("Игроки", true),
            new BooleanSetting("Голые", true),
            new BooleanSetting("Монстры", true),
            new BooleanSetting("Животные", true)
    );

    public final SliderSetting distance = new SliderSetting("Дистанция", ValueUnit.countable("блок", "блока", "блоков"), 3, 2, 6, 0.1f);
    private final SliderSetting preRotation = new SliderSetting("Пре дистанция", ValueUnit.countable("блок", "блока", "блоков"), 1.5f, 0, 3, 0.1f);
    public final BooleanSetting raycastCheck = new BooleanSetting("Проверка на наведение", true);
    public final BooleanSetting smartAim = new BooleanSetting("Умное наведение", true);
    public final BooleanSetting predictate = new BooleanSetting("Предикт", true);
    public final SliderSetting predictValue = new SliderSetting("Предикт значение", 3, 1, 5, 0.1f);

    public final BooleanSetting elytraSlowdown = new BooleanSetting("Замедление на элитрах", true);
    public final ModeSetting slowdownMode = ModuleSettingDefinitions.killAuraSprintReset().setVisible(() -> elytraSlowdown.getValue());
    public final SliderSetting slowdownRadius = new SliderSetting("Радиус замедления", ValueUnit.countable("блок", "блока", "блоков"), 3.0f, 1.0f, 6.0f, 0.1f).setVisible(() -> elytraSlowdown.getValue() && slowdownMode.is("По радиусу"));
    public final SliderSetting minSpeed = new SliderSetting("Мин. скорость", 0.3f, 0.1f, 0.9f, 0.05f).setVisible(() -> elytraSlowdown.getValue() && slowdownMode.is("По радиусу"));
    public final SliderSetting preHitTicks = new SliderSetting("Тики до удара", 3, 1, 10, 1).setVisible(() -> elytraSlowdown.getValue() && slowdownMode.is("Перед ударом"));
    public final BooleanSetting hitAfterOvertake = new BooleanSetting("Бить токо после перегона", true);

    public final ModeSetting moveFix = new ModeSetting("Коррекция движения", "Сфокусированная", "Нет", "Сфокусированная", "Таргетированная");

    public final BooleanSetting onlySpace = ModuleSettingDefinitions.killAuraOnlySpace();
    public final BooleanSetting clientLook = ModuleSettingDefinitions.killAuraClientLook();
    public final BooleanSetting showPredictPoint = new BooleanSetting("Показать предикт точку", true);
    public final BooleanSetting elytraTurnaround = new BooleanSetting("Разворот на элитрах", true);
    public final BooleanSetting visualElytraRotation = new BooleanSetting("Визуал. ротка Элитры", true);

    private ElytraTarget elytraTarget;

    public static final BooleanSetting useResolver = new BooleanSetting("Резольвер (Elytra)", true);
    public boolean isResolving = false;
    public Vec3d resolverPoint = null;
    private final StopWatch resolverTimer = new StopWatch();

    public boolean isTurnaroundActive = false;
    private static final float RANDOM_STRENGTH = 0.75f;
    public static boolean isSlowdownActive = false;
    private static final StopWatch stopWatch = new StopWatch();
    @Getter
    private LivingEntity target;
    public static LivingEntity lastTarget;
    public int ticksToAttack;

    private int razvorotikTicks;
    private boolean back;
    public float speedAcceleration;
    public static long lastPhysicalMoveTime;

    public float preddict;
    public float lastYaw;
    public float lastPitch;

    private LivingEntity slothTrackedTarget;
    private float slothCurrentYaw;
    private float slothCurrentPitch;
    private float slothVelocityYaw;
    private float slothVelocityPitch;
    private double slothAimPointX;
    private double slothAimPointY;

    public ElytraTarget getElytraTarget() {
        if (this.elytraTarget == null) {
            this.elytraTarget = CrickClient.getInstance().getModuleStorage().get(ElytraTarget.class);
        }
        return this.elytraTarget;
    }
    private double slothAimPointZ;
    private float slothNoiseAngle;
    private final float slothNoiseAmplitude = 1.8F;
    private int slothHitPhase;
    private int slothHitTimer;
    private float slothPitchBeforeHit;
    private long slothFirstSeenTime;
    private int slothReactionMs;
    private boolean slothReactionComplete;
    private float slothLastSentYaw;
    private float slothLastSentPitch;
    private float slothSmoothYaw;
    private float slothSmoothPitch;

    private final float[] funTimeState = {-1.0F, -1.0F, -1.0F, -1.0F, 0.0F, -1.0F, -1.0F, -1.0F, -1.0F, -1.0F, -1.0F, -1.0F};
    private final float[] funTimePitchHistory = new float[30];
    private int funTimeTicksSinceAttack;
    private float funTimeFocusYaw;
    private boolean funTimeMissToggle;

    private final SpookyTimeAuraUtil spookyTimeAura = new SpookyTimeAuraUtil();

    private boolean renderListenerRegistered = false;
    private final WorldRenderEvents.Last renderListener = context -> {
        if (isEnabled() && showPredictPoint.getValue()) {
            renderPredictPoint(context.matrixStack(), context.camera(), context.tickCounter().getTickDelta(true));
        }
    };

    private void findResolverPoint() {
        if (mc.player == null || mc.world == null) return;
        Vec3d eye = mc.player.getEyePos();


        float oppositeYaw = mc.player.getYaw() + 180f;

        float searchPitch = -50f;


        int[] yawOffsets = {0, 30, -30, 45, -45, 60, -60, 90, -90};

        for (int offset : yawOffsets) {
            float testYaw = oppositeYaw + offset;

            float radYaw = (float) Math.toRadians(testYaw);
            float radPitch = (float) Math.toRadians(searchPitch);

            double x = -Math.sin(radYaw) * Math.cos(radPitch);
            double y = -Math.sin(radPitch);
            double z = Math.cos(radYaw) * Math.cos(radPitch);

            Vec3d checkVec = new Vec3d(x, y, z).normalize().multiply(8.0);
            Vec3d endPoint = eye.add(checkVec);

            if (mc.world.raycast(new RaycastContext(eye, endPoint, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mc.player)).getType() == HitResult.Type.MISS) {
                resolverPoint = endPoint;
                return;
            }
        }
        resolverPoint = null;
    }

    @Subscribe
    private void onGameUpdate(EventGameUpdate e) {
        if (mc.player == null || target == null) return;

        CrickClient.getInstance().getModuleStorage().setRandomness(1);

        if (AIRotationRecorder.isRecording()) {
            return;
        }

        if (isResolving) {
            if (resolverTimer.isReached(300)) {
                isResolving = false;
            } else if (resolverPoint != null) {
                var rot = new Rotation(RotationUtil.calculate(resolverPoint));
                RotationComponent.update(rot, 360, 360, 360, 360, 0, 1, clientLook.getValue());
                lastYaw = rot.getYaw();
                lastPitch = rot.getPitch();
                return;
            }
        }


        boolean playerOnElytra = mc.player.isGliding();
        if (playerOnElytra && (this.elytraTarget == null || !this.elytraTarget.isEnabled())) {
            return;
        }

        switch (rotation.getValue()) {
            case "ReallyWorld" -> updateVanillaRotation(target);
            case "FunTime" -> updateFunTimeRotation(target);
            case "SpookyTime" -> spookyTimeAura.updateRotation(target);
        }
    }

    @Subscribe
    private void onMoveInput(MoveInputEvent event) {
        if (mc.player == null || target == null) return;

        if (moveFix.is("Таргетированная")) {
            if (mc.player.isGliding()) {
                event.forward = 0;
                event.strafe = 0;
                return;
            }

            if (event.forward == 0 && event.strafe == 0) return;

            float yaw = MathHelper.wrapDegrees(mc.gameRenderer.getCamera().getYaw());
            double dx = target.getX() - mc.player.getX();
            double dz = target.getZ() - mc.player.getZ();
            double targetAngle = MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(dz, dx)) - 90.0);

            float bestForward = 0, bestStrafe = 0;
            float smallestDiff = Float.MAX_VALUE;
            for (float f = -1f; f <= 1f; f++) {
                for (float s = -1f; s <= 1f; s++) {
                    if (f == 0 && s == 0) continue;
                    double predictedAngle = MathHelper.wrapDegrees(Math.toDegrees(RotationComponent.direction(mc.player.getYaw(), f, s)));
                    float diff = (float) Math.abs(MathHelper.wrapDegrees((float) (targetAngle - predictedAngle)));
                    if (diff < smallestDiff) {
                        smallestDiff = diff;
                        bestForward = f;
                        bestStrafe = s;
                    }
                }
            }

            event.forward = bestForward;
            event.strafe = bestStrafe;
            return;
        }

        if (moveFix.is("Нет")) return;

        float yaw = moveFix.is("Сфокусированная")
                ? (rotation.is("SpookyTime")
                    ? spookyTimeAura.getFocusYaw(target)
                    : funTimeFocusYaw)
                : (FreeLookComponent.isActive() ? FreeLookComponent.getFreeYaw() : mc.player.getYaw());
        RotationComponent.fixMovement(event, MathHelper.wrapDegrees(yaw));

        if (rotation.is("SpookyTime") && spookyTimeAura.consumeZeroMovement()) {
            event.forward = 0;
            event.strafe = 0;
            return;
        }

        if (rotation.is("FunTime")
                && funTimeState[0] > 0.0F
                && mc.player.squaredDistanceTo(target) <= distance.getValue() * distance.getValue()) {
            event.forward = 0;
            event.strafe = 0;
            funTimeState[0]--;
        }
    }


    @Subscribe
    private void onUpdate(final EventTick ignored) {
        if (mc.player == null || mc.world == null) return;

        if (ticksToAttack > 0) ticksToAttack--;

        LivingEntity previousTarget = target;
        updateTarget();
        if (rotation.is("FunTime") && target != null && target != previousTarget) {
            funTimeOnTargetChanged();
        }
        if (rotation.is("SpookyTime") && target != null && target != previousTarget) {
            spookyTimeAura.reset();
        }

        if (target != null) {
            if (rotation.is("FunTime")) {
                funTimeTicksSinceAttack++;
            }

            lastTarget = target;
            if (elytraSlowdown.getValue() && mc.player.isGliding()) {
                if (slowdownMode.is("Перед ударом")) {
                    isSlowdownActive = ticksToAttack <= preHitTicks.getValue();
                } else {
                    isSlowdownActive = false;
                }
            } else {
                isSlowdownActive = false;
            }
            Vec3d predict = PredictUtils.predict(target, predictValue.getValue());
            double distToPredict = mc.player.getEyePos().distanceTo(predict);

            if (elytraSlowdown.getValue() && mc.player.isGliding()) {
                if (slowdownMode.is("Перед ударом")) {
                    isSlowdownActive = ticksToAttack <= preHitTicks.getValue();
                } else {
                    isSlowdownActive = distToPredict < 2.7 && ticksToAttack <= 2;
                }
            } else {
                isSlowdownActive = false;
            }
            if (rotation.is("FunTime") || rotation.is("SpookyTime")) {
                updateFocusYaw();
            }

            if (canAttack()) {
                if (useResolver.getValue() && mc.player.isGliding()) {
                    mc.player.setVelocity(0, 0, 0);

                    findResolverPoint();
                    if (resolverPoint != null) {
                        isResolving = true;
                        resolverTimer.reset();
                    }
                }

                if (rotation.is("SpookyTime")) {
                    if (!spookyTimeAura.prepareSprintReset()) {
                        spookyTimeAura.tickSprintTracker();
                    } else {
                        mc.interactionManager.attackEntity(mc.player, target);
                        mc.player.swingHand(Hand.MAIN_HAND);
                        spookyTimeAura.onAttack();
                    }
                } else {
                    if (canStopSprinting()
                            && mc.player.isSprinting()
                            && !mc.player.isTouchingWater()
                            && !mc.player.isInLava()
                            && !mc.player.isSwimming()
                            && !mc.player.isOnGround()) {
                        mc.player.setSprinting(false);
                        mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.STOP_SPRINTING));
                        if (rotation.is("FunTime")) {
                            funTimeState[0] = 1.0F;
                        }
                    }

                    mc.interactionManager.attackEntity(mc.player, target);
                    mc.player.swingHand(Hand.MAIN_HAND);
                    ticksToAttack = 10;
                    if (rotation.is("FunTime")) {
                        funTimeOnAttack();
                    }
                }
            }

            if (rotation.is("SpookyTime")) {
                spookyTimeAura.tickSprintTracker();
            }
        } else {
            speedAcceleration = 0;
        }
    }

    private boolean isValidEntity(Entity entity) {
        if (!entity.isAlive()) return false;
        PlayerEntity player = CrickClient.getInstance().getModuleStorage().get(FreeCamera.class).fakePlayer != null ? CrickClient.getInstance().getModuleStorage().get(FreeCamera.class).fakePlayer : mc.player;
        if (entity == CrickClient.getInstance().getModuleStorage().get(FreeCamera.class).fakePlayer) return false;
        if (entity instanceof ClientPlayerEntity) return false;
        if (entity instanceof ArmorStandEntity) return false;
        if (entity instanceof PlayerEntity p && p.getArmor() != 0 && !targets.isEnabled("Игроки")) return false;
        if (entity instanceof PlayerEntity p && p.getArmor() == 0 && !targets.isEnabled("Голые")) return false;
        if ((entity instanceof HostileEntity || entity instanceof AmbientEntity) && !targets.isEnabled("Монстры"))
            return false;
        if ((entity instanceof PassiveEntity || entity instanceof FishEntity) && !targets.isEnabled("Животные"))
            return false;
        if (entity instanceof PlayerEntity p) {
            if (!FriendRepository.shouldAttack(p)) return false;
            if (AntiBot.isBot(p)) return false;
        }
        if (player.getEyePos().distanceTo(BestPoint.getNearestPoint(entity)) > (player.isGliding() ? 50 : distance.getValue() + preRotation.getValue()))
            return false;
        return true;
    }

    public boolean canAttack() {
        if (target == null) return false;

        isTurnaroundActive = false;

        PlayerEntity player = CrickClient.getInstance().getModuleStorage().get(FreeCamera.class).fakePlayer != null ?
                CrickClient.getInstance().getModuleStorage().get(FreeCamera.class).fakePlayer : mc.player;

        boolean playerElytra = mc.player.isGliding();
        boolean targetElytra = target.isGliding();
        boolean anyElytra = playerElytra || targetElytra;

        if (target.isGliding()) {
            Vec3d predict = PredictUtils.predict(target, predictValue.getValue());
            double distToPredict = player.getEyePos().distanceTo(predict);

            preddict = hitAfterOvertake.getValue() ? 2.7f : 4f;

            if (distToPredict <= preddict && elytraTurnaround.getValue()) {
                isTurnaroundActive = true;
            }
        }

        if (!rotation.is("SpookyTime")
                && !CrickClient.getInstance().getIdealHitUtils().cooldownIsReached(false)) {
            return false;
        }
        if (!rotation.is("SpookyTime") && ticksToAttack > 0) {
            return false;
        }

        if (anyElytra) {
            if (this.elytraTarget == null || !this.elytraTarget.isEnabled()) {
                return false;
            }
            return this.elytraTarget.canAttack(this.target);
        }

        if (rotation.is("SpookyTime")) {
            return spookyTimeAura.shouldAttack(
                    target,
                    distance.getValue(),
                    raycastCheck.getValue()
            );
        }

        if (!RaytraceUtil.rayTrace(player.getRotationVector(), distance.getValue(), target.getBoundingBox()) && raycastCheck.getValue())
            return false;

        if (player.getEyePos().distanceTo(BestPoint.getNearestPoint(target)) > (distance.getValue() - 0.2f))
            return false;

        return CrickClient.getInstance().getIdealHitUtils().canCritical();
    }

    public boolean canStopSprinting() {
        if (target == null) return false;
        if (!CrickClient.getInstance().getIdealHitUtils().cooldownIsReached(true)) return false;
        if (ticksToAttack > 1) return false;
        if (SimulatedPlayer.simulateLocalPlayer(1).fallDistance == 0) return false;
        return true;
    }

    private void updateTarget() {
        LivingEntity best = null;
        double bestFovDot = -1;

        Vec3d eyePos = mc.player.getEyePos();
        Vec3d lookVec = mc.player.getRotationVec(1.0F);

        for (Entity entity : mc.world.getEntities()) {
            if (entity instanceof LivingEntity) {
                if (!isValidEntity(entity)) continue;

                Vec3d targetVec = BestPoint.getNearestPoint(entity).subtract(eyePos).normalize();
                double dot = lookVec.dotProduct(targetVec);

                if (dot > bestFovDot) {
                    bestFovDot = dot;
                    best = (LivingEntity) entity;
                }
            }
        }

        if (target == null || !isValidEntity(target)) {
            this.target = best;
        }
    }

    private void updateVanillaRotation(LivingEntity target) {
        if (target == null) return;

        Vec3d targetPoint = resolveMultipoint(target, BestPoint.getNearestPoint(target), distance.getValue());
        var rotation = new Rotation(RotationUtil.calculate(targetPoint));

        RotationComponent.update(rotation, 360, 360, 360, 360, 0, 1, clientLook.getValue());
    }

    private void slReset() {
        slothTrackedTarget = null;
        slothVelocityYaw = slothVelocityPitch = 0.0F;
        slothAimPointX = slothAimPointY = slothAimPointZ = 0.0;
        slothNoiseAngle = 0.0F;
        slothHitPhase = slothHitTimer = 0;
        slothFirstSeenTime = 0;
        slothReactionComplete = false;
        slothReactionMs = 0;

        if (mc.player != null) {
            slothCurrentYaw = mc.player.getYaw();
            slothCurrentPitch = mc.player.getPitch();
            slothLastSentYaw = slothCurrentYaw;
            slothLastSentPitch = slothCurrentPitch;
            slothSmoothYaw = slothCurrentYaw;
            slothSmoothPitch = slothCurrentPitch;
        } else {
            slothCurrentYaw = slothCurrentPitch = 0.0F;
            slothLastSentYaw = slothLastSentPitch = 0.0F;
            slothSmoothYaw = slothSmoothPitch = 0.0F;
        }
    }

    private float slCalcGcd() {
        double s = mc.options.getMouseSensitivity().getValue() * 0.6 + 0.2;
        return (float) (s * s * s * 1.2);
    }

    private void slPickAimPoint(LivingEntity e) {
        Box bb = e.getBoundingBox();
        double w = bb.maxX - bb.minX;
        double h = bb.maxY - bb.minY;
        double d = bb.maxZ - bb.minZ;

        slothAimPointX = (Math.random() - 0.5) * w * 0.12;
        slothAimPointY = (Math.random() - 0.5) * h * 0.11;
        slothAimPointZ = (Math.random() - 0.5) * d * 0.12;
    }

    public void slOnAttack() {
        slothHitPhase = 1;
        slothHitTimer = 0;
        slothPitchBeforeHit = slothCurrentPitch;
    }

    private float slMeasureAngle(LivingEntity e) {
        if (mc.player == null) return 0.0F;

        Vec3d eyes = mc.player.getEyePos();
        Vec3d mid = e.getBoundingBox().getCenter();
        Vec3d delta = mid.subtract(eyes);

        float needYaw = (float) Math.toDegrees(Math.atan2(delta.z, delta.x)) - 90.0F;
        float needPitch = (float) -Math.toDegrees(Math.atan2(delta.y, delta.horizontalLength()));

        float dYaw = Math.abs(MathHelper.wrapDegrees(needYaw - mc.player.getYaw()));
        float dPitch = Math.abs(needPitch - mc.player.getPitch());

        return dYaw + dPitch;
    }

    private int slComputeReaction(float angle) {
        if (angle > 130.0F) return 0 + (int) (Math.random() * 10);
        if (angle > 70.0F) return 0 + (int) (Math.random() * 10);
        if (angle > 30.0F) return 0 + (int) (Math.random() * 10);
        return 0 + (int) (Math.random() * 5);
    }

    private boolean slIsMovingForward() {
        if (mc.player == null) return false;
        return mc.options.forwardKey.isPressed();
    }

    private boolean slIsOvertakingTarget(LivingEntity target) {
        if (mc.player == null || target == null) return false;

        Vec3d playerPos = mc.player.getPos();
        Vec3d targetPos = target.getPos();

        Vec3d playerVel = new Vec3d(
                mc.player.getX() - mc.player.prevX,
                mc.player.getY() - mc.player.prevY,
                mc.player.getZ() - mc.player.prevZ
        );

        Vec3d targetVel = new Vec3d(
                target.getX() - target.prevX,
                target.getY() - target.prevY,
                target.getZ() - target.prevZ
        );

        Vec3d toTarget = targetPos.subtract(playerPos).normalize();

        double playerSpeedToTarget = playerVel.dotProduct(toTarget);
        double targetSpeedToPlayer = targetVel.dotProduct(toTarget.multiply(-1));

        double relativeSpeed = playerSpeedToTarget + targetSpeedToPlayer;

        double distance = Math.sqrt(
                Math.pow(playerPos.x - targetPos.x, 2) +
                        Math.pow(playerPos.z - targetPos.z, 2)
        );

        return relativeSpeed > 0.05 && distance < 4.0;
    }

    private float[] slGenerateNoise(float dist) {
        slothNoiseAngle += 0.042F + (float)(Math.random() * 0.018F);

        float scale = MathHelper.clamp(dist / 4.5F, 0.25F, 1.0F);
        float amp = slothNoiseAmplitude * scale;

        float n1 = (float) Math.sin(slothNoiseAngle * 0.87) * 0.38F;
        float n2 = (float) Math.sin(slothNoiseAngle * 1.43 + 0.75) * 0.28F;
        float n3 = (float) Math.cos(slothNoiseAngle * 1.18 + 0.35) * 0.32F;
        float n4 = (float) Math.cos(slothNoiseAngle * 1.76 + 1.42) * 0.23F;

        float yawNoise = (n1 + n2) * amp;
        float pitchNoise = (n3 + n4) * amp * 0.52F;

        yawNoise += ((float) Math.random() - 0.5F) * amp * 0.13F;
        pitchNoise += ((float) Math.random() - 0.5F) * amp * 0.09F;

        return new float[]{yawNoise, pitchNoise};
    }

    private float slSmoothStep(float x) {
        x = MathHelper.clamp(x, 0.0F, 1.0F);
        return x * x * (3.0F - 2.0F * x);
    }

    private float slAccelCurve(float x) {
        x = MathHelper.clamp(x, 0.0F, 1.0F);
        return 1.0F - (1.0F - x) * (1.0F - x);
    }

    private float slSpringInterp(float current, float target, float vel, float stiffness, float damping) {
        float diff = target - current;
        float acc = diff * stiffness - vel * damping;
        return vel + acc;
    }

    private float slSmoothLerp(float from, float to, float alpha) {
        alpha = MathHelper.clamp(alpha, 0.0F, 1.0F);
        float delta = MathHelper.wrapDegrees(to - from);
        return from + delta * alpha;
    }

    private Vec3d slGetTargetPosition(LivingEntity target, boolean bothGliding) {
        if (target.isGliding()) {
            return PredictUtils.predict(target, predictValue.getValue());
        } else {
            return PredictUtils.predict(target, predictValue.getValue());
        }
    }

    private void updateSlothRotation(LivingEntity target) {
        if (mc.player == null || target == null) return;

        boolean playerFlying = mc.player.isGliding();
        boolean targetFlying = target.isGliding();
        boolean bothGliding = playerFlying && targetFlying;

        if (slothTrackedTarget != target) {
            slothTrackedTarget = target;

            slothCurrentYaw = mc.player.getYaw();
            slothCurrentPitch = mc.player.getPitch();
            slothLastSentYaw = slothCurrentYaw;
            slothLastSentPitch = slothCurrentPitch;
            slothSmoothYaw = slothCurrentYaw;
            slothSmoothPitch = slothCurrentPitch;
            slothVelocityYaw = slothVelocityPitch = 0.0F;

            slPickAimPoint(target);

            slothHitPhase = slothHitTimer = 0;
            slothNoiseAngle = (float) (Math.random() * Math.PI * 2);

            float angleDiff = slMeasureAngle(target);
            slothReactionMs = slComputeReaction(angleDiff);
            slothFirstSeenTime = System.currentTimeMillis();
            slothReactionComplete = false;
        }

        Vec3d eyePos = mc.player.getEyePos();
        Vec3d targetCenter = slGetTargetPosition(target, bothGliding);
        float distance = (float) eyePos.distanceTo(targetCenter);

        float gcd = slCalcGcd();

        if (!slothReactionComplete) {
            long elapsed = System.currentTimeMillis() - slothFirstSeenTime;

            if (elapsed < slothReactionMs) {
                float jitterY = ((float) Math.random() - 0.5F) * 0.22F;
                float jitterP = ((float) Math.random() - 0.5F) * 0.14F;

                float outY = slothLastSentYaw + jitterY;
                float outP = MathHelper.clamp(slothLastSentPitch + jitterP, -89.0F, 89.0F);

                outY -= (outY - slothLastSentYaw) % gcd;
                outP -= (outP - slothLastSentPitch) % gcd;

                slothLastSentYaw = outY;
                slothLastSentPitch = outP;

                RotationComponent.update(new Rotation(outY, outP), 360, 360, 360, 360, 0, 1, clientLook.getValue());
                return;
            }

            slothReactionComplete = true;
        }

        float[] noise = slGenerateNoise(distance);

        if (slothHitPhase > 0) {
            slothHitTimer++;

            int upDuration = 25;
            int downDuration = 20;
            float targetPitchUp = -89.0F;

            if (slothHitPhase == 1) {
                float t = slothHitTimer / (float) upDuration;
                t = MathHelper.clamp(t, 0.0F, 1.0F);
                float curved = slAccelCurve(t);
                slothCurrentPitch = MathHelper.lerp(curved, slothPitchBeforeHit, targetPitchUp);

                if (slothHitTimer >= upDuration) {
                    slothHitPhase = 2;
                    slothHitTimer = 0;
                }
            } else if (slothHitPhase == 2) {
                float goal = slothPitchBeforeHit;
                float t = slothHitTimer / (float) downDuration;
                t = MathHelper.clamp(t, 0.0F, 1.0F);
                float curved = slSmoothStep(t);
                slothCurrentPitch = MathHelper.lerp(curved, targetPitchUp, goal);

                if (slothHitTimer >= downDuration) {
                    slothHitPhase = 0;
                    slothHitTimer = 0;
                }
            }

            float outY = slothCurrentYaw + noise[0];
            float outP = MathHelper.clamp(slothCurrentPitch + noise[1], -89.0F, 89.0F);

            outY -= (outY - slothLastSentYaw) % gcd;
            outP -= (outP - slothLastSentPitch) % gcd;

            slothLastSentYaw = outY;
            slothLastSentPitch = outP;

            RotationComponent.update(new Rotation(outY, outP), 360, 360, 360, 360, 0, 1, clientLook.getValue());
            return;
        }

        if (Math.random() < 0.015) {
            slPickAimPoint(target);
        }

        Vec3d aimPos = targetCenter.add(slothAimPointX, slothAimPointY, slothAimPointZ);
        Vec3d direction = aimPos.subtract(eyePos);

        float wantYaw = (float) MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(direction.z, direction.x)) - 90.0);
        float wantPitch = (float) -Math.toDegrees(Math.atan2(direction.y, direction.horizontalLength()));

        float diffYaw = MathHelper.wrapDegrees(wantYaw - slothCurrentYaw);
        float diffPitch = wantPitch - slothCurrentPitch;

        float speedMultiplier = 1.0F;

        boolean movingForward = slIsMovingForward();
        boolean overtaking = slIsOvertakingTarget(target);

        if (movingForward || overtaking) {
            speedMultiplier = 0.5F;
        }

        if (bothGliding) {
            speedMultiplier = 0.42995F;
        }

        float stiffness = (0.03686F + (float) Math.random() * 0.00873F) * speedMultiplier;
        float damping = 0.68F + (0.12F * (1.0F - speedMultiplier));

        float totalDiff = (float) Math.sqrt(diffYaw * diffYaw + diffPitch * diffPitch);

        if (totalDiff > 32.0F) {
            stiffness += 0.018F * speedMultiplier;
        } else if (totalDiff < 4.2F) {
            stiffness *= 0.48F;
        }

        stiffness += MathHelper.clamp((distance - 1.6F) / 7.5F, 0.0F, 0.045F) * speedMultiplier;

        slothVelocityYaw = slSpringInterp(slothCurrentYaw, slothCurrentYaw + diffYaw, slothVelocityYaw, stiffness, damping);
        slothVelocityPitch = slSpringInterp(slothCurrentPitch, wantPitch, slothVelocityPitch, stiffness * 0.87F, damping);

        float maxVelYaw = 7.1659F * speedMultiplier;
        float maxVelPitch = 5.5416F * speedMultiplier;

        slothVelocityYaw = MathHelper.clamp(slothVelocityYaw, -maxVelYaw, maxVelYaw);
        slothVelocityPitch = MathHelper.clamp(slothVelocityPitch, -maxVelPitch, maxVelPitch);

        slothCurrentYaw += slothVelocityYaw;
        slothCurrentPitch += slothVelocityPitch;

        slothCurrentPitch = MathHelper.clamp(slothCurrentPitch, -89.0F, 89.0F);

        float smoothFactor = bothGliding ? 0.35F : 0.85F;

        slothSmoothYaw = slSmoothLerp(slothSmoothYaw, slothCurrentYaw, smoothFactor);
        slothSmoothPitch = slSmoothLerp(slothSmoothPitch, slothCurrentPitch, smoothFactor * 0.95F);

        float outY = slothSmoothYaw + noise[0];
        float outP = slothSmoothPitch + noise[1];

        outP = MathHelper.clamp(outP, -89.0F, 89.0F);

        outY -= (outY - slothLastSentYaw) % gcd;
        outP -= (outP - slothLastSentPitch) % gcd;

        slothLastSentYaw = outY;
        slothLastSentPitch = outP;

        RotationComponent.update(new Rotation(outY, outP), 360, 360, 360, 360, 0, 1, clientLook.getValue());
    }

    private void updateLonyJirRotation(LivingEntity target) {
        double time = System.nanoTime() * 1e-9;
        var angle = new Rotation(RotationUtil.calculate(target.getBoundingBox().getCenter().add(0, (float) Math.abs(Math.sin(time * 19)) / 2, 0)));
        var predict = PredictUtils.predict(target, predictValue.getValue() + 2.5f);

        if (target.isGliding() && predictate.getValue() && !isTurnaroundActive) angle = new Rotation(predict);

        if (!RaytraceUtil.rayTrace(mc.player.getRotationVector(), 999, target.getBoundingBox().expand(-0.2f))) {
            speedAcceleration += (float) Math.abs(Math.sin(time * 19)) / 666;
        } else {
            if (speedAcceleration >= 0.02f)
                speedAcceleration -= 0.02f;
        }

        var deltaYaw = MathHelper.wrapDegrees(angle.getYaw() - lastYaw);
        var deltaPitch = angle.getPitch() - lastPitch;

        var smooth = Math.min(Math.max(speedAcceleration, 0), 0.2f);

        var newYaw = lastYaw + deltaYaw * smooth;
        var newPitch = lastPitch + deltaPitch * (smooth / 3);

        newYaw -= (newYaw - lastYaw) % GCDFixer.getGCDValue();
        newPitch -= (newPitch - lastPitch) % GCDFixer.getGCDValue();

        var smoothRot = new Rotation(newYaw, newPitch);

        var deltaYaw2 = MathHelper.wrapDegrees(mc.gameRenderer.getCamera().getYaw() - lastYaw);
        var deltaPitch2 = mc.gameRenderer.getCamera().getPitch() - lastPitch;

        if (mc.options.getPerspective() == Perspective.THIRD_PERSON_FRONT) {
            deltaYaw2 = MathHelper.wrapDegrees((mc.gameRenderer.getCamera().getYaw() - 180) - lastYaw);
            deltaPitch2 = -mc.gameRenderer.getCamera().getPitch() - lastPitch;
        }

        if (mc.player.isGliding() && target.isGliding())
            RotationComponent.update(smoothRot, 360, 360, 360, 360, 0, 1, clientLook.getValue());
        lastYaw = smoothRot.getYaw();
        lastPitch = smoothRot.getPitch();
    }



    private void updateFunTimeRotation(LivingEntity target) {
        if (target == null || mc.player == null) {
            return;
        }

        Vec3d eye = mc.player.getEyePos();
        double reach = distance.getValue();
        boolean throughWalls = true;
        Vec3d targetOffset = FunTimeAuraUtil.getAimOffset(eye, target, reach, throughWalls);
        float yawToTarget = targetOffset == Vec3d.ZERO
                ? FreeLookComponent.getFreeYaw()
                : (float) MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(targetOffset.z, targetOffset.x)) - 90.0D);
        float pitchToTarget = targetOffset == Vec3d.ZERO
                ? FreeLookComponent.getFreePitch()
                : (float) (-Math.toDegrees(Math.atan2(targetOffset.y, Math.hypot(targetOffset.x, targetOffset.z))));

        System.arraycopy(funTimePitchHistory, 0, funTimePitchHistory, 1, 29);
        funTimePitchHistory[0] = pitchToTarget;

        float targetHealth = Server.getHealth(target, false);
        if (funTimeTicksSinceAttack >= 2
                && ((targetHealth > 6.0F || funTimeState[2] > 43.0F) && funTimeState[2] >= 33.0F
                && ((funTimeTicksSinceAttack == 4 || Math.random() > 0.5D)
                && (!funTimeMissToggle || !FunTimeAuraUtil.isLookingAt(
                        mc.player.getYaw(), mc.player.getPitch(), 3.0D, target, false))))) {
            mc.interactionManager.attackEntity(mc.player, target);
            mc.player.swingHand(Hand.MAIN_HAND);
            if (Math.random() > 0.5D) {
                funTimeMissToggle = !funTimeMissToggle;
            }
            funTimeState[2] = (int) MathUtil.random(-10.0F, 10.0F);
        }

        boolean skipChecks = (mc.player.isUsingItem() && mc.player.getItemUseTimeLeft() > 0 && funTimeTicksSinceAttack >= 8)
                || mc.currentScreen != null;
        if ((funTimeState[3] <= 0.0F && canAttack())
                || FunTimeAuraUtil.shouldPrepareCrit(funTimeTicksSinceAttack, target, skipChecks, 3.0D)) {
            funTimeState[3] = 1.0F;
            if (!mc.player.isTouchingWater() && !mc.player.isOnGround()) {
                funTimeState[0] = 1.0F;
            }
        }

        applyFunTimeRotation(yawToTarget, pitchToTarget);

        funTimeState[3]--;
        funTimeState[5]--;
        funTimeState[8]--;
        funTimeFocusYaw = (float) MathHelper.wrapDegrees(
                Math.toDegrees(Math.atan2(target.getZ() - mc.player.getZ(), target.getX() - mc.player.getX())) - 90.0D
        );
    }

    private void updateFocusYaw() {
        if (target == null || mc.player == null) return;
        funTimeFocusYaw = (float) MathHelper.wrapDegrees(
                Math.toDegrees(Math.atan2(target.getZ() - mc.player.getZ(), target.getX() - mc.player.getX())) - 90.0D
        );
    }

    private void applyFunTimeRotation(float yawToTarget, float pitchToTarget) {
        float t = mc.player.age + mc.getRenderTickCounter().getTickDelta(false);
        float smoothW = (float) ((Math.sin(t * 0.4000000008323731D) * 3.0D)
                + (Math.sin((t * 0.9500002390239708D) + 1.4000004888461306D) * 2.0D));
        float smoothH = (float) ((Math.cos((t * 0.5D) + 0.7000001555309916D) * 0.5D)
                + (Math.cos((t * 0.7800000620494261D) + 3.10000031689524D) * 1.5D));

        int historyIndex = MathHelper.clamp(10 - funTimeTicksSinceAttack, 0, 29);
        float finalPitch = FunTimeAuraUtil.rotateStep(
                mc.player.getPitch(),
                funTimePitchHistory[historyIndex] + (smoothH * 1.5F),
                FunTimeAuraUtil.random(0.1F, 0.5F)
        );
        float finalYaw = FunTimeAuraUtil.rotateStep(
                mc.player.getYaw(),
                yawToTarget + smoothW,
                FunTimeAuraUtil.random(0.1F, 0.4F)
        );

        if (funTimeState[3] >= 0.0F) {
            if (!FunTimeAuraUtil.isLookingAt(mc.player.getYaw(), mc.player.getPitch(), distance.getValue(), target, true)
                    && funTimeState[8] <= 0.0F) {
                finalYaw = yawToTarget;
            }
            if (!FunTimeAuraUtil.isLookingAt(yawToTarget, finalPitch, distance.getValue(), target, true)
                    && funTimeState[8] <= 0.0F) {
                finalPitch = pitchToTarget;
            }
            if (!FunTimeAuraUtil.isLookingAt(mc.player.getYaw() + smoothW, mc.player.getPitch() + smoothH, distance.getValue(), target, true)
                    && FunTimeAuraUtil.isLookingAt(mc.player.getYaw(), mc.player.getPitch(), distance.getValue(), target, true)) {
                smoothW = MathHelper.clamp(smoothW, -0.05F, 0.05F);
                smoothH = MathHelper.clamp(smoothH, -0.05F, 0.05F);
            }
        }

        if (funTimeTicksSinceAttack <= 4 && funTimeState[2] % 2.0F == 0.0F) {
            finalYaw = mc.player.getYaw();
        }

        float outputPitch = FreeLookComponent.getFreePitch() + smoothH;
        var funTimeRot = new Rotation(finalYaw + smoothW, outputPitch);
        RotationComponent.update(funTimeRot, 220.0F, 220.0F, 220.0F, 220.0F, 0, 1, clientLook.getValue());
        lastYaw = funTimeRot.getYaw();
        lastPitch = funTimeRot.getPitch();
    }

    private void funTimeReset() {
        if (funTimeState[9] == -1.0F) {
            funTimeState[9] = (int) MathUtil.random(9.0F, 13.0F);
        }
        funTimeState[8] = 2.0F;
        funTimeState[10] = 0.0F;
        funTimeState[11] = 0.0F;
        funTimeTicksSinceAttack = 0;
        Arrays.fill(funTimePitchHistory, mc.player != null ? mc.player.getPitch() : 0.0F);
    }

    private void funTimeOnTargetChanged() {
        funTimeState[10] = 0.0F;
        funTimeState[11] = 0.0F;
        funTimeTicksSinceAttack = 0;
        Arrays.fill(funTimePitchHistory, mc.player != null ? mc.player.getPitch() : 0.0F);
    }

    private void funTimeOnAttack() {
        funTimeTicksSinceAttack = 0;
        funTimeState[5] = FunTimeAuraUtil.random(8.0F, 10.0F);
        funTimeState[9] = (int) MathUtil.random(9.0F, 13.0F);
        if (funTimeState[2] == -1.0F) {
            funTimeState[4] = (int) MathUtil.random(30.0F, 35.0F);
        }
    }

    private void slothTest(LivingEntity target) {
        if (target == null) return;

        Vec3d point = resolveMultipoint(target, BestPoint.getPoint2(target), 6);
        if (target.isGliding() && predictate.getValue() && !isTurnaroundActive) {
            point = PredictUtils.predict(target, predictValue.getValue());
        }
        boolean isLooking = RaytraceUtil.rayTrace(mc.player.getRotationVector(), 6, target.getBoundingBox().expand(-0,-1,-0));
        var idealRotation = new Rotation(RotationUtil.calculate(point));
        float targetYaw = idealRotation.getYaw();
        float targetPitch = idealRotation.getPitch();
        float randomFactor = (float) Math.random();

        float deltaYaw = MathHelper.wrapDegrees(targetYaw - lastYaw);
        float deltaPitch = targetPitch - lastPitch;

        float distance = mc.player.distanceTo(target) / 30 ;
        if(!isLooking && mc.player.getAttackCooldownProgress(1) >= 0.7f){
            distance += 0.03f / 1.5f;

            stopWatch.reset();
        }
        if(!isLooking ){
            distance += 0.0075f / 1.5f;

            stopWatch.reset();
        }
        else{
            distance *= 0.15f + (randomFactor * 0.2f);

        }
        var smooth = Math.min(Math.max(distance, 0), 0.12f);

        float newYaw = lastYaw + (deltaYaw) * smooth;
        float newPitch = lastPitch + (deltaPitch * 0.5f) * smooth;

        float gcd = GCDFixer.getGCDValue();
        newYaw -= (newYaw - lastYaw) % gcd;
        newPitch -= (newPitch - lastPitch) % gcd;

        newPitch = MathHelper.clamp(newPitch, -90f, 90f);

        var legitRot = new Rotation(newYaw, newPitch);

        RotationComponent.update(legitRot, 360, 360, 360, 360, 0, 1, clientLook.getValue());

        lastYaw = legitRot.getYaw();
        lastPitch = legitRot.getPitch();
    }

    private void updateLonyGriefRotation(LivingEntity target) {
        Vec3d point = target.isGliding() && predictate.getValue() && !isTurnaroundActive ? PredictUtils.predict(target, predictValue.getValue()) : resolveMultipoint(target, BestPoint.getPoint(target), 6);

        var angle = new Rotation(RotationUtil.calculate(point));
        float targetYaw = angle.getYaw();
        float targetPitch = angle.getPitch();

        if (!back) {
            float pon = mc.player.isGliding() ? 1.35f : 1f;
            speedAcceleration += (Math.abs(MathHelper.wrapDegrees(targetYaw - lastYaw)) > 40 ? 0.005f / pon : 0.0038f / pon);

            boolean isLooking = RaytraceUtil.rayTrace(mc.player.getRotationVector(), 6, target.getBoundingBox().expand(-0.2, -0.3, -0.2));
            if (speedAcceleration >= 0.16f / pon || isLooking) {
                back = true;
            }
        } else {
            if (speedAcceleration >= -0.01f) {
                speedAcceleration -= (Math.abs(MathHelper.wrapDegrees(targetYaw - lastYaw)) > 60 ? 0.06f : 0.01f);
            }
            if (speedAcceleration <= -0.01f) {
                back = false;
            }
        }

        float deltaYaw = MathHelper.wrapDegrees(targetYaw - lastYaw);
        float deltaPitch = targetPitch - lastPitch;
        float smooth = Math.max(speedAcceleration, 0);

        float newYaw = lastYaw + deltaYaw * Math.min(Math.max(smooth, 0), 1);
        float newPitch = lastPitch + deltaPitch * Math.min(Math.max(smooth / 2, 0), 1);

        float gcdValue = GCDFixer.getGCDValue();
        newYaw -= (newYaw - lastYaw) % gcdValue;
        newPitch -= (newPitch - lastPitch) % gcdValue;

        var smoothRot = new Rotation(newYaw, newPitch);
        RotationComponent.update(smoothRot, 360, 360, 360, 360, 0, 1, clientLook.getValue());

        lastYaw = smoothRot.getYaw();
        lastPitch = smoothRot.getPitch();
    }

    private void updateWellmineRotation(LivingEntity target) {
        var box = target.getBoundingBox();
        Vec3d vector = resolveMultipoint(target, BestPoint.getMultipoint(target, 6), 6);

        if (target.isGliding() && predictate.getValue() && !isTurnaroundActive) {
            vector = PredictUtils.predict(target, predictValue.getValue());
        }

        var angle = RotationUtil.calculate(vector);

        float targetYaw = angle.x;
        float targetPitch = angle.y;

        if (!back) {
            if (speedAcceleration >= 1f) {
                speedAcceleration = 0 ;
            } else {
                if(mc.player.isGliding()){
                    float diff = Math.abs(MathHelper.wrapDegrees(angle.x - mc.player.getYaw()));
                    speedAcceleration += (diff > 40 ? 0.0025f : 0.005f);
                }
                else{
                    speedAcceleration += 0.005f ;

                }
            }

            Vec3d offset = Vec3d.ZERO;
            if (mc.player.isGliding() && target instanceof PlayerEntity && target.isGliding()) {
                offset = PredictUtils.predict(target, predictValue.getValue());
            }

            if (speedAcceleration >= 0.18 || RaytraceUtil.rayTrace(mc.player.getRotationVector(), 6, box.offset(offset).expand(-0.5, -1, -0.5))) {
                back = true;
            }
        } else {
            if (speedAcceleration >= -0.01f) {
                float diff = Math.abs(MathHelper.wrapDegrees(targetYaw - mc.player.getYaw()));
                speedAcceleration -= (diff > 40 ? 0.04f : 0.01f);
            }
            if (speedAcceleration <= -0.01f) back = false;
        }

        float randomYaw = (float) java.util.concurrent.ThreadLocalRandom.current().nextDouble(-RANDOM_STRENGTH, RANDOM_STRENGTH);
        float randomPitch = (float) java.util.concurrent.ThreadLocalRandom.current().nextDouble(-RANDOM_STRENGTH, RANDOM_STRENGTH);

        targetYaw += randomYaw;
        targetPitch += randomPitch;

        float smoothVal = Math.min(Math.max(speedAcceleration, -1), 1);

        float changeYaw = MathHelper.wrapDegrees(targetYaw - mc.player.getYaw()) * smoothVal;
        float changePitch = (targetPitch - mc.player.getPitch()) * (smoothVal / 2f);

        var smoothRot = new Rotation(
                mc.player.getYaw() + changeYaw,
                MathHelper.clamp(mc.player.getPitch() + changePitch, -90, 90)
        );

        RotationComponent.update(smoothRot, 360, 360, 360, 360, 0, 1, clientLook.getValue());

        lastYaw = smoothRot.getYaw();
        lastPitch = smoothRot.getPitch();
    }

    private Vec3d resolveMultipoint(LivingEntity target, Vec3d point, double range) {
        if (!smartAim.getValue() || target == null) {
            return point;
        }

        return BestPoint.getNearestVisiblePoint(target, point, range);
    }

    private float applyGCD(float deltaRotation) {
        float sensitivity = (float) (mc.options.getMouseSensitivity().getValue() * 0.6f + 0.2f);
        float multiplier = sensitivity * sensitivity * sensitivity * 8.0f * 0.15f;
        return (Math.round(deltaRotation / multiplier) * multiplier);
    }

    private void updateAssistRotation(LivingEntity target) {
        if (target == null) return;

        boolean elytraDuel = mc.player.isGliding();

        if (!elytraDuel && System.currentTimeMillis() - lastPhysicalMoveTime > 100) {
            return;
        } else if (System.currentTimeMillis() - lastPhysicalMoveTime < 100) {
            this.lastYaw = mc.player.getYaw();
            this.lastPitch = mc.player.getPitch();
        }

        Vec3d point = resolveMultipoint(target, BestPoint.getPoint(target), 6);

        if (elytraDuel && target.isGliding() && predictate.getValue() && !isTurnaroundActive) {
            point = PredictUtils.predict(target, predictValue.getValue());
        }

        Vec3d eyePos = mc.player.getEyePos();
        double deltaX = point.x - eyePos.x;
        double deltaY = point.y - eyePos.y;
        double deltaZ = point.z - eyePos.z;
        double distance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

        float targetYaw = (float) (Math.toDegrees(Math.atan2(deltaZ, deltaX)) - 90.0);
        float targetPitch = (float) (-Math.toDegrees(Math.atan2(deltaY, distance)));

        float radius = 0.0f;
        var box = target.getBoundingBox();
        boolean isAimed = RaytraceUtil.rayTrace(mc.player.getRotationVector(), 6, box.expand(radius, radius, radius));
        float speed;
        if (isAimed) {
            speed = 0.25f * 0.25f;
        } else if (mc.player.isGliding()) {
            speed = 2.0f * 4.0f;
        } else if (mc.player.isOnGround()) {
            speed = 2.0f * 0.25f;
        } else {
            speed = 2.0f * 0.25f;
        }

        float cooldownMultiplier = (mc.player.getAttackCooldownProgress(0.5f) > 0.85f) ? 1.2f : 0.4f;
        speed *= cooldownMultiplier;

        if (!RaytraceUtil.rayTrace(mc.player.getRotationVector(), 6, box.expand(radius + 0.1f, radius + 0.1f, radius + 0.1f))) {
            speedAcceleration += 0.0005f * cooldownMultiplier;
        } else if (speedAcceleration >= -0.01f) {
            speedAcceleration -= 0.0004f;
        }

        float smooth = Math.max(speedAcceleration, 0);
        speed += smooth + (float) ((Math.random() - 0.5) * 0.02);

        float yawDelta = MathHelper.wrapDegrees(targetYaw - lastYaw);
        float pitchDelta = targetPitch - lastPitch;

        float clampedYawDelta = MathHelper.clamp(yawDelta, -speed, speed);
        float clampedPitchDelta = MathHelper.clamp(pitchDelta, -speed, speed);

        float newYaw = lastYaw + clampedYawDelta;
        float newPitch = MathHelper.clamp(lastPitch + clampedPitchDelta, -89.9F, 89.9F);

        float gcd = GCDFixer.getGCDValue();
        if (gcd > 0.0F) {
            newYaw = lastYaw + (float) Math.round((newYaw - lastYaw) / gcd) * gcd;
            newPitch = lastPitch + (float) Math.round((newPitch - lastPitch) / gcd) * gcd;
        }

        var smoothRot = new Rotation(newYaw, newPitch);
        RotationComponent.update(smoothRot, 360, 360, 360, 360, 0, 1, clientLook.getValue());

        this.lastYaw = smoothRot.getYaw();
        this.lastPitch = smoothRot.getPitch();
    }

    private void renderPredictPoint(MatrixStack matrices, Camera camera, float tickDelta) {
        if (target == null || !target.isGliding()) return;

        Vec3d predictPos = PredictUtils.predict(target, predictValue.getValue());
        Vec3d camPos = camera.getPos();

        double renderX = predictPos.x - camPos.x;
        double renderY = predictPos.y - camPos.y;
        double renderZ = predictPos.z - camPos.z;

        float size = 0.35f;
        int color = ColorProvider.getColorClient();

        matrices.push();
        matrices.translate(renderX, renderY, renderZ);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);

        Matrix4f matrix = matrices.peek().getPositionMatrix();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);

        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;
        float a = 1;

        buffer.vertex(matrix, -size, -size, -size).color(r, g, b, a);
        buffer.vertex(matrix, size, -size, -size).color(r, g, b, a);

        buffer.vertex(matrix, size, -size, -size).color(r, g, b, a);
        buffer.vertex(matrix, size, -size, size).color(r, g, b, a);

        buffer.vertex(matrix, size, -size, size).color(r, g, b, a);
        buffer.vertex(matrix, -size, -size, size).color(r, g, b, a);

        buffer.vertex(matrix, -size, -size, size).color(r, g, b, a);
        buffer.vertex(matrix, -size, -size, -size).color(r, g, b, a);

        buffer.vertex(matrix, -size, size, -size).color(r, g, b, a);
        buffer.vertex(matrix, size, size, -size).color(r, g, b, a);

        buffer.vertex(matrix, size, size, -size).color(r, g, b, a);
        buffer.vertex(matrix, size, size, size).color(r, g, b, a);

        buffer.vertex(matrix, size, size, size).color(r, g, b, a);
        buffer.vertex(matrix, -size, size, size).color(r, g, b, a);

        buffer.vertex(matrix, -size, size, size).color(r, g, b, a);
        buffer.vertex(matrix, -size, size, -size).color(r, g, b, a);

        buffer.vertex(matrix, -size, -size, -size).color(r, g, b, a);
        buffer.vertex(matrix, -size, size, -size).color(r, g, b, a);

        buffer.vertex(matrix, size, -size, -size).color(r, g, b, a);
        buffer.vertex(matrix, size, size, -size).color(r, g, b, a);

        buffer.vertex(matrix, size, -size, size).color(r, g, b, a);
        buffer.vertex(matrix, size, size, size).color(r, g, b, a);

        buffer.vertex(matrix, -size, -size, size).color(r, g, b, a);
        buffer.vertex(matrix, -size, size, size).color(r, g, b, a);

        BufferRenderer.drawWithGlobalProgram(buffer.end());

        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();

        matrices.pop();
    }

    @Override
    public void onEnable() {
        target = null;
        razvorotikTicks = 0;
        CrickClient.getInstance().getModuleStorage().setSpeedAcceleration(0);
        slReset();
        funTimeReset();
        spookyTimeAura.reset();

        if (this.elytraTarget == null) {
            this.elytraTarget = CrickClient.getInstance().getModuleStorage().get(ElytraTarget.class);
        }

        if (!renderListenerRegistered) {
            WorldRenderEvents.LAST.register(renderListener);
            renderListenerRegistered = true;
        }

        super.onEnable();
    }

    @Override
    public void onDisable() {
        target = null;
        ticksToAttack = 0;
        speedAcceleration = 0;
        razvorotikTicks = 0;
        isResolving = false;
        resolverPoint = null;
        CrickClient.getInstance().getModuleStorage().setSpeedAcceleration(0);
        CrickClient.getInstance().getModuleStorage().setRandomness(1);
        slReset();
        super.onDisable();
    }
}






