package UI;

import account.AccountManager;
import account.User;
import storage.StorageManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class SettingsPanel extends JPanel {
    private final AccountManager accountManager;
    private final User user;
    private final StorageManager storage;
    private final Runnable profileChanged;

    private final JComboBox<String> appearance = new JComboBox<>(new String[]{"System", "Light", "Dark"});
    private final JComboBox<String> accent = new JComboBox<>(new String[]{"Blue", "Purple", "Green", "Pink", "Orange"});
    private final JCheckBox largeText = new JCheckBox("Larger text");
    private final JTextField targetDegree = new JTextField();
    private final JTextField honors = new JTextField();
    private final JTextField apIb = new JTextField();
    private final JTextField dual = new JTextField();
    private final JTextField cap = new JTextField();

    public SettingsPanel(AccountManager accountManager, User user, Runnable profileChanged) {
        this.accountManager = accountManager;
        this.user = user;
        this.storage = accountManager.getStorageManager();
        this.profileChanged = profileChanged;
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(14, 14, 14, 14));
        buildUI();
        load();
    }

    private void buildUI() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Settings");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 28f));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(title);
        content.add(Box.createVerticalStrut(12));

        JPanel appearanceCard = section("Appearance");
        appearanceCard.add(row("Base appearance", appearance));
        appearanceCard.add(row("Accent color", accent));
        appearanceCard.add(largeText);
        JButton applyTheme = new JButton("Apply Appearance");
        applyTheme.addActionListener(e -> applyAppearance());
        appearanceCard.add(applyTheme);
        content.add(appearanceCard);
        content.add(Box.createVerticalStrut(12));

        JPanel profile = section("Academic Profile");
        profile.add(row("Target Degree / Career", targetDegree));
        JButton saveProfile = new JButton("Save Academic Profile");
        saveProfile.addActionListener(e -> saveProfile());
        profile.add(saveProfile);
        content.add(profile);
        content.add(Box.createVerticalStrut(12));

        JPanel gpa = section("Weighted GPA Settings");
        gpa.add(row("Honors bonus", honors));
        gpa.add(row("AP / IB bonus", apIb));
        gpa.add(row("Dual Enrollment bonus", dual));
        gpa.add(row("Weighted GPA cap", cap));
        JButton saveGpa = new JButton("Save GPA Settings");
        saveGpa.addActionListener(e -> saveGpa());
        gpa.add(saveGpa);
        content.add(gpa);
        content.add(Box.createVerticalStrut(12));

        JPanel securityCard = section("Account & Security");
        JTextField userId = new JTextField(user.getUserId());
        userId.setEditable(false);
        JTextField roleField = new JTextField(user.getRole().getDisplayName());
        roleField.setEditable(false);
        JTextField passwordStatus = new JTextField("PBKDF2-HMAC-SHA256 hashed + salted");
        passwordStatus.setEditable(false);
        JTextField encryptionStatus = new JTextField(storage.isEncryptedAtRest()
                ? "Encrypted at rest"
                : "Development local storage - full data encryption is not enabled yet");
        encryptionStatus.setEditable(false);
        securityCard.add(row("Stable User ID", userId));
        securityCard.add(row("Account role", roleField));
        securityCard.add(row("Password storage", passwordStatus));
        securityCard.add(row("Academic data", encryptionStatus));
        content.add(securityCard);
        content.add(Box.createVerticalStrut(12));

        JPanel storageCard = section("Local Storage");
        JTextField path = new JTextField(storage.getDataDirectory().toAbsolutePath().toString());
        path.setEditable(false);
        storageCard.add(row("Data folder", path));
        JTextArea storageNote = new JTextArea("LifeTracker 1.1.1 is still a local development build. The project now has stable account IDs, roles, scoped read-only access grants, expiration/revocation, hashed invitation codes, and an audit trail so the data model can move to a secure cloud/server later without redesigning the academic features.");
        storageNote.setEditable(false);
        storageNote.setLineWrap(true);
        storageNote.setWrapStyleWord(true);
        storageNote.setOpaque(false);
        storageCard.add(storageNote);
        content.add(storageCard);

        add(new JScrollPane(content), BorderLayout.CENTER);
    }

    private JPanel section(String title) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder(title));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 260));
        return panel;
    }

    private JPanel row(String label, JComponent component) {
        JPanel p = new JPanel(new BorderLayout(10, 4));
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        JLabel l = new JLabel(label);
        l.setPreferredSize(new Dimension(180, 30));
        p.add(l, BorderLayout.WEST);
        p.add(component, BorderLayout.CENTER);
        return p;
    }

    private void load() {
        appearance.setSelectedItem(PreferencesManager.getAppearance());
        accent.setSelectedItem(PreferencesManager.getAccentColor());
        largeText.setSelected(PreferencesManager.isLargeText());
        targetDegree.setText(user.getTargetDegree());
        honors.setText(Double.toString(PreferencesManager.getHonorsBonus()));
        apIb.setText(Double.toString(PreferencesManager.getApIbBonus()));
        dual.setText(Double.toString(PreferencesManager.getDualBonus()));
        cap.setText(Double.toString(PreferencesManager.getWeightedCap()));
    }

    private void applyAppearance() {
        PreferencesManager.setAppearance((String) appearance.getSelectedItem());
        PreferencesManager.setAccentColor((String) accent.getSelectedItem());
        PreferencesManager.setLargeText(largeText.isSelected());
        ThemeManager.applyTheme((String) appearance.getSelectedItem(), (String) accent.getSelectedItem());
        ThemeManager.refreshAllWindows();
    }

    private void saveProfile() {
        user.setTargetDegree(targetDegree.getText());
        accountManager.saveUserProfile(user);
        if (profileChanged != null) profileChanged.run();
        JOptionPane.showMessageDialog(this, "Academic profile saved.");
    }

    private void saveGpa() {
        try {
            double h = Double.parseDouble(honors.getText().trim());
            double a = Double.parseDouble(apIb.getText().trim());
            double d = Double.parseDouble(dual.getText().trim());
            double c = Double.parseDouble(cap.getText().trim());
            if (h < 0 || a < 0 || d < 0) throw new IllegalArgumentException("GPA bonuses cannot be negative.");
            if (c < 4.0) throw new IllegalArgumentException("Weighted GPA cap must be at least 4.0.");
            PreferencesManager.setHonorsBonus(h);
            PreferencesManager.setApIbBonus(a);
            PreferencesManager.setDualBonus(d);
            PreferencesManager.setWeightedCap(c);
            JOptionPane.showMessageDialog(this, "Weighted GPA settings saved.");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "GPA settings must be numbers.", "Settings", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Settings", JOptionPane.ERROR_MESSAGE);
        }
    }
}
