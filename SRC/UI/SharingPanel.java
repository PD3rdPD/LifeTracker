package UI;

import account.User;
import security.AccessControlService;
import security.AccessGrant;
import security.IssuedAccessGrant;
import security.ShareScope;
import security.UserRole;
import storage.StorageManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.EnumSet;
import java.util.List;

public class SharingPanel extends JPanel {
    private final User user;
    private final StorageManager storage;
    private final AccessControlService accessControl;

    private final JTextField recipient = new JTextField();
    private final JComboBox<String> role = new JComboBox<>(new String[]{"Teacher", "Counselor", "College Reviewer"});
    private final JCheckBox grades = new JCheckBox("Grades", true);
    private final JCheckBox history = new JCheckBox("Academic History", true);
    private final JCheckBox portfolio = new JCheckBox("Portfolio", true);
    private final JCheckBox goals = new JCheckBox("Goals");
    private final JComboBox<String> expiry = new JComboBox<>(new String[]{"30 days", "90 days", "1 year", "No expiration"});
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"Recipient", "Role", "Access", "Expires", "Status", "Grant ID"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable table = new JTable(model);

    public SharingPanel(User user, StorageManager storage) {
        this.user = user;
        this.storage = storage;
        this.accessControl = new AccessControlService(storage);
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(14, 14, 14, 14));
        buildUI();
        refresh();
    }

    private void buildUI() {
        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Sharing & Access");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 28f));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        top.add(title);

        JTextArea notice = new JTextArea(
                "Development preparation: LifeTracker can create consent-based, read-only access grants for teachers, counselors, and college reviewers. " +
                "The grant model, scopes, expiration, revocation, hashed invitation codes, and audit trail are stored locally now. " +
                "Remote sign-in and remote viewing are NOT active yet; those require the future secure server/cloud service.");
        notice.setLineWrap(true);
        notice.setWrapStyleWord(true);
        notice.setEditable(false);
        notice.setOpaque(false);
        notice.setBorder(new EmptyBorder(6, 0, 10, 0));
        notice.setAlignmentX(Component.LEFT_ALIGNMENT);
        top.add(notice);

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.setBorder(BorderFactory.createTitledBorder("Prepare a read-only share grant"));
        form.add(new JLabel("Recipient username or email"));
        form.add(recipient);
        form.add(new JLabel("Recipient role"));
        form.add(role);
        form.add(new JLabel("Expiration"));
        form.add(expiry);

        JPanel scopes = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        scopes.add(grades); scopes.add(history); scopes.add(portfolio); scopes.add(goals);
        form.add(new JLabel("Allowed areas"));
        form.add(scopes);

        JButton create = new JButton("Create Access Grant");
        create.addActionListener(e -> createGrant());
        form.add(new JLabel("Access type"));
        JPanel createRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        createRow.add(new JLabel("Read-only   "));
        createRow.add(create);
        form.add(createRow);
        top.add(form);

        add(top, BorderLayout.NORTH);

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton refresh = new JButton("Refresh");
        refresh.addActionListener(e -> refresh());
        JButton revoke = new JButton("Revoke Selected Grant");
        revoke.addActionListener(e -> revokeSelected());
        bottom.add(refresh);
        bottom.add(revoke);
        add(bottom, BorderLayout.SOUTH);
    }

    private void createGrant() {
        try {
            UserRole selectedRole = switch ((String) role.getSelectedItem()) {
                case "Teacher" -> UserRole.TEACHER;
                case "Counselor" -> UserRole.COUNSELOR;
                default -> UserRole.COLLEGE_REVIEWER;
            };
            EnumSet<ShareScope> scopes = EnumSet.noneOf(ShareScope.class);
            if (grades.isSelected()) scopes.add(ShareScope.GRADES);
            if (history.isSelected()) scopes.add(ShareScope.ACADEMIC_HISTORY);
            if (portfolio.isSelected()) scopes.add(ShareScope.PORTFOLIO);
            if (goals.isSelected()) scopes.add(ShareScope.GOALS);

            Duration duration = switch ((String) expiry.getSelectedItem()) {
                case "30 days" -> Duration.ofDays(30);
                case "90 days" -> Duration.ofDays(90);
                case "1 year" -> Duration.ofDays(365);
                default -> null;
            };

            IssuedAccessGrant issued = accessControl.createReadOnlyGrant(
                    user, recipient.getText(), selectedRole, scopes, duration);
            refresh();
            JTextArea message = new JTextArea(
                    "Invitation code (shown once):\n\n" + issued.getInvitationCode() +
                    "\n\nThis code is prepared for the future remote account system. " +
                    "It cannot be redeemed remotely in LifeTracker 1.1.1 yet. Only its hash is stored.");
            message.setEditable(false);
            message.setLineWrap(true);
            message.setWrapStyleWord(true);
            JOptionPane.showMessageDialog(this, new JScrollPane(message), "Access Grant Created", JOptionPane.INFORMATION_MESSAGE);
            recipient.setText("");
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Sharing", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void revokeSelected() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Select a grant first.");
            return;
        }
        int row = table.convertRowIndexToModel(viewRow);
        String grantId = (String) model.getValueAt(row, 5);
        accessControl.revoke(user, grantId);
        refresh();
    }

    public void refresh() {
        model.setRowCount(0);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                .withZone(ZoneId.systemDefault());
        List<AccessGrant> grants = storage.loadAccessGrants(user.getUsername());
        for (AccessGrant grant : grants) {
            StringBuilder scopeText = new StringBuilder();
            for (ShareScope scope : grant.getScopes()) {
                if (scopeText.length() > 0) scopeText.append(", ");
                scopeText.append(scope.getDisplayName());
            }
            String expires = grant.getExpiresAtEpochMillis() == 0 ? "Never" :
                    fmt.format(Instant.ofEpochMilli(grant.getExpiresAtEpochMillis()));
            String status = grant.isRevoked() ? "Revoked" : grant.isExpired() ? "Expired" : "Prepared";
            model.addRow(new Object[]{grant.getRecipientIdentity(), grant.getRecipientRole().getDisplayName(),
                    scopeText.toString(), expires, status, grant.getGrantId()});
        }
    }
}
