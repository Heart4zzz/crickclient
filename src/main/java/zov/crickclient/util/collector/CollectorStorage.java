package zov.crickclient.util.collector;

import com.google.gson.*;
import lombok.Getter;
import zov.crickclient.util.collector.DescriptionFilter.DescriptionRule;
import zov.crickclient.util.collector.EnchantFilter.EnchantRule;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Getter
public class CollectorStorage {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = Paths.get("crickclient/inv_builder.json");

    private static CollectorStorage instance;

    private final List<CollectorEntry> entries = new ArrayList<>();

    public static CollectorStorage getInstance() {
        if (instance == null) {
            instance = new CollectorStorage();
            instance.reload();
        }
        return instance;
    }

    public void reload() {
        entries.clear();
        entries.addAll(CollectorPresets.createDefaults());

        if (!Files.exists(FILE)) {
            save();
            return;
        }

        try (Reader reader = Files.newBufferedReader(FILE)) {
            JsonArray array = JsonParser.parseReader(reader).getAsJsonArray();
            for (JsonElement element : array) {
                if (!element.isJsonObject()) {
                    continue;
                }
                JsonObject object = element.getAsJsonObject();
                if (!object.has("name")) {
                    continue;
                }
                String name = object.get("name").getAsString();
                entries.stream()
                        .filter(entry -> entry.getDisplayName().equals(name))
                        .findFirst()
                        .ifPresent(entry -> applySavedState(object, entry));
            }
        } catch (IOException | JsonParseException ignored) {
        }
    }

    public void save() {
        JsonArray array = new JsonArray();
        for (CollectorEntry entry : entries) {
            JsonObject object = new JsonObject();
            object.addProperty("name", entry.getDisplayName());
            object.addProperty("active", entry.isEnabled());
            object.addProperty("count", entry.getTargetCount());
            if (entry.getEnchantFilter() != null) {
                saveRules(object, "enchantments", entry.getEnchantFilter().getRules());
            }
            if (entry.getDescriptionFilter() != null) {
                saveDescriptionRules(object, entry.getDescriptionFilter().getRules());
            }
            array.add(object);
        }

        try {
            Files.createDirectories(FILE.getParent());
            Files.writeString(FILE, GSON.toJson(array));
        } catch (IOException ignored) {
        }
    }

    private void applySavedState(JsonObject object, CollectorEntry entry) {
        if (object.has("active")) {
            entry.setEnabled(object.get("active").getAsBoolean());
        }
        if (object.has("count")) {
            entry.setTargetCount(object.get("count").getAsInt());
        }
        if (object.has("enchantments") && entry.getEnchantFilter() != null) {
            loadEnchantRules(object.getAsJsonObject("enchantments"), entry.getEnchantFilter().getRules());
        }
        if (object.has("descriptions") && entry.getDescriptionFilter() != null) {
            loadDescriptionRules(object.getAsJsonObject("descriptions"), entry.getDescriptionFilter().getRules());
        }
    }

    private void saveRules(JsonObject root, String key, List<EnchantRule> rules) {
        JsonObject object = new JsonObject();
        for (EnchantRule rule : rules) {
            if (rule.isDeny()) {
                continue;
            }
            JsonObject ruleObject = new JsonObject();
            ruleObject.addProperty("level", rule.getLevel());
            ruleObject.addProperty("type", rule.getType().name());
            object.add(rule.getEnchantment().getValue().toString(), ruleObject);
        }
        if (!object.entrySet().isEmpty()) {
            root.add(key, object);
        }
    }

    private void saveDescriptionRules(JsonObject root, List<DescriptionRule> rules) {
        JsonObject object = new JsonObject();
        for (DescriptionRule rule : rules) {
            if (rule.isDeny()) {
                continue;
            }
            JsonObject ruleObject = new JsonObject();
            ruleObject.addProperty("level", rule.getLevel());
            ruleObject.addProperty("type", rule.getType().name());
            object.add(rule.getText(), ruleObject);
        }
        if (!object.entrySet().isEmpty()) {
            root.add("descriptions", object);
        }
    }

    private void loadEnchantRules(JsonObject object, List<EnchantRule> rules) {
        for (EnchantRule rule : rules) {
            String id = rule.getEnchantment().getValue().toString();
            if (!object.has(id) || rule.isDeny()) {
                continue;
            }
            JsonObject saved = object.getAsJsonObject(id);
            if (saved.has("level")) {
                rule.setLevel(saved.get("level").getAsInt());
            }
            if (saved.has("type")) {
                rule.setType(ItemMatchType.valueOf(saved.get("type").getAsString()));
            }
        }
    }

    private void loadDescriptionRules(JsonObject object, List<DescriptionRule> rules) {
        for (DescriptionRule rule : rules) {
            if (!object.has(rule.getText()) || rule.isDeny()) {
                continue;
            }
            JsonObject saved = object.getAsJsonObject(rule.getText());
            if (saved.has("level")) {
                rule.setLevel(saved.get("level").getAsInt());
            }
            if (saved.has("type")) {
                rule.setType(ItemMatchType.valueOf(saved.get("type").getAsString()));
            }
        }
    }
}
