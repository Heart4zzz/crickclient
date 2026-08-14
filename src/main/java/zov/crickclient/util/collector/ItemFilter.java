package zov.crickclient.util.collector;

import net.minecraft.item.ItemStack;

public interface ItemFilter {
    boolean matches(ItemStack stack);
}
