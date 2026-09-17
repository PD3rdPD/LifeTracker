package grades;

public class Grade {

    private final String assignmentName;
    private final double score;
    private final double maxScore;
    private final double weight;
    private final String category;

    public Grade(String assignmentName, double score, double maxScore, double weight) {
        this(assignmentName, score, maxScore, weight, "Assignments");
    }

    public Grade(String assignmentName, double score, double maxScore, double weight, String category) {
        if (assignmentName == null || assignmentName.trim().isEmpty()) {
            throw new IllegalArgumentException("Assignment name is required.");
        }
        if (score < 0) {
            throw new IllegalArgumentException("Score cannot be negative.");
        }
        if (maxScore <= 0) {
            throw new IllegalArgumentException("Maximum score must be greater than zero.");
        }
        if (weight < 0) {
            throw new IllegalArgumentException("Weight cannot be negative.");
        }

        this.assignmentName = assignmentName.trim();
        this.score = score;
        this.maxScore = maxScore;
        this.weight = weight;
        this.category = category == null || category.trim().isEmpty() ? "Assignments" : category.trim();
    }

    public String getAssignmentName() { return assignmentName; }
    public double getScore() { return score; }
    public double getMaxScore() { return maxScore; }
    public double getWeight() { return weight; }
    public String getCategory() { return category; }

    public double getPercentage() {
        return (score / maxScore) * 100;
    }
}
