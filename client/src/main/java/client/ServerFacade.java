package client;

import com.google.gson.Gson;
import requests.CreateRequest;
import requests.LoginRequest;
import requests.LogoutRequest;
import requests.RegisterRequest;
import results.CreateResult;
import results.LoginResult;
import results.RegisterResult;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

public class ServerFacade {
    private final String serverUrl;

    public ServerFacade(String url) {
        this.serverUrl = url;
    }

    public RegisterResult register(String username, String password, String email) {
        var request = new RegisterRequest(username, password, email);
        return makeRequest("POST", "/user", request, RegisterResult.class);
    }

    public LoginResult login(String username, String password) {
        var request = new LoginRequest(username, password);
        return makeRequest("POST", "/session", request, LoginResult.class);
    }

    public void logout(String authToken) {
        var request = new LogoutRequest(authToken);
        makeRequest("DELETE", "/session", null, null, authToken);
    }

    public CreateResult create(String gameName, String authToken) {
        var request = new CreateRequest(gameName);
        return makeRequest("POST", "/game", request, CreateResult.class, authToken);
    }

    private <T> T makeRequest(String method, String path, Object request, Class<T> responseClass) {
        return makeRequest(method, path, request, responseClass, null);
    }
    private <T> T makeRequest(String method, String path, Object request, Class<T> responseClass, String authToken) {
        try{
            URL url = (new URI(serverUrl + path)).toURL();
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod(method);
            http.setDoOutput(true);

            if (authToken != null) {
                http.addRequestProperty("authorization", authToken);
            }

            writeBody(request, http);
            http.connect();
            throwIfNotSuccessful(http);
            return readBody(http, responseClass);
        } catch (Exception e) {
            throw new ResponseException(500, e.getMessage());
        }
    }

    private boolean isSuccessful(int status) {
        return status >= 200 && status < 300;
    }

    private void throwIfNotSuccessful(HttpURLConnection http) throws IOException, ResponseException {
        var status = http.getResponseCode();
        if (!isSuccessful(status)) {
            throw new ResponseException(status, "failure: " + status);
        }
    }

    private static void writeBody(Object request, HttpURLConnection http) throws IOException {
        if (request != null) {
            http.addRequestProperty("Content-Type", "application/json");
            String reqData = new Gson().toJson(request);
            try (OutputStream reqBody = http.getOutputStream()){
                reqBody.write(reqData.getBytes());
            }
        }
    }

    private static <T> T readBody(HttpURLConnection http, Class<T> responseClass) throws IOException {
        T response = null;
        try (InputStream respBody = http.getInputStream()) {
            InputStreamReader reader = new InputStreamReader(respBody);
            if (responseClass != null) {
                response = new Gson().fromJson(reader, responseClass);
            }
        }
        return response;
    }
}
