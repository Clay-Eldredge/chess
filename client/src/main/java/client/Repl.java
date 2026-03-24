package client;

import chess.ChessGame;
import chess.ChessPiece;
import model.AuthData;
import model.UserData;

import java.util.Scanner;

public class Repl {
    private final ClientMain client;

    public Repl(String serverURl) {
        client =
    }

    public void run() {
        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        System.out.println("♕ 240 Chess Client: " + piece);
        System.out.println(client);

        AuthData authData = null;
        UserData userData = null;
        boolean loggedIn = false;

        Scanner scanner = new Scanner(System.in);
        var result = "";
        while (!result.equals("quit")) {
            String line = scanner.nextLine();

            String input = scanner.nextLine().trim();
            String[] tokens = input.split("\\s+");
            String cmd = tokens[0].toLowerCase();

            switch (cmd) {
                case "help": {
                    System.out.println("HELP TEXT");
                    break;
                } case "quit": {
                    return;
                } case "login": {
                    break;
                } case "register": {
                    break;
                } default: {
                    System.out.println("Command \"" + cmd + "\" does not exist. Type \"help\"");
                    break;
                }
            }
        }
    }
}
