package pl.games.checkers.ai;

import pl.games.checkers.Copier;
import pl.games.checkers.Pawn;
import pl.games.checkers.Position;

public abstract class Board<T extends Pawn> implements Copier<Pawn[][]> {

    private final int width;
    private final int height;

    public Board() {
        this.height = 0;
        this.width = 0;
    }

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

    public abstract Board setPawn(int y, int x, T pawn);

    public Pawn getPawn(Position position) {
        return getPawn(position.row(), position.column());
    }

    public abstract T getPawn(int y, int x);

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

}
