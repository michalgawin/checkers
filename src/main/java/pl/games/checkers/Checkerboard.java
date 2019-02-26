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
        Position lastPosition = pawn.lastPosition();

        result = tryMove(pawn, nextPosition);
        switch (result.type()) {
            case INVALID:
                pawn.cancelMove();
                break;
            case MOVE:
                pawn.move(nextPosition);
                board[lastPosition.x][lastPosition.y].setPawn(null);
                board[nextPosition.x][nextPosition.y].setPawn(pawn);
                break;
            case KILL:
                pawn.move(nextPosition);
                board[lastPosition.x][lastPosition.y].setPawn(null);
                board[nextPosition.x][nextPosition.y].setPawn(pawn);

                Pawn otherPawn = result.killedPawn();
                board[otherPawn.lastPosition().x][otherPawn.lastPosition().y].setPawn(null);
                pawnGroup.getChildren().remove(otherPawn);

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
        if (Rules.isWithinRange(0, Checkerboard.WIDTH, 0, Checkerboard.HEIGHT).negate()
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

    private IntFunction<Pawn> getPawnFromTail(Pawn pawn, int xDir, int yDir) {
        return (int i) -> board[pawn.lastPosition().x + i*xDir][pawn.lastPosition().y + i*yDir].getPawn();
    }

}
