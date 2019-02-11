package pl.games.checkers;

import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

public class Checkerboard {

    public static final int TILE_SIZE_X = 100;
    public static final int TILE_SIZE_Y = 80;
    public static final int WIDTH = 8;
    public static final int HEIGHT = 8;

    private Group tileGroup = new Group();
    private Group pawnGroup = new Group();

    private final Tile[][] board = new Tile[WIDTH][HEIGHT];

    public Checkerboard() {
    }

    public static int toBoardWidth(double position) {
        return (int)(position + TILE_SIZE_X / 2) / TILE_SIZE_X;
    }

    public static int toBoardHeight(double position) {
        return (int)(position + TILE_SIZE_Y / 2) / TILE_SIZE_Y;
    }

    public Parent createBoardWithPawns() {
        Pane root = new Pane();
        root.setPrefSize(WIDTH * TILE_SIZE_X, HEIGHT * TILE_SIZE_Y);
        root.getChildren().addAll(tileGroup, pawnGroup);

        for (int row = 0; row < HEIGHT; row++) {
            for (int column = 0; column < WIDTH; column++) {
                Tile tile = new Tile(column, row);
                board[column][row] = tile;

                tileGroup.getChildren().add(tile);

                if (tile.isAllowed()) {
                    PawnType pawnType = null;

                    if (row <= 2) {
                        pawnType = PawnType.BLACK;
                    }
                    if (row >= 5) {
                        pawnType = PawnType.WHITE;
                    }

                    if (pawnType != null) {
                        Pawn pawn = createPawn(pawnType, column, row);
                        tile.setPawn(pawn);
                        pawnGroup.getChildren().add(pawn);
                    }
                }
            }
        }
        root.setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));

        return root;
    }

    private Pawn createPawn(PawnType type, int column, int row) {
        Pawn pawn = new Pawn(type, column, row);

        pawn.setOnMouseReleased(e -> move(pawn, pawn.nextColumn(), pawn.nextRow()));

        return pawn;
    }

    private void move(Pawn pawn, int nextColumn, int nextRow) {
        Move result;

        result = tryMove(pawn, nextColumn, nextRow);

        int lastColumn = pawn.lastColumn();
        int lastRow = pawn.lastRow();

        switch (result.type()) {
            case INVALID:
                pawn.abortMove();
                break;
            case MOVE:
                pawn.move(nextColumn, nextRow);
                board[lastColumn][lastRow].setPawn(null);
                board[nextColumn][nextRow].setPawn(pawn);
                break;
            case KILL:
                pawn.move(nextColumn, nextRow);
                board[lastColumn][lastRow].setPawn(null);
                board[nextColumn][nextRow].setPawn(pawn);

                Pawn otherPawn = result.capturedPawn();
                board[otherPawn.lastColumn()][otherPawn.lastRow()].setPawn(null);
                pawnGroup.getChildren().remove(otherPawn);

                if (!pawn.isKing() && isOnTop(pawn)) {
                    move(pawn, nextColumn + Integer.compare(nextColumn, lastColumn) * 2, lastRow);
                }
                break;
        }

        if (!pawn.isKing() && isOnTop(pawn)) {
            pawn.setKing();
        }
    }

    private boolean isOnTop(Pawn pawn) {
        return (pawn.getType().direction < 0 && pawn.nextRow() == 0) ||
                (pawn.getType().direction > 0 && pawn.nextRow() == Checkerboard.HEIGHT - 1);
    }

    private Move tryMove(Pawn pawn, int nextColumn, int nextRow) {
        if (!withinBoard(nextColumn, nextRow) ||
                board[nextColumn][nextRow].hasPiece() ||
                Tile.isNotAllowed(nextColumn, nextRow)) {
            return new Move(MoveType.INVALID);
        }

        int lastColumn = pawn.lastColumn();
        int lastRow = pawn.lastRow();

        if (pawn.isKing()) {
            return tryMoveKing(pawn, nextColumn, nextRow, lastColumn, lastRow);
        }
        return tryMovePawn(pawn, nextColumn, nextRow, lastColumn, lastRow);
    }

    private boolean withinBoard(int column, int row) {
        return (column >= 0 && column < Checkerboard.WIDTH) && (row >= 0 && row < Checkerboard.HEIGHT);
    }

    private Move tryMoveKing(Pawn pawn, int nextColumn, int nextRow, int lastColumn, int lastRow) {
        if (Math.abs(nextColumn - lastColumn) == Math.abs(nextRow - lastRow)) {
            if (nextColumn - lastColumn == 0) {
                return new Move(MoveType.INVALID);
            }
            int columnsDiff = nextColumn - lastColumn;
            int incrementCol = Integer.compare(nextColumn, lastColumn);
            int rowsDiff = nextRow - lastRow;
            int incrementRow = Integer.compare(nextRow, lastRow);

            Move killingMove = null;
            //diagonal move
            for(int column = incrementCol, row = incrementRow;
                (Math.abs(column) < Math.abs(columnsDiff)) && (Math.abs(row) < Math.abs(rowsDiff));
                column += incrementCol, row += incrementRow)
            {
                int columnInCapture = lastColumn + column;
                int rowInCapture = lastRow + row;

                if (board[columnInCapture][rowInCapture].hasPiece()) {
                    if (board[columnInCapture][rowInCapture].getPawn().getType() == pawn.getType()) { //passing over own pawn
                        return new Move(MoveType.INVALID);
                    } else if (killingMove != null) { //passing over more than one opposed pawn
                        return new Move(MoveType.INVALID);
                    } else { //capturing pawn
                        killingMove = new Move(MoveType.KILL).capturedPawn(board[columnInCapture][rowInCapture].getPawn());
                    }
                }
            }
            if (killingMove != null) {
                return killingMove;
            }

            return new Move(MoveType.MOVE);
        }

        return new Move(MoveType.INVALID);
    }

    private Move tryMovePawn(Pawn pawn, int nextColumn, int nextRow, int lastColumn, int lastRow) {
        if (Math.abs(nextColumn - lastColumn) == 1 && nextRow - lastRow == pawn.getType().direction) {
            return new Move(MoveType.MOVE);
        } else if (Math.abs(nextColumn - lastColumn) == 2 &&
                Math.abs(nextRow - lastRow) == Math.abs(pawn.getType().direction * 2)) {
            int columnInCapture = lastColumn + (nextColumn - lastColumn) / 2;
            int rowInCapture = lastRow + (nextRow - lastRow) / 2;

            if (board[columnInCapture][rowInCapture].hasPiece() &&
                    board[columnInCapture][rowInCapture].getPawn().getType() != pawn.getType()) {
                return new Move(MoveType.KILL).capturedPawn(board[columnInCapture][rowInCapture].getPawn());
            }
        }

        return new Move(MoveType.INVALID);
    }

}
