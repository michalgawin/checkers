package pl.games.checkers;

import java.util.function.Predicate;

public class Rules {

    public static Predicate<Position> isWithinRange(int startX, int endX, int startY, int endY) {
        return position ->
                (position.x >= startX && position.x < endX) &&
                        (position.y >= startY && position.y < endY);
    }

    public static Predicate<Position> isTailBusy(Tile tiles[][]) {
        return position -> tiles[position.x][position.y].hasPiece();
    }

    public static Predicate<Position> isOpponent(Tile tiles[][], Pawn pawn) {
        return position -> tiles[position.x][position.y].getPawn().getType() != pawn.getType();
    }

    public static Predicate<Position> isTailAllowed() {
        return position -> (position.x + position.y) % 2 != 0;
    }

    public static Predicate<Pawn> isOnOppositeBorder() {
        return (Pawn pawn) -> (pawn.getType() == PawnType.WHITE && pawn.nextPosition().y == 0) ||
                    (pawn.getType() == PawnType.BLACK && pawn.nextPosition().y == Checkerboard.HEIGHT - 1);
    }

    public static Predicate<Pawn> isKing() {
        return (Pawn pawn) -> pawn.isKing();
    }
    
    public static Predicate<Pawn> isDiagonalMove(Position nextPosition) {
        return pawn ->
                (Math.abs(getChangeWidth(pawn, nextPosition)) == Math.abs(getChangeHeight(pawn, nextPosition))) &&
                        (getChangeWidth(pawn, nextPosition) != 0);
    }

    private static int getChangeWidth(Pawn pawn, Position position) {
        return position.x - pawn.currentPosition().x;
    }

    private static int getChangeHeight(Pawn pawn, Position position) {
        return position.y - pawn.currentPosition().y;
    }

}
