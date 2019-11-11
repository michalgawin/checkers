package pl.games.checkers.model;

import pl.games.checkers.Copier;
import pl.games.checkers.Rules;
import pl.games.checkers.ai.GameTree;
import pl.games.checkers.ai.MoveValue;
import pl.games.checkers.ai.PawnMoveRecursive;

import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Board represents checkerboard with pawns and provides basic operations on pawns.
 * @param <T> implementation of Pawn
 */
public abstract class Board<T extends Pawn> implements Copier<Board<T>> {

    public static final int DEEPNESS = 6;
    private final int width;
    private final int height;

    public Board(int height, int width) {
        this.height = height;
        this.width = width;
    }

    public boolean isEmpty(int y, int x) {
        return getPawn(y, x) == null;
    }

    public boolean isNotEmpty(int y, int x) {
        return !isEmpty(y, x);
    }

    public Board setPawn(Position position, T pawn) {
        return setPawn(position.row(), position.column(), pawn);
    }

    public abstract Board setPawn(int y, int x, T pawn);

    public T getPawn(Position position) {
        return getPawn(position.row(), position.column());
    }

    public abstract T getPawn(int y, int x);

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    /**
     *
     * @param currentPosition
     * @return
     */
    public Board removePawn(Position currentPosition) {
        return setPawn(currentPosition, null);
    }

    public Board beat(T pawn, T beat, Position currentPosition, Position nextPosition) {
        removePawn(beat.currentPosition());
        move(pawn, currentPosition, nextPosition);
        return this;
    }

    public Board abortMove(Pawn pawn) {
        pawn.abortMove();
        return this;
    }

    public Pawn[][] getPawnsCopy() {
        Pawn[][] pawnsCopy = new Pawn[getHeight()][getWidth()];

        for (int row = 0; row < getHeight(); row++) {
            for (int col = 0; col < getWidth(); col++) {
                if (getPawn(row, col) != null) {
                    pawnsCopy[row][col] = getPawn(row, col).copy();
                } else {
                    pawnsCopy[row][col] = null;
                }
            }
        }

        return pawnsCopy;
    }

    @Override
    public Board copy() {
        Board board = new PawnBoard(getHeight(), getWidth());

        for (int row = 0; row < this.getHeight(); row++) {
            for (int col = 0; col < this.getWidth(); col++) {
                if (this.isNotEmpty(row, col)) {
                    board.setPawn(row, col, this.getPawn(row, col).copy());
                } else {
                    board.setPawn(row, col, null);
                }
            }
        }

        return board;
    }

    public Board move(T pawn, Position currentPosition, Position nextPosition) {
        pawn.move(nextPosition);
        setPawn(currentPosition, null);
        setPawn(nextPosition, pawn);
        return this;
    }

    public void move(T pawn, Position nextPosition) {
        move(this, pawn, nextPosition, false);
    }

    /**
     * @return return true if move was executed by AI, false otherwise
     */
    private boolean move(Board board, T pawn, Position nextPosition, boolean isAi) {
        Move result;
        Position currentPosition = pawn.currentPosition();
        boolean wasAi = isAi;

        result = getPawnMove(pawn, nextPosition);
        switch (result.type()) {
        case INVALID:
            board.abortMove(pawn);
            break;
        case MOVE:
            board.move(pawn, currentPosition, nextPosition);
            break;
        case KILL:
            board.beat(pawn, result.killedPawn(), currentPosition, nextPosition);

            MoveValue bestMove = PawnMoveRecursive.getNextMove(board, pawn);
            if (bestMove != null && bestMove.getPawn().hasBeating()) {
                Pawn p = bestMove.getPawn();
                isAi = move(board, this.getPawn(p.currentPosition()), p.nextPosition(), isAi);
            }

            break;
        }

        if (isNewKing().test(pawn)) {
            pawn.setKing();
        }

        if (!isAi && result.type() != MoveType.INVALID) {// AI turn if user made a move
            Pawn p = new GameTree(board, pawn.getType().negate()).getBestMoveOfPawn(DEEPNESS);
            if (p != null && wasAi == isAi) {
                move(board, this.getPawn(p.currentPosition()), p.nextPosition(), !isAi);
            }

            return true;
        }

        return false;
    }

    private Move getPawnMove(T pawn, Position nextPosition) {
        if (isValidMoveOnBoard(pawn, nextPosition) && isValidMovePawn(pawn, nextPosition)) {
            return pawnMove(pawn, nextPosition);
        }

        return new Move(MoveType.INVALID);
    }

    private boolean isValidMoveOnBoard(Pawn pawn, Position nextPosition) {
        return Rules.isOnBoard()
                .and(Rules.isPositionAllowed())
                .and(Rules.isDiagonalMove(pawn))
                .test(nextPosition);
    }

    private boolean isValidMovePawn(Pawn pawn, Position nextPosition) {
        if (Rules.isPositionOccupied(this)
                .or(Rules.isIllegalBeating(this, pawn))
                .test(nextPosition)) {
            return false;
        } else if (!pawn.isKing()) {
            Position currentPosition = pawn.currentPosition();
            int numOfSteps = numberOfMoves(currentPosition, nextPosition);
            int verticalDirection = verticalDirection(currentPosition, nextPosition);
            boolean toward = verticalDirection == pawn.getType().getDirection();

            if (numOfSteps == 1 && !toward) {
                return false;
            } else if (numOfSteps == 2 &&
                    Rules.isAllowedBeating(this, pawn).negate()
                            .test(nextPosition)) {
                return false;
            } else if (numOfSteps > 2) {
                return false;
            }
        }

        return true;
    }

    private Move pawnMove(T pawn, Position nextPosition) {
        return streamPawnMoves(pawn, nextPosition)
                .filter(Rules.isOpponent(pawn))
                .findAny()
                .map(p -> new Move(MoveType.KILL, p))
                .orElse(new Move(MoveType.MOVE));
    }

    private Stream<T> streamPawnMoves(T pawn, Position nextPosition) {
        Position currentPosition = pawn.currentPosition();
        int hDir = horizontalDirection(currentPosition, nextPosition);
        int vDir = verticalDirection(currentPosition, nextPosition);

        return Stream.iterate(currentPosition.increment(vDir, hDir), p -> !p.equals(nextPosition), p -> p.increment(vDir, hDir))
                .limit(width)
                .map(p -> this.getPawn(p))
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
