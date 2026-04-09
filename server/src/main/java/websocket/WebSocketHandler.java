package websocket;

import com.google.gson.Gson;
import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import io.javalin.websocket.*;
import model.AuthData;
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
            UserGameCommand cmd = gson.fromJson(ctx.message(), UserGameCommand.class);

            switch (cmd.getCommandType()) {
                case CONNECT -> connect(ctx, cmd);
                case MAKE_MOVE -> makeMove(ctx, cmd);
                case RESIGN -> resign(ctx, cmd);
                case LEAVE -> leave(ctx, cmd);
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

    private void connect(WsMessageContext ctx, UserGameCommand cmd) throws Exception {
        AuthData auth = authDAO.getAuth(cmd.getAuthToken());
        if (auth == null) {
            sendError(ctx, "unauthorized");
            return;
        }

        String username = auth.username();
        int gameId = cmd.getGameID();

        connections.add(gameId, ctx.session);
        sessionGameMap.put(ctx.session, gameId);

        var gameData = gameDAO.getGame(gameId);

        ctx.send(gson.toJson(new LoadGameMessage(gameData)));

        connections.broadcast(gameId, ctx.session,
                new NotificationMessage(username + " joined the game"));
    }

    private void makeMove(WsMessageContext ctx, UserGameCommand cmd) throws Exception {
        AuthData auth = authDAO.getAuth(cmd.getAuthToken());
        if (auth == null) {
            sendError(ctx, "unauthorized");
            return;
        }

        String username = auth.username();
        int gameId = cmd.getGameID();

        var gameData = gameDAO.getGame(gameId);
        var game = gameData;

        MakeMoveCommand moveCmd = (MakeMoveCommand) cmd;

        // TODO: validate turn + legality

        gameDAO.updateGame(gameId, gameData);

        connections.broadcast(gameId, null,
                new LoadGameMessage(game));

        connections.broadcast(gameId, null,
                new NotificationMessage(username + " made a move"));
    }

    private void resign(WsMessageContext ctx, UserGameCommand cmd) throws Exception {
        AuthData auth = authDAO.getAuth(cmd.getAuthToken());
        if (auth == null) {
            sendError(ctx, "unauthorized");
            return;
        }

        String username = auth.username();
        int gameId = cmd.getGameID();

        var gameData = gameDAO.getGame(gameId);

        // TODO: mark game as resigned
        gameDAO.updateGame(gameId, gameData);

        connections.broadcast(gameId, null,
                new NotificationMessage(username + " resigned"));

        connections.broadcast(gameId, null,
                new LoadGameMessage(gameData));
    }

    private void leave(WsMessageContext ctx, UserGameCommand cmd) throws Exception {
        AuthData auth = authDAO.getAuth(cmd.getAuthToken());
        if (auth == null) {
            sendError(ctx, "unauthorized");
            return;
        }

        String username = auth.username();
        int gameId = cmd.getGameID();

        connections.remove(gameId, ctx.session);
        sessionGameMap.remove(ctx.session);

        connections.broadcast(gameId, null,
                new NotificationMessage(username + " left the game"));
    }

    private void sendError(WsMessageContext ctx, String message) {
        try {
            ctx.send(gson.toJson(new ErrorMessage(message)));
        } catch (Exception ignored) {}
    }
}