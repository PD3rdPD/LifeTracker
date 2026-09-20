package security;

public enum ShareScope {
    GRADES("Grades"),
    ACADEMIC_HISTORY("Academic History"),
    PORTFOLIO("Portfolio"),
    GOALS("Goals");

    private final String displayName;

    ShareScope(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
