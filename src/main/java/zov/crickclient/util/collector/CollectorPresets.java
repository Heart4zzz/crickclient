package zov.crickclient.util.collector;

import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Items;

import java.util.ArrayList;
import java.util.List;

public final class CollectorPresets {
    private CollectorPresets() {
    }

    public static List<CollectorEntry> createDefaults() {
        List<CollectorEntry> entries = new ArrayList<>();

        entries.add(CollectorEntry.of(Items.NETHERITE_SWORD, 1, "Незеритовый меч").enabled(true).pageScan(true)
                .enchants(new EnchantFilter().add(Enchantments.SHARPNESS, 7).add(Enchantments.FIRE_ASPECT, 2))
                .descriptions(new DescriptionFilter().add("Яд", 3).add("Вампиризм", 2).add("Окисление", 2)
                        .add("Опытный", 3, false).add("Детекция", 3)));

        entries.add(CollectorEntry.of(Items.MACE, 1, "Булава").enabled(true).pageScan(true)
                .enchants(new EnchantFilter().add(Enchantments.SHARPNESS, 7).add(Enchantments.BREACH, 3).add(Enchantments.DENSITY, 5))
                .descriptions(new DescriptionFilter()));

        entries.add(CollectorEntry.of(Items.TRIDENT, 1, "Трезубец").enabled(true).pageScan(true)
                .descriptions(new DescriptionFilter().add("Ступор", 3).add("Притяжение", 2).add("Скаут", 3).add("Возвращение").add("Подрывник")));

        entries.add(CollectorEntry.of(Items.NETHERITE_HELMET, 1, "Незеритовый шлем").enabled(true).pageScan(true)
                .enchants(new EnchantFilter().add(Enchantments.PROTECTION, 5).add(Enchantments.UNBREAKING, 5)
                        .add(Enchantments.RESPIRATION, 3).add(Enchantments.MENDING)));

        entries.add(CollectorEntry.of(Items.NETHERITE_CHESTPLATE, 1, "Незеритовый нагрудник").enabled(true).pageScan(true)
                .enchants(new EnchantFilter().add(Enchantments.PROTECTION, 5).add(Enchantments.UNBREAKING, 5).add(Enchantments.MENDING)));

        entries.add(CollectorEntry.of(Items.NETHERITE_LEGGINGS, 1, "Незеритовые поножи").enabled(true).pageScan(true)
                .enchants(new EnchantFilter().add(Enchantments.PROTECTION, 5).add(Enchantments.UNBREAKING, 5).add(Enchantments.MENDING)));

        entries.add(CollectorEntry.of(Items.NETHERITE_BOOTS, 1, "Незеритовые ботинки").enabled(true).pageScan(true)
                .enchants(new EnchantFilter().add(Enchantments.PROTECTION, 5).add(Enchantments.UNBREAKING, 5)
                        .add(Enchantments.DEPTH_STRIDER, 3).add(Enchantments.MENDING)));

        entries.add(CollectorEntry.of(Items.NETHERITE_SCRAP, 8, "Трапка").enabled(true)
                .descriptions(new DescriptionFilter().add("Каст: Нерушимая клетка")));

        entries.add(CollectorEntry.of(Items.SUGAR, 12, "Явная пыль").enabled(true)
                .descriptions(new DescriptionFilter().add("Каст: Световая вспышка")));

        entries.add(CollectorEntry.of(Items.PHANTOM_MEMBRANE, 4, "Божья аура").enabled(true)
                .descriptions(new DescriptionFilter().add("Каст: Божественная аура")));

        entries.add(CollectorEntry.of(Items.ENDER_EYE, 16, "Дезориентация").enabled(true)
                .descriptions(new DescriptionFilter().add("Каст: Звуковая волна")));

        entries.add(CollectorEntry.of(Items.WIND_CHARGE, 32, "Заряд ветра").enabled(true));
        entries.add(CollectorEntry.of(Items.DRIED_KELP, 16, "Пласт").enabled(true)
                .descriptions(new DescriptionFilter().add("Каст: Нерушимая стена")));
        entries.add(CollectorEntry.of(Items.SNOWBALL, 4, "Снежок заморозка").enabled(true)
                .descriptions(new DescriptionFilter().add("Каст: Ледяная сфера")));
        entries.add(CollectorEntry.of(Items.ENDER_PEARL, 16, "Перка").enabled(true));
        entries.add(CollectorEntry.of(Items.TOTEM_OF_UNDYING, 1, "Тотем бессмертия").enabled(true));

        entries.add(CollectorEntry.of(Items.CROSSBOW, 1, "Арбалет").enabled(true)
                .enchants(new EnchantFilter().add(Enchantments.QUICK_CHARGE, 3).add(Enchantments.MENDING).add(Enchantments.MULTISHOT)));

        entries.add(CollectorEntry.of(Items.GOLDEN_APPLE, 16, "Золотое яблоко").enabled(true));
        entries.add(CollectorEntry.of(Items.ENCHANTED_GOLDEN_APPLE, 8, "Зачарованное золотое яб").enabled(true));
        entries.add(CollectorEntry.of(Items.GOLDEN_CARROT, 64, "Золотая морковь").enabled(true));
        entries.add(CollectorEntry.of(Items.CHORUS_FRUIT, 64, "Хорус").enabled(true));
        entries.add(CollectorEntry.of(Items.ELYTRA, 1, "Элитры").enabled(true));
        entries.add(CollectorEntry.of(Items.FIREWORK_ROCKET, 64, "Фейерверк").enabled(true));

        entries.add(CollectorEntry.of(Items.SPLASH_POTION, 1, "Хлопушка").enabled(true).potionColor(0xFF0000)
                .potions(new PotionFilter()
                        .add(StatusEffects.SLOWNESS, 10, 200)
                        .add(StatusEffects.SPEED, 5, 300)
                        .add(StatusEffects.BLINDNESS, 10, 100)
                        .add(StatusEffects.GLOWING, 1, 3600)));

        entries.add(CollectorEntry.of(Items.SPLASH_POTION, 1, "Святая вода").enabled(true).potionColor(0xFFFFFF)
                .potions(new PotionFilter()
                        .add(StatusEffects.REGENERATION, 2, 900)
                        .add(StatusEffects.INVISIBILITY, 2, 12000)
                        .add(StatusEffects.INSTANT_HEALTH, 2, 0)));

        entries.add(CollectorEntry.of(Items.SPLASH_POTION, 1, "Зелье Гнева").enabled(true).pageScan(true).potionColor(0x993A0B)
                .potions(new PotionFilter()
                        .add(StatusEffects.STRENGTH, 5, 600)
                        .add(StatusEffects.SLOWNESS, 4, 600)));

        entries.add(CollectorEntry.of(Items.SPLASH_POTION, 1, "Зелье Палладина").enabled(true).potionColor(0x00FFFF)
                .potions(new PotionFilter()
                        .add(StatusEffects.RESISTANCE, 1, 12000)
                        .add(StatusEffects.FIRE_RESISTANCE, 1, 12000)
                        .add(StatusEffects.HEALTH_BOOST, 3, 1200)
                        .add(StatusEffects.INVISIBILITY, 1, 18000)));

        entries.add(CollectorEntry.of(Items.SPLASH_POTION, 1, "Зелье Ассасина").enabled(true).potionColor(0x333333)
                .potions(new PotionFilter()
                        .add(StatusEffects.STRENGTH, 4, 1200)
                        .add(StatusEffects.SPEED, 3, 6000)
                        .add(StatusEffects.HASTE, 1, 1200)
                        .add(StatusEffects.INSTANT_DAMAGE, 2, 0)));

        entries.add(CollectorEntry.of(Items.SPLASH_POTION, 1, "Зелье Радиации").enabled(true).potionColor(0x32C932)
                .potions(new PotionFilter()
                        .add(StatusEffects.POISON, 2, 300)
                        .add(StatusEffects.WITHER, 2, 300)
                        .add(StatusEffects.SLOWNESS, 3, 300)
                        .add(StatusEffects.HUNGER, 5, 300)
                        .add(StatusEffects.GLOWING, 1, 300)));

        entries.add(CollectorEntry.of(Items.SPLASH_POTION, 1, "Снотворное").enabled(true).pageScan(false).potionColor(0x484848)
                .potions(new PotionFilter()
                        .add(StatusEffects.WEAKNESS, 2, 1800)
                        .add(StatusEffects.MINING_FATIGUE, 2, 200)
                        .add(StatusEffects.WITHER, 3, 1800)
                        .add(StatusEffects.BLINDNESS, 1, 200)));

        entries.add(CollectorEntry.of(Items.POTION, 1, "Зелье").enabled(true)
                .potions(new PotionFilter()
                        .add(StatusEffects.STRENGTH, 3, 3600)
                        .add(StatusEffects.SPEED, 3, 3600)));

        entries.add(CollectorEntry.of(Items.POTION, 1, "Зелье регенерации").enabled(true)
                .potions(new PotionFilter()
                        .add(StatusEffects.INSTANT_HEALTH, 2, 0)
                        .add(StatusEffects.REGENERATION, 1, 900)));

        entries.add(CollectorEntry.of(Items.TIPPED_ARROW, 32, "Кровавая стрела").enabled(true)
                .potions(new PotionFilter()
                        .add(StatusEffects.WEAKNESS, 3, 60)
                        .add(StatusEffects.BLINDNESS, 1, 40)
                        .add(StatusEffects.MINING_FATIGUE, 1, 40)
                        .add(StatusEffects.NAUSEA, 1, 100)));

        entries.add(CollectorEntry.of(Items.TIPPED_ARROW, 64, "Стрела обледенения").enabled(false)
                .potions(new PotionFilter()
                        .add(StatusEffects.SLOWNESS, 10, 100)
                        .add(StatusEffects.MINING_FATIGUE, 3, 40)));

        entries.add(CollectorEntry.of(Items.TIPPED_ARROW, 64, "Мучительная стрела").enabled(false)
                .potions(new PotionFilter()
                        .add(StatusEffects.SLOWNESS, 3, 100)
                        .add(StatusEffects.WITHER, 3, 100)
                        .add(StatusEffects.POISON, 3, 100)));

        return entries;
    }
}
