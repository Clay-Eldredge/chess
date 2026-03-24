package requests;

import chess.ChessGame;

public record JoinRequest(ChessGame.TeamColor playerColor, Integer gameID) {
}
