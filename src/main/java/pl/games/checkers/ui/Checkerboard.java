package pl.games.checkers.ui;

import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import pl.games.checkers.model.Position;
import pl.games.checkers.model.*;

public class Checkerboard {

    public static final int TILE_SIZE_X = 100;
    public static final int TILE_SIZE_Y = 80;
    public static final int WIDTH = 8;
    public static final int HEIGHT = 8;

    private final Board board;

    public Checkerboard() {
        board = new TileBoard(HEIGHT, WIDTH, (b, p) -> e -> move(b, p, p.nextPosition()));
    }

    public static int toBoardWidth(double position) {
        return (int)(position + TILE_SIZE_X / 2) / TILE_SIZE_X;
    }

    public static int toBoardHeight(double position) {
        return (int)(position + TILE_SIZE_Y / 2) / TILE_SIZE_Y;
    }

    public Parent drawBoardWithPawns() {
        Pane root = new Pane();

        root.setPrefSize(WIDTH * TILE_SIZE_X, HEIGHT * TILE_SIZE_Y);
        root.getChildren().addAll(((TileBoard) board).getGroups());
        root.setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));

        return root;
    }

    public Board getBoard() {
        return board;
    }

    private void move(Board board, Pawn pawn, Position nextPosition) {
        board.move(pawn, nextPosition);
    }

}
