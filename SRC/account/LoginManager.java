package account;

public class LoginManager {
    private final AccountManager accountManager;

    public LoginManager(AccountManager accountManager) {
        this.accountManager = accountManager;
    }

    public User login(String username, String password) {
        User user = accountManager.findUser(username);
        if (user != null && user.passwordMatches(password)) {
            return user;
        }
        return null;
    }
}
