package pl.games.checkers.ai;

import pl.games.checkers.Pawn;
import pl.games.checkers.PawnFigure;
import pl.games.checkers.Position;
import pl.games.checkers.Tile;

import java.util.function.BiFunction;

public class TileBoard extends Board<PawnFigure> {

    private final Tile[][] tiles;

    public TileBoard(int height, int width, BiFunction<Integer, Integer, PawnFigure> createPawnFn) {
        super(height, width);
        this.tiles = new Tile[height][width];
        for (int row = 0; row < getHeight(); row++) {
            for (int column = 0; column < getWidth(); column++) {
                Tile tile = new Tile(row, column);
                this.setTile(row, column, tile);
                if (tile.isAllowed()) {
                    tile.setPawn(createPawnFn.apply(row, column));
                }
            }
        }
    }

    public Tile getTile(int y, int x) {
        try {
            return tiles[y][x];
        } catch (ArrayIndexOutOfBoundsException e) {
            return null;
        }
    }

    @Override
    public PawnFigure getPawn(Position position) {
        return getPawn(position.row(), position.column());
    }

    @Override
    public PawnFigure getPawn(int y, int x) {
        try {
            return tiles[y][x].getPawn();
        } catch (ArrayIndexOutOfBoundsException e) {
            return null;
        }
    }

    public TileBoard setTile(int y, int x, Tile tile) {
        tiles[y][x] = tile;
        return this;
    }

    @Override
    public TileBoard setPawn(int y, int x, PawnFigure pawn) {
        tiles[y][x].setPawn(pawn);
        return this;
    }

    public TileBoard removePawn(Position currentPosition) {
        setPawn(currentPosition.row(), currentPosition.column(), null);
        return this;
    }

    public TileBoard move(PawnFigure pawn, Position currentPosition, Position nextPosition) {
        pawn.move(nextPosition);
        setPawn(currentPosition.row(), currentPosition.column(), null);
        setPawn(nextPosition.row(), nextPosition.column(), pawn);
        return this;
    }

    public TileBoard abortMove(Pawn pawn) {
        pawn.abortMove();
        return this;
    }

    @Override
    public Pawn[][] copy() {
        Pawn[][] pawnsCopy = new Pawn[getHeight()][getWidth()];

        for (int row = 0; row < getHeight(); row++) {
            for (int col = 0; col < getWidth(); col++) {
                if (tiles[row][col] != null) {
                    pawnsCopy[row][col] = tiles[row][col].getPawn().copy();
                } else {
                    pawnsCopy[row][col] = null;
                }
            }
        }

        return pawnsCopy;
    }

}
