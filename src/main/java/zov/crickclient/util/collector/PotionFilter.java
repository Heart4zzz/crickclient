package zov.crickclient.util.collector;

import lombok.Getter;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.StreamSupport;

@Getter
public class PotionFilter implements ItemFilter {
    private final List<PotionRule> rules = new ArrayList<>();

    public PotionFilter add(PotionRule rule) {
        rules.add(rule);
        return this;
    }

    public PotionFilter add(RegistryEntry<StatusEffect> effect, int level, int duration) {
        rules.add(new PotionRule(effect, level, duration));
        return this;
    }

    @Override
    public boolean matches(ItemStack stack) {
        return rules.stream().allMatch(rule -> rule.matches(stack));
    }

    @Getter
    public static class PotionRule {
        private final RegistryEntry<StatusEffect> effect;
        private final int level;
        private final int duration;

        public PotionRule(RegistryEntry<StatusEffect> effect, int level, int duration) {
            this.effect = effect;
            this.level = level;
            this.duration = duration;
        }

        public boolean matches(ItemStack stack) {
            PotionContentsComponent contents = stack.get(DataComponentTypes.POTION_CONTENTS);
            if (contents == null) {
                return false;
            }
            return StreamSupport.stream(contents.getEffects().spliterator(), false)
                    .anyMatch(instance -> matchesInstance(instance));
        }

        private boolean matchesInstance(StatusEffectInstance instance) {
            return instance.getEffectType().equals(effect)
                    && instance.getAmplifier() + 1 == level
                    && instance.getDuration() >= duration;
        }
    }
}
