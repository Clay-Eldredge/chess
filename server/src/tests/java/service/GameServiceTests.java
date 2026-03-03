package service;

import chess.ChessGame;
import dataaccess.*;
import model.AuthData;
import model.GameData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.requests.CreateRequest;
import service.requests.JoinRequest;

import static org.junit.jupiter.api.Assertions.*;

public class GameServiceTests {

    private AuthDAO authDAO;
    private GameDAO gameDAO;
    private GameService gameService;

    @BeforeEach
    public void setup() {
        authDAO = new MemoryAuthDAO();
        gameDAO = new MemoryGameDAO();
        gameService = new GameService(authDAO, gameDAO);
    }

    @Test
    public void createGamePositive() throws Exception {
        AuthData auth = new AuthData("token123", "clay");
        authDAO.createAuth(auth);

        CreateRequest request = new CreateRequest("Test Game");

        var result = gameService.createGame("token123", request);

        assertNotNull(result);
        assertTrue(result.gameID() > 0);

        GameData createdGame = gameDAO.getGame(result.gameID());
        assertNotNull(createdGame);
        assertEquals("Test Game", createdGame.gameName());
    }

    @Test
    public void createGameUnauthorized() {
        CreateRequest request = new CreateRequest("Test Game");

        assertThrows(UnauthorizedException.class, () -> {
            gameService.createGame("badToken", request);
        });
    }

    @Test
    public void listGamesPositive() throws Exception {
        AuthData auth = new AuthData("token123", "clay");
        authDAO.createAuth(auth);

        gameDAO.createGame("Game1");
        gameDAO.createGame("Game2");

        var result = gameService.listGames("token123");

        assertNotNull(result);
        assertEquals(2, result.games().length);
    }

    @Test
    public void listGamesUnauthorized() {
        assertThrows(UnauthorizedException.class, () -> {
            gameService.listGames("badToken");
        });
    }

    @Test
    public void joinGamePositive() throws Exception {
        AuthData auth = new AuthData("token123", "clay");
        authDAO.createAuth(auth);

        GameData game = gameDAO.createGame("Test Game");

        JoinRequest request = new JoinRequest(
                ChessGame.TeamColor.WHITE,
                game.gameID()
        );

        gameService.joinGame("token123", request);

        GameData updated = gameDAO.getGame(game.gameID());
        assertEquals("clay", updated.whiteUsername());
    }

    @Test
    public void joinGameAlreadyTaken() throws Exception {
        AuthData auth1 = new AuthData("token1", "clay");
        AuthData auth2 = new AuthData("token2", "bob");

        authDAO.createAuth(auth1);
        authDAO.createAuth(auth2);

        GameData game = gameDAO.createGame("Test Game");

        gameService.joinGame("token1",
                new JoinRequest(ChessGame.TeamColor.WHITE, game.gameID()));

        assertThrows(AlreadyTakenException.class, () -> {
            gameService.joinGame("token2",
                    new JoinRequest(ChessGame.TeamColor.WHITE, game.gameID()));
        });
    }
}