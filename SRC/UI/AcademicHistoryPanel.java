package UI;

import GPA.GPACalculator;
import classes.Class;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.*;
import java.util.List;

public class AcademicHistoryPanel extends JPanel {
    private final ClassesPanel classesPanel;
    private final GPACalculator calculator = new GPACalculator();

    private final DefaultTableModel yearModel = new DefaultTableModel(
            new Object[]{"Academic Year", "Classes", "Overall Average", "Letter", "Unweighted GPA", "Weighted GPA"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final DefaultTableModel classModel = new DefaultTableModel(
            new Object[]{"Year", "Semester", "Class", "Level", "Average", "Letter", "Credits"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };

    public AcademicHistoryPanel(ClassesPanel classesPanel) {
        this.classesPanel = classesPanel;
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(14, 14, 14, 14));
        buildUI();
        refresh();
    }

    private void buildUI() {
        JPanel heading = new JPanel(new BorderLayout());
        JPanel text = new JPanel();
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Academic History");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 28f));
        text.add(title);
        text.add(new JLabel("See how your grades and GPA change from year to year."));
        heading.add(text, BorderLayout.WEST);
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> refresh());
        heading.add(refreshButton, BorderLayout.EAST);
        add(heading, BorderLayout.NORTH);

        JTable years = new JTable(yearModel);
        JTable classes = new JTable(classModel);
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                titledScroll("Year-to-Year Summary", years),
                titledScroll("Class History", classes));
        split.setResizeWeight(0.45);
        add(split, BorderLayout.CENTER);

        JTextArea note = new JTextArea("Unweighted GPA is capped at 4.0. Weighted GPA uses the saved course levels and your GPA settings from Settings.");
        note.setEditable(false);
        note.setLineWrap(true);
        note.setWrapStyleWord(true);
        note.setOpaque(false);
        add(note, BorderLayout.SOUTH);
    }

    private JComponent titledScroll(String title, JTable table) {
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createTitledBorder(title));
        return scroll;
    }

    public void refresh() {
        List<Class> classes = classesPanel.getClasses();
        yearModel.setRowCount(0);
        classModel.setRowCount(0);

        Set<String> years = new TreeSet<>((a, b) -> b.compareToIgnoreCase(a));
        for (Class c : classes) years.add(c.getAcademicYear());

        double honors = PreferencesManager.getHonorsBonus();
        double ap = PreferencesManager.getApIbBonus();
        double dual = PreferencesManager.getDualBonus();
        double cap = PreferencesManager.getWeightedCap();

        for (String year : years) {
            int count = 0;
            for (Class c : classes) {
                if (c.getAcademicYear().equalsIgnoreCase(year) && !c.getGrades().isEmpty()) count++;
            }
            double avg = calculator.calculateYearAverage(classes, year);
            double unweighted = calculator.calculateSemesterGPA(classes, "All", year);
            double weighted = calculator.calculateWeightedSemesterGPA(classes, "All", year, honors, ap, dual, cap);
            yearModel.addRow(new Object[]{year, count,
                    count == 0 ? "--" : String.format("%.2f%%", avg),
                    count == 0 ? "--" : calculator.letterGrade(avg),
                    count == 0 ? "--" : String.format("%.2f", unweighted),
                    count == 0 ? "--" : String.format("%.2f", weighted)});
        }

        classes.stream()
                .sorted(Comparator.comparing(Class::getAcademicYear).reversed().thenComparing(Class::getClassName))
                .forEach(c -> {
                    double avg = calculator.calculateClassAverage(c);
                    classModel.addRow(new Object[]{c.getAcademicYear(), c.getSemester(), c.getClassName(), c.getCourseLevel(),
                            c.getGrades().isEmpty() ? "--" : String.format("%.2f%%", avg),
                            c.getGrades().isEmpty() ? "--" : calculator.letterGrade(avg),
                            String.format("%.2f", c.getCreditHours())});
                });
    }
}
