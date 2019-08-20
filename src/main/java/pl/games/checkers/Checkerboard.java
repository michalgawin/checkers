package pl.games.checkers;

import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import pl.games.checkers.ai.PawnBoard;
import pl.games.checkers.ai.MoveAi;
import pl.games.checkers.ai.TileBoard;

import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.stream.IntStream;

public class Checkerboard implements Copier<PawnBoard> {

    public static final int TILE_SIZE_X = 100;
    public static final int TILE_SIZE_Y = 80;
    public static final int WIDTH = 8;
    public static final int HEIGHT = 8;

    private Group tileGroup = new Group();
    private Group pawnGroup = new Group();

    private final TileBoard tileBoard;

    public Checkerboard() {
        tileBoard = new TileBoard(HEIGHT, WIDTH, (row, column) -> createPawn(row, column));
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
        root.getChildren().addAll(tileGroup, pawnGroup);

        for (int row = 0; row < tileBoard.getHeight(); row++) {
            for (int column = 0; column < tileBoard.getWidth(); column++) {
                tileGroup.getChildren().add(tileBoard.getTile(row, column));
                if (tileBoard.isNotEmpty(row, column))
                    pawnGroup.getChildren().add(tileBoard.getPawn(row, column));
            }
        }

        root.setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));

        return root;
    }

    private PawnFigure createPawn(int row, int column) {
        PawnFigure pawn = null;
        PawnType pawnType = null;

        if (row <= 2) {
            pawnType = PawnType.BLACK;
        }
        if (row >= 5) {
            pawnType = PawnType.WHITE;
        }

        if (pawnType != null) {
            pawn = createPawn(pawnType, row, column);
        }

        return pawn;
    }

    private PawnFigure createPawn(PawnType type, int row, int column) {
        PawnFigure pawn = new PawnFigure(type, row, column);

        pawn.setOnMouseReleased(e -> move(tileBoard, pawn, pawn.nextPosition()));

        return pawn;
    }

    private void move(TileBoard board, PawnFigure pawn, Position nextPosition) {
        move(board, pawn, nextPosition, false);
    }

    private void move(TileBoard board, PawnFigure pawn, Position nextPosition, boolean ai) {
        Move result;
        Position currentPosition = pawn.currentPosition();

        result = tryMove(pawn, nextPosition);
        switch (result.type()) {
            case INVALID:
                board.abortMove(pawn);
                break;
            case MOVE:
                board.move(pawn, currentPosition, nextPosition);
                break;
            case KILL:
                board.move(pawn, currentPosition, nextPosition);
                Pawn otherPawn = result.killedPawn();
                board.removePawn(otherPawn.currentPosition());
                pawnGroup.getChildren().remove(otherPawn);

                Pawn p = new MoveAi(copy(), PawnType.WHITE).getBestMove(pawn);
                if (p != null && p.hasBeating()) {
                    move(board, board.getPawn(p.currentPosition()), p.nextPosition(), ai);
                }

                break;
        }

        if (isNewKing().test(pawn)) {
            pawn.setKing();
        }

        if (!ai && result.type() != MoveType.INVALID) {
            PawnType pType = pawn.getType() == PawnType.WHITE ? PawnType.BLACK : PawnType.WHITE;
            Pawn p = new MoveAi(copy(), pType).getBestMove();
            if (p != null) {
                move(board, board.getPawn(p.currentPosition()), p.nextPosition(), true);
            }
        }
    }

    private Move tryMove(Pawn pawn, Position nextPosition) {
        if (Rules.isOnBoard().negate()
                .or(Rules.isPositionAllowed().negate())
                .or(Rules.isPositionOccupied(tileBoard))
                .or(Rules.isDiagonalMove(pawn).negate())
                .test(nextPosition)) {
            return new Move(MoveType.INVALID);
        }

        return tryMovePawn(pawn, nextPosition);
    }

    private Move tryMovePawn(Pawn pawn, Position nextPosition) {
        int stepsNum = Math.abs(nextPosition.column() - pawn.currentPosition().column());

        Position currentPosition = pawn.currentPosition();
        int xDirection = Integer.compare(nextPosition.column(), currentPosition.column());
        int yDirection = Integer.compare(nextPosition.row(), currentPosition.row());

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

    private Predicate<Pawn> isNewKing() {
        return Rules.isKing().negate().and(Rules.isLastRow());
    }

    private IntFunction<Pawn> getPawnFromTail(Pawn pawn, int xDir, int yDir) {
        return (int i) -> tileBoard.getPawn(pawn.currentPosition().row() + i*yDir, pawn.currentPosition().column() + i*xDir);
    }

    @Override
    public PawnBoard copy() {
        PawnBoard pawns = new PawnBoard(HEIGHT, WIDTH);

        for (int row = 0; row < tileBoard.getHeight(); row++) {
            for (int col = 0; col < tileBoard.getWidth(); col++) {
                if (tileBoard.isNotEmpty(row, col)) {
                    pawns.setPawn(row, col, tileBoard.getPawn(row, col).copy());
                } else {
                    pawns.setPawn(row, col, null);
                }
            }
        }
        return pawns;
    }
}
