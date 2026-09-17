package grades;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Simple no-library test runner for the calculator features.
 * Run with: java grades.CalculatorFeatureTest
 */
public class CalculatorFeatureTest {

    public static void main(String[] args) {
        GradeCalculator calculator = new GradeCalculator();

        List<Double> standard = Arrays.asList(80.0, 90.0, 100.0);
        assertClose(90.0, GradeCalculator.calculateAverage(standard, 0), "standard average");
        assertEquals("A", GradeCalculator.letterGrade(90.0), "letter grade boundary");

        // Standalone extra credit: (80 + 90 + 100 + 6) / 3 = 92
        assertClose(92.0, GradeCalculator.calculateAverage(standard, 6), "standalone extra credit");

        WeightedCategory tests = new WeightedCategory("Tests", 50);
        tests.addGrade(90, false);
        tests.addGrade(80, false);

        WeightedCategory quizzes = new WeightedCategory("Quizzes", 20);
        quizzes.addGrade(100, false);

        WeightedCategory homework = new WeightedCategory("Homework", 30);
        homework.addGrade(90, false);

        assertClose(89.5, calculator.calculateWeightedAverage(Arrays.asList(tests, quizzes, homework)),
                "weighted category average");

        List<Double> parsed = GradeCalculator.parseGrades("95, 100, 87.5", false);
        assertEquals(3, parsed.size(), "grade parser count");

        double needed = GradeCalculator.calculateRequiredGrade(
                Arrays.asList(80.0, 85.0, 90.0), 90.0, 3, 0);
        assertClose(95.0, needed, "goal prediction");

        GradePathPlanner planner = new GradePathPlanner();
        Map<String, List<Double>> paths = planner.generateGradePaths(
                Arrays.asList(80.0, 85.0, 90.0), 90.0, 3, 0, false);
        if (paths.isEmpty()) {
            throw new AssertionError("Expected at least one grade path.");
        }

        expectError(() -> GradeCalculator.parseGrades("90,,80", false), "blank grade validation");
        expectError(() -> GradeCalculator.parseGrades("101", false), "over-100 validation");
        expectError(() -> GradeCalculator.parseGrades("-1", true), "negative-grade validation");
        expectError(() -> calculator.calculateFinalGrade(
                new double[]{90, 80}, new double[]{40, 20}), "weight-total validation");

        System.out.println("All LifeTracker calculator feature tests passed.");
        System.out.println("Example paths: " + paths);
        System.out.println(GradeCalculator.progressFeedback(88.0, 90.0));
    }

    private static void assertClose(double expected, double actual, String name) {
        if (Math.abs(expected - actual) > 0.0001) {
            throw new AssertionError(name + " failed. Expected " + expected + " but got " + actual);
        }
    }

    private static void assertEquals(Object expected, Object actual, String name) {
        if (!expected.equals(actual)) {
            throw new AssertionError(name + " failed. Expected " + expected + " but got " + actual);
        }
    }

    private static void expectError(Runnable action, String name) {
        try {
            action.run();
            throw new AssertionError(name + " failed. Expected an error.");
        } catch (IllegalArgumentException expected) {
            // Test passed.
        }
    }
}
