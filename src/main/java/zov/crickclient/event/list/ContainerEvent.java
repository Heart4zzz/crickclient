package zov.crickclient.event.list;

import lombok.Getter;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import zov.crickclient.event.Event;

import java.util.List;

@Getter
public class ContainerEvent extends Event {
    public enum Phase {
        PRE,
        POST,
        TITLE
    }

    private final HandledScreen<?> screen;
    private final ScreenHandler handler;
    private final DrawContext context;
    private final List<Slot> slots;
    private final int mouseX;
    private final int mouseY;
    private final Phase phase;
    private Text title;

    public ContainerEvent(HandledScreen<?> screen, DrawContext context, int mouseX, int mouseY, Phase phase) {
        this.screen = screen;
        this.handler = screen.getScreenHandler();
        this.slots = screen.getScreenHandler().slots;
        this.context = context;
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.phase = phase;
        this.title = screen.getTitle();
    }

    public ContainerEvent(HandledScreen<?> screen, Text title) {
        this.screen = screen;
        this.handler = screen.getScreenHandler();
        this.slots = screen.getScreenHandler().slots;
        this.context = null;
        this.mouseX = 0;
        this.mouseY = 0;
        this.phase = Phase.TITLE;
        this.title = title;
    }

    public void setTitle(Text title) {
        this.title = title;
    }
}
