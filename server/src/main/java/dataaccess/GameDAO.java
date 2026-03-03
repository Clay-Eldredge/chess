package dataaccess;

import model.GameData;

public interface GameDAO {
    GameData createGame(String gameName) throws DataAccessException;

    GameData getGame(int gameID) throws DataAccessException;

    GameData[] listGames() throws DataAccessException;

    GameData updateGame(int gameID, GameData gameData) throws DataAccessException;

    public void clearAll();
}
