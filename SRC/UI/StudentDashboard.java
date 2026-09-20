package UI;

import account.User;
import classes.Class;
import GPA.GPACalculator;
import java.util.ArrayList;

public class StudentDashboard {

    private User user;
    private ArrayList<Class> classes;
    private GPACalculator gpaCalculator;

    public StudentDashboard(User user) {
        this.user = user;
        this.classes = new ArrayList<>();
        this.gpaCalculator = new GPACalculator();
    }

    public void addClass(Class classData) {
        classes.add(classData);
    }

    public User getUser() {
        return user;
    }

    public ArrayList<Class> getClasses() {
        return classes;
    }

    public GPACalculator getGpaCalculator() {
        return gpaCalculator;
    }

    public void displayStudentInfo() {
        System.out.println("Student: " + user.getName());
        System.out.println("Education Level: " + user.getEducationLevel());
        System.out.println("Classes: " + classes.size());
    }
}