package websocket.commands;

import java.util.Objects;

/**
 * Represents a MAKE_MOVE command sent from client to server.
 * Includes the starting and ending coordinates of the move.
 */
public class MakeMoveCommand extends UserGameCommand {

    private final String from;
    private final String to;

    public MakeMoveCommand(String authToken, Integer gameID, String from, String to) {
        super(CommandType.MAKE_MOVE, authToken, gameID);
        this.from = from;
        this.to = to;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MakeMoveCommand that)) return false;
        if (!super.equals(o)) return false;
        return Objects.equals(getFrom(), that.getFrom()) &&
                Objects.equals(getTo(), that.getTo());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getFrom(), getTo());
    }
}