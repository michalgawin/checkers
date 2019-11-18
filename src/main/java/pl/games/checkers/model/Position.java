package pl.games.checkers.model;

import pl.games.checkers.Copier;

import java.awt.Point;
import java.util.function.BiFunction;

public class Position extends Point implements Copier<Position> {

    //dummy variables to hide variables from parent
    private int x;
    private int y;

    public Position(int y, int x) {
        super(x, y);
    }

    private Position(Position position) {
        this(position.row(), position.column());
    }

    public int column() {
        return super.x;
    }

    public int row() {
        return super.y;
    }

    public Position towardLeft(int direction) {
        return increment(direction, -1);
    }

    public Position towardRight(int direction) {
        return increment(direction, 1);
    }

    public Position backwardLeft(int direction) {
        return increment(-direction, -1);
    }

    public Position backwardRight(int direction) {
        return increment(-direction, 1);
    }

    public Position increment(int rDir, int cDir) {
        return new Position(row() + normalize(rDir), column() + normalize(cDir));
    }

    private int normalize(int v) {
        return Math.round(v/Math.abs(v));
    }

    @Override
    public Position copy() {
        return new Position(this);
    }

    public static BiFunction<Position, Integer, Position> towardLeft() {
        return (p, d) -> p.towardLeft(d);
    }

    public static BiFunction<Position, Integer, Position> towardRight() {
        return (p, d) -> p.towardRight(d);
    }

    public static BiFunction<Position, Integer, Position> backwardLeft() {
        return (p, d) -> p.backwardLeft(d);
    }

    public static BiFunction<Position, Integer, Position> backwardRight() {
        return (p, d) -> p.backwardRight(d);
    }

    public boolean equals(Position o) {
        if (o != null && (this.row() == o.row() && this.column() == o.column())) {
            return true;
        }

        return false;
    }
}
