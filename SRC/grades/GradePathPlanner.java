package grades;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Generates possible future-grade paths that can reach a target average.
 */
public class GradePathPlanner {

    public Map<String, List<Double>> generateGradePaths(
            List<Double> currentGrades,
            double targetAverage,
            int remainingAssignments,
            double extraCreditPoints,
            boolean allowOver100) {

        LinkedHashMap<String, List<Double>> paths = new LinkedHashMap<>();

        if (remainingAssignments <= 0) {
            return paths;
        }

        double neededAverage = GradeCalculator.calculateRequiredGrade(
                currentGrades,
                targetAverage,
                remainingAssignments,
                extraCreditPoints);

        double maxScore = allowOver100 ? 110.0 : 100.0;

        if (neededAverage > maxScore) {
            return paths;
        }

        if (neededAverage <= 0) {
            ArrayList<Double> secured = new ArrayList<>();
            for (int i = 0; i < remainingAssignments; i++) {
                secured.add(0.0);
            }
            paths.put("Target Already Secured", secured);
            return paths;
        }

        addIfSuccessful(paths, "Consistent", buildEvenPath(neededAverage, remainingAssignments, maxScore),
                currentGrades, targetAverage, extraCreditPoints);
        addIfSuccessful(paths, "Strong Finish", buildRisingPath(neededAverage, remainingAssignments, maxScore),
                currentGrades, targetAverage, extraCreditPoints);
        addIfSuccessful(paths, "Strong Start", buildFallingPath(neededAverage, remainingAssignments, maxScore),
                currentGrades, targetAverage, extraCreditPoints);
        addIfSuccessful(paths, "Buffer", buildEvenPath(Math.min(maxScore, neededAverage + 3.0), remainingAssignments, maxScore),
                currentGrades, targetAverage, extraCreditPoints);

        return paths;
    }

    private List<Double> buildEvenPath(double neededAverage, int count, double maxScore) {
        ArrayList<Double> path = new ArrayList<>();
        double score = clamp(neededAverage, 0, maxScore);
        for (int i = 0; i < count; i++) {
            path.add(score);
        }
        return path;
    }

    private List<Double> buildRisingPath(double neededAverage, int count, double maxScore) {
        ArrayList<Double> path = new ArrayList<>();
        if (count == 1) {
            path.add(clamp(neededAverage, 0, maxScore));
            return path;
        }

        double step = 4.0;
        double middle = (count - 1) / 2.0;
        for (int i = 0; i < count; i++) {
            double score = neededAverage + (i - middle) * step;
            path.add(clamp(score, 0, maxScore));
        }
        adjustPathToRequiredAverage(path, neededAverage, maxScore);
        return path;
    }

    private List<Double> buildFallingPath(double neededAverage, int count, double maxScore) {
        ArrayList<Double> rising = new ArrayList<>(buildRisingPath(neededAverage, count, maxScore));
        ArrayList<Double> falling = new ArrayList<>();
        for (int i = rising.size() - 1; i >= 0; i--) {
            falling.add(rising.get(i));
        }
        return falling;
    }

    private void adjustPathToRequiredAverage(List<Double> path, double neededAverage, double maxScore) {
        double targetTotal = neededAverage * path.size();
        double currentTotal = sum(path);
        double difference = targetTotal - currentTotal;

        for (int i = path.size() - 1; i >= 0 && Math.abs(difference) > 0.0001; i--) {
            double current = path.get(i);
            double roomUp = maxScore - current;
            double roomDown = current;
            double change;

            if (difference > 0) {
                change = Math.min(difference, roomUp);
            } else {
                change = -Math.min(-difference, roomDown);
            }

            path.set(i, current + change);
            difference -= change;
        }
    }

    private void addIfSuccessful(
            Map<String, List<Double>> paths,
            String name,
            List<Double> path,
            List<Double> currentGrades,
            double targetAverage,
            double extraCreditPoints) {

        if (path.isEmpty() || containsDuplicatePath(paths, path)) {
            return;
        }

        double finalTotal = extraCreditPoints + sum(currentGrades) + sum(path);
        int finalCount = currentGrades.size() + path.size();
        double finalAverage = finalTotal / finalCount;

        if (finalAverage + 0.0001 >= targetAverage) {
            paths.put(name, path);
        }
    }

    private boolean containsDuplicatePath(Map<String, List<Double>> paths, List<Double> candidate) {
        for (List<Double> existing : paths.values()) {
            if (samePath(existing, candidate)) {
                return true;
            }
        }
        return false;
    }

    private boolean samePath(List<Double> a, List<Double> b) {
        if (a.size() != b.size()) {
            return false;
        }
        for (int i = 0; i < a.size(); i++) {
            if (Math.abs(a.get(i) - b.get(i)) > 0.0001) {
                return false;
            }
        }
        return true;
    }

    private double sum(List<Double> values) {
        double total = 0.0;
        for (double value : values) {
            total += value;
        }
        return total;
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
