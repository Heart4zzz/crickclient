package zov.crickclient;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import lombok.Getter;
import net.fabricmc.api.ModInitializer;
import net.minecraft.client.MinecraftClient;
import zov.crickclient.event.list.EventKeyInput;
import zov.crickclient.module.Module;
import zov.crickclient.module.ModuleStorage;
import zov.crickclient.util.commands.CommandDispatcher;
import zov.crickclient.util.commands.manager.CommandRepository;
import zov.crickclient.util.config.ConfigManager;
import zov.crickclient.util.draggable.DragManager;
import zov.crickclient.util.alt.AltRepository;
import zov.crickclient.util.friend.FriendRepository;
import zov.crickclient.util.macro.MacroRepository;
import zov.crickclient.util.math.TPSGetter;
import zov.crickclient.util.player.combat.IdealHitUtils;
import zov.crickclient.util.player.move.ReallyWorldTimerBypass;
import zov.crickclient.util.player.move.TpGrim;
import zov.crickclient.util.player.other.ServerManager;
import zov.crickclient.util.rotation.ComponentManager;
import zov.crickclient.util.script.ScriptManager;
import zov.crickclient.util.staff.StaffManager;

import java.io.File;

public class CrickClient implements ModInitializer {

    private static CrickClient instance;

    @Getter
    private final EventBus eventBus;

    @Getter
    private final ModuleStorage moduleStorage;
    @Getter
    private final ComponentManager componentManager;
    @Getter
    private final DragManager dragManager;
    @Getter
    private final CommandRepository commandRepository;
    @Getter
    private final MacroRepository macroRepository;
    @Getter
    private final ConfigManager configManager;
    @Getter
    private final CommandDispatcher commandDispatcher;
    @Getter
    private final StaffManager staffManager;
    @Getter
    private final ServerManager serverManager;
    @Getter
    private final TPSGetter tpsGetter;
    @Getter
    private final IdealHitUtils idealHitUtils;
    @Getter
    private final ScriptManager scriptManager;

    public CrickClient() {
        instance = this;

        eventBus = new EventBus();
        eventBus.register(this);

        moduleStorage = new ModuleStorage();
        componentManager = new ComponentManager();
        dragManager = new DragManager();
        macroRepository = new MacroRepository();
        configManager = new ConfigManager();
        staffManager = new StaffManager();
        staffManager.load();
        commandRepository = new CommandRepository();
        commandDispatcher = new CommandDispatcher();
        TpGrim.init();
        ReallyWorldTimerBypass.init();
        serverManager = new ServerManager();
        tpsGetter = new TPSGetter();
        idealHitUtils = new IdealHitUtils();
        scriptManager = new ScriptManager();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            ConfigManager.save("autocfg");
            getDragManager().saveDraggables();
            getMacroRepository().save();
            FriendRepository.save();
            AltRepository.save();
            staffManager.save();
        }));
        File dir = new File("crickclient/configs/");
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    public static CrickClient getInstance() {
        return instance == null ? new CrickClient() : instance;
    }

    @Override
    public void onInitialize() {
        getModuleStorage().injectRegisterModules();
        componentManager.init();
        dragManager.load();
        macroRepository.load();
        FriendRepository.load();
        AltRepository.load();
        ConfigManager.loadStartupConfig();
        zov.crickclient.ui.ThemeEditor.applyStartupTheme();
    }

    @Subscribe
    private void onModuleKeyPressed(EventKeyInput event) {
        for (Module module : getModuleStorage().getModules()) {
            if (event.getAction() == 1 && MinecraftClient.getInstance().currentScreen == null) {
                if (module.getKey() == event.getKey()) {
                    module.toggle();
                }
            }
        }
    }
}