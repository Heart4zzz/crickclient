package zov.crickclient.util.collector;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.tooltip.TooltipType;
import zov.crickclient.util.IMinecraft;

import java.util.List;

public final class AuctionPriceUtil implements IMinecraft {
    private AuctionPriceUtil() {
    }

    public static int perItemPrice(ItemStack stack) {
        if (stack.isEmpty() || mc.player == null) {
            return -1;
        }

        if (stack.getItem().getName().getString().contains("Товар не актуален") || stack.getItem() == Items.GRAY_DYE) {
            return -1;
        }

        List<String> tooltipLines = stack.getTooltip(Item.TooltipContext.DEFAULT, mc.player, TooltipType.BASIC)
                .stream()
                .skip(1)
                .map(line -> line.getString())
                .toList();

        for (String line : tooltipLines) {
            int idx = line.indexOf("$ Ценa: ");
            if (idx == -1) {
                idx = line.indexOf("$ Цена: ");
            }
            if (idx == -1) {
                continue;
            }

            String marker = line.contains("$ Ценa: ") ? "$ Ценa: " : "$ Цена: ";
            String price = line.substring(line.indexOf(marker) + marker.length())
                    .trim()
                    .replace(",", "")
                    .replace("$", "")
                    .replaceAll("\\s+", "");

            if (price.isEmpty()) {
                continue;
            }

            try {
                int total = Integer.parseInt(price);
                int count = stack.getCount() > 0 ? stack.getCount() : 1;
                return total / count;
            } catch (NumberFormatException ignored) {
                return -1;
            }
        }

        return -1;
    }
}
