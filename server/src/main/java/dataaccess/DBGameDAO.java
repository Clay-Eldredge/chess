package dataaccess;

import chess.ChessGame;
import model.GameData;

public class DBGameDAO implements GameDAO{
    public GameData createGame(String gameName) throws DataAccessException {
        return new GameData(1,"","","",new ChessGame());
    }

    public GameData getGame(int gameID) throws DataAccessException {
        return new GameData(1,"","","",new ChessGame());
    }

    public GameData[] listGames() throws DataAccessException {
        return new GameData[] {};
    }

    public GameData updateGame(int gameID, GameData gameData) throws DataAccessException {
        return new GameData(1,"","","",new ChessGame());
    }

    public void clearAll() throws DataAccessException {

    }
}
