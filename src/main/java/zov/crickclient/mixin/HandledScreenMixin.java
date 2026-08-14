package zov.crickclient.mixin;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.slot.Slot;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import zov.crickclient.CrickClient;
import zov.crickclient.event.list.ContainerEvent;
import zov.crickclient.event.list.EventHandledScreen;
import zov.crickclient.event.list.EventKeyInput;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin {

    @Shadow
    @Nullable
    protected Slot focusedSlot;

    @Inject(method = "render", at = @At("HEAD"))
    private void onRenderHead(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        CrickClient.getInstance().getEventBus().post(new ContainerEvent((HandledScreen<?>) (Object) this, context, mouseX, mouseY, ContainerEvent.Phase.PRE));
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void onRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        CrickClient.getInstance().getEventBus().post(new EventHandledScreen(focusedSlot, context, mouseX, mouseY));
        CrickClient.getInstance().getEventBus().post(new ContainerEvent((HandledScreen<?>) (Object) this, context, mouseX, mouseY, ContainerEvent.Phase.POST));
    }

    @Inject(method = "keyPressed", at = @At("HEAD"))
    private void onKeyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        CrickClient.getInstance().getEventBus().post(new EventKeyInput(keyCode, 1));
    }
}