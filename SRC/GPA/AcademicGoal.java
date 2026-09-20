package GPA;

public class AcademicGoal {

    private String goalName;
    private double targetGpa;
    private String deadline;

    public AcademicGoal(String goalName, double targetGpa, String deadline) {
        this.goalName = goalName;
        this.targetGpa = targetGpa;
        this.deadline = deadline;
    }

    public String getGoalName() {
        return goalName;
    }

    public double getTargetGpa() {
        return targetGpa;
    }

    public String getDeadline() {
        return deadline;
    }
}