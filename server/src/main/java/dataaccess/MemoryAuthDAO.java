package dataaccess;

import model.AuthData;

import java.util.HashMap;
import java.util.Map;

public class MemoryAuthDAO implements AuthDAO{
    Map<String, AuthData> auths = new HashMap<>();

    @Override
    public AuthData createAuth(AuthData authData) throws DataAccessException {
        if (auths.get(authData.authToken()) == null) {
            auths.put(authData.authToken(), authData);
            return authData;
        } else {
            throw new DataAccessException("Token already in use");
        }
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        return null;
    }

    @Override
    public AuthData deleteAuth(String authToken) throws DataAccessException {
        return null;
    }
}
