package chess;

import javax.naming.event.ObjectChangeListener;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.ArrayList;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {
    private ChessGame.TeamColor teamColor;
    private ChessPiece.PieceType type;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.teamColor = pieceColor;
        this.type = type;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return this.teamColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return this.type;
    }

    private boolean isInBounds(ChessPosition position) {
        return ((position.getRow() >= 1)
                && (position.getColumn() >= 1)
                && (position.getRow() <= 8)
                && (position.getColumn() <= 8));
    }

    private boolean isOccupied(ChessBoard board, ChessPosition pos) {
        ChessPiece piece = board.getPiece(pos);
        return (piece != null);
    }

    private ChessPosition getPositionFromVector(ChessBoard board, ChessPosition startPos, int drow, int dcol, int range) {
        int rowdx = drow * range;
        int coldx = dcol * range;
        return new ChessPosition(startPos.getRow() + rowdx,startPos.getColumn() + coldx);
    }

    private Collection<ChessMove> getMovesInDirection(ChessBoard board, ChessPosition startPos, int drow, int dcol, int range) {
        Collection<ChessMove> openMoves = new ArrayList<>();
        for (int i = 1; i <= range; i++) {
            boolean lastLoop = false;
            ChessPosition targetPos = getPositionFromVector(board, startPos, drow, dcol, i);
            ChessMove targetMove = new ChessMove(startPos, targetPos, null);
            if (!(isInBounds(targetPos))) {
                break;
            }
            if (isOccupied(board, targetPos)) {
                ChessGame.TeamColor occupyingPieceColor = board.getPiece(targetPos).teamColor;
                if (occupyingPieceColor.equals(this.getTeamColor())) {
                    break;
                } else {
                    lastLoop = true;
                }
            }
            openMoves.add(targetMove);
            if (lastLoop) {
                break;
            }
        }
        return openMoves;
    }

    private Collection<ChessMove> getMovesInDirections(ChessBoard board, ChessPosition startPos, int range, int[][] directions) {
        Collection<ChessMove> moves = new ArrayList<>();
        for (int[] dir : directions) {
            int dRow = dir[0];
            int dCol = dir[1];
            moves.addAll(getMovesInDirection(board, startPos, dRow, dCol, range));
        }
        return moves;
    }

    private Collection<ChessMove> getAllPromotionsForMove(ChessMove move) {
        Collection<ChessMove> moves = new ArrayList<>();
        moves.add(new ChessMove(move.getStartPosition(), move.getEndPosition(), PieceType.BISHOP));
        moves.add(new ChessMove(move.getStartPosition(), move.getEndPosition(), PieceType.QUEEN));
        moves.add(new ChessMove(move.getStartPosition(), move.getEndPosition(), PieceType.ROOK));
        moves.add(new ChessMove(move.getStartPosition(), move.getEndPosition(), PieceType.KNIGHT));
        return moves;
    }

    private Collection<ChessMove> getDiagnolPawnMove(ChessBoard board, ChessPosition startPos, ChessPosition targetPos) {
        ChessPiece target = board.getPiece(targetPos);
        if (target == null || target.getTeamColor().equals(this.getTeamColor())) {
            return null;
        } else {
            ChessMove move = new ChessMove(startPos, targetPos, null);
            if (targetPos.getRow() == 1 && this.getTeamColor().equals(ChessGame.TeamColor.BLACK)) {
                return getAllPromotionsForMove(move);
            }
            if (targetPos.getRow() == 8 && this.getTeamColor().equals(ChessGame.TeamColor.WHITE)) {
                return getAllPromotionsForMove(move);
            }
            Collection<ChessMove> moves = new ArrayList<>();
            moves.add(move);
            return moves;
        }
    }

    private Collection<ChessMove> getStraightPawnMove(ChessBoard board, ChessPosition startPos, ChessPosition targetPos) {
        ChessPiece target = board.getPiece(targetPos);
        if (target == null) {
            ChessMove move = new ChessMove(startPos, targetPos, null);
            if (targetPos.getRow() == 1 && this.getTeamColor().equals(ChessGame.TeamColor.BLACK)) {
                return getAllPromotionsForMove(move);
            }
            if (targetPos.getRow() == 8 && this.getTeamColor().equals(ChessGame.TeamColor.WHITE)) {
                return getAllPromotionsForMove(move);
            }
            Collection<ChessMove> moves = new ArrayList<>();
            moves.add(move);
            return moves;
        } else {
            return null;
        }
    }

    private Collection<ChessMove> getPawnMoves(ChessBoard board, ChessPosition startPos, int rowdx) {
        Collection<ChessMove> moves = new ArrayList<>();

        ChessPosition nextPos = getPositionFromVector(board, startPos, rowdx, 1, 1);
        if (isInBounds(nextPos)) {
            Collection<ChessMove> movesToAdd = getDiagnolPawnMove(board, startPos, new ChessPosition(nextPos.getRow(), nextPos.getColumn()));
            if (movesToAdd != null) {
                moves.addAll(movesToAdd);
            }
        }

        nextPos = getPositionFromVector(board, startPos, rowdx, -1, 1);
        if (isInBounds(nextPos)) {
            Collection<ChessMove> movesToAdd = getDiagnolPawnMove(board, startPos, new ChessPosition(nextPos.getRow(), nextPos.getColumn()));
            if (movesToAdd != null) {
                moves.addAll(movesToAdd);
            }
        }

        boolean spaceOneOpen = false;

        nextPos = getPositionFromVector(board, startPos, rowdx, 0, 1);
        if (isInBounds(nextPos)) {
            Collection<ChessMove> movesToAdd = getStraightPawnMove(board, startPos, new ChessPosition(nextPos.getRow(), nextPos.getColumn()));
            if (movesToAdd != null) {
                moves.addAll(movesToAdd);
                spaceOneOpen = true;
            }
        }

        if (spaceOneOpen) {
            nextPos = getPositionFromVector(board, startPos, rowdx, 0, 2);
            if (startPos.getRow() == 7 && this.getTeamColor().equals(ChessGame.TeamColor.BLACK)) {
                Collection<ChessMove> movesToAdd = getStraightPawnMove(board, startPos, new ChessPosition(nextPos.getRow(), nextPos.getColumn()));
                if (movesToAdd != null) {
                    moves.addAll(movesToAdd);
                }
            }
            if (startPos.getRow() == 2 && this.getTeamColor().equals(ChessGame.TeamColor.WHITE)) {
                Collection<ChessMove> movesToAdd = getStraightPawnMove(board, startPos, new ChessPosition(nextPos.getRow(), nextPos.getColumn()));
                if (movesToAdd != null) {
                    moves.addAll(movesToAdd);
                }
            }
        }

        return moves;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> possibleMoves = new ArrayList<>();

        switch (this.getPieceType()) {
            case KING -> {
                return getMovesInDirections(board, myPosition,1, new int[][] {
                        {0,1},
                        {1,1},
                        {1,0},
                        {1,-1},
                        {0,-1},
                        {-1,-1},
                        {-1,0},
                        {-1,1},
                });
            }
            case QUEEN -> {
                return getMovesInDirections(board, myPosition,8, new int[][] {
                        {0,1},
                        {1,1},
                        {1,0},
                        {1,-1},
                        {0,-1},
                        {-1,-1},
                        {-1,0},
                        {-1,1},
                });
            }
            case BISHOP -> {
                return getMovesInDirections(board, myPosition,8, new int[][] {
                        {1,1},
                        {1,-1},
                        {-1,-1},
                        {-1,1},
                });
            }
            case ROOK -> {
                return getMovesInDirections(board, myPosition,8, new int[][] {
                        {0,1},
                        {1,0},
                        {0,-1},
                        {-1,0},
                });
            }
            case KNIGHT -> {
                return getMovesInDirections(board, myPosition,1, new int[][] {
                        {1,2},
                        {2,1},
                        {2,-1},
                        {1,-2},
                        {-1,-2},
                        {-2,-1},
                        {-2,1},
                        {-1,2},
                });
            }
            case PAWN -> {
                int rowdx;
                if (this.getTeamColor().equals(ChessGame.TeamColor.WHITE)) {
                    rowdx = 1;
                } else {
                    rowdx = -1;
                }

                return getPawnMoves(board, myPosition, rowdx);
            }
        }
        return possibleMoves;
    };

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || (obj.getClass() != this.getClass())) {
            return false;
        }

        ChessPiece chessPieceObj = (ChessPiece) obj;

        return Objects.equals(chessPieceObj.getPieceType(), this.getPieceType())
                && Objects.equals(chessPieceObj.getTeamColor(), this.getTeamColor());
    }

    @Override
    public int hashCode() {
        return Objects.hash(teamColor, type);
    }
}
