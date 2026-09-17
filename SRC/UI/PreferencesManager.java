package UI;

import java.util.prefs.Preferences;

public final class PreferencesManager {
    private static final Preferences PREFS = Preferences.userRoot().node("lifetracker");

    private PreferencesManager() {}

    public static String getLanguage() { return PREFS.get("language", "English"); }
    public static void setLanguage(String language) { PREFS.put("language", language); }

    public static String getAppearance() { return PREFS.get("appearance", "System"); }
    public static void setAppearance(String appearance) { PREFS.put("appearance", appearance); }

    public static String getAccentColor() { return PREFS.get("accentColor", "Blue"); }
    public static void setAccentColor(String accentColor) { PREFS.put("accentColor", accentColor); }

    public static boolean isDarkMode() { return "Dark".equalsIgnoreCase(getAppearance()); }
    public static void setDarkMode(boolean darkMode) { setAppearance(darkMode ? "Dark" : "Light"); }

    public static boolean isLargeText() { return PREFS.getBoolean("largeText", false); }
    public static void setLargeText(boolean largeText) { PREFS.putBoolean("largeText", largeText); }

    public static double getHonorsBonus() { return PREFS.getDouble("honorsBonus", 0.5); }
    public static void setHonorsBonus(double v) { PREFS.putDouble("honorsBonus", v); }
    public static double getApIbBonus() { return PREFS.getDouble("apIbBonus", 1.0); }
    public static void setApIbBonus(double v) { PREFS.putDouble("apIbBonus", v); }
    public static double getDualBonus() { return PREFS.getDouble("dualBonus", 1.0); }
    public static void setDualBonus(double v) { PREFS.putDouble("dualBonus", v); }
    public static double getWeightedCap() { return PREFS.getDouble("weightedCap", 5.0); }
    public static void setWeightedCap(double v) { PREFS.putDouble("weightedCap", v); }
}
