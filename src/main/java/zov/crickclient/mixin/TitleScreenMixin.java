package zov.crickclient.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import zov.crickclient.ui.menu.AlphaTitleScreen;

@Mixin(MinecraftClient.class)
public class TitleScreenMixin {
    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    private void replaceTitleScreen(Screen screen, CallbackInfo ci) {
        if (screen != null && screen.getClass() == TitleScreen.class) {
            MinecraftClient.getInstance().setScreen(new AlphaTitleScreen());
            ci.cancel();
        }
    }
}
