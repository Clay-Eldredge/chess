package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.UserDAO;
import model.AuthData;
import model.UserData;
import service.requests.RegisterRequest;
import service.results.RegisterResult;

import java.util.Objects;
import java.util.UUID;

public class UserService {
    private final UserDAO userDAO;
    private final AuthDAO authDAO;

    public UserService(UserDAO userDAO, AuthDAO authDAO) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
    }

    public RegisterResult register(RegisterRequest request) throws DataAccessException {
        UserData user = userDAO.getUser(request.username());

        if (user == null) {
            // Create user
            user = userDAO.createUser(new UserData(request.username(), request.password(), request.email()));

            // Create auth
            String token = UUID.randomUUID().toString();
            AuthData authData = authDAO.createAuth(new AuthData(token, user.username()));

            return new RegisterResult(user.username(), authData.authToken());
        } else {
            // Cancel user creation
            throw new AlreadyTakenException("Username or email already taken.");
        }
    }
}
