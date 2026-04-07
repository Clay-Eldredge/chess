package client;

import chess.*;
import ui.EscapeSequences;

public class PaintBoard {

    private static final String CELL = "   ";
    private static final String PAD = " ";

    public void paint(ChessGame game, ChessGame.TeamColor perspective) {
        ChessBoard board = game.getBoard();

        int rowStart = (perspective == ChessGame.TeamColor.WHITE) ? 8 : 1;
        int rowEnd = (perspective == ChessGame.TeamColor.WHITE) ? 0 : 9;
        int rowStep = (perspective == ChessGame.TeamColor.WHITE) ? -1 : 1;

        int colStart = (perspective == ChessGame.TeamColor.WHITE) ? 1 : 8;
        int colEnd = (perspective == ChessGame.TeamColor.WHITE) ? 9 : 0;
        int colStep = (perspective == ChessGame.TeamColor.WHITE) ? 1 : -1;

        printColumnLabels(colStart, colEnd, colStep);

        for (int row = rowStart; row != rowEnd; row += rowStep) {

            System.out.print(rowLabel(row));

            for (int col = colStart; col != colEnd; col += colStep) {
                ChessPiece piece = board.getPiece(new ChessPosition(row, col));

                boolean isLight = (row + col) % 2 == 0;

                String bgColor = isLight
                        ? EscapeSequences.SET_BG_COLOR_DARK_GREY
                        : EscapeSequences.SET_BG_COLOR_LIGHT_GREY;

                System.out.print(bgColor);

                if (piece == null) {
                    System.out.print(CELL);
                } else {
                    System.out.print(getColoredPiece(piece));
                }

                System.out.print(EscapeSequences.RESET_BG_COLOR);
            }

            System.out.print(rowLabel(row));
            System.out.println();
        }

        printColumnLabels(colStart, colEnd, colStep);
    }

    private String rowLabel(int row) {
        return EscapeSequences.SET_BG_COLOR_BLACK +
                EscapeSequences.SET_TEXT_COLOR_YELLOW +
                " " + row + " " +
                EscapeSequences.RESET_BG_COLOR;
    }

    private void printColumnLabels(int colStart, int colEnd, int colStep) {

        System.out.print(
                EscapeSequences.SET_BG_COLOR_BLACK +
                        "   " +
                        EscapeSequences.RESET_BG_COLOR
        );

        for (int col = colStart; col != colEnd; col += colStep) {
            char letter = (char) ('a' + col - 1);

            System.out.print(
                    EscapeSequences.SET_BG_COLOR_BLACK +
                            EscapeSequences.SET_TEXT_COLOR_YELLOW +
                            " " + letter + " " +
                            EscapeSequences.RESET_BG_COLOR
            );
        }

        System.out.print(
                EscapeSequences.SET_BG_COLOR_BLACK +
                        "   " +
                        EscapeSequences.RESET_BG_COLOR
        );

        System.out.println();
    }

    private String getColoredPiece(ChessPiece piece) {
        String color = (piece.getTeamColor() == ChessGame.TeamColor.WHITE)
                ? EscapeSequences.SET_TEXT_COLOR_RED
                : EscapeSequences.SET_TEXT_COLOR_BLUE;

        return color +
                PAD + getPieceSymbol(piece) + PAD +
                EscapeSequences.RESET_TEXT_COLOR;
    }

    private String getPieceSymbol(ChessPiece piece) {
        return switch (piece.getPieceType()) {
            case KING -> "K";
            case QUEEN -> "Q";
            case ROOK -> "R";
            case BISHOP -> "B";
            case KNIGHT -> "N";
            case PAWN -> "P";
        };
    }
}