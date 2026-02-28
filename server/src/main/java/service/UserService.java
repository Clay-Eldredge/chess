package service;

import dataaccess.MemoryUserDAO;
import dataaccess.UserDAO;
import service.requests.RegisterRequest;
import service.results.RegisterResult;

public class UserService {


    public RegisterResult register(RegisterRequest request) {
        UserDAO userDAO = new MemoryUserDAO();
    }
}
