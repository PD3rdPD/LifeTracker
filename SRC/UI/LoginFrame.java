package UI;

import static UI.AppStyle.*;
import UI.AppStyle.GradientPanel;
import UI.AppStyle.PillButton;
import account.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

/**
 * Persistent LifeTracker shell:
 * sign-in, registration, and the student app stay in one window.
 */
public class LoginFrame extends JFrame {

    private final AccountManager accountManager;
    private final LoginManager loginManager;

    private JTextField usernameField;
    private JPasswordField passwordField;

    public LoginFrame(AccountManager a) {

        super("LifeTracker 1.1.4");

        accountManager = a;
        loginManager = new LoginManager(a);

        setDefaultCloseOperation(EXIT_ON_CLOSE);

        setMinimumSize(new Dimension(390, 700));
        setSize(430, 820);

        setLocationRelativeTo(null);

        showLogin();
    }

    private void setScreen(JComponent c) {

        setContentPane(c);

        revalidate();
        repaint();
    }

    private JPanel shell() {

        GradientPanel root =
                new GradientPanel(new GridBagLayout());

        root.setBorder(
                new EmptyBorder(20, 20, 20, 20)
        );

        return root;
    }

    private AppStyle.RoundPanel card(int height) {

        AppStyle.RoundPanel p =
                new AppStyle.RoundPanel(new BorderLayout());

        p.setBorder(
                new EmptyBorder(26, 24, 26, 24)
        );

        p.setPreferredSize(
                new Dimension(350, height)
        );

        p.setMinimumSize(
                new Dimension(320, height)
        );

        return p;
    }

    private JLabel title(String text, int size) {

        JLabel l =
                label(
                        text,
                        size,
                        Font.BOLD,
                        TEXT
                );

        l.setHorizontalAlignment(
                SwingConstants.CENTER
        );

        return l;
    }

    private JTextField field() {

        JTextField f = new JTextField();

        styleField(f);

        return f;
    }

    private JPasswordField pass() {

        JPasswordField f =
                new JPasswordField();

        styleField(f);

        return f;
    }

    private void styleField(JTextField f) {

        f.setFont(
                font(15, Font.PLAIN)
        );

        f.setForeground(TEXT);
        f.setCaretColor(TEXT);
        f.setBackground(CARD2);

        f.setPreferredSize(
                new Dimension(280, 48)
        );

        f.setMinimumSize(
                new Dimension(280, 48)
        );

        f.setMaximumSize(
                new Dimension(280, 48)
        );

        f.setBorder(
                new CompoundBorder(
                        new LineBorder(
                                accent(),
                                1,
                                true
                        ),
                        new EmptyBorder(
                                11,
                                12,
                                11,
                                12
                        )
                )
        );
    }

    /*
     * Creates a field row with the label and input
     * using the exact same width.
     */
    private JPanel fieldBlock(
            String name,
            JComponent field) {

        JPanel block = new JPanel();

        block.setOpaque(false);

        block.setLayout(
                new BoxLayout(
                        block,
                        BoxLayout.Y_AXIS
                )
        );

        block.setPreferredSize(
                new Dimension(280, 72)
        );

        block.setMaximumSize(
                new Dimension(280, 72)
        );

        JLabel l =
                label(
                        name,
                        12,
                        Font.BOLD,
                        MUTED
                );

        l.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        field.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        block.add(l);

        block.add(
                Box.createVerticalStrut(4)
        );

        block.add(field);

        return block;
    }

    public void showLogin() {

        setTitle(
                "LifeTracker 1.1.4 • Sign In"
        );

        JPanel root = shell();

        AppStyle.RoundPanel c =
                card(390);

        /*
         * This panel controls ALL content inside
         * the card.
         */
        JPanel content = new JPanel();

        content.setOpaque(false);

        content.setLayout(
                new BoxLayout(
                        content,
                        BoxLayout.Y_AXIS
                )
        );

        /*
         * TITLE
         */
        JLabel mainTitle =
                title(
                        "LifeTracker",
                        30
                );

        mainTitle.setAlignmentX(
                Component.CENTER_ALIGNMENT
        );

        content.add(mainTitle);

        content.add(
                Box.createVerticalStrut(5)
        );

        JLabel sub =
                label(
                        "Track Today. Build Tomorrow.",
                        12,
                        Font.PLAIN,
                        MUTED
                );

        sub.setAlignmentX(
                Component.CENTER_ALIGNMENT
        );

        content.add(sub);

        content.add(
                Box.createVerticalStrut(28)
        );

        /*
         * USERNAME
         */
        usernameField = field();

        JPanel usernameBlock =
                fieldBlock(
                        "Username",
                        usernameField
                );

        usernameBlock.setAlignmentX(
                Component.CENTER_ALIGNMENT
        );

        content.add(usernameBlock);

        content.add(
                Box.createVerticalStrut(10)
        );

        /*
         * PASSWORD
         */
        passwordField = pass();

        JPanel passwordBlock =
                fieldBlock(
                        "Password",
                        passwordField
                );

        passwordBlock.setAlignmentX(
                Component.CENTER_ALIGNMENT
        );

        content.add(passwordBlock);

        content.add(
                Box.createVerticalStrut(14)
        );

        /*
         * SIGN IN
         */
        PillButton signIn =
                new PillButton(
                        "Sign In",
                        accent()
                );

        signIn.setPreferredSize(
                new Dimension(280, 48)
        );

        signIn.setMinimumSize(
                new Dimension(280, 48)
        );

        signIn.setMaximumSize(
                new Dimension(280, 48)
        );

        signIn.setAlignmentX(
                Component.CENTER_ALIGNMENT
        );

        signIn.addActionListener(
                e -> login()
        );

        content.add(signIn);

        content.add(
                Box.createVerticalStrut(12)
        );

        /*
         * CREATE ACCOUNT
         */
        JButton create =
                link("Create Account");

        create.setAlignmentX(
                Component.CENTER_ALIGNMENT
        );

        create.addActionListener(
                e -> showRegistration()
        );

        content.add(create);

        /*
         * Center all content inside card.
         */
        JPanel center = new JPanel(
                new GridBagLayout()
        );

        center.setOpaque(false);

        center.add(content);

        c.add(
                center,
                BorderLayout.CENTER
        );

        /*
         * Center card inside window.
         */
        GridBagConstraints gbc =
                new GridBagConstraints();

        gbc.gridx = 0;
        gbc.gridy = 0;

        gbc.weightx = 1.0;
        gbc.weighty = 1.0;

        gbc.anchor =
                GridBagConstraints.CENTER;

        root.add(c, gbc);

        setScreen(root);

        getRootPane()
                .setDefaultButton(signIn);
    }

    private JButton link(String text) {

        JButton b =
                new JButton(text);

        b.setForeground(
                accent()
        );

        b.setFont(
                font(
                        13,
                        Font.BOLD
                )
        );

        b.setContentAreaFilled(false);
        b.setFocusPainted(false);

        b.setBorder(
                new EmptyBorder(
                        8,
                        8,
                        8,
                        8
                )
        );

        return b;
    }

    private void login() {

        User u =
                loginManager.login(
                        usernameField
                                .getText()
                                .trim(),

                        new String(
                                passwordField
                                        .getPassword()
                        )
                );

        if (u == null) {

            JOptionPane.showMessageDialog(
                    this,
                    "Username or password is incorrect.",
                    "Sign In",
                    JOptionPane.ERROR_MESSAGE
            );

            return;
        }

        showDashboard(u);
    }

    public void showDashboard(User u) {

        setTitle(
                "LifeTracker 1.1.4 • "
                        + u.getName()
        );

        MainDashboardFrame dashboard =
                new MainDashboardFrame(
                        accountManager,
                        u,
                        this::signOut
                );

        Container content =
                dashboard.getContentPane();

        dashboard.setContentPane(
                new JPanel()
        );

        dashboard.dispose();

        setContentPane(content);

        setMinimumSize(
                new Dimension(
                        390,
                        700
                )
        );

        setSize(
                Math.max(
                        getWidth(),
                        430
                ),
                Math.max(
                        getHeight(),
                        760
                )
        );

        revalidate();
        repaint();
    }

    private void signOut() {

        showLogin();
    }

    private void showRegistration() {

        setTitle(
                "LifeTracker 1.1.4 • Create Account"
        );

        JPanel root = shell();

        AppStyle.RoundPanel c =
                card(650);

        JPanel content =
                new JPanel();

        content.setOpaque(false);

        content.setLayout(
                new BoxLayout(
                        content,
                        BoxLayout.Y_AXIS
                )
        );

        JButton back =
                link("← Back to Sign In");

        back.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        back.addActionListener(
                e -> showLogin()
        );

        content.add(back);

        JLabel registrationTitle =
                title(
                        "Create Account",
                        25
                );

        registrationTitle.setAlignmentX(
                Component.CENTER_ALIGNMENT
        );

        content.add(registrationTitle);

        content.add(
                Box.createVerticalStrut(18)
        );

        JTextField name = field();
        JTextField user = field();
        JTextField degree = field();

        JPasswordField password =
                pass();

        JPasswordField confirm =
                pass();

        JComboBox<String> level =
                new JComboBox<>(
                        new String[]{
                                "Middle School",
                                "High School",
                                "College",
                                "Graduate",
                                "Other"
                        }
                );

        level.setFont(
                font(
                        14,
                        Font.PLAIN
                )
        );

        level.setForeground(TEXT);
        level.setBackground(CARD2);

        level.setPreferredSize(
                new Dimension(280, 48)
        );

        level.setMinimumSize(
                new Dimension(280, 48)
        );

        level.setMaximumSize(
                new Dimension(280, 48)
        );

        JPanel nameBlock =
                fieldBlock(
                        "Name",
                        name
                );

        JPanel userBlock =
                fieldBlock(
                        "Username",
                        user
                );

        JPanel levelBlock =
                fieldBlock(
                        "Education Level",
                        level
                );

        JPanel degreeBlock =
                fieldBlock(
                        "Target Degree / Career",
                        degree
                );

        JPanel passwordBlock =
                fieldBlock(
                        "Password",
                        password
                );

        JPanel confirmBlock =
                fieldBlock(
                        "Confirm Password",
                        confirm
                );

        JPanel[] blocks = {
                nameBlock,
                userBlock,
                levelBlock,
                degreeBlock,
                passwordBlock,
                confirmBlock
        };

        for (JPanel block : blocks) {

            block.setAlignmentX(
                    Component.CENTER_ALIGNMENT
            );

            content.add(block);

            content.add(
                    Box.createVerticalStrut(5)
            );
        }

        PillButton create =
                new PillButton(
                        "Create Account",
                        accent()
                );

        create.setPreferredSize(
                new Dimension(280, 48)
        );

        create.setMinimumSize(
                new Dimension(280, 48)
        );

        create.setMaximumSize(
                new Dimension(280, 48)
        );

        create.setAlignmentX(
                Component.CENTER_ALIGNMENT
        );

        create.addActionListener(e -> {

            String first =
                    new String(
                            password.getPassword()
                    );

            String second =
                    new String(
                            confirm.getPassword()
                    );

            if (!first.equals(second)) {

                JOptionPane.showMessageDialog(
                        this,
                        "Passwords do not match."
                );

                return;
            }

            try {

                User made =
                        accountManager.register(
                                user.getText().trim(),
                                first,
                                name.getText().trim(),
                                String.valueOf(
                                        level.getSelectedItem()
                                ),
                                degree.getText().trim()
                        );

                showLogin();

                fillUsername(
                        made.getUsername()
                );

            } catch (Exception ex) {

                JOptionPane.showMessageDialog(
                        this,
                        ex.getMessage(),
                        "Create Account",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });

        content.add(create);

        JScrollPane scroll =
                new JScrollPane(content);

        scroll.setBorder(null);
        scroll.setOpaque(false);

        scroll.getViewport()
                .setOpaque(false);

        scroll.setHorizontalScrollBarPolicy(
                ScrollPaneConstants
                        .HORIZONTAL_SCROLLBAR_NEVER
        );

        scroll.setVerticalScrollBarPolicy(
                ScrollPaneConstants
                        .VERTICAL_SCROLLBAR_AS_NEEDED
        );

        c.add(
                scroll,
                BorderLayout.CENTER
        );

        GridBagConstraints gbc =
                new GridBagConstraints();

        gbc.gridx = 0;
        gbc.gridy = 0;

        gbc.weightx = 1.0;
        gbc.weighty = 1.0;

        gbc.anchor =
                GridBagConstraints.CENTER;

        root.add(c, gbc);

        setScreen(root);
    }

    public void fillUsername(String username) {

        if (usernameField != null) {

            usernameField.setText(
                    username
            );

            passwordField.setText("");

            passwordField
                    .requestFocusInWindow();
        }
    }
}