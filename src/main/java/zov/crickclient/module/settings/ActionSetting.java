package zov.crickclient.module.settings;

import java.util.function.Supplier;

public class ActionSetting extends Setting {
    private final Runnable action;

    public ActionSetting(String name, Runnable action) {
        super(name);
        this.action = action;
    }

    public void run() {
        action.run();
    }

    @Override
    public String getValueAsString() {
        return "";
    }

    @Override
    public void setValueFromString(String value) {
    }

    @Override
    public ActionSetting setVisible(Supplier<Boolean> visible) {
        this.visible = visible;
        return this;
    }
}
