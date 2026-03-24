package client;

import chess.*;
import model.AuthData;
import model.UserData;

import java.util.Scanner;

public class ClientMain {
    public static void main(String[] args) {
        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        System.out.println("♕ 240 Chess Client: " + piece);
        Scanner scanner = new Scanner(System.in);

        AuthData authData = null;
        UserData userData = null;
        boolean loggedIn = false;

        while (true) {
            System.out.print("> ");

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
