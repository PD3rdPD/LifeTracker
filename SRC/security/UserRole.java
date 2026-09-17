package security;

public enum UserRole {
    STUDENT("Student"),
    TEACHER("Teacher"),
    COUNSELOR("Counselor"),
    COLLEGE_REVIEWER("College Reviewer"),
    ADMIN("Administrator");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static UserRole fromStored(String value) {
        if (value == null || value.isBlank()) return STUDENT;
        try {
            return UserRole.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return STUDENT;
        }
    }
}
