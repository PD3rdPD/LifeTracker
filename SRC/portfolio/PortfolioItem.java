package portfolio;

public class PortfolioItem {
    private final String title;
    private final String description;
    private final String className;
    private final String academicYear;
    private final String grade;
    private final String skills;
    private final String filePath;
    private final String link;
    private final String reflection;
    private final boolean bestWork;
    private final boolean degreeFocused;

    public PortfolioItem(String title, String description, String className,
                         String academicYear, String grade, String skills,
                         String filePath, String link, String reflection,
                         boolean bestWork, boolean degreeFocused) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Portfolio title is required.");
        }
        this.title = title.trim();
        this.description = safe(description);
        this.className = safe(className);
        this.academicYear = safe(academicYear);
        this.grade = safe(grade);
        this.skills = safe(skills);
        this.filePath = safe(filePath);
        this.link = safe(link);
        this.reflection = safe(reflection);
        this.bestWork = bestWork;
        this.degreeFocused = degreeFocused;
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getClassName() { return className; }
    public String getAcademicYear() { return academicYear; }
    public String getGrade() { return grade; }
    public String getSkills() { return skills; }
    public String getFilePath() { return filePath; }
    public String getLink() { return link; }
    public String getReflection() { return reflection; }
    public boolean isBestWork() { return bestWork; }
    public boolean isDegreeFocused() { return degreeFocused; }

    @Override
    public String toString() {
        String tags = bestWork && degreeFocused ? "Best + Degree" :
                bestWork ? "Best Work" : degreeFocused ? "Degree Focused" : "Portfolio";
        return title + " — " + tags;
    }
}
