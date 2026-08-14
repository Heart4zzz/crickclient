package zov.crickclient.util.commands.defaults;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Formatting;
import zov.crickclient.util.commands.api.Command;
import zov.crickclient.util.commands.api.argument.IArgConsumer;
import zov.crickclient.util.commands.api.exception.CommandException;
import zov.crickclient.util.player.move.TpGrim;

import java.util.List;
import java.util.stream.Stream;

public class TpCommand extends Command {

    private final TpGrim tpGrim = TpGrim.getInstance();

    public TpCommand() {
        super("tp", "tpstop");
    }

    @Override
    public void execute(String label, IArgConsumer args) throws CommandException {
        if (label.equalsIgnoreCase("tpstop") || isStopArg(args)) {
            if (args.hasAny()) {
                args.getString();
            }
            if (tpGrim.stop()) {
                logDirect(Formatting.GREEN + "ТП остановлен.");
            } else {
                logDirect(Formatting.YELLOW + "ТП не активен.");
            }
            return;
        }

        args.requireMin(1);
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null || mc.player == null) {
            logDirect(Formatting.RED + "Невозможно выполнить команду.");
            return;
        }

        if (!TpGrim.isInFlyMode(mc.player)) {
            notifyFlyRequired();
            return;
        }

        if (args.hasExactly(3)) {
            try {
                double x = Double.parseDouble(args.getString());
                double y = Double.parseDouble(args.getString());
                double z = Double.parseDouble(args.getString());
                tpGrim.start(x, y, z);
                return;
            } catch (NumberFormatException ignored) {
                logDirect(Formatting.RED + "Координаты должны быть числами: .tp <x> <y> <z>");
                return;
            }
        }

        if (args.hasExactly(1)) {
            String name = args.getString();
            if (tpGrim.startToPlayer(name)) {
                return;
            }

            PlayerEntity entityPlayer = mc.world.getPlayers().stream()
                    .filter(player -> player.getName().getString().equalsIgnoreCase(name))
                    .findFirst()
                    .orElse(null);

            if (entityPlayer != null) {
                tpGrim.start(entityPlayer.getX(), entityPlayer.getY(), entityPlayer.getZ());
                logDirect("ТП к игроку " + entityPlayer.getName().getString());
                return;
            }

            logDirect(Formatting.RED + "Игрок " + name + " не найден.");
            return;
        }

        logDirect(Formatting.RED + "Использование: .tp <x> <y> <z> | .tp <ник> | .tp stop | .tpstop");
    }

    private void notifyFlyRequired() {
        logDirect(Formatting.RED + "" + Formatting.BOLD + "TP "
                + Formatting.RESET + Formatting.RED + "» "
                + Formatting.GRAY + "Вы " + Formatting.RED + "не в режиме полёта" + Formatting.GRAY + ".");
        logDirect(Formatting.GRAY + "Включите " + Formatting.WHITE + "/fly "
                + Formatting.GRAY + "(донат " + Formatting.GOLD + "Dragon" + Formatting.GRAY + ").");
    }

    private boolean isStopArg(IArgConsumer args) {
        if (!args.hasAny()) {
            return false;
        }
        try {
            String value = args.peekString().trim();
            return value.equalsIgnoreCase("stop")
                    || value.equalsIgnoreCase("cancel")
                    || value.equalsIgnoreCase("off");
        } catch (CommandException ignored) {
            return false;
        }
    }

    @Override
    public String getShortDesc() {
        return "Grim TP по координатам или к игроку";
    }

    @Override
    public List<String> getLongDesc() {
        return List.of(
                "Пошаговый телепорт с обходом Grim (нужен /fly на донате)",
                "",
                "> tp <x> <y> <z> — телепорт к координатам",
                "> tp <ник> — телепорт к игроку",
                "> tp stop — остановить",
                "> tpstop — остановить"
        );
    }

    @Override
    public Stream<String> tabComplete(String label, IArgConsumer args) {
        if (label.equalsIgnoreCase("tpstop")) {
            return Stream.empty();
        }
        if (args.hasExactlyOne()) {
            MinecraftClient mc = MinecraftClient.getInstance();
            Stream<String> players = Stream.empty();
            if (mc.world != null) {
                players = mc.world.getPlayers().stream()
                        .map(player -> player.getName().getString());
            }
            return Stream.concat(Stream.of("stop", "cancel", "off"), players);
        }
        return Stream.empty();
    }
}
