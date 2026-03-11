package dataaccess;

import model.AuthData;
import model.UserData;

public class DBUserDAO implements UserDAO{
    public UserData getUser(String username) throws DataAccessException {
        String sql = "SELECT username, password, email FROM user WHERE username=?";

        try (var connection = DatabaseManager.getConnection()) {
            try (var ps = connection.prepareStatement(sql)) {
                ps.setString(1, username);

                try (var rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return new UserData(
                                rs.getString("username"),
                                rs.getString("password"),
                                rs.getString("email")
                        );
                    }
                }
                return null;
            }
        } catch (Exception e) {
            throw new DataAccessException("Unable to get user", e);
        }
    }

    public UserData createUser(UserData userData) throws DataAccessException {
        String sql = "INSERT INTO user (username, password, email) VALUES (?, ?, ?)";

        try (var connection = DatabaseManager.getConnection()) {
            try (var ps = connection.prepareStatement(sql)) {
                ps.setString(1, userData.username());
                ps.setString(2, userData.password());
                ps.setString(3, userData.email());

                ps.executeUpdate();
                return userData;
            }
        } catch (Exception e) {
            throw new DataAccessException("Unable to create user", e);
        }
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
