package zov.crickclient.util.alt;

import java.util.concurrent.ThreadLocalRandom;

public final class AltNameGenerator {
    private static final String[] PREFIXES = {
            "Pro", "Dark", "Epic", "Shadow", "Night", "Fire", "Ice", "Storm",
            "Cyber", "Neo", "Ultra", "Mega", "Hyper", "Alpha", "Ghost", "Blaze",
            "Frost", "Thunder", "Sky", "Star", "Wild", "Royal", "Swift", "Silent"
    };

    private static final String[] SUFFIXES = {
            "Master", "Hunter", "Slayer", "Wolf", "Dragon", "Ninja", "King", "Lord",
            "Blade", "Knight", "Phantom", "Reaper", "Striker", "Legend", "Hero",
            "Fox", "Bear", "Eagle", "Viper", "Storm", "Spirit", "Soul", "Ace", "Pro"
    };

    private AltNameGenerator() {
    }

    public static String generate() {
        ThreadLocalRandom random = ThreadLocalRandom.current();

        for (int attempt = 0; attempt < 30; attempt++) {
            String prefix = PREFIXES[random.nextInt(PREFIXES.length)];
            String suffix = SUFFIXES[random.nextInt(SUFFIXES.length)];
            int number = 100 + random.nextInt(9900);

            String combined = prefix + suffix + "_" + number;
            if (combined.length() <= 16) return combined;

            String shortCombined = prefix + "_" + number;
            if (shortCombined.length() <= 16) return shortCombined;

            String suffixOnly = suffix + "_" + number;
            if (suffixOnly.length() <= 16) return suffixOnly;
        }

        return "Player_" + ThreadLocalRandom.current().nextInt(1000, 9999);
    }
}
