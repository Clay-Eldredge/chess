package dataaccess;

import model.UserData;

import java.util.HashMap;
import java.util.Map;

public class MemoryUserDAO implements UserDAO {
    private Map<String, UserData> users = new HashMap<>();

    @Override
    public UserData getUser(String username) throws DataAccessException {
        UserData user = users.get(username);
        return user;
    }

    @Override
    public UserData createUser(UserData userData) throws DataAccessException {
        users.put(userData.username(), userData);
        return userData;
    }

    @Override
    public void clearAll() {
        users = new HashMap<>();
    }
}
