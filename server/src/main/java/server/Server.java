package server;

import com.google.gson.Gson;
import dataaccess.*;
import io.javalin.*;
import io.javalin.http.Context;
import service.*;
import service.requests.*;
import service.results.CreateResult;
import service.results.ListResult;
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
        GameDAO gameDAO = new MemoryGameDAO();

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
            } catch (BadRequestException e) {
                applyException(ctx, gson, e, 400);
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
                applyException(ctx, gson, e, 401);
            } catch (BadRequestException e) {
                applyException(ctx, gson, e, 400);
            }
        });

        // Logout
        javalin.delete("/session", ctx -> {
            LogoutRequest logoutRequest = new LogoutRequest(ctx.header("authorization"));

            AuthService authService = new AuthService(userDAO, authDAO);

            try {
                authService.logout(logoutRequest);

                ctx.status(200);
                ctx.result("{}");
            } catch (DataAccessException e) {
                applyException(ctx, gson, e, 500);
            } catch (UnauthorizedException e) {
                applyException(ctx, gson, e, 401);
            }
        });

        // Clear DB
        javalin.delete("/db", ctx -> {
            userDAO.clearAll();
            authDAO.clearAll();
            gameDAO.clearAll();
        });

        // Create game
        javalin.post("/game", ctx -> {
            CreateRequest createRequest = gson.fromJson(ctx.body(), CreateRequest.class);
            String authToken = ctx.header("authorization");

            GameService gameService = new GameService(authDAO, gameDAO);

            try {
                CreateResult createResult = gameService.createGame(authToken, createRequest);

                ctx.status(200);
                ctx.result(gson.toJson(createResult));
            } catch (UnauthorizedException e) {
                applyException(ctx, gson, e, 401);
            } catch (BadRequestException e) {
                applyException(ctx, gson, e, 400);
            } catch (DataAccessException e) {
                applyException(ctx, gson, e, 500);
            }
        });

        // Create game
        javalin.get("/game", ctx -> {
            String authToken = ctx.header("authorization");

            GameService gameService = new GameService(authDAO, gameDAO);

            try {
                ListResult listResult = gameService.listGames(authToken);

                ctx.status(200);
                ctx.result(gson.toJson(listResult));
            } catch (UnauthorizedException e) {
                applyException(ctx, gson, e, 401);
            } catch (BadRequestException e) {
                applyException(ctx, gson, e, 400);
            } catch (DataAccessException e) {
                applyException(ctx, gson, e, 500);
            }
        });

        // Join game
        javalin.put("/game", ctx -> {
            JoinRequest joinRequest = gson.fromJson(ctx.body(), JoinRequest.class);
            String authToken = ctx.header("authorization");

            GameService gameService = new GameService(authDAO, gameDAO);

            try {
                gameService.joinGame(authToken, joinRequest);

                ctx.status(200);
                ctx.result("{}");
            } catch (UnauthorizedException e) {
                applyException(ctx, gson, e, 401);
            } catch (BadRequestException e) {
                applyException(ctx, gson, e, 400);
            } catch (DataAccessException e) {
                applyException(ctx, gson, e, 500);
            } catch (AlreadyTakenException e) {
                applyException(ctx, gson, e, 403);
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
