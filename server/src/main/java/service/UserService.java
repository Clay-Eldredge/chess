package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.UserDAO;
import model.AuthData;
import model.UserData;
import service.requests.RegisterRequest;
import service.results.RegisterResult;

import java.util.Objects;

public class UserService {
    private final UserDAO userDAO;

    public UserService(UserDAO userDAO, AuthDAO authDAO) {
        this.userDAO = userDAO;
    }

    public RegisterResult register(RegisterRequest request) {
        UserData user = userDAO.getUser(request.username());
        if (user == null || Objects.equals(user.email(), request.email())) {
            // Create user
            user = userDAO.createUser(new UserData(request.username(), request.password(), request.email()));

            // Create auth
            AuthData authData =

            return new RegisterResult(user.username(),)
        } else {
            // Cancel user creation
            throw new AlreadyTakenException("Username or email already taken.");
        }
    }
}
