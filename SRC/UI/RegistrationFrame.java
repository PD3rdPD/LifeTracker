package UI;

import account.AccountManager;
import account.User;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class RegistrationFrame extends JFrame {
    private final AccountManager accountManager;
    private final LoginFrame loginFrame;

    private final JTextField nameField = new JTextField();
    private final JTextField usernameField = new JTextField();
    private final JPasswordField passwordField = new JPasswordField();
    private final JPasswordField confirmField = new JPasswordField();
    private final JTextField targetDegreeField = new JTextField();
    private final JComboBox<String> educationLevelCombo = new JComboBox<>(new String[]{
            "Middle School", "High School", "College", "Graduate", "Other"
    });

    public RegistrationFrame(AccountManager accountManager, LoginFrame loginFrame) {
        super("LifeTracker - Create Account");
        this.accountManager = accountManager;
        this.loginFrame = loginFrame;
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(530, 500);
        setLocationRelativeTo(loginFrame);
        buildUI();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(new EmptyBorder(22, 28, 22, 28));

        JLabel title = new JLabel("Create LifeTracker Account", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 24f));
        root.add(title, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(7, 5, 7, 5);
        c.fill = GridBagConstraints.HORIZONTAL;

        addRow(form, c, 0, "Name", nameField);
        addRow(form, c, 1, "Username", usernameField);
        addRow(form, c, 2, "Education Level", educationLevelCombo);
        addRow(form, c, 3, "Target Degree / Career", targetDegreeField);
        addRow(form, c, 4, "Password", passwordField);
        addRow(form, c, 5, "Confirm Password", confirmField);

        root.add(form, BorderLayout.CENTER);

        JButton createButton = new JButton("Create Account");
        JButton cancelButton = new JButton("Cancel");
        createButton.setForeground(Color.BLACK);
        cancelButton.setForeground(Color.BLACK);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 4));
        actions.add(createButton);
        actions.add(cancelButton);
        root.add(actions, BorderLayout.SOUTH);

        setContentPane(root);
        getRootPane().setDefaultButton(createButton);
        createButton.addActionListener(e -> register());
        cancelButton.addActionListener(e -> dispose());
    }

    private void addRow(JPanel panel, GridBagConstraints c, int row, String label, JComponent field) {
        c.gridx = 0; c.gridy = row; c.weightx = 0;
        panel.add(new JLabel(label), c);
        c.gridx = 1; c.weightx = 1;
        panel.add(field, c);
    }

    private void register() {
        String name = nameField.getText().trim();
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirm = new String(confirmField.getPassword());
        String educationLevel = (String) educationLevelCombo.getSelectedItem();
        String targetDegree = targetDegreeField.getText().trim();

        if (!password.equals(confirm)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match.",
                    "Registration Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            User user = accountManager.register(username, password, name, educationLevel, targetDegree);
            JOptionPane.showMessageDialog(this, "Account created. You can now log in.",
                    "Registration Complete", JOptionPane.INFORMATION_MESSAGE);
            loginFrame.fillUsername(user.getUsername());
            dispose();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Registration Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
