package pl.games.checkers;

import pl.games.checkers.model.Board;
import pl.games.checkers.model.Pawn;
import pl.games.checkers.model.PawnType;
import pl.games.checkers.model.Position;
import pl.games.checkers.ui.Checkerboard;

import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

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

    public static Predicate<Position> isAllowedBeating(Board board, Pawn pawn) {
        return nextPosition -> numberOfOpponentsToBeat(board, pawn, nextPosition) == 1;
    }

    public static Predicate<Position> isIllegalBeating(Board board, Pawn pawn) {
        return isBeatingAlly(board, pawn).or(
                nextPosition -> numberOfOpponentsToBeat(board, pawn, nextPosition) > 1);
    }

    private static Predicate<Position> isBeatingAlly(Board board, Pawn pawn) {
        return nextPosition -> streamPawnMoves(board, pawn, nextPosition)
                .filter(Rules.isOpponent(pawn).negate())
                .limit(1)
                .count() > 0;
    }

    private static long numberOfOpponentsToBeat(Board board, Pawn pawn, Position nextPosition) {
        return streamPawnMoves(board, pawn, nextPosition)
                .filter(Rules.isOpponent(pawn))
                .limit(2)
                .count();
    }

    private static int getChangeWidth(Pawn pawn, Position position) {
        return position.column() - pawn.currentPosition().column();
    }

    private static int getChangeHeight(Pawn pawn, Position position) {
        return position.row() - pawn.currentPosition().row();
    }

    private static int numberOfMoves(Position currentPosition, Position nextPosition) {
        int colDiff = Math.abs(nextPosition.column() - currentPosition.column());
        assert colDiff == Math.abs(nextPosition.row() - currentPosition.row());

        return colDiff;
    }

    private static Stream<Pawn> streamPawnMoves(Board board, Pawn pawn, Position nextPosition) {
        Position currentPosition = pawn.currentPosition();
        int hDir = horizontalDirection(currentPosition, nextPosition);
        int vDir = verticalDirection(currentPosition, nextPosition);

        return Stream.iterate(currentPosition.increment(vDir, hDir), p -> !p.equals(nextPosition), p -> p.increment(vDir, hDir))
                .limit(Checkerboard.WIDTH)
                .map(p -> board.getPawn(p))
                .filter(Objects::nonNull);
    }

    private static int horizontalDirection(Position currentPosition, Position nextPosition) {
        return Integer.compare(nextPosition.column(), currentPosition.column());
    }

    private static int verticalDirection(Position currentPosition, Position nextPosition) {
        return Integer.compare(nextPosition.row(), currentPosition.row());
    }

}
