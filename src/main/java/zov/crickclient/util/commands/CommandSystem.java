package zov.crickclient.util.commands;

import zov.crickclient.util.commands.api.ICommandSystem;
import zov.crickclient.util.commands.api.argparser.IArgParserManager;
import zov.crickclient.util.commands.argparser.ArgParserManager;

public enum CommandSystem implements ICommandSystem {
    INSTANCE;

    @Override
    public IArgParserManager getParserManager() {
        return ArgParserManager.INSTANCE;
    }
}
