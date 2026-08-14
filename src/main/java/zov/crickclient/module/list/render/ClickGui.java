package zov.crickclient.module.list.render;

import org.lwjgl.glfw.GLFW;
import zov.crickclient.module.Module;
import zov.crickclient.module.ModuleCategory;
import zov.crickclient.module.ModuleInformation;
import zov.crickclient.module.settings.SliderSetting;
import zov.crickclient.ui.ClickGuiFrame;

@ModuleInformation(moduleName = "Click Gui", moduleDesc = "Графическое меню настройки модулей", moduleCategory = ModuleCategory.RENDER, moduleKeybind = GLFW.GLFW_KEY_RIGHT_SHIFT)
public class ClickGui extends Module {

    public final SliderSetting size =
            new SliderSetting("Размер", 0.80, 0.7, 1.4, 0.05);

    private ClickGuiFrame clickGuiFrame;

    @Override
    public void onEnable() {
        if (clickGuiFrame == null) clickGuiFrame = new ClickGuiFrame();
        mc.setScreen(clickGuiFrame);
        clickGuiFrame.playOpenAnimation();
        toggle();
    }
}
