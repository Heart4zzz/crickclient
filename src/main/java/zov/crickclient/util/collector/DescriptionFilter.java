package zov.crickclient.util.collector;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import zov.crickclient.util.IMinecraft;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class DescriptionFilter implements ItemFilter, IMinecraft {
    private final List<DescriptionRule> rules = new ArrayList<>();

    public DescriptionFilter add(String description) {
        rules.add(new DescriptionRule(description));
        return this;
    }

    public DescriptionFilter add(String description, int minLevel) {
        rules.add(new DescriptionRule(description, minLevel));
        return this;
    }

    public DescriptionFilter add(String description, int minLevel, boolean enabled) {
        DescriptionRule rule = new DescriptionRule(description, minLevel);
        rule.setEnabled(enabled);
        rules.add(rule);
        return this;
    }

    public DescriptionFilter deny(String description) {
        rules.add(new DescriptionRule(description, 0, ItemMatchType.DENY));
        return this;
    }

    public DescriptionFilter withDefaults() {
        return deny("Попрыгун").deny("Нестабильный ");
    }

    @Override
    public boolean matches(ItemStack stack) {
        return rules.stream().allMatch(rule -> rule.matches(stack));
    }

    @Getter
    @Setter
    public static class DescriptionRule implements IMinecraft {
        private final String text;
        private ItemMatchType type;
        private int level;

        public DescriptionRule(String text) {
            this(text, 0, ItemMatchType.ON);
        }

        public DescriptionRule(String text, int level) {
            this(text, level, ItemMatchType.ON);
        }

        public DescriptionRule(String text, int level, ItemMatchType type) {
            this.text = text;
            this.type = type;
            this.level = (type != ItemMatchType.ON || level <= 0) ? level : Math.min(maxLevel(), level);
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
            level = Math.max(1, Math.min(maxLevel(), level + delta));
        }

        public String displayName() {
            return level == 0 ? text : text + " " + Math.max(0, level - 1);
        }

        public boolean matches(ItemStack stack) {
            if (type == ItemMatchType.OFF) {
                return true;
            }

            String tooltip = stack.getTooltip(Item.TooltipContext.DEFAULT, mc.player, TooltipType.BASIC)
                    .stream()
                    .skip(1)
                    .map(line -> line.getString().replaceAll("§.", "").toLowerCase().replaceAll("\\s+", " ").trim())
                    .collect(Collectors.joining(" "));

            String needle = text.replaceAll("§.", "").toLowerCase().replaceAll("\\s+", " ").trim();
            if (type == ItemMatchType.DENY) {
                return !tooltip.contains(needle);
            }
            if (!tooltip.contains(needle)) {
                return false;
            }
            if (level == 0) {
                return true;
            }

            String after = tooltip.substring(tooltip.indexOf(needle) + needle.length()).trim();
            int space = after.indexOf(' ');
            String token = after.isEmpty() ? "" : (space > 0 ? after.substring(0, space) : after);
            int parsed = parseLevel(token);
            return Math.max(1, parsed) >= level;
        }

        private int maxLevel() {
            return ("Окисление".equals(text) || "Вампиризм".equals(text)) ? 2 : 3;
        }

        private int parseLevel(String token) {
            if (token == null || token.isEmpty()) {
                return 0;
            }
            StringBuilder digits = new StringBuilder();
            for (char c : token.toCharArray()) {
                if (Character.isDigit(c)) {
                    digits.append(c);
                } else if (!digits.isEmpty()) {
                    break;
                }
            }
            if (digits.isEmpty()) {
                return 0;
            }
            try {
                return Integer.parseInt(digits.toString());
            } catch (NumberFormatException e) {
                return 0;
            }
        }
    }
}
