package UI;

import GPA.GPACalculator;
import classes.Class;
import grades.Grade;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class ClassDetailDialog extends JDialog {
    private final Class classData;
    private final Runnable saveCallback;
    private final Runnable refreshCallback;
    private final GPACalculator calculator = new GPACalculator();

    private final JLabel gradeLabel = new JLabel();
    private final JLabel gpaLabel = new JLabel();
    private final JTextArea categorySummary = new JTextArea();
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"Assignment", "Category", "Score", "Max", "Weight %", "Percent"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable table = new JTable(model);

    public ClassDetailDialog(Window owner, Class classData, Runnable saveCallback, Runnable refreshCallback) {
        super(owner, classData.getClassName(), ModalityType.APPLICATION_MODAL);
        this.classData = classData;
        this.saveCallback = saveCallback;
        this.refreshCallback = refreshCallback;
        setSize(900, 650);
        setMinimumSize(new Dimension(760, 560));
        setLocationRelativeTo(owner);
        buildUI();
        refresh();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(new EmptyBorder(14, 14, 14, 14));

        JPanel header = new JPanel(new BorderLayout());
        JPanel identity = new JPanel();
        identity.setLayout(new BoxLayout(identity, BoxLayout.Y_AXIS));
        JLabel title = new JLabel(classData.getClassName());
        title.setFont(title.getFont().deriveFont(Font.BOLD, 26f));
        identity.add(title);
        identity.add(new JLabel(classData.getTeacherName() + "  •  " + classData.getCourseLevel()
                + "  •  " + classData.getSemester() + " " + classData.getAcademicYear()));
        header.add(identity, BorderLayout.WEST);

        JPanel metrics = new JPanel(new GridLayout(2, 1));
        gradeLabel.setFont(gradeLabel.getFont().deriveFont(Font.BOLD, 22f));
        gpaLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        metrics.add(gradeLabel);
        metrics.add(gpaLabel);
        header.add(metrics, BorderLayout.EAST);
        root.add(header, BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        split.setResizeWeight(0.72);
        split.setTopComponent(new JScrollPane(table));

        categorySummary.setEditable(false);
        categorySummary.setLineWrap(true);
        categorySummary.setWrapStyleWord(true);
        categorySummary.setBorder(BorderFactory.createTitledBorder("Category Breakdown"));
        split.setBottomComponent(new JScrollPane(categorySummary));
        root.add(split, BorderLayout.CENTER);

        JButton add = new JButton("+ Add Grade");
        JButton remove = new JButton("Remove Selected");
        JButton planner = new JButton("Grade Planning Tools");
        JButton close = new JButton("Close");
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actions.add(add);
        actions.add(remove);
        actions.add(planner);
        actions.add(Box.createHorizontalStrut(20));
        actions.add(close);
        root.add(actions, BorderLayout.SOUTH);

        add.addActionListener(e -> addGrade());
        remove.addActionListener(e -> removeGrade());
        planner.addActionListener(e -> new GradeCalculatorFrame().setVisible(true));
        close.addActionListener(e -> dispose());

        setContentPane(root);
    }

    private void addGrade() {
        JTextField assignment = new JTextField();
        JComboBox<String> category = new JComboBox<>(new String[]{
                "Assignments", "Homework", "Quiz", "Test", "Project", "Exam", "Participation", "Other"
        });
        category.setEditable(true);
        JTextField score = new JTextField();
        JTextField max = new JTextField("100");
        JTextField weight = new JTextField("0");

        JPanel p = new JPanel(new GridLayout(0, 2, 8, 8));
        p.add(new JLabel("Assignment")); p.add(assignment);
        p.add(new JLabel("Category")); p.add(category);
        p.add(new JLabel("Score Earned")); p.add(score);
        p.add(new JLabel("Maximum Score")); p.add(max);
        p.add(new JLabel("Weight % (0 = equal)")); p.add(weight);

        if (JOptionPane.showConfirmDialog(this, p, "Add Grade",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) return;

        try {
            Object categoryValue = category.isEditable() ? category.getEditor().getItem() : category.getSelectedItem();
            Grade grade = new Grade(assignment.getText(),
                    Double.parseDouble(score.getText().trim()),
                    Double.parseDouble(max.getText().trim()),
                    Double.parseDouble(weight.getText().trim()),
                    categoryValue == null ? "Assignments" : categoryValue.toString());
            classData.addGrade(grade);
            saveCallback.run();
            refreshCallback.run();
            refresh();
        } catch (NumberFormatException ex) {
            showError("Score, maximum score, and weight must be numbers.");
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    private void removeGrade() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        classData.getGrades().remove(row);
        saveCallback.run();
        refreshCallback.run();
        refresh();
    }

    private void refresh() {
        model.setRowCount(0);
        Map<String, double[]> categoryData = new LinkedHashMap<>();
        for (Grade g : classData.getGrades()) {
            model.addRow(new Object[]{g.getAssignmentName(), g.getCategory(),
                    g.getScore(), g.getMaxScore(), g.getWeight(), String.format("%.2f%%", g.getPercentage())});
            double[] stats = categoryData.computeIfAbsent(g.getCategory(), k -> new double[2]);
            stats[0] += g.getPercentage();
            stats[1] += 1;
        }

        double average = calculator.calculateClassAverage(classData);
        String letter = classData.getGrades().isEmpty() ? "--" : calculator.letterGrade(average);
        gradeLabel.setText(classData.getGrades().isEmpty() ? "No grades yet" : String.format("%.2f%% — %s", average, letter));
        gpaLabel.setText(classData.getGrades().isEmpty() ? "" : String.format("Unweighted GPA: %.2f",
                calculator.percentageToGPA(average)));

        if (categoryData.isEmpty()) {
            categorySummary.setText("Add grades to see category averages.");
        } else {
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, double[]> entry : categoryData.entrySet()) {
                double avg = entry.getValue()[0] / entry.getValue()[1];
                sb.append(String.format("%-16s  %.2f%%  (%s)%n", entry.getKey(), avg, calculator.letterGrade(avg)));
            }
            if (classData.getGrades().stream().anyMatch(g -> g.getWeight() > 0)) {
                sb.append("\nClass average uses the entered assignment weights. Weights are normalized across weighted items.");
            } else {
                sb.append("\nClass average currently uses equal weighting because all assignment weights are 0.");
            }
            categorySummary.setText(sb.toString());
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Grades", JOptionPane.ERROR_MESSAGE);
    }
}
