

package zov.crickclient.util.commands.defaults;

import zov.crickclient.CrickClient;
import zov.crickclient.util.commands.api.ICommand;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class DefaultCommands {

    public static List<ICommand> createAll() {
        List<ICommand> commands = new ArrayList<>(Arrays.asList(
                new CfgCommand(),
                new RotationCommand(),
                new HelpCommand(CrickClient.getInstance()),
                new MacroCommand(CrickClient.getInstance()),
                new BindCommand(CrickClient.getInstance()),
                new FriendCommand(CrickClient.getInstance()),
                new StaffCommand(CrickClient.getInstance()),
                new VClipCommand(),
                new TpCommand(),
                new PartyCommand(),
                new GpsCommand(),
                new AICommand(),
                new BotCommand()
        ));
        return Collections.unmodifiableList(commands);
    }
}