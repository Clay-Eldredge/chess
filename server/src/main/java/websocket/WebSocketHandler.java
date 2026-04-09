package websocket;

import chess.ChessGame;
import com.google.gson.Gson;
import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import io.javalin.websocket.*;
import model.AuthData;
import model.GameData;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.*;
import org.eclipse.jetty.websocket.api.Session;

import java.util.concurrent.ConcurrentHashMap;

public class WebSocketHandler implements WsConnectHandler, WsMessageHandler, WsCloseHandler {

    private final ConnectionManager connections = new ConnectionManager();
    private final ConcurrentHashMap<Session, Integer> sessionGameMap = new ConcurrentHashMap<>();
    private final AuthDAO authDAO;
    private final GameDAO gameDAO;
    private final Gson gson = new Gson();

    public WebSocketHandler(AuthDAO authDAO, GameDAO gameDAO) {
        this.authDAO = authDAO;
        this.gameDAO = gameDAO;
    }

    @Override
    public void handleConnect(WsConnectContext ctx) {
        ctx.enableAutomaticPings();
    }

    @Override
    public void handleMessage(WsMessageContext ctx) {
        try {
            String json = ctx.message();

            UserGameCommand base = gson.fromJson(json, UserGameCommand.class);

            switch (base.getCommandType()) {

                case CONNECT -> connect(ctx, base);

                case MAKE_MOVE -> {
                    MakeMoveCommand moveCmd =
                            gson.fromJson(json, MakeMoveCommand.class);
                    makeMove(ctx, moveCmd);
                }

                case RESIGN -> resign(ctx, base);

                case LEAVE -> leave(ctx, base);
            }

        } catch (Exception e) {
            sendError(ctx, e.getMessage());
        }
    }

    @Override
    public void handleClose(WsCloseContext ctx) {
        Integer gameId = sessionGameMap.remove(ctx.session);
        if (gameId != null) {
            connections.remove(gameId, ctx.session);
        }
    }

    private static class RequestContext {
        String username;
        int gameId;
        GameData gameData;

        RequestContext(String username, int gameId, GameData gameData) {
            this.username = username;
            this.gameId = gameId;
            this.gameData = gameData;
        }
    }

    private RequestContext authenticateAndGetGame(WsMessageContext ctx, UserGameCommand cmd) throws Exception {
        AuthData auth = authDAO.getAuth(cmd.getAuthToken());
        if (auth == null) {
            sendError(ctx, "unauthorized");
            return null;
        }

        int gameId = cmd.getGameID();
        GameData gameData = gameDAO.getGame(gameId);

        return new RequestContext(auth.username(), gameId, gameData);
    }

    private void connect(WsMessageContext ctx, UserGameCommand cmd) throws Exception {
        RequestContext req = authenticateAndGetGame(ctx, cmd);
        if (req == null) return;

        String username = req.username;
        int gameId = req.gameId;
        var gameData = req.gameData;

        String white = gameData.whiteUsername();
        String black = gameData.blackUsername();

        String role;
        if (username.equals(white)) {
            role = "WHITE";
        } else if (username.equals(black)) {
            role = "BLACK";
        } else {
            role = "OBSERVER";
        }

        connections.add(gameId, ctx.session);
        sessionGameMap.put(ctx.session, gameId);

        ctx.send(gson.toJson(new LoadGameMessage(gameData)));

        String message = switch (role) {
            case "WHITE" -> username + " joined as white";
            case "BLACK" -> username + " joined as black";
            default -> username + " joined as an observer";
        };

        connections.broadcast(gameId, ctx.session,
                new NotificationMessage(message));
    }

    private void makeMove(WsMessageContext ctx, MakeMoveCommand cmd) throws Exception {
        RequestContext req = authenticateAndGetGame(ctx, cmd);
        if (req == null) return;

        String username = req.username;
        int gameId = req.gameId;
        var gameData = req.gameData;
        var game = gameData.game();

        if (game.isGameEnded()) {
            sendError(ctx, "game already over");
            return;
        }

        String white = gameData.whiteUsername();
        String black = gameData.blackUsername();

        ChessGame.TeamColor playerColor;

        if (username.equals(white)) {
            playerColor = ChessGame.TeamColor.WHITE;
        } else if (username.equals(black)) {
            playerColor = ChessGame.TeamColor.BLACK;
        } else {
            sendError(ctx, "observers cannot make moves");
            return;
        }

        if (game.getTeamTurn() != playerColor) {
            sendError(ctx, "not your turn");
            return;
        }

        try {
            game.makeMove(cmd.getMove());
        } catch (Exception e) {
            sendError(ctx, "invalid move");
            return;
        }

        gameDAO.updateGame(gameId, gameData);

        connections.broadcast(gameId, ctx.session,
                new NotificationMessage(username + " made a move"));

        connections.broadcast(gameId, null,
                new LoadGameMessage(gameData));

        if (game.isInCheckmate(game.getTeamTurn())) {
            connections.broadcast(gameId, null,
                    new NotificationMessage(game.getTeamTurn() + " is in checkmate"));
        } else if (game.isInCheck(game.getTeamTurn())) {
            connections.broadcast(gameId, null,
                    new NotificationMessage(game.getTeamTurn() + " is in check"));
        } else if (game.isInStalemate(game.getTeamTurn())) {
            connections.broadcast(gameId, null,
                    new NotificationMessage("stalemate"));
        }
    }

    private void resign(WsMessageContext ctx, UserGameCommand cmd) throws Exception {
        RequestContext req = authenticateAndGetGame(ctx, cmd);
        if (req == null) return;

        var game = req.gameData.game();

        if (game.isGameEnded()) {
            sendError(ctx, "game already over");
            return;
        }

        String white = req.gameData.whiteUsername();
        String black = req.gameData.blackUsername();

        if (!req.username.equals(white) && !req.username.equals(black)) {
            sendError(ctx, "observers cannot resign");
            return;
        }

        game.setGameEnded();
        gameDAO.updateGame(req.gameId, req.gameData);

        connections.broadcast(req.gameId, null,
                new NotificationMessage(req.username + " resigned"));
    }

    private void leave(WsMessageContext ctx, UserGameCommand cmd) throws Exception {
        RequestContext req = authenticateAndGetGame(ctx, cmd);
        if (req == null) return;

        String white = req.gameData.whiteUsername();
        String black = req.gameData.blackUsername();

        String newWhite = white;
        String newBlack = black;

        if (req.username.equals(white)) {
            newWhite = null;
        } else if (req.username.equals(black)) {
            newBlack = null;
        }

        GameData updatedGame = new GameData(
                req.gameId,
                newWhite,
                newBlack,
                req.gameData.gameName(),
                req.gameData.game()
        );

        gameDAO.updateGame(req.gameId, updatedGame);

        connections.remove(req.gameId, ctx.session);
        sessionGameMap.remove(ctx.session);

        connections.broadcast(req.gameId, null,
                new NotificationMessage(req.username + " left the game"));
    }

    private void sendError(WsMessageContext ctx, String message) {
        try {
            ctx.send(gson.toJson(new ErrorMessage(message)));
        } catch (Exception ignored) {}
    }
}