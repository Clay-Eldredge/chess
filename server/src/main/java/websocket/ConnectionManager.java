package websocket;

import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {

    private final ConcurrentHashMap<Integer, Set<Session>> connections = new ConcurrentHashMap<>();
    private final Gson gson = new Gson();

    public void add(int gameID, Session session) {
        connections.computeIfAbsent(gameID, k -> ConcurrentHashMap.newKeySet()).add(session);
    }

    public void remove(int gameID, Session session) {
        Set<Session> sessions = connections.get(gameID);
        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) {
                connections.remove(gameID);
            }
        }
    }

    public void broadcast(int gameID, Session excludeSession, ServerMessage message) {
        Set<Session> sessions = connections.get(gameID);
        if (sessions == null) return;

        String json = gson.toJson(message);

        for (Session s : sessions) {
            try {
                if (s.isOpen() && (excludeSession == null || !s.equals(excludeSession))) {
                    s.getRemote().sendString(json);
                }
            } catch (IOException ignored) {}
        }
    }
}