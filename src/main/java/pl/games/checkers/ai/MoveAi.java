package pl.games.checkers.ai;

import pl.games.checkers.model.Pawn;
import pl.games.checkers.model.Board;
import pl.games.checkers.model.PawnType;

import java.util.Map;
import java.util.concurrent.ForkJoinPool;

public class MoveAi {

    private static final ForkJoinPool forkJoinPool = new ForkJoinPool(4);

    private final Board board;
    private final PawnType pawnType;

    public MoveAi(Board board, PawnType pawnType) {
        this.board = board;
        this.pawnType = pawnType;
    }

    public Pawn getBestMove() {
        Map.Entry<Integer, Pawn> bestMove = null;

        for (int y = 0; y < board.getHeight(); y++) {
            for (int x = 0; x < board.getWidth(); x++) {
                Pawn pawn = board.getPawn(y, x);
                if (pawn != null && pawn.getType() == pawnType) {
                    Map.Entry<Integer, Pawn> entry = getNextMove(pawn);
                    if (entry != null) {
                        if (bestMove == null) {
                            bestMove = entry;
                        } else if (entry.getKey() > 0 && entry.getKey() > bestMove.getKey()) {
                            bestMove = entry;
                        }
                    }
                }
            }
        }

        return bestMove != null ? bestMove.getValue() : null;
    }

    public Pawn getBestMove(Pawn pawn) {
        Map.Entry<Integer, Pawn> bestMove = getNextMove(pawn);
        return bestMove != null && bestMove.getKey() > 0 ? bestMove.getValue() : null;
    }

    private Map.Entry<Integer, Pawn> getNextMove(Pawn pawn) {
        if (pawn != null) {
            return forkJoinPool.invoke(new PawnMoveRecursive(board, pawn));
        }
        return null;
    }

}
