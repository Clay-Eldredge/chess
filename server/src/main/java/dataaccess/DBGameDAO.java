package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.AuthData;
import model.GameData;

import java.sql.Statement;
import java.util.ArrayList;

public class DBGameDAO implements GameDAO{
    private final Gson gson = new Gson();

    public GameData createGame(String gameName) throws DataAccessException {
        ChessGame game = new ChessGame();
        String gameJson = gson.toJson(game);

        String sql = "INSERT INTO game (gameName, game) VALUES (?, ?)";

        try (var connection = DatabaseManager.getConnection()) {
            try (var ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, gameName);
                ps.setString(2, gson.toJson(gameJson));

                ps.executeUpdate();

                var rs = ps.getGeneratedKeys();
                rs.next();

                int gameID = rs.getInt(1);

                return new GameData(gameID, null, null, gameName, game);
            }
        } catch (Exception e) {
            throw new DataAccessException("Unable to create game", e);
        }
    }

    public GameData getGame(int gameID) throws DataAccessException {
        String sql = "SELECT gameID, whiteUsername, blackUsername, gameName, game FROM game WHERE gameID=?";

        try (var connection = DatabaseManager.getConnection()) {
            try (var ps = connection.prepareStatement(sql)) {
                ps.setInt(1, gameID);

                try (var rs = ps.executeQuery()) {
                    if (rs.next()) {
                        ChessGame deserializedGame = gson.fromJson(rs.getString("game"), ChessGame.class);

                        return new GameData(
                                rs.getInt("gameID"),
                                rs.getString("whiteUsername"),
                                rs.getString("blackUsername"),
                                rs.getString("gameName"),
                                deserializedGame
                        );
                    }
                }
                return null;
            }
        } catch (Exception e) {
            throw new DataAccessException("Unable to get game", e);
        }
    }

    public GameData[] listGames() throws DataAccessException {
        String sql = "SELECT gameID, whiteUsername, blackUsername, gameName, game FROM game;";

        ArrayList<GameData> gamesList = new ArrayList<>();

        try (var connection = DatabaseManager.getConnection()) {
            try (var ps = connection.prepareStatement(sql)) {
                try (var rs = ps.executeQuery()) {
                    while (rs.next()) {
                        ChessGame deserializedGame = gson.fromJson(rs.getString("game"), ChessGame.class);

                        gamesList.add(new GameData(
                                rs.getInt("gameID"),
                                rs.getString("whiteUsername"),
                                rs.getString("blackUsername"),
                                rs.getString("gameName"),
                                deserializedGame
                        ));
                    }

                    return gamesList.toArray(new GameData[0]);
                }
            }
        } catch (Exception e) {
            throw new DataAccessException("Unable to get games list", e);
        }
    }

    public GameData updateGame(int gameID, GameData gameData) throws DataAccessException {
        String sql = "UPDATE game SET whiteUsername = ?, blackUsername = ?, game = ? WHERE gameID = ?;";

        try (var connection = DatabaseManager.getConnection()) {
            try (var ps = connection.prepareStatement(sql)) {
                ps.setString(1, gameData.whiteUsername());
                ps.setString(2, gameData.blackUsername());
                ps.setString(3, gson.toJson(gameData.game()));
                ps.setInt(4, gameData.gameID());

                ps.executeUpdate();

                return gameData;
            }
        } catch (Exception e) {
            throw new DataAccessException("Unable to update game", e);
        }
    }

    public void clearAll() throws DataAccessException {
        String sql = "TRUNCATE game";

        try (var connection = DatabaseManager.getConnection()) {
            try (var ps = connection.prepareStatement(sql)) {
                ps.executeUpdate();
            }
        } catch (Exception e) {
            throw new DataAccessException("Unable to clear game", e);
        }
    }
}
