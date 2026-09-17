package UI;

import GPA.GPACalculator;
import GPA.GPARecord;
import account.User;
import classes.Class;
import storage.StorageManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GPAPanel extends JPanel {
    private final User user;
    private final StorageManager storage;
    private final ClassesPanel classesPanel;
    private final GPACalculator calculator = new GPACalculator();
    private final List<GPARecord> records;

    private final JTextField semesterField = new JTextField("Fall");
    private final JTextField yearField = new JTextField("2026-2027");
    private final JTextField honorsBonusField = new JTextField("0.5");
    private final JTextField apIbBonusField = new JTextField("1.0");
    private final JTextField dualBonusField = new JTextField("1.0");
    private final JTextField weightedCapField = new JTextField("5.0");

    private final JLabel semesterResult = new JLabel("--");
    private final JLabel cumulativeResult = new JLabel("--");
    private final JLabel weightedSemesterResult = new JLabel("--");
    private final JLabel weightedCumulativeResult = new JLabel("--");

    private final DefaultTableModel historyModel = new DefaultTableModel(
            new Object[]{"Academic Year", "Semester", "Unweighted GPA"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };

    public GPAPanel(User user, StorageManager storage, ClassesPanel classesPanel) {
        this.user = user;
        this.storage = storage;
        this.classesPanel = classesPanel;
        this.records = new ArrayList<>(storage.loadGpaRecords(user.getUsername()));
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(12, 12, 12, 12));
        buildUI();
        refreshHistory();
    }

    private void buildUI() {
        JPanel settings = new JPanel(new GridLayout(2, 6, 8, 6));
        settings.setBorder(BorderFactory.createTitledBorder("GPA Settings"));
        settings.add(new JLabel("Semester"));
        settings.add(new JLabel("Academic Year"));
        settings.add(new JLabel("Honors Bonus"));
        settings.add(new JLabel("AP / IB Bonus"));
        settings.add(new JLabel("Dual Bonus"));
        settings.add(new JLabel("Weighted Cap"));
        settings.add(semesterField);
        settings.add(yearField);
        settings.add(honorsBonusField);
        settings.add(apIbBonusField);
        settings.add(dualBonusField);
        settings.add(weightedCapField);

        JButton calculateButton = new JButton("Calculate GPA");
        JButton saveRecordButton = new JButton("Save Unweighted Semester GPA Record");
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER));
        actions.add(calculateButton);
        actions.add(saveRecordButton);

        JPanel metrics = new JPanel(new GridLayout(2, 2, 12, 12));
        metrics.add(metric("Semester GPA (unweighted / 4.0)", semesterResult));
        metrics.add(metric("Cumulative GPA (unweighted / 4.0)", cumulativeResult));
        metrics.add(metric("Semester GPA (weighted)", weightedSemesterResult));
        metrics.add(metric("Cumulative GPA (weighted)", weightedCumulativeResult));

        JPanel north = new JPanel(new BorderLayout(8, 8));
        north.add(settings, BorderLayout.NORTH);
        north.add(actions, BorderLayout.CENTER);
        north.add(metrics, BorderLayout.SOUTH);

        add(north, BorderLayout.NORTH);
        add(new JScrollPane(new JTable(historyModel)), BorderLayout.CENTER);

        JTextArea note = new JTextArea(
                "Unweighted GPA always uses the standard 4.0 cap. Weighted GPA uses each class's course level. " +
                "The default bonuses are Honors +0.5, AP/IB +1.0, and Dual Enrollment +1.0 with a 5.0 cap. " +
                "Change these values to match your school.");
        note.setEditable(false);
        note.setLineWrap(true);
        note.setWrapStyleWord(true);
        note.setOpaque(false);
        add(note, BorderLayout.SOUTH);

        calculateButton.addActionListener(e -> calculate());
        saveRecordButton.addActionListener(e -> saveRecord());
    }

    private JPanel metric(String title, JLabel value) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEtchedBorder());
        JLabel heading = new JLabel(title, SwingConstants.CENTER);
        value.setHorizontalAlignment(SwingConstants.CENTER);
        value.setFont(value.getFont().deriveFont(Font.BOLD, 22f));
        panel.add(heading, BorderLayout.NORTH);
        panel.add(value, BorderLayout.CENTER);
        return panel;
    }

    private void calculate() {
        try {
            List<Class> classes = classesPanel.getClasses();
            double honors = Double.parseDouble(honorsBonusField.getText().trim());
            double apIb = Double.parseDouble(apIbBonusField.getText().trim());
            double dual = Double.parseDouble(dualBonusField.getText().trim());
            double cap = Double.parseDouble(weightedCapField.getText().trim());

            double semester = calculator.calculateSemesterGPA(
                    classes, semesterField.getText().trim(), yearField.getText().trim());
            double cumulative = calculator.calculateCumulativeGPA(classes);
            double weightedSemester = calculator.calculateWeightedSemesterGPA(
                    classes, semesterField.getText().trim(), yearField.getText().trim(), honors, apIb, dual, cap);
            double weightedCumulative = calculator.calculateWeightedCumulativeGPA(
                    classes, honors, apIb, dual, cap);

            semesterResult.setText(String.format("%.2f", semester));
            cumulativeResult.setText(String.format("%.2f", cumulative));
            weightedSemesterResult.setText(String.format("%.2f", weightedSemester));
            weightedCumulativeResult.setText(String.format("%.2f", weightedCumulative));
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "GPA bonus and cap values must be numbers.",
                    "GPA", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "GPA", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveRecord() {
        calculate();
        try {
            double gpa = Double.parseDouble(semesterResult.getText());
            records.add(new GPARecord(gpa, yearField.getText().trim(), semesterField.getText().trim()));
            storage.saveGpaRecords(user.getUsername(), records);
            refreshHistory();
        } catch (NumberFormatException ignored) {
        }
    }

    private void refreshHistory() {
        historyModel.setRowCount(0);
        for (GPARecord record : records) {
            historyModel.addRow(new Object[]{record.getAcademicYear(), record.getSemester(),
                    String.format("%.2f", record.getGpa())});
        }
    }
}
