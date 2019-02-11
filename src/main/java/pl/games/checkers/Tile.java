package pl.games.checkers;

import javafx.scene.shape.Rectangle;

public class Tile extends Rectangle {

    private final TileType type;
    private Pawn pawn;

    public boolean hasPiece() {
        return pawn != null;
    }

    public Pawn getPawn() {
        return pawn;
    }

    public void setPawn(Pawn pawn) {
        this.pawn = pawn;
    }

    public Tile(int column, int row) {
        setWidth(Checkerboard.TILE_SIZE_X);
        setHeight(Checkerboard.TILE_SIZE_Y);

        relocate(column * Checkerboard.TILE_SIZE_X, row * Checkerboard.TILE_SIZE_Y);

        this.type = isNotAllowed(column, row) ? TileType.DISALLOWED : TileType.ALLOWED;
        setFill(type.background);
    }

    public boolean isAllowed() {
        return type == TileType.ALLOWED;
    }

    public static boolean isNotAllowed(int x, int y) {
        return (x + y) % 2 == 0;
    }

}
