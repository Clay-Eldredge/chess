package service;

import dataaccess.AuthDAO;
import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryUserDAO;
import dataaccess.UserDAO;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.requests.RegisterRequest;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTests {

    private UserDAO userDAO;
    private AuthDAO authDAO;
    private UserService userService;

    @BeforeEach
    public void setup() {
        userDAO = new MemoryUserDAO();
        authDAO = new MemoryAuthDAO();
        userService = new UserService(userDAO, authDAO);
    }

    @Test
    public void registerPositive() throws Exception {
        RegisterRequest request =
                new RegisterRequest("clay", "password", "email@test.com");

        var result = userService.register(request);

        assertNotNull(result);
        assertEquals("clay", result.username());
        assertNotNull(result.authToken());

        UserData storedUser = userDAO.getUser("clay");
        assertNotNull(storedUser);
        assertEquals("password", storedUser.password());
        assertEquals("email@test.com", storedUser.email());

        AuthData storedAuth = authDAO.getAuth(result.authToken());
        assertNotNull(storedAuth);
        assertEquals("clay", storedAuth.username());
    }

    @Test
    public void registerAlreadyTaken() throws Exception {
        userDAO.createUser(new UserData(
                "clay",
                "password",
                "email@test.com"
        ));

        RegisterRequest request =
                new RegisterRequest("clay", "password", "email@test.com");

        assertThrows(AlreadyTakenException.class, () -> {
            userService.register(request);
        });
    }
}
