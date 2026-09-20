package grades;

import java.util.ArrayList;

public class GradeManager {

    private ArrayList<Grade> grades;

    public GradeManager() {
        grades = new ArrayList<>();
    }

    public void addGrade(Grade grade) {
        grades.add(grade);
    }

    public ArrayList<Grade> getGrades() {
        return grades;
    }

    public double getAverage() {
        if (grades.isEmpty()) {
            return 0.0;
        }

        double total = 0.0;

        for (Grade grade : grades) {
            total += grade.getPercentage();
        }

        return total / grades.size();
    }
}