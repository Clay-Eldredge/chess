package dataaccess;

import model.AuthData;

public class DBAuthDAO implements AuthDAO{
    @Override
    public AuthData createAuth(AuthData authData) throws DataAccessException {
        String sql = "INSERT INTO auth (authToken, username) VALUES (?, ?)";

        try (var connection = DatabaseManager.getConnection()) {
            try (var ps = connection.prepareStatement(sql)) {
                ps.setString(1, authData.authToken());
                ps.setString(2, authData.username());

                ps.executeUpdate();
                return authData;
            }
        } catch (Exception e) {
            throw new DataAccessException("Unable to create auth", e);
        }
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        String sql = "SELECT authToken, username FROM auth WHERE authToken=?";

        try (var connection = DatabaseManager.getConnection()) {
            try (var ps = connection.prepareStatement(sql)) {
                ps.setString(1, authToken);

                try (var rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return new AuthData(
                                rs.getString("authToken"),
                                rs.getString("username")
                        );
                    }
                }
                return null;
            }
        } catch (Exception e) {
            throw new DataAccessException("Unable to get auth", e);
        }
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        String sql = "DELETE FROM auth WHERE authToken=?";

        try (var connection = DatabaseManager.getConnection()) {
            try (var ps = connection.prepareStatement(sql)) {
                ps.setString(1, authToken);
                ps.executeUpdate();
            }
        } catch (Exception e) {
            throw new DataAccessException("Unable to delete auth", e);
        }
    }

    @Override
    public void clearAll() throws DataAccessException {
        String sql = "TRUNCATE auth";

        try (var connection = DatabaseManager.getConnection()) {
            try (var ps = connection.prepareStatement(sql)) {
                ps.executeUpdate();
            }
        } catch (Exception e) {
            throw new DataAccessException("Unable to clear auth", e);
        }
    }
}
