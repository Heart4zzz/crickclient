package zov.crickclient.event.list;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import zov.crickclient.event.Event;

@Getter
@Setter
@AllArgsConstructor
public class RotationEvent extends Event {
    private float yaw, pitch;
    private float partialTicks;
}