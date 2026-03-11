package dataaccess;

import model.AuthData;

public class DBAuthDAO implements AuthDAO{
    @Override
    public AuthData createAuth(AuthData authData) throws DataAccessException {
        String sql = "INSERT INTO auth (authToken, username) VALUES (?, ?)";

        try (var connection = DatabaseManager.getConnection()) {
            try (var rs = connection.prepareStatement(sql)) {
                rs.setString(1, authData.authToken());
                rs.setString(2, authData.username());

                rs.executeUpdate();
                return authData;
            }
        } catch (Exception e) {
            throw new DataAccessException("Unable to create auth", e);
        }
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        return new AuthData("","");
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {

    }

    @Override
    public void clearAll() {

    }
}
