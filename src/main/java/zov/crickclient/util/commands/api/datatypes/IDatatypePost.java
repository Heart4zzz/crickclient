package zov.crickclient.util.commands.api.datatypes;

import zov.crickclient.util.commands.api.exception.CommandException;

public interface IDatatypePost<T, O> extends IDatatype {
    T apply(IDatatypeContext datatypeContext, O original) throws CommandException;
}
