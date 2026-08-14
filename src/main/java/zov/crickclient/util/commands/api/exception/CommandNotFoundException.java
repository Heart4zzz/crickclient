package zov.crickclient.util.commands.api.exception;

import zov.crickclient.util.QuickLogger;
import zov.crickclient.util.commands.api.ICommand;
import zov.crickclient.util.commands.api.argument.ICommandArgument;

import java.util.List;

public class CommandNotFoundException extends CommandException implements QuickLogger {

    public final String command;

    public CommandNotFoundException(String command) {
        super(String.format("Команда не найдена: %s", command));
        this.command = command;
    }

    @Override
    public void handle(ICommand command, List<ICommandArgument> args) {
       logDirect(getMessage());
    }
}
