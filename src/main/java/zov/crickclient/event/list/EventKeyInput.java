package zov.crickclient.event.list;

import lombok.AllArgsConstructor;
import lombok.Getter;
import zov.crickclient.event.Event;

@Getter
@AllArgsConstructor
public class EventKeyInput extends Event {
    private final int key, action;
}