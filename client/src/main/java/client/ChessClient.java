package client;

public class ChessClient {
    private final ServerFacade server;
    private final String serverUrl;
    private State state = State.loggedOut;

    public ChessClient(String serverUrl) {
        server = new ServerFacade(serverUrl);
        this.serverUrl = serverUrl;

    }
}
