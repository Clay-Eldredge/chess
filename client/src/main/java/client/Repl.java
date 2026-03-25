package client;

import chess.ChessGame;
import chess.ChessPiece;
import ui.EscapeSequences;

import java.util.Scanner;

public class Repl {
    private final ChessClient client;
    private static final String BLUE = EscapeSequences.SET_TEXT_COLOR_BLUE;

    public Repl(String serverURl) {
        client = new ChessClient(serverURl);
    }

    public void run() {
        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        System.out.println("♕ 240 Chess Client: " + piece);
        System.out.println(client.help());

        Scanner scanner = new Scanner(System.in);
        var result = "";
        while (!result.equals("quit")) {
            client.printPrompt();
            String line = scanner.nextLine();

            try {
                result = client.eval(line);
                System.out.println(BLUE + result);
            } catch (Throwable e) {
                var msg = e.toString();
                System.out.println(msg);
            }
        }
    }
}
