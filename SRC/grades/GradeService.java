package grades;

import classes.Class;

public class GradeService {

    private GradeCalculator calculator;

    public GradeService() {
        calculator = new GradeCalculator();
    }

    /**
     * Calculates a LifeTracker class grade using assignment-level weights.
     * All assignment weights must total 100%.
     */
    public double calculateClassGrade(Class classData) {
        if (classData == null || classData.getGrades().isEmpty()) {
            return 0.0;
        }

        double[] percentages = new double[classData.getGrades().size()];
        double[] weights = new double[classData.getGrades().size()];

        for (int i = 0; i < classData.getGrades().size(); i++) {
            Grade grade = classData.getGrades().get(i);
            percentages[i] = grade.getPercentage();
            weights[i] = grade.getWeight();
        }

        return calculator.calculateFinalGrade(percentages, weights);
    }
}
