package zov.crickclient.module.settings;

import java.util.function.Supplier;

public interface ISetting {
    Setting setVisible(Supplier<Boolean> visible);
}