package client;

import java.util.Arrays;

public class ChessClient {
    private final ServerFacade server;
    private final String serverUrl;
    private State state = State.LOGGED_OUT;

    public ChessClient(String serverUrl) {
        server = new ServerFacade(serverUrl);
        this.serverUrl = serverUrl;

    }

    public void printPrompt() {
        System.out.println("PROMPT");
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

        }
        throw new ResponseException(400, "Expected: <username> <password>");
    }

    public String register(String[] params) {
        return null;
    }

    public String logout() {
        return null;
    }

    public String create(String[] params) {
        return null;
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
