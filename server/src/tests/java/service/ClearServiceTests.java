package service;

import dataaccess.*;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ClearServiceTests {

    private UserDAO userDAO;
    private AuthDAO authDAO;
    private GameDAO gameDAO;
    private ClearService clearService;

    @BeforeEach
    public void setup() {
        userDAO = new MemoryUserDAO();
        authDAO = new MemoryAuthDAO();
        gameDAO = new MemoryGameDAO();
        clearService = new ClearService(userDAO, authDAO, gameDAO);
    }

    @Test
    public void clearAllPositive() throws Exception {
        // Arrange - insert data into all DAOs
        userDAO.createUser(new UserData("clay", "pass", "email@test.com"));
        authDAO.createAuth(new AuthData("token123", "clay"));
        gameDAO.createGame("Test Game");

        // Sanity check data exists
        assertNotNull(userDAO.getUser("clay"));
        assertNotNull(authDAO.getAuth("token123"));
        assertTrue(gameDAO.listGames().length > 0);

        // Act
        clearService.clearAll();

        // Assert everything cleared
        assertNull(userDAO.getUser("clay"));
        assertNull(authDAO.getAuth("token123"));
        assertEquals(0, gameDAO.listGames().length);
    }
}