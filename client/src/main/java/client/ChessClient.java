package client;

import chess.ChessGame;
import model.GameData;
import results.*;
import ui.EscapeSequences;

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
    private PaintBoard paintBoard = new PaintBoard();

    public ChessClient(String serverUrl) {
        server = new ServerFacade(serverUrl);
        this.serverUrl = serverUrl;

    }

    public void printPrompt() {
        System.out.print(EscapeSequences.RESET_TEXT_COLOR + ">>> " + EscapeSequences.SET_TEXT_COLOR_GREEN);
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

        server.join(game.gameID(), color, authToken);

        state = State.IN_GAME;

        paintBoard.paint(new ChessGame(), color);

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

        state = State.OBSERVING_GAME;

        paintBoard.paint(new ChessGame(), ChessGame.TeamColor.WHITE);

        return "Observing game " + game.gameName();
    }

    public String redraw(String[] params) {
        if (params.length != 0) {
            throw new ResponseException(400, "Expected no args");
        }
        if (state != State.IN_GAME && state != State.OBSERVING_GAME) {
            throw new ResponseException(400, "You must be observing or playing a game");
        }

        return "string";
    }

    public String leave(String[] params) {
        if (params.length != 0) {
            throw new ResponseException(400, "Expected no args");
        }
        if (state != State.IN_GAME && state != State.OBSERVING_GAME) {
            throw new ResponseException(400, "You must be observing or playing a game");
        }


        state = State.LOGGED_IN;

        return "string";
    }

    public String move(String[] params) {
        if (params.length != 1) {
            throw new ResponseException(400, "Expected no args");
        }
        if (state != State.IN_GAME && state != State.OBSERVING_GAME) {
            throw new ResponseException(400, "You must be observing or playing a game");
        }

        return "string";
    }

    public String resign(String[] params) {
        if (params.length != 0) {
            throw new ResponseException(400, "Expected no args");
        }
        if (state != State.IN_GAME) {
            throw new ResponseException(400, "You must be playing a game");
        }

        return "string";
    }

    public String highlight(String[] params) {
        if (params.length != 2) {
            throw new ResponseException(400, "Expected: <letter coordinate> <number coordinate>");
        }
        if (state != State.IN_GAME && state != State.OBSERVING_GAME) {
            throw new ResponseException(400, "You must be observing or playing a game");
        }

        return "string";
    }
}
