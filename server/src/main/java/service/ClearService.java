package service;

import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import dataaccess.UserDAO;

public class ClearService {
    public void clearAll(UserDAO userDAO, AuthDAO authDAO, GameDAO gameDAO) {
        userDAO.clearAll();
        authDAO.clearAll();
        gameDAO.clearAll();
    }
}
