package dataaccess;

import model.AuthData;
import model.GameData;

public interface GameDAO {
    GameData createGame(GameData gameData) throws DataAccessException;

    GameData getGame(String gameName) throws DataAccessException;

    AuthData createAuth(AuthData authData) throws DataAccessException;
}
