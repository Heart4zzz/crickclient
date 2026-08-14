package zov.crickclient.event.list;

import lombok.AllArgsConstructor;
import lombok.Getter;
import zov.crickclient.event.Event;

@Getter
@AllArgsConstructor
public class ChatEvent extends Event {
    private final String message;
}