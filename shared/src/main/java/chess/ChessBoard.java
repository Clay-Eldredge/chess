package chess;

import java.util.Objects;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {
    private ChessPiece[][] PiecesMatrix = new ChessPiece[8][8];

    public ChessBoard() {

    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        this.PiecesMatrix[position.getRow()-1][position.getColumn()-1] = piece;
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        return this.PiecesMatrix[position.getRow()-1][position.getColumn()-1];
    }


    private void setupSide(ChessGame.TeamColor color) {
        int baseRow;
        int pawnRow;

        if (color ==  ChessGame.TeamColor.WHITE) {
            baseRow = 1;
            pawnRow = 2;
        } else {
            baseRow = 8;
            pawnRow = 7;
        }

        // Rooks
        this.addPiece(new ChessPosition(baseRow,1),new ChessPiece(color,ChessPiece.PieceType.ROOK));
        this.addPiece(new ChessPosition(baseRow,8),new ChessPiece(color,ChessPiece.PieceType.ROOK));

        // Knights
        this.addPiece(new ChessPosition(baseRow,2),new ChessPiece(color,ChessPiece.PieceType.KNIGHT));
        this.addPiece(new ChessPosition(baseRow,7),new ChessPiece(color,ChessPiece.PieceType.KNIGHT));

        // Bishops
        this.addPiece(new ChessPosition(baseRow,3),new ChessPiece(color,ChessPiece.PieceType.BISHOP));
        this.addPiece(new ChessPosition(baseRow,6),new ChessPiece(color,ChessPiece.PieceType.BISHOP));

        // Queen
        this.addPiece(new ChessPosition(baseRow,4),new ChessPiece(color,ChessPiece.PieceType.QUEEN));

        // King
        this.addPiece(new ChessPosition(baseRow,5),new ChessPiece(color,ChessPiece.PieceType.KING));

        // Pawns
        for (int i = 1; i < 9; i++) {
            this.addPiece(new ChessPosition(pawnRow,i),new ChessPiece(color,ChessPiece.PieceType.PAWN));
        }
    }
    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        this.PiecesMatrix = new ChessPiece[8][8];

        setupSide(ChessGame.TeamColor.WHITE);
        setupSide(ChessGame.TeamColor.BLACK);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (obj == null || !(obj.getClass() == this.getClass())) {
            return false;
        }

        return java.util.Arrays.deepEquals(this.PiecesMatrix, ((ChessBoard) obj).PiecesMatrix);
    }

    @Override
    public int hashCode() {
        return Objects.hash((Object) PiecesMatrix);
    }
}
