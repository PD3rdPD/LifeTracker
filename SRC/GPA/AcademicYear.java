package GPA;

public class AcademicYear {

    private String yearName;
    private int gradeLevel;

    public AcademicYear(String yearName, int gradeLevel) {
        this.yearName = yearName;
        this.gradeLevel = gradeLevel;
    }

    public String getYearName() {
        return yearName;
    }

    public int getGradeLevel() {
        return gradeLevel;
    }
}