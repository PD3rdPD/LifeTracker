package UI;

import GPA.AcademicGoal;
import account.User;
import storage.StorageManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GoalsPanel extends JPanel {
    private final User user;
    private final StorageManager storage;
    private final List<AcademicGoal> goals;
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"Goal", "Target GPA", "Deadline"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable table = new JTable(model);

    public GoalsPanel(User user, StorageManager storage) {
        this.user = user;
        this.storage = storage;
        this.goals = new ArrayList<>(storage.loadGoals(user.getUsername()));
        setLayout(new BorderLayout(8, 8));
        setBorder(new EmptyBorder(12, 12, 12, 12));
        add(new JScrollPane(table), BorderLayout.CENTER);

        JButton add = new JButton("Add Goal");
        JButton remove = new JButton("Remove Selected Goal");
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actions.add(add); actions.add(remove);
        add(actions, BorderLayout.SOUTH);

        add.addActionListener(e -> addGoal());
        remove.addActionListener(e -> removeGoal());
        refresh();
    }

    private void addGoal() {
        JTextField name = new JTextField();
        JTextField gpa = new JTextField("3.0");
        JTextField deadline = new JTextField("End of semester");
        JPanel panel = new JPanel(new GridLayout(0, 2, 8, 8));
        panel.add(new JLabel("Goal Name")); panel.add(name);
        panel.add(new JLabel("Target GPA")); panel.add(gpa);
        panel.add(new JLabel("Deadline")); panel.add(deadline);

        if (JOptionPane.showConfirmDialog(this, panel, "Add Academic Goal",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) return;
        try {
            double target = Double.parseDouble(gpa.getText().trim());
            if (target < 0 || target > 4.0) throw new IllegalArgumentException("Target GPA must be from 0.0 to 4.0.");
            if (name.getText().trim().isEmpty()) throw new IllegalArgumentException("Goal name is required.");
            goals.add(new AcademicGoal(name.getText().trim(), target, deadline.getText().trim()));
            storage.saveGoals(user.getUsername(), goals);
            refresh();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Target GPA must be a number.", "Goal Error", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Goal Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void removeGoal() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        goals.remove(row);
        storage.saveGoals(user.getUsername(), goals);
        refresh();
    }

    private void refresh() {
        model.setRowCount(0);
        for (AcademicGoal goal : goals) {
            model.addRow(new Object[]{goal.getGoalName(), String.format("%.2f", goal.getTargetGpa()), goal.getDeadline()});
        }
    }
}
