package zov.crickclient.event.list;

import lombok.AllArgsConstructor;
import lombok.Getter;
import zov.crickclient.event.Event;

@Getter
@AllArgsConstructor
public class LookEvent extends Event {
    private double yaw, pitch;
}