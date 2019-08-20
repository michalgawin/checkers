package pl.games.checkers.ai;

import pl.games.checkers.Pawn;
import pl.games.checkers.PawnFigure;
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

    @Override
    public PawnFigure getPawn(int y, int x) {
        try {
            Tile tile = getTile(y, x);
            return tile == null ? null : tile.getPawn();
        } catch (ArrayIndexOutOfBoundsException e) {
            return null;
        }
    }

    @Override
    public TileBoard setPawn(int y, int x, PawnFigure pawn) {
        tiles[y][x].setPawn(pawn);
        return this;
    }

    @Override
    public Pawn[][] copy() {
        Pawn[][] pawnsCopy = new Pawn[getHeight()][getWidth()];

        for (int row = 0; row < getHeight(); row++) {
            for (int col = 0; col < getWidth(); col++) {
                if (getPawn(row, col) != null) {
                    pawnsCopy[row][col] = getPawn(row, col).copy();
                } else {
                    pawnsCopy[row][col] = null;
                }
            }
        }

        return pawnsCopy;
    }

    public Tile getTile(int y, int x) {
        try {
            return tiles[y][x];
        } catch (ArrayIndexOutOfBoundsException e) {
            return null;
        }
    }

    public TileBoard setTile(int y, int x, Tile tile) {
        tiles[y][x] = tile;
        return this;
    }

}
