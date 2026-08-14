package zov.crickclient.util.commands.api;

import zov.crickclient.util.QuickLogger;
import zov.crickclient.util.commands.api.argument.IArgConsumer;
import zov.crickclient.util.commands.api.exception.CommandException;

import java.util.List;
import java.util.stream.Stream;

public interface ICommand extends QuickLogger {
    void execute(String label, IArgConsumer args) throws CommandException;

    Stream<String> tabComplete(String label, IArgConsumer args) throws CommandException;

    String getShortDesc();

    List<String> getLongDesc();

    List<String> getNames();

    default boolean hiddenFromHelp() {
        return false;
    }
}
