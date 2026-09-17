package account;

import storage.StorageManager;
import java.util.ArrayList;
import java.util.List;

public class AccountManager {
    private final ArrayList<User> users;
    private final StorageManager storageManager;

    public AccountManager() {
        this(new StorageManager());
    }

    public AccountManager(StorageManager storageManager) {
        this.storageManager = storageManager;
        this.users = new ArrayList<>(storageManager.loadUsers());
    }

    public synchronized void addUser(User user) {
        if (findUser(user.getUsername()) != null) {
            throw new IllegalArgumentException("That username is already registered.");
        }
        users.add(user);
        storageManager.saveUsers(users);
    }

    public User register(String username, String password, String name, String educationLevel) {
        return register(username, password, name, educationLevel, "");
    }

    public User register(String username, String password, String name,
                         String educationLevel, String targetDegree) {
        User user = new User(username, password, name, educationLevel, targetDegree);
        addUser(user);
        return user;
    }

    public synchronized void saveUserProfile(User user) {
        storageManager.saveUsers(users);
    }

    public User findUser(String username) {
        if (username == null) return null;
        for (User user : users) {
            if (user.getUsername().equalsIgnoreCase(username.trim())) {
                return user;
            }
        }
        return null;
    }

    public List<User> getUsers() {
        return new ArrayList<>(users);
    }

    public StorageManager getStorageManager() {
        return storageManager;
    }
}
