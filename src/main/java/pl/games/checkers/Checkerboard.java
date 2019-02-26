package pl.games.checkers;

import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import java.util.Objects;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.stream.IntStream;

public class Checkerboard {

    public static final int TILE_SIZE_X = 100;
    public static final int TILE_SIZE_Y = 80;
    public static final int WIDTH = 8;
    public static final int HEIGHT = 8;

    private final Pane pane;
    private final Tile[][] board;
    private final Group tileGroup;
    private Group pawnGroup;

    public static int toBoardWidth(double position) {
        return (int)(position + TILE_SIZE_X / 2) / TILE_SIZE_X;
    }

    public static int toBoardHeight(double position) {
        return (int)(position + TILE_SIZE_Y / 2) / TILE_SIZE_Y;
    }

    public static Parent createBoardWithPawns() {
        Checkerboard checkerboard = new Checkerboard();

        for (int row = 0; row < HEIGHT; row++) {
            for (int column = 0; column < WIDTH; column++) {
                Tile tile = new Tile(column, row);

                if (tile.isAllowed()) {
                    PawnType pawnType = null;

                    if (row <= 2) {
                        pawnType = PawnType.BLACK;
                    }
                    if (row >= 5) {
                        pawnType = PawnType.WHITE;
                    }

                    checkerboard.addTileWithPawn(column, row, tile, pawnType);
                } else {
                    checkerboard.addTile(column, row, tile);
                }
            }
        }

        return checkerboard.getPane();
    }

    private Checkerboard() {
        board = new Tile[WIDTH][HEIGHT];

        pane = new Pane();
        pane.setPrefSize(WIDTH * TILE_SIZE_X, HEIGHT * TILE_SIZE_Y);
        pane.setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));

        tileGroup = new Group();
        pawnGroup = new Group();

        pane.getChildren().addAll(tileGroup, pawnGroup);
    }

    private Pane getPane() {
        return pane;
    }

    private void addTile(int column, int row, Tile tile) {
        addTileWithPawn(column, row, tile, null);
    }

    private void addTileWithPawn(int column, int row, Tile tile, PawnType pawnType) {
        board[column][row] = tile;
        tileGroup.getChildren().add(tile);

        if (pawnType != null) {
            addPawn(column, row, tile, createPawn(pawnType, column, row));
        }
    }

    private Pawn createPawn(PawnType type, int column, int row) {
        Pawn pawn = new Pawn(type, column, row);

        pawn.setOnMouseReleased(e -> move(pawn, pawn.nextPosition()));

        return pawn;
    }

    private void addPawn(int column, int row, Tile tile, Pawn pawn) {
        tile.setPawn(pawn);
        pawnGroup.getChildren().add(pawn);
    }

    private void removePawn(Pawn pawn) {
        getTile(pawn.lastPosition().x, pawn.lastPosition().y).setPawn(null);
        pawnGroup.getChildren().remove(pawn);
    }

    private void movePawn(Pawn pawn, Position nextPosition) {
        Position lastPosition = pawn.lastPosition();

        pawn.move(nextPosition);
        getTile(lastPosition.x, lastPosition.y).setPawn(null);
        getTile(nextPosition.x, nextPosition.y).setPawn(pawn);
    }

    private Tile getTile(int row, int column) {
        return board[row][column];
    }

    private void move(Pawn pawn, Position nextPosition) {
        Move result;
        Position lastPosition = pawn.lastPosition();

        result = tryMove(pawn, nextPosition);
        switch (result.type()) {
            case INVALID:
                pawn.cancelMove();
                break;
            case MOVE:
                movePawn(pawn, nextPosition);
                break;
            case KILL:
                movePawn(pawn, nextPosition);
                removePawn(result.killedPawn());

                if (isKingCandidate().test(pawn)) {
                    Position position = new Position(
                            nextPosition.x + Integer.compare(nextPosition.x, lastPosition.x) * 2, lastPosition.y);
                    move(pawn, position);
                }
                break;
        }

        if (isKingCandidate().test(pawn)) {
            pawn.setKing();
        }
    }

    private Move tryMove(Pawn pawn, Position nextPosition) {
        if (isWithinRange().negate()
                .or(Rules.isTailAllowed().negate())
                .or(Rules.isTailBusy(board))
                .or(Rules.isDiagonalMove(pawn).negate())
                .test(nextPosition)) {
            return new Move(MoveType.INVALID);
        }

        return tryMovePawn(pawn, nextPosition);
    }

    private Move tryMovePawn(Pawn pawn, Position nextPosition) {
        int stepsNum = Math.abs(nextPosition.x - pawn.lastPosition().x);

        Position lastPosition = pawn.lastPosition();
        int xDirection = Integer.compare(nextPosition.x, lastPosition.x);
        int yDirection = Integer.compare(nextPosition.y, lastPosition.y);

        boolean toward = yDirection == pawn.getType().direction;

        long alliesToKill = IntStream.range(1, stepsNum).mapToObj(getPawnFromTail(pawn, xDirection, yDirection))
                .filter(hasPawn())
                .filter(Rules.isOpponent(pawn).negate())
                .limit(1)
                .count();
        long opponentsToKill = IntStream.range(1, stepsNum).mapToObj(getPawnFromTail(pawn, xDirection, yDirection))
                .filter(hasPawn())
                .filter(Rules.isOpponent(pawn))
                .limit(2)
                .count();

        if (opponentsToKill > 1 || alliesToKill > 0) {
            return new Move(MoveType.INVALID);
        } else if (!pawn.isKing()) {
            if (stepsNum == 1) {
                if (!toward) {
                    return new Move(MoveType.INVALID);
                }
            } else if (stepsNum == 2) {
                if (opponentsToKill != 1) {
                    return new Move(MoveType.INVALID);
                }
            } else {
                return new Move(MoveType.INVALID);
            }
        }

        return IntStream.range(1, stepsNum).mapToObj(getPawnFromTail(pawn, xDirection, yDirection))
                .filter(hasPawn())
                .filter(Rules.isOpponent(pawn))
                .findAny()
                .map(p -> new Move(MoveType.KILL, p))
                .orElse(new Move(MoveType.MOVE));
    }

    private Predicate<Pawn> hasPawn() {
        return pawn -> pawn != null;
    }

    private Predicate<Pawn> isKingCandidate() {
        return Rules.isKing().negate().and(Rules.isLastRow());
    }

    private Predicate<Position> isWithinRange() {
        return Rules.isWithinRange(0, Checkerboard.WIDTH, 0, Checkerboard.HEIGHT);
    }

    private IntFunction<Pawn> getPawnFromTail(Pawn pawn, int xDir, int yDir) {
        return (int i) -> getTile(pawn.lastPosition().x + i*xDir, pawn.lastPosition().y + i*yDir).getPawn();
    }

}
