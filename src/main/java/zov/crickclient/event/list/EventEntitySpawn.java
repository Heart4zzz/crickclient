package zov.crickclient.event.list;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.entity.Entity;
import zov.crickclient.event.Event;

@Getter
@AllArgsConstructor
public class EventEntitySpawn extends Event {
    private final Entity entity;
}