package zov.crickclient.util.collector;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import zov.crickclient.util.IMinecraft;

import java.util.ArrayList;
import java.util.List;

@Getter
public class EnchantFilter implements ItemFilter, IMinecraft {
    private final List<EnchantRule> rules = new ArrayList<>();

    public EnchantFilter add(RegistryKey<Enchantment> key) {
        rules.add(new EnchantRule(key));
        return this;
    }

    public EnchantFilter add(RegistryKey<Enchantment> key, int level) {
        rules.add(new EnchantRule(key, level));
        return this;
    }

    public EnchantFilter deny(RegistryKey<Enchantment> key) {
        rules.add(new EnchantRule(key, 0, ItemMatchType.DENY));
        return this;
    }

    public EnchantFilter deny(RegistryKey<Enchantment> key, int maxLevel) {
        rules.add(new EnchantRule(key, maxLevel, ItemMatchType.DENY));
        return this;
    }

    public EnchantFilter withDefaults() {
        return deny(Enchantments.THORNS)
                .deny(Enchantments.KNOCKBACK)
                .deny(Enchantments.BINDING_CURSE)
                .deny(Enchantments.VANISHING_CURSE);
    }

    @Override
    public boolean matches(ItemStack stack) {
        return rules.stream().allMatch(rule -> rule.matches(stack));
    }

    @Getter
    @Setter
    public static class EnchantRule implements IMinecraft {
        private final RegistryKey<Enchantment> enchantment;
        private ItemMatchType type;
        private int level;

        public EnchantRule(RegistryKey<Enchantment> enchantment) {
            this(enchantment, 0, ItemMatchType.ON);
        }

        public EnchantRule(RegistryKey<Enchantment> enchantment, int level) {
            this(enchantment, level, ItemMatchType.ON);
        }

        public EnchantRule(RegistryKey<Enchantment> enchantment, int level, ItemMatchType type) {
            this.enchantment = enchantment;
            this.type = type;
            this.level = type == ItemMatchType.ON ? clampLevel(level) : level;
        }

        public void setEnabled(boolean enabled) {
            this.type = enabled ? ItemMatchType.ON : ItemMatchType.OFF;
        }

        public boolean isEnabled() {
            return type != ItemMatchType.OFF;
        }

        public boolean isDeny() {
            return type == ItemMatchType.DENY;
        }

        public boolean hasLevel() {
            return level > 0;
        }

        public void changeLevel(int delta) {
            if (type != ItemMatchType.ON || level == 0) {
                return;
            }
            level = Math.max(1, clampLevel(level + delta));
        }

        public String displayName() {
            String name = Text.translatable("enchantment." + enchantment.getValue().getNamespace() + "."
                    + enchantment.getValue().getPath().replace("/", ".")).getString();
            return level == 0 ? name : name + " " + level;
        }

        public boolean matches(ItemStack stack) {
            if (type == ItemMatchType.OFF || mc.world == null) {
                return true;
            }

            var registry = mc.world.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT);
            if (!registry.contains(enchantment)) {
                return type == ItemMatchType.DENY;
            }

            var entry = registry.getOrThrow(enchantment);
            ItemEnchantmentsComponent enchantments = stack.getOrDefault(DataComponentTypes.ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT);
            int found = enchantments.getLevel(entry);
            int threshold = level == 0 ? 1 : level;

            if (type == ItemMatchType.DENY) {
                return found < threshold;
            }
            return !enchantments.isEmpty() && found >= threshold;
        }

        private int clampLevel(int value) {
            if (value <= 0) {
                return 0;
            }
            if (mc.world == null || !mc.world.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT).contains(enchantment)) {
                return value;
            }
            int max = enchantment.equals(Enchantments.SHARPNESS) ? 7
                    : (enchantment.equals(Enchantments.PROTECTION) || enchantment.equals(Enchantments.UNBREAKING)) ? 5
                    : mc.world.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT).getOrThrow(enchantment).value().getMaxLevel();
            return Math.max(1, Math.min(max, value));
        }
    }
}
