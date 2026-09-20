package classes;

import grades.Grade;
import java.util.ArrayList;

public class Class {
    public static final String REGULAR = "Regular";
    public static final String HONORS = "Honors";
    public static final String AP_IB = "AP / IB";
    public static final String DUAL_ENROLLMENT = "Dual Enrollment";

    private final String className;
    private final String teacherName;
    private final double creditHours;
    private final String semester;
    private final String academicYear;
    private final String courseLevel;
    private final ArrayList<Grade> grades;

    public Class(String className, String teacherName) {
        this(className, teacherName, 1.0, "Current", "Current", REGULAR);
    }

    public Class(String className, String teacherName, double creditHours,
                 String semester, String academicYear) {
        this(className, teacherName, creditHours, semester, academicYear, REGULAR);
    }

    public Class(String className, String teacherName, double creditHours,
                 String semester, String academicYear, String courseLevel) {
        if (className == null || className.trim().isEmpty()) {
            throw new IllegalArgumentException("Class name is required.");
        }
        if (creditHours <= 0) {
            throw new IllegalArgumentException("Credit value must be greater than zero.");
        }
        this.className = className.trim();
        this.teacherName = teacherName == null ? "" : teacherName.trim();
        this.creditHours = creditHours;
        this.semester = semester == null || semester.trim().isEmpty() ? "Current" : semester.trim();
        this.academicYear = academicYear == null || academicYear.trim().isEmpty() ? "Current" : academicYear.trim();
        this.courseLevel = normalizeCourseLevel(courseLevel);
        this.grades = new ArrayList<>();
    }

    private static String normalizeCourseLevel(String level) {
        if (level == null || level.isBlank()) return REGULAR;
        String value = level.trim();
        if (value.equalsIgnoreCase(HONORS)) return HONORS;
        if (value.equalsIgnoreCase(AP_IB) || value.equalsIgnoreCase("AP") || value.equalsIgnoreCase("IB")) return AP_IB;
        if (value.equalsIgnoreCase(DUAL_ENROLLMENT) || value.equalsIgnoreCase("Dual")) return DUAL_ENROLLMENT;
        return REGULAR;
    }

    public void addGrade(Grade grade) { grades.add(grade); }
    public String getClassName() { return className; }
    public String getTeacherName() { return teacherName; }
    public double getCreditHours() { return creditHours; }
    public String getSemester() { return semester; }
    public String getAcademicYear() { return academicYear; }
    public String getCourseLevel() { return courseLevel; }
    public ArrayList<Grade> getGrades() { return grades; }

    @Override
    public String toString() { return className + " (" + courseLevel + ")"; }
}
