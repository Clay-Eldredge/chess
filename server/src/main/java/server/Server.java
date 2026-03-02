package server;

import com.google.gson.Gson;
import dataaccess.AuthDAO;
import dataaccess.MemoryUserDAO;
import dataaccess.UserDAO;
import io.javalin.*;
import model.UserData;
import service.UserService;
import service.requests.RegisterRequest;
import service.results.RegisterResult;

public class Server {

    private final Javalin javalin;

    public Server() {
        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        UserDAO userDAO = new MemoryUserDAO();

        // Register a User
        javalin.post("/user", ctx -> {
            Gson gson = new Gson();

            RegisterRequest request = gson.fromJson(ctx.body(), RegisterRequest.class);

            UserService service = new UserService(userDAO);

            RegisterResult result = service.register(request);

            ctx.result(gson.toJson(result));
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
