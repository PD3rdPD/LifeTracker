package GPA;

import classes.Class;
import grades.Grade;
import java.util.ArrayList;
import java.util.List;

public class GPACalculator {

    public double calculateClassAverage(Class classData) {
        ArrayList<Grade> grades = classData.getGrades();
        if (grades.isEmpty()) return 0.0;

        double weightTotal = 0.0;
        double weightedTotal = 0.0;
        for (Grade grade : grades) {
            if (grade.getWeight() > 0) {
                weightTotal += grade.getWeight();
                weightedTotal += grade.getPercentage() * grade.getWeight();
            }
        }

        if (weightTotal > 0) {
            return weightedTotal / weightTotal;
        }

        double total = 0.0;
        for (Grade grade : grades) total += grade.getPercentage();
        return total / grades.size();
    }

    public String letterGrade(double percentage) {
        if (percentage >= 90) return "A";
        if (percentage >= 80) return "B";
        if (percentage >= 70) return "C";
        if (percentage >= 60) return "D";
        return "F";
    }

    /** Standard unweighted 4.0 scale. */
    public double percentageToGPA(double percentage) {
        if (percentage >= 93) return 4.0;
        if (percentage >= 90) return 3.7;
        if (percentage >= 87) return 3.3;
        if (percentage >= 83) return 3.0;
        if (percentage >= 80) return 2.7;
        if (percentage >= 77) return 2.3;
        if (percentage >= 73) return 2.0;
        if (percentage >= 70) return 1.7;
        if (percentage >= 67) return 1.3;
        if (percentage >= 60) return 1.0;
        return 0.0;
    }

    public double weightedClassGPA(Class classData, double honorsBonus,
                                   double apIbBonus, double dualBonus, double weightedCap) {
        double base = percentageToGPA(calculateClassAverage(classData));
        double bonus = switch (classData.getCourseLevel()) {
            case Class.HONORS -> honorsBonus;
            case Class.AP_IB -> apIbBonus;
            case Class.DUAL_ENROLLMENT -> dualBonus;
            default -> 0.0;
        };
        return capGPA(base + bonus, weightedCap);
    }

    public double calculateSemesterGPA(List<Class> classes, String semester, String academicYear) {
        return calculateUnweighted(classes, semester, academicYear);
    }

    public double calculateCumulativeGPA(List<Class> classes) {
        return calculateUnweighted(classes, null, null);
    }

    public double calculateWeightedSemesterGPA(List<Class> classes, String semester, String academicYear,
                                               double honorsBonus, double apIbBonus,
                                               double dualBonus, double weightedCap) {
        return calculateWeighted(classes, semester, academicYear,
                honorsBonus, apIbBonus, dualBonus, weightedCap);
    }

    public double calculateWeightedCumulativeGPA(List<Class> classes,
                                                 double honorsBonus, double apIbBonus,
                                                 double dualBonus, double weightedCap) {
        return calculateWeighted(classes, null, null,
                honorsBonus, apIbBonus, dualBonus, weightedCap);
    }

    public double calculateYearAverage(List<Class> classes, String academicYear) {
        double total = 0.0;
        int count = 0;
        for (Class classData : classes) {
            if (!classData.getAcademicYear().equalsIgnoreCase(academicYear) || classData.getGrades().isEmpty()) continue;
            total += calculateClassAverage(classData);
            count++;
        }
        return count == 0 ? 0.0 : total / count;
    }

    private double calculateUnweighted(List<Class> classes, String semester, String academicYear) {
        double qualityPoints = 0.0;
        double credits = 0.0;
        for (Class classData : classes) {
            if (!matches(classData, semester, academicYear) || classData.getGrades().isEmpty()) continue;
            double classGpa = percentageToGPA(calculateClassAverage(classData));
            qualityPoints += classGpa * classData.getCreditHours();
            credits += classData.getCreditHours();
        }
        return credits == 0 ? 0.0 : capGPA(qualityPoints / credits);
    }

    private double calculateWeighted(List<Class> classes, String semester, String academicYear,
                                     double honorsBonus, double apIbBonus,
                                     double dualBonus, double weightedCap) {
        validateWeightedSettings(honorsBonus, apIbBonus, dualBonus, weightedCap);
        double qualityPoints = 0.0;
        double credits = 0.0;
        for (Class classData : classes) {
            if (!matches(classData, semester, academicYear) || classData.getGrades().isEmpty()) continue;
            double classGpa = weightedClassGPA(classData, honorsBonus, apIbBonus, dualBonus, weightedCap);
            qualityPoints += classGpa * classData.getCreditHours();
            credits += classData.getCreditHours();
        }
        return credits == 0 ? 0.0 : capGPA(qualityPoints / credits, weightedCap);
    }

    private void validateWeightedSettings(double honors, double apIb, double dual, double cap) {
        if (honors < 0 || apIb < 0 || dual < 0) {
            throw new IllegalArgumentException("Weighted GPA bonuses cannot be negative.");
        }
        if (cap < 4.0) {
            throw new IllegalArgumentException("Weighted GPA cap must be at least 4.0.");
        }
    }

    public double capGPA(double gpa) { return capGPA(gpa, 4.0); }

    public double capGPA(double gpa, double cap) {
        return Math.max(0.0, Math.min(cap, gpa));
    }

    private boolean matches(Class c, String semester, String year) {
        boolean semesterMatches = semester == null || semester.isBlank() || "All".equalsIgnoreCase(semester)
                || c.getSemester().equalsIgnoreCase(semester);
        boolean yearMatches = year == null || year.isBlank() || "All".equalsIgnoreCase(year)
                || c.getAcademicYear().equalsIgnoreCase(year);
        return semesterMatches && yearMatches;
    }
}
