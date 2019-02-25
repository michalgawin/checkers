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

    private Point2D.Double currentCursorPosition = new Point2D.Double(-1, -1);;
    private Point2D.Double latestCursorPosition = new Point2D.Double(-1, -1);;

    public PawnType getType() {
        return type;
    }

    public Position currentPosition() {
        return new Position(Checkerboard.toBoardWidth(latestCursorPosition.x),
                Checkerboard.toBoardHeight(latestCursorPosition.y));
    }

    public Position nextPosition() {
        return new Position((int)(getLayoutX() + Checkerboard.TILE_SIZE_X / 2) / Checkerboard.TILE_SIZE_X,
                (int)(getLayoutY() + Checkerboard.TILE_SIZE_Y / 2) / Checkerboard.TILE_SIZE_Y);
    }

    public Pawn(PawnType type, int column, int row) {
        this.type = type;

        move(new Position(column, row));

        Shape sideOfPawn = createPawn(type.color, PAWN_SIZE_Y);
        topOfPawn = createPawn(type.color, 0);

        getChildren().addAll(sideOfPawn, topOfPawn);

        setOnMousePressed(e -> {
            currentCursorPosition.x = e.getSceneX();
            currentCursorPosition.y = e.getSceneY();
        });

        setOnMouseDragged(e -> {
            relocate(e.getSceneX() - currentCursorPosition.x + latestCursorPosition.x,
                    e.getSceneY() - currentCursorPosition.y + latestCursorPosition.y);
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

    public void move(Position position) {
        latestCursorPosition.x = position.x * Checkerboard.TILE_SIZE_X;
        latestCursorPosition.y = position.y * Checkerboard.TILE_SIZE_Y;
        relocate(latestCursorPosition.x, latestCursorPosition.y);
    }

    public void abortMove() {
        relocate(latestCursorPosition.x, latestCursorPosition.y);
    }
}
