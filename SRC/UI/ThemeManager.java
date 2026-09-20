package UI;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

public final class ThemeManager {
    private ThemeManager() {}

    public static void applySavedTheme() {
        applyTheme(PreferencesManager.getAppearance(), PreferencesManager.getAccentColor());
    }

    public static void applyTheme(String appearance, String accentName) {
        boolean dark = "Dark".equalsIgnoreCase(appearance);
        boolean system = "System".equalsIgnoreCase(appearance);
        Color accent = accent(accentName);
        boolean highContrast = PreferencesManager.getFlag("highContrast");
        // Update the custom mobile UI palette as well as Swing defaults.
        if (dark || system) {
            AppStyle.BG = highContrast ? Color.BLACK : new Color(7,16,34);
            AppStyle.BG2 = highContrast ? new Color(8,8,8) : new Color(12,27,50);
            AppStyle.CARD = highContrast ? new Color(20,20,20) : new Color(18,34,58);
            AppStyle.CARD2 = highContrast ? new Color(30,30,30) : new Color(23,43,72);
            AppStyle.TEXT = Color.WHITE;
            AppStyle.MUTED = highContrast ? new Color(225,225,225) : new Color(166,184,211);
        } else {
            AppStyle.BG = new Color(241,246,253); AppStyle.BG2 = Color.WHITE;
            AppStyle.CARD = Color.WHITE; AppStyle.CARD2 = new Color(231,239,249);
            AppStyle.TEXT = new Color(20,31,48); AppStyle.MUTED = new Color(82,101,128);
        }

        if (!system) {
            Color bg = dark ? new Color(35, 37, 42) : new Color(246, 247, 249);
            Color panel = dark ? new Color(45, 48, 54) : Color.WHITE;
            Color text = dark ? new Color(238, 238, 238) : new Color(30, 30, 30);
            Color field = dark ? new Color(58, 61, 68) : Color.WHITE;
            Color border = dark ? new Color(80, 84, 92) : new Color(205, 208, 214);

            UIManager.put("Panel.background", bg);
            UIManager.put("Viewport.background", bg);
            UIManager.put("Label.foreground", text);
            UIManager.put("TextArea.background", panel);
            UIManager.put("TextArea.foreground", text);
            UIManager.put("TextField.background", field);
            UIManager.put("TextField.foreground", text);
            UIManager.put("PasswordField.background", field);
            UIManager.put("PasswordField.foreground", text);
            UIManager.put("ComboBox.background", field);
            UIManager.put("ComboBox.foreground", text);
            UIManager.put("List.background", panel);
            UIManager.put("List.foreground", text);
            UIManager.put("Table.background", panel);
            UIManager.put("Table.foreground", text);
            UIManager.put("Table.gridColor", border);
            UIManager.put("TableHeader.background", field);
            UIManager.put("TableHeader.foreground", text);
            UIManager.put("TabbedPane.background", bg);
            UIManager.put("TabbedPane.foreground", text);
            UIManager.put("ScrollPane.background", bg);
            UIManager.put("OptionPane.background", bg);
            UIManager.put("OptionPane.messageForeground", text);
            UIManager.put("CheckBox.background", bg);
            UIManager.put("CheckBox.foreground", text);
            UIManager.put("RadioButton.background", bg);
            UIManager.put("RadioButton.foreground", text);
            UIManager.put("TitledBorder.titleColor", text);
        }

        UIManager.put("Button.background", accent);
        UIManager.put("Button.foreground", bestTextFor(accent));
        UIManager.put("Button.select", accent.darker());
        UIManager.put("TabbedPane.selected", accent.brighter());
        UIManager.put("List.selectionBackground", accent);
        UIManager.put("List.selectionForeground", bestTextFor(accent));
        UIManager.put("Table.selectionBackground", accent);
        UIManager.put("Table.selectionForeground", bestTextFor(accent));
        UIManager.put("ProgressBar.foreground", accent);
    }

    public static void refreshAllWindows() {
        for (Window window : Window.getWindows()) {
            SwingUtilities.updateComponentTreeUI(window);
            window.invalidate();
            window.validate();
            window.repaint();
        }
    }

    public static Color accent(String name) {
        if (name == null) return new Color(61, 109, 204);
        return switch (name) {
            case "Purple" -> new Color(116, 78, 180);
            case "Green" -> new Color(51, 133, 91);
            case "Pink" -> new Color(191, 78, 133);
            case "Orange" -> new Color(205, 116, 45);
            case "Teal" -> new Color(0, 145, 154);
            case "Red" -> new Color(205, 70, 70);
            default -> new Color(61, 109, 204);
        };
    }

    private static Color bestTextFor(Color color) {
        double luminance = (0.299 * color.getRed() + 0.587 * color.getGreen() + 0.114 * color.getBlue()) / 255.0;
        return luminance > 0.62 ? Color.BLACK : Color.WHITE;
    }
}
