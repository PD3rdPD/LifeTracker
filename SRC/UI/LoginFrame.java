package UI;

import account.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import static UI.AppStyle.*;

/** Persistent LifeTracker shell: sign-in, registration and the student app all stay in one window. */
public class LoginFrame extends JFrame {
 private final AccountManager accountManager; private final LoginManager loginManager;
 private JTextField usernameField; private JPasswordField passwordField;
 public LoginFrame(AccountManager a){super("LifeTracker 1.1.3");accountManager=a;loginManager=new LoginManager(a);setDefaultCloseOperation(EXIT_ON_CLOSE);setMinimumSize(new Dimension(390,700));setSize(430,820);setLocationRelativeTo(null);showLogin();}
 private void setScreen(JComponent c){setContentPane(c);revalidate();repaint();}
 private JPanel shell(){GradientPanel r=new GradientPanel(new GridBagLayout());r.setBorder(new EmptyBorder(24,20,24,20));return r;}
 private AppStyle.RoundPanel card(){AppStyle.RoundPanel p=new AppStyle.RoundPanel(new BorderLayout());p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS));p.setBorder(new EmptyBorder(26,24,26,24));p.setPreferredSize(new Dimension(360,520));return p;}
 private JLabel title(String t,int sz){JLabel l=label(t,sz,Font.BOLD,TEXT);l.setAlignmentX(CENTER_ALIGNMENT);return l;}
 private JTextField field(){JTextField f=new JTextField();styleField(f);return f;}
 private JPasswordField pass(){JPasswordField f=new JPasswordField();styleField(f);return f;}
 private void styleField(JTextField f){f.setFont(font(15,Font.PLAIN));f.setForeground(TEXT);f.setCaretColor(TEXT);f.setBackground(CARD2);f.setMaximumSize(new Dimension(Integer.MAX_VALUE,48));f.setBorder(new CompoundBorder(new LineBorder(accent(),1,true),new EmptyBorder(11,12,11,12)));}
 private void addField(JPanel p,String n,JComponent f){JLabel l=label(n,12,Font.BOLD,MUTED);l.setAlignmentX(LEFT_ALIGNMENT);p.add(l);p.add(Box.createVerticalStrut(4));f.setAlignmentX(LEFT_ALIGNMENT);p.add(f);p.add(Box.createVerticalStrut(12));}
 public void showLogin(){setTitle("LifeTracker 1.1.3 • Sign In");JPanel root=shell(), c=card();c.add(title("LifeTracker",30));c.add(Box.createVerticalStrut(5));JLabel sub=label("Track Today. Build Tomorrow.",12,Font.PLAIN,MUTED);sub.setAlignmentX(CENTER_ALIGNMENT);c.add(sub);c.add(Box.createVerticalStrut(28));usernameField=field();passwordField=pass();addField(c,"Username",usernameField);addField(c,"Password",passwordField);PillButton in=new PillButton("Sign In",accent());in.setAlignmentX(CENTER_ALIGNMENT);in.setMaximumSize(new Dimension(Integer.MAX_VALUE,48));in.addActionListener(e->login());c.add(in);c.add(Box.createVerticalStrut(12));JButton create=link("Create Account");create.addActionListener(e->showRegistration());c.add(create);root.add(c);setScreen(root);getRootPane().setDefaultButton(in);}
 private JButton link(String t){JButton b=new JButton(t);b.setAlignmentX(CENTER_ALIGNMENT);b.setForeground(accent());b.setFont(font(13,Font.BOLD));b.setContentAreaFilled(false);b.setFocusPainted(false);b.setBorder(new EmptyBorder(8,8,8,8));return b;}
 private void login(){User u=loginManager.login(usernameField.getText().trim(),new String(passwordField.getPassword()));if(u==null){JOptionPane.showMessageDialog(this,"Username or password is incorrect.","Sign In",JOptionPane.ERROR_MESSAGE);return;}showDashboard(u);}
 public void showDashboard(User u){setTitle("LifeTracker 1.1.3 • "+u.getName());MainDashboardFrame d=new MainDashboardFrame(accountManager,u,this::signOut);Container content=d.getContentPane();d.setContentPane(new JPanel());d.dispose();setContentPane(content);setMinimumSize(new Dimension(390,700));setSize(Math.max(getWidth(),430),Math.max(getHeight(),760));revalidate();repaint();}
 private void signOut(){showLogin();}
 private void showRegistration(){setTitle("LifeTracker 1.1.3 • Create Account");JPanel root=shell(),c=card();c.setPreferredSize(new Dimension(360,650));JButton back=link("← Back to Sign In");back.setAlignmentX(LEFT_ALIGNMENT);back.addActionListener(e->showLogin());c.add(back);c.add(title("Create Account",25));c.add(Box.createVerticalStrut(18));JTextField name=field(),user=field(),degree=field();JPasswordField pw=pass(),confirm=pass();JComboBox<String> level=new JComboBox<>(new String[]{"Middle School","High School","College","Graduate","Other"});level.setFont(font(14,Font.PLAIN));level.setForeground(TEXT);level.setBackground(CARD2);level.setMaximumSize(new Dimension(Integer.MAX_VALUE,48));addField(c,"Name",name);addField(c,"Username",user);addField(c,"Education Level",level);addField(c,"Target Degree / Career",degree);addField(c,"Password",pw);addField(c,"Confirm Password",confirm);PillButton create=new PillButton("Create Account",accent());create.setAlignmentX(CENTER_ALIGNMENT);create.addActionListener(e->{String a=new String(pw.getPassword()),b=new String(confirm.getPassword());if(!a.equals(b)){JOptionPane.showMessageDialog(this,"Passwords do not match.");return;}try{User made=accountManager.register(user.getText().trim(),a,name.getText().trim(),String.valueOf(level.getSelectedItem()),degree.getText().trim());showLogin();fillUsername(made.getUsername());}catch(Exception ex){JOptionPane.showMessageDialog(this,ex.getMessage(),"Create Account",JOptionPane.ERROR_MESSAGE);}});c.add(create);JScrollPane sc=new JScrollPane(c);sc.setBorder(null);sc.setOpaque(false);sc.getViewport().setOpaque(false);sc.setPreferredSize(new Dimension(380,680));root.add(sc);setScreen(root);}
 public void fillUsername(String u){if(usernameField!=null){usernameField.setText(u);passwordField.setText("");passwordField.requestFocusInWindow();}}
}
