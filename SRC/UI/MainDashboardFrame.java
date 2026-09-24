package UI;
import GPA.*;
import static UI.AppStyle.*;
import UI.AppStyle.GradientPanel;
import UI.AppStyle.PillButton;
import UI.AppStyle.RoundPanel;
import static UI.VectorIcon.Kind.*;
import account.*;
import classes.Class;
import easteregg.HiddenFeatureController;
import grades.Grade;
import java.awt.*;
import java.awt.event.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;
import portfolio.PortfolioItem;
import storage.StorageManager;
public class MainDashboardFrame extends JFrame {
    private final AccountManager accountManager;
    private final User user;
    private final StorageManager storage;
    private final Runnable signOut;
    private final JPanel stage=new JPanel(new CardLayout());
    private final Deque<String> history=new ArrayDeque<>();
    private String current="Home";
    private final Map<String,JButton> nav=new LinkedHashMap<>();
    private final String[] pages= {
        "Home","Classes","Assignments","Goals","SAT","Portfolio","History","Settings"
    }
    ;
    private final VectorIcon.Kind[] kinds= {
        HOME,BOOK,CALENDAR,TARGET,CHART,BRIEFCASE,CLOCK,GEAR
    }
    ;
    public MainDashboardFrame(AccountManager a,User u) {
        this(a,u,null);
    }
    public MainDashboardFrame(AccountManager a,User u,Runnable signOut) {
        super("LifeTracker 1.1.4 • "+u.getName());
        accountManager=a;
        user=u;
        storage=a.getStorageManager();
        this.signOut=signOut;
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(390,700));
        setSize(1180,800);
        setLocationRelativeTo(null);
        build();
    }
    private void build() {
        GradientPanel root=new GradientPanel(new BorderLayout());
        root.setBorder(new EmptyBorder(14,18,10,18));
        root.add(header(),BorderLayout.NORTH);
        stage.setOpaque(false);
        root.add(stage);
        root.add(navbar(),BorderLayout.SOUTH);
        setContentPane(root);
        go("Home",false);
    }
    private JPanel header() {
        JPanel p=new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(0,2,10,2));
        JPanel brand=new JPanel(new FlowLayout(FlowLayout.LEFT,8,0));
        brand.setOpaque(false);
        brand.add(icon(BOOK,30,GREEN));
        JPanel tx=vbox(0);
        JLabel brandTitle=label("LifeTracker",27,Font.BOLD,TEXT);
        HiddenFeatureController.install(brandTitle,this,user);
        tx.add(brandTitle);
        tx.add(label("Track Today. Build Tomorrow.",11,Font.PLAIN,MUTED));
        brand.add(tx);
        p.add(brand,BorderLayout.WEST);
        JPanel r=new JPanel(new FlowLayout(FlowLayout.RIGHT,8,2));
        r.setOpaque(false);
        r.add(label(year(),12,Font.BOLD,MUTED));
        JLabel av=label(initials(),12,Font.BOLD,Color.WHITE);
        av.setOpaque(true);
        av.setBackground(accent());
        av.setBorder(new EmptyBorder(8,9,8,9));
        r.add(av);
        r.add(label(user.getName(),12,Font.BOLD,TEXT));
        p.add(r,BorderLayout.EAST);
        return p;
    }
    private JPanel navbar() {
        RoundPanel b=new RoundPanel(new GridLayout(1,8,2,0),20,new Color(8,22,42));
        b.setBorder(new EmptyBorder(5,5,5,5));
        for(int i=0;i<pages.length;i++) {
            String pg=pages[i];
            JButton x=new JButton(pg,new VectorIcon(kinds[i],19,MUTED));
            x.setHorizontalTextPosition(SwingConstants.CENTER);
            x.setVerticalTextPosition(SwingConstants.BOTTOM);
            x.setIconTextGap(2);
            x.setFont(font(10,Font.PLAIN));
            x.setForeground(MUTED);
            x.setContentAreaFilled(false);
            x.setFocusPainted(false);
            x.setBorder(new EmptyBorder(6,2,6,2));
            x.addActionListener(e->go(pg,true));
            nav.put(pg,x);
            b.add(x);
        }
        return b;
    }
    private void go(String name,boolean push) {
        if(push&&!current.equals(name))history.push(current);
        current=name;
        stage.removeAll();
        JComponent page=switch(name) {
            case"Classes"->classesPage();
            case"Assignments"->assignmentsPage();
            case"Goals"->goalsPage();
            case"SAT"->satPage();
            case"Portfolio"->portfolioPage();
            case"History"->historyPage();
            case"Settings"->settingsPage();
            default->homePage();
        }
        ;
        stage.add(scroll(page));
        stage.revalidate();
        stage.repaint();
        nav.forEach((k,b)-> {
            boolean on=k.equals(name);b.setForeground(on?accent():MUTED);b.setIcon(new VectorIcon(kinds[Arrays.asList(pages).indexOf(k)],19,on?accent():MUTED));
        }
        );
    }
    private void sub(String id,JComponent page) {
        history.push(current);
        current=id;
        stage.removeAll();
        stage.add(scroll(page));
        stage.revalidate();
        stage.repaint();
        nav.forEach((k,b)-> {
            b.setForeground(MUTED);b.setIcon(new VectorIcon(kinds[Arrays.asList(pages).indexOf(k)],19,MUTED));
        }
        );
    }
    private void back() {
        String dest=history.isEmpty()?"Home":history.pop();
        go(dest,false);
    }
    private JScrollPane scroll(JComponent c) {
        JScrollPane s=new JScrollPane(c);
        s.setBorder(null);
        s.setOpaque(false);
        s.getViewport().setOpaque(false);
        s.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        s.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        s.getVerticalScrollBar().setUnitIncrement(18);
        s.addMouseWheelListener(e-> {
            JScrollBar b=s.getVerticalScrollBar();b.setValue(b.getValue()+e.getWheelRotation()*b.getUnitIncrement()*3);
        }
        );
        return s;
    }
    private JPanel base(String title,String sub) {
        JPanel p=vbox(0);
        p.setBorder(new EmptyBorder(8,2,18,2));
        JLabel a=label(title,27,Font.BOLD,TEXT);
        a.setAlignmentX(LEFT_ALIGNMENT);
        p.add(a);
        JLabel b=label(sub,12,Font.PLAIN,MUTED);
        b.setBorder(new EmptyBorder(2,0,14,0));
        b.setAlignmentX(LEFT_ALIGNMENT);
        p.add(b);
        return p;
    }
    private JPanel subBase(String title,String sub) {
        JPanel p=vbox(0);
        p.setBorder(new EmptyBorder(4,2,18,2));
        JButton bk=ghost("←  Back");
        bk.setAlignmentX(LEFT_ALIGNMENT);
        bk.addActionListener(e->back());
        p.add(bk);
        p.add(Box.createVerticalStrut(10));
        JLabel a=label(title,26,Font.BOLD,TEXT);
        a.setAlignmentX(LEFT_ALIGNMENT);
        p.add(a);
        JLabel b=label(sub,12,Font.PLAIN,MUTED);
        b.setBorder(new EmptyBorder(2,0,14,0));
        b.setAlignmentX(LEFT_ALIGNMENT);
        p.add(b);
        return p;
    }
    private JButton ghost(String t) {
        JButton b=new JButton(t);
        b.setForeground(accent());
        b.setFont(font(13,Font.BOLD));
        b.setContentAreaFilled(false);
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(8,0,8,10));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }
    private JLabel icon(VectorIcon.Kind k,int s,Color c) {
        return new JLabel(new VectorIcon(k,s,c));
    }
    private String year() {
        int y=LocalDate.now().getYear(),m=LocalDate.now().getMonthValue(),st=m>=7?y:y-1;
        return st+" – "+String.valueOf(st+1).substring(2);
    }
    private String fullYear() {
        int y=LocalDate.now().getYear(),m=LocalDate.now().getMonthValue(),st=m>=7?y:y-1;
        return st+"-"+(st+1);
    }
    private String initials() {
        String[] x=user.getName().trim().split("\\s+");
        return (x.length>1?""+x[0].charAt(0)+x[x.length-1].charAt(0):""+x[0].charAt(0)).toUpperCase();
    }
    private List<Class> classes() {
        return new ArrayList<>(storage.loadClasses(user.getUsername()));
    }
    private void saveClasses(List<Class> x) {
        storage.saveClasses(user.getUsername(),x);
    }
    private List<PlannerStore.Assignment> assignments() {
        return PlannerStore.loadAssignments(user.getUsername());
    }
    private RoundPanel card(LayoutManager l) {
        RoundPanel p=new RoundPanel(l);
        p.setBorder(new EmptyBorder(15,16,15,16));
        p.setAlignmentX(LEFT_ALIGNMENT);
        return p;
    }
    private JPanel homePage() {
        JPanel p=base("Good "+daypart()+", "+first()+"!","What needs your attention today.");
        List<Class> cs=classes();
        GPACalculator calc=new GPACalculator();
        int graded=0;
        double sum=0;
        for(Class c:cs)if(!c.getGrades().isEmpty()) {
            graded++;
            sum+=calc.calculateClassAverage(c);
        }
        double gpa=calc.calculateCumulativeGPA(cs),avg=graded==0?0:sum/graded;
        List<PlannerStore.Assignment> open=assignments().stream().filter(a->!a.completed()).sorted(Comparator.comparing(PlannerStore.Assignment::due)).toList();
        int sat=PlannerStore.getSatTarget(user.getUsername());
        RoundPanel todo=card(new BorderLayout(0,10));
        JPanel th=new JPanel(new BorderLayout());
        th.setOpaque(false);
        JPanel tt=vbox(0);
        tt.add(label("Today's To-Do",19,Font.BOLD,TEXT));
        tt.add(label(open.size()+" task"+(open.size()==1?"":"s")+" • Keep going!",11,Font.PLAIN,MUTED));
        th.add(tt);
        PillButton addTodo=new PillButton("+ Add To-Do",GREEN);
        addTodo.addActionListener(e->sub("AddAssignment",assignmentForm(null)));
        th.add(addTodo,BorderLayout.EAST);
        todo.add(th,BorderLayout.NORTH);
        JPanel list=vbox(0);
        if(open.isEmpty()) list.add(label("You're caught up. New academic to-dos will appear here.",12,Font.PLAIN,MUTED));
        else {
            int shown=0;
            for(PlannerStore.Assignment a:open) {
                if(shown++>=3)break;
                RoundPanel row=new RoundPanel(new BorderLayout(8,2),16,CARD2);
                row.setBorder(new EmptyBorder(9,11,9,11));
                JPanel tx=vbox(0);
                tx.add(label(a.title(),13,Font.BOLD,TEXT));
                String meta=(a.className().isBlank()?"No class":a.className())+" • "+dueText(a.due());
                tx.add(label(meta,10,Font.PLAIN,a.due().isBefore(LocalDate.now().plusDays(1))?PINK:MUTED));
                row.add(tx);
                PillButton done=new PillButton("Done",GREEN);
                done.addActionListener(e-> {
                    completeAssignment(a);go("Home",false);
                }
                );
                row.add(done,BorderLayout.EAST);
                list.add(row);
                list.add(Box.createVerticalStrut(5));
            }
        }
        if(open.size()>3) {
            JButton all=ghost("View All To-Dos ("+open.size()+") →");
            all.addActionListener(e->go("Assignments",true));
            list.add(all);
        }
        todo.add(list);
        p.add(todo);
        p.add(Box.createVerticalStrut(10));
        RoundPanel stats=card(new GridLayout(1,4,0,0));
        stats.add(infoStat("GPA",graded==0?"--":String.format("%.2f",gpa),PURPLE));
        stats.add(infoStat("AVERAGE",graded==0?"--":String.format("%.1f%%",avg),GREEN));
        stats.add(infoStat("CLASSES",String.valueOf(cs.size()),new Color(62,151,255)));
        stats.add(infoStat("UPCOMING",String.valueOf(open.size()),PINK));
        p.add(stats);
        p.add(Box.createVerticalStrut(10));
        RoundPanel progress=card(new BorderLayout(8,6));
        JPanel pr=vbox(2);
        pr.add(label("Your Progress",16,Font.BOLD,TEXT));
        pr.add(label("SAT Readiness  •  "+(sat==0?"Not set up":"Target "+sat),11,Font.PLAIN,MUTED));
        List<AcademicGoal> goals=storage.loadGoals(user.getUsername());
        pr.add(label("Active Goal  •  "+(goals.isEmpty()?"Not set up":goals.get(0).getGoalName()),11,Font.PLAIN,MUTED));
        progress.add(pr);
        JButton vg=ghost("View Goals →");
        vg.addActionListener(e->go("Goals",true));
        progress.add(vg,BorderLayout.EAST);
        p.add(progress);
        p.add(Box.createVerticalStrut(10));
        RoundPanel quick=card(new GridLayout(1,4,6,0));
        quick.add(compactAction("Add Assignment",BOOK,()->sub("AddAssignment",assignmentForm(null))));
        quick.add(compactAction("Add To-Do",CALENDAR,()->sub("AddAssignment",assignmentForm(null))));
        quick.add(compactAction("Set Goal",TARGET,()->go("Goals",true)));
        quick.add(compactAction("Progress",CHART,()->go("History",true)));
        p.add(quick);
        return p;
    }
    private String dueText(LocalDate d) {
        LocalDate n=LocalDate.now();
        if(d.equals(n))return "Due Today";
        if(d.equals(n.plusDays(1)))return "Due Tomorrow";
        if(d.isBefore(n))return "Overdue • "+d.format(DateTimeFormatter.ofPattern("MMM d"));
        return "Due "+d.format(DateTimeFormatter.ofPattern("MMM d"));
    }
    private JPanel infoStat(String name,String value,Color c) {
        JPanel x=vbox(1);
        x.setBorder(new EmptyBorder(4,8,4,8));
        x.add(label(name,9,Font.BOLD,c));
        x.add(label(value,17,Font.BOLD,TEXT));
        return x;
    }
    private JButton compactAction(String t,VectorIcon.Kind k,Runnable r) {
        JButton b=new JButton("<html><center>"+t.replace(" ","<br>")+"</center></html>",new VectorIcon(k,18,accent()));
        b.setHorizontalTextPosition(SwingConstants.CENTER);
        b.setVerticalTextPosition(SwingConstants.BOTTOM);
        b.setIconTextGap(4);
        b.setForeground(TEXT);
        b.setFont(font(10,Font.BOLD));
        b.setContentAreaFilled(false);
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(6,3,6,3));
        b.addActionListener(e->r.run());
        return b;
    }
    private void completeAssignment(PlannerStore.Assignment a) {
        List<PlannerStore.Assignment> l=assignments();
        for(int i=0;i<l.size();i++)if(l.get(i).id().equals(a.id())) {
            l.set(i,new PlannerStore.Assignment(a.id(),a.title(),a.className(),a.due(),a.points(),true,a.repeat()));
            LocalDate next=PlannerStore.nextDue(a.due(),a.repeat());
            if(next!=null)l.add(new PlannerStore.Assignment(UUID.randomUUID().toString(),a.title(),a.className(),next,a.points(),false,a.repeat()));
            break;
        }
        PlannerStore.saveAssignments(user.getUsername(),l);
    }
    private JPanel quickButton(String t,VectorIcon.Kind k,Runnable r) {
        JPanel p=vbox(0);
        p.setBorder(new EmptyBorder(18,10,18,10));
        JLabel i=icon(k,35,accent());
        i.setAlignmentX(CENTER_ALIGNMENT);
        p.add(i);
        p.add(Box.createVerticalStrut(8));
        JLabel l=label(t,14,Font.BOLD,TEXT);
        l.setAlignmentX(CENTER_ALIGNMENT);
        p.add(l);
        p.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        p.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                r.run();
            }
        }
        );
        return p;
    }
    private JPanel metric(VectorIcon.Kind k,String t,String v,String d,Color c) {
        RoundPanel x=new RoundPanel(new BorderLayout(5,3),18,new Color(c.getRed()/6+18,c.getGreen()/6+20,c.getBlue()/6+28));
        x.setBorder(new EmptyBorder(12,13,12,13));
        JPanel h=new JPanel(new FlowLayout(FlowLayout.LEFT,6,0));
        h.setOpaque(false);
        h.add(icon(k,18,c));
        h.add(label(t,10,Font.BOLD,MUTED));
        x.add(h,BorderLayout.NORTH);
        x.add(label(v,20,Font.BOLD,TEXT));
        x.add(label(d,10,Font.PLAIN,c),BorderLayout.SOUTH);
        return x;
    }
    private String daypart() {
        int h=LocalTime.now().getHour();
        return h<12?"morning":h<18?"afternoon":"evening";
    }
    private String first() {
        return user.getName().trim().split("\\s+")[0];
    }
    private JPanel classesPage() {
        JPanel p=base("Classes","Tap a class to view grades and planning tools.");
        PillButton add=new PillButton("+ Add Class",PURPLE);
        add.setAlignmentX(LEFT_ALIGNMENT);
        add.addActionListener(e->sub("AddClass",classForm()));
        p.add(add);
        p.add(Box.createVerticalStrut(12));
        List<Class> cs=classes();
        if(cs.isEmpty()) {
            p.add(emptyCard(BOOK,"No classes yet","Add your first class to begin tracking grades."));
            return p;
        }
        GPACalculator calc=new GPACalculator();
        for(Class c:cs) {
            RoundPanel row=card(new BorderLayout(12,3));
            JPanel tx=vbox(0);
            tx.add(label(c.getClassName(),16,Font.BOLD,TEXT));
            tx.add(label((c.getTeacherName().isBlank()?"No teacher":c.getTeacherName())+" • "+c.getCourseLevel(),11,Font.PLAIN,MUTED));
            row.add(tx);
            double av=c.getGrades().isEmpty()?0:calc.calculateClassAverage(c);
            row.add(label(c.getGrades().isEmpty()?"--":String.format("%.1f%%  %s",av,calc.letterGrade(av)),15,Font.BOLD,accent()),BorderLayout.EAST);
            row.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            row.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    sub("ClassDetail",classDetail(c));
                }
            }
            );
            p.add(row);
            p.add(Box.createVerticalStrut(8));
        }
        return p;
    }
    private JPanel classForm() {
        JPanel p=subBase("Add Class","Nothing is saved until you tap Save Class.");
        JTextField name=field("Class name"),teacher=field("Teacher"),credits=field("1.0"),semester=field("Fall"),yr=field(fullYear());
        JComboBox<String> level=combo(new String[] {
            Class.REGULAR,Class.HONORS,Class.AP_IB,Class.DUAL_ENROLLMENT
        }
        );
        RoundPanel f=formCard();
        addField(f,"Class Name",name);
        addField(f,"Teacher",teacher);
        addField(f,"Credit Value",credits);
        addField(f,"Course Level",level);
        addField(f,"Semester",semester);
        addField(f,"Academic Year",yr);
        JLabel err=errorLabel();
        f.add(err);
        PillButton save=new PillButton("Save Class",accent());
        save.addActionListener(e-> {
            try {
                Class c=new Class(name.getText(),teacher.getText(),Double.parseDouble(credits.getText().trim()),semester.getText(),yr.getText(),(String)level.getSelectedItem());List<Class> cs=classes();cs.add(c);saveClasses(cs);history.clear();go("Classes",false);
            } catch(Exception ex) {
                err.setText(ex.getMessage()==null?"Check the class information.":ex.getMessage());
            }
        }
        );
        f.add(save);
        p.add(f);
        return p;
    }
    private JPanel classDetail(Class c) {
        GPACalculator calc = new GPACalculator();

        JPanel p = subBase(
            c.getClassName(),
            (c.getTeacherName().isBlank() ? "No teacher" : c.getTeacherName())
                + " • " + c.getCourseLevel()
                + " • " + c.getAcademicYear()
        );

        double av = c.getGrades().isEmpty()
            ? 0
            : calc.calculateClassAverage(c);

        // Mobile-friendly summary: button sits below the grade status.
        RoundPanel summary = card(new BorderLayout(0, 10));

        summary.add(
            label(
                c.getGrades().isEmpty()
                    ? "No grades yet"
                    : String.format("%.2f%% — %s", av, calc.letterGrade(av)),
                22,
                Font.BOLD,
                TEXT
            ),
            BorderLayout.CENTER
        );

        JPanel summaryActions = new JPanel(
            new FlowLayout(FlowLayout.LEFT, 0, 0)
        );
        summaryActions.setOpaque(false);

        PillButton add = new PillButton("+ Add Grade", PURPLE);
        add.addActionListener(e -> sub("AddGrade", gradeForm(c)));
        summaryActions.add(add);

        summary.add(summaryActions, BorderLayout.SOUTH);

        p.add(summary);
        p.add(Box.createVerticalStrut(10));

        PillButton plan = new PillButton(
            "Grade Planning",
            new Color(54, 126, 255)
        );
        plan.setAlignmentX(LEFT_ALIGNMENT);
        plan.addActionListener(
            e -> sub("GradePlanning", gradePlanning(c))
        );

        p.add(plan);
        p.add(Box.createVerticalStrut(12));

        p.add(label("Assignments", 18, Font.BOLD, TEXT));
        p.add(Box.createVerticalStrut(6));

        List<PlannerStore.Assignment> linked = assignments()
            .stream()
            .filter(a -> a.className().equalsIgnoreCase(c.getClassName()))
            .sorted(Comparator.comparing(PlannerStore.Assignment::due))
            .toList();

        if (linked.isEmpty()) {
            p.add(
                emptyCard(
                    CALENDAR,
                    "No assignments for this class",
                    "Academic To-Dos linked to this class will appear here."
                )
            );
        } else {
            for (PlannerStore.Assignment a : linked) {
                boolean graded = hasGrade(c, a.title());

                // Mobile-friendly assignment card.
                RoundPanel r = card(new BorderLayout(0, 8));

                JPanel t = vbox(0);
                t.add(label(a.title(), 14, Font.BOLD, TEXT));

                String status = graded
                    ? "Graded"
                    : (a.completed()
                        ? "Work complete — awaiting grade"
                        : "To-Do");

                t.add(
                    label(
                        a.due().format(
                            DateTimeFormatter.ofPattern("MMM d")
                        ) + " • " + status,
                        11,
                        Font.PLAIN,
                        graded ? GREEN : MUTED
                    )
                );

                r.add(t, BorderLayout.CENTER);

                if (a.completed() && !graded) {
                    JPanel actions = new JPanel(
                        new FlowLayout(FlowLayout.LEFT, 0, 0)
                    );
                    actions.setOpaque(false);

                    PillButton grade = new PillButton(
                        "Add Grade",
                        PURPLE
                    );

                    grade.addActionListener(
                        e -> sub(
                            "GradeAssignment",
                            gradeForm(c, null, a)
                        )
                    );

                    actions.add(grade);
                    r.add(actions, BorderLayout.SOUTH);
                }

                r.setMaximumSize(
                    new Dimension(
                        Integer.MAX_VALUE,
                        r.getPreferredSize().height
                    )
                );
                r.setAlignmentX(LEFT_ALIGNMENT);

                p.add(r);
                p.add(Box.createVerticalStrut(6));
            }
        }

        p.add(Box.createVerticalStrut(10));
        p.add(label("Grades", 18, Font.BOLD, TEXT));
        p.add(Box.createVerticalStrut(6));

        if (c.getGrades().isEmpty()) {
            p.add(
                emptyCard(
                    CHART,
                    "No grades yet",
                    "Complete an assignment, then add its grade when your teacher returns it."
                )
            );
        } else {
            for (Grade g : c.getGrades()) {
                RoundPanel r = card(new BorderLayout());

                JPanel t = vbox(0);
                t.add(
                    label(
                        g.getAssignmentName(),
                        15,
                        Font.BOLD,
                        TEXT
                    )
                );
                t.add(
                    label(
                        g.getCategory()
                            + " • "
                            + g.getScore()
                            + " / "
                            + g.getMaxScore(),
                        11,
                        Font.PLAIN,
                        MUTED
                    )
                );

                r.add(t);

                r.add(
                    label(
                        String.format("%.1f%%", g.getPercentage()),
                        15,
                        Font.BOLD,
                        accent()
                    ),
                    BorderLayout.EAST
                );

                r.setCursor(
                    Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                );

                r.addMouseListener(new MouseAdapter() {
                    public void mouseClicked(MouseEvent e) {
                        sub("EditGrade", gradeForm(c, g));
                    }
                });

                p.add(r);
                p.add(Box.createVerticalStrut(7));
            }
        }

        return p;
    }
    private boolean hasGrade(Class c,String assignmentTitle) {
        return c.getGrades().stream().anyMatch(g->g.getAssignmentName().equalsIgnoreCase(assignmentTitle));
    }
    private JPanel gradeForm(Class c) {
        return gradeForm(c,null,null);
    }
    private JPanel gradeForm(Class c,Grade old) {
        return gradeForm(c,old,null);
    }
    private JPanel gradeForm(Class c,Grade old,PlannerStore.Assignment linked) {
        JPanel p=subBase(old==null?"Add Grade":"Edit Grade","Back cancels changes. Save applies them.");
        JTextField assignment=field(old!=null?old.getAssignmentName():(linked!=null?linked.title():"")),score=field(old==null?"":String.valueOf(old.getScore())),max=field(old==null?(linked!=null&&!linked.points().isBlank()?linked.points():"100"):String.valueOf(old.getMaxScore())),weight=field(old==null?"0":String.valueOf(old.getWeight()));
        JComboBox<String> cat=combo(new String[] {
            "Assignments","Homework","Quiz","Test","Project","Exam","Participation","Other"
        }
        );
        if(old!=null)cat.setSelectedItem(old.getCategory());
        RoundPanel f=formCard();
        addField(f,"Assignment",assignment);
        addField(f,"Category",cat);
        addField(f,"Score Earned",score);
        addField(f,"Maximum Score",max);
        addField(f,"Weight % (0 = equal)",weight);
        JLabel err=errorLabel();
        f.add(err);
        PillButton save=new PillButton(old==null?"Save Grade":"Save Changes",accent());
        save.addActionListener(e-> {
            try {
                Grade ng=new Grade(assignment.getText(),Double.parseDouble(score.getText()),Double.parseDouble(max.getText()),Double.parseDouble(weight.getText()),String.valueOf(cat.getSelectedItem()));List<Class> cs=classes();Class target=findClass(cs,c);if(target==null)throw new Exception("Class could not be found.");if(old!=null) {
                    int idx=indexGrade(target,old);if(idx>=0)target.getGrades().set(idx,ng);else throw new Exception("Grade could not be found.");
                } else target.addGrade(ng);saveClasses(cs);history.clear();go("Classes",false);
            } catch(Exception ex) {
                err.setText(ex.getMessage()==null?"Check the grade values.":ex.getMessage());
            }
        }
        );
        f.add(save);
        p.add(f);
        return p;
    }
    private Class findClass(List<Class> cs,Class c) {
        for(Class x:cs)if(x.getClassName().equals(c.getClassName())&&x.getAcademicYear().equals(c.getAcademicYear()))return x;
        return null;
    }
    private int indexGrade(Class c,Grade g) {
        for(int i=0;i<c.getGrades().size();i++) {
            Grade x=c.getGrades().get(i);
            if(x.getAssignmentName().equals(g.getAssignmentName())&&x.getScore()==g.getScore()&&x.getMaxScore()==g.getMaxScore())return i;
        }
        return -1;
    }
    private JPanel gradePlanning(Class c) {
        JPanel p=subBase("Grade Planning",c.getClassName()+" • Try scenarios without changing saved grades.");
        JTextField target=field("90"),remaining=field("1");
        RoundPanel f=formCard();
        addField(f,"Target Average",target);
        addField(f,"Remaining Assignments",remaining);
        JLabel result=label("Enter a target to explore what you need next.",13,Font.PLAIN,MUTED);
        result.setBorder(new EmptyBorder(12,0,12,0));
        f.add(result);
        PillButton calcBtn=new PillButton("Calculate",accent());
        calcBtn.addActionListener(e-> {
            try {
                double t=Double.parseDouble(target.getText());int n=Integer.parseInt(remaining.getText());double current=c.getGrades().isEmpty()?0:new GPACalculator().calculateClassAverage(c);int done=c.getGrades().size();double needed=(t*(done+n)-current*done)/Math.max(1,n);result.setText(String.format("Average needed on the next %d assignment(s): %.1f%%",n,needed));
            } catch(Exception ex) {
                result.setText("Enter valid numbers.");
            }
        }
        );
        f.add(calcBtn);
        p.add(f);
        return p;
    }
    private JPanel assignmentsPage() {
        JPanel p=base("Assignments","Upcoming, overdue, completed, and graded work.");
        PillButton add=new PillButton("+ Add Assignment",PURPLE);
        add.setAlignmentX(LEFT_ALIGNMENT);
        add.addActionListener(e->sub("AddAssignment",assignmentForm(null)));
        p.add(add);
        p.add(Box.createVerticalStrut(12));
        List<PlannerStore.Assignment> as=assignments();
        if(as.isEmpty()) {
            p.add(emptyCard(CALENDAR,"No assignments yet","Add assignments as teachers give them to you."));
            return p;
        }
        for(PlannerStore.Assignment a:as) {
            Class linkedClass=findClassByName(a.className());
            boolean graded=linkedClass!=null&&hasGrade(linkedClass,a.title());
            RoundPanel r=card(new BorderLayout(0,8));
            JPanel t=vbox(0);
            t.add(label(a.title(),15,Font.BOLD,a.completed()?MUTED:TEXT));
            String status=graded?" • Graded":(a.completed()?" • Work complete — awaiting grade":"");
            String repeat="None".equals(a.repeat())?"":" • "+a.repeat();
            t.add(label((a.className().isBlank()?"No class":a.className())+" • "+a.due().format(DateTimeFormatter.ofPattern("MMM d"))+repeat+status,11,Font.PLAIN,graded?GREEN:MUTED));
            r.add(t,BorderLayout.CENTER);
            JPanel acts=new JPanel(new FlowLayout(FlowLayout.LEFT,5,0));
            acts.setOpaque(false);
            if(!graded) {
                PillButton done=new PillButton(a.completed()?"Undo":"Done",GREEN);
                done.addActionListener(e-> {
                    if(a.completed()) {
                        List<PlannerStore.Assignment> l=assignments();
                        for(int i=0;i<l.size();i++) {
                            if(l.get(i).id().equals(a.id())) {
                                l.set(i,new PlannerStore.Assignment(a.id(),a.title(),a.className(),a.due(),a.points(),false,a.repeat()));
                            }
                        }
                        PlannerStore.saveAssignments(user.getUsername(),l);
                    } else {
                        completeAssignment(a);
                    }
                    go("Assignments",false);
                }
                );
                acts.add(done);
            }
            if(a.completed()&&!graded&&linkedClass!=null) {
                PillButton grade=new PillButton("Add Grade",PURPLE);
                grade.addActionListener(e->sub("GradeAssignment",gradeForm(linkedClass,null,a)));
                acts.add(grade);
            }
            PillButton edit=new PillButton("Edit",new Color(54,126,255));
            edit.addActionListener(e->sub("EditAssignment",assignmentForm(a)));
            acts.add(edit);
            r.add(acts,BorderLayout.SOUTH);
            r.setMaximumSize(new Dimension(Integer.MAX_VALUE,r.getPreferredSize().height));
            r.setAlignmentX(LEFT_ALIGNMENT);
            p.add(r);
            p.add(Box.createVerticalStrut(7));
        }
        return p;
    }
    private Class findClassByName(String name) {
        if(name==null||name.isBlank())return null;
        for(Class c:classes())if(c.getClassName().equalsIgnoreCase(name))return c;
        return null;
    }
    private JPanel assignmentForm(PlannerStore.Assignment old) {
        JPanel p = subBase(
            old == null ? "Add Assignment / To-Do" : "Edit Assignment",
            old == null
                ? "Link it to a class now, or keep it personal."
                : "Update the assignment details below."
        );

        JTextField title = field(
            old == null ? "" : old.title()
        );
        JTextField date = field(
            old == null
                ? LocalDate.now().plusDays(1).toString()
                : old.due().toString()
        );
        JTextField pts = field(
            old == null ? "" : old.points()
        );

        List<Class> cs = classes();

        String[] classNames = new String[cs.size() + 1];
        classNames[0] = "Personal / No Class";

        for (int i = 0; i < cs.size(); i++) {
            classNames[i + 1] = cs.get(i).getClassName();
        }

        JComboBox<String> cls = combo(classNames);

        if (old != null && !old.className().isBlank()) {
            cls.setSelectedItem(old.className());
        }

        JComboBox<String> repeat = combo(
            new String[] {
                "None",
                "Daily",
                "Weekly",
                "Monthly"
            }
        );

        if (old != null) {
            repeat.setSelectedItem(old.repeat());
        }

        // Keep this form responsive without changing gradeForm().
        title.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        date.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        pts.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        cls.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        repeat.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        RoundPanel f = formCard();
        f.setAlignmentX(LEFT_ALIGNMENT);

        addField(f, "Assignment / To-Do", title);
        addField(f, "Class", cls);
        addField(f, "Due Date (YYYY-MM-DD)", date);
        addField(f, "Points (optional)", pts);
        addField(f, "Repeat", repeat);

        JLabel err = errorLabel();
        f.add(err);

        PillButton save = new PillButton(
            old == null ? "Save Assignment" : "Save Changes",
            accent()
        );

        save.addActionListener(e -> {
            try {
                if (title.getText().trim().isEmpty()) {
                    throw new Exception("Assignment name is required.");
                }

                LocalDate d = LocalDate.parse(
                    date.getText().trim()
                );

                String selected = String.valueOf(
                    cls.getSelectedItem()
                );

                String className =
                    "Personal / No Class".equals(selected)
                        ? ""
                        : selected;

                List<PlannerStore.Assignment> l = assignments();

                if (old != null) {
                    l.removeIf(x -> x.id().equals(old.id()));
                }

                l.add(
                    new PlannerStore.Assignment(
                        old == null
                            ? UUID.randomUUID().toString()
                            : old.id(),
                        title.getText().trim(),
                        className,
                        d,
                        pts.getText().trim(),
                        old != null && old.completed(),
                        String.valueOf(repeat.getSelectedItem())
                    )
                );

                PlannerStore.saveAssignments(
                    user.getUsername(),
                    l
                );

                history.clear();
                go("Assignments", false);

            } catch (Exception ex) {
                err.setText(
                    ex.getMessage() == null
                        ? "Enter a name and a valid date like 2026-09-25."
                        : ex.getMessage()
                );
            }
        });

        f.add(save);
        p.add(f);

        // Lets the Save button scroll fully above the fixed bottom nav.
        p.add(Box.createVerticalStrut(90));

        return p;
    }
    private JPanel goalsPage() {
        JPanel p=base("Goals","Academic and personal progress in one place.");
        PillButton add=new PillButton("+ Add Goal",PURPLE);
        add.setAlignmentX(LEFT_ALIGNMENT);
        add.addActionListener(e->sub("AddGoal",goalForm()));
        p.add(add);
        p.add(Box.createVerticalStrut(12));
        List<AcademicGoal> gs=storage.loadGoals(user.getUsername());
        if(gs.isEmpty()) {
            p.add(emptyCard(TARGET,"No goals yet","Create a goal and track your progress."));
            return p;
        }
        for(AcademicGoal g:gs) {
            RoundPanel r=card(new BorderLayout());
            JPanel t=vbox(0);
            t.add(label(g.getGoalName(),15,Font.BOLD,TEXT));
            t.add(label("Target GPA: "+g.getTargetGpa()+" • "+g.getDeadline(),11,Font.PLAIN,MUTED));
            r.add(t);
            p.add(r);
            p.add(Box.createVerticalStrut(7));
        }
        return p;
    }
    private JPanel goalForm() {
        JPanel p=subBase("Add Goal","Back cancels changes. Save creates the goal.");
        JTextField name=field(""),gpa=field("3.5"),deadline=field("");
        RoundPanel f=formCard();
        addField(f,"Goal Name",name);
        addField(f,"Target GPA",gpa);
        addField(f,"Deadline",deadline);
        JLabel err=errorLabel();
        f.add(err);
        PillButton save=new PillButton("Save Goal",accent());
        save.addActionListener(e-> {
            try {
                double v=Double.parseDouble(gpa.getText());if(name.getText().isBlank())throw new Exception("Goal name is required.");List<AcademicGoal> l=new ArrayList<>(storage.loadGoals(user.getUsername()));l.add(new AcademicGoal(name.getText().trim(),v,deadline.getText().trim()));storage.saveGoals(user.getUsername(),l);history.clear();go("Goals",false);
            } catch(Exception ex) {
                err.setText(ex.getMessage()==null?"Check the goal information.":ex.getMessage());
            }
        }
        );
        f.add(save);
        p.add(f);
        return p;
    }
    private JPanel satPage() {
        JPanel p=base("SAT Readiness","Track your target, practice scores, and test date.");
        int t=PlannerStore.getSatTarget(user.getUsername());
        if(t==0) {
            p.add(emptyCard(TARGET,"SAT readiness is not set up yet","Set a target when you're ready to begin."));
            p.add(Box.createVerticalStrut(10));
            PillButton b=new PillButton("Set Up SAT",PURPLE);
            b.setAlignmentX(LEFT_ALIGNMENT);
            b.addActionListener(e->sub("SATSetup",satForm()));
            p.add(b);
            return p;
        }
        RoundPanel c=card(new BorderLayout());
        JPanel x=vbox(0);
        x.add(label("Target: "+t,22,Font.BOLD,TEXT));
        x.add(label("Practice: "+value(PlannerStore.getSatCurrent(user.getUsername()))+" • Math: "+value(PlannerStore.getSatMath(user.getUsername()))+" • Reading & Writing: "+value(PlannerStore.getSatRW(user.getUsername())),12,Font.PLAIN,MUTED));
        x.add(label("Test date / note: "+PlannerStore.getSatDate(user.getUsername()),11,Font.PLAIN,MUTED));
        c.add(x);
        PillButton e=new PillButton("Edit",accent());
        e.addActionListener(ev->sub("SATSetup",satForm()));
        c.add(e,BorderLayout.EAST);
        p.add(c);
        return p;
    }
    private String value(int n) {
        return n==0?"--":String.valueOf(n);
    }
    private JPanel satForm() {
        JPanel p=subBase("SAT Readiness Setup","Back cancels changes. Save applies them.");
        JTextField target=field(value0(PlannerStore.getSatTarget(user.getUsername()))),cur=field(value0(PlannerStore.getSatCurrent(user.getUsername()))),math=field(value0(PlannerStore.getSatMath(user.getUsername()))),rw=field(value0(PlannerStore.getSatRW(user.getUsername()))),date=field(PlannerStore.getSatDate(user.getUsername()));
        RoundPanel f=formCard();
        addField(f,"Target Score",target);
        addField(f,"Current Practice Score",cur);
        addField(f,"Math",math);
        addField(f,"Reading & Writing",rw);
        addField(f,"Test Date / Note",date);
        JLabel err=errorLabel();
        f.add(err);
        PillButton save=new PillButton("Save SAT Readiness",accent());
        save.addActionListener(e-> {
            try {
                int t=parse(target),c=parse(cur),m=parse(math),r=parse(rw);if(t<400||t>1600)throw new Exception();PlannerStore.setSatTarget(user.getUsername(),t);PlannerStore.setSatCurrent(user.getUsername(),c);PlannerStore.setSatMath(user.getUsername(),m);PlannerStore.setSatRW(user.getUsername(),r);PlannerStore.setSatDate(user.getUsername(),date.getText().trim());history.clear();go("SAT",false);
            } catch(Exception ex) {
                err.setText("Target must be 400–1600. Other scores can be blank.");
            }
        }
        );
        f.add(save);
        p.add(f);
        return p;
    }
    private int parse(JTextField f) {
        return f.getText().trim().isEmpty()?0:Integer.parseInt(f.getText().trim());
    }
    private String value0(int n) {
        return n==0?"":String.valueOf(n);
    }
    private JPanel portfolioPage() {
        JPanel p=base("Portfolio","Projects, awards, certifications, activities, and skills.");
        PillButton add=new PillButton("+ Add Item",PURPLE);
        add.setAlignmentX(LEFT_ALIGNMENT);
        add.addActionListener(e->sub("AddPortfolio",portfolioForm()));
        p.add(add);
        p.add(Box.createVerticalStrut(12));
        List<PortfolioItem> l=storage.loadPortfolio(user.getUsername());
        if(l.isEmpty()) {
            p.add(emptyCard(BRIEFCASE,"Your portfolio is empty","Add work and achievements as you earn them."));
            return p;
        }
        for(PortfolioItem it:l) {
            RoundPanel r=card(new BorderLayout());
            JPanel t=vbox(0);
            t.add(label(it.getTitle(),15,Font.BOLD,TEXT));
            t.add(label((it.getClassName().isBlank()?"Portfolio":it.getClassName())+(it.getAcademicYear().isBlank()?"":" • "+it.getAcademicYear()),11,Font.PLAIN,MUTED));
            r.add(t);
            p.add(r);
            p.add(Box.createVerticalStrut(7));
        }
        return p;
    }
    private JPanel portfolioForm() {
        JPanel p=subBase("Add Portfolio Item","Back cancels changes. Save adds the item.");
        JTextField title=field(""),desc=field(""),cls=field(""),yr=field(fullYear()),grade=field(""),skills=field("");
        JCheckBox best=check("Best Work"),degree=check("Degree Focused");
        RoundPanel f=formCard();
        addField(f,"Title",title);
        addField(f,"Description",desc);
        addField(f,"Class",cls);
        addField(f,"Academic Year",yr);
        addField(f,"Grade",grade);
        addField(f,"Skills",skills);
        f.add(best);
        f.add(degree);
        JLabel err=errorLabel();
        f.add(err);
        PillButton save=new PillButton("Save Portfolio Item",accent());
        save.addActionListener(e-> {
            try {
                List<PortfolioItem> l=new ArrayList<>(storage.loadPortfolio(user.getUsername()));l.add(new PortfolioItem(title.getText(),desc.getText(),cls.getText(),yr.getText(),grade.getText(),skills.getText(),"","","",best.isSelected(),degree.isSelected()));storage.savePortfolio(user.getUsername(),l);history.clear();go("Portfolio",false);
            } catch(Exception ex) {
                err.setText(ex.getMessage());
            }
        }
        );
        f.add(save);
        p.add(f);
        return p;
    }
    private JPanel historyPage() {
        JPanel p=base("History","Your academic progress across school years.");
        List<Class> cs=classes();
        if(cs.isEmpty()) {
            p.add(emptyCard(CLOCK,"No academic history yet","Classes and grades build your history automatically."));
            return p;
        }
        Map<String,List<Class>> by=new TreeMap<>(Comparator.reverseOrder());
        for(Class c:cs)by.computeIfAbsent(c.getAcademicYear(),k->new ArrayList<>()).add(c);
        GPACalculator calc=new GPACalculator();
        for(var e:by.entrySet()) {
            RoundPanel sec=card(new BorderLayout());
            JPanel b=vbox(0);
            b.add(label(e.getKey(),18,Font.BOLD,TEXT));
            for(Class c:e.getValue()) {
                double av=c.getGrades().isEmpty()?0:calc.calculateClassAverage(c);
                b.add(label(c.getClassName()+"   "+(c.getGrades().isEmpty()?"No grades":String.format("%.1f%% %s",av,calc.letterGrade(av))),13,Font.PLAIN,MUTED));
            }
            sec.add(b);
            p.add(sec);
            p.add(Box.createVerticalStrut(8));
        }
        return p;
    }
    private JPanel settingsPage() {
        JPanel p=base("Settings","Appearance and accessibility designed for you.");
        RoundPanel a=card(new GridLayout(0,1,8,8));
        a.add(label("Appearance",18,Font.BOLD,TEXT));
        JComboBox<String> appearance=combo(new String[] {
            "Dark","Light","System"
        }
        );
        appearance.setSelectedItem(PreferencesManager.getAppearance());
        addField(a,"Theme",appearance);
        JComboBox<String> accentBox=combo(new String[] {
            "Purple","Blue","Green","Pink","Orange","Teal","Red"
        }
        );
        accentBox.setSelectedItem(PreferencesManager.getAccentColor());
        addField(a,"Accent Color",accentBox);
        a.add(label("Accessibility",18,Font.BOLD,TEXT));
        String[][] opts= {
            {
                "Color Vision Assistance","colorVision"
            }
            , {
                "High Contrast","highContrast"
            }
            , {
                "Dyslexia-Friendly Readability","dyslexia"
            }
            , {
                "Larger Text","largeText"
            }
            , {
                "Increase Line Spacing","lineSpacing"
            }
            , {
                "Simplified Layout","simpleLayout"
            }
            , {
                "Reduce Motion","reduceMotion"
            }
            , {
                "Larger Touch Targets","touchTargets"
            }
        }
        ;
        java.util.Map<String,JCheckBox> boxes=new java.util.LinkedHashMap<>();
        for(String[] o:opts) {
            JCheckBox c=check(o[0]);
            c.setSelected(PreferencesManager.getFlag(o[1]));
            boxes.put(o[1],c);
            a.add(c);
        }
        PillButton apply=new PillButton("Apply Settings",accent());
        apply.addActionListener(e-> {
            PreferencesManager.setAppearance(String.valueOf(appearance.getSelectedItem())); PreferencesManager.setAccentColor(String.valueOf(accentBox.getSelectedItem()));
            boxes.forEach((k,c)->PreferencesManager.setFlag(k,c.isSelected())); PreferencesManager.setLargeText(boxes.get("largeText").isSelected());
            ThemeManager.applySavedTheme(); ThemeManager.refreshAllWindows(); Window w=SwingUtilities.getWindowAncestor(p); if(w instanceof LoginFrame lf) {
                lf.showDashboard(user);
            } else {
                build();revalidate();repaint();go("Settings",false);
            }
        }
        );
        a.add(apply);
        if(signOut!=null) {
            PillButton out=new PillButton("Sign Out",new Color(180,65,75));
            out.addActionListener(e->signOut.run());
            a.add(out);
        }
        p.add(a);
        return p;
    }
    private RoundPanel emptyCard(VectorIcon.Kind k, String title, String sub) {
        RoundPanel c = card(new BorderLayout());

        JPanel b = vbox(0);
        b.setBorder(new EmptyBorder(28, 12, 28, 12));

        JLabel i = icon(k, 42, new Color(104, 128, 164));
        i.setAlignmentX(CENTER_ALIGNMENT);
        b.add(i);
        b.add(Box.createVerticalStrut(10));

        JLabel t = label(title, 16, Font.BOLD, TEXT);
        t.setAlignmentX(CENTER_ALIGNMENT);
        b.add(t);

        // HTML lets the explanatory copy wrap on phone-width windows.
        JLabel s = label(
            "<html><div style='text-align:center; width:280px;'>"
                + sub
                + "</div></html>",
            12,
            Font.PLAIN,
            MUTED
        );
        s.setAlignmentX(CENTER_ALIGNMENT);
        s.setHorizontalAlignment(SwingConstants.CENTER);
        b.add(s);

        c.add(b, BorderLayout.CENTER);
        c.setMaximumSize(
            new Dimension(
                Integer.MAX_VALUE,
                c.getPreferredSize().height
            )
        );
        c.setAlignmentX(LEFT_ALIGNMENT);

        return c;
    }
    private RoundPanel formCard() {
        RoundPanel p=card(new GridLayout(0,1,6,6));
        return p;
    }
    private JTextField field(String text) {
        JTextField f=new JTextField(text);
        f.setFont(font(14,Font.PLAIN));
        f.setForeground(TEXT);
        f.setCaretColor(TEXT);
        f.setBackground(CARD2);
        f.setBorder(new CompoundBorder(new LineBorder(accent(),1,true),new EmptyBorder(10,12,10,12)));
        f.setPreferredSize(new Dimension(250,44));
        return f;
    }
    private <T> JComboBox<T> combo(T[] vals) {
        JComboBox<T> c=new JComboBox<>(vals);
        c.setFont(font(14,Font.PLAIN));
        c.setForeground(TEXT);
        c.setBackground(CARD2);
        c.setPreferredSize(new Dimension(250,44));
        c.setRenderer(new DefaultListCellRenderer() {
            public Component getListCellRendererComponent(JList<?> l,Object v,int i,boolean sel,boolean focus) {
                JLabel x=(JLabel)super.getListCellRendererComponent(l,v,i,sel,focus);x.setOpaque(true);x.setForeground(sel?Color.WHITE:TEXT);x.setBackground(sel?accent():CARD2);x.setBorder(new EmptyBorder(7,10,7,10));return x;
            }
        }
        );
        return c;
    }
    private JCheckBox check(String t) {
        JCheckBox c=new JCheckBox(t);
        c.setOpaque(false);
        c.setForeground(TEXT);
        c.setFont(font(13,Font.PLAIN));
        c.setFocusPainted(false);
        c.setBorder(new EmptyBorder(7,0,7,0));
        return c;
    }
    private void addField(JPanel p,String name,JComponent f) {
        JLabel l=label(name,12,Font.BOLD,MUTED);
        l.setBorder(new EmptyBorder(5,0,0,0));
        p.add(l);
        p.add(f);
    }
    private JLabel errorLabel() {
        JLabel l=label(" ",12,Font.BOLD,PINK);
        l.setBorder(new EmptyBorder(5,0,5,0));
        return l;
    }
}
