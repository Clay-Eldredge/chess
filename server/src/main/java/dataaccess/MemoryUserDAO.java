package dataaccess;

import model.UserData;

import java.util.HashMap;
import java.util.Map;

public class MemoryUserDAO implements UserDAO {
    private Map<String, UserData> users = new HashMap<>();

    @Override
    public UserData getUser(String username) throws DataAccessException {
        UserData user = users.get(username);

        if (user == null) {
            throw new DataAccessException("Could not find user.");
        }

        return user;
    }
}
