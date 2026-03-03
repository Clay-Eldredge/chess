package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.UserDAO;
import model.AuthData;
import model.UserData;
import service.requests.LoginRequest;
import service.requests.LogoutRequest;
import service.results.LoginResult;

import java.util.UUID;

public class AuthService {
    private final UserDAO userDAO;
    private final AuthDAO authDAO;

    public AuthService(UserDAO userDAO, AuthDAO authDAO) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
    }

    public LoginResult login(LoginRequest loginRequest) throws DataAccessException {
        if (loginRequest.username() == null || loginRequest.password() == null) {
            throw new BadRequestException("Bad request");
        }

        UserData user = userDAO.getUser(loginRequest.username());

        if (user == null || !user.password().equals(loginRequest.password())) {
            throw new UnauthorizedException("User is not authorized");
        }

        // Create auth
        String token = UUID.randomUUID().toString();
        AuthData authData = authDAO.createAuth(new AuthData(token, user.username()));

        return new LoginResult(user.username(), authData.authToken());
    }

    public void logout(LogoutRequest logoutRequest) throws DataAccessException {
        if (logoutRequest.authToken() == null) {
            throw new UnauthorizedException("User is not authorized");
        }

        AuthData authData = authDAO.getAuth(logoutRequest.authToken());

        if (authData == null) {
            throw new UnauthorizedException("User is not authorized");
        }

        authDAO.deleteAuth(authData.authToken());
    }
}
