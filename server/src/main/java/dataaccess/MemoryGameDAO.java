package dataaccess;

import chess.ChessGame;
import model.GameData;

import java.util.HashMap;
import java.util.Map;

public class MemoryGameDAO implements GameDAO {
    Map<String, GameData> games = new HashMap<>();
    int currentId = 0;


    @Override
    public GameData createGame(String gameName) throws DataAccessException {
        GameData newGame = new GameData(currentId, null, null, gameName, new ChessGame());
        games.put(Integer.toString(currentId),newGame);

        currentId += 1;

        return newGame;
    }

    @Override
    public GameData getGame(String gameName) throws DataAccessException {
        return null;
    }

    @Override
    public GameData[] listGames() throws DataAccessException {
        return games.values().toArray(new GameData[0]);
    }

    @Override
    public GameData updateGame(String gameName, GameData gameData) throws DataAccessException {
        return null;
    }

    @Override
    public void clearAll() {
        games = new HashMap<>();
    }
}
