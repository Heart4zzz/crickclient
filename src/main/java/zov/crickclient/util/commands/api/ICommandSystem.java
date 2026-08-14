package zov.crickclient.util.commands.api;

import zov.crickclient.util.commands.api.argparser.IArgParserManager;

public interface ICommandSystem {
    IArgParserManager getParserManager();
}
