package dataaccess;

import chess.ChessGame;
import model.GameData;

import java.util.HashMap;
import java.util.Map;

public class MemoryGameDAO implements GameDAO {
    Map<String, GameData> games = new HashMap<>();
    int currentId = 1;


    @Override
    public GameData createGame(String gameName) throws DataAccessException {
        GameData newGame = new GameData(currentId, null, null, gameName, new ChessGame());
        games.put(Integer.toString(currentId),newGame);

        currentId += 1;

        return newGame;
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        return games.get(gameID);
    }

    @Override
    public GameData[] listGames() throws DataAccessException {
        return games.values().toArray(new GameData[0]);
    }

    @Override
    public GameData updateGame(int gameID, GameData gameData) throws DataAccessException {
        games.put(Integer.toString(gameID),gameData);
        return gameData;
    }

    @Override
    public void clearAll() {
        games = new HashMap<>();
    }
}
