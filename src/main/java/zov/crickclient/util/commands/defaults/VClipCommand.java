package zov.crickclient.util.commands.defaults;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import zov.crickclient.util.commands.api.Command;
import zov.crickclient.util.commands.api.argument.IArgConsumer;
import zov.crickclient.util.commands.api.exception.CommandException;
import zov.crickclient.util.player.move.VerticalTeleport;

import java.util.List;
import java.util.stream.Stream;

public class VClipCommand extends Command {
    public VClipCommand() {
        super("vclip");
    }

    @Override
    public void execute(String label, IArgConsumer args) throws CommandException {
        args.requireMin(1);
        String input = args.getString();

        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        ClientWorld world = MinecraftClient.getInstance().world;

        double yOffset;
        switch (input.toLowerCase()) {
            case "up" -> yOffset = findOffset(player.getBlockPos(), true, world);
            case "down" -> yOffset = findOffset(player.getBlockPos(), false, world);
            default -> {
                try {
                    yOffset = Double.parseDouble(input);
                } catch (NumberFormatException e) {
                    logDirect(Formatting.RED + input + " не является числом.");
                    return;
                }
            }
        }

        if (yOffset == 0) {
            logDirect(Formatting.RED + "Не удалось выполнить телепортацию.");
            return;
        }

        VerticalTeleport.teleport(yOffset);

        logDirect("Телепортировано на " + (int) yOffset + " блоков по вертикали");
    }

    private double findOffset(BlockPos pos, boolean toUp, ClientWorld world) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) {
            return 0;
        }

        if (toUp) {
            int x = pos.getX();
            int z = pos.getZ();
            int startY = (int) Math.floor(player.getY()) + 1;
            boolean passedSolid = false;

            for (int y = startY; y < 320; y++) {
                BlockPos feet = new BlockPos(x, y, z);
                BlockPos head = feet.up();
                boolean blocked = isBlocking(world, feet) || isBlocking(world, head);

                if (blocked) {
                    passedSolid = true;
                    continue;
                }

                if (passedSolid) {
                    return y - player.getY();
                }
            }

            for (int i = 2; i < 255; i++) {
                BlockPos feet = pos.up(i);
                BlockPos head = feet.up();
                if (!isBlocking(world, feet) && !isBlocking(world, head)) {
                    double offset = feet.getY() - player.getY();
                    if (offset >= 1.0D) {
                        return offset;
                    }
                }
            }
        } else {
            for (int i = -1; i > -255; i--) {
                BlockPos solid = pos.add(0, i, 0);
                BlockPos air1 = solid.down();
                BlockPos air2 = air1.down();

                boolean isSolid = !world.getBlockState(solid).isAir();
                boolean isAirBelow1 = world.getBlockState(air1).isAir();
                boolean isAirBelow2 = world.getBlockState(air2).isAir();

                if (isSolid && isAirBelow1 && isAirBelow2) {
                    return air2.getY() - player.getY();
                }
            }
        }

        return 0;
    }

    private boolean isBlocking(ClientWorld world, BlockPos blockPos) {
        return !world.getBlockState(blockPos).getCollisionShape(world, blockPos).isEmpty();
    }


    @Override
    public String getShortDesc() {
        return "Телепорт по вертикали";
    }

    @Override
    public List<String> getLongDesc() {
        return List.of(
                "Телепортирует игрока вверх или вниз",
                "",
                "> vclip <расстояние> — телепорт на определенное количество блоков",
                "> vclip up — вверх до свободного блока",
                "> vclip down — вниз до свободного блока"
        );
    }

    @Override
    public Stream<String> tabComplete(String label, IArgConsumer args) {
        if (args.hasExactlyOne()) {
            return Stream.of("up", "down");
        }
        return Stream.empty();
    }
}