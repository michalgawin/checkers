package pl.games.checkers.ai;

import pl.games.checkers.Pawn;
import pl.games.checkers.PawnType;

import java.util.Map;
import java.util.concurrent.ForkJoinPool;

public class MoveAi {

    ForkJoinPool forkJoinPool = new ForkJoinPool(4);

    private final PawnBoard pawnBoard;
    private final PawnType pawnType;

    public MoveAi(PawnBoard pawnBoard, PawnType pawnType) {
        this.pawnBoard = pawnBoard;
        this.pawnType = pawnType;
    }

    public Pawn getBestMove() {
        Map.Entry<Integer, Pawn> bestMove = null;

        for (int y = 0; y < pawnBoard.getHeight(); y++) {
            for (int x = 0; x < pawnBoard.getWidth(); x++) {
                Pawn pawn = pawnBoard.getPawn(y, x);
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
            return forkJoinPool.invoke(new PawnMoveRecursive(pawnBoard, pawn));
        }
        return null;
    }

}
