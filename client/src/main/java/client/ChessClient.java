package client;

import results.CreateResult;
import results.LoginResult;
import results.RegisterResult;
import ui.EscapeSequences;

import java.util.Arrays;

public class ChessClient {
    private final ServerFacade server;
    private final String serverUrl;
    private String authToken;
    private String username;
    private State state = State.LOGGED_OUT;

    public ChessClient(String serverUrl) {
        server = new ServerFacade(serverUrl);
        this.serverUrl = serverUrl;

    }

    public void printPrompt() {
        System.out.print("\n" + EscapeSequences.RESET_TEXT_COLOR + ">>> " + EscapeSequences.SET_TEXT_COLOR_GREEN);
    }

    public String eval(String input) {
        try {
            String[] tokens = input.toLowerCase().split(" ");
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
                case "quit" -> "quit";
                default -> help();
            };
        } catch (ResponseException ex) {
            return ex.getMessage();
        }
    }

    public String help() {
        if (getState().equals(State.LOGGED_OUT)) {
            return """
Help text appears in many forms...
Like this one.
                    """;
        } else {
            return """
HELP!!! YOU'RE LOGGED IN!!!
                    """;
        }
    }

    public String login(String[] params) {
        if (params.length >= 2) {
            LoginResult result = server.login(params[0], params[1]);
            this.state = State.LOGGED_IN;
            this.authToken = result.authToken();
            this.username = result.username();
            return "Successfully logged in as " + this.username;
        }
        throw new ResponseException(400, "Expected: <username> <password>");
    }

    public String register(String[] params) {
        if (params.length >= 3) {
            RegisterResult result = server.register(params[0], params[1], params[2]);
            this.state = State.LOGGED_IN;
            this.authToken = result.authToken();
            this.username = result.username();
            return "Successfully registered as " + this.username;
        }
        throw new ResponseException(400, "Expected: <username> <password> <email>");
    }

    public String logout() {
        server.logout(authToken);
        this.state = State.LOGGED_OUT;
        this.authToken = null;
        this.username = null;
        return "Successfully logged out";
    }

    public String create(String[] params) {
        if (params.length >= 1) {
            CreateResult result = server.create(params[0], authToken);
            return "Successfully created new game: " + params[0];
        }
        throw new ResponseException(400, "Expected: <name>");
    }

    public String list() {
        return null;
    }

    public String join(String[] params) {
        return null;
    }

    public String observe(String[] params) {
        return null;
    }

    public State getState() {
        return state;
    }
}
