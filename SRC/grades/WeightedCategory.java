package grades;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents one weighted grading category such as Tests, Quizzes, or Homework.
 */
public class WeightedCategory {

    private final String name;
    private final double weight;
    private final ArrayList<Double> grades;

    public WeightedCategory(String name, double weight) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Category name is required.");
        }
        if (weight < 0) {
            throw new IllegalArgumentException("Category weight cannot be negative.");
        }

        this.name = name;
        this.weight = weight;
        this.grades = new ArrayList<>();
    }

    public void addGrade(double grade, boolean allowOver100) {
        GradeCalculator.validateGrade(grade, allowOver100);
        grades.add(grade);
    }

    public String getName() {
        return name;
    }

    public double getWeight() {
        return weight;
    }

    public List<Double> getGrades() {
        return new ArrayList<>(grades);
    }

    public double getAverage() {
        if (grades.isEmpty()) {
            throw new IllegalStateException("Category " + name + " must contain at least one grade.");
        }

        double total = 0.0;
        for (double grade : grades) {
            total += grade;
        }
        return total / grades.size();
    }
}
