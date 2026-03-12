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

    public Server() {
        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        try {
            DatabaseManager.createDatabase();
            MySqlDAO mySqlDAO = new MySqlDAO();
            System.out.println("DB CREATED");
        } catch (DataAccessException e) {
            System.out.println("DB not created");
        }

        UserDAO userDAO = new DBUserDAO();
        AuthDAO authDAO = new DBAuthDAO();
        GameDAO gameDAO = new DBGameDAO();
        Gson gson = new Gson();

        register(userDAO, authDAO, gson);
        login(userDAO, authDAO, gson);
        logout(userDAO, authDAO, gson);
        clear(userDAO, authDAO, gameDAO);
        createGame(authDAO, gameDAO, gson);
        listGames(authDAO, gameDAO, gson);
        joinGame(authDAO, gameDAO, gson);
    }

    private void applyException(Context ctx, Gson gson, Exception e, int statusCode) {
        ctx.status(statusCode);
        ctx.result(gson.toJson(new ErrorResponse("Error: " + e.getMessage())));
    }

    private void register(UserDAO userDAO, AuthDAO authDAO, Gson gson) {
        javalin.post("/user", ctx -> {
            RegisterRequest request = gson.fromJson(ctx.body(), RegisterRequest.class);
            UserService service = new UserService(userDAO, authDAO);
            try {
                RegisterResult result = service.register(request);
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
    }

    private void login(UserDAO userDAO, AuthDAO authDAO, Gson gson) {
        javalin.post("/session", ctx -> {
            LoginRequest request = gson.fromJson(ctx.body(), LoginRequest.class);
            AuthService service = new AuthService(userDAO, authDAO);
            try {
                LoginResult result = service.login(request);
                ctx.status(200);
                ctx.result(gson.toJson(result));
            } catch (DataAccessException e) {
                applyException(ctx, gson, e, 500);
            } catch (UnauthorizedException e) {
                applyException(ctx, gson, e, 401);
            } catch (BadRequestException e) {
                applyException(ctx, gson, e, 400);
            }
        });
    }

    private void logout(UserDAO userDAO, AuthDAO authDAO, Gson gson) {
        javalin.delete("/session", ctx -> {
            LogoutRequest request = new LogoutRequest(ctx.header("authorization"));
            AuthService service = new AuthService(userDAO, authDAO);
            try {
                service.logout(request);
                ctx.status(200);
                ctx.result("{}");
            } catch (DataAccessException e) {
                applyException(ctx, gson, e, 500);
            } catch (UnauthorizedException e) {
                applyException(ctx, gson, e, 401);
            }
        });
    }

    private void clear(UserDAO userDAO, AuthDAO authDAO, GameDAO gameDAO) {
        javalin.delete("/db", ctx -> {
            ClearService service = new ClearService(userDAO, authDAO, gameDAO);
            service.clearAll();
        });
    }

    private void createGame(AuthDAO authDAO, GameDAO gameDAO, Gson gson) {
        javalin.post("/game", ctx -> {
            CreateRequest request = gson.fromJson(ctx.body(), CreateRequest.class);
            String authToken = ctx.header("authorization");
            GameService service = new GameService(authDAO, gameDAO);
            try {
                CreateResult result = service.createGame(authToken, request);
                ctx.status(200);
                ctx.result(gson.toJson(result));
            } catch (UnauthorizedException e) {
                applyException(ctx, gson, e, 401);
            } catch (BadRequestException e) {
                applyException(ctx, gson, e, 400);
            } catch (DataAccessException e) {
                applyException(ctx, gson, e, 500);
            }
        });
    }

    private void listGames(AuthDAO authDAO, GameDAO gameDAO, Gson gson) {
        javalin.get("/game", ctx -> {
            String authToken = ctx.header("authorization");
            GameService service = new GameService(authDAO, gameDAO);
            try {
                ListResult result = service.listGames(authToken);
                ctx.status(200);
                ctx.result(gson.toJson(result));
            } catch (UnauthorizedException e) {
                applyException(ctx, gson, e, 401);
            } catch (BadRequestException e) {
                applyException(ctx, gson, e, 400);
            } catch (DataAccessException e) {
                applyException(ctx, gson, e, 500);
            }
        });
    }

    private void joinGame(AuthDAO authDAO, GameDAO gameDAO, Gson gson) {
        javalin.put("/game", ctx -> {
            JoinRequest request = gson.fromJson(ctx.body(), JoinRequest.class);
            String authToken = ctx.header("authorization");
            GameService service = new GameService(authDAO, gameDAO);
            try {
                service.joinGame(authToken, request);
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