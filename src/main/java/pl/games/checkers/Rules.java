package pl.games.checkers;

import pl.games.checkers.model.Board;
import pl.games.checkers.model.Pawn;
import pl.games.checkers.model.PawnType;
import pl.games.checkers.model.Position;
import pl.games.checkers.ui.Checkerboard;

import java.util.function.Predicate;

public class Rules {

    public static Predicate<Position> isOnBoard() {
        return isOnBoard(0, Checkerboard.WIDTH, 0, Checkerboard.HEIGHT);
    }

    private static Predicate<Position> isOnBoard(int startX, int endX, int startY, int endY) {
        return position ->
                (position.column() >= startX && position.column() < endX) &&
                        (position.row() >= startY && position.row() < endY);
    }

    public static Predicate<Position> isPositionOccupied(Board board) {
        return position -> board.isNotEmpty(position.row(), position.column());
    }

    public static Predicate<Position> isPositionAllowed() {
        return position -> (position.column() + position.row()) % 2 != 0;
    }

    public static Predicate<Pawn> isLastRow() {
        return (Pawn pawn) -> (pawn.getType() == PawnType.WHITE && pawn.nextPosition().row() == 0) ||
                    (pawn.getType() == PawnType.BLACK && pawn.nextPosition().row() == Checkerboard.HEIGHT - 1);
    }

    public static Predicate<Pawn> isOpponent(Pawn pawn) {
        return p -> p.getType() != pawn.getType();
    }

    public static Predicate<Pawn> isKing() {
        return (Pawn pawn) -> pawn.isKing();
    }

    public static Predicate<Position> isDiagonalMove(Pawn pawn) {
        return nextPosition ->
                (Math.abs(getChangeWidth(pawn, nextPosition)) == Math.abs(getChangeHeight(pawn, nextPosition))) &&
                        (getChangeWidth(pawn, nextPosition) != 0);
    }

    private static int getChangeWidth(Pawn pawn, Position position) {
        return position.column() - pawn.currentPosition().column();
    }

    private static int getChangeHeight(Pawn pawn, Position position) {
        return position.row() - pawn.currentPosition().row();
    }

}
