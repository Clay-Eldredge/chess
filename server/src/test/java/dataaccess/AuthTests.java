package dataaccess;

import model.AuthData;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class AuthTests {

    private DBAuthDAO authDAO;

    @BeforeEach
    void setup() throws DataAccessException {
        authDAO = new DBAuthDAO();
        authDAO.clearAll();
    }

    // ---------- createAuth ----------

    @Test
    @DisplayName("createAuth Positive")
    void createAuthPositive() throws DataAccessException {
        AuthData auth = new AuthData("token123", "user1");

        AuthData result = authDAO.createAuth(auth);

        assertNotNull(result);
        assertEquals("token123", result.authToken());
        assertEquals("user1", result.username());

        AuthData fromDB = authDAO.getAuth("token123");
        assertNotNull(fromDB);
        assertEquals("user1", fromDB.username());
    }

    @Test
    @DisplayName("createAuth Negative (duplicate token)")
    void createAuthNegative() throws DataAccessException {
        AuthData auth = new AuthData("token123", "user1");

        authDAO.createAuth(auth);

        assertThrows(DataAccessException.class, () -> {
            authDAO.createAuth(auth);
        });
    }

    // ---------- getAuth ----------

    @Test
    @DisplayName("getAuth Positive")
    void getAuthPositive() throws DataAccessException {
        AuthData auth = new AuthData("tokenABC", "user2");

        authDAO.createAuth(auth);

        AuthData result = authDAO.getAuth("tokenABC");

        assertNotNull(result);
        assertEquals("tokenABC", result.authToken());
        assertEquals("user2", result.username());
    }

    @Test
    @DisplayName("getAuth Negative (token not found)")
    void getAuthNegative() throws DataAccessException {
        AuthData result = authDAO.getAuth("doesNotExist");

        assertNull(result);
    }

    // ---------- deleteAuth ----------

    @Test
    @DisplayName("deleteAuth Positive")
    void deleteAuthPositive() throws DataAccessException {
        AuthData auth = new AuthData("tokenDEL", "user3");

        authDAO.createAuth(auth);

        authDAO.deleteAuth("tokenDEL");

        AuthData result = authDAO.getAuth("tokenDEL");
        assertNull(result);
    }

    @Test
    @DisplayName("deleteAuth Negative (token doesn't exist)")
    void deleteAuthNegative() throws DataAccessException {
        assertDoesNotThrow(() -> {
            authDAO.deleteAuth("fakeToken");
        });
    }

    // ---------- clearAll ----------

    @Test
    @DisplayName("clearAll Positive")
    void clearAllPositive() throws DataAccessException {
        authDAO.createAuth(new AuthData("token1", "user1"));
        authDAO.createAuth(new AuthData("token2", "user2"));

        authDAO.clearAll();

        assertNull(authDAO.getAuth("token1"));
        assertNull(authDAO.getAuth("token2"));
    }
}