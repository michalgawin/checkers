package pl.games.checkers.ai;

import pl.games.checkers.Copier;
import pl.games.checkers.Pawn;
import pl.games.checkers.Position;

/**
 * Board represents checkerboard with pawns and provides basic operations on pawns.
 * @param <T> implementation of Pawn
 */
public abstract class Board<T extends Pawn> implements Copier<Pawn[][]> {

    private final int width;
    private final int height;

    public Board(int height, int width) {
        this.height = height;
        this.width = width;
    }

    public boolean isEmpty(int y, int x) {
        return getPawn(y, x) == null;
    }

    public boolean isNotEmpty(int y, int x) {
        return !isEmpty(y, x);
    }

    public Board setPawn(Position position, T pawn) {
        return setPawn(position.row(), position.column(), pawn);
    }

    public abstract Board setPawn(int y, int x, T pawn);

    public T getPawn(Position position) {
        return getPawn(position.row(), position.column());
    }

    public abstract T getPawn(int y, int x);

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Board removePawn(Position currentPosition) {
        return setPawn(currentPosition, null);
    }

    public Board move(T pawn, Position currentPosition, Position nextPosition) {
        pawn.move(nextPosition);
        setPawn(currentPosition, null);
        setPawn(nextPosition, pawn);
        return this;
    }

    public Board abortMove(Pawn pawn) {
        pawn.abortMove();
        return this;
    }

}
