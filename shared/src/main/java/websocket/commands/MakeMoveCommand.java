package websocket.commands;

import chess.ChessMove;

import java.util.Objects;

/**
 * Represents a MAKE_MOVE command sent from client to server.
 * Includes the starting and ending coordinates of the move.
 */
public class MakeMoveCommand extends UserGameCommand {

    private final ChessMove move;

    public MakeMoveCommand(String authToken, Integer gameID, ChessMove move) {
        super(CommandType.MAKE_MOVE, authToken, gameID);
        this.move = move;
    }

    public ChessMove getMove() {
        return move;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MakeMoveCommand that)) return false;
        if (!super.equals(o)) return false;
        return Objects.equals(getMove(), that.getMove());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getMove());
    }
}