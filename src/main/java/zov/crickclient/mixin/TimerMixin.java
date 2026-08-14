package zov.crickclient.mixin;

import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import zov.crickclient.util.timer.TimerManager;

@Mixin(MinecraftClient.class)
public abstract class TimerMixin {
    @Unique
    private long crickclientLastRealTime = 0L;
    @Unique
    private long crickclientFakeTime = 0L;

    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/RenderTickCounter$Dynamic;beginRenderTick(JZ)I"), index = 0)
    private long modifyRenderTime(long currentTime) {
        float speed = TimerManager.getTimer();
        if (speed == 1.0f) {
            this.crickclientLastRealTime = currentTime;
            this.crickclientFakeTime = currentTime;
            return currentTime;
        }
        if (this.crickclientLastRealTime == 0L) {
            this.crickclientLastRealTime = currentTime;
            this.crickclientFakeTime = currentTime;
            return currentTime;
        }
        long realElapsed = currentTime - this.crickclientLastRealTime;
        this.crickclientLastRealTime = currentTime;
        this.crickclientFakeTime += (long) ((double) realElapsed * (double) speed);
        return this.crickclientFakeTime;
    }
}
