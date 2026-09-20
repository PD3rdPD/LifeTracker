package UI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public final class AppStyle {
    public static Color BG = new Color(7,16,34);
    public static Color BG2 = new Color(12,27,50);
    public static Color CARD = new Color(18,34,58);
    public static Color CARD2 = new Color(23,43,72);
    public static Color TEXT = new Color(246,249,255);
    public static Color MUTED = new Color(166,184,211);
    public static final Color PURPLE = new Color(157,74,255);
    public static final Color CYAN = new Color(44,214,222);
    public static final Color GREEN = new Color(67,224,155);
    public static final Color PINK = new Color(255,91,176);
    public static final Color ORANGE = new Color(255,169,73);
    private AppStyle() {}

    public static Color accent(){ return ThemeManager.accent(PreferencesManager.getAccentColor()); }
    public static Font font(float size, int style){ float scale=(PreferencesManager.getFlag("largeText")||PreferencesManager.isLargeText())?1.18f:1f; String family=PreferencesManager.getFlag("dyslexia")?"Verdana":"Segoe UI"; return new Font(family, style, Math.round(size*scale)); }

    public static class GradientPanel extends JPanel {
        public GradientPanel(LayoutManager lm){ super(lm); setOpaque(false); }
        @Override protected void paintComponent(Graphics g){
            Graphics2D g2=(Graphics2D)g.create();
            g2.setPaint(new GradientPaint(0,0,BG,getWidth(),getHeight(),BG2));
            g2.fillRect(0,0,getWidth(),getHeight()); g2.dispose(); super.paintComponent(g);
        }
    }
    public static class RoundPanel extends JPanel {
        private final int radius; private Color fill;
        public RoundPanel(LayoutManager lm){ this(lm,22,CARD); }
        public RoundPanel(LayoutManager lm,int radius,Color fill){ super(lm);this.radius=radius;this.fill=fill;setOpaque(false); }
        public void setFill(Color c){fill=c;repaint();}
        @Override protected void paintComponent(Graphics g){ Graphics2D g2=(Graphics2D)g.create();g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);g2.setColor(fill);g2.fillRoundRect(0,0,getWidth(),getHeight(),radius,radius);g2.setColor(new Color(255,255,255,22));g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,radius,radius);g2.dispose();super.paintComponent(g); }
    }
    public static class PillButton extends JButton {
        private Color fill;
        public PillButton(String text){ this(text, accent()); }
        public PillButton(String text,Color fill){ super(text);this.fill=fill;setFont(font(14,Font.BOLD));setForeground(Color.WHITE);setBorder(new EmptyBorder(11,18,11,18));setContentAreaFilled(false);setFocusPainted(false);setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); }
        public void setFill(Color c){fill=c;repaint();}
        @Override protected void paintComponent(Graphics g){Graphics2D g2=(Graphics2D)g.create();g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);Color c=getModel().isPressed()?fill.darker():getModel().isRollover()?fill.brighter():fill;g2.setColor(c);g2.fillRoundRect(0,0,getWidth(),getHeight(),18,18);g2.dispose();super.paintComponent(g);}
    }
    public static JLabel label(String text,float size,int style,Color c){JLabel l=new JLabel(text);l.setFont(font(size,style));l.setForeground(c);return l;}
    public static JPanel vbox(int gap){JPanel p=new JPanel();p.setOpaque(false);p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS));return p;}
}
