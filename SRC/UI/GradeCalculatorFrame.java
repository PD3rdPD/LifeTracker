package UI;

import grades.GradeCalculator;
import grades.GradePathPlanner;
import grades.WeightedCategory;
import GPA.GPACalculator;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Main Swing interface for LifeTracker 1.1.1.
 *
 * Keeps the existing grades/GPA backend and adds the LifeTracker-style UI.
 * No external libraries are required.
 */
public class GradeCalculatorFrame extends JFrame {

    private final GradeCalculator calculator = new GradeCalculator();
    private final GradePathPlanner pathPlanner = new GradePathPlanner();
    private final GPACalculator gpaCalculator = new GPACalculator();

    private String currentLanguage = PreferencesManager.getLanguage();
    private boolean darkMode = PreferencesManager.isDarkMode();
    private boolean largeText = PreferencesManager.isLargeText();

    private final JPanel rootPanel = new JPanel(new BorderLayout());
    private final JTabbedPane tabs = new JTabbedPane();

    private JLabel titleLabel;
    private JLabel taglineLabel;

    private final JComboBox<String> languageCombo =
            new JComboBox<>(Translations.languages());
    private final JComboBox<String> gradingModeCombo =
            new JComboBox<>(new String[]{"Standard", "Weighted"});

    private final JTextField studentNameField = new JTextField();

    // Standard grading
    private final JTextField gradesField = new JTextField();
    private final JCheckBox allowOver100Check = new JCheckBox();
    private final JCheckBox weightedAllowOver100Check = new JCheckBox();
    private final JCheckBox useExtraCreditCheck = new JCheckBox();
    private final JTextField extraCreditField = new JTextField("0");

    // Weighted grading
    private final JTextField testsGradesField = new JTextField();
    private final JTextField testsWeightField = new JTextField();
    private final JTextField quizzesGradesField = new JTextField();
    private final JTextField quizzesWeightField = new JTextField();
    private final JTextField homeworkGradesField = new JTextField();
    private final JTextField homeworkWeightField = new JTextField();

    private final JPanel standardPanel = new JPanel(new GridBagLayout());
    private final JPanel weightedPanel = new JPanel(new GridBagLayout());
    private JPanel modeHolder;

    // Results
    private final JLabel averageResultLabel = new JLabel("--", SwingConstants.CENTER);
    private final JLabel letterResultLabel = new JLabel("--", SwingConstants.CENTER);
    private final JTextArea feedbackArea = createTextArea(3);

    // Goal/path
    private final JTextField targetAverageField = new JTextField();
    private final JTextField remainingAssignmentsField = new JTextField();
    private final JTextArea goalResultArea = createTextArea(3);
    private final JTextArea pathResultArea = createTextArea(10);

    // GPA
    private final JTextField gpaPercentField = new JTextField();
    private final JLabel gpaResultLabel = new JLabel("--");

    // Settings/actions
    private final JCheckBox darkModeCheck = new JCheckBox();
    private final JCheckBox largeTextCheck = new JCheckBox();
    private final JButton calculateButton = new JButton();
    private final JButton calculateGoalButton = new JButton();
    private final JButton calculatePathsButton = new JButton();
    private final JButton exportButton = new JButton();
    private final JButton clearButton = new JButton();

    // State from latest standard calculation
    private List<Double> currentStandardGrades = new ArrayList<>();
    private double currentAverage = Double.NaN;
    private String currentLetterGrade = "";

    public GradeCalculatorFrame() {
        super("LifeTracker - Grade Calculator");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(850, 720));
        setSize(980, 860);
        setLocationRelativeTo(null);

        languageCombo.setSelectedItem(currentLanguage);
        darkModeCheck.setSelected(darkMode);
        largeTextCheck.setSelected(largeText);
        extraCreditField.setEnabled(false);

        buildUI();
        wireEvents();
        updateLanguage();
        applyTheme();
        showSelectedMode();
    }

    private void buildUI() {
        rootPanel.setBorder(new EmptyBorder(14, 18, 18, 18));

        JPanel header = new JPanel(new BorderLayout(12, 8));
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));

        titleLabel = new JLabel("LifeTracker Grade Calculator");
        titleLabel.setName("appTitle");
        taglineLabel = new JLabel("Plan. Calculate. Improve.");

        titlePanel.add(titleLabel);
        titlePanel.add(Box.createVerticalStrut(3));
        titlePanel.add(taglineLabel);
        header.add(titlePanel, BorderLayout.WEST);

        JPanel settingsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        settingsPanel.add(new JLabel("Language:"));
        settingsPanel.add(languageCombo);
        settingsPanel.add(darkModeCheck);
        settingsPanel.add(largeTextCheck);
        header.add(settingsPanel, BorderLayout.EAST);

        rootPanel.add(header, BorderLayout.NORTH);

        tabs.addTab("Calculator", buildCalculatorTab());
        tabs.addTab("GPA", buildGpaTab());
        rootPanel.add(tabs, BorderLayout.CENTER);

        setContentPane(rootPanel);
    }

    private JScrollPane buildCalculatorTab() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(14, 5, 18, 5));

        content.add(buildStudentCard());
        content.add(Box.createVerticalStrut(10));
        content.add(buildCurrentGradeCard());
        content.add(Box.createVerticalStrut(10));
        content.add(buildGoalCard());
        content.add(Box.createVerticalStrut(10));
        content.add(buildPathCard());
        content.add(Box.createVerticalStrut(10));
        content.add(buildAboutCard());
        content.add(Box.createVerticalStrut(10));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 4));
        actions.add(exportButton);
        actions.add(clearButton);
        content.add(actions);

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    private JPanel buildStudentCard() {
        JPanel card = createCard("Student Information");
        GridBagConstraints c = gbc();

        addRow(card, c, 0, "Student Name", studentNameField);
        addRow(card, c, 1, "Grading Mode", gradingModeCombo);

        buildStandardPanel();
        buildWeightedPanel();

        modeHolder = new JPanel(new CardLayout());
        modeHolder.add(standardPanel, "Standard");
        modeHolder.add(weightedPanel, "Weighted");

        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 2;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        card.add(modeHolder, c);

        c.gridy = 3;
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.NONE;
        card.add(calculateButton, c);

        return card;
    }

    private void buildStandardPanel() {
        standardPanel.setBorder(new EmptyBorder(6, 0, 0, 0));
        GridBagConstraints c = gbc();

        addRow(standardPanel, c, 0, "Grades", gradesField);
        gradesField.setToolTipText("Example: 85, 90, 92, 100");

        c.gridx = 1;
        c.gridy = 1;
        c.gridwidth = 1;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;

        JPanel options = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        options.add(allowOver100Check);
        options.add(Box.createHorizontalStrut(16));
        options.add(useExtraCreditCheck);
        standardPanel.add(options, c);

        addRow(standardPanel, c, 2, "Standalone Extra Credit Points", extraCreditField);
    }

    private void buildWeightedPanel() {
        weightedPanel.setBorder(new EmptyBorder(6, 0, 0, 0));
        GridBagConstraints c = gbc();

        addWeightedRow(weightedPanel, c, 0, "Tests", testsGradesField, testsWeightField);
        addWeightedRow(weightedPanel, c, 1, "Quizzes", quizzesGradesField, quizzesWeightField);
        addWeightedRow(weightedPanel, c, 2, "Homework", homeworkGradesField, homeworkWeightField);

        c.gridx = 1;
        c.gridy = 3;
        c.gridwidth = 3;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        weightedPanel.add(weightedAllowOver100Check, c);
    }

    private JPanel buildCurrentGradeCard() {
        JPanel card = createCard("Current Grade");
        card.setLayout(new BorderLayout(10, 10));

        JPanel metrics = new JPanel(new GridLayout(1, 2, 16, 0));
        metrics.add(createMetricPanel("Average", averageResultLabel));
        metrics.add(createMetricPanel("Letter Grade", letterResultLabel));

        card.add(metrics, BorderLayout.NORTH);
        card.add(new JScrollPane(feedbackArea), BorderLayout.CENTER);
        return card;
    }

    private JPanel buildGoalCard() {
        JPanel card = createCard("Grade Goal");
        GridBagConstraints c = gbc();

        addRow(card, c, 0, "Target Average", targetAverageField);
        addRow(card, c, 1, "Remaining Assignments", remainingAssignmentsField);

        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.NONE;
        card.add(calculateGoalButton, c);

        c.gridy = 3;
        c.weightx = 1;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        card.add(new JScrollPane(goalResultArea), c);

        return card;
    }

    private JPanel buildPathCard() {
        JPanel card = createCard("Grade Path Planning");
        card.setLayout(new BorderLayout(8, 8));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.CENTER));
        top.add(calculatePathsButton);
        card.add(top, BorderLayout.NORTH);
        card.add(new JScrollPane(pathResultArea), BorderLayout.CENTER);

        return card;
    }

    private JPanel buildAboutCard() {
        JPanel card = createCard("About");
        card.setLayout(new BorderLayout());

        JTextArea about = createTextArea(5);
        about.setText(
                "LifeTracker 1.1.1\n\n" +
                "A student-focused grade and GPA tool for calculating current grades, " +
                "weighted categories, target grades, grade paths, and academic progress.\n\n" +
                "This Java version continues the LifeTracker project while expanding toward " +
                "multiple classes, semester GPA, cumulative GPA, weighted GPA, and academic goals."
        );

        card.add(about, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildGpaTab() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBorder(new EmptyBorder(18, 18, 18, 18));

        JPanel card = createCard("GPA Calculator");
        GridBagConstraints c = gbc();

        addRow(card, c, 0, "Course Percentage", gpaPercentField);

        JButton calculateGpaButton = new JButton("Convert to GPA");
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.NONE;
        card.add(calculateGpaButton, c);

        JPanel resultPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        resultPanel.add(new JLabel("GPA:"));
        resultPanel.add(gpaResultLabel);

        c.gridy = 2;
        card.add(resultPanel, c);

        JLabel note = new JLabel(
                "Uses your existing GPA scale and preserves its 4.0 cap.",
                SwingConstants.CENTER
        );
        c.gridy = 3;
        c.fill = GridBagConstraints.HORIZONTAL;
        card.add(note, c);

        calculateGpaButton.addActionListener(e -> calculateGpa());
        outer.add(card, BorderLayout.NORTH);
        return outer;
    }

    private void wireEvents() {
        gradingModeCombo.addActionListener(e -> showSelectedMode());

        useExtraCreditCheck.addActionListener(e -> {
            extraCreditField.setEnabled(useExtraCreditCheck.isSelected());
            if (!useExtraCreditCheck.isSelected()) {
                extraCreditField.setText("0");
            }
        });

        allowOver100Check.addActionListener(e ->
                weightedAllowOver100Check.setSelected(allowOver100Check.isSelected()));
        weightedAllowOver100Check.addActionListener(e ->
                allowOver100Check.setSelected(weightedAllowOver100Check.isSelected()));

        languageCombo.addActionListener(e -> {
            Object selected = languageCombo.getSelectedItem();
            if (selected != null) {
                currentLanguage = selected.toString();
                PreferencesManager.setLanguage(currentLanguage);
                updateLanguage();
            }
        });

        darkModeCheck.addActionListener(e -> {
            darkMode = darkModeCheck.isSelected();
            PreferencesManager.setDarkMode(darkMode);
            applyTheme();
        });

        largeTextCheck.addActionListener(e -> {
            largeText = largeTextCheck.isSelected();
            PreferencesManager.setLargeText(largeText);
            applyTheme();
        });

        calculateButton.addActionListener(e -> calculateCurrentGrade());
        calculateGoalButton.addActionListener(e -> calculateGoal());
        calculatePathsButton.addActionListener(e -> calculatePaths());
        exportButton.addActionListener(e -> exportCsv());
        clearButton.addActionListener(e -> clearAll());
    }

    private void calculateCurrentGrade() {
        try {
            requireStudentName();

            boolean weighted = "Weighted".equals(gradingModeCombo.getSelectedItem());
            boolean allowOver100 = allowOver100Check.isSelected();

            if (weighted) {
                List<WeightedCategory> categories = new ArrayList<>();
                addCategoryIfUsed(categories, "Tests", testsGradesField, testsWeightField, allowOver100);
                addCategoryIfUsed(categories, "Quizzes", quizzesGradesField, quizzesWeightField, allowOver100);
                addCategoryIfUsed(categories, "Homework", homeworkGradesField, homeworkWeightField, allowOver100);

                currentAverage = calculator.calculateWeightedAverage(categories);
                currentStandardGrades = new ArrayList<>();
            } else {
                currentStandardGrades =
                        GradeCalculator.parseGrades(gradesField.getText(), allowOver100);
                currentAverage =
                        GradeCalculator.calculateAverage(currentStandardGrades, readExtraCredit());
            }

            currentLetterGrade = GradeCalculator.letterGrade(currentAverage);
            averageResultLabel.setText(String.format("%.2f%%", currentAverage));
            letterResultLabel.setText(currentLetterGrade);
            feedbackArea.setText(GradeCalculator.progressFeedback(currentAverage));
            goalResultArea.setText("");
            pathResultArea.setText("");

        } catch (IllegalArgumentException | IllegalStateException ex) {
            showError(ex.getMessage());
        }
    }

    private void calculateGoal() {
        try {
            requireStandardCalculation();

            double target = parseTarget();
            int remaining = parseRemainingAssignments();
            double extraCredit = readExtraCredit();

            double required = GradeCalculator.calculateRequiredGrade(
                    currentStandardGrades,
                    target,
                    remaining,
                    extraCredit
            );

            if (required <= 0) {
                goalResultArea.setText(
                        Translations.get(currentLanguage, "goal_secured")
                );
            } else if (required > 100 && !allowOver100Check.isSelected()) {
                goalResultArea.setText(String.format(
                        "You would need an average of %.2f%%. %s",
                        required,
                        Translations.get(currentLanguage, "goal_impossible")
                ));
            } else if (required > 100) {
                goalResultArea.setText(String.format(
                        "You would need an average of %.2f%%. %s",
                        required,
                        Translations.get(currentLanguage, "goal_possible_extra")
                ));
            } else {
                goalResultArea.setText(String.format(
                        "%s: %.2f%% on the remaining %d assignment(s).",
                        Translations.get(currentLanguage, "required_average"),
                        required,
                        remaining
                ));
            }

            feedbackArea.setText(
                    GradeCalculator.progressFeedback(currentAverage, target)
            );

        } catch (IllegalArgumentException | IllegalStateException ex) {
            showError(ex.getMessage());
        }
    }

    private void calculatePaths() {
        try {
            requireStandardCalculation();

            double target = parseTarget();
            int remaining = parseRemainingAssignments();
            double extraCredit = readExtraCredit();

            double needed = GradeCalculator.calculateRequiredGrade(
                    currentStandardGrades,
                    target,
                    remaining,
                    extraCredit
            );

            Map<String, List<Double>> paths = pathPlanner.generateGradePaths(
                    currentStandardGrades,
                    target,
                    remaining,
                    extraCredit,
                    allowOver100Check.isSelected()
            );

            StringBuilder output = new StringBuilder();
            output.append(String.format("Target Average: %.2f%%\n", target));
            output.append(String.format("Average Needed: %.2f%%\n\n", needed));

            if (paths.isEmpty()) {
                output.append("No achievable grade path could be generated for these values.");
            } else {
                for (Map.Entry<String, List<Double>> entry : paths.entrySet()) {
                    output.append(entry.getKey()).append(":\n");

                    List<Double> grades = entry.getValue();
                    for (int i = 0; i < grades.size(); i++) {
                        output.append(String.format(
                                "  Assignment %d: %.2f%%\n",
                                i + 1,
                                grades.get(i)
                        ));
                    }
                    output.append("\n");
                }
            }

            pathResultArea.setText(output.toString());
            pathResultArea.setCaretPosition(0);

        } catch (IllegalArgumentException | IllegalStateException ex) {
            showError(ex.getMessage());
        }
    }

    private void addCategoryIfUsed(
            List<WeightedCategory> categories,
            String name,
            JTextField gradesField,
            JTextField weightField,
            boolean allowOver100) {

        String gradesText = gradesField.getText().trim();
        String weightText = weightField.getText().trim();

        if (gradesText.isEmpty() && weightText.isEmpty()) {
            return;
        }

        if (gradesText.isEmpty() || weightText.isEmpty()) {
            throw new IllegalArgumentException(
                    "Enter both grades and a weight for " + name + "."
            );
        }

        double weight;
        try {
            weight = Double.parseDouble(weightText);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(name + " weight must be a number.");
        }

        if (weight < 0) {
            throw new IllegalArgumentException(name + " weight cannot be negative.");
        }

        WeightedCategory category = new WeightedCategory(name, weight);

        List<Double> grades = GradeCalculator.parseGrades(gradesText, allowOver100);
        for (double grade : grades) {
            category.addGrade(grade, allowOver100);
        }

        categories.add(category);
    }

    private double readExtraCredit() {
        if (!useExtraCreditCheck.isSelected()) {
            return 0.0;
        }

        String raw = extraCreditField.getText().trim();
        if (raw.isEmpty()) {
            return 0.0;
        }

        try {
            double value = Double.parseDouble(raw);
            if (value < 0) {
                throw new IllegalArgumentException(
                        Translations.get(currentLanguage, "invalid_extra_credit")
                );
            }
            return value;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(
                    Translations.get(currentLanguage, "invalid_extra_credit")
            );
        }
    }

    private void requireStudentName() {
        if (studentNameField.getText().trim().isEmpty()) {
            throw new IllegalArgumentException(
                    Translations.get(currentLanguage, "name_required")
            );
        }
    }

    private void requireStandardCalculation() {
        if ("Weighted".equals(gradingModeCombo.getSelectedItem())) {
            throw new IllegalStateException(
                    "Grade Goal and Grade Path Planning currently use Standard grading entries."
            );
        }

        if (currentStandardGrades.isEmpty() || Double.isNaN(currentAverage)) {
            throw new IllegalStateException(
                    "Calculate the current Standard grade first."
            );
        }
    }

    private double parseTarget() {
        try {
            double target = Double.parseDouble(targetAverageField.getText().trim());
            if (target < 0 || target > 100) {
                throw new NumberFormatException();
            }
            return target;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(
                    Translations.get(currentLanguage, "invalid_target")
            );
        }
    }

    private int parseRemainingAssignments() {
        try {
            int remaining = Integer.parseInt(remainingAssignmentsField.getText().trim());
            if (remaining <= 0) {
                throw new NumberFormatException();
            }
            return remaining;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(
                    Translations.get(currentLanguage, "invalid_remaining")
            );
        }
    }

    private void calculateGpa() {
        try {
            double percentage = Double.parseDouble(gpaPercentField.getText().trim());
            if (percentage < 0) {
                throw new NumberFormatException();
            }

            double gpa = gpaCalculator.percentageToGPA(percentage);
            gpaResultLabel.setText(String.format("%.2f", gpa));

        } catch (NumberFormatException ex) {
            showError("Enter a valid non-negative course percentage.");
        }
    }

    private void showSelectedMode() {
        if (modeHolder != null && modeHolder.getLayout() instanceof CardLayout) {
            CardLayout layout = (CardLayout) modeHolder.getLayout();
            layout.show(modeHolder, (String) gradingModeCombo.getSelectedItem());
        }

        currentStandardGrades = new ArrayList<>();
        currentAverage = Double.NaN;
        currentLetterGrade = "";
        clearCalculationResults();
    }

    private void clearAll() {
        studentNameField.setText("");
        gradingModeCombo.setSelectedItem("Standard");

        gradesField.setText("");
        allowOver100Check.setSelected(false);
        weightedAllowOver100Check.setSelected(false);
        useExtraCreditCheck.setSelected(false);
        extraCreditField.setText("0");
        extraCreditField.setEnabled(false);

        testsGradesField.setText("");
        testsWeightField.setText("");
        quizzesGradesField.setText("");
        quizzesWeightField.setText("");
        homeworkGradesField.setText("");
        homeworkWeightField.setText("");

        targetAverageField.setText("");
        remainingAssignmentsField.setText("");
        gpaPercentField.setText("");
        gpaResultLabel.setText("--");

        currentStandardGrades = new ArrayList<>();
        currentAverage = Double.NaN;
        currentLetterGrade = "";

        clearCalculationResults();
    }

    private void clearCalculationResults() {
        averageResultLabel.setText("--");
        letterResultLabel.setText("--");
        feedbackArea.setText("");
        goalResultArea.setText("");
        pathResultArea.setText("");
    }

    private void exportCsv() {
        if (Double.isNaN(currentAverage)) {
            showError("Calculate a grade before exporting.");
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("grade_results.csv"));

        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        try (FileWriter writer = new FileWriter(chooser.getSelectedFile())) {
            writer.write("Student,Mode,Average,Letter Grade,Target,Remaining Assignments\n");
            writer.write(
                    csv(studentNameField.getText()) + "," +
                    csv((String) gradingModeCombo.getSelectedItem()) + "," +
                    String.format("%.2f", currentAverage) + "," +
                    csv(currentLetterGrade) + "," +
                    csv(targetAverageField.getText()) + "," +
                    csv(remainingAssignmentsField.getText()) + "\n"
            );

            JOptionPane.showMessageDialog(
                    this,
                    "CSV exported successfully.",
                    "Export",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (IOException ex) {
            showError("Could not export CSV: " + ex.getMessage());
        }
    }

    private String csv(String value) {
        String safe = value == null ? "" : value.replace("\"", "\"\"");
        return "\"" + safe + "\"";
    }

    private void updateLanguage() {
        setTitle(Translations.get(currentLanguage, "app_title"));

        titleLabel.setText(Translations.get(currentLanguage, "app_title"));
        taglineLabel.setText(Translations.get(currentLanguage, "tagline"));

        darkModeCheck.setText(Translations.get(currentLanguage, "dark_mode"));
        largeTextCheck.setText(Translations.get(currentLanguage, "large_text"));
        allowOver100Check.setText(Translations.get(currentLanguage, "allow_over_100"));
        weightedAllowOver100Check.setText(Translations.get(currentLanguage, "allow_over_100"));
        useExtraCreditCheck.setText(Translations.get(currentLanguage, "use_extra_credit"));

        calculateButton.setText(Translations.get(currentLanguage, "calculate"));
        calculateGoalButton.setText(Translations.get(currentLanguage, "calculate_goal"));
        calculatePathsButton.setText(Translations.get(currentLanguage, "calculate_paths"));
        exportButton.setText(Translations.get(currentLanguage, "export"));
        clearButton.setText(Translations.get(currentLanguage, "clear"));

        tabs.setTitleAt(0, Translations.get(currentLanguage, "app_title"));
        tabs.setTitleAt(1, Translations.get(currentLanguage, "gpa"));
    }

    private void applyTheme() {
        Color background = darkMode ? new Color(31, 34, 40) : new Color(245, 247, 250);
        Color card = darkMode ? new Color(43, 47, 54) : Color.WHITE;
        Color text = darkMode ? new Color(235, 238, 242) : new Color(32, 36, 42);
        Color input = darkMode ? new Color(55, 60, 68) : Color.WHITE;
        int fontSize = largeText ? 17 : 14;

        styleComponentTree(rootPanel, background, card, text, input, fontSize);
        rootPanel.setBackground(background);
        rootPanel.revalidate();
        rootPanel.repaint();
    }

    private void styleComponentTree(
            Component component,
            Color background,
            Color card,
            Color text,
            Color input,
            int fontSize) {

        Font font = component.getFont();
        if (font != null) {
            int size = "appTitle".equals(component.getName()) ? fontSize + 12 : fontSize;
            int style = "appTitle".equals(component.getName()) ? Font.BOLD : font.getStyle();
            component.setFont(font.deriveFont(style, (float) size));
        }

        if (component instanceof JTextField ||
                component instanceof JTextArea ||
                component instanceof JComboBox) {
            component.setBackground(input);
            component.setForeground(text);
        } else if (component instanceof JButton) {
            component.setBackground(darkMode ? new Color(76, 95, 122) : new Color(225, 232, 242));
            component.setForeground(text);
        } else if (component instanceof JPanel) {
            component.setBackground(component == rootPanel ? background : card);
            component.setForeground(text);
        } else {
            component.setBackground(background);
            component.setForeground(text);
        }

        if (component instanceof Container) {
            for (Component child : ((Container) component).getComponents()) {
                styleComponentTree(child, background, card, text, input, fontSize);
            }
        }
    }

    private JPanel createCard(String title) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createEtchedBorder(),
                        title,
                        TitledBorder.LEFT,
                        TitledBorder.TOP
                ),
                new EmptyBorder(8, 10, 10, 10)
        ));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1000));
        return panel;
    }

    private JPanel createMetricPanel(String label, JLabel value) {
        JPanel panel = new JPanel(new BorderLayout(4, 4));
        JLabel heading = new JLabel(label, SwingConstants.CENTER);
        value.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(heading, BorderLayout.NORTH);
        panel.add(value, BorderLayout.CENTER);
        return panel;
    }

    private static JTextArea createTextArea(int rows) {
        JTextArea area = new JTextArea(rows, 20);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setEditable(false);
        area.setMargin(new Insets(8, 8, 8, 8));
        return area;
    }

    private GridBagConstraints gbc() {
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        return c;
    }

    private void addRow(
            JPanel panel,
            GridBagConstraints c,
            int row,
            String label,
            JComponent field) {

        c.gridx = 0;
        c.gridy = row;
        c.gridwidth = 1;
        c.weightx = 0;
        panel.add(new JLabel(label), c);

        c.gridx = 1;
        c.weightx = 1;
        panel.add(field, c);
    }

    private void addWeightedRow(
            JPanel panel,
            GridBagConstraints c,
            int row,
            String label,
            JTextField grades,
            JTextField weight) {

        c.gridx = 0;
        c.gridy = row;
        c.gridwidth = 1;
        c.weightx = 0;
        panel.add(new JLabel(label), c);

        c.gridx = 1;
        c.weightx = 1;
        panel.add(grades, c);

        c.gridx = 2;
        c.weightx = 0;
        panel.add(new JLabel("Weight (%)"), c);

        c.gridx = 3;
        c.weightx = 0.3;
        panel.add(weight, c);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(
                this,
                message,
                Translations.get(currentLanguage, "error"),
                JOptionPane.ERROR_MESSAGE
        );
    }
}
