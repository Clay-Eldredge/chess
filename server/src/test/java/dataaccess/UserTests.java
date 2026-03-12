package dataaccess;

import model.UserData;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class UserTests {

    private DBUserDAO userDAO;

    @BeforeEach
    void setup() throws DataAccessException {
        userDAO = new DBUserDAO();
        userDAO.clearAll();
    }

    // ---------- createUser ----------

    @Test
    @DisplayName("createUser Positive")
    void createUserPositive() throws DataAccessException {

        UserData user = new UserData("testUser", "password123", "test@email.com");

        UserData created = userDAO.createUser(user);

        assertNotNull(created);
        assertEquals("testUser", created.username());
        assertEquals("test@email.com", created.email());

        UserData fromDB = userDAO.getUser("testUser");
        assertNotNull(fromDB);

        // password should NOT equal plaintext because bcrypt hashes it
        assertNotEquals("password123", fromDB.password());
    }

    @Test
    @DisplayName("createUser Negative (duplicate username)")
    void createUserNegative() throws DataAccessException {

        UserData user = new UserData("duplicateUser", "pass", "email@test.com");

        userDAO.createUser(user);

        assertThrows(DataAccessException.class, () -> {
            userDAO.createUser(user);
        });
    }

    // ---------- getUser ----------

    @Test
    @DisplayName("getUser Positive")
    void getUserPositive() throws DataAccessException {

        UserData user = new UserData("lookupUser", "password", "lookup@email.com");

        userDAO.createUser(user);

        UserData retrieved = userDAO.getUser("lookupUser");

        assertNotNull(retrieved);
        assertEquals("lookupUser", retrieved.username());
        assertEquals("lookup@email.com", retrieved.email());
    }

    @Test
    @DisplayName("getUser Negative (user does not exist)")
    void getUserNegative() throws DataAccessException {

        UserData result = userDAO.getUser("nonexistent");

        assertNull(result);
    }

    // ---------- clearAll ----------

    @Test
    @DisplayName("clearAll Positive")
    void clearAllPositive() throws DataAccessException {

        userDAO.createUser(new UserData("user1", "pass", "a@email.com"));
        userDAO.createUser(new UserData("user2", "pass", "b@email.com"));

        userDAO.clearAll();

        assertNull(userDAO.getUser("user1"));
        assertNull(userDAO.getUser("user2"));
    }
}