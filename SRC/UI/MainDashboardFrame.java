package UI;

import GPA.GPACalculator;
import account.AccountManager;
import account.User;
import classes.Class;
import easteregg.HiddenFeatureController;
import storage.StorageManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class MainDashboardFrame extends JFrame {
    private final AccountManager accountManager;
    private final User user;
    private final StorageManager storage;
    private final ClassesPanel classesPanel;
    private final AcademicHistoryPanel academicHistoryPanel;
    private final PortfolioPanel portfolioPanel;
    private final JLabel profileLine = new JLabel();
    private final JPanel homePanel = new JPanel(new BorderLayout(12, 12));

    public MainDashboardFrame(AccountManager accountManager, User user) {
        super("LifeTracker 1.1.1 - " + user.getName());
        this.accountManager = accountManager;
        this.user = user;
        this.storage = accountManager.getStorageManager();
        this.classesPanel = new ClassesPanel(user, storage);
        this.academicHistoryPanel = new AcademicHistoryPanel(classesPanel);
        this.portfolioPanel = new PortfolioPanel(user, storage, classesPanel);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(980, 700));
        setSize(1180, 820);
        setLocationRelativeTo(null);
        buildUI();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(new EmptyBorder(12, 14, 14, 14));

        JPanel header = new JPanel(new BorderLayout());
        JPanel identity = new JPanel();
        identity.setLayout(new BoxLayout(identity, BoxLayout.Y_AXIS));
        JLabel welcome = new JLabel("LifeTracker");
        welcome.setFont(welcome.getFont().deriveFont(Font.BOLD, 27f));
        HiddenFeatureController.install(welcome, this, user);
        identity.add(welcome);
        refreshProfileLine();
        identity.add(profileLine);
        header.add(identity, BorderLayout.WEST);

        JPanel headerActions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JLabel version = new JLabel("Development 1.1.1");
        JButton logout = new JButton("Logout");
        logout.addActionListener(e -> logout());
        headerActions.add(version);
        headerActions.add(logout);
        header.add(headerActions, BorderLayout.EAST);
        root.add(header, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane(JTabbedPane.LEFT);
        tabs.addTab("Dashboard", homePanel);
        tabs.addTab("Grades", classesPanel);
        tabs.addTab("Academic History", academicHistoryPanel);
        tabs.addTab("Goals", new GoalsPanel(user, storage));
        tabs.addTab("Portfolio", portfolioPanel);
        SharingPanel sharingPanel = new SharingPanel(user, storage);
        tabs.addTab("Sharing", sharingPanel);
        tabs.addTab("Settings", new SettingsPanel(accountManager, user, this::profileChanged));
        tabs.addChangeListener(e -> {
            Component selected = tabs.getSelectedComponent();
            if (selected == homePanel) refreshHome();
            if (selected == academicHistoryPanel) academicHistoryPanel.refresh();
            if (selected == portfolioPanel) portfolioPanel.refresh();
            if (selected == sharingPanel) sharingPanel.refresh();
        });
        root.add(tabs, BorderLayout.CENTER);

        setContentPane(root);
        refreshHome();
    }

    private void refreshHome() {
        homePanel.removeAll();
        homePanel.setBorder(new EmptyBorder(18, 18, 18, 18));

        JPanel heading = new JPanel();
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Welcome, " + user.getName());
        title.setFont(title.getFont().deriveFont(Font.BOLD, 30f));
        heading.add(title);
        heading.add(new JLabel("Your grades, academic progress, goals, and portfolio in one place."));
        homePanel.add(heading, BorderLayout.NORTH);

        List<Class> classes = classesPanel.getClasses();
        GPACalculator calculator = new GPACalculator();
        int gradedClasses = 0;
        double avgTotal = 0.0;
        for (Class c : classes) {
            if (!c.getGrades().isEmpty()) {
                gradedClasses++;
                avgTotal += calculator.calculateClassAverage(c);
            }
        }
        double overallAverage = gradedClasses == 0 ? 0.0 : avgTotal / gradedClasses;
        double unweighted = calculator.calculateCumulativeGPA(classes);
        double weighted = calculator.calculateWeightedCumulativeGPA(classes,
                PreferencesManager.getHonorsBonus(), PreferencesManager.getApIbBonus(),
                PreferencesManager.getDualBonus(), PreferencesManager.getWeightedCap());

        JPanel cards = new JPanel(new GridLayout(2, 2, 12, 12));
        cards.add(metricCard("Saved Classes", Integer.toString(classes.size()), "Open Grades to add or update class work"));
        cards.add(metricCard("Overall Class Average",
                gradedClasses == 0 ? "--" : String.format("%.1f%%  %s", overallAverage, calculator.letterGrade(overallAverage)),
                "Across classes that currently have grades"));
        cards.add(metricCard("Cumulative GPA", gradedClasses == 0 ? "--" : String.format("%.2f", unweighted),
                "Unweighted GPA • 4.0 cap"));
        cards.add(metricCard("Weighted GPA", gradedClasses == 0 ? "--" : String.format("%.2f", weighted),
                "Uses course level and your saved GPA settings"));
        homePanel.add(cards, BorderLayout.CENTER);

        JTextArea tip = new JTextArea("Grades is the main academic workspace. Open a class to add assignments, tests, quizzes, projects, or exams and LifeTracker will calculate what you are making in that class. Academic History shows year-to-year results. Portfolio is where you save your strongest work and work related to your target degree or career.");
        tip.setEditable(false);
        tip.setLineWrap(true);
        tip.setWrapStyleWord(true);
        tip.setOpaque(false);
        tip.setBorder(new EmptyBorder(8, 0, 0, 0));
        homePanel.add(tip, BorderLayout.SOUTH);

        homePanel.revalidate();
        homePanel.repaint();
    }

    private JPanel metricCard(String title, String value, String detail) {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEtchedBorder(), new EmptyBorder(16, 16, 16, 16)));
        JLabel heading = new JLabel(title);
        heading.setFont(heading.getFont().deriveFont(Font.BOLD, 15f));
        JLabel amount = new JLabel(value);
        amount.setFont(amount.getFont().deriveFont(Font.BOLD, 28f));
        JLabel note = new JLabel(detail);
        panel.add(heading, BorderLayout.NORTH);
        panel.add(amount, BorderLayout.CENTER);
        panel.add(note, BorderLayout.SOUTH);
        return panel;
    }

    private void profileChanged() {
        refreshProfileLine();
        portfolioPanel.refresh();
        refreshHome();
    }

    private void refreshProfileLine() {
        String degree = user.getTargetDegree().isBlank() ? "No target degree/career set" : user.getTargetDegree();
        profileLine.setText(user.getEducationLevel() + "  •  @" + user.getUsername() + "  •  " + degree);
    }

    private void logout() {
        new LoginFrame(accountManager).setVisible(true);
        dispose();
    }
}
