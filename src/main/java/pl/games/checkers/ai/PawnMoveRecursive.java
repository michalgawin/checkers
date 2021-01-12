package pl.games.checkers.ai;

import pl.games.checkers.*;
import pl.games.checkers.model.*;

import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class PawnMoveRecursive extends RecursiveTask<List<MoveRate>> {

    private static final ForkJoinPool forkJoinPool = ForkJoinPool.commonPool();

    private static final int BEAT = 3;
    private static final int WALK = 1;
    private static final int INVALID = Integer.MIN_VALUE;

    private final Board pawnBoard;
    private final Pawn pawn;
    private final boolean fork;

    /**
     * Method check moves in all directions and return the best one if exists even if is not acceptable by rules.
     * @param pawn pawn to analyze
     * @return best move if exists or null
     */
    public static List<MoveRate> getNextMoves(Board board, Pawn pawn) {
        if (pawn != null) {
            return forkJoinPool.invoke(new PawnMoveRecursive(PawnBoard.create(board), pawn));
        }
        return List.of();
    }

    public static MoveRate getNextMove(Board board, Pawn pawn) {
        return getNextMoves(board, pawn).stream()
                .filter(Objects::nonNull)
                .filter(mv -> mv.rate() > 0)
                .max(MoveRate.compareByScore())
                .orElse(null);
    }

    private PawnMoveRecursive(Board pawnBoard, Pawn pawn) {
        this(pawnBoard, pawn, true);
    }

    private PawnMoveRecursive(Board pawnBoard, Pawn pawn, boolean fork) {
        this.pawnBoard = pawnBoard;
        this.pawn = pawn;
        this.fork = fork;
    }

    @Override protected List<MoveRate> compute() {
        if (fork) {
            return ForkJoinTask.invokeAll(createMovesOf(pawnBoard, pawn)).stream()
                    .map(ForkJoinTask::join)
                    .flatMap(e -> e.stream())
                    .filter(e -> e.rate() >= 0)
                    .collect(Collectors.toList());
        }

        return List.of(getMove());
    }

    private List<PawnMoveRecursive> createMovesOf(Board board, Pawn pawn) {
        List<PawnMoveRecursive> pawnMoveRecursives = new ArrayList<>();

        pawnMoveRecursives.addAll(changePosition(PawnBoard.create(board), Position::towardLeft, pawn));
        pawnMoveRecursives.addAll(changePosition(PawnBoard.create(board), Position::towardRight, pawn));
        pawnMoveRecursives.addAll(changePosition(PawnBoard.create(board), Position::backwardLeft, pawn));
        pawnMoveRecursives.addAll(changePosition(PawnBoard.create(board), Position::backwardRight, pawn));

        return pawnMoveRecursives;
    }

    private MoveRate getMove() {
        if (pawnBoard.getPawn(pawn.nextPosition()) == null &&
                Rules.isOnBoard().test(pawn.nextPosition())) {
            Position currentPosition = pawn.currentPosition();
            int yDirection = Integer.compare(pawn.nextPosition().row(), currentPosition.row());
            boolean toward = yDirection == pawn.getType().getDirection();

            if (pawn.hasBeating()) {
                return MoveRate.create(pawn, BEAT);
            } else if (pawn.isKing()){
                return MoveRate.create(pawn, WALK);
            } else if (toward) {
                return MoveRate.create(pawn, WALK);
            }
        }
        return MoveRate.create(pawn, INVALID);
    }

    private List<PawnMoveRecursive> changePosition(Board pawnBoard, BiFunction<Position, Integer, Position> operation, Pawn pawn) {
        if (pawn.isKing()) {
            return changePositionOfKingPawn(PawnBoard.create(pawnBoard), pawn.copy(), operation);
        }
        return changePositionOfMerePawn(PawnBoard.create(pawnBoard), pawn.copy(), operation);
    }

    private List<PawnMoveRecursive> changePositionOfMerePawn(Board pawnBoard, Pawn p, BiFunction<Position, Integer, Position> operation) {
        int direction = p.getType().getDirection();

        Position position = operation.apply(p.currentPosition(), direction);
        p.nextPosition(position);

        List<PawnMoveRecursive> pawnMoveRecursiveList = new ArrayList<>();

        if (Rules.isOnBoard().negate().test(p.nextPosition())) {
            return pawnMoveRecursiveList;
        }

        if (pawnBoard.getPawn(p.nextPosition()) == null) {
            int yDirection = Integer.compare(p.nextPosition().row(), p.currentPosition().row());
            boolean toward = yDirection == direction;

            if (toward) {
                p.setMove(new Move(MoveType.MOVE));
                pawnMoveRecursiveList.add(new PawnMoveRecursive(pawnBoard, p, false));
            }
        } else if (Rules.isOpponent(pawnBoard.getPawn(p.nextPosition())).test(p)) {
            Pawn victim = pawnBoard.getPawn(p.nextPosition());
            p.setMove(new Move(MoveType.KILL, victim));

            p.nextPosition(operation.apply(p.nextPosition(), direction));
            if (pawnBoard.getPawn(p.nextPosition()) == null && Rules.isOnBoard().test(p.nextPosition())) {
                pawnMoveRecursiveList.add(new PawnMoveRecursive(PawnBoard.create(pawnBoard), p.copy(), false));
            }
        }

        return pawnMoveRecursiveList;
    }

    private List<PawnMoveRecursive> changePositionOfKingPawn(Board pawnBoard, Pawn p, BiFunction<Position, Integer, Position> operation) {
        int direction = p.getType().getDirection();

        List<PawnMoveRecursive> pawnMoveRecursiveList = new ArrayList<>();

        for (p.nextPosition(operation.apply(p.currentPosition(), direction));
             Rules.isOnBoard().test(p.nextPosition());
             p.nextPosition(operation.apply(p.nextPosition(), direction))
        ) {
            if (pawnBoard.getPawn(p.nextPosition()) == null) {
                p.setMove(new Move(MoveType.MOVE));
                pawnMoveRecursiveList.add(new PawnMoveRecursive(PawnBoard.create(pawnBoard), p.copy(), false));
            } else if (Rules.isOpponent(pawnBoard.getPawn(p.nextPosition())).test(p)) {
                Pawn victim = pawnBoard.getPawn(p.nextPosition());
                p.setMove(new Move(MoveType.KILL, victim));
                for (p.nextPosition(operation.apply(p.nextPosition(), direction));
                     pawnBoard.getPawn(p.nextPosition()) == null && Rules.isOnBoard().test(p.nextPosition());
                     p.nextPosition(operation.apply(p.nextPosition(), direction))
                ) {
                    pawnMoveRecursiveList.add(new PawnMoveRecursive(PawnBoard.create(pawnBoard), p.copy(), false));
                }
                break;
            }
        }

        return pawnMoveRecursiveList;
    }

}
