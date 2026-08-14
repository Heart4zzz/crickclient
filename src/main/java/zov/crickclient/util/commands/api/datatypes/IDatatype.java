package zov.crickclient.util.commands.api.datatypes;

import zov.crickclient.util.IMinecraft;
import zov.crickclient.util.commands.api.exception.CommandException;

import java.util.stream.Stream;

public interface IDatatype extends IMinecraft {
    Stream<String> tabComplete(IDatatypeContext ctx) throws CommandException;
}
