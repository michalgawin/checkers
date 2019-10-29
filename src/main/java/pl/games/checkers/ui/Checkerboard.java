package pl.games.checkers.ui;

import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import pl.games.checkers.Copier;
import pl.games.checkers.model.Position;
import pl.games.checkers.Rules;
import pl.games.checkers.model.*;
import pl.games.checkers.ai.MoveAi;

import java.util.Objects;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class Checkerboard implements Copier<Board> {

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

    @Override
    public Board copy() {
        Board board = new PawnBoard(HEIGHT, WIDTH);

        for (int row = 0; row < this.board.getHeight(); row++) {
            for (int col = 0; col < this.board.getWidth(); col++) {
                if (this.board.isNotEmpty(row, col)) {
                    board.setPawn(row, col, this.board.getPawn(row, col).copy());
                } else {
                    board.setPawn(row, col, null);
                }
            }
        }

        return board;
    }

    public Parent drawBoardWithPawns() {
        Pane root = new Pane();

        root.setPrefSize(WIDTH * TILE_SIZE_X, HEIGHT * TILE_SIZE_Y);
        root.getChildren().addAll(((TileBoard) board).getGroups());
        root.setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));

        return root;
    }

    private void move(Board board, Pawn pawn, Position nextPosition) {
        move(board, pawn, nextPosition, false);
    }

    /**
     * @return return true if move was executed by AI, false otherwise
     */
    private boolean move(Board board, Pawn pawn, Position nextPosition, boolean isAi) {
        Move result;
        Position currentPosition = pawn.currentPosition();
        boolean wasAi = isAi;

        result = tryMovePawn(pawn, nextPosition);
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

                Pawn p = new MoveAi(copy(), pawn.getType()).getBestPawnMove(pawn);
                if (p != null && p.hasBeating()) {
                    isAi = move(board, board.getPawn(p.currentPosition()), p.nextPosition(), isAi);
                }

                break;
        }

        if (isNewKing().test(pawn)) {
            pawn.setKing();
        }

        // AI turn if user made a move
        if (!isAi && result.type() != MoveType.INVALID) {
            PawnType pType = pawn.getType() == PawnType.WHITE ? PawnType.BLACK : PawnType.WHITE;
            Pawn p = new MoveAi(copy(), pType).getBestMove();
            if (p != null && wasAi == isAi) {
                move(board, board.getPawn(p.currentPosition()), p.nextPosition(), !isAi);
            }

            return true;
        }

        return false;
    }

    private Move tryMovePawn(Pawn pawn, Position nextPosition) {
        if (canMoveOnBoard(pawn, nextPosition) && canMovePawn(pawn, nextPosition)) {
            return pawnMove(pawn, nextPosition);
        }

        return new Move(MoveType.INVALID);
    }

    private boolean canMoveOnBoard(Pawn pawn, Position nextPosition) {
        return Rules.isOnBoard()
                .and(Rules.isPositionAllowed())
                .and(Rules.isPositionOccupied(board).negate())
                .and(Rules.isDiagonalMove(pawn))
                .test(nextPosition);
    }

    private boolean canMovePawn(Pawn pawn, Position nextPosition) {
        long countedAlliesToBeat = countAlliesToBeat(pawn, nextPosition);
        long countedOpponentsToBeat = countOpponentsToBeat(pawn, nextPosition);

        if (countedOpponentsToBeat > 1 || countedAlliesToBeat > 0) {
            return false;
        } else if (!pawn.isKing()) {
            Position currentPosition = pawn.currentPosition();
            int numOfSteps = numberOfMoves(currentPosition, nextPosition);
            int verticalDirection = verticalDirection(currentPosition, nextPosition);
            boolean toward = verticalDirection == pawn.getType().getDirection();

            if (numOfSteps == 1 && !toward) {
                return false;
            } else if (numOfSteps == 2 && countedOpponentsToBeat != 1) {
                return false;
            } else if (numOfSteps > 2) {
                return false;
            }
        }

        return true;
    }

    private long countAlliesToBeat(Pawn pawn, Position nextPosition) {
        return streamPawnMoves(pawn, nextPosition)
                .filter(Rules.isOpponent(pawn).negate())
                .limit(1)
                .count();
    }

    private long countOpponentsToBeat(Pawn pawn, Position nextPosition) {
        return streamPawnMoves(pawn, nextPosition)
                .filter(Rules.isOpponent(pawn))
                .limit(2)
                .count();
    }

    private Move pawnMove(Pawn pawn, Position nextPosition) {
        return streamPawnMoves(pawn, nextPosition)
                .filter(Rules.isOpponent(pawn))
                .findAny()
                .map(p -> new Move(MoveType.KILL, p))
                .orElse(new Move(MoveType.MOVE));
    }

    private Stream<Pawn> streamPawnMoves(Pawn pawn, Position nextPosition) {
        Position currentPosition = pawn.currentPosition();
        int hDir = horizontalDirection(currentPosition, nextPosition);
        int vDir = verticalDirection(currentPosition, nextPosition);

        return Stream.iterate(currentPosition.increment(vDir, hDir), p -> !p.equals(nextPosition), p -> p.increment(vDir, hDir))
                .limit(WIDTH)
                .map(p -> board.getPawn(p))
                .filter(Objects::nonNull);
    }

    private Predicate<Pawn> isNewKing() {
        return Rules.isKing().negate().and(Rules.isLastRow());
    }

    private int numberOfMoves(Position currentPosition, Position nextPosition) {
        int colDiff = Math.abs(nextPosition.column() - currentPosition.column());
        assert colDiff == Math.abs(nextPosition.row() - currentPosition.row());

        return colDiff;
    }

    private int horizontalDirection(Position currentPosition, Position nextPosition) {
        return Integer.compare(nextPosition.column(), currentPosition.column());
    }

    private int verticalDirection(Position currentPosition, Position nextPosition) {
        return Integer.compare(nextPosition.row(), currentPosition.row());
    }

}
