package pl.games.checkers.ui;

import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Shape;
import pl.games.checkers.Position;
import pl.games.checkers.model.Move;
import pl.games.checkers.model.Pawn;
import pl.games.checkers.model.PawnImpl;
import pl.games.checkers.model.PawnType;

import java.awt.geom.Point2D;

public class PawnFigure extends StackPane implements Pawn {

    private static final double RADIUS_X = Checkerboard.TILE_SIZE_X * 0.35;
    private static final double RADIUS_Y = Checkerboard.TILE_SIZE_Y * 0.35;
    private static final double PAWN_SIZE_Y = Checkerboard.TILE_SIZE_Y * 0.1;

    private final Pawn reference;

    private Point2D.Double currentCursorPosition = new Point2D.Double(-1, -1);
    private Point2D.Double latestCursorPosition = new Point2D.Double(-1, -1);

    public PawnFigure(PawnType type, int row, int column) {
        this(type, row, column, false);
    }

    public PawnFigure(PawnType type, int row, int column, boolean isKing) {
        this(type, new Position(row, column), isKing);
    }

    public PawnFigure(PawnType type, Position position, boolean isKing) {
        reference = new PawnImpl(type, position, isKing, this);
        move(position);
        setType(type);

        Shape firstRoundel = createRoundel(type.getColor(), PAWN_SIZE_Y);
        Shape secondRoundel = createRoundel(type.getColor(), 0);
        getChildren().addAll(firstRoundel, secondRoundel);
        if (isKing) {
            setKing();
        }

        setOnMousePressed(e -> {
            currentCursorPosition.x = e.getSceneX();
            currentCursorPosition.y = e.getSceneY();
        });

        setOnMouseDragged(e -> {
            relocate(e.getSceneX() - currentCursorPosition.x + latestCursorPosition.x,
                    e.getSceneY() - currentCursorPosition.y + latestCursorPosition.y);
        });
    }

    @Override
    public PawnType getType() {
        return reference.getType();
    }

    @Override
    public void setType(PawnType pawnType) {
        reference.setType(getType());
    }

    @Override
    public Position currentPosition() {
        return new Position(Checkerboard.toBoardHeight(latestCursorPosition.y),
                Checkerboard.toBoardWidth(latestCursorPosition.x));
    }

    @Override
    public Position nextPosition() {
        return nextPosition(getNextRow(), getNextColumn());
    }

    @Override
    public Pawn nextPosition(Position position) {
        throw new IllegalArgumentException("Cannot set next position in case of UI");
    }

    private Position nextPosition(int nextRow, int nextColumn) {
        return new Position(nextRow, nextColumn);
    }

    private int getNextColumn() {
        return (int)(getLayoutX() + Checkerboard.TILE_SIZE_X / 2) / Checkerboard.TILE_SIZE_X;
    }

    private int getNextRow() {
        return (int)(getLayoutY() + Checkerboard.TILE_SIZE_Y / 2) / Checkerboard.TILE_SIZE_Y;
    }

    @Override
    public boolean isKing() {
        return reference.isKing();
    }

    @Override
    public void setKing() {
        reference.setKing();
        Shape thirdRoundel = createRoundel(getType().getColor(), -PAWN_SIZE_Y);
        getChildren().add(thirdRoundel);
    }

    @Override
    public void move(Position position) {
        latestCursorPosition.x = position.column() * Checkerboard.TILE_SIZE_X;
        latestCursorPosition.y = position.row() * Checkerboard.TILE_SIZE_Y;
        relocate(latestCursorPosition.x, latestCursorPosition.y);

        reference.move(currentPosition());
    }

    @Override
    public Pawn setMove(Move move) {
        reference.setMove(move);
        return this;
    }

    @Override
    public boolean hasBeating() {
        return reference.hasBeating();
    }

    @Override
    public void abortMove() {
        relocate(latestCursorPosition.x, latestCursorPosition.y);
    }

    @Override
    public Pawn copy() {
        return reference.copy();
    }

    private Shape createRoundel(Color color, double shiftY) {
        Ellipse roundel = new Ellipse(RADIUS_X, RADIUS_Y);
        roundel.setFill(color);
        roundel.setStroke(Color.BLACK);

        roundel.setTranslateX((Checkerboard.TILE_SIZE_X - RADIUS_X * 2) / 2);
        roundel.setTranslateY((Checkerboard.TILE_SIZE_Y - RADIUS_Y * 2) / 2 + shiftY);

        return roundel;
    }

}
