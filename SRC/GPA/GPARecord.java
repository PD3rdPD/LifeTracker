package GPA;

public class GPARecord {

    private double gpa;
    private String academicYear;
    private String semester;

    public GPARecord(double gpa, String academicYear, String semester) {
        this.gpa = gpa;
        this.academicYear = academicYear;
        this.semester = semester;
    }

    public double getGpa() {
        return gpa;
    }

    public String getAcademicYear() {
        return academicYear;
    }

    public String getSemester() {
        return semester;
    }
}