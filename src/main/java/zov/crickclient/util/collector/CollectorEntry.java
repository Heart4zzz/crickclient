package zov.crickclient.util.collector;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PotionItem;
import net.minecraft.item.SplashPotionItem;

import java.util.List;
import java.util.Optional;

@Getter
@Setter
public class CollectorEntry {
    private DescriptionFilter descriptionFilter;
    private EnchantFilter enchantFilter;
    private PotionFilter potionFilter;
    private final Item item;
    private final String displayName;
    private boolean enabled;
    private boolean pageScan;
    private int targetCount;
    private int potionColor;

    private CollectorEntry(Item item, int count, String displayName) {
        this.item = item;
        this.displayName = displayName;
        this.targetCount = count;
    }

    public static CollectorEntry of(Item item, int count, String displayName) {
        return new CollectorEntry(item, count, displayName);
    }

    public CollectorEntry descriptions(DescriptionFilter filter) {
        this.descriptionFilter = filter;
        return this;
    }

    public CollectorEntry enchants(EnchantFilter filter) {
        this.enchantFilter = filter;
        return this;
    }

    public CollectorEntry potions(PotionFilter filter) {
        this.potionFilter = filter;
        return this;
    }

    public CollectorEntry enabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public CollectorEntry pageScan(boolean pageScan) {
        this.pageScan = pageScan;
        return this;
    }

    public CollectorEntry potionColor(int color) {
        this.potionColor = color;
        return this;
    }

    public boolean supportsCountEditor() {
        return item.getMaxCount() > 1 || item == Items.TOTEM_OF_UNDYING || item instanceof SplashPotionItem || item instanceof PotionItem;
    }

    public int maxPurchaseBatch() {
        if (item instanceof PotionItem) {
            return 16;
        }
        if (item == Items.TOTEM_OF_UNDYING || item instanceof SplashPotionItem) {
            return 6;
        }
        return item.getMaxCount();
    }

    public boolean matches(ItemStack stack) {
        return stack.isOf(item)
                && (descriptionFilter == null || descriptionFilter.matches(stack))
                && (enchantFilter == null || enchantFilter.matches(stack))
                && (potionFilter == null || potionFilter.matches(stack));
    }

    public ItemStack previewStack() {
        ItemStack stack = new ItemStack(item, targetCount);
        if (potionColor != 0) {
            List<StatusEffectInstance> effects = potionFilter == null ? List.of()
                    : potionFilter.getRules().stream()
                    .map(rule -> new StatusEffectInstance(rule.getEffect(), rule.getDuration(), Math.max(0, rule.getLevel() - 1)))
                    .toList();
            stack.set(DataComponentTypes.POTION_CONTENTS,
                    new PotionContentsComponent(Optional.empty(), Optional.of(potionColor), effects, Optional.empty()));
        }
        return stack;
    }

    public CollectorEntry copy() {
        CollectorEntry copy = of(item, targetCount, displayName)
                .enabled(enabled)
                .pageScan(pageScan)
                .potionColor(potionColor);
        if (descriptionFilter != null) {
            DescriptionFilter descriptions = new DescriptionFilter();
            descriptionFilter.getRules().forEach(rule ->
                    descriptions.getRules().add(new DescriptionFilter.DescriptionRule(rule.getText(), rule.getLevel(), rule.getType())));
            copy.descriptions(descriptions);
        }
        if (enchantFilter != null) {
            EnchantFilter enchants = new EnchantFilter();
            enchantFilter.getRules().forEach(rule ->
                    enchants.getRules().add(new EnchantFilter.EnchantRule(rule.getEnchantment(), rule.getLevel(), rule.getType())));
            copy.enchants(enchants);
        }
        if (potionFilter != null) {
            PotionFilter potions = new PotionFilter();
            potionFilter.getRules().forEach(rule ->
                    potions.getRules().add(new PotionFilter.PotionRule(rule.getEffect(), rule.getLevel(), rule.getDuration())));
            copy.potions(potions);
        }
        return copy;
    }
}
