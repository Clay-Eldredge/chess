package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    private ChessBoard board;
    private ChessGame.TeamColor currentTurn;


    public ChessGame() {
        board = new ChessBoard();
        board.resetBoard();

        currentTurn = TeamColor.WHITE;
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return currentTurn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        currentTurn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    // Helpers
    private Collection<ChessPosition> allPositions() {
        Collection<ChessPosition> positions = new ArrayList<>();
        for (int i = 1; i < 9; i++) {
            for (int j = 1; j < 9; j++) {
                positions.add(new ChessPosition(i, j));
            }
        }
        return positions;
    }

    private boolean moveLeavesKingSafe(ChessMove move, TeamColor teamColor) {
        ChessPiece piece = board.getPiece(move.getStartPosition());
        ChessPiece captured = board.getPiece(move.getEndPosition());

        board.addPiece(move.getEndPosition(), piece);
        board.addPiece(move.getStartPosition(), null);

        boolean inCheck = isInCheck(teamColor);

        board.addPiece(move.getEndPosition(), captured);
        board.addPiece(move.getStartPosition(), piece);

        return !inCheck;
    }

    private boolean hasAnyValidMove(TeamColor teamColor) {
        for (ChessPosition pos : allPositions()) {
            ChessPiece piece = board.getPiece(pos);

            if (piece != null && piece.getTeamColor() == teamColor) {
                for (ChessMove move : piece.pieceMoves(board, pos)) {
                    if (moveLeavesKingSafe(move, teamColor)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        Collection<ChessMove> valid = new ArrayList<>();
        ChessPiece piece = board.getPiece(startPosition);

        if (piece == null) {
            return valid;
        }

        for (ChessMove move : piece.pieceMoves(board, startPosition)) {
            if (moveLeavesKingSafe(move, piece.getTeamColor())) {
                valid.add(move);
            }
        }

        return valid;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        Collection<ChessMove> validMoves = validMoves(move.getStartPosition());
        if (validMoves.contains(move)) {
            ChessPiece piece = this.board.getPiece(move.getStartPosition());
            if (piece.getTeamColor() != this.currentTurn) {
                throw new InvalidMoveException();
            }
            if (move.getPromotionPiece() != null) {
                piece = new ChessPiece(piece.getTeamColor(),move.getPromotionPiece());
            }
            this.board.addPiece(move.getEndPosition(), piece);
            this.board.addPiece(move.getStartPosition(), null);

            if (piece.getTeamColor() == TeamColor.BLACK) {
                setTeamTurn(TeamColor.WHITE);
            } else {
                setTeamTurn(TeamColor.BLACK);
            }
        } else {
            throw new InvalidMoveException();
        }
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        Collection<ChessPosition> opposingPiecesPositions = new ArrayList<>();
        ChessPosition kingPos = new ChessPosition(1,1);
        for (int i = 1; i < 9; i++) {
            for (int j = 1; j < 9; j++) {
                ChessPosition pos = new ChessPosition(i,j);
                ChessPiece piece = this.board.getPiece(pos);
                if (piece != null && piece.getTeamColor() != teamColor) {
                    opposingPiecesPositions.add(pos);
                } else if (piece != null && piece.getTeamColor() == teamColor && piece.getPieceType() == ChessPiece.PieceType.KING) {
                    kingPos = pos;
                }
            }
        }

        for (ChessPosition opPos : opposingPiecesPositions) {
            ChessPiece opPiece = this.board.getPiece(opPos);
            Collection<ChessMove> moves = opPiece.pieceMoves(this.board, opPos);
            for (ChessMove move : moves) {
                if (move.getEndPosition().equals(kingPos)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        if (!isInCheck(teamColor)) {
            return false;
        }
        return !hasAnyValidMove(teamColor);
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        if (isInCheck(teamColor)) {
            return false;
        }
        return !hasAnyValidMove(teamColor);
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }

    @Override
    public int hashCode() {
        return Objects.hash(board, currentTurn);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }

        ChessGame casted = (ChessGame) obj;

        return Objects.equals(this.getBoard(), casted.getBoard())
                && Objects.equals(this.getTeamTurn(), casted.getTeamTurn());
    }
}
