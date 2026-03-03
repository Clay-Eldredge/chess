package dataaccess;

import model.GameData;

public interface GameDAO {
    GameData createGame(String gameName) throws DataAccessException;

    GameData getGame(String gameName) throws DataAccessException;

    GameData[] listGames() throws DataAccessException;

    GameData updateGame(String gameName, GameData gameData) throws DataAccessException;

    public void clearAll();
}
