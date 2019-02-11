package pl.games.checkers;

import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Shape;

public class Pawn extends StackPane {

    private static final double RADIUS_X = Checkerboard.TILE_SIZE_X * 0.35;
    private static final double RADIUS_Y = Checkerboard.TILE_SIZE_Y * 0.35;
    private static final double PAWN_SIZE_Y = Checkerboard.TILE_SIZE_Y * 0.1;

    private PawnType type;
    private boolean king = false;
    private Shape topOfPawn;

    private double mouseX, mouseY;
    private double latestMouseX, latestMouseY;

    public PawnType getType() {
        return type;
    }

    public int lastColumn() {
        return Checkerboard.toBoardWidth(latestMouseX);
    }

    public int lastRow() {
        return Checkerboard.toBoardHeight(latestMouseY);
    }

    public int nextColumn() {
        return (int)(getLayoutX() + Checkerboard.TILE_SIZE_X / 2) / Checkerboard.TILE_SIZE_X;
    }

    public int nextRow() {
        return (int)(getLayoutY() + Checkerboard.TILE_SIZE_Y / 2) / Checkerboard.TILE_SIZE_Y;
    }

    public Pawn(PawnType type, int column, int row) {
        this.type = type;

        move(column, row);

        Shape sideOfPawn = createPawn(type.color, PAWN_SIZE_Y);
        topOfPawn = createPawn(type.color, 0);

        getChildren().addAll(sideOfPawn, topOfPawn);

        setOnMousePressed(e -> {
            mouseX = e.getSceneX();
            mouseY = e.getSceneY();
        });

        setOnMouseDragged(e -> {
            relocate(e.getSceneX() - mouseX + latestMouseX, e.getSceneY() - mouseY + latestMouseY);
        });
    }

    public Shape createPawn(Color color, double shiftY) {
        Ellipse shape = new Ellipse(RADIUS_X, RADIUS_Y);
        shape.setFill(color);
        shape.setStroke(Color.BLACK);

        shape.setTranslateX((Checkerboard.TILE_SIZE_X - RADIUS_X * 2) / 2);
        shape.setTranslateY((Checkerboard.TILE_SIZE_Y - RADIUS_Y * 2) / 2 + shiftY);

        return shape;
    }

    public boolean isKing() {
        return king;
    }

    public void setKing() {
        king = true;
        getChildren().add(createPawn(type.color, -PAWN_SIZE_Y));
    }

    public void move(int column, int row) {
        latestMouseX = column * Checkerboard.TILE_SIZE_X;
        latestMouseY = row * Checkerboard.TILE_SIZE_Y;
        relocate(latestMouseX, latestMouseY);
    }

    public void abortMove() {
        relocate(latestMouseX, latestMouseY);
    }
}
