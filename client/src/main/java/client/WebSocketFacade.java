package client;

import com.google.gson.Gson;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;

import jakarta.websocket.*;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class WebSocketFacade extends Endpoint {

    private Session session;
    private final ChessClient chessClient;
    private final Gson gson = new Gson();

    public WebSocketFacade(String url, ChessClient chessClient) throws ResponseException {
        try {
            url = url.replace("http", "ws");
            URI socketURI = new URI(url + "/ws");
            System.out.println(socketURI);

            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            this.session = container.connectToServer(this, socketURI);
            this.chessClient = chessClient;

            this.session.addMessageHandler(new MessageHandler.Whole<String>() {
                @Override
                public void onMessage(String message) {
                    handleMessage(message);
                }
            });
        } catch (DeploymentException | IOException | URISyntaxException ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }

    private void handleMessage(String message) {
        ServerMessage base = gson.fromJson(message, ServerMessage.class);

        switch (base.getServerMessageType()) {
            case LOAD_GAME -> {
                LoadGameMessage load =
                        gson.fromJson(message, LoadGameMessage.class);
                chessClient.notify(load);
            }
            case ERROR -> {
                ErrorMessage error =
                        gson.fromJson(message, ErrorMessage.class);
                chessClient.notify(error);
            }
            case NOTIFICATION -> {
                NotificationMessage note =
                        gson.fromJson(message, NotificationMessage.class);
                chessClient.notify(note);
            }
        }
    }

    @Override
    public void onOpen(Session session, EndpointConfig config) {
    }

    public void makeMove(String authToken, int gameId, String from, String to) throws ResponseException {
        sendCommand(new MakeMoveCommand(authToken, gameId, from, to));
    }

    public void resign(String authToken, int gameId) throws ResponseException {
        sendCommand(new UserGameCommand(UserGameCommand.CommandType.RESIGN, authToken, gameId));
    }

    public void leaveGame(String authToken, int gameId) throws ResponseException {
        sendCommand(new UserGameCommand(UserGameCommand.CommandType.LEAVE, authToken, gameId));
    }

    public void connect(String authToken, int gameId) throws ResponseException {
        sendCommand(new UserGameCommand(
                UserGameCommand.CommandType.CONNECT,
                authToken,
                gameId
        ));
    }

    private void sendCommand(UserGameCommand command) throws ResponseException {
        try {
            session.getBasicRemote().sendText(gson.toJson(command));
        } catch (IOException ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }
}