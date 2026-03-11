package dataaccess;

import model.UserData;

public class DBUserDAO implements UserDAO{
    public UserData getUser(String username) throws DataAccessException {
        return new UserData("","","");
    }

    public UserData createUser(UserData userData) throws DataAccessException {
        return new UserData("","","");
    }

    public void clearAll() throws DataAccessException {
        String sql = "TRUNCATE user";

        try (var connection = DatabaseManager.getConnection()) {
            try (var ps = connection.prepareStatement(sql)) {
                ps.executeUpdate();
            }
        } catch (Exception e) {
            throw new DataAccessException("Unable to clear users", e);
        }
    }
}
