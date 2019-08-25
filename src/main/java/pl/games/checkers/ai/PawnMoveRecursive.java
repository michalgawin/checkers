package pl.games.checkers.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.games.checkers.*;
import pl.games.checkers.model.*;

import java.util.*;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import java.util.function.BiFunction;


public class PawnMoveRecursive extends RecursiveTask<Map.Entry<Integer, Pawn>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PawnMoveRecursive.class);

    private static final int BEAT = 3;
    private static final int WALK = 1;
    private static final int INVALID = -1;

    private final Board pawnBoard;
    private final Pawn pawn;
    private final boolean fork;

    public PawnMoveRecursive(Board pawnBoard, Pawn pawn) {
        this(pawnBoard, pawn, true);
    }

    public PawnMoveRecursive(Board pawnBoard, Pawn pawn, boolean fork) {
        this.pawnBoard = pawnBoard;
        this.pawn = pawn;
        this.fork = fork;
    }

    @Override
    protected Map.Entry<Integer, Pawn> compute() {
        if (fork) {
            return ForkJoinTask.invokeAll(createMovesOf(pawnBoard, pawn)).stream()
                    .map(ForkJoinTask::join)
                    .max(Map.Entry.comparingByKey())
                    .orElse(null);
        }

        return getMove();
    }

    private List<PawnMoveRecursive> createMovesOf(Board board, Pawn pawn) {
        List<PawnMoveRecursive> pawnMoveRecursives = new ArrayList<>();

        pawnMoveRecursives.addAll(changePosition(new PawnBoard(board), Position::towardLeft, pawn));
        pawnMoveRecursives.addAll(changePosition(new PawnBoard(board), Position::towardRight, pawn));
        pawnMoveRecursives.addAll(changePosition(new PawnBoard(board), Position::backwardLeft, pawn));
        pawnMoveRecursives.addAll(changePosition(new PawnBoard(board), Position::backwardRight, pawn));

        return pawnMoveRecursives;
    }

    private Map.Entry<Integer, Pawn> getMove() {
        if (pawnBoard.getPawn(pawn.nextPosition()) == null &&
                Rules.isOnBoard().test(pawn.nextPosition())) {
            Position currentPosition = pawn.currentPosition();
            int yDirection = Integer.compare(pawn.nextPosition().row(), currentPosition.row());
            boolean toward = yDirection == pawn.getType().getDirection();

            if (pawn.hasBeating()) {
                return new AbstractMap.SimpleEntry<>(BEAT, pawn);
            } else if (pawn.isKing()){
                return new AbstractMap.SimpleEntry<>(WALK, pawn);
            } else if (toward) {
                return new AbstractMap.SimpleEntry<>(WALK, pawn);
            }
        }
        return new AbstractMap.SimpleEntry<>(INVALID, pawn);
    }

    private List<PawnMoveRecursive> changePosition(Board pawnBoard, BiFunction<Position, Integer, Position> operation, Pawn pawn) {
        List<PawnMoveRecursive> pawnMoveRecursives = new ArrayList<>();

        int direction = pawn.getType().getDirection();
        for (Position position = operation.apply(pawn.currentPosition(), direction);
             Rules.isOnBoard().test(position);
             position = operation.apply(position, direction)
        ) {
            Pawn p = pawn.copy();
            PawnMoveRecursive pawnMoveRecursive = changePosition(new PawnBoard(pawnBoard), p, position, operation);
            if (pawnMoveRecursive != null) {
                pawnMoveRecursives.add(pawnMoveRecursive);
            } else {
                break;
            }

            if (!p.isKing()) { //one move for not king
                break;
            }
        }

        return pawnMoveRecursives;
    }

    private PawnMoveRecursive changePosition(Board pawnBoard, Pawn p, Position position, BiFunction<Position, Integer, Position> operation) {
        int direction = p.getType().getDirection();
        p.nextPosition(position);

        if (Rules.isOnBoard().negate().test(p.nextPosition())) {
            return null;
        }

        int yDirection = Integer.compare(p.nextPosition().row(), p.currentPosition().row());
        boolean toward = yDirection == p.getType().getDirection();

        if (pawnBoard.getPawn(p.nextPosition()) == null) {
            if (toward) {
                p.setMove(new Move(MoveType.MOVE));
                return new PawnMoveRecursive(pawnBoard, p, false);
            } else if (p.isKing()) {
                p.setMove(new Move(MoveType.MOVE));
                return new PawnMoveRecursive(pawnBoard, p, false);
            }

            return null;
        } else if (Rules.isOpponent(pawnBoard.getPawn(p.nextPosition())).test(p)) { //check killing
            Pawn victim = pawnBoard.getPawn(p.nextPosition());
            p.nextPosition(operation.apply(p.nextPosition(), direction));
            if (Rules.isOnBoard().negate().test(p.nextPosition())) {
                return null;
            }
            if (pawnBoard.getPawn(p.nextPosition()) == null) {
                p.setMove(new Move(MoveType.KILL, victim));
                return new PawnMoveRecursive(pawnBoard, p, false);
            }
        }

        return null;
    }

}
