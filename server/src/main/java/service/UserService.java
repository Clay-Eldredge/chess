package service;

import dataaccess.AlreadyTakenException;
import dataaccess.DataAccessException;
import dataaccess.UserDAO;
import model.UserData;
import service.requests.RegisterRequest;
import service.results.RegisterResult;

import java.util.Objects;

public class UserService {
    private final UserDAO userDAO;

    public UserService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public RegisterResult register(RegisterRequest request) {
        try {
            UserData user = userDAO.getUser(request.username());
            if (user == null || Objects.equals(user.email(), request.email())) {
                // Create user
                user = userDAO.createUser(new UserData(request.username(), request.password(), request.email()));

                // Create auth

            } else {
                // Cancel user creation
                throw new AlreadyTakenException("Username or email already taken.");
            }
        } catch (DataAccessException e) {
            System.out.println("caught it");
        }
    }
}
