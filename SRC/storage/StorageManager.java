package storage;

import GPA.AcademicGoal;
import GPA.GPARecord;
import account.User;
import classes.Class;
import grades.Grade;
import portfolio.PortfolioItem;
import security.AccessGrant;
import security.ShareScope;
import security.UserRole;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.PosixFilePermission;
import java.util.ArrayList;
import java.util.Base64;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class StorageManager {
    private final Path dataDirectory;
    private final Path usersFile;

    public StorageManager() {
        this(Path.of(System.getProperty("user.home"), ".lifetracker"));
    }

    public StorageManager(Path dataDirectory) {
        this.dataDirectory = dataDirectory;
        this.usersFile = dataDirectory.resolve("users.tsv");
        ensureStorage();
    }

    private void ensureStorage() {
        try {
            Files.createDirectories(dataDirectory);
            if (!Files.exists(usersFile)) Files.createFile(usersFile);
            hardenPermissions(dataDirectory);
            hardenPermissions(usersFile);
        } catch (IOException e) {
            throw new IllegalStateException("Could not initialize app storage: " + e.getMessage(), e);
        }
    }

    public List<User> loadUsers() {
        List<User> users = new ArrayList<>();
        boolean migratedLegacyRecord = false;
        try (BufferedReader reader = Files.newBufferedReader(usersFile, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] p = line.split("\\t", -1);
                if (p.length == 5) {
                    users.add(User.fromStored(decode(p[0]), p[1], p[2], decode(p[3]), decode(p[4])));
                    migratedLegacyRecord = true;
                } else if (p.length == 6) {
                    users.add(User.fromStored(decode(p[0]), p[1], p[2], decode(p[3]), decode(p[4]), decode(p[5])));
                    migratedLegacyRecord = true;
                } else if (p.length >= 8) {
                    users.add(User.fromStored(p[0], decode(p[1]), p[2], p[3], decode(p[4]), decode(p[5]),
                            decode(p[6]), UserRole.fromStored(p[7])));
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Could not load users: " + e.getMessage(), e);
        }
        if (migratedLegacyRecord) saveUsers(users);
        return users;
    }

    public void saveUser(User user) {
        List<User> users = loadUsers();
        users.add(user);
        saveUsers(users);
    }

    public void saveUsers(List<User> users) {
        List<String> lines = new ArrayList<>();
        for (User user : users) {
            lines.add(String.join("\t",
                    user.getUserId(), encode(user.getUsername()), user.getPasswordHash(), user.getPasswordSalt(),
                    encode(user.getName()), encode(user.getEducationLevel()), encode(user.getTargetDegree()),
                    user.getRole().name()));
        }
        writeLines(usersFile, lines);
    }

    public List<Class> loadClasses(String username) {
        Path classesFile = userFile(username, "classes.tsv");
        Path gradesFile = userFile(username, "grades.tsv");
        List<Class> classes = new ArrayList<>();

        if (Files.exists(classesFile)) {
            try (BufferedReader reader = Files.newBufferedReader(classesFile, StandardCharsets.UTF_8)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.isBlank()) continue;
                    String[] p = line.split("\\t", -1);
                    if (p.length != 5 && p.length != 6) continue;
                    String courseLevel = p.length == 6 ? decode(p[5]) : Class.REGULAR;
                    classes.add(new Class(decode(p[0]), decode(p[1]), Double.parseDouble(p[2]),
                            decode(p[3]), decode(p[4]), courseLevel));
                }
            } catch (IOException e) {
                throw new IllegalStateException("Could not load classes: " + e.getMessage(), e);
            }
        }

        if (Files.exists(gradesFile)) {
            try (BufferedReader reader = Files.newBufferedReader(gradesFile, StandardCharsets.UTF_8)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.isBlank()) continue;
                    String[] p = line.split("\\t", -1);
                    if (p.length != 5 && p.length != 6 && p.length != 7) continue;
                    Class match;
                    String category;
                    if (p.length >= 7) {
                        match = findClass(classes, decode(p[0]), decode(p[6]));
                        category = decode(p[5]);
                    } else {
                        match = findClass(classes, decode(p[0]));
                        category = p.length == 6 ? decode(p[5]) : "Assignments";
                    }
                    if (match != null) {
                        match.addGrade(new Grade(decode(p[1]), Double.parseDouble(p[2]),
                                Double.parseDouble(p[3]), Double.parseDouble(p[4]), category));
                    }
                }
            } catch (IOException e) {
                throw new IllegalStateException("Could not load grades: " + e.getMessage(), e);
            }
        }
        return classes;
    }

    public void saveClasses(String username, List<Class> classes) {
        Path classesFile = userFile(username, "classes.tsv");
        Path gradesFile = userFile(username, "grades.tsv");
        List<String> classLines = new ArrayList<>();
        List<String> gradeLines = new ArrayList<>();

        for (Class c : classes) {
            classLines.add(String.join("\t", encode(c.getClassName()), encode(c.getTeacherName()),
                    Double.toString(c.getCreditHours()), encode(c.getSemester()), encode(c.getAcademicYear()),
                    encode(c.getCourseLevel())));
            for (Grade g : c.getGrades()) {
                gradeLines.add(String.join("\t", encode(c.getClassName()), encode(g.getAssignmentName()),
                        Double.toString(g.getScore()), Double.toString(g.getMaxScore()),
                        Double.toString(g.getWeight()), encode(g.getCategory()), encode(c.getAcademicYear())));
            }
        }
        writeLines(classesFile, classLines);
        writeLines(gradesFile, gradeLines);
    }

    public List<AcademicGoal> loadGoals(String username) {
        Path file = userFile(username, "goals.tsv");
        List<AcademicGoal> goals = new ArrayList<>();
        if (!Files.exists(file)) return goals;
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] p = line.split("\\t", -1);
                if (p.length == 3) goals.add(new AcademicGoal(decode(p[0]), Double.parseDouble(p[1]), decode(p[2])));
            }
        } catch (IOException e) {
            throw new IllegalStateException("Could not load goals: " + e.getMessage(), e);
        }
        return goals;
    }

    public void saveGoals(String username, List<AcademicGoal> goals) {
        List<String> lines = new ArrayList<>();
        for (AcademicGoal goal : goals) {
            lines.add(String.join("\t", encode(goal.getGoalName()), Double.toString(goal.getTargetGpa()), encode(goal.getDeadline())));
        }
        writeLines(userFile(username, "goals.tsv"), lines);
    }

    public List<GPARecord> loadGpaRecords(String username) {
        Path file = userFile(username, "gpa_records.tsv");
        List<GPARecord> records = new ArrayList<>();
        if (!Files.exists(file)) return records;
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] p = line.split("\\t", -1);
                if (p.length == 3) records.add(new GPARecord(Double.parseDouble(p[0]), decode(p[1]), decode(p[2])));
            }
        } catch (IOException e) {
            throw new IllegalStateException("Could not load GPA records: " + e.getMessage(), e);
        }
        return records;
    }

    public void saveGpaRecords(String username, List<GPARecord> records) {
        List<String> lines = new ArrayList<>();
        for (GPARecord record : records) {
            lines.add(String.join("\t", Double.toString(record.getGpa()), encode(record.getAcademicYear()), encode(record.getSemester())));
        }
        writeLines(userFile(username, "gpa_records.tsv"), lines);
    }

    public List<PortfolioItem> loadPortfolio(String username) {
        Path file = userFile(username, "portfolio.tsv");
        List<PortfolioItem> items = new ArrayList<>();
        if (!Files.exists(file)) return items;
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] p = line.split("\\t", -1);
                if (p.length != 11) continue;
                items.add(new PortfolioItem(decode(p[0]), decode(p[1]), decode(p[2]), decode(p[3]), decode(p[4]),
                        decode(p[5]), decode(p[6]), decode(p[7]), decode(p[8]),
                        Boolean.parseBoolean(p[9]), Boolean.parseBoolean(p[10])));
            }
        } catch (IOException e) {
            throw new IllegalStateException("Could not load portfolio: " + e.getMessage(), e);
        }
        return items;
    }

    public void savePortfolio(String username, List<PortfolioItem> items) {
        List<String> lines = new ArrayList<>();
        for (PortfolioItem item : items) {
            lines.add(String.join("\t", encode(item.getTitle()), encode(item.getDescription()), encode(item.getClassName()),
                    encode(item.getAcademicYear()), encode(item.getGrade()), encode(item.getSkills()),
                    encode(item.getFilePath()), encode(item.getLink()), encode(item.getReflection()),
                    Boolean.toString(item.isBestWork()), Boolean.toString(item.isDegreeFocused())));
        }
        writeLines(userFile(username, "portfolio.tsv"), lines);
    }

    public String importPortfolioFile(String username, Path source) {
        if (source == null || !Files.exists(source)) return "";
        try {
            Path folder = dataDirectory.resolve("portfolio").resolve(safeUsername(username));
            Files.createDirectories(folder);
            String fileName = source.getFileName().toString();
            Path target = folder.resolve(fileName);
            int counter = 1;
            while (Files.exists(target)) {
                int dot = fileName.lastIndexOf('.');
                String base = dot > 0 ? fileName.substring(0, dot) : fileName;
                String ext = dot > 0 ? fileName.substring(dot) : "";
                target = folder.resolve(base + "_" + counter++ + ext);
            }
            Files.copy(source, target, StandardCopyOption.COPY_ATTRIBUTES);
            return target.toAbsolutePath().toString();
        } catch (IOException e) {
            throw new IllegalStateException("Could not import portfolio file: " + e.getMessage(), e);
        }
    }

    public List<AccessGrant> loadAccessGrants(String username) {
        Path file = userFile(username, "access_grants.tsv");
        List<AccessGrant> grants = new ArrayList<>();
        if (!Files.exists(file)) return grants;
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] p = line.split("\\t", -1);
                if (p.length != 10) continue;
                EnumSet<ShareScope> scopes = EnumSet.noneOf(ShareScope.class);
                if (!p[5].isBlank()) {
                    for (String value : p[5].split(",")) {
                        try { scopes.add(ShareScope.valueOf(value)); } catch (IllegalArgumentException ignored) {}
                    }
                }
                grants.add(new AccessGrant(p[0], p[1], decode(p[2]), decode(p[3]),
                        UserRole.fromStored(p[4]), scopes, Long.parseLong(p[6]), Long.parseLong(p[7]),
                        p[8], Boolean.parseBoolean(p[9])));
            }
        } catch (IOException e) {
            throw new IllegalStateException("Could not load sharing grants: " + e.getMessage(), e);
        }
        return grants;
    }

    public void saveAccessGrants(String username, List<AccessGrant> grants) {
        List<String> lines = new ArrayList<>();
        for (AccessGrant grant : grants) {
            StringBuilder scopeText = new StringBuilder();
            for (ShareScope scope : grant.getScopes()) {
                if (scopeText.length() > 0) scopeText.append(',');
                scopeText.append(scope.name());
            }
            lines.add(String.join("\t", grant.getGrantId(), grant.getOwnerUserId(),
                    encode(grant.getOwnerUsername()), encode(grant.getRecipientIdentity()),
                    grant.getRecipientRole().name(), scopeText.toString(),
                    Long.toString(grant.getCreatedAtEpochMillis()), Long.toString(grant.getExpiresAtEpochMillis()),
                    grant.getInvitationTokenHash(), Boolean.toString(grant.isRevoked())));
        }
        writeLines(userFile(username, "access_grants.tsv"), lines);
    }

    public void appendAuditEvent(String username, String eventType, String objectId, String detail) {
        Path file = userFile(username, "audit.tsv");
        String line = String.join("\t", Long.toString(System.currentTimeMillis()), encode(eventType),
                encode(objectId == null ? "" : objectId), encode(detail == null ? "" : detail));
        try {
            Files.writeString(file, line + System.lineSeparator(), StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            hardenPermissions(file);
        } catch (IOException e) {
            throw new IllegalStateException("Could not write audit event: " + e.getMessage(), e);
        }
    }

    public boolean isEncryptedAtRest() {
        return false;
    }

    public Path getDataDirectory() { return dataDirectory; }

    private Path userFile(String username, String suffix) {
        return dataDirectory.resolve(safeUsername(username) + "_" + suffix);
    }

    private static String safeUsername(String username) {
        return username.replaceAll("[^A-Za-z0-9._-]", "_");
    }

    private static Class findClass(List<Class> classes, String className) {
        for (Class c : classes) if (c.getClassName().equals(className)) return c;
        return null;
    }

    private static Class findClass(List<Class> classes, String className, String academicYear) {
        for (Class c : classes) {
            if (c.getClassName().equals(className) && c.getAcademicYear().equals(academicYear)) return c;
        }
        return null;
    }

    private void writeLines(Path file, List<String> lines) {
        try {
            Files.write(file, lines, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            hardenPermissions(file);
        } catch (IOException e) {
            throw new IllegalStateException("Could not save data: " + e.getMessage(), e);
        }
    }

    private static void hardenPermissions(Path path) {
        try {
            Set<PosixFilePermission> permissions = Files.isDirectory(path)
                    ? EnumSet.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE, PosixFilePermission.OWNER_EXECUTE)
                    : EnumSet.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE);
            Files.setPosixFilePermissions(path, permissions);
        } catch (UnsupportedOperationException | IOException ignored) {
            // Windows and some file systems do not expose POSIX permissions.
        }
    }

    private static String encode(String value) {
        return Base64.getEncoder().encodeToString((value == null ? "" : value).getBytes(StandardCharsets.UTF_8));
    }

    private static String decode(String value) {
        return new String(Base64.getDecoder().decode(value), StandardCharsets.UTF_8);
    }
}
