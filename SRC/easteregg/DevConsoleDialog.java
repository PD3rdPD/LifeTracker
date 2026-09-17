package easteregg;

import account.User;
import security.SpeechService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class DevConsoleDialog extends JDialog {
    private final User user;
    private final JTextArea output = new JTextArea();
    private final JTextField input = new JTextField();
    private boolean awaitingGameAnswer;

    public DevConsoleDialog(Window owner, User user) {
        super(owner, "LifeTracker Developer Console", ModalityType.MODELESS);
        this.user = user;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(720, 470);
        setMinimumSize(new Dimension(480, 340));
        setLocationRelativeTo(owner);
        buildUI();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBorder(new EmptyBorder(12, 12, 12, 12));
        root.setBackground(new Color(10, 14, 12));

        output.setEditable(false);
        output.setLineWrap(true);
        output.setWrapStyleWord(true);
        output.setBackground(new Color(10, 14, 12));
        output.setForeground(new Color(126, 255, 153));
        output.setCaretColor(new Color(126, 255, 153));
        output.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 15));
        output.setText("LifeTracker Developer Console\nBuild 1.1.1\nType help if you know what you are doing.\n\n> ");

        JScrollPane scroll = new JScrollPane(output);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(65, 95, 72)));
        root.add(scroll, BorderLayout.CENTER);

        input.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 15));
        input.setBackground(new Color(18, 24, 20));
        input.setForeground(new Color(126, 255, 153));
        input.setCaretColor(new Color(126, 255, 153));
        input.addActionListener(e -> execute());
        root.add(input, BorderLayout.SOUTH);

        setContentPane(root);
        SwingUtilities.invokeLater(input::requestFocusInWindow);
    }

    private void execute() {
        String command = input.getText().trim();
        input.setText("");
        if (command.isEmpty()) return;
        append(command);

        String lower = command.toLowerCase();
        if (awaitingGameAnswer && lower.equals("yes")) {
            awaitingGameAnswer = false;
            appendLine("A strange game. The only winning move is... actually, let's play.");
            SwingUtilities.invokeLater(() -> new TicTacToeDialog(this).setVisible(true));
            return;
        }
        if (awaitingGameAnswer && (lower.equals("no") || lower.equals("n"))) {
            awaitingGameAnswer = false;
            appendLine("Wise choice.");
            return;
        }

        switch (lower) {
            case "hello" -> {
                appendLine("...");
                SpeechService.speakAsync("Wanna play a game?");
                awaitingGameAnswer = true;
            }
            case "help" -> appendLine("Commands: hello, whoami, status, compile, 42, sudo, clear, exit");
            case "whoami" -> appendLine(user.getName() + " // @" + user.getUsername() + " // " + user.getRole().getDisplayName());
            case "status" -> appendLine("Build 1.1.1 // local development mode // systems nominal");
            case "compile" -> appendLine("Compiling hopes, dreams, and approximately 1 metric ton of Java... success.");
            case "42" -> appendLine("The answer remains 42.");
            case "sudo" -> appendLine("Nice try.");
            case "clear" -> output.setText("");
            case "exit" -> dispose();
            default -> appendLine("command not found: " + command);
        }
    }

    private void append(String command) {
        output.append(command + "\n");
        output.setCaretPosition(output.getDocument().getLength());
    }

    private void appendLine(String text) {
        output.append(text + "\n\n> ");
        output.setCaretPosition(output.getDocument().getLength());
    }
}
