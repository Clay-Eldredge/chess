package client;

import jakarta.websocket.*;
import com.google.gson.Gson;

import java.net.URI;

public class WebSocketClient extends Endpoint {
    public Session session;
    private final Gson gson = new Gson();

    public WebSocketClient(String url) throws Exception {
        URI uri = new URI(url);
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();

        // connect to server
        this.session = container.connectToServer(this, uri);

        // listen for messages
        this.session.addMessageHandler((MessageHandler.Whole<String>) this::onMessage);
    }


    @Override
    public void onOpen(Session session, EndpointConfig config) {
        // nothing required here
    }

    // send any object as JSON
    public void send(Object obj) throws Exception {
        String json = gson.toJson(obj);
        session.getBasicRemote().sendText(json);
    }

    // handle incoming messages
    private void onMessage(String json) {
        System.out.println("Received: " + json);
    }
}
