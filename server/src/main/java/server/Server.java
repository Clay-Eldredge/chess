package server;

import com.google.gson.Gson;
import dataaccess.*;
import io.javalin.*;
import io.javalin.http.Context;
import service.AlreadyTakenException;
import service.AuthService;
import service.UnauthorizedException;
import service.UserService;
import service.requests.LoginRequest;
import service.requests.LogoutRequest;
import service.requests.RegisterRequest;
import service.results.LoginResult;
import service.results.RegisterResult;

public class Server {

    private final Javalin javalin;

    private void applyException(Context ctx, Gson gson, Exception e, int statusCode) {
        ctx.status(statusCode);
        String jsonResponse = gson.toJson(
                new ErrorResponse("Error: " + e.getMessage())
        );
        ctx.result(jsonResponse);
    }

    public Server() {
        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        UserDAO userDAO = new MemoryUserDAO();
        AuthDAO authDAO = new MemoryAuthDAO();

        Gson gson = new Gson();

        // Register
        javalin.post("/user", ctx -> {
            RegisterRequest registerRequest = gson.fromJson(ctx.body(), RegisterRequest.class);

            UserService userService = new UserService(userDAO, authDAO);

            try {
                RegisterResult result = userService.register(registerRequest);
                ctx.status(200);
                ctx.result(gson.toJson(result));
            } catch (AlreadyTakenException e) {
                applyException(ctx, gson, e, 403);
            } catch (DataAccessException e) {
                applyException(ctx, gson, e, 500);
            }
        });

        // Login
        javalin.post("/session", ctx -> {
            LoginRequest loginRequest = gson.fromJson(ctx.body(), LoginRequest.class);

            AuthService authService = new AuthService(userDAO, authDAO);

            try {
                LoginResult loginResult = authService.login(loginRequest);

                ctx.status(200);
                ctx.result(gson.toJson(loginResult));
            } catch (DataAccessException e) {
                applyException(ctx, gson, e, 500);
            } catch (UnauthorizedException e) {
                applyException(ctx, gson, e, 400);
            }
        });

        // Logout
        javalin.delete("/session", ctx -> {
            LogoutRequest logoutRequest = gson.fromJson(ctx.body(), LogoutRequest.class);

            AuthService authService = new AuthService(userDAO, authDAO);

            try {
                authService.logout(logoutRequest);

                ctx.status(200);
                ctx.result();
            } catch (DataAccessException e) {
                applyException(ctx, gson, e, 500);
            }
        });
    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}
