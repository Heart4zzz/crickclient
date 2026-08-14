package zov.crickclient.ui;

public final class ThemePresets {

    public record Preset(String name, int[] colors) {
    }

    public static final int[] BLUE = {
            0xFF3C6EF5, 0xFF5B8CFF, 0xFF3C6EF5, 0xFFE8E8E8, 0xFF7E86A0,
            0xFF0A0F1E, 0xFF141B30, 0xFF3C6EF5, 0xFF232A45, 0xFF232A45,
            0xFF000000, 0xFF12141C
    };

    public static final Preset[] ALL = {
            new Preset("Blue", BLUE),
            new Preset("Orange", new int[]{
                    0xFFFF7A18, 0xFFFF9F43, 0xFFFF7A18, 0xFFF2EDE6, 0xFF9A9084,
                    0xFF110D0A, 0xFF1A1510, 0xFFFF7A18, 0xFF2A2218, 0xFF2A2218,
                    0xFF000000, 0xFF141210
            }),
            new Preset("Red", new int[]{
                    0xFFEF4444, 0xFFF87171, 0xFFEF4444, 0xFFF0E8E8, 0xFF9A8484,
                    0xFF120A0A, 0xFF1A1010, 0xFFEF4444, 0xFF2A1818, 0xFF2A1818,
                    0xFF000000, 0xFF141010
            }),
            new Preset("Green", new int[]{
                    0xFF22C55E, 0xFF4ADE80, 0xFF22C55E, 0xFFE8F0EA, 0xFF849A8C,
                    0xFF0A120E, 0xFF101A14, 0xFF22C55E, 0xFF182A20, 0xFF182A20,
                    0xFF000000, 0xFF101412
            }),
            new Preset("Purple", new int[]{
                    0xFFA855F7, 0xFFC084FC, 0xFFA855F7, 0xFFEDE8F2, 0xFF9084A0,
                    0xFF0E0A14, 0xFF14101A, 0xFFA855F7, 0xFF20182A, 0xFF20182A,
                    0xFF000000, 0xFF121014
            }),
            new Preset("Cyan", new int[]{
                    0xFF06B6D4, 0xFF22D3EE, 0xFF06B6D4, 0xFFE6F0F2, 0xFF849A9E,
                    0xFF0A1012, 0xFF10181A, 0xFF06B6D4, 0xFF182428, 0xFF182428,
                    0xFF000000, 0xFF101214
            }),
            new Preset("Pink", new int[]{
                    0xFFEC4899, 0xFFF472B6, 0xFFEC4899, 0xFFF2E8EE, 0xFF9A8490,
                    0xFF120A10, 0xFF1A1016, 0xFFEC4899, 0xFF2A1822, 0xFF2A1822,
                    0xFF000000, 0xFF141012
            })
    };

    private ThemePresets() {
    }

    public static int indexOf(String name) {
        if (name == null) return 0;
        for (int i = 0; i < ALL.length; i++) {
            if (ALL[i].name().equalsIgnoreCase(name)) return i;
        }
        return -1;
    }
}
