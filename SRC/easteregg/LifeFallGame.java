package easteregg;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public final class LifeFallGame {
    private LifeFallGame() {}

    public static void show(Window owner) {
        JDialog dialog = new JDialog(owner, "LifeFall", Dialog.ModalityType.MODELESS);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setSize(900, 560);
        dialog.setMinimumSize(new Dimension(520, 420));
        dialog.setLocationRelativeTo(owner);
        dialog.setContentPane(new GamePanel(dialog));
        dialog.setVisible(true);
    }

    private static final class GamePanel extends JPanel {
        private final JDialog dialog;
        private final Timer timer;
        private final Random random = new Random();
        private final List<Obstacle> obstacles = new ArrayList<>();
        private final List<Collectible> collectibles = new ArrayList<>();

        private double playerX = 155;
        private double playerY;
        private double velocityY;
        private boolean leftPressed;
        private boolean rightPressed;
        private boolean jumpHeld;
        private boolean gameOver;
        private boolean paused;
        private int score;
        private int knowledge;
        private long ticks;
        private int nextSpawn = 120;

        GamePanel(JDialog dialog) {
            this.dialog = dialog;
            setLayout(new BorderLayout());
            setFocusable(true);
            setBackground(new Color(20, 34, 27));

            Canvas canvas = new Canvas();
            add(canvas, BorderLayout.CENTER);
            add(buildControls(canvas), BorderLayout.SOUTH);

            installKeyBindings(canvas);
            timer = new Timer(16, e -> {
                if (!paused && !gameOver) updateGame(canvas.getWidth(), canvas.getHeight());
                canvas.repaint();
            });
            timer.start();
            SwingUtilities.invokeLater(canvas::requestFocusInWindow);
        }

        private JPanel buildControls(JComponent focusTarget) {
            JPanel controls = new JPanel(new BorderLayout(8, 8));
            controls.setBorder(BorderFactory.createEmptyBorder(8, 10, 10, 10));

            JPanel movement = new JPanel(new GridLayout(1, 3, 8, 8));
            JButton left = new JButton("◀");
            JButton jump = new JButton("JUMP");
            JButton right = new JButton("▶");
            Dimension touch = new Dimension(120, 48);
            left.setPreferredSize(touch);
            jump.setPreferredSize(touch);
            right.setPreferredSize(touch);
            attachHoldButton(left, () -> leftPressed = true, () -> leftPressed = false);
            attachHoldButton(right, () -> rightPressed = true, () -> rightPressed = false);
            attachHoldButton(jump, () -> {
                jumpHeld = true;
                jump();
            }, () -> jumpHeld = false);
            movement.add(left);
            movement.add(jump);
            movement.add(right);
            controls.add(movement, BorderLayout.CENTER);

            JPanel actions = new JPanel(new GridLayout(1, 3, 8, 8));
            JButton pause = new JButton("Pause");
            pause.addActionListener(e -> {
                paused = !paused;
                pause.setText(paused ? "Resume" : "Pause");
                focusTarget.requestFocusInWindow();
            });
            JButton again = new JButton("Play Again");
            again.addActionListener(e -> {
                reset();
                focusTarget.requestFocusInWindow();
            });
            JButton exit = new JButton("Back");
            exit.addActionListener(e -> {
                timer.stop();
                dialog.dispose();
            });
            actions.add(pause);
            actions.add(again);
            actions.add(exit);
            controls.add(actions, BorderLayout.EAST);
            return controls;
        }

        private void attachHoldButton(JButton button, Runnable pressed, Runnable released) {
            button.addMouseListener(new MouseAdapter() {
                @Override public void mousePressed(MouseEvent e) { pressed.run(); }
                @Override public void mouseReleased(MouseEvent e) { released.run(); }
                @Override public void mouseExited(MouseEvent e) { released.run(); }
            });
            button.addActionListener(e -> released.run());
        }

        private void installKeyBindings(JComponent canvas) {
            bind(canvas, "pressed LEFT", "leftDown", () -> leftPressed = true);
            bind(canvas, "released LEFT", "leftUp", () -> leftPressed = false);
            bind(canvas, "pressed A", "aDown", () -> leftPressed = true);
            bind(canvas, "released A", "aUp", () -> leftPressed = false);
            bind(canvas, "pressed RIGHT", "rightDown", () -> rightPressed = true);
            bind(canvas, "released RIGHT", "rightUp", () -> rightPressed = false);
            bind(canvas, "pressed D", "dDown", () -> rightPressed = true);
            bind(canvas, "released D", "dUp", () -> rightPressed = false);
            bind(canvas, "pressed SPACE", "jump", this::jump);
            bind(canvas, "pressed UP", "jump2", this::jump);
            bind(canvas, "pressed W", "jump3", this::jump);
            bind(canvas, "pressed R", "restart", this::reset);
            bind(canvas, "pressed P", "pause", () -> paused = !paused);
            bind(canvas, "pressed ESCAPE", "exit", () -> {
                timer.stop();
                dialog.dispose();
            });
        }

        private void bind(JComponent c, String key, String name, Runnable action) {
            c.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(key), name);
            c.getActionMap().put(name, new AbstractAction() {
                @Override public void actionPerformed(ActionEvent e) { action.run(); }
            });
        }

        private void jump() {
            if (gameOver) return;
            double ground = groundY(getHeight() - 66);
            if (Math.abs(playerY - ground) < 2) velocityY = -12.7;
        }

        private void updateGame(int width, int height) {
            int playHeight = Math.max(220, height);
            double ground = groundY(playHeight);
            if (ticks == 0) playerY = ground;
            ticks++;
            score++;

            double speed = 4.5 + Math.min(4.0, ticks / 2200.0);
            if (leftPressed) playerX -= 3.8;
            if (rightPressed) playerX += 3.8;
            playerX = Math.max(35, Math.min(Math.max(35, width - 70), playerX));

            velocityY += 0.68;
            playerY += velocityY;
            if (playerY > ground) {
                playerY = ground;
                velocityY = 0;
            }

            nextSpawn--;
            if (nextSpawn <= 0 && width > 0) {
                spawn(width, ground);
                nextSpawn = 95 + random.nextInt(90);
            }

            Rectangle player = new Rectangle((int) playerX, (int) playerY - 42, 30, 42);
            Iterator<Obstacle> oi = obstacles.iterator();
            while (oi.hasNext()) {
                Obstacle obstacle = oi.next();
                obstacle.x -= speed;
                if (obstacle.x + obstacle.width < -30) {
                    oi.remove();
                    continue;
                }
                if (player.intersects(obstacle.bounds(ground))) {
                    gameOver = true;
                }
            }

            Iterator<Collectible> ci = collectibles.iterator();
            while (ci.hasNext()) {
                Collectible item = ci.next();
                item.x -= speed;
                if (item.x < -30) {
                    ci.remove();
                    continue;
                }
                Rectangle itemBounds = new Rectangle((int) item.x - 10, (int) item.y - 10, 20, 20);
                if (player.intersects(itemBounds)) {
                    knowledge += 100;
                    score += 250;
                    ci.remove();
                }
            }
        }

        private double groundY(int playHeight) {
            return Math.max(130, playHeight - 64);
        }

        private void spawn(int width, double ground) {
            int type = random.nextInt(3);
            int h = switch (type) {
                case 0 -> 28;
                case 1 -> 18;
                default -> 36;
            };
            int w = switch (type) {
                case 0 -> 50;
                case 1 -> 42;
                default -> 24;
            };
            obstacles.add(new Obstacle(width + 40, w, h, type));
            if (random.nextDouble() < 0.65) {
                collectibles.add(new Collectible(width + 100 + random.nextInt(90), ground - 75 - random.nextInt(75)));
            }
        }

        private void reset() {
            obstacles.clear();
            collectibles.clear();
            playerX = 155;
            playerY = 0;
            velocityY = 0;
            gameOver = false;
            paused = false;
            score = 0;
            knowledge = 0;
            ticks = 0;
            nextSpawn = 120;
        }

        private final class Canvas extends JPanel {
            Canvas() {
                setFocusable(true);
                setOpaque(true);
            }

            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();
                double ground = groundY(h);

                GradientPaint sky = new GradientPaint(0, 0, new Color(28, 57, 46), 0, h, new Color(78, 110, 66));
                g2.setPaint(sky);
                g2.fillRect(0, 0, w, h);

                g2.setColor(new Color(21, 77, 46));
                for (int x = -40; x < w + 100; x += 95) {
                    int offset = (int) ((ticks * 0.8) % 95);
                    g2.fillOval(x - offset, 55, 130, 210);
                }

                g2.setColor(new Color(83, 62, 36));
                g2.fillRect(0, (int) ground, w, Math.max(0, h - (int) ground));
                g2.setColor(new Color(51, 130, 67));
                g2.fillRect(0, (int) ground - 8, w, 8);

                for (Obstacle obstacle : obstacles) obstacle.paint(g2, ground);
                for (Collectible item : collectibles) item.paint(g2);

                int px = (int) playerX;
                int py = (int) playerY;
                g2.setColor(new Color(235, 214, 167));
                g2.fillOval(px + 5, py - 42, 20, 20);
                g2.setColor(new Color(45, 67, 128));
                g2.fillRoundRect(px, py - 24, 30, 24, 8, 8);
                g2.setColor(new Color(35, 35, 35));
                g2.fillRect(px + 4, py, 8, 18);
                g2.fillRect(px + 18, py, 8, 18);

                g2.setColor(Color.WHITE);
                g2.setFont(getFont().deriveFont(Font.BOLD, 16f));
                g2.drawString("LifeFall", 16, 26);
                g2.drawString("Score: " + score, 16, 48);
                g2.drawString("Knowledge: " + knowledge, 16, 70);

                if (paused || gameOver) {
                    g2.setColor(new Color(0, 0, 0, 165));
                    g2.fillRect(0, 0, w, h);
                    g2.setColor(Color.WHITE);
                    g2.setFont(getFont().deriveFont(Font.BOLD, 32f));
                    String headline = gameOver ? "Semester Over" : "Paused";
                    int tx = Math.max(20, (w - g2.getFontMetrics().stringWidth(headline)) / 2);
                    g2.drawString(headline, tx, h / 2 - 20);
                    g2.setFont(getFont().deriveFont(Font.PLAIN, 17f));
                    String line = gameOver ? "Press R or Play Again to try again" : "Press P or Resume";
                    int lx = Math.max(20, (w - g2.getFontMetrics().stringWidth(line)) / 2);
                    g2.drawString(line, lx, h / 2 + 14);
                    if (gameOver) {
                        String earned = "Knowledge earned: " + knowledge;
                        int ex = Math.max(20, (w - g2.getFontMetrics().stringWidth(earned)) / 2);
                        g2.drawString(earned, ex, h / 2 + 40);
                    }
                }
                g2.dispose();
            }
        }

        private static final class Obstacle {
            double x;
            final int width;
            final int height;
            final int type;

            Obstacle(double x, int width, int height, int type) {
                this.x = x;
                this.width = width;
                this.height = height;
                this.type = type;
            }

            Rectangle bounds(double ground) {
                return new Rectangle((int) x, (int) ground - height, width, height);
            }

            void paint(Graphics2D g, double ground) {
                if (type == 1) {
                    g.setColor(new Color(72, 116, 47));
                    g.fillOval((int) x, (int) ground - height, width, height);
                    g.setColor(new Color(30, 48, 25));
                    g.drawArc((int) x + 8, (int) ground - height + 3, width - 12, height - 5, 0, 180);
                } else if (type == 2) {
                    g.setColor(new Color(48, 38, 28));
                    g.fillRoundRect((int) x, (int) ground - height, width, height, 8, 8);
                } else {
                    g.setColor(new Color(92, 58, 30));
                    g.fillRoundRect((int) x, (int) ground - height, width, height, 18, 18);
                    g.setColor(new Color(55, 37, 23));
                    g.drawLine((int) x + 8, (int) ground - height, (int) x + 8, (int) ground);
                }
            }
        }

        private static final class Collectible {
            double x;
            final double y;

            Collectible(double x, double y) {
                this.x = x;
                this.y = y;
            }

            void paint(Graphics2D g) {
                g.setColor(new Color(244, 235, 181));
                g.fillRoundRect((int) x - 12, (int) y - 9, 24, 18, 4, 4);
                g.setColor(new Color(68, 89, 153));
                g.drawLine((int) x, (int) y - 8, (int) x, (int) y + 8);
            }
        }
    }
}
