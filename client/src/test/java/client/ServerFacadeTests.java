package client;

import chess.ChessGame;
import org.junit.jupiter.api.*;
import server.Server;

import results.*;

import static org.junit.jupiter.api.Assertions.*;

public class ServerFacadeTests {

    private static Server server;
    private static String serverUrl;

    private ServerFacade facade;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        serverUrl = "http://localhost:" + port;
        System.out.println("Started test HTTP server on " + port);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @BeforeEach
    void setup() {
        facade = new ServerFacade(serverUrl);
    }

    @Test
    public void registerPositive() {
        RegisterResult result = facade.register("userA", "pass", "email@test.com");
        assertNotNull(result.authToken());
        assertEquals("userA", result.username());
    }

    @Test
    public void registerNegative() {
        facade.register("userB", "pass", "email@test.com");

        assertThrows(ResponseException.class, () ->
                facade.register("userB", "pass", "email@test.com")
        );
    }

    @Test
    public void loginPositive() {
        facade.register("userC", "pass", "email@test.com");

        LoginResult result = facade.login("userC", "pass");

        assertNotNull(result.authToken());
        assertEquals("userC", result.username());
    }

    @Test
    public void loginNegative() {
        assertThrows(ResponseException.class, () ->
                facade.login("badUser", "badPass")
        );
    }

    @Test
    public void logoutPositive() {
        RegisterResult reg = facade.register("userD", "pass", "email@test.com");

        assertDoesNotThrow(() ->
                facade.logout(reg.authToken())
        );
    }

    @Test
    public void logoutNegative() {
        assertThrows(ResponseException.class, () ->
                facade.logout("badToken")
        );
    }

    @Test
    public void createPositive() {
        RegisterResult reg = facade.register("userE", "pass", "email@test.com");

        CreateResult result = facade.create("game1", reg.authToken());

        assertTrue(result.gameID() > 0);
    }

    @Test
    public void createNegative() {
        assertThrows(ResponseException.class, () ->
                facade.create("gameFail", "badToken")
        );
    }

    @Test
    public void listPositive() {
        RegisterResult reg = facade.register("userF", "pass", "email@test.com");

        facade.create("gameList", reg.authToken());

        ListResult result = facade.list(reg.authToken());

        assertNotNull(result.games());
        assertTrue(result.games().length >= 1);
    }

    @Test
    public void listNegative() {
        assertThrows(ResponseException.class, () ->
                facade.list("badToken")
        );
    }

    @Test
    public void joinPositive() {
        RegisterResult reg = facade.register("userG", "pass", "email@test.com");

        CreateResult game = facade.create("joinGame", reg.authToken());

        assertDoesNotThrow(() ->
                facade.join(game.gameID(), ChessGame.TeamColor.WHITE, reg.authToken())
        );
    }

    @Test
    public void joinNegative() {
        RegisterResult reg = facade.register("userH", "pass", "email@test.com");

        CreateResult game = facade.create("joinFail", reg.authToken());

        assertThrows(ResponseException.class, () ->
                facade.join(game.gameID(), ChessGame.TeamColor.WHITE, "badToken")
        );
    }
}