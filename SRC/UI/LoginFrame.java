package UI;

import account.AccountManager;
import account.LoginManager;
import account.User;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class LoginFrame extends JFrame {
    private final AccountManager accountManager;
    private final LoginManager loginManager;
    private final JTextField usernameField = new JTextField();
    private final JPasswordField passwordField = new JPasswordField();

    public LoginFrame(AccountManager accountManager) {
        super("LifeTracker - Login");
        this.accountManager = accountManager;
        this.loginManager = new LoginManager(accountManager);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(460, 360);
        setMinimumSize(new Dimension(430, 330));
        setLocationRelativeTo(null);
        buildUI();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBorder(new EmptyBorder(25, 32, 25, 32));

        JPanel heading = new JPanel();
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("LifeTracker", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 28f));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel subtitle = new JLabel("Sign in to your student workspace", SwingConstants.CENTER);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        heading.add(title);
        heading.add(Box.createVerticalStrut(6));
        heading.add(subtitle);

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(7, 5, 7, 5);
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx = 0; c.gridy = 0; c.weightx = 0;
        form.add(new JLabel("Username"), c);
        c.gridx = 1; c.weightx = 1;
        form.add(usernameField, c);

        c.gridx = 0; c.gridy = 1; c.weightx = 0;
        form.add(new JLabel("Password"), c);
        c.gridx = 1; c.weightx = 1;
        form.add(passwordField, c);

        JButton loginButton = new JButton("Login");
        JButton registerButton = new JButton("Create Account");
        loginButton.setForeground(Color.BLACK);
        registerButton.setForeground(Color.BLACK);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 4));
        actions.add(loginButton);
        actions.add(registerButton);

        JLabel note = new JLabel("Accounts and student data are saved locally on this computer.", SwingConstants.CENTER);

        root.add(heading, BorderLayout.NORTH);
        root.add(form, BorderLayout.CENTER);

        JPanel south = new JPanel();
        south.setLayout(new BoxLayout(south, BoxLayout.Y_AXIS));
        actions.setAlignmentX(Component.CENTER_ALIGNMENT);
        note.setAlignmentX(Component.CENTER_ALIGNMENT);
        south.add(actions);
        south.add(Box.createVerticalStrut(8));
        south.add(note);
        root.add(south, BorderLayout.SOUTH);

        setContentPane(root);
        getRootPane().setDefaultButton(loginButton);

        loginButton.addActionListener(e -> login());
        registerButton.addActionListener(e -> new RegistrationFrame(accountManager, this).setVisible(true));
    }

    private void login() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        User user = loginManager.login(username, password);
        if (user == null) {
            JOptionPane.showMessageDialog(this,
                    "Username or password is incorrect.",
                    "Login Failed",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        new MainDashboardFrame(accountManager, user).setVisible(true);
        dispose();
    }

    public void fillUsername(String username) {
        usernameField.setText(username);
        passwordField.setText("");
        passwordField.requestFocusInWindow();
    }
}
