package dataaccess;

import chess.ChessGame;
import model.GameData;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class GameTests {

    private DBGameDAO gameDAO;

    @BeforeEach
    void setup() throws DataAccessException {
        gameDAO = new DBGameDAO();
        gameDAO.clearAll();
    }

    // ---------- createGame ----------

    @Test
    @DisplayName("createGame Positive")
    void createGamePositive() throws DataAccessException {
        GameData game = gameDAO.createGame("Test Game");

        assertNotNull(game);
        assertEquals("Test Game", game.gameName());
        assertNotNull(game.game());
        assertTrue(game.gameID() > 0);
    }

    @Test
    @DisplayName("createGame Negative (null name)")
    void createGameNegative() {
        assertThrows(DataAccessException.class, () -> {
            gameDAO.createGame(null);
        });
    }

    // ---------- getGame ----------

    @Test
    @DisplayName("getGame Positive")
    void getGamePositive() throws DataAccessException {
        GameData created = gameDAO.createGame("Retrieve Game");

        GameData retrieved = gameDAO.getGame(created.gameID());

        assertNotNull(retrieved);
        assertEquals(created.gameID(), retrieved.gameID());
        assertEquals("Retrieve Game", retrieved.gameName());
        assertNotNull(retrieved.game());
    }

    @Test
    @DisplayName("getGame Negative (game does not exist)")
    void getGameNegative() throws DataAccessException {
        GameData result = gameDAO.getGame(999999);

        assertNull(result);
    }

    // ---------- listGames ----------

    @Test
    @DisplayName("listGames Positive")
    void listGamesPositive() throws DataAccessException {
        gameDAO.createGame("Game One");
        gameDAO.createGame("Game Two");

        GameData[] games = gameDAO.listGames();

        assertNotNull(games);
        assertEquals(2, games.length);
    }

    @Test
    @DisplayName("listGames Negative (empty list)")
    void listGamesNegative() throws DataAccessException {
        GameData[] games = gameDAO.listGames();

        assertNotNull(games);
        assertEquals(0, games.length);
    }

    // ---------- updateGame ----------

    @Test
    @DisplayName("updateGame Positive")
    void updateGamePositive() throws DataAccessException {
        GameData created = gameDAO.createGame("Update Game");

        ChessGame game = created.game();

        GameData updated = new GameData(
                created.gameID(),
                "whitePlayer",
                "blackPlayer",
                created.gameName(),
                game
        );

        gameDAO.updateGame(created.gameID(), updated);

        GameData retrieved = gameDAO.getGame(created.gameID());

        assertEquals("whitePlayer", retrieved.whiteUsername());
        assertEquals("blackPlayer", retrieved.blackUsername());
    }

    @Test
    @DisplayName("updateGame Negative (game doesn't exist)")
    void updateGameNegative() {
        ChessGame game = new ChessGame();

        GameData fakeGame = new GameData(
                9999,
                "white",
                "black",
                "Fake",
                game
        );

        assertThrows(DataAccessException.class, () -> {
            gameDAO.updateGame(9999, fakeGame);
        });
    }

    // ---------- clearAll ----------

    @Test
    @DisplayName("clearAll Positive")
    void clearAllPositive() throws DataAccessException {
        gameDAO.createGame("Game A");
        gameDAO.createGame("Game B");

        gameDAO.clearAll();

        GameData[] games = gameDAO.listGames();

        assertEquals(0, games.length);
    }
}