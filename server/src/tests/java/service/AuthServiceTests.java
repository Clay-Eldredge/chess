package service;

import dataaccess.AuthDAO;
import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryUserDAO;
import dataaccess.UserDAO;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.requests.LoginRequest;
import service.requests.LogoutRequest;

import static org.junit.jupiter.api.Assertions.*;

public class AuthServiceTests {

    private UserDAO userDAO;
    private AuthDAO authDAO;
    private AuthService authService;

    @BeforeEach
    public void setup() {
        userDAO = new MemoryUserDAO();
        authDAO = new MemoryAuthDAO();
        authService = new AuthService(userDAO, authDAO);
    }

    @Test
    public void loginPositive() throws Exception {
        UserData user = new UserData("clay", "password", "email@test.com");
        userDAO.createUser(user);

        LoginRequest request = new LoginRequest("clay", "password");

        var result = authService.login(request);

        assertNotNull(result);
        assertEquals("clay", result.username());
        assertNotNull(result.authToken());

        AuthData storedAuth = authDAO.getAuth(result.authToken());
        assertNotNull(storedAuth);
        assertEquals("clay", storedAuth.username());
    }

    @Test
    public void loginWrongPassword() throws Exception {
        UserData user = new UserData("clay", "password", "email@test.com");
        userDAO.createUser(user);

        LoginRequest request = new LoginRequest("clay", "wrongpassword");

        assertThrows(UnauthorizedException.class, () -> {
            authService.login(request);
        });
    }

    @Test
    public void logoutPositive() throws Exception {
        UserData user = new UserData("clay", "password", "email@test.com");
        userDAO.createUser(user);

        AuthData auth = new AuthData("token123", "clay");
        authDAO.createAuth(auth);

        LogoutRequest request = new LogoutRequest("token123");

        authService.logout(request);

        assertNull(authDAO.getAuth("token123"));
    }

    @Test
    public void logoutInvalidToken() {
        LogoutRequest request = new LogoutRequest("doesNotExist");

        assertThrows(UnauthorizedException.class, () -> {
            authService.logout(request);
        });
    }
}