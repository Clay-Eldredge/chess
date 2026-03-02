package dataaccess;

import model.AuthData;

public class MemoryAuthDAO implements AuthDAO{

    @Override
    public AuthData createAuth(AuthData authData) throws DataAccessException {
        return null;
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
