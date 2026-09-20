package grades;

import java.util.ArrayList;
import java.util.List;

/**
 * Core LifeTracker/LifeTracker calculator logic.
 *
 * This keeps the original LifeTracker percentage/weight methods while adding
 * the calculator features from the Python LifeTracker version.
 */
public class GradeCalculator {

    private static final double WEIGHT_TOLERANCE = 0.01;

    // Existing LifeTracker calculation.
    public double calculatePercentage(double score, double maxScore) {
        if (maxScore <= 0) {
            throw new IllegalArgumentException("Maximum score must be greater than zero.");
        }
        if (score < 0) {
            throw new IllegalArgumentException("Score cannot be negative.");
        }
        return (score / maxScore) * 100;
    }

    // Existing LifeTracker calculation.
    public double calculateWeightedGrade(double percentage, double weight) {
        if (weight < 0) {
            throw new IllegalArgumentException("Weight cannot be negative.");
        }
        return percentage * (weight / 100.0);
    }

    /**
     * Assignment-level weighted calculation retained for current LifeTracker use.
     * The weights must total 100%.
     */
    public double calculateFinalGrade(double[] percentages, double[] weights) {
        if (percentages == null || weights == null || percentages.length == 0) {
            throw new IllegalArgumentException("At least one percentage and weight are required.");
        }
        if (percentages.length != weights.length) {
            throw new IllegalArgumentException("Percentages and weights must have the same length.");
        }

        double total = 0.0;
        double totalWeight = 0.0;

        for (int i = 0; i < percentages.length; i++) {
            if (percentages[i] < 0) {
                throw new IllegalArgumentException("Percentage cannot be negative.");
            }
            if (weights[i] < 0) {
                throw new IllegalArgumentException("Weight cannot be negative.");
            }
            total += calculateWeightedGrade(percentages[i], weights[i]);
            totalWeight += weights[i];
        }

        validateWeightTotal(totalWeight);
        return total;
    }

    /**
     * Standard LifeTracker average. Standalone extra credit is added to the
     * numerator without adding another assignment to the denominator.
     */
    public static double calculateAverage(List<Double> grades, double extraCreditPoints) {
        validateGradeList(grades);
        if (extraCreditPoints < 0) {
            throw new IllegalArgumentException("Extra credit points cannot be negative.");
        }

        double total = extraCreditPoints;
        for (double grade : grades) {
            total += grade;
        }
        return total / grades.size();
    }

    /**
     * Python-compatible weighted category average.
     */
    public double calculateWeightedAverage(List<WeightedCategory> categories) {
        if (categories == null || categories.isEmpty()) {
            throw new IllegalArgumentException("At least one weighted category is required.");
        }

        double weightedTotal = 0.0;
        double totalWeight = 0.0;

        for (WeightedCategory category : categories) {
            if (category == null) {
                throw new IllegalArgumentException("Weighted categories cannot contain null values.");
            }
            weightedTotal += category.getAverage() * (category.getWeight() / 100.0);
            totalWeight += category.getWeight();
        }

        validateWeightTotal(totalWeight);
        return weightedTotal;
    }

    public static String letterGrade(double average) {
        if (average >= 90) {
            return "A";
        } else if (average >= 80) {
            return "B";
        } else if (average >= 70) {
            return "C";
        } else if (average >= 60) {
            return "D";
        }
        return "F";
    }

    /**
     * Parses comma-separated grade input the same way the Python calculator did.
     */
    public static List<Double> parseGrades(String rawGrades, boolean allowOver100) {
        if (rawGrades == null || rawGrades.trim().isEmpty()) {
            throw new IllegalArgumentException("Enter at least one grade.");
        }

        String[] parts = rawGrades.split(",", -1);
        ArrayList<Double> grades = new ArrayList<>();

        for (String part : parts) {
            String cleaned = part.trim();
            if (cleaned.isEmpty()) {
                throw new IllegalArgumentException("Grades cannot contain blank entries.");
            }

            double grade;
            try {
                grade = Double.parseDouble(cleaned);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Each grade must be a number.");
            }

            validateGrade(grade, allowOver100);
            grades.add(grade);
        }

        return grades;
    }

    public static void validateGrade(double grade, boolean allowOver100) {
        if (Double.isNaN(grade) || Double.isInfinite(grade)) {
            throw new IllegalArgumentException("Grade must be a real number.");
        }
        if (grade < 0) {
            throw new IllegalArgumentException("Grades cannot be negative.");
        }
        if (!allowOver100 && grade > 100) {
            throw new IllegalArgumentException("Grades over 100 require extra-credit mode.");
        }
    }

    /**
     * Calculates the average required across the remaining assignments.
     */
    public static double calculateRequiredGrade(
            List<Double> grades,
            double targetAverage,
            int remainingAssignments,
            double extraCreditPoints) {

        validateGradeList(grades);

        if (targetAverage < 0 || targetAverage > 100) {
            throw new IllegalArgumentException("Target average must be between 0 and 100.");
        }
        if (remainingAssignments <= 0) {
            throw new IllegalArgumentException("Remaining assignments must be greater than zero.");
        }
        if (extraCreditPoints < 0) {
            throw new IllegalArgumentException("Extra credit points cannot be negative.");
        }

        double currentTotal = extraCreditPoints;
        for (double grade : grades) {
            currentTotal += grade;
        }

        int finalCount = grades.size() + remainingAssignments;
        double targetTotal = targetAverage * finalCount;
        return (targetTotal - currentTotal) / remainingAssignments;
    }

    public static String progressFeedback(double average) {
        if (average >= 90) {
            return "Excellent progress - you are currently in the A range.";
        } else if (average >= 80) {
            return "Good progress - you are currently in the B range.";
        } else if (average >= 70) {
            return "You are currently in the C range. Keep working toward your goal.";
        } else if (average >= 60) {
            return "You are currently in the D range. There is room to improve.";
        }
        return "You are currently below 60. Focus on upcoming assignments and available support.";
    }

    public static String progressFeedback(double average, double targetAverage) {
        if (targetAverage < 0 || targetAverage > 100) {
            throw new IllegalArgumentException("Target average must be between 0 and 100.");
        }

        String base = progressFeedback(average);
        double difference = average - targetAverage;

        if (Math.abs(difference) < 0.005) {
            return base + " You are exactly at your target.";
        } else if (difference > 0) {
            return base + String.format(" You are %.2f points above your target.", difference);
        }
        return base + String.format(" You are %.2f points below your target.", -difference);
    }

    private static void validateGradeList(List<Double> grades) {
        if (grades == null || grades.isEmpty()) {
            throw new IllegalArgumentException("At least one grade is required.");
        }
        for (Double grade : grades) {
            if (grade == null || Double.isNaN(grade) || Double.isInfinite(grade)) {
                throw new IllegalArgumentException("Grades must contain valid numbers.");
            }
            if (grade < 0) {
                throw new IllegalArgumentException("Grades cannot be negative.");
            }
        }
    }

    private void validateWeightTotal(double totalWeight) {
        if (Math.abs(totalWeight - 100.0) > WEIGHT_TOLERANCE) {
            throw new IllegalArgumentException(
                    String.format("Weights must total 100%%. Current total: %.2f%%", totalWeight));
        }
    }
}
