package security;

import java.util.Locale;

public final class SpeechService {
    private SpeechService() {}

    public static void speakAsync(String text) {
        Thread thread = new Thread(() -> speak(text), "lifetracker-speech");
        thread.setDaemon(true);
        thread.start();
    }

    private static void speak(String text) {
        if (text == null || text.isBlank()) return;
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        try {
            if (os.contains("win")) {
                String safe = text.replace("'", "''");
                new ProcessBuilder("powershell", "-NoProfile", "-Command",
                        "Add-Type -AssemblyName System.Speech; " +
                        "$s = New-Object System.Speech.Synthesis.SpeechSynthesizer; " +
                        "$s.Speak('" + safe + "')").start().waitFor();
            } else if (os.contains("mac")) {
                new ProcessBuilder("say", text).start().waitFor();
            } else {
                try {
                    new ProcessBuilder("spd-say", text).start().waitFor();
                } catch (Exception ignored) {
                    java.awt.Toolkit.getDefaultToolkit().beep();
                }
            }
        } catch (Exception ignored) {
            java.awt.Toolkit.getDefaultToolkit().beep();
        }
    }
}
