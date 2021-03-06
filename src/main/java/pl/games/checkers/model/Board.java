package pl.games.checkers.model;

import pl.games.checkers.Rules;
import pl.games.checkers.ai.GameTree;
import pl.games.checkers.ai.algorithm.Minimax;
import pl.games.checkers.ai.algorithm.NextMove;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Board represents checkerboard with pawns and provides basic operations on pawns.
 * @param <T> implementation of Pawn
 */
public abstract class Board<T extends Pawn> {

    public static final int DEEPNESS = 6;
    private final int width;
    private final int height;
    private final NextMove algorithm = new Minimax(); //new SimplyBest();

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

    public Board removePawn(Position currentPosition) {
        return setPawn(currentPosition, null);
    }

    public Board beatPawn(T pawn, T beat, Position currentPosition, Position nextPosition) {
        removePawn(beat.currentPosition());
        movePawn(pawn, currentPosition, nextPosition);
        return this;
    }

    public Board abortPawnMove(Pawn pawn) {
        pawn.abortMove();
        return this;
    }

    public Board movePawn(T pawn, Position currentPosition, Position nextPosition) {
        pawn.move(nextPosition);
        setPawn(currentPosition, null);
        setPawn(nextPosition, pawn);
        return this;
    }

    public Board<T> move(T pawn, Position nextPosition, boolean isAi) {
        move(this, pawn, nextPosition, isAi);
        return this;
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
            board.abortPawnMove(pawn);
            break;
        case MOVE:
            board.movePawn(pawn, currentPosition, nextPosition);
            break;
        case KILL:
            board.beatPawn(pawn, result.killedPawn(), currentPosition, nextPosition);

            GameTree gameTree = new GameTree(board, pawn.getType())
                    .constraint(pawn)
                    .buildTree();
            Pawn bestMove = algorithm.nextMove(gameTree);
            if (bestMove != null && bestMove.hasBeating()) {
                Pawn p = bestMove;
                isAi = move(board, this.getPawn(p.currentPosition()), p.nextPosition(), isAi);
            }

            break;
        }

        if (isNewKing().test(pawn)) {
            pawn.setKing();
        }

        if (!isAi && result.type() != MoveType.INVALID) {// AI turn if user made a move
            GameTree gameTree = new GameTree(board, pawn.getType().negate(), DEEPNESS)
                    .buildTree();
            Pawn p = algorithm.nextMove(gameTree);
            if (p != null && wasAi == isAi) {
                isAi = move(board, this.getPawn(p.currentPosition()), p.nextPosition(), !isAi);
            }
        }

        return isAi;
    }

    public List<T> pawnsAsList() {
        Function<Integer, List<T>> getFromRow = row -> IntStream.range(0, getWidth())
                .mapToObj(col -> getPawn(row, col))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        List<T> pawns = IntStream.range(0, getHeight())
                .mapToObj(row -> getFromRow.apply(row))
                .flatMap(pawnsRow -> pawnsRow.stream())
                .collect(Collectors.toList());

        return pawns;
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
            int numOfSteps = countPositionChanges(currentPosition, nextPosition);
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

    private int countPositionChanges(Position currentPosition, Position nextPosition) {
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
