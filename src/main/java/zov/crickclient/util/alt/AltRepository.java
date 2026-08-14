package zov.crickclient.util.alt;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public final class AltRepository {
    private static final File FILE = new File("crickclient/alts.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final List<AltAccount> accounts = new ArrayList<>();
    private static String selectedName;

    private AltRepository() {
    }

    public static List<AltAccount> getAccounts() {
        sortAccounts();
        return new ArrayList<>(accounts);
    }

    public static Optional<AltAccount> getAccount(String name) {
        if (name == null) return Optional.empty();
        return accounts.stream()
                .filter(a -> a.getName().equalsIgnoreCase(name))
                .findFirst();
    }

    public static Optional<String> getSelectedName() {
        return Optional.ofNullable(selectedName);
    }

    public static void setSelected(String name) {
        selectedName = name;
    }

    public static boolean addAccount(String name) {
        if (name == null || name.isBlank()) return false;
        String trimmed = name.trim();
        if (trimmed.length() > 16) trimmed = trimmed.substring(0, 16);
        if (getAccount(trimmed).isPresent()) return false;

        accounts.add(new AltAccount(trimmed));
        sortAccounts();
        save();
        return true;
    }

    public static boolean removeAccount(String name) {
        boolean removed = accounts.removeIf(a -> a.getName().equalsIgnoreCase(name));
        if (removed) {
            if (name.equalsIgnoreCase(selectedName)) {
                selectedName = null;
            }
            save();
        }
        return removed;
    }

    public static boolean toggleFavorite(String name) {
        return getAccount(name).map(account -> {
            account.setFavorite(!account.isFavorite());
            sortAccounts();
            save();
            return true;
        }).orElse(false);
    }

    private static void sortAccounts() {
        accounts.sort(Comparator
                .comparing(AltAccount::isFavorite).reversed()
                .thenComparing(a -> a.getName().toLowerCase()));
    }

    public static void save() {
        try {
            FILE.getParentFile().mkdirs();
            AltStorage storage = new AltStorage(new ArrayList<>(accounts), selectedName);
            try (Writer writer = new OutputStreamWriter(new FileOutputStream(FILE), StandardCharsets.UTF_8)) {
                GSON.toJson(storage, writer);
            }
        } catch (IOException ignored) {
        }
    }

    public static void load() {
        if (!FILE.exists()) return;

        try (Reader reader = new InputStreamReader(new FileInputStream(FILE), StandardCharsets.UTF_8)) {
            Type type = new TypeToken<AltStorage>() {}.getType();
            AltStorage storage = GSON.fromJson(reader, type);
            if (storage == null) return;

            accounts.clear();
            if (storage.accounts != null) {
                accounts.addAll(storage.accounts);
            }
            selectedName = storage.selectedName;
            sortAccounts();
        } catch (IOException ignored) {
        }
    }

    private static class AltStorage {
        List<AltAccount> accounts;
        String selectedName;

        AltStorage(List<AltAccount> accounts, String selectedName) {
            this.accounts = accounts;
            this.selectedName = selectedName;
        }
    }
}
