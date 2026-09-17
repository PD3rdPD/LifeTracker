package easteregg;

import account.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public final class HiddenFeatureController {
    private static final String SOS = "...---...";
    private final JLabel target;
    private final Window owner;
    private final User user;
    private final StringBuilder morse = new StringBuilder();
    private final StringBuilder typed = new StringBuilder();
    private long pressStarted;
    private long lastSymbolAt;
    private boolean devArmed;
    private int devTapCount;
    private long devArmedAt;

    private HiddenFeatureController(JLabel target, Window owner, User user) {
        this.target = target;
        this.owner = owner;
        this.user = user;
    }

    public static void install(JLabel target, Window owner, User user) {
        HiddenFeatureController controller = new HiddenFeatureController(target, owner, user);
        controller.installListeners();
    }

    private void installListeners() {
        target.setFocusable(true);
        target.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        target.setToolTipText(null);

        target.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                pressStarted = System.currentTimeMillis();
                target.requestFocusInWindow();
            }

            @Override public void mouseReleased(MouseEvent e) {
                long duration = System.currentTimeMillis() - pressStarted;
                handlePress(duration);
            }
        });

        target.addKeyListener(new KeyAdapter() {
            @Override public void keyTyped(KeyEvent e) {
                typed.append(e.getKeyChar());
                if (typed.length() > 6) typed.delete(0, typed.length() - 6);
                if (typed.toString().endsWith("</>")) {
                    typed.setLength(0);
                    openConsole();
                }
            }
        });
    }

    private void handlePress(long durationMs) {
        long now = System.currentTimeMillis();

        if (durationMs >= 1800) {
            devArmed = true;
            devTapCount = 0;
            devArmedAt = now;
            morse.setLength(0);
            return;
        }

        if (devArmed) {
            if (now - devArmedAt > 2200) {
                devArmed = false;
                devTapCount = 0;
            } else if (durationMs < 450) {
                devTapCount++;
                if (devTapCount >= 2) {
                    devArmed = false;
                    devTapCount = 0;
                    openConsole();
                    return;
                }
                return;
            }
        }

        if (now - lastSymbolAt > 1600) morse.setLength(0);
        lastSymbolAt = now;
        morse.append(durationMs >= 650 ? '-' : '.');
        String current = morse.toString();
        if (SOS.equals(current)) {
            morse.setLength(0);
            LifeFallGame.show(owner);
        } else if (!SOS.startsWith(current)) {
            morse.setLength(0);
            morse.append(durationMs >= 650 ? '-' : '.');
            if (!SOS.startsWith(morse.toString())) morse.setLength(0);
        }
    }

    private void openConsole() {
        SwingUtilities.invokeLater(() -> new DevConsoleDialog(owner, user).setVisible(true));
    }
}
