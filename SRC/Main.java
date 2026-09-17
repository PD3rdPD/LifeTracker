import UI.LoginFrame;
import UI.ThemeManager;
import account.AccountManager;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }
            ThemeManager.applySavedTheme();
            AccountManager accountManager = new AccountManager();
            new LoginFrame(accountManager).setVisible(true);
        });
    }
}
