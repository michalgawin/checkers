package pl.games.checkers;

import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Shape;

import java.awt.geom.Point2D;

public class Pawn extends StackPane {

    private static final double RADIUS_X = Checkerboard.TILE_SIZE_X * 0.35;
    private static final double RADIUS_Y = Checkerboard.TILE_SIZE_Y * 0.35;
    private static final double PAWN_SIZE_Y = Checkerboard.TILE_SIZE_Y * 0.1;

    private PawnType type;
    private boolean king = false;
    private Shape topOfPawn;

    private Point2D.Double latestCursorPosition = new Point2D.Double(-1, -1);;
    private Point2D.Double lastCursorPosition = new Point2D.Double(-1, -1);;

    public Pawn(PawnType type, int column, int row) {
        this.type = type;

        move(new Position(column, row));

        Shape sideOfPawn = createPawn(type.color, PAWN_SIZE_Y);
        topOfPawn = createPawn(type.color, 0);

        getChildren().addAll(sideOfPawn, topOfPawn);

        setOnMousePressed(e -> {
            latestCursorPosition.x = e.getSceneX();
            latestCursorPosition.y = e.getSceneY();
        });

        setOnMouseDragged(e -> {
            relocate(e.getSceneX() - latestCursorPosition.x + lastCursorPosition.x,
                    e.getSceneY() - latestCursorPosition.y + lastCursorPosition.y);
        });
    }

    public PawnType getType() {
        return type;
    }

    public Position lastPosition() {
        return new Position(Checkerboard.toBoardWidth(lastCursorPosition.x),
                Checkerboard.toBoardHeight(lastCursorPosition.y));
    }

    public Position nextPosition() {
        return new Position((int)(getLayoutX() + Checkerboard.TILE_SIZE_X / 2) / Checkerboard.TILE_SIZE_X,
                (int)(getLayoutY() + Checkerboard.TILE_SIZE_Y / 2) / Checkerboard.TILE_SIZE_Y);
    }

    public boolean isKing() {
        return king;
    }

    public void setKing() {
        king = true;
        getChildren().add(createPawn(type.color, -PAWN_SIZE_Y));
    }

    public void move(Position position) {
        lastCursorPosition.x = position.x * Checkerboard.TILE_SIZE_X;
        lastCursorPosition.y = position.y * Checkerboard.TILE_SIZE_Y;
        relocate(lastCursorPosition.x, lastCursorPosition.y);
    }

    public void cancelMove() {
        relocate(lastCursorPosition.x, lastCursorPosition.y);
    }

    private Shape createPawn(Color color, double shiftY) {
        Ellipse shape = new Ellipse(RADIUS_X, RADIUS_Y);
        shape.setFill(color);
        shape.setStroke(Color.BLACK);

        shape.setTranslateX((Checkerboard.TILE_SIZE_X - RADIUS_X * 2) / 2);
        shape.setTranslateY((Checkerboard.TILE_SIZE_Y - RADIUS_Y * 2) / 2 + shiftY);

        return shape;
    }

}
