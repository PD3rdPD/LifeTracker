package account;

import security.UserRole;

import java.util.UUID;

public class User {
    private final String userId;
    private final String username;
    private final String passwordHash;
    private final String passwordSalt;
    private final String name;
    private final String educationLevel;
    private final UserRole role;
    private String targetDegree;

    public User(String username, String password, String name, String educationLevel) {
        this(username, password, name, educationLevel, "");
    }

    public User(String username, String password, String name, String educationLevel, String targetDegree) {
        validateProfile(username, name, educationLevel);
        this.userId = UUID.randomUUID().toString();
        this.username = username.trim();
        this.passwordSalt = PasswordUtil.newSalt();
        this.passwordHash = PasswordUtil.hashPassword(password, this.passwordSalt);
        this.name = name.trim();
        this.educationLevel = educationLevel.trim();
        this.targetDegree = targetDegree == null ? "" : targetDegree.trim();
        this.role = UserRole.STUDENT;
    }

    private User(String userId, String username, String passwordHash, String passwordSalt,
                 String name, String educationLevel, String targetDegree, UserRole role) {
        validateProfile(username, name, educationLevel);
        this.userId = userId == null || userId.isBlank() ? UUID.randomUUID().toString() : userId;
        this.username = username.trim();
        this.passwordHash = passwordHash;
        this.passwordSalt = passwordSalt;
        this.name = name.trim();
        this.educationLevel = educationLevel.trim();
        this.targetDegree = targetDegree == null ? "" : targetDegree.trim();
        this.role = role == null ? UserRole.STUDENT : role;
    }

    public static User fromStored(String username, String passwordHash, String passwordSalt,
                                  String name, String educationLevel) {
        return new User(UUID.randomUUID().toString(), username, passwordHash, passwordSalt,
                name, educationLevel, "", UserRole.STUDENT);
    }

    public static User fromStored(String username, String passwordHash, String passwordSalt,
                                  String name, String educationLevel, String targetDegree) {
        return new User(UUID.randomUUID().toString(), username, passwordHash, passwordSalt,
                name, educationLevel, targetDegree, UserRole.STUDENT);
    }

    public static User fromStored(String userId, String username, String passwordHash, String passwordSalt,
                                  String name, String educationLevel, String targetDegree, UserRole role) {
        return new User(userId, username, passwordHash, passwordSalt,
                name, educationLevel, targetDegree, role);
    }

    private static void validateProfile(String username, String name, String educationLevel) {
        if (username == null || username.trim().length() < 3) {
            throw new IllegalArgumentException("Username must be at least 3 characters long.");
        }
        if (!username.trim().matches("[A-Za-z0-9._-]+")) {
            throw new IllegalArgumentException("Username can use letters, numbers, dots, underscores, and hyphens only.");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name is required.");
        }
        if (educationLevel == null || educationLevel.trim().isEmpty()) {
            throw new IllegalArgumentException("Education level is required.");
        }
    }

    public String getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public String getPasswordSalt() { return passwordSalt; }
    public String getName() { return name; }
    public String getEducationLevel() { return educationLevel; }
    public String getTargetDegree() { return targetDegree; }
    public UserRole getRole() { return role; }

    public void setTargetDegree(String targetDegree) {
        this.targetDegree = targetDegree == null ? "" : targetDegree.trim();
    }

    public boolean passwordMatches(String password) {
        return PasswordUtil.verifyPassword(password, passwordSalt, passwordHash);
    }
}
