package server;

import com.google.gson.Gson;
import dataaccess.*;
import io.javalin.*;
import io.javalin.http.Context;
import requests.*;
import service.*;
import results.CreateResult;
import results.ListResult;
import results.LoginResult;
import results.RegisterResult;
import websocket.commands.UserGameCommand;

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
        clear(userDAO, authDAO, gameDAO, gson);
        createGame(authDAO, gameDAO, gson);
        listGames(authDAO, gameDAO, gson);
        joinGame(authDAO, gameDAO, gson);
        ws(gson);
    }

    private void applyException(Context ctx, Gson gson, Exception e, int statusCode) {
        ctx.status(statusCode);
        ctx.result(gson.toJson(new ErrorResponse("Error: " + e.getMessage())));
    }

    private void ws(Gson gson) {
        javalin.ws("/ws", ws -> {
            ws.onConnect(ctx -> {
                ctx.enableAutomaticPings();
                System.out.println("Connected");
            });

            ws.onMessage(ctx -> {
                System.out.println("Message");

                String json = ctx.message();

                UserGameCommand command = gson.fromJson(json, UserGameCommand.class);

                if (command.getCommandType().equals(UserGameCommand.CommandType.CONNECT)) {
                    System.out.println("CONNECT");
                } else if (command.getCommandType().equals(UserGameCommand.CommandType.MAKE_MOVE)) {
                    System.out.println("MAKE MOVE");
                } else if (command.getCommandType().equals(UserGameCommand.CommandType.RESIGN)) {
                    System.out.println("RESIGN");
                } else if (command.getCommandType().equals(UserGameCommand.CommandType.LEAVE)) {
                    System.out.println("LEAVE");
                }
            });

            ws.onClose(ctx -> {
                System.out.println("Closed");
            });
        });
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
                e.printStackTrace();
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

    private void clear(UserDAO userDAO, AuthDAO authDAO, GameDAO gameDAO, Gson gson) {
        javalin.delete("/db", ctx -> {
            ClearService service = new ClearService(userDAO, authDAO, gameDAO);
            try {
                service.clearAll();
            } catch (DataAccessException e) {
                applyException(ctx, gson, e, 500);
            }
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