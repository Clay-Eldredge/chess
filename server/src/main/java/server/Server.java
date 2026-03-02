package server;

import com.google.gson.Gson;
import dataaccess.*;
import io.javalin.*;
import service.AlreadyTakenException;
import service.UserService;
import service.requests.RegisterRequest;
import service.results.RegisterResult;

public class Server {

    private final Javalin javalin;

    public Server() {
        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        UserDAO userDAO = new MemoryUserDAO();
        AuthDAO authDAO = new MemoryAuthDAO();

        // Register a User
        javalin.post("/user", ctx -> {
            Gson gson = new Gson();

            RegisterRequest request = gson.fromJson(ctx.body(), RegisterRequest.class);

            UserService service = new UserService(userDAO, authDAO);

            try {
                RegisterResult result = service.register(request);
                ctx.status(200);
                ctx.result(gson.toJson(result));
            } catch (AlreadyTakenException e) {
                ctx.status(403);
                String jsonResponse = gson.toJson(new ErrorResponse("Error: username already taken"));
                ctx.result(jsonResponse);
            } catch (DataAccessException e) {
                ctx.status(500);
                String jsonResponse = gson.toJson(
                        new ErrorResponse("Error: " + e.getMessage())
                );
                ctx.result(jsonResponse);
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
