package easteregg;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TicTacToeDialog extends JDialog {
    private final JButton[] cells = new JButton[9];
    private final char[] board = new char[9];
    private final JLabel status = new JLabel("Your move. You are X.", SwingConstants.CENTER);
    private final Random random = new Random();
    private boolean finished;

    public TicTacToeDialog(Window owner) {
        super(owner, "LifeTracker // Tic-Tac-Toe", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(430, 500);
        setMinimumSize(new Dimension(340, 420));
        setLocationRelativeTo(owner);
        buildUI();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        JLabel title = new JLabel("A strange game.", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 24f));
        root.add(title, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(3, 3, 7, 7));
        Font cellFont = new Font(Font.SANS_SERIF, Font.BOLD, 52);
        for (int i = 0; i < cells.length; i++) {
            final int index = i;
            JButton button = new JButton("");
            button.setFont(cellFont);
            button.setFocusable(false);
            button.addActionListener(e -> playerMove(index));
            cells[i] = button;
            grid.add(button);
        }
        root.add(grid, BorderLayout.CENTER);

        JPanel south = new JPanel(new BorderLayout(6, 6));
        south.add(status, BorderLayout.CENTER);
        JButton reset = new JButton("Play Again");
        reset.addActionListener(e -> reset());
        south.add(reset, BorderLayout.SOUTH);
        root.add(south, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private void playerMove(int index) {
        if (finished || board[index] != '\0') return;
        place(index, 'X');
        if (finishIfNeeded('X')) return;
        if (isBoardFull()) {
            finishDraw();
            return;
        }
        status.setText("LifeTracker is thinking...");
        Timer timer = new Timer(260, e -> {
            ((Timer) e.getSource()).stop();
            computerMove();
        });
        timer.setRepeats(false);
        timer.start();
    }

    private void computerMove() {
        if (finished) return;
        int move = findWinningMove('O');
        if (move < 0) move = findWinningMove('X');
        if (move < 0 && board[4] == '\0') move = 4;
        if (move < 0) {
            int[] corners = {0, 2, 6, 8};
            List<Integer> openCorners = new ArrayList<>();
            for (int c : corners) if (board[c] == '\0') openCorners.add(c);
            if (!openCorners.isEmpty() && random.nextDouble() < 0.8) {
                move = openCorners.get(random.nextInt(openCorners.size()));
            }
        }
        if (move < 0) {
            List<Integer> open = new ArrayList<>();
            for (int i = 0; i < 9; i++) if (board[i] == '\0') open.add(i);
            if (!open.isEmpty()) move = open.get(random.nextInt(open.size()));
        }
        if (move >= 0) place(move, 'O');
        if (finishIfNeeded('O')) return;
        if (isBoardFull()) {
            finishDraw();
            return;
        }
        status.setText("Your move. You are X.");
    }

    private int findWinningMove(char mark) {
        for (int i = 0; i < 9; i++) {
            if (board[i] != '\0') continue;
            board[i] = mark;
            boolean win = hasWon(mark);
            board[i] = '\0';
            if (win) return i;
        }
        return -1;
    }

    private void place(int index, char mark) {
        board[index] = mark;
        cells[index].setText(String.valueOf(mark));
        cells[index].setEnabled(false);
    }

    private boolean finishIfNeeded(char mark) {
        if (!hasWon(mark)) return false;
        finished = true;
        status.setText(mark == 'X' ? "You win." : "LifeTracker wins.");
        disableOpenCells();
        return true;
    }

    private void finishDraw() {
        finished = true;
        status.setText("Draw. The board learned nothing.");
        disableOpenCells();
    }

    private void disableOpenCells() {
        for (JButton cell : cells) cell.setEnabled(false);
    }

    private boolean isBoardFull() {
        for (char c : board) if (c == '\0') return false;
        return true;
    }

    private boolean hasWon(char mark) {
        int[][] wins = {
                {0,1,2},{3,4,5},{6,7,8},
                {0,3,6},{1,4,7},{2,5,8},
                {0,4,8},{2,4,6}
        };
        for (int[] w : wins) {
            if (board[w[0]] == mark && board[w[1]] == mark && board[w[2]] == mark) return true;
        }
        return false;
    }

    private void reset() {
        finished = false;
        for (int i = 0; i < board.length; i++) {
            board[i] = '\0';
            cells[i].setText("");
            cells[i].setEnabled(true);
        }
        status.setText("Your move. You are X.");
    }
}
