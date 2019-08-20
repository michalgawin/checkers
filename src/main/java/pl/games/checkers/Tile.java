package pl.games.checkers;

import javafx.scene.shape.Rectangle;

public class Tile extends Rectangle {

    private final TileType type;
    private PawnFigure pawn;

    public PawnFigure getPawn() {
        return pawn;
    }

    public void setPawn(PawnFigure pawn) {
        this.pawn = pawn;
    }

    public Tile(int row, int column) {
        setWidth(Checkerboard.TILE_SIZE_X);
        setHeight(Checkerboard.TILE_SIZE_Y);

        relocate(column * Checkerboard.TILE_SIZE_X, row * Checkerboard.TILE_SIZE_Y);

        this.type = isNotAllowed(row, column) ? TileType.DISALLOWED : TileType.ALLOWED;
        setFill(type.background);
    }

    public boolean isAllowed() {
        return type == TileType.ALLOWED;
    }

    public static boolean isNotAllowed(int y, int x) {
        return (y + x) % 2 == 0;
    }

}
