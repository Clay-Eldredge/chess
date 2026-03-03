package service;

import chess.ChessGame;
import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import dataaccess.UserDAO;
import model.AuthData;
import model.GameData;
import service.requests.CreateRequest;
import service.requests.JoinRequest;
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

    private AuthData checkAuth(String authToken) throws DataAccessException {
        if (authToken == null) {
            throw new UnauthorizedException("User is not authorized");
        }

        AuthData authData = authDAO.getAuth(authToken);

        if (authData == null) {
            throw new UnauthorizedException("User is not authorized");
        }

        return authData;
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

    public void joinGame(String authToken, JoinRequest joinRequest) throws DataAccessException {
        if (joinRequest.gameID() == null || joinRequest.playerColor() == null) {
            throw new BadRequestException("Bad request");
        }

        AuthData authData = checkAuth(authToken);
        String username = authData.username();

        GameData gameData = gameDAO.getGame(joinRequest.gameID());

        if (gameData == null) {
            throw new DataAccessException("Trying to join game that doesn't exist");
        }

        if (joinRequest.playerColor() == ChessGame.TeamColor.BLACK
                && gameData.blackUsername() != null) {
            throw new AlreadyTakenException("Spot is already filled");
        }

        if (joinRequest.playerColor() == ChessGame.TeamColor.WHITE
                && gameData.whiteUsername() != null) {
            throw new AlreadyTakenException("Spot is already filled");
        }

        GameData updatedGame = new GameData(
                gameData.gameID(),
                joinRequest.playerColor() == ChessGame.TeamColor.WHITE ? username : gameData.whiteUsername(),
                joinRequest.playerColor() == ChessGame.TeamColor.BLACK ? username : gameData.blackUsername(),
                gameData.gameName(),
                gameData.game()
        );

        gameDAO.updateGame(gameData.gameID(), updatedGame);
    }
}
