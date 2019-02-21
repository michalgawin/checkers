package pl.games.checkers;

import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.stream.IntStream;

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

        pawn.setOnMouseReleased(e -> move(pawn, pawn.nextPosition()));

        return pawn;
    }

    private void move(Pawn pawn, Position nextPosition) {
        Move result;
        Position currentPosition = pawn.currentPosition();

        result = tryMove(pawn, nextPosition);
        switch (result.type()) {
            case INVALID:
                pawn.abortMove();
                break;
            case MOVE:
                pawn.move(nextPosition);
                board[currentPosition.x][currentPosition.y].setPawn(null);
                board[nextPosition.x][nextPosition.y].setPawn(pawn);
                break;
            case KILL:
                pawn.move(nextPosition);
                board[currentPosition.x][currentPosition.y].setPawn(null);
                board[nextPosition.x][nextPosition.y].setPawn(pawn);

                Pawn otherPawn = result.capturedPawn();
                board[otherPawn.currentPosition().x][otherPawn.currentPosition().y].setPawn(null);
                pawnGroup.getChildren().remove(otherPawn);

                if (isKingCandidate().test(pawn)) {
                    Position position = new Position(
                            nextPosition.x + Integer.compare(nextPosition.x, currentPosition.x) * 2, currentPosition.y);
                    move(pawn, position);
                }
                break;
        }

        if (isKingCandidate().test(pawn)) {
            pawn.setKing();
        }
    }

    private Move tryMove(Pawn pawn, Position nextPosition) {
        if (isNotAllowedMove().test(nextPosition)) {
            return new Move(MoveType.INVALID);
        }

        if (pawn.isKing()) {
            return tryMoveKing(pawn, nextPosition);
        }
        return tryMovePawn(pawn, nextPosition);
    }

    private Move tryMoveKing(Pawn pawn, Position nextPosition) {
        if (Rules.isDiagonalMove(nextPosition).negate().test(pawn)) {
            return new Move(MoveType.INVALID);
        }

        int stepsNum = Math.abs(nextPosition.x - pawn.currentPosition().x);

        Position currentPosition = pawn.currentPosition();
        int xDirection = Integer.compare(nextPosition.x, currentPosition.x);
        int yDirection = Integer.compare(nextPosition.y, currentPosition.y);

        long opponentsToKill = IntStream.range(1, stepsNum).mapToObj(getPawn(pawn, xDirection, yDirection))
                .filter(p -> p != null)
                .filter(p -> p.getType() != pawn.getType())
                .limit(2)
                .count();
        long ownPawns = IntStream.range(1, stepsNum).mapToObj(getPawn(pawn, xDirection, yDirection))
                .filter(p -> p != null)
                .filter(p -> p.getType() == pawn.getType())
                .limit(1)
                .count();
        if (opponentsToKill > 1 || ownPawns > 0) {
            return new Move(MoveType.INVALID);
        }

        return IntStream.range(1, stepsNum).mapToObj(getPawn(pawn, xDirection, yDirection))
                .filter(p -> p != null)
                .filter(p -> p.getType() != pawn.getType())
                .findAny()
                .map(p -> new Move(MoveType.KILL).capturedPawn(p))
                .orElse(new Move(MoveType.MOVE));
    }

    private Move tryMovePawn(Pawn pawn, Position nextPosition) {
        if (Rules.isDiagonalMove(nextPosition).negate().test(pawn)) {
            return new Move(MoveType.INVALID);
        }

        int stepsNum = Math.abs(nextPosition.x - pawn.currentPosition().x);

        Position currentPosition = pawn.currentPosition();
        int xDirection = Integer.compare(nextPosition.x, currentPosition.x);
        int yDirection = Integer.compare(nextPosition.y, currentPosition.y);

        long ownPawns = 0;
        long opponentsToKill = 0;
        boolean toward = yDirection == pawn.getType().direction;

        if (stepsNum == 1 && toward) {
            ownPawns = IntStream.range(1, stepsNum).mapToObj(getPawn(pawn, xDirection, yDirection))
                    .filter(p -> p != null)
                    .filter(p -> p.getType() == pawn.getType())
                    .limit(1)
                    .count();
        } else if (stepsNum == 2) {
            opponentsToKill = IntStream.range(1, stepsNum).mapToObj(getPawn(pawn, xDirection, yDirection))
                    .filter(p -> p != null)
                    .filter(p -> p.getType() != pawn.getType())
                    .limit(2)
                    .count();
        }
        if ((opponentsToKill == 0 && !toward) || opponentsToKill > 1 || ownPawns > 0) {
            return new Move(MoveType.INVALID);
        }

        return IntStream.range(1, stepsNum).mapToObj(getPawn(pawn, xDirection, yDirection))
                .filter(p -> p != null)
                .filter(p -> p.getType() != pawn.getType())
                .findAny()
                .map(p -> new Move(MoveType.KILL).capturedPawn(p))
                .orElse(new Move(MoveType.MOVE));
    }

    private Predicate<Position> isNotAllowedMove() {
        return isWithinBoard().negate()
                .or(isTailBusy())
                .or(isTailAllowed().negate());
    }

    private Predicate<Position> isWithinBoard() {
        return Rules.isWithinRange(0, Checkerboard.WIDTH, 0, Checkerboard.HEIGHT);
    }

    private Predicate<Position> isTailBusy() {
        return Rules.isTailBusy(board);
    }

    private Predicate<Position> isOpponent(Pawn pawn) {
        return Rules.isOpponent(board, pawn);
    }

    private Predicate<Position> isTailAllowed() {
        return Rules.isTailAllowed();
    }

    private Predicate<Pawn> isKingCandidate() {
        return Rules.isKing().negate().and(Rules.isOnOppositeBorder());
    }

    private IntFunction<Pawn> getPawn(Pawn pawn, int xDir, int yDir) {
        return (int i) -> board[pawn.currentPosition().x + i*xDir][pawn.currentPosition().y + i*yDir].getPawn();
    }

}
