package zov.crickclient.event;

import lombok.Data;
import zov.crickclient.CrickClient;

@Data
public class Event {
    private boolean cancelled;

    public void post() {
        CrickClient.getInstance().getEventBus().post(this);
    }

    public void cancelEvent() {
        setCancelled(true);
    }
}