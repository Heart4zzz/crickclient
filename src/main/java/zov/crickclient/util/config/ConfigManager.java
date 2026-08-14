package zov.crickclient.util.config;

import com.google.gson.*;
import zov.crickclient.CrickClient;
import zov.crickclient.module.Module;
import zov.crickclient.module.settings.*;
import zov.crickclient.module.settings.impl.Theme;

import java.io.IOException;
import java.io.Reader;
import java.math.BigInteger;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ConfigManager {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_FOLDER = Paths.get("crickclient/configs");
    private static final Path LAST_CONFIG_FILE = Paths.get("crickclient/lastconfig.txt");

    public static void save(String name) {
        JsonObject root = new JsonObject();

        for (Module module : CrickClient.getInstance().getModuleStorage().getModules()) {
            JsonObject moduleObject = new JsonObject();
            moduleObject.addProperty("enabled", module.isEnabled());
            moduleObject.addProperty("keybind", module.getKey());

            JsonObject settingsObject = new JsonObject();
            for (Setting setting : module.getSettings()) {
                if (setting instanceof BooleanSetting s) {
                    settingsObject.addProperty(setting.getName(), s.getValue());
                } else if (setting instanceof BindSetting s) {
                    settingsObject.addProperty(setting.getName(), s.getValue());
                } else if (setting instanceof ModeSetting s) {
                    settingsObject.addProperty(setting.getName(), s.getValue());
                } else if (setting instanceof SliderSetting s) {
                    settingsObject.addProperty(setting.getName(), s.getValue());
                } else if (setting instanceof ColorSetting s) {
                    settingsObject.addProperty(setting.getName(), Integer.toUnsignedLong(s.getValue()));
                } else if (setting instanceof ThemeSetting s) {
                    settingsObject.addProperty(setting.getName(), s.getValue().name);
                } else if (setting instanceof ModeListSetting s) {
                    JsonArray enabledModes = new JsonArray();
                    for (String name2 : s.getEnabledModules()) {
                        enabledModes.add(name2);
                    }
                    settingsObject.add(setting.getName(), enabledModes);
                }
            }

            moduleObject.add("settings", settingsObject);
            root.add(module.getName(), moduleObject);
        }

        try {
            Files.createDirectories(CONFIG_FOLDER);
            Path configFile = CONFIG_FOLDER.resolve(name + ".json");
            Files.write(configFile, gson.toJson(root).getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void load(String name) {
        Path configFile = CONFIG_FOLDER.resolve(name + ".json");
        if (!Files.exists(configFile)) return;

        try (Reader reader = Files.newBufferedReader(configFile)) {
            JsonObject root = gson.fromJson(reader, JsonObject.class);

            for (Module module : CrickClient.getInstance().getModuleStorage().getModules()) {
                JsonElement moduleElement = findAliased(root, module.getName());
                if (moduleElement == null || !moduleElement.isJsonObject()) continue;
                JsonObject moduleObject = moduleElement.getAsJsonObject();

                if (moduleObject.has("enabled")) {
                    boolean enabled = moduleObject.get("enabled").getAsBoolean();
                    module.setEnabled(enabled);
                }
                if (moduleObject.has("keybind")) {
                    int keybind = moduleObject.get("keybind").getAsInt();
                    module.setKey(keybind);
                }
                if (moduleObject.has("settings")) {
                    JsonObject settingsObject = moduleObject.getAsJsonObject("settings");
                    for (Setting setting : module.getSettings()) {
                        JsonElement element = findAliased(settingsObject, setting.getName());
                        if (element == null) continue;
                        if (setting instanceof BooleanSetting s) {
                            s.setValue(element.getAsBoolean());
                        } else if (setting instanceof BindSetting s) {
                            s.setValue(element.getAsInt());
                        } else if (setting instanceof ModeSetting s) {
                            s.setValue(element.getAsString());
                        } else if (setting instanceof SliderSetting s) {
                            s.setValue(element.getAsDouble());
                        } else if (setting instanceof ColorSetting s) {
                            Integer color = parseArgb(element);
                            if (color != null) {
                                s.setValue(color);
                            }
                        } else if (setting instanceof ThemeSetting s) {
                            String themeName = element.getAsString();
                            for (Theme theme : s.getThemes()) {
                                if (theme.name.equals(themeName)) {
                                    s.setValue(theme);
                                    break;
                                }
                            }
                        } else if (setting instanceof ModeListSetting s && element.isJsonArray()) {
                            JsonArray array = element.getAsJsonArray();
                            List<String> enabled = new ArrayList<>();
                            for (JsonElement e : array) {
                                enabled.add(e.getAsString());
                            }
                            for (BooleanSetting subSetting : s.getSettings()) {
                                subSetting.setValue(enabled.contains(subSetting.getName()));
                            }
                        }
                    }
                }
            }

            rememberLoadedConfig(name);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void loadStartupConfig() {
        String last = readLastConfig();
        if (last != null) {
            load(last);
            return;
        }
        Path auto = CONFIG_FOLDER.resolve("autocfg.json");
        if (Files.exists(auto)) {
            load("autocfg");
        }
    }

    private static void rememberLoadedConfig(String name) {
        if (name == null || name.isBlank() || name.equalsIgnoreCase("autocfg")) return;
        try {
            Files.createDirectories(LAST_CONFIG_FILE.getParent());
            Files.writeString(LAST_CONFIG_FILE, name);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String readLastConfig() {
        try {
            if (!Files.exists(LAST_CONFIG_FILE)) return null;
            String name = Files.readString(LAST_CONFIG_FILE).trim();
            if (name.isEmpty() || name.equalsIgnoreCase("autocfg")) return null;
            if (!Files.exists(CONFIG_FOLDER.resolve(name + ".json"))) return null;
            return name;
        } catch (IOException e) {
            return null;
        }
    }

    private static JsonElement findAliased(JsonObject object, String currentName) {
        if (object == null) return null;
        for (String candidate : ConfigKeyAliases.candidates(currentName)) {
            if (object.has(candidate)) return object.get(candidate);
        }
        return null;
    }

    private static Integer parseArgb(JsonElement element) {
        if (element == null || element.isJsonNull() || !element.isJsonPrimitive()) return null;

        String value = element.getAsString().trim();
        int radix = 10;
        if (value.startsWith("#")) {
            value = value.substring(1);
            radix = 16;
        } else if (value.regionMatches(true, 0, "0x", 0, 2)) {
            value = value.substring(2);
            radix = 16;
        }

        try {
            BigInteger parsed = new BigInteger(value, radix);
            BigInteger minimum = radix == 10
                    ? BigInteger.valueOf(Integer.MIN_VALUE)
                    : BigInteger.ZERO;
            BigInteger maximum = BigInteger.valueOf(0xFFFFFFFFL);
            if (parsed.compareTo(minimum) < 0 || parsed.compareTo(maximum) > 0) return null;
            return parsed.intValue();
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    public static List<String> getConfigs() {
        List<String> configs = new ArrayList<>();
        try {
            if (Files.exists(CONFIG_FOLDER)) {
                try (DirectoryStream<Path> stream = Files.newDirectoryStream(CONFIG_FOLDER, "*.json")) {
                    for (Path path : stream) {
                        String fileName = path.getFileName().toString();
                        if (fileName.endsWith(".json")) {
                            configs.add(fileName.substring(0, fileName.length() - 5));
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return configs;
    }

    public static List<String> getUserConfigs() {
        List<String> configs = new ArrayList<>();
        for (String name : getConfigs()) {
            if (!name.equalsIgnoreCase("autocfg")) {
                configs.add(name);
            }
        }
        return configs;
    }

    public static boolean delete(String name) {
        if (name == null || name.isBlank() || name.equalsIgnoreCase("autocfg")) return false;
        try {
            return Files.deleteIfExists(CONFIG_FOLDER.resolve(name + ".json"));
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}