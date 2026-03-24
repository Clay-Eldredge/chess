package client;

import model.AuthData;

public class ServerFacade {
    private final String serverUrl;

    public ServerFacade(int port) {
        this.serverUrl = "http://localhost:" + port;
    }

    public AuthData register() {

        return null;
    }

    public AuthData login() {

        return null;
    }

    private <T> T makeRequest(String method, String path, Object request, Class<T> responseClass) {

    }
}
