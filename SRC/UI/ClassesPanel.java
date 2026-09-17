package UI;

import GPA.GPACalculator;
import account.User;
import classes.Class;
import storage.StorageManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class ClassesPanel extends JPanel {
    private final User user;
    private final StorageManager storage;
    private final List<Class> classes;
    private final GPACalculator calculator = new GPACalculator();

    private final DefaultListModel<Class> classListModel = new DefaultListModel<>();
    private final JList<Class> classList = new JList<>(classListModel);
    private final JComboBox<String> yearFilter = new JComboBox<>();
    private final JLabel yearSummary = new JLabel("No classes yet");

    public ClassesPanel(User user, StorageManager storage) {
        this.user = user;
        this.storage = storage;
        this.classes = new ArrayList<>(storage.loadClasses(user.getUsername()));
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(14, 14, 14, 14));
        buildUI();
        refreshAll();
    }

    private void buildUI() {
        JPanel top = new JPanel(new BorderLayout(10, 10));
        JPanel titleBox = new JPanel();
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Grades");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 28f));
        titleBox.add(title);
        titleBox.add(new JLabel("Open a class to add grades and see what you are making in that class."));
        top.add(titleBox, BorderLayout.WEST);

        JPanel yearBox = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        yearBox.add(new JLabel("Academic Year"));
        yearFilter.setPreferredSize(new Dimension(150, 30));
        yearBox.add(yearFilter);
        top.add(yearBox, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        classList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        classList.setFixedCellHeight(62);
        classList.setCellRenderer(new ClassRenderer());
        add(new JScrollPane(classList), BorderLayout.CENTER);

        JButton addClass = new JButton("+ Add Class");
        JButton open = new JButton("Open Class");
        JButton delete = new JButton("Delete Class");
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.add(yearSummary, BorderLayout.WEST);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.add(addClass);
        actions.add(open);
        actions.add(delete);
        bottom.add(actions, BorderLayout.EAST);
        add(bottom, BorderLayout.SOUTH);

        yearFilter.addActionListener(e -> refreshClassList());
        addClass.addActionListener(e -> addClass());
        open.addActionListener(e -> openSelected());
        delete.addActionListener(e -> deleteClass());
        classList.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) openSelected();
            }
        });
    }

    private void addClass() {
        JTextField name = new JTextField();
        JTextField teacher = new JTextField();
        JTextField credits = new JTextField("1.0");
        JTextField semester = new JTextField("Fall");
        JTextField year = new JTextField(currentSuggestedYear());
        JComboBox<String> level = new JComboBox<>(new String[]{
                Class.REGULAR, Class.HONORS, Class.AP_IB, Class.DUAL_ENROLLMENT
        });
        JPanel panel = new JPanel(new GridLayout(0, 2, 8, 8));
        panel.add(new JLabel("Class Name")); panel.add(name);
        panel.add(new JLabel("Teacher")); panel.add(teacher);
        panel.add(new JLabel("Credit Value")); panel.add(credits);
        panel.add(new JLabel("Course Level")); panel.add(level);
        panel.add(new JLabel("Semester")); panel.add(semester);
        panel.add(new JLabel("Academic Year")); panel.add(year);

        if (JOptionPane.showConfirmDialog(this, panel, "Add Class",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) return;

        try {
            Class classData = new Class(name.getText(), teacher.getText(),
                    Double.parseDouble(credits.getText().trim()), semester.getText(), year.getText(),
                    (String) level.getSelectedItem());
            for (Class existing : classes) {
                if (existing.getClassName().equalsIgnoreCase(classData.getClassName())
                        && existing.getAcademicYear().equalsIgnoreCase(classData.getAcademicYear())) {
                    throw new IllegalArgumentException("That class already exists for this academic year.");
                }
            }
            classes.add(classData);
            save();
            refreshAll();
            yearFilter.setSelectedItem(classData.getAcademicYear());
            refreshClassList();
            classList.setSelectedValue(classData, true);
            openSelected();
        } catch (NumberFormatException ex) {
            showError("Credit value must be a number.");
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    private void openSelected() {
        Class selected = classList.getSelectedValue();
        if (selected == null) {
            showError("Select a class first.");
            return;
        }
        Window owner = SwingUtilities.getWindowAncestor(this);
        new ClassDetailDialog(owner, selected, this::save, this::refreshAll).setVisible(true);
    }

    private void deleteClass() {
        Class selected = classList.getSelectedValue();
        if (selected == null) return;
        int choice = JOptionPane.showConfirmDialog(this,
                "Delete " + selected.getClassName() + " and its saved grades?",
                "Delete Class", JOptionPane.YES_NO_OPTION);
        if (choice != JOptionPane.YES_OPTION) return;
        classes.remove(selected);
        save();
        refreshAll();
    }

    private void refreshAll() {
        String selectedYear = (String) yearFilter.getSelectedItem();
        yearFilter.removeAllItems();
        yearFilter.addItem("All Years");
        Set<String> years = new TreeSet<>((a, b) -> b.compareToIgnoreCase(a));
        for (Class c : classes) years.add(c.getAcademicYear());
        for (String year : years) yearFilter.addItem(year);
        if (selectedYear != null) yearFilter.setSelectedItem(selectedYear);
        if (yearFilter.getSelectedIndex() < 0) yearFilter.setSelectedIndex(0);
        refreshClassList();
    }

    private void refreshClassList() {
        String year = (String) yearFilter.getSelectedItem();
        classListModel.clear();
        for (Class c : classes) {
            if (year == null || "All Years".equals(year) || c.getAcademicYear().equalsIgnoreCase(year)) {
                classListModel.addElement(c);
            }
        }
        if (classListModel.isEmpty()) {
            yearSummary.setText("No classes for this year yet.");
        } else if (year != null && !"All Years".equals(year)) {
            double avg = calculator.calculateYearAverage(classes, year);
            yearSummary.setText(String.format("%s overall class average: %.2f%% — %s", year, avg, calculator.letterGrade(avg)));
        } else {
            yearSummary.setText(classes.size() + " saved classes across all years");
        }
        repaint();
    }

    private String currentSuggestedYear() {
        java.time.LocalDate now = java.time.LocalDate.now();
        int year = now.getYear();
        int start = now.getMonthValue() >= 7 ? year : year - 1;
        return start + "-" + (start + 1);
    }

    private void save() { storage.saveClasses(user.getUsername(), classes); }
    public List<Class> getClasses() { return new ArrayList<>(classes); }
    public void reload() {
        classes.clear();
        classes.addAll(storage.loadClasses(user.getUsername()));
        refreshAll();
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Grades", JOptionPane.ERROR_MESSAGE);
    }

    private class ClassRenderer extends JPanel implements ListCellRenderer<Class> {
        private final JLabel name = new JLabel();
        private final JLabel details = new JLabel();
        private final JLabel grade = new JLabel();

        ClassRenderer() {
            setLayout(new BorderLayout(10, 4));
            JPanel center = new JPanel();
            center.setOpaque(false);
            center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
            name.setFont(name.getFont().deriveFont(Font.BOLD, 17f));
            center.add(name);
            center.add(details);
            add(center, BorderLayout.CENTER);
            grade.setFont(grade.getFont().deriveFont(Font.BOLD, 18f));
            add(grade, BorderLayout.EAST);
            setBorder(new EmptyBorder(7, 10, 7, 10));
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends Class> list, Class value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            name.setText(value.getClassName());
            details.setText(value.getCourseLevel() + " • " + value.getSemester() + " • " + value.getAcademicYear()
                    + (value.getTeacherName().isBlank() ? "" : " • " + value.getTeacherName()));
            if (value.getGrades().isEmpty()) {
                grade.setText("No grades");
            } else {
                double avg = calculator.calculateClassAverage(value);
                grade.setText(String.format("%.1f%%  %s", avg, calculator.letterGrade(avg)));
            }
            Color bg = isSelected ? list.getSelectionBackground() : list.getBackground();
            Color fg = isSelected ? list.getSelectionForeground() : list.getForeground();
            setBackground(bg);
            name.setForeground(fg);
            details.setForeground(fg);
            grade.setForeground(fg);
            return this;
        }
    }
}
