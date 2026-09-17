package UI;

import account.User;
import classes.Class;
import portfolio.PortfolioItem;
import storage.StorageManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class PortfolioPanel extends JPanel {
    private final User user;
    private final StorageManager storage;
    private final ClassesPanel classesPanel;
    private final List<PortfolioItem> items;

    private final JLabel targetDegreeLabel = new JLabel();
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"Title", "Type", "Class", "Year", "Grade", "Skills"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable table = new JTable(model);

    public PortfolioPanel(User user, StorageManager storage, ClassesPanel classesPanel) {
        this.user = user;
        this.storage = storage;
        this.classesPanel = classesPanel;
        this.items = new ArrayList<>(storage.loadPortfolio(user.getUsername()));
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(14, 14, 14, 14));
        buildUI();
        refresh();
    }

    private void buildUI() {
        JPanel heading = new JPanel();
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Portfolio");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 28f));
        heading.add(title);
        targetDegreeLabel.setFont(targetDegreeLabel.getFont().deriveFont(Font.BOLD, 15f));
        heading.add(targetDegreeLabel);
        heading.add(new JLabel("Save your best work and projects that support the degree or career you are working toward."));
        add(heading, BorderLayout.NORTH);

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(28);
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) viewSelected();
            }
        });
        add(new JScrollPane(table), BorderLayout.CENTER);

        JButton add = new JButton("+ Add Portfolio Work");
        JButton view = new JButton("View Details");
        JButton open = new JButton("Open File / Link");
        JButton remove = new JButton("Remove");
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.add(add);
        actions.add(view);
        actions.add(open);
        actions.add(remove);
        add(actions, BorderLayout.SOUTH);

        add.addActionListener(e -> addItem());
        view.addActionListener(e -> viewSelected());
        open.addActionListener(e -> openSelected());
        remove.addActionListener(e -> removeSelected());
    }

    public void refresh() {
        targetDegreeLabel.setText("Target Degree / Career: " +
                (user.getTargetDegree().isBlank() ? "Not set yet — add it in Settings" : user.getTargetDegree()));
        model.setRowCount(0);
        for (PortfolioItem item : items) {
            String type = item.isBestWork() && item.isDegreeFocused() ? "Best + Degree" :
                    item.isBestWork() ? "Best Work" : item.isDegreeFocused() ? "Degree Focused" : "Portfolio";
            model.addRow(new Object[]{item.getTitle(), type, item.getClassName(), item.getAcademicYear(), item.getGrade(), item.getSkills()});
        }
    }

    private void addItem() {
        JTextField title = new JTextField();
        JTextArea description = new JTextArea(3, 24);
        JComboBox<String> className = new JComboBox<>();
        className.setEditable(true);
        className.addItem("");
        for (Class c : classesPanel.getClasses()) className.addItem(c.getClassName());
        JTextField year = new JTextField();
        JTextField grade = new JTextField();
        JTextField skills = new JTextField();
        JTextField filePath = new JTextField();
        JButton browse = new JButton("Choose File");
        JTextField link = new JTextField();
        JTextArea reflection = new JTextArea(3, 24);
        JCheckBox best = new JCheckBox("Best Work");
        JCheckBox degree = new JCheckBox("Degree / Career Focused");

        browse.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                filePath.setText(chooser.getSelectedFile().getAbsolutePath());
            }
        });

        JPanel filePanel = new JPanel(new BorderLayout(6, 0));
        filePanel.add(filePath, BorderLayout.CENTER);
        filePanel.add(browse, BorderLayout.EAST);

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.add(row("Title", title));
        form.add(row("Description", new JScrollPane(description)));
        form.add(row("Class", className));
        form.add(row("Academic Year", year));
        form.add(row("Grade", grade));
        form.add(row("Skills", skills));
        form.add(row("File", filePanel));
        form.add(row("Link", link));
        form.add(row("Reflection", new JScrollPane(reflection)));
        JPanel checks = new JPanel(new FlowLayout(FlowLayout.LEFT));
        checks.add(best); checks.add(degree);
        form.add(checks);

        JScrollPane wrapper = new JScrollPane(form);
        wrapper.setPreferredSize(new Dimension(620, 540));
        if (JOptionPane.showConfirmDialog(this, wrapper, "Add Portfolio Work",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) return;

        try {
            String storedPath = filePath.getText().trim();
            if (!storedPath.isBlank()) {
                File source = new File(storedPath);
                if (!source.exists()) throw new IllegalArgumentException("The selected file no longer exists.");
                storedPath = storage.importPortfolioFile(user.getUsername(), source.toPath());
            }
            Object classValue = className.getEditor().getItem();
            PortfolioItem item = new PortfolioItem(title.getText(), description.getText(),
                    classValue == null ? "" : classValue.toString(), year.getText(), grade.getText(), skills.getText(),
                    storedPath, link.getText(), reflection.getText(), best.isSelected(), degree.isSelected());
            items.add(item);
            save();
            refresh();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Portfolio", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel row(String label, Component input) {
        JPanel p = new JPanel(new BorderLayout(8, 4));
        JLabel l = new JLabel(label);
        l.setPreferredSize(new Dimension(115, 28));
        p.add(l, BorderLayout.WEST);
        p.add(input, BorderLayout.CENTER);
        p.setBorder(new EmptyBorder(3, 2, 3, 2));
        return p;
    }

    private PortfolioItem selected() {
        int row = table.getSelectedRow();
        return row < 0 || row >= items.size() ? null : items.get(row);
    }

    private void viewSelected() {
        PortfolioItem item = selected();
        if (item == null) return;
        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setText("Title: " + item.getTitle() + "\n\nDescription:\n" + item.getDescription()
                + "\n\nClass: " + item.getClassName() + "\nAcademic Year: " + item.getAcademicYear()
                + "\nGrade: " + item.getGrade() + "\nSkills: " + item.getSkills()
                + "\n\nBest Work: " + (item.isBestWork() ? "Yes" : "No")
                + "\nDegree / Career Focused: " + (item.isDegreeFocused() ? "Yes" : "No")
                + "\n\nReflection:\n" + item.getReflection()
                + "\n\nSaved File: " + item.getFilePath() + "\nLink: " + item.getLink());
        JScrollPane scroll = new JScrollPane(area);
        scroll.setPreferredSize(new Dimension(620, 470));
        JOptionPane.showMessageDialog(this, scroll, item.getTitle(), JOptionPane.INFORMATION_MESSAGE);
    }

    private void openSelected() {
        PortfolioItem item = selected();
        if (item == null) return;
        try {
            if (!item.getFilePath().isBlank()) {
                Desktop.getDesktop().open(Path.of(item.getFilePath()).toFile());
            } else if (!item.getLink().isBlank()) {
                Desktop.getDesktop().browse(new URI(item.getLink()));
            } else {
                JOptionPane.showMessageDialog(this, "This portfolio item does not have a saved file or link.");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Could not open the file or link: " + ex.getMessage(),
                    "Portfolio", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void removeSelected() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        items.remove(row);
        save();
        refresh();
    }

    private void save() {
        storage.savePortfolio(user.getUsername(), items);
    }
}
