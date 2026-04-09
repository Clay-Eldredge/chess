package client;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;
import results.*;
import ui.EscapeSequences;
import websocket.commands.MakeMoveCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChessClient {
    private final ServerFacade server;
    private final String serverUrl;
    private String authToken;
    private String username;
    private List<GameInfo> lastListedGames = new ArrayList<>();
    private State state = State.LOGGED_OUT;
    private WebSocketFacade ws;
    private int currentGameId;
    private ChessGame.TeamColor myColor;
    private ChessGame currentGame;
    private PaintBoard paintBoard = new PaintBoard();

    public ChessClient(String serverUrl) {
        server = new ServerFacade(serverUrl);
        this.serverUrl = serverUrl;
    }

    public void printPrompt() {
        System.out.print(EscapeSequences.RESET_TEXT_COLOR + ">>> " + EscapeSequences.SET_TEXT_COLOR_GREEN);
    }

    public void openWebSocketConnection(int gameId) {
        try {
            // open websocket connection
            ws = new WebSocketFacade(serverUrl, this);

            // send the initial CONNECT command
            ws.connect(authToken, gameId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void notify(ServerMessage message) {
        switch (message.getServerMessageType()) {
            case LOAD_GAME -> {
                LoadGameMessage load = (LoadGameMessage) message;
                currentGame = load.getGame().game();
                paintBoard.paint(currentGame, myColor);
            }
            case NOTIFICATION -> {
                NotificationMessage note = (NotificationMessage) message;
                System.out.println(note.getMessage());
            }
            case ERROR -> {
                ErrorMessage error = (ErrorMessage) message;
                System.out.println(error.getErrorMessage());
            }
        }
    }

    public String eval(String input) {
        try {
            String[] tokens = input.trim().split("\\s+");
            String cmd = (tokens.length > 0) ? tokens[0] : "help";
            String[] params = Arrays.copyOfRange(tokens, 1, tokens.length);
            return switch (cmd) {
                case "login" -> login(params);
                case "register" -> register(params);
                case "logout" -> logout();
                case "create" -> create(params);
                case "list" -> list();
                case "join" -> join(params);
                case "observe" -> observe(params);
                case "move" -> move(params);
                case "highlight" -> highlight(params);
                case "redraw" -> redraw(params);
                case "resign" -> resign(params);
                case "leave" -> leave(params);
                case "quit" -> "quit";
                default -> help();
            };
        } catch (ResponseException ex) {
            return ex.getMessage();
        }
    }

    public String help() {
        if (this.state.equals(State.LOGGED_OUT)) {
            return """
register <username> <password> <email> - to create an account
login <username> <password> - to play chess
quit - playing chess
help - see commands list
                    """;
        } else if (this.state.equals(State.LOGGED_IN)) {
            return """
create <name> - create a game
list - see all games
join <number> [white|black] - play chess
observe <number> - watch a game
logout - when you are done
quit - playing chess
help - see commands list
                    """;
        } else if (this.state.equals(State.IN_GAME)) {
            return """
redraw - paint the board
leave - exit this game
move <letter coordinate 1> <number coordinate 1> <letter coordinate 2> <number coordinate 2> - move a piece
resign - forfeit the game
highlight <letter coordinate> <number coordinate> - show the legal moves for a piece
help - see commands list
                    """;
        } else if (this.state.equals(State.OBSERVING_GAME)) {
            return """
redraw - paint the board
leave - exit this game
highlight <letter coordinate> <number coordinate> - show the legal moves for a piece
help - see commands list
                    """;
        } else {
            return """
client has no state
                    """;
        }
    }

    public String login(String[] params) {
        if (state != State.LOGGED_OUT) {
            throw new ResponseException(400, "Already logged in!");
        }
        if (params.length == 2) {
            LoginResult result = server.login(params[0], params[1]);
            this.state = State.LOGGED_IN;
            this.authToken = result.authToken();
            this.username = result.username();
            return "Successfully logged in as " + this.username;
        }
        throw new ResponseException(400, "Expected: <username> <password>");
    }

    public String register(String[] params) {
        if (state != State.LOGGED_OUT) {
            throw new ResponseException(400, "Already logged in!");
        }
        if (params.length == 3) {
            RegisterResult result = server.register(params[0], params[1], params[2]);
            this.state = State.LOGGED_IN;
            this.authToken = result.authToken();
            this.username = result.username();
            return "Successfully registered as " + this.username;
        }
        throw new ResponseException(400, "Expected: <username> <password> <email>");
    }

    public String logout() {
        if (state != State.LOGGED_IN) {
            throw new ResponseException(400, "You must log in first");
        }
        server.logout(authToken);
        this.state = State.LOGGED_OUT;
        this.authToken = null;
        this.username = null;
        return "Successfully logged out";
    }

    public String create(String[] params) {
        if (state != State.LOGGED_IN) {
            throw new ResponseException(400, "You must log in first");
        }
        if (params.length == 1) {
            CreateResult result = server.create(params[0], authToken);
            return "Successfully created new game: " + params[0];
        }
        throw new ResponseException(400, "Expected: <name>");
    }

    public String list() {
        if (state != State.LOGGED_IN) {
            throw new ResponseException(400, "You must log in first");
        }
        ListResult result = server.list(authToken);

        lastListedGames = Arrays.asList(result.games());

        if (lastListedGames.isEmpty()) {
            return "No games available";
        }

        StringBuilder output = new StringBuilder();

        for (int i = 0; i < lastListedGames.size(); i++) {
            var game = lastListedGames.get(i);

            output.append(i + 1)
                    .append(". ")
                    .append(game.gameName())
                    .append(" | White: ")
                    .append(game.whiteUsername() == null ? "-" : game.whiteUsername())
                    .append(" | Black: ")
                    .append(game.blackUsername() == null ? "-" : game.blackUsername())
                    .append("\n");
        }

        return output.toString();
    }

    public String join(String[] params) {
        if (state != State.LOGGED_IN) {
            throw new ResponseException(400, "You must log in first");
        }

        if (params.length != 2) {
            throw new ResponseException(400, "Expected: <game number> <WHITE|BLACK>");
        }

        int index;
        try {
            index = Integer.parseInt(params[0]) - 1;
        } catch (NumberFormatException e) {
            throw new ResponseException(400, "Game number must be an integer");
        }

        if (index < 0 || index >= lastListedGames.size()) {
            throw new ResponseException(400, "Invalid game number");
        }

        var game = lastListedGames.get(index);

        ChessGame.TeamColor color;

        try {
            color = ChessGame.TeamColor.valueOf(params[1].toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseException(400, "Color must be WHITE or BLACK");
        }

        if ((game.blackUsername()!=null && color.equals(ChessGame.TeamColor.BLACK))
            ||(game.whiteUsername()!=null && color.equals(ChessGame.TeamColor.WHITE))) {
            throw new ResponseException(400, "Color already taken!");
        }

        int curGameId = game.gameID();

        server.join(curGameId, color, authToken);

        this.currentGameId = curGameId;
        this.myColor = color;
        this.currentGame = new ChessGame();

        openWebSocketConnection(currentGameId);

        state = State.IN_GAME;

        return "Joined game " + game.gameName() + " as " + color;
    }

    public String observe(String[] params) {
        if (params.length != 1) {
            throw new ResponseException(400, "Expected: <game number>");
        }
        if (state != State.LOGGED_IN) {
            throw new ResponseException(400, "You must log in first");
        }

        int index;
        try {
            index = Integer.parseInt(params[0]) - 1;
        } catch (NumberFormatException e) {
            throw new ResponseException(400, "Game number must be an integer");
        }

        if (index < 0 || index >= lastListedGames.size()) {
            throw new ResponseException(400, "Invalid game number");
        }

        var game = lastListedGames.get(index);

        server.list(authToken);

        this.currentGameId = game.gameID();
        this.currentGame = new ChessGame();

        openWebSocketConnection(currentGameId);

        state = State.OBSERVING_GAME;

        return "Observing game " + game.gameName();
    }

    public String redraw(String[] params) {
        if (params.length != 0) {
            throw new ResponseException(400, "Expected no args");
        }
        if (state != State.IN_GAME && state != State.OBSERVING_GAME) {
            throw new ResponseException(400, "You must be observing or playing a game");
        }

        paintBoard.paint(this.currentGame, this.myColor);

        return "";
    }

    public String leave(String[] params) {
        if (params.length != 0) {
            throw new ResponseException(400, "Expected no args");
        }
        if (state != State.IN_GAME && state != State.OBSERVING_GAME) {
            throw new ResponseException(400, "You must be observing or playing a game");
        }

        ws.leaveGame(authToken, currentGameId);

        state = State.LOGGED_IN;

        return "Left game";
    }

    private ChessPosition getPositionFromCoordinates(String letter, String number) {
        if (letter == null || number == null) {
            return null;
        }

        letter = letter.toLowerCase();

        int col = letter.charAt(0) - 'a' + 1;
        int row = Integer.parseInt(number);

        return new ChessPosition(row, col);
    }

    private ChessPiece.PieceType parsePromotion(String input) {
        return switch (input.toLowerCase()) {
            case "queen", "q" -> ChessPiece.PieceType.QUEEN;
            case "rook", "r" -> ChessPiece.PieceType.ROOK;
            case "bishop", "b" -> ChessPiece.PieceType.BISHOP;
            case "knight", "n" -> ChessPiece.PieceType.KNIGHT;
            default -> throw new ResponseException(400, "Invalid promotion piece");
        };
    }

    public String move(String[] params) {
        if (params.length != 4 && params.length != 5) {
            throw new ResponseException(400,
                    "Expected <fromCol> <fromRow> <toCol> <toRow> [promotion]");
        }

        if (state != State.IN_GAME) {
            throw new ResponseException(400, "You must be observing or playing a game");
        }

        ChessPosition from = getPositionFromCoordinates(params[0], params[1]);
        ChessPosition to = getPositionFromCoordinates(params[2], params[3]);

        ChessPiece.PieceType promotion = null;

        if (params.length == 5) {
            promotion = parsePromotion(params[4]);
        }

        ChessMove move = new ChessMove(from, to, promotion);

        ws.makeMove(authToken, currentGameId, move);

        return "Move sent: " + params[0] + params[1] + " -> " + params[2] + params[3] +
                (promotion != null ? " promoting to " + promotion : "");
    }

    public String resign(String[] params) {
        if (params.length != 0) {
            throw new ResponseException(400, "Expected no args");
        }
        if (state != State.IN_GAME) {
            throw new ResponseException(400, "You must be playing a game");
        }

        ws.resign(authToken, currentGameId);

        return "string";
    }

    public String highlight(String[] params) {
        if (params.length != 2) {
            throw new ResponseException(400, "Expected: <letter coordinate> <number coordinate>");
        }
        if (state != State.IN_GAME && state != State.OBSERVING_GAME) {
            throw new ResponseException(400, "You must be observing or playing a game");
        }

        paintBoard.paintLegalMoves(currentGame, getPositionFromCoordinates(params[0], params[1]), myColor);

        return "string";
    }
}
