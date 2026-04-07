package client;

import jakarta.websocket.*;
import com.google.gson.Gson;

import java.net.URI;

public class WebSocketClient extends Endpoint {
    public Session session;
    private java.util.function.Consumer<String> messageListener;
    private final Gson gson = new Gson();

    public WebSocketClient(String url) throws Exception {
        URI uri = new URI(url);
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();

        this.session = container.connectToServer(this, uri);

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
        System.out.println("Received: " + json); // optional debug
        if (messageListener != null) {
            messageListener.accept(json); // forward to ChessClient
        }
    }

    public void setMessageListener(java.util.function.Consumer<String> listener) {
        this.messageListener = listener;
    }
}
