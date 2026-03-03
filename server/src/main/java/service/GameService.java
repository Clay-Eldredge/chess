package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import model.AuthData;
import model.GameData;
import service.requests.CreateRequest;
import service.results.CreateResult;
import service.results.GameInfo;
import service.results.ListResult;

import java.util.Arrays;

public class GameService {
    private final AuthDAO authDAO;
    private final GameDAO gameDAO;

    public GameService(AuthDAO authDAO, GameDAO gameDAO) {
        this.authDAO = authDAO;
        this.gameDAO = gameDAO;
    }

    private void checkAuth(String authToken) throws DataAccessException {
        if (authToken == null) {
            throw new UnauthorizedException("User is not authorized");
        }

        AuthData authData = authDAO.getAuth(authToken);

        if (authData == null) {
            throw new UnauthorizedException("User is not authorized");
        }
    }

    public CreateResult createGame(String authToken, CreateRequest createRequest) throws DataAccessException {
        if (createRequest.gameName() == null) {
            throw new BadRequestException("Bad request");
        }

        checkAuth(authToken);

        GameData gameData = gameDAO.createGame(createRequest.gameName());

        return new CreateResult(gameData.gameID());
    }

    public ListResult listGames(String authToken) throws DataAccessException {
        checkAuth(authToken);

        GameData[] games = gameDAO.listGames();

        GameInfo[] entries = Arrays.stream(games)
                .map(g -> new GameInfo(
                        g.gameID(),
                        g.whiteUsername(),
                        g.blackUsername(),
                        g.gameName()
                ))
                .toArray(GameInfo[]::new);

        return new ListResult(entries);
    }
}
